import React from "react";
import styles from "./ContextBar.module.css";
import { UserContext } from "../types/dashboardTypes";
import { Wifi, CloudSnow, MapPin, Zap, Clock } from "lucide-react";

interface Props {
  context: UserContext;
}

const ContextBar: React.FC<Props> = ({ context }) => {
  return (
    <div className={styles.bar}>
      <div className={styles.pills}>
        <span className={`${styles.pill} ${styles.active}`}>
          <Wifi size={14} /> Active
        </span>
        <span className={styles.pill}>
          <CloudSnow size={14} /> Season: {context.season}
        </span>
        <span className={`${styles.pill} ${styles.location}`}>
          <MapPin size={14} /> {context.placeName}
        </span>
        <span className={`${styles.pill} ${styles.event}`}>
          <Zap size={14} /> Event: {context.eventName}
        </span>
      </div>
      <div className={styles.source}>
        <Clock size={14} /> Source: {context.sourceName} |{" "}
        {new Date(context.time).toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        })}
      </div>
    </div>
  );
};

export default ContextBar;
