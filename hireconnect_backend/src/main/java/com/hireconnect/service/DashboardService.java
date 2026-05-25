package com.hireconnect.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hireconnect.entity.Leave;
import com.hireconnect.entity.User;
import com.hireconnect.repository.AttendanceSessionRepository;
import com.hireconnect.repository.LeaveRepository;
import com.hireconnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final LeaveRepository leaveRepository;

    /* ---------------- DATE HELPERS ---------------- */

    private LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay(); // exclusive
    }

    /* ---------------- EMPLOYEE DASHBOARD ---------------- */

    public Map<String, Object> getEmployeeDashboard(Long employeeId) {

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        long pendingLeaves = leaveRepository.findByUserId(employeeId)
                .stream()
                .filter(l -> l.getStatus() == Leave.LeaveStatus.PENDING)
                .count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("employee", employee);
        dashboard.put("pendingLeaves", pendingLeaves);

        return dashboard;
    }

    /* ---------------- ADMIN DASHBOARD ---------------- */

    public Map<String, Object> getAdminDashboard() {

        LocalDateTime todayStart = startOfDay(LocalDate.now());
        LocalDateTime todayEnd = todayStart.plusDays(1);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put(
                "totalEmployees",
                userRepository.countByRole(User.Role.EMPLOYEE)
        );
        dashboard.put(
                "totalAdmins",
                userRepository.countByRole(User.Role.ADMIN)
        );
        dashboard.put(
                "presentToday",
                attendanceSessionRepository.countPresentToday(todayStart, todayEnd)
        );

        return dashboard;
    }

    /* ---------------- DASHBOARD STATS ---------------- */

    public Map<String, Object> getDashboardStats() {

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put(
                "totalEmployees",
                userRepository.countByRole(User.Role.EMPLOYEE)
        );

        return stats;
    }

    /* ---------------- UPCOMING EVENTS ---------------- */

    public List<Map<String, Object>> getUpcomingEvents(int days) {

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        List<Map<String, Object>> events = new ArrayList<>();

        /* 🎂 Birthdays */
        List<User> birthdays =
                userRepository.findUpcomingBirthdays(today, endDate);

        for (User u : birthdays) {
            if (u.getDob() == null) continue;

            Map<String, Object> event = new HashMap<>();
            event.put("type", "BIRTHDAY");
            event.put("name", u.getFullName());
            event.put("department", u.getDepartment());
            event.put("date", u.getDob());
            event.put(
                    "isToday",
                    u.getDob().getDayOfMonth() == today.getDayOfMonth()
                            && u.getDob().getMonth() == today.getMonth()
            );
            events.add(event);
        }

        /* 🎉 Work Anniversaries */
        List<User> anniversaries =
                userRepository.findUpcomingAnniversaries(today, endDate);

        for (User u : anniversaries) {
            if (u.getJoiningDate() == null) continue;

            Map<String, Object> event = new HashMap<>();
            event.put("type", "ANNIVERSARY");
            event.put("name", u.getFullName());
            event.put("department", u.getDepartment());
            event.put("date", u.getJoiningDate());
            event.put(
                    "yearsCompleted",
                    Period.between(u.getJoiningDate(), today).getYears()
            );
            events.add(event);
        }

        return events;
    }
}

