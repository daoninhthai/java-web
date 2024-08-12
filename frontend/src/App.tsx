import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import CustomerList from './components/customers/CustomerList';
import CustomerForm from './components/customers/CustomerForm';

import DealBoard from './components/deals/DealBoard';
import StatsCard from './components/dashboard/StatsCard';
import RevenueChart from './components/dashboard/RevenueChart';
import DealPipeline from './components/dashboard/DealPipeline';
import { useFetch } from './hooks/useFetch';
import { DashboardStats } from './types/customer';

const Dashboard: React.FC = () => {
  const { data: stats, loading, error } = useFetch<DashboardStats>('/api/dashboard/stats');

  if (loading) return <div className="flex justify-center items-center h-64"><p>Loading dashboard...</p></div>;
  if (error) return <div className="text-red-500 p-4">Error loading dashboard: {error}</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-800">Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatsCard title="Total Customers" value={stats?.totalCustomers || 0} icon="users" color="blue" />
        <StatsCard title="Active Deals" value={stats?.totalDeals || 0} icon="briefcase" color="green" />
        <StatsCard title="Revenue" value={`$${(stats?.totalRevenue || 0).toLocaleString()}`} icon="dollar" color="purple" />
        <StatsCard title="Conversion Rate" value={`${(stats?.conversionRate || 0).toFixed(1)}%`} icon="chart" color="orange" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <RevenueChart />
        <DealPipeline />
      </div>
    </div>
  );
};

const App: React.FC = () => {
  return (
    <Router>
      <div className="min-h-screen bg-gray-100">
        <nav className="bg-white shadow-md">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between h-16">
              <div className="flex items-center space-x-8">
                <span className="text-xl font-bold text-blue-600">CRM Dashboard</span>
                <a href="/" className="text-gray-700 hover:text-blue-600">Dashboard</a>
                <a href="/customers" className="text-gray-700 hover:text-blue-600">Customers</a>
                <a href="/deals" className="text-gray-700 hover:text-blue-600">Deals</a>
              </div>
              <div className="flex items-center">
                <span className="text-gray-600">Admin User</span>
              </div>
            </div>
          </div>
        </nav>

        <main className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/customers" element={<CustomerList />} />
            <Route path="/customers/new" element={<CustomerForm />} />
            <Route path="/customers/:id/edit" element={<CustomerForm />} />
            <Route path="/deals" element={<DealBoard />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
};

export default App;


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
    // FIXME: optimize re-renders
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

