package org.example.webbackend.api;


import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.example.webbackend.rdf.RdfStore;
import org.example.webbackend.service.ShaclValidationService;
import org.apache.jena.shacl.ValidationReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class ShaclDebugController {

    private final RdfStore store;
    private final ShaclValidationService shacl;

    public ShaclDebugController(RdfStore store, ShaclValidationService shacl) {
        this.store = store;
        this.shacl = shacl;
    }

    // Human-readable report (easy for you to debug quickly)
    @GetMapping(value = "/shacl", produces = "text/plain")
    public String validateDatasetText() {
        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try {
            Model data = ds.getDefaultModel();
            ValidationReport report = shacl.validate(data);
            return shacl.formatText(report);
        } finally {
            ds.end();
        }
    }

    // Optional: machine-readable report (RDF Turtle)
    @GetMapping(value = "/shacl.ttl", produces = "text/turtle")
    public String validateDatasetTurtle() {
        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try {
            Model data = ds.getDefaultModel();
            ValidationReport report = shacl.validate(data);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            RDFDataMgr.write(out, report.getModel(), RDFFormat.TURTLE_PRETTY);
            return out.toString(StandardCharsets.UTF_8);
        } finally {
            ds.end();
        }
    }

    // Optional: quick JSON summary (conforms + count of violations)
    @GetMapping(value = "/shacl.json", produces = "application/json")
    public ResponseEntity<?> validateDatasetJson() {
        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try {
            Model data = ds.getDefaultModel();
            ValidationReport report = shacl.validate(data);

            long violations = report.getEntries().stream()
                    .filter(e -> e.severity() != null) // entries are violations typically
                    .count();

            return ResponseEntity.ok(Map.of(
                    "conforms", report.conforms(),
                    "violations", violations
            ));
        } finally {
            ds.end();
        }
    }

    @GetMapping(value = "/shacl/summary", produces = "application/json")
    public ResponseEntity<?> shaclSummary() {
        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try {
            Model data = ds.getDefaultModel();
            var report = shacl.validate(data);
            Model shapes = shacl.getShapesModel();

            // Count shapes (NodeShapes)
            long nodeShapes = shapes.listResourcesWithProperty(
                    RDF.type, shapes.createResource("http://www.w3.org/ns/shacl#NodeShape")
            ).toList().size();

            // Count violations in report
            long results = report.getModel().listResourcesWithProperty(
                    RDF.type, report.getModel().createResource("http://www.w3.org/ns/shacl#ValidationResult")
            ).toList().size();

            // Count how many foaf:Person and sosa:Observation nodes exist in data
            long persons = data.listResourcesWithProperty(
                    RDF.type, data.createResource("http://xmlns.com/foaf/0.1/Person")
            ).toList().size();

            long observations = data.listResourcesWithProperty(
                    RDF.type, data.createResource("http://www.w3.org/ns/sosa/Observation")
            ).toList().size();

            return ResponseEntity.ok(Map.of(
                    "conforms", report.conforms(),
                    "nodeShapesLoaded", nodeShapes,
                    "validationResults", results,
                    "dataCounts", Map.of(
                            "foaf:Person", persons,
                            "sosa:Observation", observations
                    )
            ));
        } finally {
            ds.end();
        }
    }

}

