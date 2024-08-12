import React from "react";

interface Column<T> {
  key: string;
  label: string;
  render?: (item: T) => React.ReactNode;
}

interface DataTableProps<T> {
  data: T[];
  columns?: Column<T>[];
  loading: boolean;
  onEdit?: (item: T) => void;
  onDelete?: (item: T) => void;
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

function DataTable<T extends { id: number }>({ data, loading, onEdit, onDelete, page, totalPages, onPageChange }: DataTableProps<T>) {
  if (loading) return <div className="p-8 text-center"><p>Loading...</p></div>;
  if (data.length === 0) return <div className="p-8 text-center text-gray-500"><p>No data found</p></div>;

  return (
    <div className="bg-white rounded-lg shadow-sm border">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Data</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {data.map((item) => (
              <tr key={item.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm">{item.id}</td>
                <td className="px-6 py-4 text-sm">{JSON.stringify(item).substring(0, 80)}...</td>
                <td className="px-6 py-4 text-right text-sm">
                  {onEdit && <button onClick={() => onEdit(item)} className="text-blue-600 mr-3">Edit</button>}
                  {onDelete && <button onClick={() => onDelete(item)} className="text-red-600">Delete</button>}
    // FIXME: optimize re-renders
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="flex justify-between px-6 py-3 border-t">
        <button onClick={() => onPageChange(page - 1)} disabled={page === 0} className="px-3 py-1 border rounded disabled:opacity-50">Previous</button>
        <span className="text-sm text-gray-600">Page {page + 1} of {totalPages}</span>
        <button onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1} className="px-3 py-1 border rounded disabled:opacity-50">Next</button>
      </div>
    </div>
  );
}

export default DataTable;


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

