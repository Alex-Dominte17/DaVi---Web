import React from "react";
import styles from "./PollenWidget.module.css";
import { Wind } from "lucide-react";

interface PollenWidgetProps {
  observation: {
    value: string;
    unit: string;
    property: string;
    timestamp: string;
  };
}

const PollenWidget: React.FC<PollenWidgetProps> = ({ observation }) => {
  const pollenValue = parseInt(observation.value);
  
  const getStatus = (val: number) => {
    if (val > 100) return { text: "High", color: "#e11d48" };
    if (val > 50) return { text: "Moderate", color: "#f59e0b" };
    return { text: "Low", color: "#10b981" };
  };

  const status = getStatus(pollenValue);

  return (
    <div className={styles.widget}>
      <div className={styles.header}>
        <Wind size={20} />
        <h3>Pollen Forecast</h3>
      </div>
      <div className={styles.content}>
        <div className={styles.valueContainer}>
          <span className={styles.value}>{observation.value}</span>
          <span className={styles.unit}>{observation.unit}</span>
        </div>
        <div className={styles.statusBadge} style={{ backgroundColor: status.color + '22', color: status.color }}>
          {status.text}
        </div>
      </div>
      <p className={styles.footer}>Proprietate detectată: {observation.property}</p>
    </div>
  );
};

export default PollenWidget;
