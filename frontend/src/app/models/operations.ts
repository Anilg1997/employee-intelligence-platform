export interface ModelMetadata { name: string; version: string; algorithm?: string; status?: string; metrics?: Record<string, unknown>; limitations?: string[]; }
export interface ActuatorHealth { status?: string; components?: Record<string, { status?: string; details?: Record<string, unknown> }>; [key: string]: unknown; }
export interface ActuatorMetricNames { names?: string[]; }
export interface DocumentSearchResult { [key: string]: unknown; content?: string; metadata?: Record<string, unknown>; similarity?: number; }
