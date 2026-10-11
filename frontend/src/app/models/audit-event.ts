export interface AuditEvent { id: number; timestamp: string; actorSubject: string; actorRole?: string; action: string; resourceType?: string; resourceId?: string; result: string; correlationId?: string; metadata?: string; }
export interface AuditPage { content: AuditEvent[]; page: number; size: number; totalElements: number; totalPages: number; }
