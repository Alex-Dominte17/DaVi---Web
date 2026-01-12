const API_BASE_URL = '/api';

export const ENDPOINTS = {
  USERS: {
    PHOBIAS: (userLocalName: string) => 
      `${API_BASE_URL}/users/${userLocalName}/phobias`,
    LATEST_CONTEXT: (userLocalName: string) => 
      `${API_BASE_URL}/users/${userLocalName}/context/latest`,
    LATEST_OBSERVATIONS: (userLocalName: string) => 
      `${API_BASE_URL}/users/${userLocalName}/observations/latest`,
    EVALUATE: (userLocalName: string) => 
      `${API_BASE_URL}/users/${userLocalName}/evaluate`,
    NOTIFICATIONS: (userLocalName: string, limit: number = 20) => 
      `${API_BASE_URL}/users/${userLocalName}/notifications?limit=${limit}`,
  },

  ENTOURAGE: {
    LIST: (userId: string) => 
      `${API_BASE_URL}/users/${userId}/entourage`,
    ADD: (userId: string) => 
      `${API_BASE_URL}/users/${userId}/entourage`,
    PATCH: (userId: string, contactId: string) => 
      `${API_BASE_URL}/users/${userId}/entourage/${contactId}`,
    REMOVE: (userId: string, contactId: string) => 
      `${API_BASE_URL}/users/${userId}/entourage/${contactId}`,
  },

  PHOBIAS: {
    ENRICH: (phobiaId: string) => 
      `${API_BASE_URL}/phobias/${phobiaId}/enrich`,
    RANDOM_SUGGESTIONS: (limit: number = 5) => `${API_BASE_URL}/phobias/random?limit=${limit}`,
  },

  INGEST: {
    CONTEXT: `${API_BASE_URL}/context`,
    OBSERVATIONS: `${API_BASE_URL}/observations`,
  },

  NOTIFICATIONS: {
    ACK: (id: string) => 
      `${API_BASE_URL}/notifications/${id}/ack`,
  },

  INTERVENTIONS: {
    LIST: `${API_BASE_URL}/interventions`,
  },

  DEBUG: {
    DUMP_RDF: `${API_BASE_URL}/debug/rdf`,
  }
};