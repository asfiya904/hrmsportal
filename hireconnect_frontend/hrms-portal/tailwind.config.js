// /** @type {import('tailwindcss').Config} */
// export default {
//   content: [
//     "./index.html",
//     "./src/**/*.{js,jsx,ts,tsx}",
//   ],
//   theme: {
//     extend: {
//       colors: {
//         primary: {
//           50: "#eff6ff",
//           100: "#dbeafe",
//           500: "#2563eb",
//           600: "#1d4ed8",
//         },
//         sidebar: "#0f172a",
//       },
//     },
//   },
//   plugins: [],
// };

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        /* EXISTING (kept as-is) */
        primary: {
          50: "#eff6ff",
          100: "#dbeafe",
          500: "#2563eb",
          600: "#1d4ed8",
        },
        sidebar: "#0f172a",

        /* TRUECORE HR BRAND */
        brand: {
          primary: "#011A8B",
          primaryDark: "#00156f",
          accent: "#3B82F6",
          background: "#F7F8FC",
        },

        /* STATUS COLORS (attendance / notifications) */
        status: {
          present: "#22C55E",
          absent: "#EF4444",
          break: "#F59E0B",
          late: "#FB923C",
          info: "#6366F1",
          danger: "#DC2626",
        },
      },

      /* SHADOWS */
      boxShadow: {
        card: "0 4px 16px rgba(0, 0, 0, 0.06)",
      },

      /* BORDER RADIUS (enterprise feel) */
      borderRadius: {
        xl: "1rem",
        "2xl": "1.25rem",
        "3xl": "1.5rem",
      },
    },
  },
  plugins: [],
};
