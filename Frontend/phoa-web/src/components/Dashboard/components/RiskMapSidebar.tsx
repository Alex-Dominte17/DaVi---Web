import React, { useEffect, useState } from "react";
import styles from "./RiskMapSidebar.module.css";
import { MapPin, AlertTriangle } from "lucide-react";
import { ENDPOINTS } from "../../../api/endpoints";

const RiskMapSidebar: React.FC = () => {
  const [phobias, setPhobias] = useState<any[]>([]);
  const userId = "Alice";

  useEffect(() => {
    fetch(ENDPOINTS.USERS.PHOBIAS(userId))
      .then(res => res.json())
      .then(data => setPhobias(data))
      .catch(console.error);
  }, []);

  return (
    <div className={styles.sidebarCard}>
      <div className={styles.header}>
        <MapPin size={20} />
        <h3>Risk Areas</h3>
      </div>
      <div className={styles.riskList}>
        {phobias.map((phobia, index) => (
          <div key={index} className={styles.riskItem}>
            <AlertTriangle size={16} color="#f59e0b" />
            <div>
              <p className={styles.location}>{phobia.name}</p>
              <p className={styles.riskLevel}>Trigger detectat în baza de date</p>
            </div>
          </div>
        ))}
        {phobias.length === 0 && <p>Nicio zonă de risc detectată momentan.</p>}
      </div>
    </div>
  );
};

export default RiskMapSidebar;
