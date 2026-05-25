// import React from "react";
// import { Outlet, useNavigate } from "react-router-dom";
// import Sidebar from "../components/AdminSidebar.jsx";   
// import TopNav from "../components/Navbar.jsx";     

// const AdminLayout = () => {
//   const navigate = useNavigate();

//   const handleLogout = () => {
//     // clear auth if you want
// sessionStorage.clear();

//     navigate("/login");
//   };

//   return (
//       <div className="min-h-screen flex bg-white text-gray-800">
//       {/* Sidebar stays constant */}
//       <Sidebar />

//       {/* Right side: navbar + page content */}
//       <div className="flex-1 flex flex-col">
//         {/* Top nav stays constant */}
//         <TopNav onLogout={handleLogout} />

//         {/* Page content changes here */}
        
//         <main className="flex-1 bg-[#F5F6FB] p-4 md:p-6">
//           <Outlet />
//         </main>
//       </div>
//     </div>
//   );
// };

// export default AdminLayout;

import React from "react";
import { Outlet, useNavigate } from "react-router-dom";
import axios from "axios";
import Sidebar from "../components/AdminSidebar.jsx";
import TopNav from "../components/Navbar.jsx";

const api = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true,
});

const AdminLayout = () => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await api.post("/api/auth/logout"); // 🔥 REAL LOGOUT
    } catch (err) {
      console.error("Logout failed", err);
    } finally {
      sessionStorage.clear(); // optional
      navigate("/login");
    }
  };

  return (
    <div className="min-h-screen flex bg-white text-gray-800">
      <Sidebar />

      <div className="flex-1 flex flex-col">
        <TopNav onLogout={handleLogout} />

        <main className="flex-1 bg-[#F5F6FB] p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;
