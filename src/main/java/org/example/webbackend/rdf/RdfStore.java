package org.example.webbackend.rdf;

import jakarta.annotation.PostConstruct;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class RdfStore {
    private final Dataset dataset = DatasetFactory.createTxnMem();

    public Dataset dataset() {
        return dataset;
    }

    @PostConstruct
    public void loadInitialData() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("phobia_model.ttl")) {
            if (in == null) throw new IllegalStateException("phobia_model.ttl not found in resources");
            dataset.begin();
            try {
                RDFDataMgr.read(dataset.getDefaultModel(), in, Lang.TURTLE);
                dataset.commit();
            } finally {
                dataset.end();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TTL", e);
        }
    }
}

