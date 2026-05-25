import React, { useState, useEffect } from "react";
import axios from "axios";
import { ArrowLeft, X } from "lucide-react";

/* ---------------- API BASE ---------------- */

const getApiBaseUrl = () => {
  const env = import.meta.env?.VITE_API_BASE_URL?.trim();
  if (env) return env;
  if (["localhost", "127.0.0.1"].includes(window.location.hostname)) {
    return "http://localhost:8080";
  }
  return "";
};

const API_BASE_URL = getApiBaseUrl();
const apiUrl = (path) =>
  `${API_BASE_URL.replace(/\/+$/, "")}/${path.replace(/^\/+/, "")}`;

/* ---------------- COMPONENT ---------------- */

const AdminManagement = ({ onBack }) => {
  const [admins, setAdmins] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [isEdit, setIsEdit] = useState(false);

  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);

const [currentUserEmail, setCurrentUserEmail] = useState(null);



  const [formData, setFormData] = useState({
    id: null,
    fullName: "",
    email: "",
    designation: "",
    password: "",
  });

  /* ---------------- FETCH ADMINS ---------------- */

  const fetchAdmins = async () => {
    try {
      setLoading(true);
      const res = await axios.get(apiUrl("/api/users/admins"), {
        withCredentials: true,
      });
      setAdmins(res.data.data || []);
    } catch {
      setError("Failed to load admins");
    } finally {
      setLoading(false);
    }
  };

const fetchCurrentUser = async () => {
  try {
    const res = await axios.get(apiUrl("/api/users/me"), {
      withCredentials: true, // HttpOnly cookie auth
    });

    setCurrentUserEmail(res.data.data.email);
  } catch (err) {
    console.error("Failed to fetch current user", err);
  }
};



useEffect(() => {
  fetchAdmins();
  fetchCurrentUser();
}, []);

  /* ---------------- ADD / UPDATE ADMIN ---------------- */

const handleSubmit = async (e) => {
  e.preventDefault();
  try {
    setSubmitting(true);

await axios.put(
  apiUrl(`/api/users/admins/${formData.id}`),
  {
    fullName: formData.fullName,
    designation: formData.designation,
  },
  { withCredentials: true }
);


    await fetchAdmins();
    closeModal();
  } catch (err) {
    alert(err?.response?.data?.message || "Update failed");
  } finally {
    setSubmitting(false);
  }
};


  /* ---------------- DELETE ADMIN ---------------- */

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this admin?")) return;

    try {
await axios.delete(apiUrl(`/api/users/admins/${id}`), {
  withCredentials: true,
});

      setAdmins((prev) => prev.filter((a) => a.id !== id));
    } catch {
      alert("Failed to delete admin");
    }
  };

  /* ---------------- MODAL HELPERS ---------------- */


  const openEditModal = (admin) => {
    setIsEdit(true);
    setFormData({
      id: admin.id,
      fullName: admin.fullName,
      email: admin.email,
      designation: admin.designation || "",
      password: "",
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setIsEdit(false);
  };

  /* ---------------- UI ---------------- */

  return (
    <div className="min-h-screen bg-[#F9FAFF] px-4 md:px-6 py-4">
      {/* HEADER */}
      <div
        className="mb-6 flex justify-between items-center rounded-2xl px-6 py-4 shadow-sm"
        style={{ backgroundColor: "#00008B" }}
      >
        <div>
          <h1 className="text-xl font-bold text-white">Admin Management</h1>
          <p className="text-xs text-blue-100">
            Admins created via employee onboarding
          </p>
        </div>

        <div className="flex gap-2">
          {onBack && (
            <button
              onClick={onBack}
              className="flex items-center gap-1 rounded-full border border-blue-300 px-3 py-1 text-xs text-white"
            >
              <ArrowLeft size={14} /> Back
            </button>
          )}
        </div>
      </div>

      {/* TABLE */}
      <div className="rounded-2xl bg-white border shadow-sm">
        {loading ? (
          <p className="p-6 text-center">Loading...</p>
        ) : error ? (
          <p className="p-6 text-center text-red-500">{error}</p>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-[#F3F4FF] text-[#011A8B]">
              <tr>
                <th className="px-4 py-3 text-left">Employee ID</th>
                <th className="px-4 py-3 text-left">Admin Name</th>
                <th className="px-4 py-3 text-left">Email</th>
                <th className="px-4 py-3 text-left">Designation</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
<tbody>
  {admins.map((admin) => {
    const isSelf = admin.email === currentUserEmail;


    return (
      <tr key={admin.id} className="border-t hover:bg-slate-50">
        <td className="px-4 py-3">{admin.employeeId}</td>
        <td className="px-4 py-3">{admin.fullName}</td>
        <td className="px-4 py-3">{admin.email}</td>
        <td className="px-4 py-3">{admin.designation || "-"}</td>

        <td className="px-4 py-3 text-right">
          {/* Edit always allowed */}
          <button
            className="mr-3 text-blue-600 hover:underline"
            onClick={() => openEditModal(admin)}
          >
            Edit
          </button>

          {/* Delete disabled for logged-in admin */}
          <button
            title={
              isSelf
                ? "You cannot delete your own account"
                : "Delete admin"
            }
            disabled={isSelf}
            onClick={() => {
              if (!isSelf) handleDelete(admin.id);
            }}
            className={
              isSelf
                ? "text-gray-400 cursor-not-allowed"
                : "text-red-600 hover:underline"
            }
          >
            Delete
          </button>
        </td>
      </tr>
    );
  })}
</tbody>

          </table>
        )}
      </div>

      {/* MODAL */}
      {showModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center">
          <div className="bg-white w-full max-w-md rounded-xl shadow-lg">
            <div className="flex justify-between items-center p-4 border-b">
            <h2 className="font-semibold">Edit Admin</h2>

              <X className="cursor-pointer" onClick={closeModal} />
            </div>

            <form onSubmit={handleSubmit} className="p-4 space-y-3">
              <input
                placeholder="Full Name"
                className="w-full border rounded px-3 py-2"
                value={formData.fullName}
                onChange={(e) =>
                  setFormData({ ...formData, fullName: e.target.value })
                }
                required
              />

              <input
                placeholder="Designation"
                className="w-full border rounded px-3 py-2"
                value={formData.designation}
                onChange={(e) =>
                  setFormData({ ...formData, designation: e.target.value })
                }
              />

              {!isEdit && (
                <input
                  placeholder="Password"
                  type="password"
                  className="w-full border rounded px-3 py-2"
                  value={formData.password}
                  onChange={(e) =>
                    setFormData({ ...formData, password: e.target.value })
                  }
                  required
                />
              )}

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={closeModal}
                  className="px-4 py-1 border rounded"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-1 bg-[#011A8B] text-white rounded"
                >
                  {submitting ? "Saving..." : "Save"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminManagement;
