import React, { useEffect, useRef, useMemo } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  ChartData,
  ChartOptions,
} from 'chart.js';
import { Bar, Line, Doughnut } from 'react-chartjs-2';
import { useAnalytics, AnalyticsData } from '../hooks/useAnalytics';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

type ChartType = 'revenue' | 'pipeline' | 'status' | 'activity';

interface AnalyticsChartProps {
  chartType: ChartType;
  dateRange?: { start: string; end: string };
  height?: number;
  showLegend?: boolean;
}

const COLORS = {
  primary: 'rgba(25, 118, 210, 1)',
  primaryLight: 'rgba(25, 118, 210, 0.2)',
  success: 'rgba(76, 175, 80, 1)',
  successLight: 'rgba(76, 175, 80, 0.2)',
  warning: 'rgba(255, 152, 0, 1)',
  warningLight: 'rgba(255, 152, 0, 0.2)',
  error: 'rgba(244, 67, 54, 1)',
  errorLight: 'rgba(244, 67, 54, 0.2)',
  info: 'rgba(33, 150, 243, 1)',
  infoLight: 'rgba(33, 150, 243, 0.2)',
  purple: 'rgba(156, 39, 176, 1)',
  purpleLight: 'rgba(156, 39, 176, 0.2)',
};

const STATUS_PALETTE = [
  COLORS.primary,
  COLORS.success,
  COLORS.warning,
  COLORS.error,
  COLORS.purple,
  COLORS.info,
];

const AnalyticsChart: React.FC<AnalyticsChartProps> = ({
  chartType,
  dateRange,
  height = 350,
  showLegend = true,
}) => {
  const { data, isLoading, error, refetch } = useAnalytics(dateRange);

  const revenueChartData = useMemo((): ChartData<'bar'> | null => {
    if (!data?.revenueByMonth) return null;
    return {
      labels: data.revenueByMonth.map((item) => item.month),
      datasets: [
        {
          label: 'Revenue',
          data: data.revenueByMonth.map((item) => item.revenue),
          backgroundColor: COLORS.primaryLight,
          borderColor: COLORS.primary,
          borderWidth: 2,
          borderRadius: 4,
        },
        {
          label: 'Target',
          data: data.revenueByMonth.map((item) => item.target),
          backgroundColor: COLORS.successLight,
          borderColor: COLORS.success,
          borderWidth: 2,
          borderRadius: 4,
        },
      ],
    };
  }, [data?.revenueByMonth]);

  const pipelineChartData = useMemo((): ChartData<'line'> | null => {
    if (!data?.pipelineHistory) return null;
    return {
      labels: data.pipelineHistory.map((item) => item.date),
      datasets: [
        {
          label: 'Pipeline Value ($)',
          data: data.pipelineHistory.map((item) => item.value),
          borderColor: COLORS.primary,
          backgroundColor: COLORS.primaryLight,
          fill: true,
          tension: 0.3,
          pointRadius: 3,
          pointHoverRadius: 6,
        },
        {
          label: 'Won Deals ($)',
          data: data.pipelineHistory.map((item) => item.wonValue),
          borderColor: COLORS.success,
          backgroundColor: COLORS.successLight,
          fill: true,
          tension: 0.3,
          pointRadius: 3,
          pointHoverRadius: 6,
        },
      ],
    };
  }, [data?.pipelineHistory]);

  const statusChartData = useMemo((): ChartData<'doughnut'> | null => {
    if (!data?.statusDistribution) return null;
    const labels = Object.keys(data.statusDistribution);
    const values = Object.values(data.statusDistribution);
    return {
      labels,
      datasets: [
        {
          data: values,
          backgroundColor: STATUS_PALETTE.slice(0, labels.length),
          borderWidth: 2,
          borderColor: '#fff',
          hoverOffset: 8,
        },
      ],
    };
  }, [data?.statusDistribution]);

  const activityChartData = useMemo((): ChartData<'bar'> | null => {
    if (!data?.activityByWeek) return null;
    return {
      labels: data.activityByWeek.map((item) => item.week),
      datasets: [
        {
          label: 'Calls',
          data: data.activityByWeek.map((item) => item.calls),
          backgroundColor: COLORS.primary,
          borderRadius: 4,
        },
        {
          label: 'Emails',
          data: data.activityByWeek.map((item) => item.emails),
          backgroundColor: COLORS.success,
          borderRadius: 4,
        },
        {
          label: 'Meetings',
          data: data.activityByWeek.map((item) => item.meetings),
          backgroundColor: COLORS.warning,
          borderRadius: 4,
        },
      ],
    };
  }, [data?.activityByWeek]);

  const barOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: showLegend, position: 'top' as const },
      tooltip: {
        mode: 'index',
        intersect: false,
        callbacks: {
          label: (context) => {
            const value = context.parsed.y;
            if (chartType === 'revenue') {
              return `${context.dataset.label}: $${value.toLocaleString()}`;
            }
            return `${context.dataset.label}: ${value}`;
          },
        },
      },
    },
    scales: {
      x: { grid: { display: false } },
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value) =>
            chartType === 'revenue' ? `$${Number(value).toLocaleString()}` : String(value),
        },
      },
    },
  };

  const lineOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: showLegend, position: 'top' as const },
      tooltip: {
        mode: 'index',
        intersect: false,
        callbacks: {
          label: (context) =>
            `${context.dataset.label}: $${context.parsed.y.toLocaleString()}`,
        },
      },
    },
    scales: {
      x: { grid: { display: false } },
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value) => `$${Number(value).toLocaleString()}`,
        },
      },
    },
    interaction: { mode: 'nearest', axis: 'x', intersect: false },
  };

  const doughnutOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '60%',
    plugins: {
      legend: { display: showLegend, position: 'right' as const },
      tooltip: {
        callbacks: {
          label: (context) => {
            const total = (context.dataset.data as number[]).reduce((a, b) => a + b, 0);
            const percentage = ((context.parsed / total) * 100).toFixed(1);
            return `${context.label}: ${context.parsed} (${percentage}%)`;
          },
        },
      },
    },
  };

  if (isLoading) {
    return (
      <div style={styles.loadingContainer}>
        <div style={styles.skeleton} />
        <p style={styles.loadingText}>Loading analytics...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={styles.errorContainer}>
        <p style={styles.errorText}>Failed to load analytics data</p>
        <p style={styles.errorDetail}>{error}</p>
        <button onClick={() => refetch()} style={styles.retryButton}>
          Retry
        </button>
      </div>
    );
  }

  const renderChart = () => {
    switch (chartType) {
      case 'revenue':
        return revenueChartData ? (
          <Bar data={revenueChartData} options={barOptions} />
        ) : null;
      case 'pipeline':
        return pipelineChartData ? (
          <Line data={pipelineChartData} options={lineOptions} />
        ) : null;
      case 'status':
        return statusChartData ? (
          <Doughnut data={statusChartData} options={doughnutOptions} />
        ) : null;
      case 'activity':
        return activityChartData ? (
          <Bar data={activityChartData} options={{ ...barOptions, scales: { ...barOptions.scales, x: { stacked: true }, y: { stacked: true, beginAtZero: true } } }} />
        ) : null;
      default:
        return <p>Unsupported chart type: {chartType}</p>;
    }
  };

  const chartTitles: Record<ChartType, string> = {
    revenue: 'Revenue vs Target',
    pipeline: 'Deal Pipeline Trend',
    status: 'Customer Status Distribution',
    activity: 'Team Activity Overview',
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h3 style={styles.title}>{chartTitles[chartType]}</h3>
        {data?.lastUpdated && (
          <span style={styles.lastUpdated}>
            Updated: {new Date(data.lastUpdated).toLocaleString()}
          </span>
        )}
      </div>

      {data?.summary && chartType === 'revenue' && (
        <div style={styles.summaryRow}>
          <div style={styles.summaryCard}>
            <span style={styles.summaryLabel}>Total Revenue</span>
            <span style={styles.summaryValue}>
              ${data.summary.totalRevenue?.toLocaleString() ?? '0'}
            </span>
          </div>
          <div style={styles.summaryCard}>
            <span style={styles.summaryLabel}>Avg Deal Size</span>
            <span style={styles.summaryValue}>
              ${data.summary.avgDealSize?.toLocaleString() ?? '0'}
            </span>
          </div>
          <div style={styles.summaryCard}>
            <span style={styles.summaryLabel}>Win Rate</span>
            <span style={styles.summaryValue}>
              {data.summary.winRate ?? 0}%
            </span>
          </div>
        </div>
      )}

      <div style={{ height, position: 'relative' as const }}>{renderChart()}</div>
    </div>
  );
};

const styles: Record<string, React.CSSProperties> = {
  container: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 24,
    boxShadow: '0 1px 3px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.06)',
    border: '1px solid #f0f0f0',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  title: {
    margin: 0,
    fontSize: 18,
    fontWeight: 600,
    color: '#1a1a1a',
  },
  lastUpdated: {
    fontSize: 12,
    color: '#999',
  },
  summaryRow: {
    display: 'flex',
    gap: 16,
    marginBottom: 20,
  },
  summaryCard: {
    flex: 1,
    padding: '12px 16px',
    backgroundColor: '#f8f9fa',
    borderRadius: 8,
    display: 'flex',
    flexDirection: 'column' as const,
    gap: 4,
  },
  summaryLabel: {
    fontSize: 12,
    color: '#666',
    fontWeight: 500,
    textTransform: 'uppercase' as const,
    letterSpacing: '0.5px',
  },
  summaryValue: {
    fontSize: 22,
    fontWeight: 700,
    color: '#1a1a1a',
  },
  loadingContainer: {
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 60,
    backgroundColor: '#fff',
    borderRadius: 12,
    border: '1px solid #f0f0f0',
  },
  skeleton: {
    width: '80%',
    height: 200,
    backgroundColor: '#f0f0f0',
    borderRadius: 8,
    marginBottom: 16,
  },
  loadingText: {
    color: '#999',
    fontSize: 14,
  },
  errorContainer: {
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 40,
    backgroundColor: '#fff',
    borderRadius: 12,
    border: '1px solid #fdecea',
  },
  errorText: {
    color: '#d32f2f',
    fontSize: 16,
    fontWeight: 600,
    margin: '0 0 8px 0',
  },
  errorDetail: {
    color: '#999',
    fontSize: 13,
    margin: '0 0 16px 0',
  },
  retryButton: {
    padding: '8px 20px',
    backgroundColor: '#1976d2',
    color: '#fff',
    border: 'none',
    borderRadius: 6,
    cursor: 'pointer',
    fontSize: 14,
    fontWeight: 500,
  },
};

export default AnalyticsChart;


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

