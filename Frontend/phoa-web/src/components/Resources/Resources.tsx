import React, { useEffect, useState } from "react";
import styles from "./Resources.module.css";
import {
  Search,
  LayoutGrid,
  List,
  ExternalLink,
  Info,
  Loader2,
} from "lucide-react";
import { apiService } from "../../api/apiService";
import { InterventionResource } from "./types";

const Resources: React.FC = () => {
  const [resources, setResources] = useState<InterventionResource[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [activeCategory, setActiveCategory] = useState("All Resources");

  const categories = [
    "All Resources",
    "Exercises",
    "Medications",
    "Serious Games",
  ];

  useEffect(() => {
    const loadResources = async () => {
      try {
        const data = await apiService.getAllInterventions();
        setResources(data);
      } catch (err) {
        console.error("Error loading resources:", err);
      } finally {
        setLoading(false);
      }
    };
    loadResources();
  }, []);

  const getReadableType = (typeIri: string) => {
    if (typeIri.includes("Exercise")) return "Exercises";
    if (typeIri.includes("Medication")) return "Medications";
    if (typeIri.includes("Game")) return "Serious Games";
    return "Resource";
  };

  const filteredResources = resources.filter((res) => {
    const matchesSearch = res.label.toLowerCase().includes(searchTerm.toLowerCase());
    
    if (activeCategory === "All Resources") {
      return matchesSearch;
    }
    
    const resourceType = getReadableType(res.typeIri);
    return matchesSearch && resourceType === activeCategory;
  });

  const getBorderClass = (typeIri: string) => {
    if (typeIri.includes("Exercise")) return styles.borderTurq;
    if (typeIri.includes("Medication")) return styles.borderBlue;
    if (typeIri.includes("Game")) return styles.borderOrange;
    return styles.borderGreen;
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1>Remedies & Resources</h1>
        <p>Explore semantic resources from our knowledge base</p>
      </header>

      <div className={styles.searchBarRow}>
        <div className={styles.searchWrapper}>
          <Search size={20} className={styles.searchIcon} />
          <input
            type="text"
            placeholder="Search remedies, phobias, exercises..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className={styles.viewToggle}>
          <button className={styles.activeToggle}>
            <LayoutGrid size={20} />
          </button>
          <button>
            <List size={20} />
          </button>
        </div>
      </div>

      <div className={styles.filterRow}>
        {categories.map((cat) => (
          <button
            key={cat}
            className={activeCategory === cat ? styles.activeChip : styles.chip}
            onClick={() => setActiveCategory(cat)}
          >
            {cat}
          </button>
        ))}
      </div>

      <div className={styles.statusRow}>
        <span>
          Showing <strong>{filteredResources.length}</strong> resources 
          {activeCategory !== "All Resources" && ` in ${activeCategory}`}
        </span>
        <span className={styles.linkedStatus}>● RDF Data Linked</span>
      </div>

      {loading ? (
        <div className={styles.loaderBox}>
          <Loader2 className={styles.spinner} />
          <p>Loading semantic knowledge base...</p>
        </div>
      ) : (
        <div className={styles.resourceGrid}>
          {filteredResources.map((res) => (
            <article
              key={res.iri}
              className={`${styles.resourceCard} ${getBorderClass(res.typeIri)}`}
            >
              <div className={styles.cardContent}>
                <div className={styles.cardHeader}>
                  <span className={styles.badgeGray}>
                    {getReadableType(res.typeIri).replace(/s$/, '')}
                  </span>
                  <span className={styles.linkedIcon}>🔗</span>
                </div>

                <h3>{res.label}</h3>

                <div className={styles.uriBox}>
                  <label>Resource IRI</label>
                  <div className={styles.iriLink}>
                    <Info size={12} />
                    <span>{res.iri}</span>
                  </div>
                </div>

                <div
                  className={styles.cardActions}
                  style={{ marginTop: "auto" }}
                >
                  <a
                    href={res.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className={styles.btnVisit}
                  >
                    <ExternalLink size={16} />
                    Access Resource
                  </a>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
};

export default Resources;
