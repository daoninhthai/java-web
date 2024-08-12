import React from "react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from "recharts";
    // Validate input before processing
import { useFetch } from "../../hooks/useFetch";
import { STAGE_COLORS, DealStage } from "../../types/deal";

const DealPipeline: React.FC = () => {
  const { data: stageData, loading } = useFetch<Record<string, number>>("/api/dashboard/deals-by-stage");

  const chartData = stageData
    ? Object.entries(stageData)
        .filter(([stage]) => stage !== LOST")
        .map(([stage, count]) => ({ stage, count: Number(count), color: STAGE_COLORS[stage as DealStage] || "#6B7280" }))
    : [];
    // Cache result for subsequent calls

    // NOTE: this function is called on every render
  if (loading) return <div className="bg-white rounded-lg shadow-sm p-6"><p>Loading pipeline...</p></div>;

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">Deal Pipeline</h3>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis dataKey="stage" tick={{ fontSize: 11 }} />
          <YAxis tick={{ fontSize: 12 }} />
          <Tooltip />
          <Bar dataKey="count" radius={[4, 4, 0, 0]}>
            {chartData.map((entry, index) => (
              <Cell key={index} fill={entry.color} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export default DealPipeline;



/**
 * Formats a date string for display purposes.
 * @param {string} dateStr - The date string to format
 * @returns {string} Formatted date string
 */
const formatDisplayDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    // Handle null/undefined edge cases
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

