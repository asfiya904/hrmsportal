'use client';

import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';

import {
  Search,
  Plus,
  Filter,
  Clock,
  MessageSquare,
  Phone,
  Mail,
  BookOpen,
  HelpCircle,
  FileText,
  Send,
} from 'lucide-react';

// ---------- UTIL: API BASE (LOCALHOST + PROD, NO process.env) ----------
const getApiBaseUrl = () => {
  // 1) Vite env
  try {
    if (
      typeof import.meta !== 'undefined' &&
      import.meta.env &&
      import.meta.env.VITE_API_BASE_URL
    ) {
      return import.meta.env.VITE_API_BASE_URL;
    }
  } catch {
    // ignore
  }

  // 2) Optional global override
  if (typeof window !== 'undefined' && window.__API_BASE_URL__) {
    return window.__API_BASE_URL__;
  }

  // 3) Fallbacks
  if (typeof window !== 'undefined') {
    if (window.location.hostname === 'localhost') {
      return 'http://localhost:8080';
    }
    return `${window.location.protocol}//${window.location.host}`;
  }

  return '';
};

// ---------- HELPERS ----------
const statusDot = (status) => {
  if (status === 'open') return 'bg-blue-500';
  if (status === 'in-progress') return 'bg-yellow-500';
  if (status === 'resolved') return 'bg-green-500';
  return 'bg-slate-400';
};

const priorityClass = (priority) => {
  const p = (priority || '').toLowerCase();
  if (p === 'urgent') return 'text-red-500';
  if (p === 'high') return 'text-orange-500';
  if (p === 'medium') return 'text-yellow-500';
  if (p === 'low') return 'text-green-500';
  return 'text-slate-400';
};

const normalizeTickets = (data) => {
  return (
    data?.data || // { data: [] }
    data?.tickets || // { tickets: [] }
    data?.records || // { records: [] }
    data?.list || // { list: [] }
    data || [] // plain array
  );
};

export default function SupportCenter() {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState('');

  const [search, setSearch] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [filterPriority, setFilterPriority] = useState('all');
  const [filterCategory, setFilterCategory] = useState('all');
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [reply, setReply] = useState('');

  // ---------- LOAD TICKETS FROM API ----------
  useEffect(() => {
    let cancelled = false;
const fetchTickets = async () => {
  try {
    setLoading(true);
    setLoadError('');

    const baseUrl = getApiBaseUrl();

    const res = await axios.get(`${baseUrl}/api/support/tickets`, {
      withCredentials: true, // ✅ HttpOnly cookie auth
    });

    const records = normalizeTickets(res.data);

    if (!cancelled) {
      setTickets(records);
    }
  } catch (err) {
    console.error('Error fetching tickets:', err);
    if (!cancelled) {
      setLoadError(
        err.response?.data?.message ||
        err.message ||
        'Failed to load tickets.'
      );
    }
  } finally {
    if (!cancelled) setLoading(false);
  }
};

fetchTickets();

    return () => {
      cancelled = true;
    };
  }, []);

  // ---------- STATS ----------
  const stats = useMemo(() => {
    const total = tickets.length;
    const open = tickets.filter((t) => t.status === 'open').length;
    const inprogress = tickets.filter((t) => t.status === 'in-progress').length;
    const resolved = tickets.filter((t) => t.status === 'resolved').length;
    return { total, open, inprogress, resolved };
  }, [tickets]);

  // ---------- FILTERED LIST ----------
  const filtered = useMemo(() => {
    return tickets.filter((t) => {
      const q = search.trim().toLowerCase();
      if (q) {
        const hay = `${t.ticketId || ''} ${t.subject || ''} ${
          t.employeeName || ''
        } ${t.description || ''}`.toLowerCase();
        if (!hay.includes(q)) return false;
      }
      if (filterStatus !== 'all' && t.status !== filterStatus) return false;
      if (filterPriority !== 'all' && (t.priority || '').toLowerCase() !== filterPriority)
        return false;
      if (filterCategory !== 'all' && (t.category || '').toLowerCase() !== filterCategory)
        return false;
      return true;
    });
  }, [tickets, search, filterStatus, filterPriority, filterCategory]);

  // ---------- CATEGORIES ----------
  const categories = useMemo(() => {
    const set = new Set(
      tickets
        .map((t) => (t.category || '').toLowerCase())
        .filter((c) => c && c.trim().length > 0),
    );
    return Array.from(set);
  }, [tickets]);

  // ---------- API SYNC HELPERS (NO ENDPOINT NAME CHANGES) ----------
const syncNewTicketToServer = async (ticket) => {
  try {
    const baseUrl = getApiBaseUrl();
    await axios.post(
      `${baseUrl}/api/support/tickets`,
      ticket,
      { withCredentials: true }
    );
  } catch (err) {
    console.error('Failed to sync new ticket:', err);
  }
};

const syncStatusToServer = async (ticketId, status) => {
  try {
    const baseUrl = getApiBaseUrl();
    await axios.patch(
      `${baseUrl}/api/support/tickets/${ticketId}/status`,
      { status },
      { withCredentials: true }
    );
  } catch (err) {
    console.error('Failed to sync ticket status:', err);
  }
};

const syncReplyToServer = async (ticketId, comment) => {
  try {
    const baseUrl = getApiBaseUrl();
    await axios.post(
      `${baseUrl}/api/support/tickets/${ticketId}/comments`,
      comment,
      { withCredentials: true }
    );
  } catch (err) {
    console.error('Failed to sync comment:', err);
  }
};


  // ---------- NEW TICKET (QUICK ACTION) ----------
  const handleNewTicketQuick = () => {
    const today = new Date().toISOString().slice(0, 10);

    // simple validation: must have at least subject/description
    const newTicket = {
      ticketId: `TKT-${1000 + tickets.length + 1}`,
      employeeId: 'EMP-NEW',
      employeeName: 'Quick User',
      subject: 'New quick ticket',
      description: 'Created from quick action (demo).',
      category: 'General',
      priority: 'Low',
      status: 'open',
      comments: [],
      attachments: [],
      createdAt: today,
      updatedAt: today,
    };

    setTickets((s) => [newTicket, ...s]);
    syncNewTicketToServer(newTicket);
  };

  // ---------- STATUS CHANGE ----------
  const changeTicketStatus = (ticketId, status) => {
    setTickets((prev) =>
      prev.map((t) =>
        t.ticketId === ticketId
          ? { ...t, status, updatedAt: new Date().toISOString().slice(0, 10) }
          : t,
      ),
    );
    if (selectedTicket?.ticketId === ticketId) {
      setSelectedTicket((s) => (s ? { ...s, status } : s));
    }
    syncStatusToServer(ticketId, status);
  };

  // ---------- REPLY SUBMISSION (CONTROLLED + VALIDATION + API) ----------
  const sendReply = () => {
    if (!selectedTicket) return;
    if (!reply.trim()) return; // validation: ignore empty reply

    const newComment = {
      from: 'Admin',
      text: reply.trim(),
      at: new Date().toISOString(),
    };

    setTickets((prev) =>
      prev.map((t) =>
        t.ticketId === selectedTicket.ticketId
          ? {
              ...t,
              comments: [...(t.comments || []), newComment],
              updatedAt: new Date().toISOString().slice(0, 10),
            }
          : t,
      ),
    );
    setSelectedTicket((s) =>
      s
        ? {
            ...s,
            comments: [...(s.comments || []), newComment],
            updatedAt: new Date().toISOString().slice(0, 10),
          }
        : s,
    );

    syncReplyToServer(selectedTicket.ticketId, newComment);
    setReply('');
  };

  // ---------- SELECT DEFAULT TICKET WHEN FILTERED LIST CHANGES ----------
  useEffect(() => {
    if (!selectedTicket && filtered.length) {
      setSelectedTicket(filtered[0]);
    }
    if (filtered.length === 0) setSelectedTicket(null);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filtered]);

  return (
    <div className="w-full min-h-screen bg-[#F5F7FF]">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
        {/* Header */}
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="relative">
              <div className="absolute inset-0 rounded-2xl bg-[#011A8B]/15 blur-md" />
              <div className="relative flex h-12 w-12 items-center justify-center rounded-2xl bg-[#011A8B] shadow-md">
                <MessageSquare className="w-6 h-6 text-white" />
              </div>
            </div>
          <div>
              <h1 className="text-2xl md:text-3xl font-semibold text-[#011A8B]">
                Support Center
              </h1>
              <p className="text-sm text-gray-600">
                Manage employee tickets and resolve issues faster.
              </p>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 text-sm">
            <div className="inline-flex items-center gap-2 rounded-full border border-gray-200 bg-white px-3 py-1.5 shadow-sm">
              <Phone className="w-4 h-4 text-[#011A8B]" />
              <span className="text-gray-700">+1 (91) 123-4567</span>
            </div>
            <div className="inline-flex items-center gap-2 rounded-full border border-gray-200 bg-white px-3 py-1.5 shadow-sm">
              <Mail className="w-4 h-4 text-[#011A8B]" />
              <span className="text-gray-700">support@hirecorehr.com</span>
            </div>
          </div>
        </div>

        {/* Quick actions */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* New ticket primary card */}
          <button
            type="button"
            onClick={handleNewTicketQuick}
            className="group cursor-pointer rounded-2xl border border-transparent bg-gradient-to-r from-[#011A8B] to-[#2745D9] px-4 py-4 text-left text-white shadow-sm transition hover:shadow-md"
          >
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2 font-medium">
                  <Plus className="w-4 h-4" />
                  <span>New Ticket</span>
                </div>
                <p className="mt-2 text-xs text-blue-100">
                  Create a quick ticket (demo)
                </p>
              </div>
              <Clock className="w-8 h-8 opacity-80" />
            </div>
          </button>

          <div className="rounded-2xl border border-gray-200 bg-white px-4 py-4 shadow-sm">
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-[#E0E5FF]">
                    <BookOpen className="w-4 h-4 text-[#011A8B]" />
                  </div>
                  <span className="text-sm font-semibold text-gray-900">
                    Knowledge Base
                  </span>
                </div>
                <p className="mt-2 text-xs text-gray-500">Search internal articles</p>
              </div>
              <FileText className="w-7 h-7 text-gray-300" />
            </div>
          </div>

          <div className="rounded-2xl border border-gray-200 bg-white px-4 py-4 shadow-sm">
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-[#FFEDE5]">
                    <HelpCircle className="w-4 h-4 text-[#F97316]" />
                  </div>
                  <span className="text-sm font-semibold text-gray-900">FAQs</span>
                </div>
                <p className="mt-2 text-xs text-gray-500">Common support answers</p>
              </div>
              <MessageSquare className="w-7 h-7 text-gray-300" />
            </div>
          </div>

          <div className="rounded-2xl border border-gray-200 bg-white px-4 py-4 shadow-sm">
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-[#E5F6FF]">
                    <FileText className="w-4 h-4 text-[#0284C7]" />
                  </div>
                  <span className="text-sm font-semibold text-gray-900">
                    Documentation
                  </span>
                </div>
                <p className="mt-2 text-xs text-gray-500">Guides & API docs</p>
              </div>
              <HelpCircle className="w-7 h-7 text-gray-300" />
            </div>
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
          <div className="rounded-2xl border border-gray-200 bg-white px-4 py-3 shadow-sm">
            <div className="text-xs text-gray-500">Total Tickets</div>
            <div className="mt-1 text-2xl font-semibold text-[#011A8B]">
              {stats.total}
            </div>
          </div>
          <div className="rounded-2xl border border-gray-200 bg-white px-4 py-3 shadow-sm">
            <div className="text-xs text-gray-500">Open</div>
            <div className="mt-1 text-2xl font-semibold text-[#0B82F6]">
              {stats.open}
            </div>
          </div>
          <div className="rounded-2xl border border-gray-200 bg-white px-4 py-3 shadow-sm">
            <div className="text-xs text-gray-500">In Progress</div>
            <div className="mt-1 text-2xl font-semibold text-[#D97706]">
              {stats.inprogress}
            </div>
          </div>
          <div className="rounded-2xl border border-gray-200 bg-white px-4 py-3 shadow-sm">
            <div className="text-xs text-gray-500">Resolved</div>
            <div className="mt-1 text-2xl font-semibold text-[#16A34A]">
              {stats.resolved}
            </div>
          </div>
        </div>

        {/* Search & Filters */}
        <div className="flex flex-col gap-3 rounded-2xl border border-gray-200 bg-white px-4 py-3 shadow-sm md:flex-row md:items-center md:gap-4">
          <div className="flex flex-1 items-center gap-2">
            <div className="relative w-full">
              <div className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2">
                <Search className="w-4 h-4 text-gray-400" />
              </div>
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search by ticket id, subject, employee..."
                className="w-full rounded-full border border-gray-300 bg-white pl-9 pr-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#011A8B]/50"
              />
            </div>
            <button
              title="Filters"
              className="inline-flex items-center justify-center rounded-full border border-gray-300 bg-gray-50 p-2 text-gray-600 hover:bg-gray-100"
              onClick={() => {}}
            >
              <Filter className="w-4 h-4" />
            </button>
          </div>

          <div className="flex flex-wrap items-center gap-2 text-sm">
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="rounded-full border border-gray-300 bg-white px-3 py-1.5 text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#011A8B]/40"
            >
              <option value="all">All status</option>
              <option value="open">Open</option>
              <option value="in-progress">In Progress</option>
              <option value="resolved">Resolved</option>
            </select>

            <select
              value={filterPriority}
              onChange={(e) => setFilterPriority(e.target.value)}
              className="rounded-full border border-gray-300 bg-white px-3 py-1.5 text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#011A8B]/40"
            >
              <option value="all">All priority</option>
              <option value="urgent">Urgent</option>
              <option value="high">High</option>
              <option value="medium">Medium</option>
              <option value="low">Low</option>
            </select>

            <select
              value={filterCategory}
              onChange={(e) => setFilterCategory(e.target.value)}
              className="rounded-full border border-gray-300 bg-white px-3 py-1.5 text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#011A8B]/40"
            >
              <option value="all">All categories</option>
              {categories.map((c) => (
                <option key={c} value={c}>
                  {c.charAt(0).toUpperCase() + c.slice(1)}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Main layout */}
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          {/* Ticket list */}
          <div className="lg:col-span-2">
            <div className="rounded-2xl border border-gray-200 bg-white px-4 py-4 shadow-sm">
              <h2 className="mb-3 text-base font-semibold text-gray-900">Tickets</h2>

              {loading ? (
                <div className="py-10 text-center text-sm text-gray-500">
                  Loading tickets...
                </div>
              ) : loadError && tickets.length === 0 ? (
                <div className="py-10 text-center text-sm text-red-500">
                  {loadError}
                </div>
              ) : filtered.length === 0 ? (
                <div className="py-10 text-center text-sm text-gray-500">
                  No tickets match the selected filters.
                </div>
              ) : (
                <div className="space-y-3">
                  {filtered.map((t) => (
                    <button
                      key={t.ticketId}
                      type="button"
                      onClick={() => setSelectedTicket(t)}
                      className={`w-full rounded-xl border px-4 py-3 text-left transition ${
                        selectedTicket?.ticketId === t.ticketId
                          ? 'border-[#011A8B] bg-[#F3F4FF]'
                          : 'border-gray-200 bg-white hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex items-start gap-3">
                          <div
                            className={`mt-1 h-2.5 w-2.5 rounded-full ${statusDot(
                              t.status,
                            )}`}
                          />
                          <div>
                            <div className="flex flex-wrap items-center gap-2">
                              <span className="text-sm font-medium text-gray-900">
                                {t.subject}
                              </span>
                              <span className="rounded-full bg-gray-100 px-2 py-0.5 text-[11px] font-medium text-gray-600">
                                {t.ticketId}
                              </span>
                            </div>
                            <div className="mt-1 text-xs text-gray-500">
                              {t.employeeName} · {t.category}
                            </div>
                          </div>
                        </div>
                        <div className="text-right">
                          <div className={`text-xs font-semibold ${priorityClass(t.priority)}`}>
                            {t.priority}
                          </div>
                          <div className="mt-1 text-[11px] text-gray-400">
                            {t.createdAt}
                          </div>
                        </div>
                      </div>
                      <p className="mt-2 line-clamp-2 text-xs text-gray-600">
                        {t.description}
                      </p>
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Ticket details */}
          <div>
            <div className="min-h-[260px] rounded-2xl border border-gray-200 bg-white px-4 py-4 shadow-sm">
              {selectedTicket ? (
                <>
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="text-[11px] text-gray-400">
                        {selectedTicket.ticketId}
                      </div>
                      <h3 className="text-base font-semibold text-gray-900">
                        {selectedTicket.subject}
                      </h3>
                      <div className="mt-1 text-xs text-gray-500">
                        {selectedTicket.employeeName} · {selectedTicket.category}
                      </div>
                    </div>
                    <div className="text-right">
                      <div
                        className={`inline-flex items-center rounded-full px-2 py-1 text-[11px] font-medium ${
                          selectedTicket.status === 'open'
                            ? 'bg-blue-50 text-blue-700'
                            : selectedTicket.status === 'in-progress'
                            ? 'bg-amber-50 text-amber-700'
                            : 'bg-emerald-50 text-emerald-700'
                        }`}
                      >
                        <span
                          className={`mr-1 h-1.5 w-1.5 rounded-full ${statusDot(
                            selectedTicket.status,
                          )}`}
                        />
                        {selectedTicket.status}
                      </div>
                      <div className="mt-1 text-[11px] text-gray-400">
                        {selectedTicket.createdAt}
                      </div>
                    </div>
                  </div>

                  <p className="mt-3 text-sm text-gray-700">
                    {selectedTicket.description}
                  </p>

                  <div className="mt-4 flex gap-2">
                    <button
                      onClick={() =>
                        changeTicketStatus(selectedTicket.ticketId, 'in-progress')
                      }
                      className="flex-1 rounded-full bg-amber-100 px-3 py-2 text-xs font-medium text-amber-800 hover:bg-amber-200"
                    >
                      Mark In Progress
                    </button>
                    <button
                      onClick={() =>
                        changeTicketStatus(selectedTicket.ticketId, 'resolved')
                      }
                      className="flex-1 rounded-full bg-emerald-100 px-3 py-2 text-xs font-medium text-emerald-800 hover:bg-emerald-200"
                    >
                      Resolve
                    </button>
                  </div>

                  {/* Comments */}
                  <div className="mt-4">
                    <div className="mb-2 text-xs font-semibold text-gray-800">
                      Comments
                    </div>
                    <div className="max-h-40 space-y-2 overflow-auto pr-1">
                      {(selectedTicket.comments || []).length === 0 && (
                        <div className="text-xs text-gray-400">No comments yet.</div>
                      )}
                      {(selectedTicket.comments || []).map((c, i) => (
                        <div
                          key={i}
                          className="rounded-lg border border-gray-100 bg-gray-50 px-2 py-2"
                        >
                          <div className="text-[11px] text-gray-500">
                            {c.from} · {new Date(c.at).toLocaleString()}
                          </div>
                          <div className="mt-1 text-xs text-gray-800">{c.text}</div>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Reply box */}
                  <div className="mt-4">
                    <textarea
                      value={reply}
                      onChange={(e) => setReply(e.target.value)}
                      placeholder="Write a reply..."
                      rows={3}
                      className="w-full rounded-xl border border-gray-300 bg-white px-3 py-2 text-xs text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#011A8B]/40"
                    />
                    <div className="mt-2 flex justify-end">
                      <button
                        onClick={sendReply}
                        className="inline-flex items-center gap-1 rounded-full bg-[#011A8B] px-4 py-1.5 text-xs font-medium text-white hover:bg-[#02106a]"
                      >
                        <Send className="h-3.5 w-3.5" />
                        Send
                      </button>
                    </div>
                  </div>
                </>
              ) : (
                <div className="flex h-full flex-col items-center justify-center gap-2 py-10 text-center">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#E0E5FF]">
                    <MessageSquare className="h-5 w-5 text-[#011A8B]" />
                  </div>
                  <div className="text-sm font-medium text-gray-800">
                    Select a ticket to view details
                  </div>
                  <div className="text-xs text-gray-500">
                    Click on a ticket from the list to see full information here.
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

