import React from "react";
import styles from "./SPARQL.module.css";
import {
  Database,
  Copy,
  Play,
  FileCode,
  Info,
  ExternalLink,
} from "lucide-react";

const SPARQL: React.FC = () => {
  const exampleQueries = [
    "All Phobias",
    "Remedies for Claustrophobia",
    "Exercise Resources",
  ];

  const defaultQuery = `PREFIX phoa: <http://phoa-project.org/ontology#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?phobia ?label ?description
WHERE {
    ?phobia a phoa:Phobia ;
            rdfs:label ?label ;
            rdfs:comment ?description .
}
LIMIT 10`;

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1>SPARQL Playground</h1>
          <p>Query the PhoA knowledge base directly</p>
        </div>
        <button className={styles.btnDocs}>
          <Info size={18} /> SPARQL Docs
        </button>
      </header>

      <div className={styles.layout}>
        <main className={styles.editorSection}>
          <div className={styles.editorCard}>
            <div className={styles.editorHeader}>
              <div className={styles.titleGroup}>
                <Database size={20} color="#2dd4bf" />
                <h3>Query Editor</h3>
              </div>
              <div className={styles.editorActions}>
                <button className={styles.btnIconText}>
                  <Copy size={16} /> Copy
                </button>
                <button className={styles.btnRun}>
                  <Play size={16} fill="white" /> Run Query
                </button>
              </div>
            </div>

            <div className={styles.codeArea}>
              <textarea
                spellCheck="false"
                defaultValue={defaultQuery}
                className={styles.sparqlInput}
              />
            </div>

            <div className={styles.endpointBar}>
              <span>
                Endpoint:{" "}
                <a href="http://phoa-project.org/sparql">
                  http://phoa-project.org/sparql
                </a>
              </span>
            </div>
          </div>
        </main>

        <aside className={styles.sidebar}>
          <section className={styles.sideCard}>
            <h3>Example Queries</h3>
            <div className={styles.exampleList}>
              {exampleQueries.map((q, i) => (
                <button key={i} className={styles.exampleBtn}>
                  {q}
                </button>
              ))}
            </div>
          </section>

          <section className={styles.sideCard}>
            <h3>Export Profile</h3>
            <p className={styles.sideDesc}>
              Download your user profile data in semantic formats.
            </p>
            <div className={styles.exportButtons}>
              <button className={styles.btnExport}>
                <FileCode size={16} /> JSON-LD
              </button>
              <button className={styles.btnExport}>
                <Database size={16} /> Turtle
              </button>
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
};

export default SPARQL;
