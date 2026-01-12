import { ENDPOINTS } from "./endpoints";

export const apiService = {
  getLatestContext: async (userId: string) => {
    const response = await fetch(ENDPOINTS.USERS.LATEST_CONTEXT(userId));
    if (!response.ok) throw new Error("Failed to fetch context");
    return response.json();
  },

  getLatestObservation: async (userId: string, property: string) => {
    const response = await fetch(
      `${ENDPOINTS.USERS.LATEST_OBSERVATIONS(userId)}?property=${property}`
    );
    if (!response.ok) throw new Error("Failed to fetch observation");
    return response.json();
  },

  getEntourage: async (userId: string) => {
    const response = await fetch(ENDPOINTS.ENTOURAGE.LIST(userId));
    return response.json();
  },

  addContact: async (userId: string, contactData: any) => {
    const response = await fetch(ENDPOINTS.ENTOURAGE.ADD(userId), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(contactData),
    });
    return response.json();
  },

  updateContact: async (userId: string, contactId: string, contactData: any) => {
    const response = await fetch(ENDPOINTS.ENTOURAGE.PATCH(userId, contactId), {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(contactData),
    });
    if (!response.ok) throw new Error("Failed to update contact");
    return response.json();
  },

  deleteContact: async (userId: string, contactId: string) => {
    const response = await fetch(ENDPOINTS.ENTOURAGE.REMOVE(userId, contactId), {
      method: "DELETE",
    });
    if (!response.ok) throw new Error("Failed to delete contact");
    return response;
  },

  ackNotification: async (notifId: string, status: string = "read") => {
    const response = await fetch(ENDPOINTS.NOTIFICATIONS.ACK(notifId), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status }),
    });
    console.log("Ack response:", response);
    return response.json();
  },

  evaluateUser: async (userLocalName: string, reason: string) => {
    const response = await fetch(ENDPOINTS.USERS.EVALUATE(userLocalName), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ reason }),
    });

    if (!response.ok) {
      throw new Error("Failed to evaluate user");
    }

    if (response.status === 204) {
      return null;
    }

    const text = await response.text();
    if (!text) return null;

    return JSON.parse(text);
  },

  getRandomPhobiaSuggestions: async (limit: number = 5) => {
    const response = await fetch(ENDPOINTS.PHOBIAS.RANDOM_SUGGESTIONS(limit));
    console.log('response', response.json);
    if (!response.ok) throw new Error("Failed to fetch phobia suggestions");
    return response.json();
  },

  getNotifications: async (userId: string) => {
    const response = await fetch(ENDPOINTS.USERS.NOTIFICATIONS(userId));
    if (!response.ok) throw new Error("Failed to fetch notifications");
    return response.json();
  },

  addUserPhobia: async (userLocalName: string, phobiaData: any) => {
    const response = await fetch(ENDPOINTS.USERS.PHOBIAS(userLocalName), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(phobiaData),
    });

    if (!response.ok) {
      throw new Error("Failed to add phobia to user profile");
    }

    return response.json();
  },

  getAllInterventions: async () => {
    const response = await fetch(ENDPOINTS.INTERVENTIONS.LIST);
    if (!response.ok) throw new Error("Failed to fetch interventions");
    return response.json();
  },

  getUserPhobias: async (userId: string) => {
    const response = await fetch(ENDPOINTS.USERS.PHOBIAS(userId));
    if (!response.ok) throw new Error("Failed to fetch user phobias");
    return response.json();
  },

  ingestObservation: async (payload: any) => {
    const response = await fetch(ENDPOINTS.INGEST.OBSERVATIONS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    if (!response.ok) throw new Error("Failed to ingest observation");
    return response.json();
  },

  ingestContext: async (payload: any) => {
    const response = await fetch(ENDPOINTS.INGEST.CONTEXT, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    if (!response.ok) throw new Error("Failed to ingest context");
    return response.json();
  },
};
