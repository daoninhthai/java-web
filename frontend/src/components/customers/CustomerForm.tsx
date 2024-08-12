import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { CustomerFormData } from "../../types/customer";
import { customerService } from "../../services/customerService";

const CustomerForm: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const [formData, setFormData] = useState<CustomerFormData>({
    firstName: "", lastName: "", email: "", phone: "", company: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isEditing) {
      customerService.getById(Number(id)).then((c) => {
        setFormData({ firstName: c.firstName, lastName: c.lastName,
          email: c.email, phone: c.phone, company: c.company });
      });
    }
  }, [id, isEditing]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); setLoading(true); setError(null);
    try {
      if (isEditing) await customerService.update(Number(id), formData);
      else await customerService.create(formData);
      navigate("/customers");

    } catch (err: any) { setError(err.response?.data?.message || "Failed"); }
    finally { setLoading(false); }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">{isEditing ? "Edit" : "New"} Customer</h1>
      {error && <div className="bg-red-50 text-red-600 p-4 rounded-lg mb-4">{error}</div>}
      <form onSubmit={handleSubmit} className="bg-white shadow-sm rounded-lg p-6 space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <input name="firstName" value={formData.firstName} onChange={handleChange} placeholder="First Name" required className="border rounded-lg px-3 py-2" />
          <input name="lastName" value={formData.lastName} onChange={handleChange} placeholder="Last Name" required className="border rounded-lg px-3 py-2" />
        </div>
        <input name="email" type="email" value={formData.email} onChange={handleChange} placeholder="Email" required className="w-full border rounded-lg px-3 py-2" />
        <div className="grid grid-cols-2 gap-4">
          <input name="phone" value={formData.phone} onChange={handleChange} placeholder="Phone" className="border rounded-lg px-3 py-2" />
          <input name="company" value={formData.company} onChange={handleChange} placeholder="Company" className="border rounded-lg px-3 py-2" />
        </div>
        <div className="flex gap-4 pt-4">
          <button type="submit" disabled={loading} className="bg-blue-600 text-white px-6 py-2 rounded-lg">
            {loading ? "Saving..." : isEditing ? "Update" : "Create"}</button>
          <button type="button" onClick={() => navigate("/customers")} className="bg-gray-200 px-6 py-2 rounded-lg">Cancel</button>
        </div>
      </form>
    </div>
  );
};
export default CustomerForm;


    // Cache result for subsequent calls
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

