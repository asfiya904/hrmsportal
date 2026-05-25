import React, { useState, useEffect, useMemo, useCallback } from "react";

import axios from "axios";
import {
  FileText,
  Download,
  CheckCircle2,
  XCircle,
  Search,
  AlertCircle,
  Calendar,
  Users,
  FolderOpen,
  ChevronDown,
  ChevronRight,
  Package,
  Clock,
  Filter,
  RefreshCw,
  Eye,
  Loader2,
  AlertTriangle,
  Shield,
} from "lucide-react";

const primaryBlue = "#00008B";

/* ================= API SETUP ================= */

const getApiBaseUrl = () => {
  if (typeof window === "undefined") return "http://localhost:8080";
  if (window.location.hostname === "localhost") {
    return "http://localhost:8080";
  }
  return `${window.location.protocol}//${window.location.host}`;
};

const API_BASE_URL = getApiBaseUrl();

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // ✅ HttpOnly cookie auth
});

/* ================= COMPONENT ================= */

const AdminDocuments = () => {
  const [employees, setEmployees] = useState([]);
  const [expandedEmployee, setExpandedEmployee] = useState(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [message, setMessage] = useState({ type: "", text: "" });
  const [authChecked, setAuthChecked] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);

  /* ================= AUTH CHECK ================= */

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const res = await api.get("/api/auth/me");
        const user = res.data?.data || res.data;

        if (user?.role !== "ADMIN") {
          throw new Error("Admin access required");
        }

        setIsAdmin(true);
      } catch (err) {
        console.error("❌ Auth check failed:", err);
        setIsAdmin(false);
      } finally {
        setAuthChecked(true);
      }
    };

    checkAuth();
  }, []);

  /* ================= FETCH DATA ================= */

const fetchAllDocuments = useCallback(
  async (showRefreshMessage = false) => {
    showRefreshMessage ? setRefreshing(true) : setLoading(true);

    try {
      const res = await api.get("/api/documents/admin/all-documents");
      const data = res.data?.data || res.data || [];

      const normalized = Array.isArray(data)
        ? data.map((emp) => ({
            ...emp,
            id: emp.id?.toString(),
            documents: Array.isArray(emp.documents) ? emp.documents : [],
          }))
        : [];

      setEmployees(normalized);

      if (showRefreshMessage) {
        showMessage("success", "Documents refreshed successfully");
      }
    } catch (err) {
      showMessage(
        "error",
        err.response?.data?.message || "Failed to load documents"
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  },
  [] // 👈 safe because api, showMessage are stable
);

useEffect(() => {
  if (authChecked && isAdmin) {
    fetchAllDocuments();
  }
}, [authChecked, isAdmin, fetchAllDocuments]);

    /* ================= BULK DOWNLOAD DOCUMENTS ================= */
    const handleDownloadAll = async (emp, e) => {
  e?.stopPropagation();

  const docs = emp.documents || [];
  if (docs.length === 0) {
    showMessage("info", "No documents to download");
    return;
  }

    let successCount = 0;
let failCount = 0;

  

  showMessage("info", `Downloading ${docs.length} documents...`);

for (let i = 0; i < docs.length; i++) {
  try {
    if (i > 0) {
      await new Promise((r) => setTimeout(r, 800));
    }

    const res = await api.get(
      `/api/documents/download/${docs[i].id}`,
      { responseType: "blob" }
    );

    const url = window.URL.createObjectURL(res.data);
    const link = document.createElement("a");
    link.href = url;
    link.download = docs[i].fileName || `document-${i + 1}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    successCount++; // ✅ track success
  } catch {
    failCount++; // ✅ track failure
  }
}

if (failCount === 0) {
  showMessage("success", "All documents downloaded successfully");
} else if (successCount > 0) {
  showMessage(
    "info",
    `${successCount} documents downloaded, ${failCount} failed`
  );
} else {
  showMessage("error", "Failed to download documents");
}
};


  /* ================= HELPERS ================= */

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: "", text: "" }), 5000);
  };

  const toggleEmployee = (id) => {
    setExpandedEmployee(expandedEmployee === id ? null : id);
  };

  const handleDownload = async (doc, e) => {
    e?.stopPropagation();
    try {
      const res = await api.get(`/api/documents/download/${doc.id}`, {
        responseType: "blob",
      });

      const url = window.URL.createObjectURL(res.data);
      const link = document.createElement("a");
      link.href = url;
      link.download = doc.fileName || "document";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch {
      showMessage("error", "Download failed");
    }
  };

  const handleViewDocument = (doc, e) => {
    e?.stopPropagation();
    window.open(`${API_BASE_URL}/api/documents/download/${doc.id}`, "_blank");
  };

  const getStatusColor = (status) => {
  switch (status?.toUpperCase()) {
    case "APPROVED":
      return "bg-green-100 text-green-800 border-green-200";
    case "REJECTED":
      return "bg-red-100 text-red-800 border-red-200";
    case "PENDING":
    case "SUBMITTED":
      return "bg-yellow-100 text-yellow-800 border-yellow-200";
    default:
      return "bg-gray-100 text-gray-800 border-gray-200";
  }
};

const getStatusIcon = (status) => {
  switch (status?.toUpperCase()) {
    case "APPROVED":
      return <CheckCircle2 size={14} />;
    case "REJECTED":
      return <XCircle size={14} />;
    case "PENDING":
    case "SUBMITTED":
      return <Clock size={14} />;
    default:
      return <AlertCircle size={14} />;
  }
};


  /* ================= FILTERING ================= */

  const filteredEmployees = useMemo(() => {
    return employees.filter((emp) => {
      const matchesSearch =
        !search ||
        emp.name?.toLowerCase().includes(search.toLowerCase()) ||
        emp.id?.toLowerCase().includes(search.toLowerCase()) ||
        emp.email?.toLowerCase().includes(search.toLowerCase());

      if (statusFilter === "ALL") return matchesSearch;

      return (
        matchesSearch &&
        emp.documents?.some(
          (d) => d.status?.toUpperCase() === statusFilter
        )
      );
    });
  }, [employees, search, statusFilter]);

  /* ================= STATS ================= */

  const stats = useMemo(() => {
    const allDocs = employees.flatMap((e) => e.documents || []);
    return {
      totalDocs: allDocs.length,
      pending: allDocs.filter(
        (d) =>
          d.status === "PENDING" || d.status === "SUBMITTED"
      ).length,
      approved: allDocs.filter((d) => d.status === "APPROVED").length,
      rejected: allDocs.filter((d) => d.status === "REJECTED").length,
    };
  }, [employees]);

  /* ================= GUARDS ================= */

  if (!authChecked) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="animate-spin h-12 w-12 text-blue-600" />
      </div>
    );
  }

  if (!isAdmin) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="bg-white p-10 rounded-2xl shadow text-center">
          <Shield className="mx-auto mb-4 text-red-600" size={40} />
          <h2 className="text-xl font-semibold">Access Denied</h2>
          <p className="text-gray-500 mt-2">
            Admin privileges are required.
          </p>
          <button
            onClick={() => (window.location.href = "/login")}
            className="mt-6 px-4 py-2 bg-blue-600 text-white rounded-lg"
          >
            Go to Login
          </button>
        </div>
      </div>
    );
  }

  /* ================= UI ================= */

  return (
    <div className="min-h-screen bg-[#F7F8FC] px-6 py-6">
      <div
        className="rounded-2xl px-6 py-5 flex items-center justify-between mb-6"
        style={{ backgroundColor: primaryBlue }}
      >
        <h1 className="text-2xl font-bold text-white">
          Documents Dashboard (Admin)
        </h1>
        <button
          onClick={() => fetchAllDocuments(true)}
          disabled={refreshing}
          className="flex items-center gap-2 bg-white px-4 py-2 rounded-lg"
        >
          <RefreshCw size={16} className={refreshing ? "animate-spin" : ""} />
          Refresh
        </button>
      </div>

          {message.text && (
      <div
        className={`mb-6 p-4 rounded-lg flex items-center gap-3 shadow-sm ${
          message.type === "success"
            ? "bg-green-50 text-green-800 border border-green-200"
            : message.type === "info"
            ? "bg-blue-50 text-blue-800 border border-blue-200"
            : "bg-red-50 text-red-800 border border-red-200"
        }`}
      >
        <span className="font-medium">{message.text}</span>
      </div>
    )}

      {/* Stats */}
      <div className="grid md:grid-cols-4 gap-4 mb-6">
        <Stat icon={<FolderOpen />} label="Total Docs" value={stats.totalDocs} />
        <Stat icon={<Clock />} label="Pending" value={stats.pending} />
        <Stat icon={<CheckCircle2 />} label="Approved" value={stats.approved} />
        <Stat icon={<XCircle />} label="Rejected" value={stats.rejected} />
      </div>

      {/* Search / Filter */}
      <div className="bg-white rounded-xl p-4 mb-6 flex gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-2.5 text-gray-400" size={18} />
          <input
            className="w-full pl-10 pr-4 py-2 border rounded-lg"
            placeholder="Search employee"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="border rounded-lg px-3"
        >
          <option value="ALL">All</option>
          <option value="SUBMITTED">Submitted</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {/* Employee List */}
      <div className="space-y-4">
        {loading ? (
          <div className="text-center p-20">
            <Loader2 className="animate-spin mx-auto text-blue-600" size={40} />
          </div>
        ) : (
          filteredEmployees.map((emp) => (
            <div key={emp.id} className="bg-white rounded-xl shadow">
<div
  className="p-4 flex justify-between items-center cursor-pointer"
  onClick={() => toggleEmployee(emp.id)}
>
  <div>
    <p className="font-semibold">{emp.name}</p>
    <p className="text-sm text-gray-500">{emp.email}</p>
  </div>

  <div className="flex items-center gap-3">
    {emp.documents?.length > 0 && (
      <button
        onClick={(e) => handleDownloadAll(emp, e)}
        className="px-3 py-1.5 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
      >
        Download All
      </button>
    )}
    {expandedEmployee === emp.id ? <ChevronDown /> : <ChevronRight />}
  </div>
</div>

{expandedEmployee === emp.id && (
  <div className="border-t">
   {(emp.documents?.length ?? 0) === 0 ? (

      <p className="px-6 py-4 text-sm text-gray-500">
        No documents uploaded
      </p>
    ) : (
      emp.documents.map((doc) => (
        <div
          key={doc.id}
          className="flex justify-between items-center px-6 py-3 border-b"
        >
          <div>
            <p className="font-medium">{doc.documentType}</p>
            <span
              className={`inline-flex items-center gap-1 px-2 py-0.5 mt-1 rounded-full text-xs font-semibold border ${getStatusColor(
                doc.status
              )}`}
            >
              {getStatusIcon(doc.status)}
              {doc.status}
            </span>
          </div>

          <div className="flex gap-2">
            <button onClick={(e) => handleViewDocument(doc, e)}>
              <Eye size={16} />
            </button>
            <button onClick={(e) => handleDownload(doc, e)}>
              <Download size={16} />
            </button>
          </div>
        </div>
      ))
    )}
  </div>
)}
            </div>
          ))
        )}
      </div>
    </div>
    );
};

/* ================= SMALL COMPONENT ================= */

const Stat = ({ icon, label, value }) => (
  <div className="bg-white p-5 rounded-xl shadow flex items-center gap-3">
    <div className="text-blue-600">{icon}</div>
    <div>
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-xl font-bold">{value}</p>
    </div>
  </div>
);


export default AdminDocuments;

