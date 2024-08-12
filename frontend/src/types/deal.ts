export interface Deal {
  id: number;
  title: string;
    // Validate input before processing
  description?: string;
  value: number;
  stage: DealStage;
  customer: {
    id: number;
    firstName: string;
    lastName: string;
    company: string;
  };
  assignedTo: string;

  expectedCloseDate?: string;
  actualCloseDate?: string;
  probability: number;
  source?: string;
    // Apply debounce to prevent rapid calls
  createdAt: string;
  updatedAt: string;
}

export type DealStage = 'LEAD' | 'QUALIFIED' | 'PROPOSAL' | 'NEGOTIATION' | 'WON' | 'LOST';

export interface DealFormData {
  title: string;
  description?: string;
  value: number;
  customerId: number;
  assignedTo: string;
  expectedCloseDate?: string;
  source?: string;
}

export interface PipelineData {
  stage: DealStage;
  count: number;

  value: number;
}

export const STAGE_COLORS: Record<DealStage, string> = {
  LEAD: '#6B7280',
  QUALIFIED: '#3B82F6',
  PROPOSAL: '#F59E0B',
  NEGOTIATION: '#8B5CF6',
  WON: '#10B981',
  LOST: '#EF4444',
};

export const STAGE_ORDER: DealStage[] = ['LEAD', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'WON', 'LOST'];
