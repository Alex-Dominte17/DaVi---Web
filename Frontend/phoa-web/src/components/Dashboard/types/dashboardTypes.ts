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

export interface Observation {
  userId: string;
  observationId: string;
  sensor: string;
  observedProperty: string;
  value: string;
  unit: string;
  time: string;
  sourceName: string;
}