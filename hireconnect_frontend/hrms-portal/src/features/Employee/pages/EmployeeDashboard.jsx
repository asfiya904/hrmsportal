import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import axios from "axios";

import {
  User,
  Calendar,
  Clock,
  FileText,
  ArrowRightCircle,
  Activity,
  Briefcase,
  CheckCircle,
  HeartPulse,
} from "lucide-react";

const API_BASE_URL =
  (typeof import.meta !== "undefined" &&
    import.meta.env &&
    import.meta.env.VITE_API_BASE_URL) ||
  "http://localhost:8080";

const cleanBase = API_BASE_URL.replace(/\/+$/, "");

const api = axios.create({
  baseURL: cleanBase,
  withCredentials: true, // ✅ HttpOnly cookie auth
});


export default function EmployeeDashboard() {
  const navigate = useNavigate();

  // ---------------- ROUTE HANDLERS ----------------
  const handleProfileClick = () => navigate("/employee/profile");
  const handleLeaveClick = () => navigate("/employee/attendance");
  const handlePayslipsClick = () => navigate("/employee/finance");
  const handleAttendanceClick = () => navigate("/employee/attendance");

  // ---------------- STATE ----------------
  const [employeeName, setEmployeeName] = useState("");
  const [employeePhotoUrl, setEmployeePhotoUrl] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  // header stats
  const [headerStats, setHeaderStats] = useState({
    todayStatus: null,
    performanceStatus: null,
    thisMonthAttendancePercent: null,
    availableLeave: null,
  });

  // snapshot cards
  const [snapshot, setSnapshot] = useState({
    currentProject: null,
    currentProjectStatus: null,
    projectProgressPercent: null,
    loggedHours: null,
    hoursAboveTarget: null,
    avgHoursPerDay: null,
    pendingItems: [],
  });

  // activity + events + wellness
  const [todaysActivity, setTodaysActivity] = useState([]);
  const [upcomingEvents, setUpcomingEvents] = useState([]);
  const [wellnessHighlights, setWellnessHighlights] = useState([]);

  // ---------------- HELPERS ----------------
  const formatShortDate = (value) => {
    if (!value) return "--";
    try {
      const d = new Date(value);
      if (Number.isNaN(d.getTime())) return value;
      return d.toLocaleDateString(undefined, {
        day: "2-digit",
        month: "short",
      });
    } catch {
      return value;
    }
  };

  const formatTime = (value) => {
    if (!value) return "--";
    try {
      const d = new Date(value);
      if (Number.isNaN(d.getTime())) return value;
      return d.toLocaleTimeString(undefined, {
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return value;
    }
  };

  const getActivityPillClasses = (item) => {
    const severity = (item.severity || item.type || "").toUpperCase();
    switch (severity) {
      case "SUCCESS":
      case "ATTENDANCE":
        return "bg-[#EAF9F1] text-[#19724A] border-[#B8EBD1]";
      case "INFO":
      case "UPDATE":
        return "bg-[#E3EBFF] text-[#2952CC] border-[#C2D1FF]";
      case "REMINDER":
        return "bg-[#FFF5E6] text-[#C96A13] border-[#FFE0B8]";
      case "ALERT":
      case "DANGER":
        return "bg-[#FFECEF] text-[#C2273D] border-[#FFC1CD]";
      default:
        return "bg-[#F3F4F6] text-[#374151] border-[#E5E7EB]";
    }
  };

  const getEventBadgeClasses = (event) => {
    const type = (event.type || event.eventType || "").toUpperCase();
    const status = (event.status || "").toUpperCase();

    if (type === "LEAVE") {
      return status === "APPROVED" ? "text-[#19724A]" : "text-[#C96A13]";
    }
    if (type === "HOLIDAY") return "text-gray-600";
    return "text-[#19724A]";
  };

  // ---------------- FETCH DASHBOARD & USER DATA ----------------
useEffect(() => {
  const fetchDashboardAndUser = async () => {
    setLoading(true);
    try {
      // ---------- AUTH / CURRENT USER ----------
      const meRes = await api.get("/api/auth/me");
      const me = meRes.data?.data || meRes.data;

      if (!me?.id) {
        navigate("/login");
        return;
      }

      setEmployeeName(me.fullName || "Employee");
      if (me.profilePhotoUrl) {
        setEmployeePhotoUrl(me.profilePhotoUrl);
      }

      const userId = me.id;

      // ---------- DASHBOARD ----------
      const res = await api.get(`/api/dashboard/users/${userId}`);
      const dashboardData = res.data?.data || res.data || {};

      setHeaderStats({
        todayStatus:
          dashboardData.todayStatus ||
          dashboardData.attendanceStatus ||
          null,
        performanceStatus:
          dashboardData.performanceStatus ||
          dashboardData.performance ||
          null,
        thisMonthAttendancePercent:
          dashboardData.thisMonthAttendancePercent ??
          dashboardData.attendancePercent ??
          null,
        availableLeave:
          dashboardData.availableLeaveCount ??
          dashboardData.availableLeave ??
          null,
      });

      setSnapshot({
        currentProject:
          dashboardData.currentProject?.name ||
          dashboardData.currentProjectName ||
          null,
        currentProjectStatus:
          dashboardData.currentProject?.status ||
          dashboardData.currentProjectStatus ||
          null,
        projectProgressPercent:
          dashboardData.currentProject?.progressPercent ??
          dashboardData.projectProgressPercent ??
          null,
        loggedHours:
          dashboardData.loggedHoursThisMonth ??
          dashboardData.loggedHours ??
          null,
        hoursAboveTarget: dashboardData.hoursAboveTarget ?? null,
        avgHoursPerDay: dashboardData.avgHoursPerDay ?? null,
        pendingItems: Array.isArray(dashboardData.pendingItems)
          ? dashboardData.pendingItems
          : [],
      });

      setTodaysActivity(
        Array.isArray(dashboardData.todaysActivity)
          ? dashboardData.todaysActivity
          : []
      );
      setUpcomingEvents(
        Array.isArray(dashboardData.upcomingEvents)
          ? dashboardData.upcomingEvents
          : []
      );
      setWellnessHighlights(
        Array.isArray(dashboardData.wellnessHighlights)
          ? dashboardData.wellnessHighlights
          : []
      );

      setLoadError("");
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login");
        return;
      }
      console.error("Dashboard load failed", err);
      setLoadError("Could not load dashboard data.");
    } finally {
      setLoading(false);
    }
  };

  fetchDashboardAndUser();
}, [navigate]);


  const displayName = loading ? "..." : employeeName || "Employee";

  const attendancePercent = headerStats.thisMonthAttendancePercent;
  const availableLeave = headerStats.availableLeave;

  return (
    <div className="min-h-screen px-4 md:px-6 py-4 bg-[#F9FAFF]">
      {/* BLUE HEADER STRIP */}
      <div
        className="rounded-2xl px-6 py-5 flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-6 shadow-sm"
        style={{ backgroundColor: "#00008B" }}
      >
        <div>
          <h1 className="text-xl md:text-2xl font-bold text-white">
            Employee Dashboard
          </h1>
          <p className="text-xs md:text-sm text-blue-100 mt-1 max-w-xl">
            View your attendance, leaves, payslips and HR updates in one place.
          </p>
        </div>

        <div className="flex flex-col items-end text-xs md:text-sm text-blue-100">
          {loadError && (
            <span className="text-[10px] text-red-200 text-right">
              {loadError}
            </span>
          )}
        </div>
      </div>

      <main className="max-w-7xl mx-auto px-0 lg:px-1 py-2 md:py-4 space-y-6 md:space-y-8">
        {/* GREETING + QUICK SUMMARY */}
        <section className="grid grid-cols-1 lg:grid-cols-[2fr,1.2fr] gap-6">
          {/* GREETING CARD WITH PROFILE PHOTO */}
          <div className="relative overflow-hidden rounded-2xl bg-white border border-gray-200 shadow-sm">
            <div className="absolute -top-16 -right-10 h-40 w-40 rounded-full bg-[#E3EBFF]" />
            <div className="relative p-5 md:p-7 flex flex-col justify-between gap-6">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
                <div className="flex items-center gap-4">
                  {/* Round profile photo */}
                  <div className="h-16 w-16 md:h-20 md:w-20 rounded-full border-2 border-[#C2D1FF] bg-[#E3EBFF] overflow-hidden flex items-center justify-center">
                    {employeePhotoUrl ? (
                      <img
                        src={employeePhotoUrl}
                        alt={displayName}
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <User className="h-8 w-8 md:h-10 md:w-10 text-[#2952CC]" />
                    )}
                  </div>

                  <div className="space-y-1">
                    <p className="text-[11px] uppercase tracking-[0.18em] text-[#2952CC]">
                      Welcome back
                    </p>
                    <h2 className="text-2xl md:text-3xl font-semibold text-[#0B2A6F]">
                      Hi, {displayName} 👋
                    </h2>
                    <p className="text-sm md:text-base text-gray-600 max-w-xl">
                      Check your attendance, manage leaves, download payslips
                      and stay updated with HR announcements.
                    </p>
                  </div>
                </div>

                {/* Mini stats on the right inside the white card */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 min-w-[220px] text-xs md:text-sm">
                  <div className="rounded-xl border border-gray-200 bg-[#F9FBFF] px-3 py-3">
                    <p className="text-gray-500 text-[11px]">
                      This month attendance
                    </p>
                    <p className="mt-1 text-xl font-semibold text-[#0B2A6F]">
                      {attendancePercent != null ? `${attendancePercent}%` : "--"}
                    </p>
                    <p className="mt-1 text-[11px] text-[#1B7C4D] flex items-center gap-1">
                      {attendancePercent != null ? (
                        <>
                          <Activity className="h-3 w-3" />
                          On record
                        </>
                      ) : (
                        "No data yet"
                      )}
                    </p>
                  </div>

                  <div className="rounded-xl border border-gray-200 bg-[#F9FBFF] px-3 py-3">
                    <p className="text-gray-500 text-[11px]">Available leave</p>
                    <p className="mt-1 text-xl font-semibold text-[#0B2A6F]">
                      {availableLeave != null ? availableLeave : "--"}
                    </p>
                    <p className="mt-1 text-[11px] text-[#1B5FBF]">
                      {availableLeave != null
                        ? "Leave balance as per HR"
                        : "Leave balance unavailable"}
                    </p>
                  </div>
                </div>
              </div>

              {/* Status chips */}
              <div className="flex flex-wrap gap-2 pt-1">
                <span className="inline-flex items-center gap-2 rounded-full bg-[#E3F5FF] text-[#05518C] border border-[#B8E1FF] px-3 py-1 text-[11px] font-medium">
                  <Clock className="h-3 w-3" />
                  Today&apos;s status:{" "}
                  {headerStats.todayStatus || "No data yet"}
                </span>
                <span className="inline-flex items-center gap-2 rounded-full bg-[#EAF9F1] text-[#19724A] border border-[#B8EBD1] px-3 py-1 text-[11px] font-medium">
                  <CheckCircle className="h-3 w-3" />
                  Performance:{" "}
                  {headerStats.performanceStatus || "Not available"}
                </span>
              </div>
            </div>
          </div>

          {/* QUICK ACTIONS PANEL */}
          <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4 md:p-5 flex flex-col justify-between">
            <div className="flex items-center justify-between mb-3">
              <div>
                <h3 className="text-sm font-semibold text-[#0B2A6F]">
                  Quick Actions
                </h3>
                <p className="text-xs text-gray-500">
                  One-click access to your most used features.
                </p>
              </div>
              <span className="rounded-full bg-[#E3EBFF] text-[#0B2A6F] text-[11px] px-3 py-1 font-medium">
                Employee Panel
              </span>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs md:text-sm">
              <button
                onClick={handleLeaveClick}
                className="group rounded-xl border border-gray-200 bg-[#F9FBFF] px-3 py-3 flex flex-col items-start gap-2 hover:border-[#2952CC] hover:bg-[#EAF0FF] transition"
              >
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-[#2952CC]" />
                  <span className="font-medium text-[#0B2A6F]">
                    Request Leave
                  </span>
                </div>
                <p className="text-[11px] text-gray-500 group-hover:text-gray-700">
                  Apply for leave and track status.
                </p>
              </button>

              <button
                onClick={handlePayslipsClick}
                className="group rounded-xl border border-gray-200 bg-[#F9FBFF] px-3 py-3 flex flex-col items-start gap-2 hover:border-[#2952CC] hover:bg-[#EAF0FF] transition"
              >
                <div className="flex items-center gap-2">
                  <FileText className="h-4 w-4 text-[#2952CC]" />
                  <span className="font-medium text-[#0B2A6F]">
                    View Payslips
                  </span>
                </div>
                <p className="text-[11px] text-gray-500 group-hover:text-gray-700">
                  Download monthly salary slips.
                </p>
              </button>

              <button
                onClick={handleAttendanceClick}
                className="group rounded-xl border border-gray-200 bg-[#F9FBFF] px-3 py-3 flex flex-col items-start gap-2 hover:border-[#2952CC] hover:bg-[#EAF0FF] transition"
              >
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-[#2952CC]" />
                  <span className="font-medium text-[#0B2A6F]">
                    Attendance
                  </span>
                </div>
                <p className="text-[11px] text-gray-500 group-hover:text-gray-700">
                  Check in/out history & hours.
                </p>
              </button>

              <button
                onClick={handleProfileClick}
                className="group rounded-xl border border-gray-200 bg-[#F9FBFF] px-3 py-3 flex flex-col items-start gap-2 hover:border-[#2952CC] hover:bg-[#EAF0FF] transition"
              >
                <div className="flex items-center gap-2">
                  <User className="h-4 w-4 text-[#2952CC]" />
                  <span className="font-medium text-[#0B2A6F]">
                    My Profile
                  </span>
                </div>
                <p className="text-[11px] text-gray-500 group-hover:text-gray-700">
                  Update personal & work details.
                </p>
              </button>
            </div>
          </div>
        </section>

        {/* MIDDLE SECTION: SNAPSHOT + ACTIVITY */}
        <section className="grid grid-cols-1 xl:grid-cols-[1.7fr,1.3fr] gap-6">
          {/* SNAPSHOT & ACTIVITY */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-[#0B2A6F]">
                Work &amp; HR Snapshot
              </h3>
              <button className="flex items-center gap-1 text-[11px] text-[#2952CC] hover:text-[#1F3DA3]">
                View detailed reports
                <ArrowRightCircle className="h-3 w-3" />
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Current project */}
              <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] text-gray-500">
                    Current project
                  </span>
                  <Briefcase className="h-4 w-4 text-[#2952CC]" />
                </div>
                <p className="text-sm font-semibold text-[#0B2A6F] min-h-[20px]">
                  {snapshot.currentProject || "No project assigned"}
                </p>
                <p className="text-[11px] text-gray-500">
                  Status:{" "}
                  <span className="text-[#1B7C4D] font-medium">
                    {snapshot.currentProjectStatus || "—"}
                  </span>
                </p>
                <div className="mt-2 h-1.5 w-full rounded-full bg-[#E5EDFF] overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-[#2952CC] to-[#2DAF7D]"
                    style={{
                      width: `${
                        snapshot.projectProgressPercent != null
                          ? snapshot.projectProgressPercent
                          : 0
                      }%`,
                    }}
                  />
                </div>
                <p className="text-[11px] text-gray-500">
                  {snapshot.projectProgressPercent != null
                    ? `${snapshot.projectProgressPercent}% completed`
                    : "Progress not available"}
                </p>
              </div>

              {/* Logged hours */}
              <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] text-gray-500">This month</span>
                  <Clock className="h-4 w-4 text-[#2952CC]" />
                </div>
                <p className="text-sm font-semibold text-[#0B2A6F]">
                  Logged Hours
                </p>
                <p className="text-2xl font-semibold text-[#0B2A6F]">
                  {snapshot.loggedHours != null ? snapshot.loggedHours : "--"}
                </p>
                <p className="text-[11px] text-[#1B7C4D]">
                  {snapshot.hoursAboveTarget != null
                    ? `${snapshot.hoursAboveTarget} hrs above target`
                    : "Target data unavailable"}
                </p>
                <p className="text-[11px] text-gray-500">
                  Avg per day:{" "}
                  {snapshot.avgHoursPerDay != null
                    ? `${snapshot.avgHoursPerDay} hrs`
                    : "—"}
                </p>
              </div>

              {/* Pending HR items */}
              <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] text-gray-500">HR Updates</span>
                  <CheckCircle className="h-4 w-4 text-[#2952CC]" />
                </div>
                <p className="text-sm font-semibold text-[#0B2A6F]">
                  Pending Items
                </p>
                {snapshot.pendingItems && snapshot.pendingItems.length > 0 ? (
                  <ul className="mt-1 space-y-1 text-[11px] text-gray-700">
                    {snapshot.pendingItems.slice(0, 3).map((item) => (
                      <li key={item.id || item.label}>
                        • {item.label || item.title || "Pending task"}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="mt-1 text-[11px] text-gray-500">
                    You&apos;re all caught up. No pending HR items.
                  </p>
                )}
                <button className="mt-2 text-[11px] text-[#2952CC] hover:text-[#1F3DA3]">
                  View details
                </button>
              </div>
            </div>

            {/* Today activity timeline */}
            <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4 md:p-5">
              <div className="flex items-center justify-between mb-3">
                <div>
                  <h3 className="text-sm font-semibold text-[#0B2A6F]">
                    Today&apos;s Activity
                  </h3>
                  <p className="text-xs text-gray-500">
                    Key updates from attendance, leaves and HR.
                  </p>
                </div>
              </div>

              {todaysActivity.length === 0 ? (
                <p className="text-xs text-gray-500">
                  No activity logged for today yet.
                </p>
              ) : (
                <div className="space-y-4 text-xs md:text-sm">
                  {todaysActivity.map((item) => (
                    <div
                      key={item.id || `${item.title}-${item.time}`}
                      className="flex gap-3 md:gap-4 items-start relative"
                    >
                      <div
                        className={`mt-0.5 inline-flex items-center justify-center rounded-full border px-2 py-1 text-[10px] md:text-[11px] ${getActivityPillClasses(
                          item
                        )}`}
                      >
                        <Clock className="h-3 w-3 mr-1" />
                        <span className="hidden sm:inline">
                          {formatTime(item.time || item.timestamp)}
                        </span>
                      </div>
                      <div className="flex-1">
                        <p className="font-medium text-xs md:text-sm text-[#0B2A6F]">
                          {item.title || "Activity"}
                        </p>
                        <p className="text-[11px] md:text-xs text-gray-500">
                          {item.description || item.details || ""}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* RIGHT SIDE: UPCOMING & WELLNESS */}
          <div className="space-y-4">
            {/* Upcoming events */}
            <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4 md:p-5">
              <div className="flex items-center justify-between mb-3">
                <div>
                  <h3 className="text-sm font-semibold text-[#0B2A6F]">
                    Upcoming
                  </h3>
                  <p className="text-xs text-gray-500">
                    Leaves, holidays & important dates.
                  </p>
                </div>
                <span className="text-[11px] text-gray-500">Next 30 days</span>
              </div>

              {upcomingEvents.length === 0 ? (
                <p className="text-xs text-gray-500">
                  No upcoming events in the next few days.
                </p>
              ) : (
                <div className="space-y-3 text-xs md:text-sm">
                  {upcomingEvents.slice(0, 3).map((event) => (
                    <div
                      key={event.id || `${event.title}-${event.date}`}
                      className="flex items-start gap-3 rounded-xl border border-gray-200 bg-[#F9FBFF] p-3"
                    >
                      <div className="rounded-lg bg-[#E3EBFF] text-[#0B2A6F] h-10 w-10 flex flex-col items-center justify-center text-[11px] font-semibold">
                        <span>
                          {formatShortDate(event.date || event.startDate).slice(
                            0,
                            2
                          )}
                        </span>
                        <span className="text-[9px] uppercase">
                          {formatShortDate(event.date || event.startDate).slice(
                            3
                          )}
                        </span>
                      </div>
                      <div className="flex-1">
                        <p className="font-medium text-[#0B2A6F]">
                          {event.title || event.name || "Event"}
                        </p>
                        <p className="text-[11px] text-gray-500">
                          {event.description || event.details || ""}
                        </p>
                      </div>
                      <span
                        className={`text-[11px] font-medium ${getEventBadgeClasses(
                          event
                        )}`}
                      >
                        {event.status || event.type || ""}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Wellness / People highlights */}
            <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4 md:p-5">
              <div className="flex items-center justify-between mb-3">
                <div>
                  <h3 className="text-sm font-semibold text-[#0B2A6F]">
                    People &amp; Wellness
                  </h3>
                  <p className="text-xs text-gray-500">
                    HR&apos;s latest initiatives and recognitions.
                  </p>
                </div>
              </div>

              {wellnessHighlights.length === 0 ? (
                <p className="text-xs text-gray-500">
                  No wellness updates available right now.
                </p>
              ) : (
                <div className="space-y-3 text-xs md:text-sm">
                  {wellnessHighlights.slice(0, 3).map((item) => (
                    <div
                      key={item.id || item.title}
                      className="flex items-start gap-3 rounded-xl border border-gray-200 bg-[#F9FBFF] p-3"
                    >
                      <div className="h-9 w-9 rounded-xl bg-[#E3EBFF] flex items-center justify-center">
                        {item.icon === "HEART" ? (
                          <HeartPulse className="h-4 w-4 text-[#2952CC]" />
                        ) : (
                          <CheckCircle className="h-4 w-4 text-[#2952CC]" />
                        )}
                      </div>
                      <div>
                        <p className="font-medium text-[#0B2A6F]">
                          {item.title || "Update"}
                        </p>
                        <p className="text-[11px] text-gray-500">
                          {item.description || ""}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              <div className="flex items-center justify-between pt-2">
                <p className="text-[11px] text-gray-500">
                  Want to update your personal details or bank info?
                </p>
                <button
                  onClick={handleProfileClick}
                  className="text-[11px] text-[#2952CC] hover:text-[#1F3DA3] flex items-center gap-1"
                >
                  Go to profile
                  <ArrowRightCircle className="h-3 w-3" />
                </button>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
