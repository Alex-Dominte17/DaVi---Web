import React, { useEffect, useState } from "react";
import styles from "./DashboardView.module.css";
import ContextBar from "../components/ContextBar";
import HealthMonitor from "../components/HealthMonitor";
import {
  UserContext,
  Observation,
  NotificationSummary,
  PhobiaSuggestItem,
} from "../types/dashboardTypes";
import {
  Bell,
  Play,
  Loader2,
  AlertCircle,
  Target,
  Clock,
  Zap,
  Plus,
} from "lucide-react";
import { ENDPOINTS } from "../../../api/endpoints";
import { apiService } from "../../../api/apiService";
import { getCurrentSeason } from "../../utils/utils";

const DashboardView: React.FC = () => {
  const [context, setContext] = useState<UserContext | null>(null);
  const [notifications, setNotifications] = useState<NotificationSummary[]>([]);

  const [heartRate, setHeartRate] = useState<Observation | null>(null);
  const [altitude, setAltitude] = useState<Observation | null>(null);
  const [fearRating, setFearRating] = useState<Observation | null>(null);

  const [suggestions, setSuggestions] = useState<PhobiaSuggestItem[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const userLocalName = "Alice";

  useEffect(() => {
    if (!userLocalName) return; // don't call API with empty user
  
    const triggerEvaluation = async () => {
      try {
        console.log("Triggering periodic evaluation...");
        await apiService.evaluateUser(userLocalName, "Periodic background check");
        fetchNotifications();
      } catch (err) {
        console.error("Eroare la evaluarea automată:", err);
      }
    };
  
    triggerEvaluation();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // still runs once
  

  const fetchNotifications = async () => {
    try {
      const notifRes = await fetch(
        ENDPOINTS.USERS.NOTIFICATIONS(userLocalName)
      );
      if (notifRes.ok) {
        const notifData = await notifRes.json();
        setNotifications(notifData);
      }
    } catch (err) {
      console.error("Eroare la preluarea notificărilor:", err);
    }
  };

  useEffect(() => {
    fetchDashboardData();
    // const interval = setInterval(fetchDashboardData, 30000);
    // return () => clearInterval(interval);
  }, [userLocalName]);

  const fetchDashboardData = async () => {
    try {
      const [contextData, notifData, hrData, altData, fearData, suggestData] =
        await Promise.all([
          apiService.getLatestContext(userLocalName),
          apiService.getNotifications(userLocalName),
          apiService.getLatestObservation(userLocalName, "heartRateBpm"),
          apiService.getLatestObservation(userLocalName, "altitudeMeters"),
          apiService.getLatestObservation(userLocalName, "fearRating"),
          apiService.getRandomPhobiaSuggestions(5),
        ]);

      setContext(contextData);
      setNotifications(notifData);
      setHeartRate(hrData);
      setAltitude(altData);
      setFearRating(fearData);
      setSuggestions(suggestData);

      setError(null);
    } catch (err) {
      console.error("Dashboard fetch error:", err);
      setError("Error synchronizing data.");
    } finally {
      setLoading(false);
    }
  };

  if (loading && !context) {
    return (
      <div className={styles.loadingOverlay}>
        <div className={styles.loadingCard}>
          <div className={styles.spinnerWrapper}>
            <Loader2 className={styles.spinnerIcon} size={48} />
            <div className={styles.spinnerPulse}></div>
          </div>
          <h2>Semantic Synchronization</h2>
          <p>Fetching data from PhoA system...</p>
          <div className={styles.loadingBar}>
            <div className={styles.loadingProgress}></div>
          </div>
        </div>
      </div>
    );
  }

  const handleAckNotification = async (fullUri: string) => {
    try {
      const notificationId = fullUri.includes("#")
        ? fullUri.split("#")[1]
        : fullUri;

      console.log("Acknowledging short ID:", notificationId);

      await apiService.ackNotification(notificationId, "read");

      setNotifications((prev) =>
        prev.filter((n) => n.notificationUri !== fullUri)
      );
    } catch (err) {
      console.error("Error acknowledging notification:", err);
      alert("Could not mark notification as read.");
    }
  };

  const handleAddPhobia = async (item: PhobiaSuggestItem) => {
    try {
      const payload = {
        wikidataUri: item.wikidataUri,
        label: item.label,
        severity: 7,
        afflictionStart: new Date().toISOString(),
      };

      await apiService.addUserPhobia(userLocalName, payload);
      alert(`Successfully added ${item.label} to your profile!`);

      fetchDashboardData();
    } catch (err) {
      console.error("Error adding phobia:", err);
      alert("Failed to add phobia. It might already exist in your profile.");
    }
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1>Phobia Monitor</h1>
        <p>Real-time monitoring based on semantic context</p>
      </header>

      {context && (
        <ContextBar
          context={{
            ...context,
            season: getCurrentSeason(),
          }}
        />
      )}

      <div className={styles.layout}>
        <main className={styles.mainContent}>
          <HealthMonitor
            heartRate={heartRate}
            altitude={altitude}
            fearRating={fearRating}
          />

          <div className={styles.suggestionsSection}>
            <div className={styles.sectionHeader}>
              <Zap size={20} className={styles.zapIcon} />
              <h2 className={styles.sectionTitle}>
                Explore Phobias (Wikidata)
              </h2>
            </div>
            <div className={styles.suggestionsGrid}>
              {suggestions.map((item) => (
                <div key={item.wikidataUri} className={styles.suggestionCard}>
                  <div className={styles.suggestionHeader}>
                    <h4>{item.label}</h4>
                    <button
                      className={styles.btnAddPhobia}
                      onClick={() => handleAddPhobia(item)}
                      title="Add to my profile"
                    >
                      <Plus size={18} />
                    </button>
                  </div>
                  <p>
                    {item.description ||
                      "No description available on Wikidata."}
                  </p>
                  <a
                    href={item.wikidataUri}
                    target="_blank"
                    rel="noopener noreferrer"
                    className={styles.wikiLink}
                  >
                    See Wikidata details →
                  </a>
                </div>
              ))}
            </div>
          </div>

          <div className={styles.sectionHeader}>
            <Bell size={20} className={styles.bellIcon} />
            <h2 className={styles.sectionTitle}>
              Active Alerts & Notifications
            </h2>
            {notifications.length > 0 && (
              <span className={styles.criticalBadge}>
                {notifications.length} new
              </span>
            )}
          </div>

          {notifications.map((notif) => (
            <div
              key={notif.notificationUri}
              className={
                notif.status === "unread"
                  ? styles.alertCardCritical
                  : styles.alertCardInfo
              }
            >
              <div className={styles.alertIconBg}>
                <Bell
                  size={24}
                  color={notif.status === "unread" ? "#e11d48" : "#3b82f6"}
                />
              </div>

              <div className={styles.alertBody}>
                <div className={styles.alertTitleRow}>
                  <h3>Detection: {notif.detectedPhobiaLabel}</h3>
                  <button
                    className={styles.closeBtn}
                    onClick={() => handleAckNotification(notif.notificationUri)}
                  >
                    ×
                  </button>
                </div>

                <p>
                  The system detected the state{" "}
                  <strong>"{notif.contextEventName}"</strong> in the location{" "}
                  <strong>{notif.contextPlaceName}</strong>.
                </p>

                <div className={styles.semanticGrid}>
                  <div className={styles.metaItem}>
                    <Target size={14} />
                    <span className={styles.metaLabel}>
                      Confidence: {(notif.confidence * 100).toFixed(0)}%
                    </span>
                  </div>
                  <div className={styles.metaItem}>
                    <Clock size={14} />
                    <span className={styles.metaLabel}>
                      {new Date(notif.contextTime).toLocaleTimeString()}
                    </span>
                  </div>
                </div>

                <div className={styles.interventionsBox}>
                  <p className={styles.interventionsTitle}>
                    <strong>Recommendations:</strong>
                  </p>
                  <div className={styles.recommendationsContainer}>
                    {notif.interventions.map((inv, idx) => (
                      <div key={idx} className={styles.recommendationCard}>
                        <div className={styles.recommendationContent}>
                          <Zap
                            size={16}
                            className={styles.recommendationIcon}
                          />
                          <span className={styles.recommendationLabel}>
                            {inv.label}
                          </span>
                        </div>
                        <button
                          className={styles.btnActionSmall}
                          onClick={() => {
                            if (inv.url) {
                              window.open(
                                inv.url,
                                "_blank",
                                "noopener,noreferrer"
                              );
                            } else {
                              console.log(`No URL available for: ${inv.label}`);
                              alert(
                                `No external resource available for ${inv.label}`
                              );
                            }
                          }}
                          title={inv.iri}
                        >
                          <Play size={12} fill="currentColor" />
                          Start
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          ))}

          {notifications.length === 0 && (
            <div className={styles.alertCardInfo}>
              <div className={styles.infoIconBg}>i</div>
              <div className={styles.alertBody}>
                <h3>System Status</h3>
                <p>No recent critical situations detected.</p>
              </div>
            </div>
          )}
        </main>
      </div>

      {error && (
        <div className={styles.errorToast}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}
    </div>
  );
};

export default DashboardView;
