import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import { Eye, Pencil, Trash2 } from "lucide-react";


const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  (window.location.hostname === "localhost"
    ? "http://localhost:8080"
    : "");



export default function ManageEmployees() {
  const [search, setSearch] = useState("");
  const [showEditModal, setShowEditModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [currentEmployee, setCurrentEmployee] = useState(null);

const [editForm, setEditForm] = useState({
  id: null,
  employeeId:"",
  fullName: "",
  designation: "",
  department: "",
  employmentType: "",
  gender: "",
  reportingManager: "",
  shiftType: "",
  workLocation: "",
  joiningDate: "",
  dob: "",
  mobile: "",
  emergencyContact: "",
  status: "",
});


  const primaryBlue = "#00008B";

  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  // ========== API ==========

const extractEmployeeList = useCallback((data) => {

const normalizeEmployee = (e) => ({
  id: e.id,                       // internal DB id (keep for edit/delete)
  employeeId: e.employeeId ?? "", // REAL employee id (admin-defined)
  name: e.fullName ?? e.name ?? "",
  email: e.email ?? "",
  mobile: e.mobile ?? "",
  emergencyContact: e.emergencyContact ?? "",
  gender: e.gender ?? "",
  dob: e.dob ?? "",
  department: e.department ?? "",
  designation: e.designation ?? "",
  employmentType: e.employmentType ?? "",
  reportingManager: e.reportingManager ?? "",
  shiftType: e.shiftType ?? "",
  workLocation: e.workLocation ?? "",
  joiningDate: e.joiningDate ?? "",
  status: e.status ?? "",
});



  if (!data) return [];

  if (Array.isArray(data)) return data.map(normalizeEmployee);
  if (Array.isArray(data.content)) return data.content.map(normalizeEmployee);
  if (Array.isArray(data.data)) return data.data.map(normalizeEmployee);
  if (Array.isArray(data.users)) return data.users.map(normalizeEmployee);
  if (Array.isArray(data.employees)) return data.employees.map(normalizeEmployee);

  return [];
}, []);



const fetchEmployees = useCallback(async () => {
  try {
    setLoading(true);
    setError(null);

const res = await axios.get(
  `${API_BASE_URL}/api/users`,
  { withCredentials: true }
);


    const list = extractEmployeeList(res.data);
    setEmployees(list);
  } catch (err) {
    console.error("Error fetching employees", err);
    setError("Failed to load employees");
    setEmployees([]);
  } finally {
    setLoading(false);
  }
}, [extractEmployeeList]);


const deleteEmployeeApi = async (empId) => {
  return axios.delete(
    `${API_BASE_URL}/api/users/employees/${empId}`,
    { withCredentials: true }
  );
};

  // ========== EFFECTS ==========
useEffect(() => {
  axios
    .get(`${API_BASE_URL}/api/auth/me`, { withCredentials: true })
    .then(() => {
      fetchEmployees();
    })
    .catch(() => {
      window.location.href = "/login";
    });
}, [fetchEmployees]);

  // ========== FILTERING ==========

  const filtered = employees.filter((emp) => {
    const s = search.trim().toLowerCase();
    if (!s) return true;

const searchMatch =
  emp.employeeId?.toString().toLowerCase().includes(s) ||
  emp.name?.toLowerCase().includes(s) ||
  emp.email?.toLowerCase().includes(s) ||
  emp.designation?.toLowerCase().includes(s) ||
  emp.department?.toLowerCase().includes(s) ||
  emp.employmentType?.toLowerCase().includes(s) ||
  emp.gender?.toLowerCase().includes(s);

    return !!searchMatch;
  });

  // View Modal
  const handleView = (emp) => {
    setCurrentEmployee(emp);
    setShowViewModal(true);
  };

  // Edit Modal
  const toDateInput = (date) =>
  date ? date.split("T")[0] : "";

  const formatDate = (date) => {
  if (!date) return "-";
  return new Date(date).toLocaleDateString();
};

  const handleEdit = (emp) => {
    setCurrentEmployee(emp);
setEditForm({
  id: emp.id,
  employeeId: emp.employeeId,
  fullName: emp.name || "",
  designation: emp.designation || "",
  department: emp.department || "",
  employmentType: emp.employmentType || "",
  gender: emp.gender || "",
  reportingManager: emp.reportingManager || "",
  shiftType: emp.shiftType || "",
  workLocation: emp.workLocation || "",
  joiningDate: toDateInput(emp.joiningDate),
  dob: toDateInput(emp.dob),
  mobile: emp.mobile || "",
  emergencyContact: emp.emergencyContact || "",
  status: emp.status || "",
});

    setShowEditModal(true);
  };

 const handleEditSubmit = async (e) => {
  e.preventDefault();

  try {
    setSaving(true);

    const payload = {
      fullName: editForm.fullName,
      designation: editForm.designation,
      department: editForm.department,
      employmentType: editForm.employmentType,
      gender: editForm.gender,
      reportingManager: editForm.reportingManager,
      shiftType: editForm.shiftType,
      workLocation: editForm.workLocation,
      joiningDate: editForm.joiningDate,
      dob: editForm.dob,
      mobile: editForm.mobile,
      emergencyContact: editForm.emergencyContact,
      status: editForm.status,
    };

    await axios.put(
      `${API_BASE_URL}/api/users/employees/${editForm.id}`,
      payload,
      { withCredentials: true }
    );

    // update UI instantly
    setEmployees((prev) =>
      prev.map((emp) =>
        emp.id === editForm.id
          ? { ...emp, ...payload, name: payload.fullName }
          : emp
      )
    );

    setShowEditModal(false);
    setCurrentEmployee(null);
  } catch (err) {
    console.error("Error updating employee", err);
    alert("Failed to update employee");
  } finally {
    setSaving(false);
  }
};


  // Delete
  const handleDelete = async (emp) => {
    if (!window.confirm(`Delete ${emp.name}?`)) return;
    try {
      await deleteEmployeeApi(emp.id);
      setEmployees((prev) => prev.filter((e) => e.id !== emp.id));
    } catch (err) {
      console.error("Error deleting employee", err);
      alert("Failed to delete employee");
    }
  };

  return (
    <div className="min-h-screen px-4 md:px-6 py-4">
      {/* Header */}
      <div
        className="rounded-2xl px-6 py-5 flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-6"
        style={{ backgroundColor: primaryBlue }}
      >
        <div>
          <h1 className="text-xl md:text-2xl font-bold text-white">
            Employee Management
          </h1>
          <p className="text-xs md:text-sm text-blue-100 mt-1 max-w-xl">
            Manage employee records, update details, and perform all actions
            from one place.
          </p>
        </div>
        <button
          onClick={fetchEmployees}
          className="text-xs px-3 py-2 rounded-full bg-white/10 text-white border border-white/30 hover:bg-white/20"
        >
          Refresh List
        </button>
      </div>

      <div className="flex flex-col md:flex-row gap-4 mb-6">
        {/* Search */}
        <input
          type="text"
          placeholder="Search by ID, name, email, department, designation..."

          className="w-full md:w-1/2 px-4 py-2 border border-[#000080] rounded-lg
                     text-[#000080] placeholder-gray-400
                     focus:ring-2 focus:ring-[#000080] outline-none"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {error && (
        <p className="text-xs text-red-600 mb-2">
          {error}
        </p>
      )}

      {/* Table */}
      <div className="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
<thead className="bg-[#000080] text-white">
  <tr>
    <th className="py-3 px-4 text-left">Employee ID</th>
    <th className="py-3 px-4 text-left">Employee</th>
    <th className="py-3 px-4 text-left">Email</th>
    <th className="py-3 px-4 text-left">Department</th>
    <th className="py-3 px-4 text-left">Employment Type</th>
    <th className="py-3 px-4 text-left">Gender</th>
    <th className="py-3 px-4 text-left">Reporting Manager</th>
    <th className="py-3 px-4 text-center">Actions</th>
  </tr>
</thead>


          <tbody>
            {loading ? (
              <tr>
                <td
                  colSpan={8}
                  className="py-6 px-4 text-center text-sm text-gray-500"
                >
                  Loading employees...
                </td>
              </tr>
            ) : filtered.length === 0 ? (
              <tr>
                <td
                  colSpan={8}
                  className="py-6 px-4 text-center text-sm text-gray-500"
                >
                  No employees found.
                </td>
              </tr>
            ) : (
              filtered.map((emp) => (
                <tr
                  key={emp.id}
                  className="border-b border-gray-200 hover:bg-gray-50"
                >
                  {/* EMPLOYEE ID */}
                  <td className="py-4 px-4 text-[#000080] font-semibold">
                      {emp.employeeId || "-"}
                  </td>

                  {/* EMPLOYEE NAME + ROLE */}
                  <td className="py-4 px-4">
                    <p className="text-[#000080] font-semibold">
                      {emp.name}
                    </p>
                    <p className="text-sm text-gray-500">
                   {emp.designation || "-"}
                    </p>

                  </td>

                  {/* EMAIL */}
                  <td className="py-4 px-4 text-[#000080] font-medium">
                    {emp.email}
                  </td>

                  {/* Department */}
                   <td className="py-4 px-4">
                     {emp.department || "-"}
                   </td>

                  {/* Employment Type */}
                   <td className="py-4 px-4">
                    {emp.employmentType || "-"}
                  </td>

                  {/* Gender */}
                     <td className="py-4 px-4">
                       {emp.gender || "-"}
                      </td>

  {/* Reporting Manager */}
  <td className="py-4 px-4">
    {emp.reportingManager || "-"}
  </td>

                  {/* ACTION BUTTONS */}
<td className="py-4 px-4 flex justify-center gap-3">
  {/* View */}
  <button
    onClick={() => handleView(emp)}
    title="View employee"
    className="p-2 rounded-lg bg-blue-50 text-blue-700 hover:bg-blue-100"
  >
    <Eye size={18} />
  </button>

  {/* Edit */}
  <button
    onClick={() => handleEdit(emp)}
    title="Edit employee"
    className="p-2 rounded-lg bg-emerald-50 text-emerald-700 hover:bg-emerald-100"
  >
    <Pencil size={18} />
  </button>

  {/* Delete */}
<button
  onClick={() => handleDelete(emp)}
  title="Delete employee"
  className="p-2 rounded-lg bg-red-50 text-red-700 hover:bg-red-100"
>
  <Trash2 size={18} />
</button>
</td>
</tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* ---------- VIEW MODAL ---------- */}
      {showViewModal && currentEmployee && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white w-[90%] max-w-md rounded-xl p-6 shadow-lg border-t-4 border-[#000080]">
            <h2 className="text-xl font-bold text-[#000080] mb-4">
              Employee Details
            </h2>
<div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm text-gray-700">
  <p><strong>Employee ID:</strong>{" "}{currentEmployee.employeeId || "-"}</p>

  <p><strong>Name:</strong> {currentEmployee.name}</p>

  <p><strong>Email:</strong> {currentEmployee.email}</p>
  <p><strong>Mobile:</strong> {currentEmployee.mobile || "-"}</p>

  <p><strong>Emergency Contact:</strong> {currentEmployee.emergencyContact || "-"}</p>
  <p><strong>Gender:</strong> {currentEmployee.gender || "-"}</p>

  <p><strong>Date of Birth:</strong> {formatDate(currentEmployee.dob)}</p>
  <p><strong>Joining Date:</strong> {formatDate(currentEmployee.joiningDate)}</p>

  <p><strong>Department:</strong> {currentEmployee.department || "-"}</p>
  <p><strong>Designation:</strong> {currentEmployee.designation || "-"}</p>

  <p><strong>Employment Type:</strong> {currentEmployee.employmentType || "-"}</p>
  <p><strong>Shift Type:</strong> {currentEmployee.shiftType || "-"}</p>

  <p><strong>Reporting Manager:</strong> {currentEmployee.reportingManager || "-"}</p>
  <p><strong>Work Location:</strong> {currentEmployee.workLocation || "-"}</p>
</div>


            <button
              onClick={() => setShowViewModal(false)}
              className="mt-6 w-full py-2 bg-[#000080] text-white rounded-lg"
            >
              Close
            </button>
          </div>
        </div>
      )}

      {/* ---------- EDIT MODAL ---------- */}
      {showEditModal && currentEmployee && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white w-[95%] max-w-2xl rounded-3xl p-8 shadow-2xl max-h-[90vh] overflow-y-auto">
            {/* Header */}
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-semibold text-[#000080]">
                Edit Employee
              </h2>
              <button
                onClick={() => setShowEditModal(false)}
                className="text-gray-400 hover:text-gray-700"
              >
                ✕
              </button>
            </div>

            {/* Form */}
            <form onSubmit={handleEditSubmit} className="space-y-5">
              {/* Employee ID (read-only) */}
              <div className="space-y-1">
                <label className="text-sm font-medium text-gray-500">
                  Employee ID:
                </label>
                <input
                  type="text"
                  value={editForm.employeeId}
                  readOnly
                  className="w-full rounded-2xl bg-gray-100 text-gray-700 px-4 py-3 border-none outline-none"
                />
              </div>
<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
{/* Full Name */}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Full Name:
  </label>
  <input
    type="text"
    value={editForm.fullName}
    onChange={(e) =>
      setEditForm({ ...editForm, fullName: e.target.value })
    }
    className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3 border-none focus:ring-2 focus:ring-indigo-500 outline-none"
  />
</div>

{/*Designation*/}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Designation:
  </label>
  <input
    type="text"
    value={editForm.designation}
    onChange={(e) =>
      setEditForm({ ...editForm, designation: e.target.value })
    }
    className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3"
  />
</div>


{/*Department*/}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Department:
  </label>
<input
  type="text"
  value={editForm.department}
  onChange={(e) =>
    setEditForm({ ...editForm, department: e.target.value })
  }
  className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3 border-none focus:ring-2 focus:ring-indigo-500 outline-none"
/>
</div>

{/*Employment Type*/}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Employment Type:
  </label>
<select
  value={editForm.employmentType}
  onChange={(e) =>
    setEditForm({ ...editForm, employmentType: e.target.value })
  }
  className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3"
>

  <option value="">Select Employment Type</option>
  <option value="FULL_TIME">Full Time</option>
  <option value="CONTRACT">Contract</option>
  <option value="INTERN">Intern</option>
</select>
</div>

{/*Gender*/}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Gender:
  </label>
<select
  value={editForm.gender}
  onChange={(e) =>
    setEditForm({ ...editForm, gender: e.target.value })
  }
  className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3"
>
  <option value="">Select Gender</option>
  <option value="MALE">Male</option>
  <option value="FEMALE">Female</option>
  <option value="OTHER">Other</option>
</select>
</div>

{/*Reporting Manager*/}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Reporting Manager:
  </label>
<input
  type="text"
  value={editForm.reportingManager}
  onChange={(e) =>
    setEditForm({ ...editForm, reportingManager: e.target.value })
  }
  className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3 border-none focus:ring-2 focus:ring-indigo-500 outline-none"
/>
</div>


{/*Shift Type*/}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Shift Type:
  </label>
<select
  value={editForm.shiftType}
  onChange={(e) =>
    setEditForm({ ...editForm, shiftType: e.target.value })
  }
  className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3"
>
  <option value="">Select Shift</option>
  <option value="GENERAL">General</option>
  <option value="MORNING">Morning</option>
  <option value="EVENING">Evening</option>
  <option value="NIGHT">Night</option>
  <option value="ROTATIONAL">Rotational</option>
  <option value="FLEXIBLE">Flexible</option>
  <option value="ON_CALL">On Call</option>
</select>
</div>



{/*Work Location*/}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Work Location:
  </label>
<input
  type="text"
  value={editForm.workLocation}
  onChange={(e) =>
    setEditForm({ ...editForm, workLocation: e.target.value })
  }
  className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3 border-none focus:ring-2 focus:ring-indigo-500 outline-none"
/>
</div>

{/*Mobile*/}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Mobile:
  </label>
<input
  type="text"
  value={editForm.mobile}
  onChange={(e) =>
    setEditForm({ ...editForm, mobile: e.target.value })
  }
  className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3 border-none focus:ring-2 focus:ring-indigo-500 outline-none"
/>
</div>

{/*Emergency Contact*/}

<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Emergency Contact:
  </label>
<input
  type="text"
  value={editForm.emergencyContact}
  onChange={(e) =>
    setEditForm({ ...editForm, emergencyContact: e.target.value })
  }
  className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3 border-none focus:ring-2 focus:ring-indigo-500 outline-none"
/>
</div>

<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Date of Birth:
  </label>
  <input
    type="date"
    value={editForm.dob}
    onChange={(e) =>
      setEditForm({ ...editForm, dob: e.target.value })
    }
    className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3 border-none focus:ring-2 focus:ring-indigo-500 outline-none"
  />
</div>

<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Joining Date:
  </label>
  <input
    type="date"
    value={editForm.joiningDate}
    onChange={(e) =>
      setEditForm({ ...editForm, joiningDate: e.target.value })
    }
    className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3 border-none focus:ring-2 focus:ring-indigo-500 outline-none"
  />
</div>



{/*Status*/}
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-500">
    Status:
  </label>
<select
  value={editForm.status}
  onChange={(e) =>
    setEditForm({ ...editForm, status: e.target.value })
  }
  className="w-full rounded-2xl bg-[#020617] text-white px-4 py-3"
>
  <option value="">Select Status</option>
  <option value="ACTIVE">Active</option>
  <option value="INACTIVE">Inactive</option>
</select>
</div>
</div>



              {/* Save Button */}
              <div className="pt-4 flex justify-center">
                <button
                  type="submit"
                  disabled={saving}
                  className="w-full max-w-sm py-3 rounded-2xl bg-[#4f46e5] hover:bg-[#4338ca] text-white font-semibold text-sm tracking-wide shadow-md disabled:opacity-60"
                >
                  {saving ? "Saving..." : "Save Changes"}
                </button>
              </div>

              {/* Cancel */}
              <div className="flex justify-center">
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="mt-2 text-xs text-gray-500 hover:text-gray-700"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
