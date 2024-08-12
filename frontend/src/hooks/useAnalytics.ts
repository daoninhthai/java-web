import { useState, useEffect, useCallback, useRef } from 'react';

export interface RevenueByMonth {
  month: string;
  revenue: number;
  target: number;
}

export interface PipelineHistory {
  date: string;
  value: number;
  wonValue: number;
}

export interface ActivityByWeek {
  week: string;
  calls: number;
  emails: number;
  meetings: number;
}

export interface AnalyticsSummary {
  totalRevenue: number;
  avgDealSize: number;
  winRate: number;
  totalCustomers: number;
  newCustomersThisPeriod: number;
}

export interface AnalyticsData {
  revenueByMonth: RevenueByMonth[];
  pipelineHistory: PipelineHistory[];
  statusDistribution: Record<string, number>;
  activityByWeek: ActivityByWeek[];
  summary: AnalyticsSummary;
  lastUpdated: string;
}

interface DateRange {
  start: string;
  end: string;
}

interface UseAnalyticsReturn {
  data: AnalyticsData | null;
  isLoading: boolean;
  error: string | null;
  refetch: () => void;
  updateDateRange: (range: DateRange) => void;
}

const API_BASE_URL = process.env.REACT_APP_API_URL || '/api';
const DEFAULT_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

/**
 * Custom hook for fetching CRM analytics data with date range filtering.
 *
 * Features:
 * - Automatic data fetching on mount and when date range changes
 * - In-memory caching with configurable TTL
 * - Request deduplication (prevents duplicate concurrent requests)
 * - Automatic refetch capability
 * - Error handling with retry support
 *
 * @param initialDateRange - Optional initial date range filter
 * @param cacheTtlMs - Cache time-to-live in milliseconds (default: 5 minutes)
 * @returns Analytics data, loading state, error state, and control functions
 */
export function useAnalytics(
  initialDateRange?: DateRange,
  cacheTtlMs: number = DEFAULT_CACHE_TTL_MS
): UseAnalyticsReturn {
  const [data, setData] = useState<AnalyticsData | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [dateRange, setDateRange] = useState<DateRange | undefined>(initialDateRange);

  // Cache and deduplication refs
  const cacheRef = useRef<Map<string, { data: AnalyticsData; timestamp: number }>>(new Map());
  const abortControllerRef = useRef<AbortController | null>(null);
  const isMountedRef = useRef<boolean>(true);

  const buildCacheKey = useCallback((range?: DateRange): string => {
    if (!range) return 'analytics:all';
    return `analytics:${range.start}:${range.end}`;
  }, []);

  const getCachedData = useCallback(
    (key: string): AnalyticsData | null => {
      const cached = cacheRef.current.get(key);
      if (!cached) return null;

      const isExpired = Date.now() - cached.timestamp > cacheTtlMs;
      if (isExpired) {
        cacheRef.current.delete(key);
        return null;
      }

      return cached.data;
    },
    [cacheTtlMs]
  );

  const setCachedData = useCallback((key: string, analyticsData: AnalyticsData): void => {
    cacheRef.current.set(key, { data: analyticsData, timestamp: Date.now() });
  }, []);

  const fetchAnalytics = useCallback(
    async (range?: DateRange) => {
      const cacheKey = buildCacheKey(range);

      // Check cache first
      const cached = getCachedData(cacheKey);
      if (cached) {
        setData(cached);
        setIsLoading(false);
        setError(null);
        return;
      }

      // Cancel any in-flight request
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }

      const controller = new AbortController();
      abortControllerRef.current = controller;

      setIsLoading(true);
      setError(null);

      try {
        const params = new URLSearchParams();
        if (range?.start) params.append('startDate', range.start);
        if (range?.end) params.append('endDate', range.end);

        const queryString = params.toString();
        const url = `${API_BASE_URL}/analytics${queryString ? `?${queryString}` : ''}`;

        const response = await fetch(url, {
          signal: controller.signal,
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
          },
        });

        if (!response.ok) {
          const errorBody = await response.text();
          throw new Error(
            `Failed to fetch analytics: ${response.status} ${response.statusText}${
              errorBody ? ` - ${errorBody}` : ''
            }`
          );
        }

        const analyticsData: AnalyticsData = await response.json();

        if (isMountedRef.current) {
          setData(analyticsData);
          setError(null);
          setCachedData(cacheKey, analyticsData);
        }
      } catch (err) {
        if (err instanceof DOMException && err.name === 'AbortError') {
          // Request was cancelled, ignore
          return;
        }

        const errorMessage = err instanceof Error ? err.message : 'An unexpected error occurred';

        if (isMountedRef.current) {
          setError(errorMessage);
          console.error('Analytics fetch error:', errorMessage);
        }
      } finally {
        if (isMountedRef.current) {
          setIsLoading(false);
        }
        if (abortControllerRef.current === controller) {
          abortControllerRef.current = null;
        }
      }
    },
    [buildCacheKey, getCachedData, setCachedData]
  );

  // Fetch on mount and when date range changes
  useEffect(() => {
    fetchAnalytics(dateRange);
  }, [dateRange, fetchAnalytics]);

  // Cleanup on unmount
  useEffect(() => {
    isMountedRef.current = true;

    return () => {
      isMountedRef.current = false;
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, []);

  const refetch = useCallback(() => {
    // Clear cache for current key to force fresh fetch
    const cacheKey = buildCacheKey(dateRange);
    cacheRef.current.delete(cacheKey);
    fetchAnalytics(dateRange);
  }, [dateRange, fetchAnalytics, buildCacheKey]);

  const updateDateRange = useCallback((range: DateRange) => {
    setDateRange(range);
  }, []);

  return {
    data,
    isLoading,
    error,
    refetch,
    updateDateRange,
  };
}


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

