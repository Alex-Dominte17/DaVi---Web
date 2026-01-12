import React, { useEffect, useState } from "react";
import styles from "./Entourage.module.css";
import {
  UserPlus,
  Shield,
  Bell,
  Info,
  Trash2,
  Edit,
  X,
  Save,
} from "lucide-react";
import { apiService } from "../../api/apiService";

interface Contact {
  name: string;
  email: string;
  relationship: string;
  alertsEnabled: boolean;
}

const Entourage: React.FC = () => {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingContact, setEditingContact] = useState<Contact | null>(null);

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    relationship: "Friend",
    alertsEnabled: true,
  });

  const userId = "Alice";
  const generateContactId = (name: string) => `Contact_${userId}_${name.replace(/\s+/g, '')}`;

  const fetchContacts = async () => {
    try {
      setLoading(true);
      const data = await apiService.getEntourage(userId);
      setContacts(data);
    } catch (err) {
      console.error("Error fetching entourage:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchContacts();
  }, []);

  const handleOpenModal = (contact?: Contact) => {
    if (contact) {
      setEditingContact(contact);
      setFormData({
        name: contact.name,
        email: contact.email,
        relationship: contact.relationship,
        alertsEnabled: contact.alertsEnabled,
      });
    } else {
      setEditingContact(null);
      setFormData({ name: "", email: "", relationship: "Friend", alertsEnabled: true });
    }
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingContact) {
        // Generăm ID-ul formatat pentru PATCH: Contact_Alice_Maria
        const contactId = generateContactId(editingContact.name);
        
        // Request body-ul conține: {name, email, relationship, alertsEnabled}
        await apiService.updateContact(userId, contactId, formData);
      } else {
        // Pentru POST folosim direct formData
        await apiService.addContact(userId, formData);
      }
      setIsModalOpen(false);
      fetchContacts();
    } catch (err) {
      alert("Error saving contact. Please try again.");
    }
  };

  const handleDelete = async (contactName: string) => {
    if (!window.confirm(`Are you sure you want to remove ${contactName}?`)) return;
    try {
      const contactId = generateContactId(contactName);
      
      await apiService.deleteContact(userId, contactId);
      setContacts(prev => prev.filter(c => c.name !== contactName));
    } catch (err) {
      alert("Error deleting contact.");
    }
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div>
          <h1>Social Entourage</h1>
          <p>Your trusted circle and community insights</p>
        </div>
        <button className={styles.btnAdd} onClick={() => handleOpenModal()}>
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
              {loading ? (
                <p>Loading your semantic circle...</p>
              ) : contacts.map((contact, index) => (
                <div key={index} className={styles.contactItem}>
                  <div className={styles.avatar}>{contact.name.substring(0, 2).toUpperCase()}</div>
                  <div className={styles.contactInfo}>
                    <div className={styles.nameRow}>
                      <h3>{contact.name}</h3>
                      <span className={`${styles.roleBadge} ${styles[contact.relationship.toLowerCase()] || styles.friend}`}>
                        {contact.relationship}
                      </span>
                    </div>
                    <p>{contact.email}</p>
                  </div>
                  <div className={styles.contactActions}>
                    <span className={contact.alertsEnabled ? styles.alertsOn : styles.alertsOff}>
                      <Bell size={14} /> Alerts {contact.alertsEnabled ? "On" : "Off"}
                    </span>
                    <button className={styles.btnChat} onClick={() => handleOpenModal(contact)}>
                      <Edit size={18} />
                    </button>
                    <button className={styles.btnChat} onClick={() => handleDelete(contact.name)}>
                      <Trash2 size={18} color="#ef4444" />
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className={styles.semanticNote}>
              <Info size={16} />
              <p>
                <strong>Semantic Note:</strong> Contacts are modeled using FOAF (Friend of a Friend) ontology.
              </p>
            </div>
          </section>
        </main>

        <aside className={styles.sidebar}>
          <section className={styles.sideCard}>
            <div className={styles.sideHeader}>
              <UserPlus size={18} color="#2dd4bf" />
              <h3>Add to Circle</h3>
            </div>
            <p className={styles.sideDesc}>Securely add trusted members to receive context-aware alerts.</p>
            <button className={styles.btnAddTip} onClick={() => handleOpenModal()}>
               Open Add Form
            </button>
          </section>

          <section className={`${styles.sideCard} ${styles.privacyCard}`}>
            <Shield size={20} color="#3b82f6" />
            <div>
              <h4>Privacy First</h4>
              <p>Your health data is never shared. Only configured alerts are sent.</p>
            </div>
          </section>
        </aside>
      </div>

      {isModalOpen && (
        <div className={styles.modalOverlay}>
          <div className={styles.modalContent}>
            <div className={styles.modalHeader}>
              <h2 className={styles.popupTitle}>{editingContact ? "Edit Contact" : "Add New Contact"}</h2>
              <button className={styles.closeBtn} onClick={() => setIsModalOpen(false)}><X /></button>
            </div>
            <form onSubmit={handleSubmit} className={styles.modalForm}>
              <div className={styles.inputGroup}>
                <label>Full Name</label>
                <input 
                  type="text" required value={formData.name} 
                  onChange={e => setFormData({...formData, name: e.target.value})}
                />
              </div>
              <div className={styles.inputGroup}>
                <label>Email</label>
                <input 
                  type="email" required value={formData.email} 
                  onChange={e => setFormData({...formData, email: e.target.value})}
                />
              </div>
              <div className={styles.inputGroup}>
                <label>Relationship</label>
                <select value={formData.relationship} onChange={e => setFormData({...formData, relationship: e.target.value})}>
                  <option value="Family">Family</option>
                  <option value="Friend">Friend</option>
                  <option value="Professional">Professional</option>
                </select>
              </div>
              <div className={styles.checkboxRow}>
                <input 
                  type="checkbox" id="notif-check"
                  checked={formData.alertsEnabled} 
                  onChange={e => setFormData({...formData, alertsEnabled: e.target.checked})}
                />
                <label htmlFor="notif-check">Enable Panic Alerts</label>
              </div>
              <button type="submit" className={styles.btnAdd}>
                <Save size={18} /> {editingContact ? "Update" : "Create"} Contact
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Entourage;
