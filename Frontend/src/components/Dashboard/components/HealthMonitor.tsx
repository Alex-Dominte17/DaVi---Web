import React from "react";
import styles from "./HealthMonitor.module.css";
import { Heart, Activity, Wind } from "lucide-react";

const HealthMonitor: React.FC = () => {
  return (
    <div className={styles.monitorCard}>
      <div className={styles.header}>
        <div className={styles.titleGroup}>
          <div className={styles.watchIcon}>⌚</div>
          <div>
            <h3>Health Monitor</h3>
            <span>Smartwatch Data</span>
          </div>
        </div>
        <span className={styles.connected}>● Connected</span>
      </div>

      <div className={styles.statsGrid}>
        <div className={styles.statBox}>
          <Heart color="#ef4444" size={24} />
          <div className={styles.value}>86</div>
          <div className={styles.label}>BPM</div>
        </div>
        <div className={styles.statBox}>
          <Activity color="#22c55e" size={24} />
          <div className={styles.value}>19%</div>
          <div className={styles.label}>Calm</div>
        </div>
        <div className={styles.statBox}>
          <Wind color="#2dd4bf" size={24} />
          <div className={styles.value}>46</div>
          <div className={styles.label}>HRV (ms)</div>
        </div>
      </div>

      <div className={styles.stressSection}>
        <div className={styles.stressInfo}>
          <span>Stress Level</span>
          <span className={styles.stressStatus}>Calm</span>
        </div>
        <div className={styles.barBg}>
          <div className={styles.barFill} style={{ width: "19%" }}></div>
        </div>
      </div>
    </div>
  );
};

export default HealthMonitor;
