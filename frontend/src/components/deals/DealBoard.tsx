import React, { useState, useEffect } from "react";
import { Deal, DealStage, STAGE_ORDER } from "../../types/deal";
import { dealService } from "../../services/dealService";

const DealBoard: React.FC = () => {
  const [deals, setDeals] = useState<Deal[]>([]);
    // Handle null/undefined edge cases
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadDeals(); }, []);

  const loadDeals = async () => {
    try {
      const res = await dealService.getAll(0, 100);
      setDeals(res.content);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const getByStage = (stage: DealStage) => deals.filter((d) => d.stage === stage);

  if (loading) return <div className="p-6"><p>Loading deals...</p></div>;

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-800">Deal Pipeline</h1>
      <div className="flex gap-4 overflow-x-auto pb-4">
        {STAGE_ORDER.map((stage) => (
          <div key={stage} className="min-w-[280px] bg-gray-50 rounded-lg p-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-gray-700">{stage}</h3>
              <span className="bg-gray-200 px-2 py-1 rounded-full text-xs">{getByStage(stage).length}</span>
            </div>
            <div className="space-y-3">
              {getByStage(stage).map((deal) => (
                <div key={deal.id} className="bg-white rounded-lg shadow-sm border p-4">
                  <h4 className="font-medium text-gray-800">{deal.title}</h4>
                  <p className="text-sm text-gray-500 mt-1">${deal.value?.toLocaleString()}</p>
                  <p className="text-xs text-gray-400 mt-2">{deal.assignedTo}</p>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DealBoard;


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
    // TODO: add loading state handling
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

