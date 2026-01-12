package org.example.webbackend.rdf;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;
import org.apache.jena.tdb2.TDB2Factory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class RdfStore {
    private final Dataset dataset;

    public RdfStore(@Value("${phoa.store.tdb2-dir:./data/tdb2}") String tdb2Dir) {
        this.dataset = TDB2Factory.connectDataset(tdb2Dir);
    }

    public Dataset dataset() {
        return dataset;
    }

    @PostConstruct
    public void loadInitialDataIfEmpty() {
        dataset.begin(ReadWrite.READ);
        try {
            if (!dataset.getDefaultModel().isEmpty()) return;
        } finally {
            dataset.end();
        }

        reloadBaselineIntoDefaultModel();
    }

    public void resetToInitialModel() {
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.getDefaultModel().removeAll();
            dataset.commit();
        } finally {
            dataset.end();
        }
        reloadBaselineIntoDefaultModel();
    }

    private void reloadBaselineIntoDefaultModel() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("phobia_model.ttl")) {
            if (in == null) throw new IllegalStateException("phobia_model.ttl not found in resources");

            dataset.begin(ReadWrite.WRITE);
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

    @PreDestroy
    public void close() {
        dataset.close();
    }
}
