export interface Observation {
  userId: string;
  observationId?: string;
  sensor?: string;
  observedProperty?: string;
  property: string;
  value: string;
  unit: string;
  time?: string;
  timestamp: string;  
  sourceName?: string;
}

export interface UserContext {
  userId: string;
  contextId: string;
  eventName: string;
  placeName: string;
  lat: number;
  lon: number;
  time: string;
  season: string;
  sourceName: string;
}

export interface Intervention {
  iri: string; 
  label: string;     
  typeIri: string;   
  url: string | null; 
}

export interface NotificationSummary {
  id?: string; 
  notificationUri: string;
  title?: string; 
  message?: string;
  status: string;
  createdAt: string;
  confidence: number;
  contextEventName: string;
  contextPlaceName: string;
  contextTime: string;
  contextUri: string;
  detectedPhobia: string;
  detectedPhobiaLabel: string;
  interventions: Intervention[];
}

export interface PhobiaSuggestItem {
  wikidataUri: string;
  label: string;
  description: string;
}