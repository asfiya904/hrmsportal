'use client';

import React, { useEffect, useMemo, useState, useCallback } from 'react';
import {
  Plus,
  Search,
  MessageSquare,
  Clock,
  HelpCircle,
} from 'lucide-react';
import axios from 'axios';

/* ---------- AXIOS SETUP ---------- */
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // HttpOnly cookie auth
});

/* ---------- MAIN COMPONENT ---------- */
export default function MyTickets() {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);

  const [activeTab, setActiveTab] = useState('all');
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('all');

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [viewingTicket, setViewingTicket] = useState(null);

  const [newSubject, setNewSubject] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [newCategory, setNewCategory] = useState('General');
  const [newPriority, setNewPriority] = useState('Low');

  /* ---------- FETCH TICKETS ---------- */
  const fetchTickets = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/employee/tickets');
      setTickets(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error('Failed to load tickets', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTickets();
  }, [fetchTickets]);

  /* ---------- DERIVED DATA ---------- */
  const categories = useMemo(
    () => Array.from(new Set(tickets.map((t) => t.category))),
    [tickets],
  );

  const filtered = useMemo(() => {
    return tickets.filter((t) => {
      if (activeTab !== 'all' && t.status !== activeTab) return false;
      if (categoryFilter !== 'all' && t.category !== categoryFilter) return false;

      const q = search.trim().toLowerCase();
      if (q) {
        const hay = `${t.ticketId} ${t.subject} ${t.description}`.toLowerCase();
        if (!hay.includes(q)) return false;
      }
      return true;
    });
  }, [tickets, activeTab, categoryFilter, search]);

  /* ---------- ACTIONS ---------- */
  const createTicket = async (e) => {
    e.preventDefault();
    if (!newSubject.trim() || !newDescription.trim()) return;

    try {
      await api.post('/api/employee/tickets', {
        subject: newSubject.trim(),
        description: newDescription.trim(),
        category: newCategory,
        priority: newPriority,
      });

      setShowCreateModal(false);
      setNewSubject('');
      setNewDescription('');
      setNewCategory('General');
      setNewPriority('Low');

      fetchTickets();
    } catch (err) {
      console.error('Create ticket failed', err);
    }
  };

  const openView = async (ticketId) => {
    try {
      const res = await api.get(`/api/employee/tickets/${ticketId}`);
      setViewingTicket(res.data);
      setShowViewModal(true);
    } catch (err) {
      console.error('Failed to load ticket details', err);
    }
  };

  const addComment = async (ticketId, text) => {
    if (!text.trim()) return;

    try {
      await api.post(`/api/employee/tickets/${ticketId}/comments`, {
        message: text.trim(),
      });

      const updated = await api.get(`/api/employee/tickets/${ticketId}`);
      setViewingTicket(updated.data);
      fetchTickets();
    } catch (err) {
      console.error('Add comment failed', err);
    }
  };

  /* ---------- BADGES ---------- */
  const priorityBadgeClass = (p) => {
    const v = (p || '').toLowerCase();
    if (v === 'urgent') return 'bg-red-50 text-red-700 border border-red-200';
    if (v === 'high') return 'bg-orange-50 text-orange-700 border border-orange-200';
    if (v === 'medium') return 'bg-amber-50 text-amber-700 border border-amber-200';
    if (v === 'low') return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
    return 'bg-slate-100 text-slate-700 border border-slate-200';
  };

  const statusBadgeClass = (s) => {
    if (s === 'open') return 'bg-blue-50 text-blue-700 border border-blue-200';
    if (s === 'in-progress') return 'bg-amber-50 text-amber-700 border border-amber-200';
    if (s === 'resolved') return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
    return 'bg-slate-100 text-slate-700 border border-slate-200';
  };

  /* ---------- UI ---------- */
  return (
    <div className="w-full min-h-screen bg-[#F5F7FF]">
      <div className="max-w-6xl mx-auto px-4 py-6 space-y-6">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 flex items-center justify-center rounded-2xl bg-[#011A8B]">
              <MessageSquare className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-2xl font-semibold text-[#011A8B]">
                My Support Tickets
              </h1>
              <p className="text-sm text-gray-600">
                Track and manage your support requests.
              </p>
            </div>
          </div>

          <button
            onClick={() => setShowCreateModal(true)}
            className="inline-flex items-center gap-2 rounded-full bg-[#011A8B] px-4 py-2 text-sm text-white"
          >
            <Plus className="w-4 h-4" />
            Create Ticket
          </button>
        </div>

        {/* Help Note */}
        <div className="flex items-center gap-2 rounded-2xl border border-blue-100 bg-[#F3F4FF] px-4 py-2 text-xs text-gray-600">
          <HelpCircle className="h-4 w-4 text-[#011A8B]" />
          For urgent issues, please contact HR / IT directly.
        </div>

        {/* Filters */}
        <div className="flex flex-wrap gap-2">
          {['all', 'open', 'in-progress', 'resolved'].map((k) => (
            <button
              key={k}
              onClick={() => setActiveTab(k)}
              className={`rounded-full px-4 py-1.5 text-xs ${
                activeTab === k
                  ? 'bg-[#011A8B] text-white'
                  : 'bg-white border'
              }`}
            >
              {k}
            </button>
          ))}
        </div>

        {/* Search */}
        <div className="flex gap-3 bg-white p-3 rounded-2xl border">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search tickets..."
              className="w-full pl-9 py-2 rounded-full border"
            />
          </div>

          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="rounded-full border px-3 py-2 text-xs"
          >
            <option value="all">All</option>
            {categories.map((c) => (
              <option key={c}>{c}</option>
            ))}
          </select>
        </div>

        {/* Tickets */}
        {loading ? (
          <div className="text-sm text-gray-500">Loading tickets…</div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {filtered.map((t) => (
              <button
                key={t.ticketId}
                onClick={() => openView(t.ticketId)}
                className="text-left rounded-2xl border bg-white p-4 hover:shadow"
              >
                <div className="flex justify-between text-xs text-gray-400">
                  <span className={statusBadgeClass(t.status)}>{t.status}</span>
                  <span>{t.ticketId}</span>
                </div>
                <h3 className="mt-2 text-sm font-semibold">{t.subject}</h3>
                <p className="mt-1 text-xs text-gray-600 line-clamp-3">
                  {t.description}
                </p>
                <div className="mt-2 flex gap-2 text-[11px]">
                  <span className="px-2 py-0.5 bg-gray-100 rounded-full">
                    {t.category}
                  </span>
                  <span className={priorityBadgeClass(t.priority)}>
                    {t.priority}
                  </span>
                </div>
              </button>
            ))}
          </div>
        )}

        {/* Create Modal */}
        {showCreateModal && (
          <Modal onClose={() => setShowCreateModal(false)}>
            <form onSubmit={createTicket} className="space-y-4">
              <input
                value={newSubject}
                onChange={(e) => setNewSubject(e.target.value)}
                placeholder="Subject"
                className="w-full border rounded p-2"
              />
              <textarea
                value={newDescription}
                onChange={(e) => setNewDescription(e.target.value)}
                placeholder="Description"
                className="w-full border rounded p-2"
              />
              <div className="flex gap-2">
                <select value={newCategory} onChange={(e) => setNewCategory(e.target.value)} className="border rounded p-2 flex-1">
                  <option>General</option>
                  <option>Access</option>
                  <option>Payroll</option>
                  <option>Hardware</option>
                  <option>HR</option>
                </select>
                <select value={newPriority} onChange={(e) => setNewPriority(e.target.value)} className="border rounded p-2 w-32">
                  <option>Low</option>
                  <option>Medium</option>
                  <option>High</option>
                  <option>Urgent</option>
                </select>
              </div>
              <button className="bg-[#011A8B] text-white px-4 py-2 rounded-full">
                Create Ticket
              </button>
            </form>
          </Modal>
        )}

        {/* View Modal */}
        {showViewModal && viewingTicket && (
          <ViewTicketModal
            ticket={viewingTicket}
            onClose={() => {
              setShowViewModal(false);
              setViewingTicket(null);
            }}
            onAddComment={(text) => addComment(viewingTicket.ticketId, text)}
          />
        )}
      </div>
    </div>
  );
}

/* ---------- MODAL ---------- */
function Modal({ children, onClose }) {
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40">
      <div className="bg-white rounded-2xl p-6 w-full max-w-xl">
        <button onClick={onClose} className="text-xs text-gray-500 mb-3">
          Close
        </button>
        {children}
      </div>
    </div>
  );
}

/* ---------- VIEW TICKET ---------- */
function ViewTicketModal({ ticket, onClose, onAddComment }) {
  const [commentText, setCommentText] = useState('');

  return (
    <Modal onClose={onClose}>
      <h2 className="text-lg font-semibold">{ticket.subject}</h2>
      <p className="text-sm text-gray-600 mt-2">{ticket.description}</p>

      <div className="mt-4 space-y-2">
        {ticket.comments?.map((c, i) => (
          <div key={i} className="text-xs bg-gray-50 p-2 rounded">
            <div className="text-gray-500">{c.from}</div>
            {c.text}
          </div>
        ))}
      </div>

      <form
        onSubmit={(e) => {
          e.preventDefault();
          onAddComment(commentText);
          setCommentText('');
        }}
        className="mt-4"
      >
        <textarea
          value={commentText}
          onChange={(e) => setCommentText(e.target.value)}
          placeholder="Add comment..."
          className="w-full border rounded p-2 text-xs"
        />
        <button className="mt-2 bg-[#011A8B] text-white px-4 py-1.5 rounded-full text-xs">
          Add Comment
        </button>
      </form>
    </Modal>
  );
}
