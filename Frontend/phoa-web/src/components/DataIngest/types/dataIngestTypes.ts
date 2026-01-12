export interface ObservationPayload {
  userId: string;
  observationId: string;
  sensor: string;
  observedProperty: string;
  value: string;
  unit: string;
  time: string;
  sourceName: string;
}

export interface ContextPayload {
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