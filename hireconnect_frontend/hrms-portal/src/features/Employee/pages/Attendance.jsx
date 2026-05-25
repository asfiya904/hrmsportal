import React, { useEffect, useRef, useState} from "react";
import axios from "axios";

/* ================= API CONFIG ================= */

const getApiBaseUrl = () => {
  if (typeof window === "undefined") return "http://localhost:8080";
  const { origin, hostname } = window.location;
  if (hostname === "localhost" || hostname === "127.0.0.1") {
    return "http://localhost:8080";
  }
  return origin;
};

const api = axios.create({
  baseURL: getApiBaseUrl(),
  withCredentials: true,
});

/* ================= HELPERS ================= */

const monthNames = [
  "January","February","March","April","May","June",
  "July","August","September","October","November","December",
];

const formatSeconds = (secs = 0) => {
  const h = Math.floor(secs / 3600);
  const m = Math.floor((secs % 3600) / 60);
  const s = secs % 60;
  return `${h.toString().padStart(2,"0")}:${m.toString().padStart(2,"0")}:${s
    .toString()
    .padStart(2,"0")}`;
};

const formatTime = (t) =>
  t ? new Date(t).toLocaleTimeString() : "N/A";


const formatDuration = formatSeconds;

const normalizeStatus = (s) => (s ? s.toLowerCase() : "notstarted");





/* ================= MODAL ================= */

const Modal = ({ title, onClose, children }) => (
  <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
    <div className="bg-white rounded-2xl p-6 w-full max-w-lg">
      <div className="flex justify-between items-center mb-4">
        <h3 className="font-semibold text-lg">{title}</h3>
        <button onClick={onClose}>✕</button>
      </div>
      {children}
    </div>
  </div>
);


/* ================= COMPONENT ================= */

export default function Attendance() {
  const today = new Date();
  const intervalRef = useRef(null);
  const clearTimer = () => {
  if (intervalRef.current) {
    clearInterval(intervalRef.current);
    intervalRef.current = null;
  }
};

  /* ---------- AUTH ---------- */
  const [user, setUser] = useState(null);

  const loadMe = async () => {
    const res = await api.get("/api/auth/me");
    setUser(res.data?.data);
  };

  /* ---------- STATE ---------- */
  const [currentMonth, setCurrentMonth] = useState(today.getMonth());
  const [currentYear, setCurrentYear] = useState(today.getFullYear());
  const [status, setStatus] = useState("notstarted");
  const [workSeconds, setWorkSeconds] = useState(0);
  const [breakSeconds, setBreakSeconds] = useState(0);
  const [shiftStartTime, setShiftStartTime] = useState(null);
  const [shiftEndTime, setShiftEndTime] = useState(null);
  const [shiftDate, setShiftDate] = useState(null);
  const [workStartedToday, setWorkStartedToday] = useState(false);

  const [showTimesheetModal, setShowTimesheetModal] = useState(false);
  const [showLeaveModal, setShowLeaveModal] = useState(false);
  const [showCorrectionModal, setShowCorrectionModal] = useState(false);

  const [timesheetTasks, setTimesheetTasks] = useState("");
  const [timesheetRemarks, setTimesheetRemarks] = useState("");

  const [calendarData, setCalendarData] = useState({});
  const [loadingCalendar, setLoadingCalendar] = useState(false);

  const [loadingAction, setLoadingAction] = useState(false);

  const [timesheetSubmitted, setTimesheetSubmitted] = useState(false);


  /* ---------- HELPERS ---------- */

  const toDateKey = (date) =>
    `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(
      2,
      "0"
    )}-${String(date.getDate()).padStart(2, "0")}`;

const [selectedDate, setSelectedDate] = useState(toDateKey(today));

useEffect(() => {
  setSelectedDate((prev) => {
    const prevDate = new Date(prev + "T00:00:00");
    const monthStart = new Date(currentYear, currentMonth, 1);
    const monthEnd = new Date(currentYear, currentMonth + 1, 0);

    if (prevDate >= monthStart && prevDate <= monthEnd) {
      return prev;
    }

    return toDateKey(monthStart);
  });
}, [currentMonth, currentYear]);




  const shouldDisplayShiftTimes = () =>
    shiftDate === toDateKey(today) || workStartedToday;

  /* ---------- CALENDAR ---------- */

  const fetchCalendarData = async (month, year) => {
    if (!user) return;
    setLoadingCalendar(true);
    try {
      const res = await api.get(
        `/api/attendance/calendar/${year}/${month + 1}`
      );
      setCalendarData(res.data?.data || {});
    } finally {
      setLoadingCalendar(false);
    }
  };

  const changeMonth = (dir) => {
    if (dir === "prev") {
      if (currentMonth === 0) {
        setCurrentMonth(11);
        setCurrentYear((y) => y - 1);
      } else setCurrentMonth((m) => m - 1);
    } else {
      if (currentMonth === 11) {
        setCurrentMonth(0);
        setCurrentYear((y) => y + 1);
      } else setCurrentMonth((m) => m + 1);
    }
  };

const renderCalendar = () => {
  const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
  const firstDay = new Date(currentYear, currentMonth, 1).getDay();

  const cells = [];

  // Empty leading cells
  for (let i = 0; i < firstDay; i++) {
    cells.push(<div key={`empty-${i}`} className="h-8" />);
  }

  // Actual days
  for (let day = 1; day <= daysInMonth; day++) {
    const dateKey = `${currentYear}-${String(currentMonth + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    const status = calendarData[dateKey];

    let bg = "bg-gray-100";
if (status === "COMPLETED" || status === "PRESENT") bg = "bg-green-500";
else if (status === "ABSENT") bg = "bg-red-500";
else if (status === "LEAVE") bg = "bg-yellow-400";
else if (status === "HALF_DAY") bg = "bg-orange-400";


const isFuture =
  new Date(dateKey + "T00:00:00") > new Date(toDateKey(today) + "T00:00:00");

cells.push(
  <div
    key={dateKey}
    onClick={() => {
      if (loadingAction || isFuture) return;
      setSelectedDate(dateKey);
    }}
    className={`h-8 rounded-md flex items-center justify-center text-xs font-semibold text-white
      ${bg}
      ${selectedDate === dateKey ? "ring-2 ring-blue-600" : ""}
      ${isFuture ? "opacity-40 cursor-not-allowed" : "cursor-pointer"}
    `}
    title={isFuture ? "Future date not allowed" : dateKey}
  >
    {day}
  </div>
);


  }

  // Trailing padding
  while (cells.length % 7 !== 0) {
    cells.push(<div key={`pad-${cells.length}`} />);
  }

  return cells;
};


  /* ---------- ATTENDANCE ---------- */

const loadTodayAttendance = async () => {
  if (!user) return;

  const res = await api.get(`/api/attendance/today`);
  const d = res.data?.data || {};

  const normalized = normalizeStatus(d.status);
setStatus(normalized);

 setWorkStartedToday(
    normalized === "working" || normalized === "on_break"
  );

  if (normalized === "completed") {
  setTimesheetSubmitted(true);
}


  setWorkSeconds(d.totalSeconds || 0);
  setBreakSeconds(d.totalBreakSeconds || 0);
  setShiftStartTime(d.shiftStartTime || null);
  setShiftEndTime(d.shiftEndTime || null);
  const todayKey = toDateKey(today);
setShiftDate(todayKey);
setSelectedDate((prev) => {
  if (!prev) return todayKey;
  return prev;
});

clearTimer();

if (normalized === "working") {
  intervalRef.current = setInterval(() => {
    setWorkSeconds((p) => p + 1);
  }, 1000);
}

if (normalized === "on_break") {
  intervalRef.current = setInterval(() => {
    setBreakSeconds((p) => p + 1);
  }, 1000);
}

};


const startWork = async () => {
  if (loadingAction) return;

  setLoadingAction(true);
  clearTimer();

  try {
    await api.post("/api/attendance/start-work");

    setStatus("working");
    setWorkStartedToday(true);

    intervalRef.current = setInterval(() => {
      setWorkSeconds((p) => p + 1);
    }, 1000);
  } finally {
    setLoadingAction(false);
  }
};


const startBreak = async () => {
  if (loadingAction) return;

  setLoadingAction(true);
  clearTimer();

  try {
    await api.post("/api/attendance/break-start");

    setStatus("on_break");
    intervalRef.current = setInterval(() => {
      setBreakSeconds((p) => p + 1);
    }, 1000);
  } finally {
    setLoadingAction(false);
  }
};

const resumeWork = async () => {
  if (loadingAction) return;

  setLoadingAction(true);
  clearTimer();

  try {
    await api.post("/api/attendance/break-resume");

    setStatus("working");
    intervalRef.current = setInterval(() => {
      setWorkSeconds((p) => p + 1);
    }, 1000);
  } finally {
    setLoadingAction(false);
  }
};


const endWork = async () => {
  if (loadingAction) return;

  setLoadingAction(true);
  clearTimer();

  try {
    await api.post("/api/attendance/end-work");

    setStatus("completed");
    setShowTimesheetModal(true);
  } finally {
    setLoadingAction(false);
  }
};

const submitTimesheet = async () => {
  if (loadingAction) return;

  setLoadingAction(true);
  try {
    await api.post("/api/attendance/save-timesheet");

    setTimesheetTasks("");
    setTimesheetRemarks("");
    setShowTimesheetModal(false);

    setStatus("completed");
    setTimesheetSubmitted(true);   // ✅ important

    fetchCalendarData(currentMonth, currentYear);
  } finally {
    setLoadingAction(false);
  }
};


  /* ---------- LEAVE / CORRECTION ---------- */

 const submitCorrection = async (payload) => {
  await api.post("/api/attendance/correction-request", {
    date: payload.date,
    reason: payload.reason,
  });

};

const submitLeave = async (payload) => {
  await api.post("/api/attendance/leave-request", {
    startDate: payload.startDate,
    endDate: payload.endDate,
    type: payload.type,
    reason: payload.reason,
  });

};


  /* ---------- EFFECTS ---------- */

  useEffect(() => {
    loadMe();
  }, []);

  useEffect(() => {
    if (!user) return;
    loadTodayAttendance();
    fetchCalendarData(currentMonth, currentYear);
    return () => clearTimer();

  }, [user, currentMonth, currentYear]);

  /* ================= UI (UNCHANGED) ================= */

return (
  <div className="min-h-screen bg-[#F7F8FC] px-6 py-6">
    <div className="w-full bg-[#011A8B] text-white rounded-3xl px-8 py-6 mb-8 shadow-md">
      <h1 className="text-3xl font-semibold mb-2">Attendance Dashboard</h1>
      <p className="text-sm text-blue-100 max-w-xl">
        Monitor your real-time attendance, manage leaves and request corrections in one place.
      </p>
    </div>

    <div className="flex flex-col lg:flex-row gap-6">
      {/* ================= LEFT ================= */}
      <div className="flex-1 flex flex-col gap-6">
        {/* ===== Calendar ===== */}
        <div className="bg-white rounded-3xl shadow-sm p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-800">
              Attendance Calendar
            </h2>
            <div className="flex items-center gap-3">
              <button onClick={() => changeMonth("prev")}>‹</button>
              <span className="text-sm font-medium">
                {monthNames[currentMonth]} {currentYear}
              </span>
              <button onClick={() => changeMonth("next")}>›</button>
            </div>
          </div>

          {loadingCalendar ? (
            <div className="text-center text-gray-500 py-10">
              Loading calendar...
            </div>
          ) : (
            <div className="grid grid-cols-7 gap-2 text-xs min-h-[240px]">
              {renderCalendar()}
            </div>
          )}
        </div>

        {/* ===== Real-time Overview ===== */}
        <div className="bg-white rounded-3xl shadow-sm p-6">
          <h2 className="text-xl font-semibold text-center mb-6">
            Real-Time Attendance Overview
          </h2>

          <div className="flex justify-center gap-12 mb-6">
            <div className="text-center">
              <div className="text-4xl font-bold text-blue-600">
                {formatDuration(workSeconds)}
              </div>
              <p className="text-sm text-gray-500">Work Time</p>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold text-red-600">
                {formatDuration(breakSeconds)}
              </div>
              <p className="text-sm text-gray-500">Break Time</p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-6 text-sm text-gray-600">
            <p>
              <span className="font-medium">Shift Start:</span>{" "}
              {shouldDisplayShiftTimes() ? formatTime(shiftStartTime) : "N/A"}

            </p>
            <p>
              <span className="font-medium">Shift End:</span>{" "}
              {shouldDisplayShiftTimes() ? formatTime(shiftEndTime) : "N/A"}

            </p>
          </div>
        </div>
      </div>

      {/* ================= RIGHT ================= */}
      <aside className="w-full lg:w-80 flex flex-col gap-6">
        {/* ===== Punch Controls ===== */}
        <div className="bg-white rounded-3xl shadow-sm p-6">
          <h2 className="text-lg font-semibold mb-4">Punch Controls</h2>

          {status === "notstarted" && (
<button
  disabled={loadingAction}
  className={`w-full rounded-full py-2.5 text-white 
    ${loadingAction ? "bg-gray-400 cursor-not-allowed" : "bg-green-500"}`}
  onClick={startWork}
>
  {loadingAction ? "Processing..." : "Start Work"}
</button>
          )}

          {status === "working" && (
            <>
<button
  disabled={loadingAction}
  className={`w-full rounded-full py-2.5 text-white
    ${loadingAction ? "bg-gray-400 cursor-not-allowed" : "bg-yellow-400"}`}
  onClick={startBreak}
>
  Start Break
</button>
              <button
                className="w-full bg-red-500 text-white rounded-full py-2.5"
                onClick={endWork}
              >
                End Work
              </button>
            </>
          )}

{status === "on_break" && (
  <>
    {/* Resume Work */}
    <button
      disabled={loadingAction}
      className={`w-full rounded-full py-2.5 mb-3 text-white
        ${loadingAction ? "bg-gray-400 cursor-not-allowed" : "bg-blue-500"}`}
      onClick={resumeWork}
    >
      Resume Work
    </button>

    {/* End Work */}
    <button
      disabled={loadingAction}
      className={`w-full rounded-full py-2.5 text-white
        ${loadingAction ? "bg-gray-400 cursor-not-allowed" : "bg-red-500"}`}
      onClick={endWork}
    >
      End Work
    </button>
  </>
)}

          {status === "completed" && timesheetSubmitted && (
  <div className="text-center text-sm text-green-600 font-medium">
    ✅ Attendance completed and timesheet submitted for today
  </div>
)}

        </div>

        {/* ===== Quick Actions ===== */}
        <div className="bg-white rounded-3xl shadow-sm p-6">
          <h2 className="text-lg font-semibold mb-4">Quick Actions</h2>
          <button
            className="w-full bg-green-500 text-white rounded-full py-2.5 mb-3"
            onClick={() => setShowLeaveModal(true)}
          >
            Apply Leave
          </button>
<button
  className="w-full bg-orange-400 text-white rounded-full py-2.5"
  onClick={() => {
    if (new Date(selectedDate) > new Date(toDateKey(today))) {
      alert("Cannot request correction for future dates");
      return;
    }
    setShowCorrectionModal(true);
  }}
>
  Request Correction
</button>

        </div>
      </aside>
    </div>

    {/* ===== Modals ===== */}
{showTimesheetModal && (
  <Modal title="Submit Timesheet" onClose={() => setShowTimesheetModal(false)}>
    <div className="flex flex-col gap-4">
      <textarea
        className="w-full min-h-[80px] border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        placeholder="Enter work details / tasks completed"
        value={timesheetTasks}
        onChange={(e) => setTimesheetTasks(e.target.value)}
      />

      <textarea
        className="w-full min-h-[60px] border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        placeholder="Remarks (optional)"
        value={timesheetRemarks}
        onChange={(e) => setTimesheetRemarks(e.target.value)}
      />

      <div className="flex justify-end gap-3">
        <button
          className="px-4 py-2 rounded-md text-sm bg-gray-200 hover:bg-gray-300"
          onClick={() => setShowTimesheetModal(false)}
        >
          Cancel
        </button>

        <button
          disabled={loadingAction}
          className={`px-5 py-2 rounded-md text-sm text-white
            ${loadingAction ? "bg-gray-400 cursor-not-allowed" : "bg-blue-600 hover:bg-blue-700"}`}
          onClick={submitTimesheet}
        >
          {loadingAction ? "Submitting..." : "Submit"}
        </button>
      </div>
    </div>
  </Modal>
)}


    {showLeaveModal && (
      <Modal title="Request Leave" onClose={() => setShowLeaveModal(false)}>
        <LeaveForm
          onCancel={() => setShowLeaveModal(false)}
          onSubmit={async (data) => {
            await submitLeave(data);
            setShowLeaveModal(false);
            fetchCalendarData(currentMonth, currentYear);
          }}
        />
      </Modal>
    )}

    {showCorrectionModal && (
      <Modal
        title="Request Attendance Correction"
        onClose={() => setShowCorrectionModal(false)}
      >
        <CorrectionForm
          dateKey={selectedDate}
          onCancel={() => setShowCorrectionModal(false)}
          onSubmit={async (data) => {
            await submitCorrection(data);
            setShowCorrectionModal(false);
            fetchCalendarData(currentMonth, currentYear);
          }}
        />
      </Modal>
    )}
  </div>
);

}

/* ================= FORMS ================= */

const LeaveForm = ({ onCancel, onSubmit }) => {
  const [type, setType] = useState("ANNUAL_LEAVE");
  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");
  const [reason, setReason] = useState("");

  return (
    <div className="flex flex-col gap-4">
      {/* Leave Type */}
      <div>
        <label className="text-sm font-medium text-gray-700">
          Leave Type
        </label>
        <select
          className="w-full mt-1 border rounded-md px-3 py-2 text-sm"
          value={type}
          onChange={(e) => setType(e.target.value)}
        >
          <option value="ANNUAL_LEAVE">Annual Leave</option>
          <option value="SICK_LEAVE">Sick Leave</option>
          <option value="CASUAL_LEAVE">Casual Leave</option>
          <option value="UNPAID_LEAVE">Unpaid Leave</option>
        </select>
      </div>

      {/* Date Range */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="text-sm font-medium text-gray-700">
            From Date
          </label>
          <input
            type="date"
            className="w-full mt-1 border rounded-md px-3 py-2 text-sm"
            value={start}
            onChange={(e) => setStart(e.target.value)}
          />
        </div>

        <div>
          <label className="text-sm font-medium text-gray-700">
            To Date
          </label>
          <input
            type="date"
            className="w-full mt-1 border rounded-md px-3 py-2 text-sm"
            value={end}
            onChange={(e) => setEnd(e.target.value)}
          />
        </div>
      </div>

      {/* Reason */}
      <div>
        <label className="text-sm font-medium text-gray-700">
          Reason
        </label>
        <textarea
          className="w-full mt-1 min-h-[80px] border rounded-md px-3 py-2 text-sm"
          placeholder="Reason for leave"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />
      </div>

      {/* Actions */}
      <div className="flex justify-end gap-3 pt-2">
        <button
          className="px-4 py-2 rounded-md bg-gray-200 text-sm hover:bg-gray-300"
          onClick={onCancel}
        >
          Cancel
        </button>

        <button
          className="px-5 py-2 rounded-md bg-green-600 text-white text-sm hover:bg-green-700"
          onClick={() =>
            onSubmit({
              type,
              startDate: start,
              endDate: end,
              reason,
            })
          }
        >
          Submit
        </button>
      </div>
    </div>
  );
};


const CorrectionForm = ({ dateKey, onCancel, onSubmit }) => {
  const [date, setDate] = useState(dateKey);
  const [reason, setReason] = useState("");

  useEffect(() => {
    setDate(dateKey);
  }, [dateKey]);

  return (
    <div className="flex flex-col gap-4">
      {/* Date */}
      <div>
        <label className="text-sm font-medium text-gray-700">
          Date
        </label>
        <input
          type="date"
          className="w-full mt-1 border rounded-md px-3 py-2 text-sm"
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />
      </div>

      {/* Reason */}
      <div>
        <label className="text-sm font-medium text-gray-700">
          Correction Reason
        </label>
        <textarea
          className="w-full mt-1 min-h-[80px] border rounded-md px-3 py-2 text-sm"
          placeholder="Explain why correction is needed"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />
      </div>

      {/* Actions */}
      <div className="flex justify-end gap-3 pt-2">
        <button
          className="px-4 py-2 rounded-md bg-gray-200 text-sm hover:bg-gray-300"
          onClick={onCancel}
        >
          Cancel
        </button>

        <button
          className="px-5 py-2 rounded-md bg-orange-500 text-white text-sm hover:bg-orange-600"
          onClick={() => onSubmit({ date, reason })}
        >
          Submit
        </button>
      </div>
    </div>
  );
};

