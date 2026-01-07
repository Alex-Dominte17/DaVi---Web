import React from "react";
import styles from "./DashboardView.module.css";
import ContextBar from "../components/ContextBar";
import HealthMonitor from "../components/HealthMonitor";
import PollenWidget from "../components/PollenWidget";
import RiskMapSidebar from "../components/RiskMapSidebar";
import { UserContext, Observation } from "../types/dashboardTypes";
import { Bell, ChevronDown, Play } from "lucide-react";

const DashboardView: React.FC = () => {
  const mockContext: UserContext = {
    userId: "Alice",
    contextId: "Ctx_123",
    eventName: "In elevator",
    placeName: "Office elevator",
    lat: 44.4268,
    lon: 26.1025,
    time: "2026-01-05T09:05:00Z",
    season: "Winter",
    sourceName: "PhoneGPS",
  };

  const mockObservation: Observation = {
    userId: "Alice",
    observationId: "Obs_456",
    sensor: "EnvAPI",
    observedProperty: "pollenCount",
    value: "180",
    unit: "grains/m3",
    time: "2026-01-05T09:05:20Z",
    sourceName: "EnvAPI",
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1>Phobia Monitor</h1>
        <p>Real-time context-aware monitoring and alerts</p>
      </header>

      <ContextBar context={mockContext} />

      <div className={styles.layout}>
        <main className={styles.mainContent}>
          <div className={styles.sectionHeader}>
            <Bell size={20} className={styles.bellIcon} />
            <h2 className={styles.sectionTitle}>Context-Aware Alerts</h2>
            <span className={styles.criticalBadge}>1 critical</span>
          </div>

          <div className={styles.alertCardCritical}>
            <div className={styles.alertIconBg}>
              <Play
                size={24}
                color="#e11d48"
                style={{ transform: "rotate(-90deg)" }}
              />
            </div>
            <div className={styles.alertBody}>
              <div className={styles.alertTitleRow}>
                <h3>Elevator Detected</h3>
                <button className={styles.closeBtn}>×</button>
              </div>
              <p>
                We know this can be tough, {mockContext.userId}. Do you want to
                start your breathing exercise?
              </p>
              <div className={styles.alertActions}>
                <button className={styles.btnPrimary}>
                  <Play size={16} fill="white" /> Start Breathing Exercise
                </button>
                <div className={styles.dropdown}>
                  <span>Why am I seeing this?</span>
                  <ChevronDown size={14} />
                </div>
              </div>
            </div>
            <span className={styles.timestamp}>11:05 AM</span>
          </div>

          <div className={styles.alertCardInfo}>
            <div className={styles.infoIconBg}>i</div>
            <div className={styles.alertBody}>
              <h3>Weekly Progress Report</h3>
              <p>
                You've completed 5 exposure exercises this week. Great progress!
              </p>
              <button className={styles.textAction}>View Details →</button>
            </div>
          </div>

          <HealthMonitor />
        </main>

        <aside className={styles.sidebar}>
          <PollenWidget observation={mockObservation} />
          <RiskMapSidebar />
        </aside>
      </div>
    </div>
  );
};

export default DashboardView;
