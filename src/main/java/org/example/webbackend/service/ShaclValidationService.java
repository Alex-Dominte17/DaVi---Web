package org.example.webbackend.service;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.vocabulary.RDF;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import org.apache.jena.shacl.ShaclValidator;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.rdf.model.RDFNode;

@Service
public class ShaclValidationService {

    private final Model shapesModel;

    public ShaclValidationService() {
        this.shapesModel = ModelFactory.createDefaultModel();
        try (InputStream in = new ClassPathResource("shapes.ttl").getInputStream()) {
            RDFDataMgr.read(this.shapesModel, in, Lang.TURTLE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SHACL shapes (classpath:shapes.ttl)", e);
        }
    }

    public ValidationReport validate(Model dataModel) {
        return ShaclValidator.get().validate(shapesModel.getGraph(), dataModel.getGraph());
    }


    public String formatText(ValidationReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Conforms: ").append(report.conforms()).append("\n");

        if (report.conforms()) {
            return sb.toString();
        }

        Model m = report.getModel();

        // SHACL validation results are instances of sh:ValidationResult
        long count = m.listResourcesWithProperty(RDF.type,
                        m.createResource("http://www.w3.org/ns/shacl#ValidationResult"))
                .toList().size();

        sb.append("Violations: ").append(count).append("\n");
        sb.append("\nUse /debug/shacl.ttl to inspect full details.\n");

        return sb.toString();
    }
    private String nodeToString(RDFNode n) {
        if (n == null) return "null";
        if (n.isLiteral()) return n.asLiteral().getLexicalForm();
        if (n.isResource() && n.asResource().getURI() != null) return n.asResource().getURI();
        return n.toString();
    }

    public String reportAsTurtle(org.apache.jena.shacl.ValidationReport report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, report.getModel(), RDFFormat.TURTLE_PRETTY);
        return out.toString(StandardCharsets.UTF_8);
    }

    public Model getShapesModel() {
        return shapesModel;
    }
}

