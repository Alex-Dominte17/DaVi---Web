import React, { useState, useEffect } from "react";
import styles from "./DataIngest.module.css";
import { Database, Send, Code, FileText, CheckCircle2, AlertCircle, MapPin } from "lucide-react";
import { apiService } from "../../../api/apiService";
import { getCurrentSeason } from "../../utils/utils";

const DataIngest: React.FC = () => {
  const [activeTab, setActiveTab] = useState<"observation" | "context">("observation");
  const [method, setMethod] = useState<"form" | "json">("form");
  const [jsonInput, setJsonInput] = useState("");
  const [status, setStatus] = useState<{ type: "success" | "error"; msg: string } | null>(null);

  const userId = "Alice";

  // State pentru Observation Form
  const [obsForm, setObsForm] = useState({
    userId: userId,
    observationId: "",
    sensor: "",
    observedProperty: "",
    value: "",
    unit: "",
    time: "",
    sourceName: ""
  });

  // State pentru Context Form
  const [ctxForm, setCtxForm] = useState({
    userId: userId,
    contextId: "",
    eventName: "",
    placeName: "",
    lat: "", // String pentru a permite input manual usor, convertim la submit
    lon: "",
    time: "",
    season: getCurrentSeason(),
    sourceName: ""
  });

  // Re-generăm ID-urile și Timpul la schimbarea tab-ului sau la cerere
  const refreshDefaults = () => {
    const now = new Date().toISOString();
    const ts = Date.now();
    if (activeTab === "observation") {
      setObsForm(prev => ({ 
        ...prev, 
        time: now, 
        observationId: `Obs_${ts}` 
      }));
    } else {
      setCtxForm(prev => ({ 
        ...prev, 
        time: now, 
        contextId: `Ctx_${ts}`,
        season: getCurrentSeason()
      }));
    }
  };

  useEffect(() => {
    refreshDefaults();
  }, [activeTab]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatus(null);
    try {
      let payload;
      if (method === "json") {
        payload = JSON.parse(jsonInput);
      } else {
        if (activeTab === "observation") {
          payload = obsForm;
        } else {
          payload = {
            ...ctxForm,
            lat: parseFloat(ctxForm.lat),
            lon: parseFloat(ctxForm.lon)
          };
        }
      }

      if (activeTab === "observation") {
        await apiService.ingestObservation(payload);
      } else {
        await apiService.ingestContext(payload);
      }

      setStatus({ type: "success", msg: "Data ingested successfully!" });
      refreshDefaults();
    } catch (err: any) {
      setStatus({ type: "error", msg: err.message || "Invalid Input" });
    }
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1>Data Ingestion Tool</h1>
        <p>Manually define and inject semantic observations or contexts</p>
      </header>

      <div className={styles.tabs}>
        <button className={activeTab === "observation" ? styles.activeTab : ""} onClick={() => setActiveTab("observation")}>
          <Database size={18} /> Observation
        </button>
        <button className={activeTab === "context" ? styles.activeTab : ""} onClick={() => setActiveTab("context")}>
          <MapPin size={18} /> Context
        </button>
      </div>

      <div className={styles.card}>
        <div className={styles.methodToggle}>
          <button onClick={() => setMethod("form")} className={method === "form" ? styles.activeMethod : ""}>
            <FileText size={16} /> Manual Form
          </button>
          <button onClick={() => setMethod("json")} className={method === "json" ? styles.activeMethod : ""}>
            <Code size={16} /> Raw JSON
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {method === "json" ? (
            <textarea
              className={styles.jsonArea}
              value={jsonInput}
              onChange={(e) => setJsonInput(e.target.value)}
              placeholder={`Paste your ${activeTab} JSON structure here...`}
            />
          ) : (
            <div className={styles.gridForm}>
              {activeTab === "observation" ? (
                <>
                  <div className={styles.inputGroup}><label>User ID</label><input value={obsForm.userId} readOnly /></div>
                  <div className={styles.inputGroup}><label>Observation ID</label><input value={obsForm.observationId} onChange={e => setObsForm({...obsForm, observationId: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Sensor</label><input placeholder="e.g. SelfReportInput" value={obsForm.sensor} onChange={e => setObsForm({...obsForm, sensor: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Property</label><input placeholder="e.g. heartRateBpm" value={obsForm.observedProperty} onChange={e => setObsForm({...obsForm, observedProperty: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Value</label><input placeholder="e.g. 85" value={obsForm.value} onChange={e => setObsForm({...obsForm, value: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Unit</label><input placeholder="e.g. bpm" value={obsForm.unit} onChange={e => setObsForm({...obsForm, unit: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Time (ISO)</label><input value={obsForm.time} onChange={e => setObsForm({...obsForm, time: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Source</label><input placeholder="e.g. manual" value={obsForm.sourceName} onChange={e => setObsForm({...obsForm, sourceName: e.target.value})} /></div>
                </>
              ) : (
                <>
                  <div className={styles.inputGroup}><label>User ID</label><input value={ctxForm.userId} readOnly /></div>
                  <div className={styles.inputGroup}><label>Context ID</label><input value={ctxForm.contextId} onChange={e => setCtxForm({...ctxForm, contextId: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Event Name</label><input placeholder="What happened?" value={ctxForm.eventName} onChange={e => setCtxForm({...ctxForm, eventName: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Place Name</label><input placeholder="Where?" value={ctxForm.placeName} onChange={e => setCtxForm({...ctxForm, placeName: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Latitude</label><input type="number" step="any" placeholder="44.43" value={ctxForm.lat} onChange={e => setCtxForm({...ctxForm, lat: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Longitude</label><input type="number" step="any" placeholder="26.10" value={ctxForm.lon} onChange={e => setCtxForm({...ctxForm, lon: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Season</label><input value={ctxForm.season} onChange={e => setCtxForm({...ctxForm, season: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Time (ISO)</label><input value={ctxForm.time} onChange={e => setCtxForm({...ctxForm, time: e.target.value})} /></div>
                  <div className={styles.inputGroup}><label>Source</label><input placeholder="e.g. manual" value={ctxForm.sourceName} onChange={e => setCtxForm({...ctxForm, sourceName: e.target.value})} /></div>
                </>
              )}
            </div>
          )}

          <button type="submit" className={styles.submitBtn}>
            <Send size={18} /> Ingest Data
          </button>
        </form>

        {status && (
          <div className={`${styles.status} ${styles[status.type]}`}>
            {status.type === "success" ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
            {status.msg}
          </div>
        )}
      </div>
    </div>
  );
};

export default DataIngest;