'use client';

import React, { useEffect, useMemo, useState, useCallback } from 'react';
import {
  Bell,
  CheckCircle,
  AlertTriangle,
  Calendar,
  Clock,
  Info,
  CheckCheck,
  Trash2,
  X,
  Pin,
  Download,
  Eye,
  FileText
} from 'lucide-react';

import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // ✅ HttpOnly cookie auth
});


export default function EmployeeNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [activeFilter, setActiveFilter] = useState('all');

  // ---------- LOAD DATA ----------
  // Auto-clear errors
  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(""), 5000);
      return () => clearTimeout(timer);
    }
  }, [error]);

const fetchNotifications = useCallback(async () => {
  setLoading(true);
  try {
    const res = await api.get("/api/employee/notifications");
    const data = res.data || [];

const sorted = [...data].sort((a, b) => {
  if (a.isPinned !== b.isPinned) return a.isPinned ? -1 : 1;
  if (a.read !== b.read) return a.read ? 1 : -1;
  if (a.reqAck !== b.reqAck && !a.isAcknowledged)
    return a.reqAck ? -1 : 1;
  return new Date(b.createdAt) - new Date(a.createdAt);
});

    setNotifications(sorted);
  } catch (err) {
    console.error("Fetch failed:", err);
    setError("Could not load latest notifications.");
  } finally {
    setLoading(false);
  }
}, []);

useEffect(() => {
  fetchNotifications();
}, [fetchNotifications]);




  // ---------- ACTIONS ----------
  
  const markAsRead = async (id) => {
    // Optimistic Update
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
    try {
  await api.put(`/api/employee/notifications/${id}/read`);


    } catch (err) {
      console.error("Mark read failed", err);
    }
  };

  const markAllRead = async () => {
    if (!notifications.some(n => !n.read)) return; 

    const prevNotifications = [...notifications];
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));

    try {
await api.put("/api/employee/notifications/read-all");
    } catch (err) {
      console.error("Mark all read failed", err);
      setError("Failed to mark all as read on server.");
      setNotifications(prevNotifications);
    }
  };

  const handleAcknowledge = async (id, e) => {
    e.stopPropagation();
    
    // Optimistic Update
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, isAcknowledged: true, read: true } : n));

    try {
await api.put(`/api/employee/notifications/${id}/acknowledge`);


    } catch (err) {
      console.error(err);
      setError("Failed to submit acknowledgement.");
    }
  };

const handleDownload = async (n, e) => {
  e.stopPropagation();

  if (!n.attachmentUrl) {
    alert("No attachment available");
    return;
  }

  try {
    const response = await api.get(n.attachmentUrl, {
      responseType: "blob", // 🔴 VERY IMPORTANT
    });

    const blob = new Blob([response.data]);
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = n.attachmentName || "attachment";
    document.body.appendChild(link);
    link.click();

    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (err) {
    console.error("Download failed", err);
    alert("You are not authorized to download this file");
  }
};


  const deleteNotification = async (id, e) => {
    e.stopPropagation(); 
    if (!confirm("Remove this notification?")) return;

    const prevNotifications = [...notifications];
    setNotifications(prev => prev.filter(n => n.id !== id));

    try {
await api.delete(`/api/employee/notifications/${id}`);

    } catch (err) {
      console.error("Delete failed", err);
      setError("Could not remove notification.");
      setNotifications(prevNotifications);
    }
  };

  // ---------- COMPUTED DATA ----------
  const unreadCount = useMemo(() => notifications.filter(n => !n.read).length, [notifications]);
  
  const actionRequiredCount = useMemo(() => 
    notifications.filter(n => n.reqAck && !n.isAcknowledged).length, 
  [notifications]);

  const filteredNotifications = useMemo(() => {
    return notifications.filter(n => {
      if (activeFilter === 'all') return true;
      if (activeFilter === 'unread') return !n.read;
      if (activeFilter === 'action') return n.reqAck && !n.isAcknowledged;
      return true;
    });
  }, [notifications, activeFilter]);

  // ---------- UI HELPERS ----------
  const getIcon = (priority) => {
    if (priority === 'HIGH') return <AlertTriangle className="w-5 h-5 text-red-600" />;
    if (priority === 'LOW') return <Info className="w-5 h-5 text-slate-500" />;
    return <Bell className="w-5 h-5 text-blue-600" />;
  };

  const getPriorityBorder = (priority, read) => {
    if (read) return 'border-slate-200';
    if (priority === 'HIGH') return 'border-l-4 border-l-red-500 border-y-slate-100 border-r-slate-100';
    return 'border-l-4 border-l-blue-600 border-y-slate-100 border-r-slate-100';
  };

  return (
    <div className="w-full min-h-screen bg-[#F8F9FC] font-sans text-slate-800">
      
      <div className="max-w-7xl mx-auto p-6 lg:p-10 space-y-6">

        {/* HEADER */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-100 p-8">
          <div className="flex flex-col gap-1">
            <h1 className="text-2xl font-bold text-slate-900">My Notifications</h1>
            <p className="text-slate-500 text-sm">
              Manage alerts, policy updates, and compliance acknowledgements.
            </p>
          </div>
        </div>

        {/* STATS ROW */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Card 1: Unread */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-100 p-6 flex items-center gap-4">
            <div className="p-3 rounded-full bg-blue-50 text-blue-600">
              <Bell className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Unread</p>
              <h3 className="text-2xl font-bold text-slate-800">{unreadCount}</h3>
            </div>
          </div>

          {/* Card 2: Action Required */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-100 p-6 flex items-center gap-4">
            <div className="p-3 rounded-full bg-purple-50 text-purple-600">
              <Eye className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Action Required</p>
              <h3 className="text-2xl font-bold text-slate-800">{actionRequiredCount}</h3>
            </div>
          </div>

          {/* Card 3: Filters */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-100 p-6 flex flex-col justify-center gap-2">
            <div className="flex justify-between items-center mb-1">
              <span className="text-xs font-bold text-slate-400 uppercase">Quick Filter</span>
              <button onClick={markAllRead} className="text-xs text-blue-600 font-semibold hover:underline">Mark all read</button>
            </div>
            <div className="flex gap-2">
              {['all', 'unread', 'action'].map(filter => (
                 <button
                   key={filter}
                   onClick={() => setActiveFilter(filter)}
                   className={`flex-1 py-2 text-xs font-bold rounded-lg border transition-all capitalize ${
                     activeFilter === filter 
                     ? 'bg-blue-900 text-white border-blue-900' 
                     : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
                   }`}
                 >
                   {filter}
                 </button>
              ))}
            </div>
          </div>
        </div>

        {/* ERROR MESSAGE */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl flex items-center justify-between text-sm">
            <span className="flex items-center gap-2">
               <AlertTriangle className="w-4 h-4" /> {error}
            </span>
            <button onClick={() => setError("")}><X className="w-4 h-4" /></button>
          </div>
        )}

        {/* NOTIFICATIONS LIST */}
        <div className="space-y-4">
          <h2 className="text-sm font-bold text-slate-700 ml-1">
            Recent Alerts
            {loading && <span className="ml-2 font-normal text-slate-400">Loading...</span>}
          </h2>

          {!loading && filteredNotifications.length === 0 && (
            <div className="bg-white rounded-2xl border border-slate-200 border-dashed p-12 text-center">
               <div className="mx-auto w-12 h-12 bg-slate-50 rounded-full flex items-center justify-center mb-3">
                 <CheckCircle className="w-6 h-6 text-slate-300" />
               </div>
               <p className="text-slate-500 font-medium">No notifications found.</p>
            </div>
          )}

          {filteredNotifications.map((n) => (
            <div
              key={n.id}
              onClick={() => !n.read && markAsRead(n.id)}
              className={`group flex flex-col md:flex-row gap-5 p-5 bg-white rounded-2xl border shadow-sm transition-all duration-200 ${getPriorityBorder(n.priority, n.read)} hover:shadow-md cursor-pointer`}
            >
              {/* Left: Icon & Main Content */}
              <div className="flex flex-1 gap-5 overflow-hidden">
                <div className={`flex-shrink-0 w-12 h-12 rounded-xl flex items-center justify-center ${!n.read ? 'bg-slate-100' : 'bg-slate-50'}`}>
                  {getIcon(n.priority)}
                </div>

                <div className="flex-1 min-w-0 space-y-2">
                   {/* Title Row */}
                   <div className="flex items-center gap-2 flex-wrap">
                      <h3 className={`text-base ${!n.read ? 'font-bold text-slate-900' : 'font-medium text-slate-600'}`}>
                        {n.title}
                      </h3>
                      {n.isPinned && (
                        <span className="text-[10px] bg-blue-100 text-blue-700 px-2 py-0.5 rounded font-bold flex items-center gap-1">
                          <Pin className="w-3 h-3" /> Pinned
                        </span>
                      )}
                      {!n.read && (
                        <span className="text-[10px] bg-blue-600 text-white px-2 py-0.5 rounded font-bold">New</span>
                      )}
                      {n.reqAck && !n.isAcknowledged && (
                         <span className="text-[10px] bg-purple-100 text-purple-700 px-2 py-0.5 rounded font-bold border border-purple-200">
                           Action Required
                         </span>
                      )}
                   </div>
                   
                   {/* Message Body - preserves whitespace/lines */}
                   <p className="text-sm text-slate-500 whitespace-pre-wrap leading-relaxed max-w-3xl">
                     {n.message}
                   </p>

                   {/* Attachment Block */}
                   {n.attachmentName && (
                     <div 
                        onClick={(e) => handleDownload(n, e)}
                        className="flex items-center gap-3 p-3 bg-slate-50 border border-slate-200 rounded-lg w-fit mt-2 hover:bg-slate-100 transition-colors group/file"
                     >
                       <div className="p-2 bg-white rounded border border-slate-200">
                         <FileText className="w-4 h-4 text-red-500" />
                       </div>
                       <div>
                         <p className="text-xs font-bold text-slate-700">{n.attachmentName}</p>
                         <p className="text-[10px] text-slate-400">Click to download</p>
                       </div>
                       <Download className="w-4 h-4 text-slate-400 group-hover/file:text-blue-600 ml-2" />
                     </div>
                   )}
                </div>
              </div>

              {/* Right: Actions & Meta */}
              <div className="flex flex-row md:flex-col items-center md:items-end justify-between gap-4 border-t md:border-t-0 md:border-l border-slate-100 pt-4 md:pt-0 md:pl-6 md:min-w-[180px]">
                 
                 {/* Timestamp */}
                 <div className="text-right">
                    <p className="text-xs text-slate-400 font-medium">
                      {new Date(n.createdAt).toLocaleDateString()}
                    </p>
                    <p className="text-[10px] text-slate-300">
                      {new Date(n.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                    </p>
                    {/* Expiry Warning */}
                    {n.expiresAt && new Date(n.expiresAt) < new Date(Date.now() + 86400000) && (
                      <span className="flex items-center justify-end gap-1 text-[10px] text-amber-600 mt-1 font-medium">
                         <Clock className="w-3 h-3" /> Expires Soon
                      </span>
                    )}
                 </div>

                 <div className="flex items-center gap-2">
                   <button
                      onClick={(e) => deleteNotification(n.id, e)}
                      className="p-2 text-slate-300 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                      title="Delete"
                   >
                     <Trash2 className="w-4 h-4" />
                   </button>

                   {/* Conditional Action Button */}
                   {n.reqAck && !n.isAcknowledged ? (
                      <button
                        onClick={(e) => handleAcknowledge(n.id, e)}
                        className="flex items-center gap-2 bg-purple-600 hover:bg-purple-700 text-white px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-sm active:scale-95"
                      >
                        <Eye className="w-3 h-3" /> Acknowledge
                      </button>
                   ) : !n.read ? (
                     <button 
                       onClick={(e) => { e.stopPropagation(); markAsRead(n.id); }}
                       className="flex items-center gap-2 text-blue-600 hover:bg-blue-50 px-3 py-1.5 rounded-lg text-xs font-bold transition-colors"
                     >
                       <CheckCheck className="w-3 h-3" /> Mark Read
                     </button>
                   ) : (
                     <div className="flex items-center gap-1 text-slate-300 text-xs font-medium px-2">
                       {n.isAcknowledged ? (
                         <><CheckCircle className="w-3 h-3 text-purple-400" /> Ack'd</>
                       ) : (
                         <><CheckCircle className="w-3 h-3" /> Read</>
                       )}
                     </div>
                   )}
                 </div>
              </div>
            </div>
          ))}
        </div>

      </div>
    </div>
  );
}