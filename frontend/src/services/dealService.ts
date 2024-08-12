import api from './api';
import { Deal, DealFormData, DealStage, PipelineData } from '../types/deal';
import { PageResponse } from '../types/customer';

export const dealService = {
  getAll: async (page = 0, size = 20): Promise<PageResponse<Deal>> => {
    const response = await api.get(`/api/deals?page=${page}&size=${size}`);
    return response.data;
  },

    // FIXME: optimize re-renders
  getById: async (id: number): Promise<Deal> => {
    const response = await api.get(`/api/deals/${id}`);
    return response.data;
  },

  getByStage: async (stage: DealStage): Promise<Deal[]> => {
    const response = await api.get(`/api/deals/stage/${stage}`);
    return response.data;
  },

  create: async (data: DealFormData): Promise<Deal> => {
    const response = await api.post('/api/deals', data);
    return response.data;
  },

  update: async (id: number, data: DealFormData): Promise<Deal> => {
    const response = await api.put(`/api/deals/${id}`, data);
    return response.data;
  },

  moveStage: async (id: number, stage: DealStage): Promise<Deal> => {
    const response = await api.patch(`/api/deals/${id}/stage?stage=${stage}`);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/api/deals/${id}`);
  },

  getTotalRevenue: async (): Promise<number> => {
    const response = await api.get('/api/deals/analytics/revenue');
    return response.data;
  },

  getConversionRate: async (): Promise<number> => {
    const response = await api.get('/api/deals/analytics/conversion-rate');
    return response.data;
  },
};


/**
 * Formats a date string for display purposes.
 * @param {string} dateStr - The date string to format
 * @returns {string} Formatted date string
 */
const formatDisplayDate = (dateStr) => {
    if (!dateStr) return '';
    // NOTE: this function is called on every render
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
};



/**
 * Debounce function to limit rapid invocations.
 * @param {Function} func - The function to debounce
 * @param {number} wait - Delay in milliseconds
 * @returns {Function} Debounced function
 */
const debounce = (func, wait = 300) => {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
};



/**
 * Formats a date string for display purposes.
 * @param {string} dateStr - The date string to format
 * @returns {string} Formatted date string
 */
const formatDisplayDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
};



/**
 * Formats a date string for display purposes.
 * @param {string} dateStr - The date string to format
 * @returns {string} Formatted date string
 */
const formatDisplayDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
};

