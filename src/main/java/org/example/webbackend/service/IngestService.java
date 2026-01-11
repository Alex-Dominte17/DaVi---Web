package org.example.webbackend.service;

import org.apache.jena.shacl.ValidationReport;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.example.webbackend.api.error.ShaclValidationException;
import org.example.webbackend.dto.ContextRequest;
import org.example.webbackend.dto.ObservationRequest;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

@Service
public class IngestService {

    private static final String PHOA_NS   = "http://example.org/phoa#";
    private static final String SCHEMA_NS = "https://schema.org/";
    private static final String TIME_NS   = "http://www.w3.org/2006/time#";
    private static final String SOSA_NS   = "http://www.w3.org/ns/sosa/";

    // Classes
    private static final Resource PHOA_CONTEXT  = ResourceFactory.createResource(PHOA_NS + "Context");
    private static final Resource PHOA_SEASON   = ResourceFactory.createResource(PHOA_NS + "Season");
    private static final Resource PHOA_DATASRC  = ResourceFactory.createResource(PHOA_NS + "DataSource");

    private static final Resource SCHEMA_EVENT  = ResourceFactory.createResource(SCHEMA_NS + "Event");
    private static final Resource SCHEMA_PLACE  = ResourceFactory.createResource(SCHEMA_NS + "Place");
    private static final Resource SCHEMA_GEOCO  = ResourceFactory.createResource(SCHEMA_NS + "GeoCoordinates");

    private static final Resource TIME_INSTANT  = ResourceFactory.createResource(TIME_NS + "Instant");
    private static final Property TIME_IN_XSD   = ResourceFactory.createProperty(TIME_NS + "inXSDDateTime");

    private static final Resource SOSA_OBS      = ResourceFactory.createResource(SOSA_NS + "Observation");
    private static final Resource SOSA_SENSOR   = ResourceFactory.createResource(SOSA_NS + "Sensor");
    private static final Property SOSA_MADE_BY  = ResourceFactory.createProperty(SOSA_NS + "madeBySensor");
    private static final Property SOSA_OBS_PROP = ResourceFactory.createProperty(SOSA_NS + "observedProperty");
    private static final Property SOSA_SIMPLE   = ResourceFactory.createProperty(SOSA_NS + "hasSimpleResult");
    private static final Property SOSA_TIME     = ResourceFactory.createProperty(SOSA_NS + "resultTime");

    // Phoa properties
    private static final Property PHOA_HAS_CONTEXT      = ResourceFactory.createProperty(PHOA_NS + "hasContext");
    private static final Property PHOA_CONTEXT_EVENT    = ResourceFactory.createProperty(PHOA_NS + "contextEvent");
    private static final Property PHOA_CONTEXT_LOCATION = ResourceFactory.createProperty(PHOA_NS + "contextLocation");
    private static final Property PHOA_CONTEXT_SEASON   = ResourceFactory.createProperty(PHOA_NS + "contextSeason");
    private static final Property PHOA_CONTEXT_TIME     = ResourceFactory.createProperty(PHOA_NS + "contextTime");
    private static final Property PHOA_REPORTED_BY      = ResourceFactory.createProperty(PHOA_NS + "reportedBy");

    private static final Property PHOA_HAS_OBSERVATION  = ResourceFactory.createProperty(PHOA_NS + "hasObservation");

    // schema properties
    private static final Property SCHEMA_NAME      = ResourceFactory.createProperty(SCHEMA_NS + "name");
    private static final Property SCHEMA_GEO       = ResourceFactory.createProperty(SCHEMA_NS + "geo");
    private static final Property SCHEMA_LAT       = ResourceFactory.createProperty(SCHEMA_NS + "latitude");
    private static final Property SCHEMA_LON       = ResourceFactory.createProperty(SCHEMA_NS + "longitude");
    private static final Property SCHEMA_UNIT_TEXT = ResourceFactory.createProperty(SCHEMA_NS + "unitText");

    private final RdfStore store;
    private final ShaclValidationService shaclValidationService;


    public IngestService(RdfStore store, ShaclValidationService shaclValidationService) {
        this.store = store;
        this.shaclValidationService = shaclValidationService;
    }

    public String ingestContext(ContextRequest req) {
        if (req == null || req.userId == null || req.contextId == null) {
            throw new IllegalArgumentException("userId and contextId are required");
        }

        var ds = store.dataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Model m = ds.getDefaultModel();

            Resource user = m.createResource(PHOA_NS + req.userId);

            Resource ctx = m.createResource(PHOA_NS + req.contextId);
            ctx.addProperty(RDF.type, PHOA_CONTEXT);

            // Link user -> context
            user.addProperty(PHOA_HAS_CONTEXT, ctx);

            // Event
            if (req.eventName != null && !req.eventName.isBlank()) {
                Resource ev = m.createResource(PHOA_NS + req.contextId + "_Event");
                ev.addProperty(RDF.type, SCHEMA_EVENT);
                ev.addProperty(SCHEMA_NAME, req.eventName);
                ctx.addProperty(PHOA_CONTEXT_EVENT, ev);
            }

            // Place + geo
            if (req.placeName != null && !req.placeName.isBlank()) {
                Resource place = m.createResource(PHOA_NS + req.contextId + "_Place");
                place.addProperty(RDF.type, SCHEMA_PLACE);
                place.addProperty(SCHEMA_NAME, req.placeName);

                if (req.lat != null && req.lon != null) {
                    Resource geo = m.createResource(); // blank node
                    geo.addProperty(RDF.type, SCHEMA_GEOCO);
                    geo.addLiteral(SCHEMA_LAT, m.createTypedLiteral(req.lat));
                    geo.addLiteral(SCHEMA_LON, m.createTypedLiteral(req.lon));
                    place.addProperty(SCHEMA_GEO, geo);
                }

                ctx.addProperty(PHOA_CONTEXT_LOCATION, place);
            }

            // Season (create if not present)
            if (req.season != null && !req.season.isBlank()) {
                String seasonId = req.season.trim().replaceAll("\\s+", "");
                Resource season = m.createResource(PHOA_NS + seasonId);
                season.addProperty(RDF.type, PHOA_SEASON);
                if (!season.hasProperty(RDFS.label)) {
                    season.addProperty(RDFS.label, req.season);
                }
                ctx.addProperty(PHOA_CONTEXT_SEASON, season);
            }

            // Time (Instant)
            if (req.time != null && !req.time.isBlank()) {
                Resource instant = m.createResource(PHOA_NS + req.contextId + "_Time");
                instant.addProperty(RDF.type, TIME_INSTANT);
                instant.addProperty(TIME_IN_XSD, m.createTypedLiteral(req.time, XSDDatatype.XSDdateTime));
                ctx.addProperty(PHOA_CONTEXT_TIME, instant);
            }

            // Data source (e.g. PhoneGPS)
            if (req.sourceName != null && !req.sourceName.isBlank()) {
                String srcId = req.sourceName.trim().replaceAll("\\s+", "");
                Resource src = m.createResource(PHOA_NS + srcId);
                src.addProperty(RDF.type, PHOA_DATASRC);
                if (!src.hasProperty(RDFS.label)) {
                    src.addProperty(RDFS.label, req.sourceName);
                }
                ctx.addProperty(PHOA_REPORTED_BY, src);
            }

            ds.commit();
            return ctx.getURI();
        } finally {
            ds.end();
        }
    }

//    public String ingestObservation(ObservationRequest req) {
//        if (req == null || req.userId == null || req.observationId == null) {
//            throw new IllegalArgumentException("userId and observationId are required");
//        }
//
//        var ds = store.dataset();
//        ds.begin(ReadWrite.WRITE);
//        try {
//            Model m = ds.getDefaultModel();
//
//            Resource user = m.createResource(PHOA_NS + req.userId);
//
//            Resource obs = m.createResource(PHOA_NS + req.observationId);
//            obs.addProperty(RDF.type, SOSA_OBS);
//
//            // Link user -> observation
//            user.addProperty(PHOA_HAS_OBSERVATION, obs);
//
//            // Sensor (create if not present)
//            if (req.sensor != null && !req.sensor.isBlank()) {
//                Resource sensor = m.createResource(PHOA_NS + req.sensor.trim().replaceAll("\\s+", ""));
//                sensor.addProperty(RDF.type, SOSA_SENSOR);
//                obs.addProperty(SOSA_MADE_BY, sensor);
//            }
//
//            // observedProperty: allow full URI or short name (default to schema)
//            if (req.observedProperty != null && !req.observedProperty.isBlank()) {
//                String p = req.observedProperty.trim();
//                String propUri = p.startsWith("http://") || p.startsWith("https://")
//                        ? p
//                        : (SCHEMA_NS + p);
//                obs.addProperty(SOSA_OBS_PROP, m.createResource(propUri));
//            }
//
//            // value: try numeric, else string
//            if (req.value != null && !req.value.isBlank()) {
//                Literal valueLit = tryParseNumberLiteral(m, req.value.trim());
//                obs.addProperty(SOSA_SIMPLE, valueLit);
//            }
//
//            // time
//            if (req.time != null && !req.time.isBlank()) {
//                obs.addProperty(SOSA_TIME, m.createTypedLiteral(req.time, XSDDatatype.XSDdateTime));
//            }
//
//            // unit (optional): store on observation as schema:unitText (simple MVP)
//            if (req.unit != null && !req.unit.isBlank()) {
//                obs.addProperty(SCHEMA_UNIT_TEXT, req.unit);
//            }
//
//            // (Optional) sourceName could be modeled similarly to DataSource; for MVP you can ignore or add later.
//
//            ds.commit();
//            return obs.getURI();
//        } finally {
//            ds.end();
//        }
//    }


    public String ingestObservation(ObservationRequest req) {
        if (req == null || req.userId == null || req.observationId == null) {
            throw new IllegalArgumentException("userId and observationId are required");
        }

        // 1) Build triples in a temporary model
        Model tmp = ModelFactory.createDefaultModel();

        Resource user = tmp.createResource(PHOA_NS + req.userId);

        Resource obs = tmp.createResource(PHOA_NS + req.observationId);
        obs.addProperty(RDF.type, SOSA_OBS);

        // Link user -> observation
        user.addProperty(PHOA_HAS_OBSERVATION, obs);

        // Sensor (create if not present)
        if (req.sensor != null && !req.sensor.isBlank()) {
            Resource sensor = tmp.createResource(PHOA_NS + req.sensor.trim().replaceAll("\\s+", ""));
            sensor.addProperty(RDF.type, SOSA_SENSOR);
            obs.addProperty(SOSA_MADE_BY, sensor);
        }

        // observedProperty: allow full URI or short name
        if (req.observedProperty != null && !req.observedProperty.isBlank()) {
            String p = req.observedProperty.trim();
            String propUri = p.startsWith("http://") || p.startsWith("https://")
                    ? p
                    : (SCHEMA_NS + p); // you can later change to PHOA_NS if you want phoa:heartRate etc.
            obs.addProperty(SOSA_OBS_PROP, tmp.createResource(propUri));
        }

        // value: try numeric, else string
        if (req.value != null && !req.value.isBlank()) {
            Literal valueLit = tryParseNumberLiteral(tmp, req.value.trim());
            obs.addProperty(SOSA_SIMPLE, valueLit);
        }

        // time
        if (req.time != null && !req.time.isBlank()) {
            obs.addProperty(SOSA_TIME, tmp.createTypedLiteral(req.time, XSDDatatype.XSDdateTime));
        }

        // unit (optional)
        if (req.unit != null && !req.unit.isBlank()) {
            obs.addProperty(SCHEMA_UNIT_TEXT, req.unit);
        }

        // 2) SHACL validate the temp model BEFORE writing
        ValidationReport report = shaclValidationService.validate(tmp);
        if (!report.conforms()) {
            String ttl = shaclValidationService.reportAsTurtle(report);
            throw new ShaclValidationException("SHACL validation failed for observation", ttl);
        }

        // 3) Only if valid: add to dataset and commit
        var ds = store.dataset();
        ds.begin(ReadWrite.WRITE);
        try {
            ds.getDefaultModel().add(tmp);
            ds.commit();
            return obs.getURI();
        } catch (RuntimeException e) {
            ds.abort();
            throw e;
        } finally {
            ds.end();
        }
    }


    private Literal tryParseNumberLiteral(Model m, String raw) {
        try {
            if (raw.contains(".")) {
                double d = Double.parseDouble(raw);
                return m.createTypedLiteral(d);
            } else {
                long l = Long.parseLong(raw);
                return m.createTypedLiteral(l);
            }
        } catch (Exception ignored) {
            return m.createLiteral(raw);
        }
    }
}
