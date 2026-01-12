import React, { useEffect, useState } from "react";
import styles from "./HealthMonitor.module.css";
import { Activity, Heart, Mountain, ShieldAlert } from "lucide-react";
import { Observation } from "../types/dashboardTypes";

interface HealthMonitorProps {
  heartRate: Observation | null;
  altitude: Observation | null;
  fearRating: Observation | null;
}

const HealthMonitor: React.FC<HealthMonitorProps> = ({ heartRate, altitude, fearRating }) => {
  return (
    <div className={styles.monitorCard}>
      <div className={styles.header}>
        <Activity size={20} color="#ef4444" />
        <h3>Live Health Metrics</h3>
      </div>
      
      <div className={styles.metricsGrid}>
        <div className={styles.metricItem}>
          <div className={styles.metricIconBox} style={{ backgroundColor: "#fef2f2" }}>
            <Heart size={20} color="#ef4444" className={styles.pulseIcon} />
          </div>
          <div className={styles.metricData}>
            <span className={styles.label}>Heart Rate</span>
            <div className={styles.valueRow}>
              <span className={styles.value}>{heartRate?.value || "--"}</span>
              <span className={styles.unit}>{heartRate?.unit || "bpm"}</span>
            </div>
          </div>
        </div>

        <div className={styles.metricItem}>
          <div className={styles.metricIconBox} style={{ backgroundColor: "#f0f9ff" }}>
            <Mountain size={20} color="#0ea5e9" />
          </div>
          <div className={styles.metricData}>
            <span className={styles.label}>Altitude</span>
            <div className={styles.valueRow}>
              <span className={styles.value}>{altitude?.value || "--"}</span>
              <span className={styles.unit}>{altitude?.unit || "m"}</span>
            </div>
          </div>
        </div>

        <div className={styles.metricItem}>
          <div className={styles.metricIconBox} style={{ backgroundColor: "#fff7ed" }}>
            <ShieldAlert size={20} color="#f97316" />
          </div>
          <div className={styles.metricData}>
            <span className={styles.label}>Fear Level</span>
            <div className={styles.valueRow}>
              <span className={styles.value}>{fearRating?.value || "--"}</span>
              <span className={styles.unit}>/ 10</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HealthMonitor;
