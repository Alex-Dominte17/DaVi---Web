import React from "react";
import styles from "./RiskMapSidebar.module.css";
import { MapPin, Info, AlertTriangle, ShieldCheck } from "lucide-react";

const RiskMapSidebar: React.FC = () => {
  const zones = [
    {
      id: 1,
      name: "Shopping Mall",
      desc: "Crowded spaces - Agoraphobia trigger",
      type: "caution",
      tag: "Agoraphobia",
    },
    {
      id: 2,
      name: "Elevator Zone",
      desc: "Small enclosed space - Claustrophobia trigger",
      type: "high",
      tag: "Claustrophobia",
    },
    {
      id: 3,
      name: "Park Area",
      desc: "Open space, low trigger risk",
      type: "safe",
      tag: "Park",
    },
  ];

  return (
    <div className={styles.sidebar}>
      <div className={styles.mapCard}>
        <div className={styles.mapHeader}>
          <MapPin size={18} className={styles.mapIcon} />
          <div>
            <h3>Risk Map</h3>
            <span>Nearby trigger zones</span>
          </div>
        </div>

        <div className={styles.radarContainer}>
          <div className={styles.radarCircle}>
            <div
              className={styles.dot}
              style={{ top: "20%", left: "70%", background: "#ef4444" }}
            ></div>
            <div
              className={styles.dot}
              style={{ top: "50%", left: "30%", background: "#22c55e" }}
            ></div>
            <div
              className={styles.dot}
              style={{ top: "70%", left: "60%", background: "#f59e0b" }}
            ></div>
          </div>
          <span className={styles.radarLabel}>
            Interactive map • 500m radius
          </span>
        </div>
      </div>

      <div className={styles.zonesList}>
        {zones.map((zone) => (
          <div
            key={zone.id}
            className={`${styles.zoneItem} ${styles[zone.type]}`}
          >
            <div className={styles.zoneHeader}>
              <div className={styles.zoneTitle}>
                <span className={styles.indicator}>●</span>
                <h4>{zone.name}</h4>
                {zone.type === "caution" && <Info size={14} />}
                {zone.type === "high" && <AlertTriangle size={14} />}
                {zone.type === "safe" && <ShieldCheck size={14} />}
              </div>
            </div>
            <p>{zone.desc}</p>
            <span className={styles.tag}>{zone.tag}</span>
          </div>
        ))}
      </div>

      <div className={styles.legend}>
        <span>
          <b style={{ color: "#ef4444" }}>●</b> High Risk
        </span>
        <span>
          <b style={{ color: "#f59e0b" }}>●</b> Caution
        </span>
        <span>
          <b style={{ color: "#22c55e" }}>●</b> Safe
        </span>
      </div>
    </div>
  );
};

export default RiskMapSidebar;
