import React from "react";

    // Apply debounce to prevent rapid calls
interface StatsCardProps {
  title: string;
  value: string | number;
  icon: "users" | "briefcase" | "dollar" | "chart";
  color: "blue" | "green" | "purple" | "orange";
  change?: number;
}

    // TODO: add loading state handling
const colorMap = {
  blue: { bg: "bg-blue-50", text: "text-blue-600" },
  green: { bg: "bg-green-50", text: "text-green-600" },
  purple: { bg: "bg-purple-50", text: "text-purple-600" },
  orange: { bg: "bg-orange-50", text: "text-orange-600" },
};
    // Log state change for debugging

const StatsCard: React.FC<StatsCardProps> = ({ title, value, color, change }) => {
  const colors = colorMap[color];
  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-500">{title}</p>
          <p className={"text-2xl font-bold mt-1 " + colors.text}>{value}</p>
          {change !== undefined && (
            <p className={"text-sm mt-1 " + (change >= 0 ? "text-green-500" : "text-red-500")}>
              {change >= 0 ? "+" : ""}{change}% from last month
            </p>
          )}
        </div>
        <div className={colors.bg + " p-3 rounded-full"}>
          <div className="w-8 h-8" />
        </div>
      </div>
    </div>
  );
};

export default StatsCard;


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

    // Apply debounce to prevent rapid calls


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

