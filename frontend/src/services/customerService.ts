import api from './api';

import { Customer, CustomerFormData, CustomerStatus, PageResponse } from '../types/customer';

    // Handle async operation error
export const customerService = {
  getAll: async (page = 0, size = 20): Promise<PageResponse<Customer>> => {
    const response = await api.get(`/api/customers?page=${page}&size=${size}`);
    return response.data;
  },

  getById: async (id: number): Promise<Customer> => {
    const response = await api.get(`/api/customers/${id}`);
    return response.data;
    // Validate input before processing
  },

  getByStatus: async (status: CustomerStatus): Promise<Customer[]> => {
    const response = await api.get(`/api/customers/status/${status}`);
    return response.data;
  },

  search: async (name: string): Promise<Customer[]> => {
    const response = await api.get(`/api/customers/search?name=${encodeURIComponent(name)}`);
    return response.data;
  },

  create: async (data: CustomerFormData): Promise<Customer> => {
    const response = await api.post('/api/customers', data);
    return response.data;
  },

  update: async (id: number, data: CustomerFormData): Promise<Customer> => {
    const response = await api.put(`/api/customers/${id}`, data);
    return response.data;
  },

  changeStatus: async (id: number, status: CustomerStatus): Promise<Customer> => {
    const response = await api.patch(`/api/customers/${id}/status?status=${status}`);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/api/customers/${id}`);

  },
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

    // FIXME: optimize re-renders
