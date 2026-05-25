// import React from "react";
// import { Outlet, useNavigate } from "react-router-dom";
// import EmployeeSidebar from "../Components/EmployeeSidebar.jsx";
// import EmployeeNavbar from "../Components/Navbar.jsx";


// const EmployeeLayout = () => {
//   const navigate = useNavigate();

//   const handleLogout = () => {
// sessionStorage.clear();

//     navigate("/login");
//   };

//   return (

//       <div className="min-h-screen flex bg-white text-gray-800">

      
//       {/* Sidebar stays fixed */}
//       <EmployeeSidebar />

//       {/* Right section */}
//       <div className="flex-1 flex flex-col">
        
//         {/* Top Navbar */}
//         <EmployeeNavbar onLogout={handleLogout} />

//         {/* Page content */}
//         <main className="flex-1 bg-[#F5F6FB] p-4 md:p-6">
//           <Outlet />
//         </main>
//       </div>
//     </div>
//   );
// };

// export default EmployeeLayout;

import React from "react";
import { Outlet, useNavigate } from "react-router-dom";
import axios from "axios";
import EmployeeSidebar from "../Components/EmployeeSidebar.jsx";
import EmployeeNavbar from "../Components/Navbar.jsx";

const api = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true,
});

const EmployeeLayout = () => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await api.post("/api/auth/logout"); // 🔥 REAL LOGOUT
    } catch (err) {
      console.error("Logout failed", err);
    } finally {
      sessionStorage.clear(); // optional (UI state only)
      navigate("/login");
    }
  };

  return (
    <div className="min-h-screen flex bg-white text-gray-800">
      <EmployeeSidebar />

      <div className="flex-1 flex flex-col">
        <EmployeeNavbar onLogout={handleLogout} />

        <main className="flex-1 bg-[#F5F6FB] p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default EmployeeLayout;

