import React from "react";
import styles from "./Home.module.css";
import { ROUTES } from "../../routes/index";
import { Link } from "react-router-dom";
import {
  Search,
  Brain,
  Map,
  Watch,
  Users,
  Shield,
  Zap,
  Database,
  Activity,
  Gamepad2,
  MapPin,
  Thermometer,
  Link as LinkIcon,
} from "lucide-react";

const Home: React.FC = () => {
  return (
    <div className={styles.homeContainer}>
      <div className={styles.contextHeader}>
        <div className={styles.contextItem}>
          <MapPin size={16} className={styles.iconTurq} />
          <span>Iași, Romania</span>
        </div>
        <span className={styles.separator}>•</span>
        <div className={styles.contextItem}>
          <Zap size={16} className={styles.iconTurq} />
          <span>Spring</span>
        </div>
        <span className={styles.separator}>•</span>
        <div className={styles.contextItem}>
          <Thermometer size={16} className={styles.iconTurq} />
          <span>18°C</span>
        </div>
      </div>

      <section className={styles.hero}>
        <h1 className={styles.heroTitle}>
          Hello, Andrei!
          <br />
          <span>Your wellness, understood.</span>
        </h1>

        <div className={styles.heroActions}>
          <Link to={ROUTES.DASHBOARD} className={styles.btnPrimary}>
            View my dashboard
          </Link>
          <button className={styles.btnSecondary}>Explore Remedies</button>
        </div>

        <div className={styles.heroTags}>
          <span className={styles.tagRed}>Pollen: high</span>
          <span className={styles.tagGray}>High pollen levels detected</span>
          <span className={styles.tagGray}>Tree blossoming in your area</span>
        </div>
      </section>

      <div className={styles.searchSection}>
        <div className={styles.searchWrapper}>
          <Search className={styles.searchIcon} size={20} />
          <input
            type="text"
            placeholder="Search phobias, remedies, resources..."
          />
          <kbd className={styles.searchKbd}>⌘K</kbd>
        </div>
      </div>

      <section className={styles.statsSection}>
        <h2 className={styles.sectionTitle}>Knowledge Base Statistics</h2>
        <p className={styles.sectionSubtitle}>
          Real-time data from our semantic knowledge graph
        </p>

        <div className={styles.statsGrid}>
          <StatCard
            icon={<Database />}
            value="2,847"
            label="Phobias Indexed"
            sub="From Wikidata & DBpedia"
          />
          <StatCard
            icon={<Brain />}
            value="1,234"
            label="Remedies"
            sub="Exercises & medications"
          />
          <StatCard
            icon={<Gamepad2 />}
            value="89"
            label="Serious Games"
            sub="Interactive therapy"
          />
          <StatCard
            icon={<LinkIcon />}
            value="456"
            label="Web Resources"
            sub="Curated external links"
          />
          <StatCard
            icon={<Activity />}
            value="12.4K"
            label="SPARQL Queries"
            sub="Processed this month"
          />
        </div>
        <div className={styles.endpointStatus}>
          <span>RDF Endpoint Active • Data refreshed 5 minutes ago</span>
        </div>
      </section>

      <section className={styles.featuresSection}>
        <div className={styles.featuresHeader}>
          <h2 className={styles.featuresMainTitle}>
            Semantic Web Meets
            <br />
            <span>Personalized Wellness</span>
          </h2>
          <p className={styles.featuresMainSubtitle}>
            PhoA combines linked data technologies with context-aware computing
            to provide intelligent, personalized support for managing phobias.
          </p>
        </div>

        <div className={styles.featuresGrid}>
          <FeatureCard
            icon={<Brain />}
            title="Semantic Knowledge"
            desc="Powered by DBpedia, Wikidata, and Schema.org medical types for accurate, linked health data."
          />
          <FeatureCard
            icon={<Map />}
            title="Context-Aware Alerts"
            desc="Location-based triggers for phobias like claustrophobia or agoraphobia based on your environment."
          />
          <FeatureCard
            icon={<Watch />}
            title="Multi-Device Support"
            desc="Connect smartwatches and mobile devices for real-time health monitoring and stress detection."
          />
          <FeatureCard
            icon={<Users />}
            title="Social Entourage"
            desc="Build a trusted circle of family and friends who can receive alerts and provide support."
          />
          <FeatureCard
            icon={<Shield />}
            title="Rule-Based System"
            desc="Smart IF-THEN rules: if pollen is high and trees are blossoming, receive personalized alerts."
          />
          <FeatureCard
            icon={<Zap />}
            title="Serious Games"
            desc="Interactive desensitization exercises and mini-games designed for gradual exposure therapy."
          />
        </div>
      </section>

      <footer className={styles.footer}>
        <p>
          PhoA - Phobia-related Web Assistant • Powered by Semantic Web
          Technologies
        </p>
        <p className={styles.techStack}>
          DBpedia • Wikidata • Schema.org • SPARQL • RDF/JSON-LD
        </p>
      </footer>
    </div>
  );
};

const StatCard = ({ icon, value, label, sub }: any) => (
  <div className={styles.statCard}>
    <div className={styles.statIcon}>{icon}</div>
    <h3>{value}</h3>
    <p>
      <strong>{label}</strong>
    </p>
    <span>{sub}</span>
  </div>
);

const FeatureCard = ({ icon, title, desc }: any) => (
  <div className={styles.featureCard}>
    <div className={styles.featureIcon}>{icon}</div>
    <h3>{title}</h3>
    <p>{desc}</p>
  </div>
);

export default Home;
