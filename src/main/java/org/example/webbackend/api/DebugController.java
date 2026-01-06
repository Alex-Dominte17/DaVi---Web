package org.example.webbackend.api;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@RestController
public class DebugController {

    private final RdfStore store;

    public DebugController(RdfStore store) {
        this.store = store;
    }

    @GetMapping(value = "/debug/rdf", produces = "text/turtle")
    public String dumpRdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        store.dataset().begin(ReadWrite.READ);
        try {
            RDFDataMgr.write(
                    out,
                    store.dataset().getDefaultModel(),
                    RDFFormat.TURTLE_PRETTY
            );
        } finally {
            store.dataset().end();
        }
        return out.toString(StandardCharsets.UTF_8);
    }
}
