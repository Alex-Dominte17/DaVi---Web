import React from "react";
import styles from "./PollenWidget.module.css";
import { Observation } from "../types/dashboardTypes";
import { Flower } from "lucide-react";

interface Props {
  observation: Observation;
}

const PollenWidget: React.FC<Props> = ({ observation }) => {
  return (
    <div className={styles.pollenCard}>
      <div className={styles.widgetHeader}>
        <Flower
          size={24}
          color="#e11d48"
          style={{ transform: "rotate(-90deg)" }}
        />
        <div>
          <h4>Pollen Level</h4>
          <p>Environmental Monitor</p>
        </div>
        <span className={styles.alertPill}>⚠️ Alert</span>
      </div>

      <div className={styles.pollenCircle}>
        <svg viewBox="0 0 36 36" className={styles.circularChart}>
          <path
            className={styles.circleBg}
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
          />
          <path
            className={styles.circle}
            strokeDasharray="80, 100"
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
          />
          <text x="18" y="19" className={styles.pollenValue}>
            {observation.value}
          </text>
          <text x="18" y="24" className={styles.pollenUnit}>
            {observation.unit}
          </text>
        </svg>
        <div className={styles.statusBox}>
          <span className={styles.statusBadge}>Very High</span>
          <p>High pollen count detected. Even if it's Winter, stay alert!</p>
        </div>
      </div>
    </div>
  );
};

export default PollenWidget;
