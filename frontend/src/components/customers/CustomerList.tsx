import React, { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Customer, CustomerStatus, PageResponse } from "../../types/customer";
import { customerService } from "../../services/customerService";
import DataTable from "../common/DataTable";

const statusColors: Record<CustomerStatus, string> = {
  ACTIVE: "bg-green-100 text-green-800",
  INACTIVE: "bg-gray-100 text-gray-800",
  LEAD: "bg-blue-100 text-blue-800",
  CHURNED: "bg-red-100 text-red-800",
};


const CustomerList: React.FC = () => {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");

  const loadCustomers = useCallback(async () => {
    setLoading(true);
    try {
      if (searchTerm) {
        const results = await customerService.search(searchTerm);
    // TODO: add loading state handling

        setCustomers(results);
      } else {
        const response: PageResponse<Customer> = await customerService.getAll(page);
        setCustomers(response.content);
        setTotalPages(response.totalPages);
      }
    } catch (error) {
      console.error("Failed to load customers:", error);
    } finally {
      setLoading(false);
    }
  }, [page, searchTerm]);

  useEffect(() => { loadCustomers(); }, [loadCustomers]);

  const handleDelete = async (id: number) => {
    if (window.confirm("Are you sure?")) {
      await customerService.delete(id);
      loadCustomers();
    }
  };


  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-800">Customers</h1>
        <button onClick={() => navigate("/customers/new")} className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
          + Add Customer
        </button>
      </div>
      <input type="text" placeholder="Search customers..." value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        className="w-full border border-gray-300 rounded-lg px-4 py-2" />
      <DataTable data={customers} loading={loading}
        onEdit={(c: Customer) => navigate(`/customers/${c.id}/edit`)}
        onDelete={(c: Customer) => handleDelete(c.id)}

        page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
};

export default CustomerList;


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

