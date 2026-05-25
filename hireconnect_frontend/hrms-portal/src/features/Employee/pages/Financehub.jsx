import React, { useEffect, useState } from "react";
import { Download } from "lucide-react";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";

import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  withCredentials: true,
});

export default function Payslip() {
  const [payrolls, setPayrolls] = useState([]);
  const [loading, setLoading] = useState(true);

useEffect(() => {
  const loadPayrolls = async () => {
    try {
      const res = await api.get("/api/payroll/my");
      setPayrolls(res.data?.data || res.data || []);
    } catch (err) {
      console.error("Error fetching payrolls:", err);
      setPayrolls([]);
    } finally {
      setLoading(false);
    }
  };

  loadPayrolls();
}, []);



  const money = (num) => "₹" + Number(num).toLocaleString("en-IN");
  const moneyPDF = (num) => "Rs. " + Number(num).toLocaleString("en-IN");

  const numberToWords = (num) => {
  num = Math.round(Number(num) || 0);
  if (num === 0) return "Zero Only";

  const ones = ["","One","Two","Three","Four","Five","Six","Seven","Eight","Nine"];
  const teens = ["Ten","Eleven","Twelve","Thirteen","Fourteen","Fifteen","Sixteen","Seventeen","Eighteen","Nineteen"];
  const tens = ["","","Twenty","Thirty","Forty","Fifty","Sixty","Seventy","Eighty","Ninety"];

  const helper = (n) => {
    let words = "";

    if (n >= 10000000) {
      words += helper(Math.floor(n / 10000000)) + " Crore ";
      n %= 10000000;
    }
    if (n >= 100000) {
      words += helper(Math.floor(n / 100000)) + " Lakh ";
      n %= 100000;
    }
    if (n >= 1000) {
      words += helper(Math.floor(n / 1000)) + " Thousand ";
      n %= 1000;
    }
    if (n >= 100) {
      words += ones[Math.floor(n / 100)] + " Hundred ";
      n %= 100;
    }
    if (n >= 20) {
      words += tens[Math.floor(n / 10)];
      if (n % 10) words += " " + ones[n % 10];
    } else if (n >= 10) {
      words += teens[n - 10];
    } else if (n > 0) {
      words += ones[n];
    }

    return words.trim();
  };

  return helper(num) + " Only";
};


  // --- FIX: Extract Employee details safely ---
  const getEmployeeName = (p) =>
    p.employeeName ||
    p.employee?.name ||
    p.userName ||
    "N/A";

  const getEmployeeId = (p) =>
    p.employeeId ||
    p.employee?.id ||
    p.userId ||
    "N/A";

    const formatMonthYear = (dateStr) => {
  if (!dateStr) return "N/A";

  const date = new Date(dateStr);
  return date.toLocaleString("en-GB", {
    month: "long",
    year: "numeric",
  });
};

  const downloadPDF = (p) => {
    const employeeName = getEmployeeName(p);
    const employeeId = getEmployeeId(p);

    const doc = new jsPDF("p", "pt", "a4");
    const pageWidth = doc.internal.pageSize.width;

    const company = {
      name: "Taheer Global Tech",
      address:
        "Plot No. 40, 12th Main Road, Near HSR BDA Complex Bengaluru, Karnataka - 560102",
      website: "www.taheerglobaltech.com",
      email: "info@taheerglobaltech.com",
      phone: "+91 080 7755893",
      gst: "GST No :- 29590129309",
      logo: "/assets/Logo_Truerize.png",
    };

    const formatDateDDMMYYYY = (dateStr) => {
  if (!dateStr) return "N/A";

  const date = new Date(dateStr);
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();

  return `${day}-${month}-${year}`;
};

const employee = {
  id: employeeId,
  name: employeeName,
  joiningDate: p.joiningDate || "N/A",
  payPeriod: p.payPeriod || p.payrollMonth,
  payDate: p.payDate || p.payrollMonth,
  month: p.payrollMonth || "N/A",

  basic: p.basicSalary || 0,
  hra: p.hra || 0,
  conveyance: p.conveyanceAllowance || 0,
  medical: p.medicalAllowance || 0,
  special: p.otherAllowances || 0,

  employerPf: p.pfEmployer || 0,    
  pfEmployee: p.pfEmployee || 0,

  deduction_IT: p.taxDeductions || 0,
  deduction_PT: p.professionalTax || 0,
};

    const totalEarnings =
      employee.basic +
      employee.hra +
      employee.conveyance +
      employee.medical +
      employee.special;

const totalDeductions =
  employee.deduction_IT +
  employee.pfEmployee +
  employee.deduction_PT;

    const netSalary = totalEarnings - totalDeductions;

const logo = new Image();
logo.src = company.logo;

logo.onload = () => {
  /* ===== LOGO ===== */
  doc.addImage(logo, "PNG", 40, 30, 55, 55);

  /* ===== COMPANY NAME (LEFT BLOCK) ===== */
  doc.setFontSize(18);
  doc.setFont("helvetica", "bold");
  doc.setTextColor(0, 26, 91);
  doc.text("Taheer", 110, 45);
  doc.text("Global Tech", 110, 65);

  /* ===== PAYSLIP MONTH (RIGHT BLOCK – SEPARATE COLUMN) ===== */
  const rightX = pageWidth - 40;

  doc.setFontSize(10);
  doc.setFont("helvetica", "normal");
  doc.setTextColor(80);
  doc.text(
    "Payslip For the Month",
    rightX,
    45,
    { align: "right" }
  );

  doc.setFontSize(12);
  doc.setFont("helvetica", "bold");
  doc.setTextColor(0);
  doc.text(
    formatMonthYear(employee.month), // e.g. September 2025
    rightX,
    65,
    { align: "right" }
  );

  /* ===== ADDRESS ===== */
  doc.setFontSize(9);
  doc.setFont("helvetica", "normal");
  doc.setTextColor(0);

  doc.text(
    company.address,
    40,
    100
  );

  doc.text(
    `${company.website} | ${company.email} | ${company.phone} | ${company.gst}`,
    40,
    115
  );

  /* ===== DIVIDER LINE ===== */
  doc.setDrawColor(180);
  doc.line(40, 130, pageWidth - 40, 130);


      // EMPLOYEE SUMMARY
      const summaryStartY = 145;
      doc.setFontSize(11);
      doc.setFont("helvetica", "bold");
      doc.text("EMPLOYEE SUMMARY", 40, summaryStartY);

      let textY = summaryStartY + 15;
      doc.setFontSize(9);
      doc.setFont("helvetica", "normal");
      doc.text("Employee Name" + " : ", 70, textY);
      doc.text(employee.name, 200, textY);
      textY += 15;
      doc.text("Employee ID"  + " : ", 70, textY);
      doc.text(employee.id, 200, textY);

      textY += 15;
      doc.text("Joining Date"  + " : ", 70, textY);
      doc.text(formatDateDDMMYYYY(employee.joiningDate), 200, textY);


      textY += 15;
      doc.text("Pay Period"  + " : ", 70, textY);
      doc.text(employee.payPeriod, 200, textY);

      textY += 15;
      doc.text("Pay Date"  + " : ", 70, textY);
      doc.text(formatDateDDMMYYYY(employee.payDate), 200, textY);

      // Earnings table
      const tablesStartY = textY + 25;

      autoTable(doc, {
        startY: tablesStartY + 5,
        margin: { left: 40, right: pageWidth / 2 + 10 },
        head: [["EARNINGS", "AMOUNT"]],
        body: [
          ["Basic", moneyPDF(employee.basic)],
          ["House Rent Allowance", moneyPDF(employee.hra)],
          ["Conveyance", moneyPDF(employee.conveyance)],
          ["Medical Allowance", moneyPDF(employee.medical)],
          ["Special Allowance", moneyPDF(employee.special)],
          ["Employer PF Contribution", moneyPDF(employee.employerPf)],
          ["", ""],
          ["Gross Earnings", moneyPDF(totalEarnings)],
        ],
      });

      // Deductions
autoTable(doc, {
  startY: tablesStartY + 5,
  margin: { left: pageWidth / 2 + 20, right: 40 },
  head: [["DEDUCTIONS", "AMOUNT"]],
  body: [
    ["Income Tax", moneyPDF(employee.deduction_IT)],
    ["Provident Fund (Employee)", moneyPDF(employee.pfEmployee)],
    ["Professional Tax", moneyPDF(employee.deduction_PT)],
    ["Total Deductions", moneyPDF(totalDeductions)],
  ],
});
      const lastTableY = doc.lastAutoTable?.finalY || tablesStartY;
      const finalY = Math.max(lastTableY, tablesStartY + 200) + 20;


// TOTAL NET PAYABLE
doc.setFontSize(11);
doc.setFont("helvetica", "bold");
doc.text("TOTAL NET PAYABLE", 55, finalY + 20);
doc.text(moneyPDF(netSalary), pageWidth - 160, finalY + 20);

// Amount in words (uses numberToWords)
doc.setFontSize(9);
doc.setFont("helvetica", "normal");

const wordsLine = `Amount In Words : Indian Rupee ${numberToWords(netSalary)}`;
doc.text(
  doc.splitTextToSize(wordsLine, pageWidth - 80),
  40,
  finalY + 45
);

// Save PDF
doc.save(`Payslip_${employee.name}_${employee.month}.pdf`);

    };
  };

  // ✅ Loading state
if (loading) {
  return <p className="p-4">Loading payroll data...</p>;
}


  return (
    <div className="w-full p-6">
      <h2 className="text-xl font-semibold text-[#011A8B] mb-4">Payslip</h2>

      <div className="rounded-2xl border border-gray-200 shadow-sm overflow-x-auto">
        <table className="min-w-full bg-white">
          <thead className="bg-[#F3F4FF] text-[#011A8B] text-sm font-semibold">
            <tr>
              <th className="px-4 py-3 text-left">Employee Name</th>
              <th className="px-4 py-3 text-left">Employee ID</th>
              <th className="px-4 py-3 text-left">Month</th>
              <th className="px-4 py-3 text-left">Source</th>
              <th className="px-4 py-3 text-left">Net Salary</th>
              <th className="px-4 py-3 text-left">Actions</th>
            </tr>
          </thead>

          <tbody className="text-sm text-gray-700">
            {payrolls.map((p) => {
              const netSalary = (p.totalEarnings || 0) - (p.totalDeductions || 0);

              return (
                <tr key={p.id} className="border-t hover:bg-gray-50 transition">
                  <td className="px-4 py-3">{getEmployeeName(p)}</td>
                  <td className="px-4 py-3">{getEmployeeId(p)}</td>
                  <td className="px-4 py-3">{p.payrollMonth}</td>
                  <td className="px-4 py-3">{p.source || "Payroll System"}</td>
                  <td className="px-4 py-3 font-semibold">{money(netSalary)}</td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => downloadPDF(p)}
                      className="hover:text-[#011A8B] transition"
                    >
                      <Download size={20} />
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
