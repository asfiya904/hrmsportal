import React, { useEffect } from "react";
import {
  Plus,
  RefreshCw,
  ClipboardList,
  DollarSign,
  BarChart3,
  IndianRupee,
  FileText,
  Receipt,
  Banknote,
  Shield,
  ScrollText,
  Settings,
  TrendingUp,
  ArrowLeft,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const primaryBlue = "#00008B";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  withCredentials: true,
});

const FinanceHub = ({ onBack }) => {
  const navigate = useNavigate();

  useEffect(() => {
  let mounted = true;

  const checkAuth = async () => {
    try {
      const res = await api.get("/api/auth/me");
      const user = res.data?.data;

      if (!mounted) return;

      if (!user) {
        navigate("/login");
        return;
      }

      if (user.role !== "ADMIN") {
        navigate("/employee/dashboard");
      }
    } catch (err) {
      console.error("Auth check failed", err);
      if (mounted) navigate("/login");
    }
  };

  checkAuth();

  return () => {
    mounted = false;
  };
}, [navigate]);



  /* ================= UI DATA ================= */

  const quickActions = [
    {
      id: "manual-payroll",
      title: "Manual Payroll",
      description: "Select employee → Input details → Save",
      icon: <Plus size={18} />,
    },
    {
      id: "auto-generate",
      title: "Auto Generate",
      description: "Auto-calculates payroll using attendance",
      icon: <RefreshCw size={18} />,
    },
    {
      id: "tax-review",
      title: "Tax Review",
      description: "View pending declarations",
      icon: <ClipboardList size={18} />,
    },
    {
      id: "reimbursements-quick",
      title: "Reimbursements",
      description: "View pending claims",
      icon: <DollarSign size={18} />,
    },
  ];

  const featureCards = [
    {
      id: "overview",
      title: "Overview",
      description: "Stats, graphs, payroll trends",
      icon: <BarChart3 size={28} />,
    },
    {
      id: "payroll-management",
      title: "Payroll Management",
      description: "Manual + Auto payroll",
      icon: <IndianRupee size={28} />,
    },
    {
      id: "payslip-management",
      title: "Payslip Management",
      description: "Generate & download payslips",
      icon: <FileText size={28} />,
    },
    {
      id: "tax-management",
      title: "Tax Management",
      description: "Approve declarations",
      icon: <Receipt size={28} />,
    },
    {
      id: "reimbursements",
      title: "Reimbursements",
      description: "Approve / reject claims",
      icon: <Banknote size={28} />,
    },
    {
      id: "compliance",
      title: "Compliance",
      description: "PF, TDS, PT reports",
      icon: <Shield size={28} />,
    },
    {
      id: "audit-logs",
      title: "Audit & Logs",
      description: "Track payroll actions",
      icon: <ScrollText size={28} />,
    },
    {
      id: "settings",
      title: "Settings",
      description: "Pay cycle & templates",
      icon: <Settings size={28} />,
    },
    {
      id: "manual-entry",
      title: "Manual Entry",
      description: "Individual salary entry",
      icon: <TrendingUp size={28} />,
    },
  ];

  /* ================= NAVIGATION ================= */

  const comingSoon = (feature) => {
  alert(`${feature} module is under development`);
};

  const handleFeatureClick = (id) => {
    switch (id) {
      case "overview":
        navigate("/admin/financeoverview");
        break;
      case "payroll-management":
        navigate("/admin/payrollmanagement");
        break;
      case "manual-entry":
        navigate("/admin/manualentry");
        break;
      default:
        comingSoon("This feature");
    }
  };

  /* ================= UI ================= */

  return (
    <div className="min-h-screen px-4 md:px-6 py-4">
      {/* Header */}
      <div
        className="rounded-2xl px-6 py-5 flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-6"
        style={{ backgroundColor: primaryBlue }}
      >
        <div>
          <h1 className="text-xl md:text-2xl font-bold text-white">
            Finance Hub
          </h1>
          <p className="text-xs md:text-sm text-blue-100 mt-1">
            Central place to manage payroll, taxes, and compliance.
          </p>
        </div>

        {onBack && (
          <button
            onClick={onBack}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white hover:bg-gray-50 shadow-sm"
            style={{ color: primaryBlue }}
          >
            <ArrowLeft size={18} />
            Back
          </button>
        )}
      </div>

      {/* Quick Actions */}
      <section className="mb-8">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {quickActions.map((action) => (
            <button
              key={action.id}
              className="flex items-center gap-3 rounded-2xl bg-white border px-4 py-3 shadow-sm hover:shadow-md transition"
            >
              <div className="h-10 w-10 flex items-center justify-center rounded-xl bg-blue-100 text-blue-900">
                {action.icon}
              </div>
              <div className="text-left">
                <h3 className="text-sm font-semibold">{action.title}</h3>
                <p className="text-xs text-gray-500">{action.description}</p>
              </div>
            </button>
          ))}
        </div>
      </section>

      {/* Feature Grid */}
      <section>
        <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
          {featureCards.map((card) => (
            <button
              key={card.id}
              onClick={() => handleFeatureClick(card.id)}
              className="flex flex-col rounded-2xl bg-white border p-5 shadow-sm hover:shadow-lg transition"
            >
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="font-semibold mb-1">{card.title}</h3>
                  <p className="text-xs text-gray-500">{card.description}</p>
                </div>
                <div className="h-11 w-11 flex items-center justify-center rounded-xl bg-blue-100 text-blue-900">
                  {card.icon}
                </div>
              </div>
            </button>
          ))}
        </div>
      </section>
    </div>
  );
};

export default FinanceHub;
