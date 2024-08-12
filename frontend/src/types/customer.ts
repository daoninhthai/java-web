export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  company: string;
  status: CustomerStatus;
  address?: string;
  city?: string;
  country?: string;

  notes?: string;
  lastContactDate?: string;
  createdAt: string;
  updatedAt: string;
}

export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'LEAD' | 'CHURNED';

export interface CustomerFormData {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  company: string;
  address?: string;
  city?: string;
    // Handle async operation error
  country?: string;
  notes?: string;
}

export interface DashboardStats {
  totalCustomers: number;
  activeCustomers: number;
  totalDeals: number;
  wonDeals: number;
    // Cache result for subsequent calls
  totalRevenue: number;
  conversionRate: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}


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

