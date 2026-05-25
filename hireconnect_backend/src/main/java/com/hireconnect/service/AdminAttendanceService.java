package com.hireconnect.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.dto.request.BulkActionRequest;
import com.hireconnect.dto.request.ManualAttendanceRequest;
import com.hireconnect.dto.response.DashboardStatsResponse;
import com.hireconnect.entity.AttendanceSession;
import com.hireconnect.entity.Break;
import com.hireconnect.entity.CorrectionRequest;
import com.hireconnect.entity.Leave;
import com.hireconnect.entity.Timesheet;
import com.hireconnect.entity.User;
import com.hireconnect.repository.AttendanceSessionRepository;
import com.hireconnect.repository.BreakRepository;
import com.hireconnect.repository.CorrectionRequestRepository;
import com.hireconnect.repository.LeaveRepository;
import com.hireconnect.repository.TimesheetRepository;
import com.hireconnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAttendanceService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final BreakRepository breakRepository;
    private final LeaveRepository leaveRepository;
    private final CorrectionRequestRepository correctionRequestRepository;
    private final UserRepository userRepository;
    private final TimesheetRepository timesheetRepository;

    /* ================= DATE HELPERS ================= */

    private LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }

    /* ================= DASHBOARD ================= */

    public DashboardStatsResponse getDashboardStats() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = startOfDay(today);
        LocalDateTime end = endOfDay(today);

        DashboardStatsResponse stats = new DashboardStatsResponse();

        long totalEmployees = userRepository.countByRole(User.Role.EMPLOYEE);
        long presentToday = attendanceSessionRepository.countPresentToday(start, end);
        long workingNow = attendanceSessionRepository.countByStatusToday(
                AttendanceSession.AttendanceStatus.WORKING, start, end);
        long lateToday = attendanceSessionRepository.countByStatusToday(
                AttendanceSession.AttendanceStatus.LATE, start, end);

        stats.setTotalEmployees(totalEmployees);
        stats.setPresentToday(presentToday);
        stats.setWorkingNow(workingNow);
        stats.setLateToday(lateToday);
        stats.setOnBreak(breakRepository.countActiveBreaks());
        stats.setAbsentToday(totalEmployees - presentToday);

        stats.setPendingRequests(
                leaveRepository.countPendingLeaves()
                        + correctionRequestRepository.countByStatus(
                                CorrectionRequest.CorrectionStatus.PENDING
                        )
        );

        return stats;
    }

    /* ================= LEAVE ================= */

    @Transactional
    public void applyLeave(Long employeeId, LocalDate startDate, LocalDate endDate,
                           String leaveType, String reason) {

        userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {

            boolean exists =
                    !attendanceSessionRepository.findSessionsForDay(
                            employeeId,
                            startOfDay(date),
                            endOfDay(date)
                    ).isEmpty();

            if (!exists) {
                AttendanceSession s = new AttendanceSession();
                s.setEmployeeId(employeeId);
                s.setStartTime(startOfDay(date));
                s.setEndTime(startOfDay(date));
                s.setTotalSeconds(0);
                s.setStatus(AttendanceSession.AttendanceStatus.LEAVE);
                attendanceSessionRepository.save(s);
            }

            date = date.plusDays(1);
        }

        Leave leave = new Leave();
        leave.setUserId(employeeId);
        leave.setLeaveType(leaveType);
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setTotalDays((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);
        leave.setReason(reason);
        leave.setStatus(Leave.LeaveStatus.APPROVED);

        leaveRepository.save(leave);
    }

    @Transactional
    public void applyLeaveAll(LocalDate startDate, LocalDate endDate,
                              String leaveType, String reason) {
        for (User u : userRepository.findByRole(User.Role.EMPLOYEE)) {
            applyLeave(u.getId(), startDate, endDate, leaveType, reason);
        }
    }

    /* ================= FORCE PUNCH ================= */

    @Transactional
    public void forcePunchIn(Long employeeId, LocalDateTime time) {

        userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDateTime punchTime = time != null ? time : LocalDateTime.now();
        LocalDate day = punchTime.toLocalDate();

        boolean exists =
                !attendanceSessionRepository.findSessionsForDay(
                        employeeId,
                        startOfDay(day),
                        endOfDay(day)
                ).isEmpty();

        if (exists) {
            throw new RuntimeException("Attendance already exists for this day");
        }

        AttendanceSession s = new AttendanceSession();
        s.setEmployeeId(employeeId);
        s.setStartTime(punchTime);
        s.setStatus(AttendanceSession.AttendanceStatus.WORKING);

        attendanceSessionRepository.save(s);
    }

    @Transactional
    public void forcePunchOut(Long employeeId, LocalDateTime time) {

        AttendanceSession s = attendanceSessionRepository
                .findActiveSessionByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("No active session"));

        LocalDateTime end = time != null ? time : LocalDateTime.now();
        s.setEndTime(end);
        s.setTotalSeconds((int) ChronoUnit.SECONDS.between(s.getStartTime(), end));
        s.setStatus(AttendanceSession.AttendanceStatus.COMPLETED);

        attendanceSessionRepository.save(s);
    }

    /* ================= BULK & MANUAL ATTENDANCE ================= */

    @Transactional
    public void applyBulkActionForUser(BulkActionRequest request) {

        AttendanceSession.AttendanceStatus status =
                AttendanceSession.AttendanceStatus.valueOf(
                        request.getStatus().toUpperCase()
                );

        LocalDate date = request.getStartDate();

        while (!date.isAfter(request.getEndDate())) {

            attendanceSessionRepository.deleteAll(
                    attendanceSessionRepository.findSessionsForDay(
                            request.getEmployeeId(),
                            startOfDay(date),
                            endOfDay(date)
                    )
            );

            AttendanceSession s = new AttendanceSession();
            s.setEmployeeId(request.getEmployeeId());
            s.setStartTime(startOfDay(date));
            s.setEndTime(startOfDay(date));
            s.setTotalSeconds(0);
            s.setStatus(status);

            attendanceSessionRepository.save(s);
            date = date.plusDays(1);
        }
    }

    
    @Transactional
    public void applyBulkActionForUser(
            Long employeeId,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            String reason) {

        BulkActionRequest request = new BulkActionRequest();
        request.setEmployeeId(employeeId);
        request.setStatus(status);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setReason(reason);

        applyBulkActionForUser(request);
    }

    
    @Transactional
    public void applyManualAttendance(Long employeeId, ManualAttendanceRequest request) {

        BulkActionRequest bulkRequest = new BulkActionRequest();
        bulkRequest.setEmployeeId(employeeId);
        bulkRequest.setStatus(request.getLeaveType());
        bulkRequest.setStartDate(request.getStartDate());
        bulkRequest.setEndDate(request.getEndDate());
        bulkRequest.setReason(request.getReason());

        applyBulkActionForUser(bulkRequest);
    }


    @Transactional
    public void updateManualAttendance(Long employeeId, ManualAttendanceRequest request) {
        applyManualAttendance(employeeId, request);
    }

    /* ================= LIVE ATTENDANCE ================= */

    public List<Map<String, Object>> getLiveAttendance() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = startOfDay(today);
        LocalDateTime end = endOfDay(today);

        List<AttendanceSession> sessions =
                attendanceSessionRepository.findTodaysSessions(start, end);

        Map<Long, AttendanceSession> latestSessionMap =
                sessions.stream().collect(Collectors.toMap(
                        AttendanceSession::getEmployeeId,
                        s -> s,
                        (a, b) -> a.getStartTime().isAfter(b.getStartTime()) ? a : b
                ));

        List<Map<String, Object>> result = new ArrayList<>();

        for (User u : userRepository.findByRole(User.Role.EMPLOYEE)) {

            Map<String, Object> row = new HashMap<>();
            row.put("employeeId", u.getId());
            row.put("employeeName", u.getFullName());
            row.put("email", u.getEmail());

            AttendanceSession s = latestSessionMap.get(u.getId());

            if (s != null) {
                row.put("status", s.getStatus().name());
                row.put("startTime", s.getStartTime());
                row.put("endTime", s.getEndTime());

                int workSeconds = s.getTotalSeconds() != null
                        ? s.getTotalSeconds()
                        : (int) ChronoUnit.SECONDS.between(
                                s.getStartTime(), LocalDateTime.now());

                row.put("workDuration", format(workSeconds));

                int breakSeconds = breakRepository.findBySessionId(s.getId()).stream()
                        .filter(b -> b.getDurationSeconds() != null)
                        .mapToInt(Break::getDurationSeconds)
                        .sum();

                row.put("breakDuration", format(breakSeconds));
            } else {
                row.put("status", "ABSENT");
                row.put("workDuration", "00:00:00");
                row.put("breakDuration", "00:00:00");
            }

            result.add(row);
        }

        return result;
    }

    /* ================= ADMIN CALENDAR (UPDATED) ================= */

    public Map<String, Object> getCalendar(Long userId, Integer year, Integer month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<AttendanceSession> sessions =
                attendanceSessionRepository.findByEmployeeIdAndDateRange(
                        userId, startOfDay(start), endOfDay(end));

        Map<LocalDate, AttendanceSession> sessionMap =
                sessions.stream().collect(Collectors.toMap(
                        s -> s.getStartTime().toLocalDate(),
                        s -> s,
                        (a, b) -> a
                ));

        List<Map<String, Object>> days = new ArrayList<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {

            Map<String, Object> day = new HashMap<>();
            day.put("date", d.toString());

            if (d.getDayOfWeek() == DayOfWeek.SATURDAY
                    || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
                day.put("status", "WEEKEND");
            }
            else {
                AttendanceSession s = sessionMap.get(d);
                day.put("status", s == null ? "ABSENT" : normalizeStatus(s));
            }

            days.add(day);
        }

        return Map.of(
                "userId", userId,
                "year", year,
                "month", month,
                "days", days
        );
    }

    /* ================= STATUS NORMALIZATION ================= */

    private String normalizeStatus(AttendanceSession s) {
        return switch (s.getStatus()) {
            case COMPLETED, PRESENT -> "PRESENT";
            case LEAVE -> "LEAVE";
            case HALF_DAY -> "HALF_DAY";
            case HOLIDAY -> "HOLIDAY";
            default -> "PRESENT";
        };
    }

    /* ================= REQUESTS ================= */

    public Map<String, Object> getPendingRequests() {
        return Map.of(
                "leaves", leaveRepository.findByStatus(Leave.LeaveStatus.PENDING),
                "corrections", correctionRequestRepository.findByStatus(
                        CorrectionRequest.CorrectionStatus.PENDING
                )
        );
    }

    @Transactional
    public void approveRequest(Long id, String type) {
        if ("correction".equalsIgnoreCase(type)) {
            CorrectionRequest c = correctionRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Correction not found"));
            c.setStatus(CorrectionRequest.CorrectionStatus.APPROVED);
            correctionRequestRepository.save(c);
        }
    }

    @Transactional
    public void rejectRequest(Long id, String type, String reason) {
        if ("correction".equalsIgnoreCase(type)) {
            CorrectionRequest c = correctionRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Correction not found"));
            c.setStatus(CorrectionRequest.CorrectionStatus.REJECTED);
            c.setRemarks(reason);
            correctionRequestRepository.save(c);
        }
    }

    /* ================= MISC ================= */

    public List<Timesheet> getTimesheets() {
        return timesheetRepository.findTop500ByOrderByCreatedAtDesc();
    }
    
    public Map<String, Object> getReports(Integer month, Integer year) {

        LocalDate start = (month != null && year != null)
                ? LocalDate.of(year, month, 1)
                : LocalDate.now().withDayOfMonth(1);

        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Map<String, Object>> reports = new ArrayList<>();

        for (User u : userRepository.findByRole(User.Role.EMPLOYEE)) {

            List<AttendanceSession> sessions =
                    attendanceSessionRepository.findByEmployeeIdAndDateRange(
                            u.getId(),
                            startOfDay(start),
                            endOfDay(end)
                    );

            long presentDays = sessions.stream()
                    .filter(s ->
                            s.getStatus() == AttendanceSession.AttendanceStatus.COMPLETED
                         || s.getStatus() == AttendanceSession.AttendanceStatus.PRESENT
                    )
                    .count();

            int totalSeconds = sessions.stream()
                    .filter(s -> s.getTotalSeconds() != null)
                    .mapToInt(AttendanceSession::getTotalSeconds)
                    .sum();

            Map<String, Object> row = new HashMap<>();
            row.put("employeeId", u.getId());
            row.put("employeeName", u.getFullName());
            row.put("presentDays", presentDays);
            row.put("totalWorkHours",
                    String.format("%.2f", totalSeconds / 3600.0));

            reports.add(row);
        }

        return Map.of(
                "month", start.getMonthValue(),
                "year", start.getYear(),
                "reports", reports
        );
    }


    public List<User> getUsers() {
        return userRepository.findByRole(User.Role.EMPLOYEE);
    }

    /* ================= UTIL ================= */

    private String format(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}

