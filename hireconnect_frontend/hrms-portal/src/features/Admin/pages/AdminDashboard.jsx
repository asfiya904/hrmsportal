import React, { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

import {
  Users,
  CheckCircle,
  Clock,
  PlusCircle,
  Settings,
  Activity,
  Server,
  ArrowRightCircle,
  Gift,
  Sparkles,
} from "lucide-react";

/* -------------------- Config -------------------- */
const API_BASE_URL =
  import.meta.env?.VITE_API_BASE_URL?.trim() ||
  (["localhost", "127.0.0.1"].includes(window.location.hostname)
    ? "http://localhost:8080"
    : "");

/* -------------------- Component -------------------- */
export default function AdminDashboard() {
  const navigate = useNavigate();

const userRole = sessionStorage.getItem("role")?.toUpperCase() || null;


  /* -------------------- State -------------------- */
  const [summary, setSummary] = useState({
    totalEmployees: 0,
    presentToday: 0,
    pendingApprovals: 0,
  });

  const [attendance, setAttendance] = useState({
    present: 0,
    absent: 0,
    onLeave: 0,
    lateCheckIns: 0,
    lastUpdated: null,
  });

  const [events, setEvents] = useState([]);
  const [eventsLoading, setEventsLoading] = useState(false);
  const [eventsError, setEventsError] = useState(null);

  /* -------------------- Helpers -------------------- */

  const api = (path) =>
    `${API_BASE_URL.replace(/\/+$/, "")}/${path.replace(/^\/+/, "")}`;

  /* -------------------- Fetch Dashboard Stats -------------------- */
const fetchDashboardStats = useCallback(async () => {
  try {
    const res = await axios.get(
      api("/api/admin/attendance/dashboard-stats"),
      { withCredentials: true }
    );

    const p = res.data?.data || res.data || {};

    setSummary({
      totalEmployees: p.totalEmployees ?? 0,
      presentToday: p.presentToday ?? 0,
      pendingApprovals: p.pendingApprovals ?? 0,
    });

    setAttendance({
      present: p.present ?? 0,
      absent: p.absent ?? 0,
      onLeave: p.onLeave ?? 0,
      lateCheckIns: p.lateCheckIns ?? 0,
      lastUpdated: p.lastUpdated ?? null,
    });
  } catch (e) {
    console.error("Dashboard stats error", e);
  }
}, []);

  /* -------------------- Fetch Events (Next 7 days) -------------------- */
const fetchEvents = useCallback(async () => {
  setEventsLoading(true);
  setEventsError(null);
  try {
    const res = await axios.get(
      api("/api/dashboard/upcoming-events?days=7"),
      { withCredentials: true }
    );

    setEvents(Array.isArray(res.data?.data) ? res.data.data : []);
  } catch {
    setEventsError("Events not found.");
  } finally {
    setEventsLoading(false);
  }
}, []);

  useEffect(() => {
    fetchDashboardStats();
    fetchEvents();
  }, [fetchDashboardStats, fetchEvents]);

  /* -------------------- Utils -------------------- */
  const formatDate = (d) =>
    new Date(d).toLocaleDateString(undefined, {
      day: "2-digit",
      month: "short",
    });

  const isToday = (date) => {
    const d = new Date(date);
    const t = new Date();
    return d.getDate() === t.getDate() && d.getMonth() === t.getMonth();
  };

  const birthdays = events.filter(
    (e) => e.type?.toUpperCase() === "BIRTHDAY"
  );
  const anniversaries = events.filter(
    (e) => e.type?.toUpperCase() === "ANNIVERSARY"
  );

  /* -------------------- Render -------------------- */
  return (
    <div className="min-h-screen bg-[#F9FAFF] px-4 py-4">
      {/* Header */}
      <div className="bg-blue-900 rounded-2xl px-6 py-5 mb-6 flex justify-between items-center text-white">
        <div>
          <h1 className="text-2xl font-bold">Admin Dashboard</h1>
          <p className="text-blue-200 text-sm">
            Overview of employees, attendance, and HR actions
          </p>
        </div>
        {userRole && (
          <span className="text-xs bg-blue-800 px-3 py-1 rounded-full">
            Logged in as {userRole}
          </span>
        )}
      </div>

      {/* Stats */}
      <div className="grid md:grid-cols-3 gap-4 mb-6">
        <StatCard title="Total Employees" value={summary.totalEmployees} icon={<Users />} />
        <StatCard title="Present Today" value={attendance.present} icon={<CheckCircle />} />
        <StatCard title="Pending Approvals" value={summary.pendingApprovals} icon={<Clock />} />
      </div>

      {/* Actions */}
      <div className="grid md:grid-cols-2 gap-4 mb-6">
        <ActionCard
          title="Add New Employee"
          icon={<PlusCircle />}
          onClick={() => navigate("/admin/add-employee")}
        />
        <ActionCard
          title="Admin Management"
          icon={<Settings />}
          onClick={() => navigate("/admin/management")}
        />
      </div>

      {/* Attendance */}
      <div className="bg-white rounded-2xl border border-blue-200 p-5 mb-6">
        <h3 className="font-semibold flex items-center gap-2 mb-4">
          <Activity className="text-blue-700 h-4 w-4" />
          Today’s Attendance Summary
        </h3>
        <div className="grid md:grid-cols-4 gap-4">
          <AttendanceBox label="Present" value={attendance.present} />
          <AttendanceBox label="Absent" value={attendance.absent} />
          <AttendanceBox label="On Leave" value={attendance.onLeave} />
          <AttendanceBox label="Late Check-ins" value={attendance.lateCheckIns} />
        </div>
      </div>

      {/* Events */}
      <div className="grid lg:grid-cols-2 gap-6">
        <EventCard
          title="Upcoming Birthdays"
          icon={<Gift />}
          loading={eventsLoading}
          error={eventsError}
          items={birthdays}
          renderItem={(e) => (
            <EventRow
              key={e.id}
              highlight={isToday(e.date)}
              name={e.name}
              sub={e.department}
              label={isToday(e.date) ? "🎉 Today" : formatDate(e.date)}
            />
          )}
        />

        <EventCard
          title="Work Anniversaries"
          icon={<Sparkles />}
          loading={eventsLoading}
          error={eventsError}
          items={anniversaries}
          renderItem={(e) => (
            <EventRow
              key={e.id}
              name={e.name}
              sub={`${e.yearsCompleted || 0}+ years`}
              label={formatDate(e.date)}
            />
          )}
        />
      </div>

      {/* Quick Links */}
      <div className="bg-white rounded-2xl border border-blue-200 p-5 mt-6">
        <h3 className="font-semibold flex items-center gap-2 mb-3">
          <Server className="h-4 w-4 text-blue-700" />
          Quick Links
        </h3>
        <QuickLink onClick={() => navigate("/admin/add-employee")} text="Add employee via form" />
        <QuickLink onClick={() => navigate("/admin/management")} text="Manage admin roles" />
      </div>
    </div>
  );
}

/* -------------------- Small Components -------------------- */

const StatCard = ({ title, value, icon }) => (
  <div className="bg-white rounded-2xl border border-blue-200 p-5 flex justify-between items-center">
    <div>
      <p className="text-xs text-blue-600">{title}</p>
      <p className="text-2xl font-bold text-blue-800">{value}</p>
    </div>
    <div className="bg-blue-50 p-3 rounded-xl text-blue-700">{icon}</div>
  </div>
);

const ActionCard = ({ title, icon, onClick }) => (
  <button
    onClick={onClick}
    className="bg-white rounded-2xl border border-blue-200 p-5 flex items-center gap-4 hover:shadow"
  >
    <div className="bg-blue-50 p-3 rounded-xl text-blue-700">{icon}</div>
    <p className="font-semibold">{title}</p>
  </button>
);

const AttendanceBox = ({ label, value }) => (
  <div className="bg-blue-50 border border-blue-200 rounded-2xl px-4 py-3">
    <p className="text-xs text-blue-700">{label}</p>
    <p className="text-xl font-semibold text-blue-800">{value}</p>
  </div>
);

const EventCard = ({ title, icon, loading, error, items, renderItem }) => (
  <div className="bg-white rounded-2xl border border-blue-200 p-5">
    <h3 className="font-semibold flex items-center gap-2 mb-3 text-blue-800">
      <span className="text-blue-700">{icon}</span>
      {title}
    </h3>
    {loading && <p className="text-sm text-blue-600">Loading…</p>}
    {error && <p className="text-sm text-blue-600">{error}</p>}
    {!loading && !items.length && (
      <p className="text-sm text-blue-600">No upcoming events</p>
    )}
    <div className="space-y-2">{items.slice(0, 4).map(renderItem)}</div>
  </div>
);

const EventRow = ({ name, sub, label, highlight }) => (
  <div
    className={`flex justify-between items-center p-2 rounded-lg ${
      highlight
        ? "bg-blue-50 border border-blue-300"
        : "hover:bg-blue-50"
    }`}
  >
    <div>
      <p className="font-medium text-blue-800">{name}</p>
      <p className="text-xs text-blue-600">{sub}</p>
    </div>
    <span className="text-xs px-2 py-0.5 rounded-full bg-blue-50 border border-blue-200 text-blue-700">
      {label}
    </span>
  </div>
);

const QuickLink = ({ text, onClick }) => (
  <button
    onClick={onClick}
    className="w-full flex justify-between items-center py-2 px-2 rounded-lg hover:bg-blue-50 text-blue-700"
  >
    {text}
    <ArrowRightCircle size={16} />
  </button>
);

