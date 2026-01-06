package org.example.webbackend.service;


import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.example.webbackend.dto.AckRequest;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class NotificationCommandService {

    private static final String PHOA = "http://example.org/phoa#";
    private static final Property PHOA_STATUS  = ResourceFactory.createProperty(PHOA + "status");
    private static final Property PHOA_ACK_TIME = ResourceFactory.createProperty(PHOA + "ackTime");

    private final RdfStore store;

    public NotificationCommandService(RdfStore store) {
        this.store = store;
    }

    public boolean ackNotification(String notificationIdOrUri, AckRequest req) {
        String notifUri = notificationIdOrUri.startsWith("http://") || notificationIdOrUri.startsWith("https://")
                ? notificationIdOrUri
                : PHOA + notificationIdOrUri;

        String status = (req != null && req.status != null && !req.status.isBlank())
                ? req.status.trim().toLowerCase()
                : "read";

        if (!status.equals("read") && !status.equals("unread")) {
            throw new IllegalArgumentException("status must be 'read' or 'unread'");
        }

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Model m = ds.getDefaultModel();
            Resource notif = m.getResource(notifUri);

            // Existence check (no SPARQL, no nested tx)
            // If the resource has no triples, it doesn't exist in the graph.
            if (!m.contains(notif, null)) {
                ds.abort();
                return false;
            }

            // Update status
            m.removeAll(notif, PHOA_STATUS, null);
            notif.addLiteral(PHOA_STATUS, status);

            // Update ackTime
            if (status.equals("read")) {
                m.removeAll(notif, PHOA_ACK_TIME, null);
                notif.addLiteral(PHOA_ACK_TIME,
                        m.createTypedLiteral(Instant.now().toString(), XSDDatatype.XSDdateTime));
            } else {
                m.removeAll(notif, PHOA_ACK_TIME, null);
            }

            ds.commit();
            return true;
        } catch (RuntimeException e) {
            ds.abort(); // make sure write tx is closed properly on any error
            throw e;
        } finally {
            ds.end();
        }
    }
}
