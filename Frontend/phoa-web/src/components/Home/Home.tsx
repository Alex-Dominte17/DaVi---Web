import React, { useEffect, useState } from "react";
import styles from "./Home.module.css";
import { ROUTES } from "../../routes/index";
import { Link } from "react-router-dom";
import {
  Brain,
  Map,
  Watch,
  Users,
  Shield,
  Zap,
  Database,
  Gamepad2,
  Link as LinkIcon,
} from "lucide-react";
import { apiService } from "../../api/apiService";

const Home: React.FC = () => {
  const [phobias, setPhobias] = useState<any[]>([]);
  const userLocalName = "Alice";

  useEffect(() => {
    const fetchPhobias = async () => {
      try {
        const data = await apiService.getUserPhobias(userLocalName);
        setPhobias(data);
      } catch (err) {
        console.error("Error loading phobias on Home:", err);
      }
    };
    fetchPhobias();
  }, []);

  const getSeverityClass = (severity: number) => {
    if (severity >= 7) return styles.severityHigh;
    if (severity >= 4) return styles.severityMedium;
    return styles.severityLow;
  };

  return (
    <div className={styles.homeContainer}>
      <section className={styles.hero}>
        <h1 className={styles.heroTitle}>
          Hello!
          <br />
          <span>Your wellness, understood.</span>
        </h1>

        <div className={styles.heroActions}>
          <Link to={ROUTES.DASHBOARD} className={styles.btnPrimary}>
            View my dashboard
          </Link>
        </div>

        {phobias.length > 0 && (
          <div className={styles.userPhobias}>
            <p className={styles.phobiaTitle}>Monitoring {phobias.length} Conditions:</p>
            <div className={styles.phobiaList}>
              {phobias.map((p, index) => (
                <div 
                  key={index} 
                  className={`${styles.phobiaBadge} ${getSeverityClass(p.severity)}`}
                  title={`Severity Level: ${p.severity}/10`}
                >
                  <Shield size={14} />
                  <span className={styles.phobiaLabel}>{p.phobiaLabel}</span>
                  <span className={styles.severityDot}></span>
                </div>
              ))}
            </div>
          </div>
        )}
      </section>

      <section className={styles.statsSection}>
        <div className={styles.statsGrid}>
          <StatCard
            icon={<Database />}
            label="Phobias Indexed"
            sub="From Wikidata & DBpedia"
          />
          <StatCard
            icon={<Brain />}
            label="Remedies"
            sub="Exercises & medications"
          />
          <StatCard
            icon={<Gamepad2 />}
            label="Serious Games"
            sub="Interactive therapy"
          />
          <StatCard
            icon={<LinkIcon />}
            label="Web Resources"
            sub="Curated external links"
          />
        </div>
        <div className={styles.endpointStatus}>
          <span>RDF Endpoint Active • Data refreshed 1 minute ago</span>
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
          DBpedia • Wikidata • Schema.org • RDF/JSON-LD
        </p>
      </footer>
    </div>
  );
};

const StatCard = ({ icon, label, sub }: any) => (
  <div className={styles.statCard}>
    <div className={styles.statIcon}>{icon}</div>
    <h3>{label}</h3>
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
