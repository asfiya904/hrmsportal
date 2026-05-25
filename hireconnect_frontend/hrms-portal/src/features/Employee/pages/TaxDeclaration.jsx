'use client';

import React, { useEffect, useState, useCallback } from "react";
import axios from "axios";

/* ---------- AXIOS SETUP ---------- */
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // ✅ HttpOnly cookie auth
});

export default function TaxDeclaration() {
  const [declarations, setDeclarations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  /* ---------- FETCH DATA ---------- */
  const fetchDeclarations = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const res = await api.get("/api/employee/tax-declarations");
      setDeclarations(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error("Failed to load tax declarations", err);
      setError("Unable to load tax declarations.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDeclarations();
  }, [fetchDeclarations]);

  /* ---------- HELPERS ---------- */
  const statusBadge = (status) => {
    if (status === "SUBMITTED")
      return "text-emerald-700 bg-emerald-50 border border-emerald-200";
    if (status === "APPROVED")
      return "text-blue-700 bg-blue-50 border border-blue-200";
    if (status === "REJECTED")
      return "text-red-700 bg-red-50 border border-red-200";
    return "text-amber-700 bg-amber-50 border border-amber-200";
  };

  return (
    <div className="w-full">
      {/* Page Title */}
      <h2 className="text-xl font-semibold text-[#011A8B] mb-4">
        Tax Declarations
      </h2>

      {/* Error */}
      {error && (
        <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {/* Table */}
      <div className="overflow-x-auto bg-white rounded-2xl shadow-sm border border-gray-200">
        <table className="min-w-full">
          <thead className="bg-[#F3F4FF] text-[#011A8B] text-sm font-semibold">
            <tr>
              <th className="px-4 py-3 text-left">Financial Year</th>
              <th className="px-4 py-3 text-left">Total Income</th>
              <th className="px-4 py-3 text-left">Taxable Income</th>
              <th className="px-4 py-3 text-left">Tax Payable</th>
              <th className="px-4 py-3 text-left">Status</th>
              <th className="px-4 py-3 text-left">Submitted</th>
              <th className="px-4 py-3 text-left">Actions</th>
            </tr>
          </thead>

          <tbody className="text-sm text-gray-700">
            {/* Loading */}
            {loading && (
              <tr>
                <td colSpan={7} className="px-4 py-6 text-center text-gray-500">
                  Loading tax declarations…
                </td>
              </tr>
            )}

            {/* Empty */}
            {!loading && declarations.length === 0 && (
              <tr>
                <td colSpan={7} className="px-4 py-6 text-center text-gray-500">
                  No tax declarations found.
                </td>
              </tr>
            )}

            {/* Data */}
            {declarations.map((d) => (
              <tr
                key={d.id}
                className="border-t hover:bg-gray-50 transition"
              >
                <td className="px-4 py-3 font-medium">
                  {d.financialYear}
                </td>

                <td className="px-4 py-3">
                  {d.totalIncome != null ? `₹${d.totalIncome}` : "—"}
                </td>

                <td className="px-4 py-3">
                  {d.taxableIncome != null ? `₹${d.taxableIncome}` : "—"}
                </td>

                <td className="px-4 py-3">
                  {d.taxPayable != null ? `₹${d.taxPayable}` : "—"}
                </td>

                <td className="px-4 py-3">
                  <span
                    className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${statusBadge(
                      d.status
                    )}`}
                  >
                    {d.status || "PENDING"}
                  </span>
                </td>

                <td className="px-4 py-3 text-gray-500">
                  {d.submittedAt
                    ? new Date(d.submittedAt).toLocaleDateString()
                    : "No"}
                </td>

                <td className="px-4 py-3">
                  {d.status === "PENDING" ? (
                    <button
                      onClick={() =>
                        window.location.href = `/employee/tax-declaration/${d.id}`
                      }
                      className="text-xs font-semibold text-[#011A8B] hover:underline"
                    >
                      Fill / Edit
                    </button>
                  ) : (
                    <button
                      onClick={() =>
                        window.location.href = `/employee/tax-declaration/${d.id}`
                      }
                      className="text-xs text-gray-600 hover:underline"
                    >
                      View
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
