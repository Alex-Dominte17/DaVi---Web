import React from "react";
import styles from "./Entourage.module.css";
import {
  UserPlus,
  Shield,
  Bell,
  MessageSquare,
  Check,
  X,
  Info,
  Heart,
} from "lucide-react";

const Entourage: React.FC = () => {
  const contacts = [
    {
      id: 1,
      name: "Maria Popescu",
      role: "Family",
      email: "maria@example.com",
      initials: "MP",
      alerts: true,
    },
    {
      id: 2,
      name: "Ion Ionescu",
      role: "Friend",
      email: "ion@example.com",
      initials: "II",
      alerts: true,
    },
    {
      id: 3,
      name: "Dr. Ana Medicescu",
      role: "Professional",
      email: "ana.med@example.com",
      initials: "DAM",
      alerts: false,
    },
  ];

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div>
          <h1>Social Entourage</h1>
          <p>Your trusted circle and community insights</p>
        </div>
        <button className={styles.btnAdd}>
          <UserPlus size={18} /> Add Contact
        </button>
      </header>

      <div className={styles.layout}>
        <main className={styles.mainContent}>
          <section className={styles.trustCircleCard}>
            <div className={styles.cardTitleRow}>
              <div className={styles.iconBox}>
                <Shield size={20} color="#2dd4bf" />
              </div>
              <div>
                <h2>Trust Circle</h2>
                <span>FOAF-modeled contacts</span>
              </div>
            </div>

            <div className={styles.contactList}>
              {contacts.map((contact) => (
                <div key={contact.id} className={styles.contactItem}>
                  <div className={styles.avatar}>{contact.initials}</div>
                  <div className={styles.contactInfo}>
                    <div className={styles.nameRow}>
                      <h3>{contact.name}</h3>
                      <span
                        className={`${styles.roleBadge} ${
                          styles[contact.role.toLowerCase()]
                        }`}
                      >
                        {contact.role}
                      </span>
                    </div>
                    <p>{contact.email}</p>
                  </div>
                  <div className={styles.contactActions}>
                    <span
                      className={
                        contact.alerts ? styles.alertsOn : styles.alertsOff
                      }
                    >
                      <Bell size={14} /> Alerts {contact.alerts ? "On" : "Off"}
                    </span>
                    <button className={styles.btnChat}>
                      <MessageSquare size={18} />
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className={styles.semanticNote}>
              <Info size={16} />
              <p>
                <strong>Semantic Note:</strong> Contacts are modeled using FOAF
                (Friend of a Friend) ontology for interoperability with other
                semantic web applications.
              </p>
            </div>
          </section>
          <section className={styles.tipsCard}>
            <div className={styles.tipsHeader}>
              <div className={styles.iconBoxTips}>
                <MessageSquare size={20} color="#f97316" />
              </div>
              <div>
                <h2>Crowdsourced Tips</h2>
                <span>RDF annotations on resources</span>
              </div>
            </div>

            <div className={styles.tipsList}>
              <div className={styles.tipItem}>
                <div className={styles.tipUserAvatar}>M</div>
                <div className={styles.tipContent}>
                  <div className={styles.tipMeta}>
                    <strong>Maria P.</strong>{" "}
                    <span>on "4-7-8 Breathing Technique"</span>
                  </div>
                  <p>
                    The 4-7-8 technique really helped my son during elevator
                    rides. We practice it together before entering.
                  </p>
                  <div className={styles.tipActions}>
                    <span>1/6/2026</span>
                  </div>
                </div>
              </div>

              <div className={styles.tipItem}>
                <div className={styles.tipUserAvatarGray}>A</div>
                <div className={styles.tipContent}>
                  <div className={styles.tipMeta}>
                    <strong>Anonymous</strong>{" "}
                    <span>on "4-7-8 Breathing Technique"</span>
                  </div>
                  <p>
                    I found that doing the breathing exercise while looking at a
                    photo of a safe place helps even more.
                  </p>
                  <div className={styles.tipActions}>
                    <span>1/5/2026</span>
                  </div>
                </div>
              </div>

              <button className={styles.btnAddTip}>Add Your Tip</button>
            </div>
          </section>
        </main>

        <aside className={styles.sidebar}>
          <section className={styles.sideCard}>
            <div className={styles.sideHeader}>
              <Bell size={18} color="#2dd4bf" />
              <h3>Alert Settings</h3>
            </div>
            <div className={styles.settingRow}>
              <div>
                <h4>Panic Alerts</h4>
                <p>Notify circle on panic events</p>
              </div>
              <div className={`${styles.toggle} ${styles.on}`}></div>
            </div>
            <div className={styles.settingRow}>
              <div>
                <h4>Location Sharing</h4>
                <p>Share location during alerts</p>
              </div>
              <div className={styles.toggle}></div>
            </div>
          </section>

          <section className={styles.sideCard}>
            <div className={styles.sideHeader}>
              <UserPlus size={18} color="#2dd4bf" />
              <h3>Pending Invites</h3>
            </div>
            <div className={styles.inviteItem}>
              <div className={styles.smallAvatar}>A</div>
              <span className={styles.inviteName}>Alexandru M.</span>
              <div className={styles.inviteActions}>
                <Check size={18} className={styles.iconCheck} />
                <X size={18} className={styles.iconX} />
              </div>
            </div>
          </section>

          <section className={`${styles.sideCard} ${styles.privacyCard}`}>
            <Shield size={20} color="#3b82f6" />
            <div>
              <h4>Privacy First</h4>
              <p>
                Your health data is never shared. Only alerts you configure are
                sent to your trusted circle.
              </p>
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
};

export default Entourage;
