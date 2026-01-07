import React from "react";
import styles from "./Resources.module.css";
import { Search, LayoutGrid, List } from "lucide-react";

const Resources: React.FC = () => {
  const categories = [
    "All Resources",
    "Exercises",
    "Medications",
    "Serious Games",
    "Web Resources",
  ];

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
        {categories.map((cat, index) => (
          <button
            key={cat}
            className={index === 0 ? styles.activeChip : styles.chip}
          >
            {cat}
          </button>
        ))}
      </div>

      <div className={styles.statusRow}>
        <span>
          Showing <strong>6</strong> resources
        </span>
        <span className={styles.linkedStatus}>● RDF Data Linked</span>
      </div>

      <div className={styles.resourceGrid}>
        <article className={`${styles.resourceCard} ${styles.borderTurq}`}>
          <div className={styles.cardHeader}>
            <span className={styles.badgeGray}>Exercise</span>
            <span className={styles.badgeGreen}>easy</span>
            <span className={styles.metaText}>5 min</span>
          </div>
          <h3>4-7-8 Breathing Technique</h3>
          <p>
            A calming breathing pattern that helps reduce anxiety and panic
            symptoms. Inhal...
          </p>
          <div className={styles.uriBox}>
            <label>Resource URI</label>
            <a href="#">http://phoa-project.org/remedy/breathing-478</a>
          </div>
          <div className={styles.tagCloud}>
            <span>Claustrophobia</span>
            <span>Panic Disorder</span>
            <span>Anxiety</span>
          </div>
        </article>

        <article className={`${styles.resourceCard} ${styles.borderOrange}`}>
          <div className={styles.cardHeader}>
            <span className={styles.badgeGray}>Serious Game</span>
            <span className={styles.badgeAmber}>medium</span>
            <span className={styles.metaText}>15 min</span>
          </div>
          <h3>Spider Desensitization Game</h3>
          <p>
            A gradual exposure game that helps users overcome arachnophobia
            through controll...
          </p>
          <div className={styles.uriBox}>
            <label>Resource URI</label>
            <a href="#">http://phoa-project.org/game/spider-exposure</a>
          </div>
          <div className={styles.tagCloud}>
            <span>Arachnophobia</span>
          </div>
        </article>

        <article className={`${styles.resourceCard} ${styles.borderBlue}`}>
          <div className={styles.cardHeader}>
            <span className={styles.badgeGray}>Medication</span>
          </div>
          <h3>Propranolol Information</h3>
          <p>
            Beta-blocker medication commonly used to manage physical symptoms of
            anxiety suc...
          </p>
          <div className={styles.uriBox}>
            <label>Resource URI</label>
            <a href="#">http://phoa-project.org/medication/propranolol</a>
          </div>
          <div className={styles.tagCloud}>
            <span>Social Phobia</span>
            <span>Performance Anxiety</span>
          </div>
        </article>
      </div>
    </div>
  );
};

export default Resources;
