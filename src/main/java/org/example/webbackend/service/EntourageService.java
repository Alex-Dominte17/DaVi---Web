package org.example.webbackend.service;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.example.webbackend.dto.EntourageContactPatchRequest;
import org.example.webbackend.dto.EntourageContactRequest;
import org.example.webbackend.dto.EntourageContactResponse;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class EntourageService {

    private static final String PHOA_NS = "http://example.org/phoa#";
    private static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";

    private static final Resource FOAF_PERSON = ResourceFactory.createResource(FOAF_NS + "Person");
    private static final Property FOAF_NAME   = ResourceFactory.createProperty(FOAF_NS + "name");
    private static final Property FOAF_MBOX   = ResourceFactory.createProperty(FOAF_NS + "mbox");

    private static final Property PHOA_HAS_ENTOURAGE = ResourceFactory.createProperty(PHOA_NS + "hasEntourageContact");
    private static final Property PHOA_RELATIONSHIP  = ResourceFactory.createProperty(PHOA_NS + "relationship");
    private static final Property PHOA_ALERTS_ENABLED= ResourceFactory.createProperty(PHOA_NS + "alertsEnabled");

    private final RdfStore store;

    public EntourageService(RdfStore store) {
        this.store = store;
    }

    public List<EntourageContactResponse> listEntourage(String userId) {
        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try {
            Model m = ds.getDefaultModel();
            Resource user = m.getResource(PHOA_NS + userId);

            List<EntourageContactResponse> out = new ArrayList<>();
            StmtIterator it = m.listStatements(user, PHOA_HAS_ENTOURAGE, (RDFNode) null);

            while (it.hasNext()) {
                RDFNode node = it.nextStatement().getObject();
                if (!node.isResource()) continue;
                Resource c = node.asResource();

                EntourageContactResponse r = new EntourageContactResponse();
                r.contactUri = c.getURI();
                r.contactId = localNameFromUri(c.getURI());

                Statement sName = c.getProperty(FOAF_NAME);
                if (sName != null && sName.getObject().isLiteral()) r.name = sName.getString();

                Statement sRel = c.getProperty(PHOA_RELATIONSHIP);
                if (sRel != null && sRel.getObject().isLiteral()) r.relationship = sRel.getString();

                Statement sAlerts = c.getProperty(PHOA_ALERTS_ENABLED);
                if (sAlerts != null && sAlerts.getObject().isLiteral()) {
                    r.alertsEnabled = sAlerts.getBoolean();
                } else {
                    r.alertsEnabled = true; // default for older contacts
                }

                Statement sMbox = c.getProperty(FOAF_MBOX);
                if (sMbox != null && sMbox.getObject().isResource()) {
                    String uri = sMbox.getResource().getURI(); // mailto:...
                    r.email = uri.startsWith("mailto:") ? uri.substring("mailto:".length()) : uri;
                }

                out.add(r);
            }

            // Optional: sort by name
            out.sort(Comparator.comparing(a -> a.name == null ? "" : a.name.toLowerCase()));
            return out;
        } finally {
            ds.end();
        }
    }

    public EntourageContactResponse addContact(String userId, EntourageContactRequest req) {
        if (req == null || req.name == null || req.name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        String contactId = (req.contactId != null && !req.contactId.isBlank())
                ? req.contactId.trim()
                : generateContactId(userId, req.name);

        String contactUri = PHOA_NS + contactId;

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Model m = ds.getDefaultModel();
            Resource user = m.getResource(PHOA_NS + userId);
            Resource contact = m.getResource(contactUri);

            // create contact node
            contact.addProperty(RDF.type, FOAF_PERSON);
            m.removeAll(contact, FOAF_NAME, null);
            contact.addProperty(FOAF_NAME, req.name.trim());

            // email -> foaf:mbox <mailto:...>
            if (req.email != null && !req.email.isBlank()) {
                String mailto = "mailto:" + req.email.trim();
                m.removeAll(contact, FOAF_MBOX, null);
                contact.addProperty(FOAF_MBOX, m.createResource(mailto));
            }

            if (req.relationship != null && !req.relationship.isBlank()) {
                m.removeAll(contact, PHOA_RELATIONSHIP, null);
                contact.addLiteral(PHOA_RELATIONSHIP, req.relationship.trim());
            }

            boolean alerts = req.alertsEnabled == null ? true : req.alertsEnabled;
            m.removeAll(contact, PHOA_ALERTS_ENABLED, null);
            contact.addLiteral(PHOA_ALERTS_ENABLED, m.createTypedLiteral(alerts, XSDDatatype.XSDboolean));

            // link user -> contact (avoid duplicates)
            if (!m.contains(user, PHOA_HAS_ENTOURAGE, contact)) {
                user.addProperty(PHOA_HAS_ENTOURAGE, contact);
            }

            ds.commit();

            EntourageContactResponse out = new EntourageContactResponse();
            out.contactId = contactId;
            out.contactUri = contactUri;
            out.name = req.name.trim();
            out.email = (req.email == null || req.email.isBlank()) ? null : req.email.trim();
            out.relationship = (req.relationship == null || req.relationship.isBlank()) ? null : req.relationship.trim();
            out.alertsEnabled = alerts;
            return out;
        } catch (RuntimeException e) {
            ds.abort();
            throw e;
        } finally {
            ds.end();
        }
    }

    public boolean removeContact(String userId, String contactIdOrUri) {
        String contactUri = toUri(contactIdOrUri);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Model m = ds.getDefaultModel();
            Resource user = m.getResource(PHOA_NS + userId);
            Resource contact = m.getResource(contactUri);

            // tell if link existed
            boolean existed = m.contains(user, PHOA_HAS_ENTOURAGE, contact);

            // remove link user->contact
            m.removeAll(user, PHOA_HAS_ENTOURAGE, contact);

            // optional: also delete contact node data (safe for MVP)
            m.removeAll(contact, null, null);

            ds.commit();
            return existed;
        } catch (RuntimeException e) {
            ds.abort();
            throw e;
        } finally {
            ds.end();
        }
    }

    public boolean patchContact(String userId, String contactIdOrUri, EntourageContactPatchRequest req) {
        String contactUri = toUri(contactIdOrUri);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Model m = ds.getDefaultModel();
            Resource user = m.getResource(PHOA_NS + userId);
            Resource contact = m.getResource(contactUri);

            // Must be linked to that user (avoid editing random nodes)
            if (!m.contains(user, PHOA_HAS_ENTOURAGE, contact)) {
                ds.abort();
                return false;
            }

            if (req == null) {
                ds.commit();
                return true;
            }

            if (req.name != null) {
                m.removeAll(contact, FOAF_NAME, null);
                contact.addProperty(FOAF_NAME, req.name.trim());
            }

            if (req.email != null) {
                m.removeAll(contact, FOAF_MBOX, null);
                if (!req.email.isBlank()) {
                    contact.addProperty(FOAF_MBOX, m.createResource("mailto:" + req.email.trim()));
                }
            }

            if (req.relationship != null) {
                m.removeAll(contact, PHOA_RELATIONSHIP, null);
                if (!req.relationship.isBlank()) {
                    contact.addLiteral(PHOA_RELATIONSHIP, req.relationship.trim());
                }
            }

            if (req.alertsEnabled != null) {
                m.removeAll(contact, PHOA_ALERTS_ENABLED, null);
                contact.addLiteral(PHOA_ALERTS_ENABLED,
                        m.createTypedLiteral(req.alertsEnabled, XSDDatatype.XSDboolean));
            }

            ds.commit();
            return true;
        } catch (RuntimeException e) {
            ds.abort();
            throw e;
        } finally {
            ds.end();
        }
    }

    private String toUri(String idOrUri) {
        if (idOrUri == null) throw new IllegalArgumentException("contact id required");
        return (idOrUri.startsWith("http://") || idOrUri.startsWith("https://"))
                ? idOrUri
                : PHOA_NS + idOrUri;
    }

    private String generateContactId(String userId, String name) {
        // stable-ish, readable IDs
        String base = "Contact_" + userId + "_" + slug(name);
        return base.length() > 120 ? base.substring(0, 120) : base;
    }

    private String slug(String s) {
        String trimmed = s.trim().replaceAll("\\s+", "");
        String safe = trimmed.replaceAll("[^A-Za-z0-9_]", "");
        if (safe.isBlank()) safe = "Contact";
        return safe;
    }

    private String localNameFromUri(String uri) {
        int idx = uri.lastIndexOf('#');
        return idx >= 0 ? uri.substring(idx + 1) : uri;
    }
}
