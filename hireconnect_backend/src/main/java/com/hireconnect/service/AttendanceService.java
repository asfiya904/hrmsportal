package com.hireconnect.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import com.hireconnect.dto.request.AttendanceCorrectionRequest;
import com.hireconnect.dto.request.LeaveRequest;
import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.dto.response.AttendanceResponse;
import com.hireconnect.entity.AttendanceSession;
import com.hireconnect.entity.AttendanceSession.AttendanceStatus;
import com.hireconnect.exception.BusinessException;
import com.hireconnect.repository.AttendanceSessionRepository;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceSessionRepository repo;

    public AttendanceService(AttendanceSessionRepository repo) {
        this.repo = repo;
    }

    /* ================= INTERNAL HELPERS ================= */

    private AttendanceSession getActiveSession(Long employeeId) {
        return repo.findActiveSessionForUpdate(employeeId)
        		.orElseThrow(() -> new BusinessException("No active attendance session found"));

    }

    private AttendanceResponse buildResponse(AttendanceSession session) {
        AttendanceResponse response = new AttendanceResponse();
        response.setId(session.getId());
        response.setStatus(session.getStatus().name().toLowerCase());
        response.setShiftStartTime(session.getStartTime());
        response.setShiftEndTime(session.getEndTime());
        response.setTotalSeconds(session.getTotalSeconds());
        response.setTotalBreakSeconds(session.getTotalBreakSeconds());
        response.setInternalWorkSeconds(session.getInternalWorkSeconds());
        response.setCurrentBreakStart(session.getLastBreakStart());
        response.setTimesheetSubmitted(false); // extend later
        return response;
    }

    /* ================= CORE PUNCH LOGIC ================= */

    public AttendanceSession startWork(Long employeeId) {

        if (repo.findActiveSessionForUpdate(employeeId).isPresent()) {
            throw new BusinessException("Attendance already started for today");
        }

        AttendanceSession session = new AttendanceSession(employeeId);
        session.setStartTime(LocalDateTime.now());
        session.setStatus(AttendanceStatus.WORKING);

        return repo.save(session);
    }

    public void startBreak(Long employeeId) {
        AttendanceSession session = getActiveSession(employeeId);

        if (session.getStatus() != AttendanceStatus.WORKING) {
            throw new BusinessException("Cannot start break in current state");
        }

        session.setLastBreakStart(LocalDateTime.now());
        session.setStatus(AttendanceStatus.ON_BREAK);
    }

    public void resumeBreak(Long employeeId) {
        AttendanceSession session = getActiveSession(employeeId);

        if (session.getStatus() != AttendanceStatus.ON_BREAK) {
            throw new BusinessException("Employee is not on break");
        }

        if (session.getLastBreakStart() == null) {
            throw new BusinessException("Break start time missing");
        }

        long breakSeconds = Duration
                .between(session.getLastBreakStart(), LocalDateTime.now())
                .getSeconds();

        session.setTotalBreakSeconds(
                session.getTotalBreakSeconds() + (int) breakSeconds
        );

        session.setLastBreakStart(null);
        session.setStatus(AttendanceStatus.WORKING);
    }

    public AttendanceSession endWork(Long employeeId) {
        AttendanceSession session = getActiveSession(employeeId);
        LocalDateTime now = LocalDateTime.now();

        if (session.getStatus() == AttendanceStatus.ON_BREAK
                && session.getLastBreakStart() != null) {

            long breakSeconds = Duration
                    .between(session.getLastBreakStart(), now)
                    .getSeconds();

            session.setTotalBreakSeconds(
                    session.getTotalBreakSeconds() + (int) breakSeconds
            );

            session.setLastBreakStart(null);
        }

        session.setEndTime(now);

        long totalSeconds = Duration
                .between(session.getStartTime(), now)
                .getSeconds();

        session.setTotalSeconds((int) totalSeconds);

        int internalSeconds =
                (int) totalSeconds - session.getTotalBreakSeconds();

        internalSeconds = Math.max(internalSeconds, 0);
        session.setInternalWorkSeconds(internalSeconds);

        if (internalSeconds >= 8 * 3600) {
            session.setStatus(AttendanceStatus.COMPLETED);
        } else if (internalSeconds >= 5 * 3600) {
            session.setStatus(AttendanceStatus.HALF_DAY);
        } else if (internalSeconds > 0) {
            session.setStatus(AttendanceStatus.PARTIAL);
        } else {
            session.setStatus(AttendanceStatus.ABSENT);
        }

        return session;
    }

    /* ================= CONTROLLER WRAPPERS ================= */

    public AttendanceResponse startWorkAndBuildResponse(Long employeeId) {
        return buildResponse(startWork(employeeId));
    }

    public AttendanceResponse startBreakAndBuildResponse(Long employeeId) {
        startBreak(employeeId);
        return buildResponse(getActiveSession(employeeId));
    }

    public AttendanceResponse resumeBreakAndBuildResponse(Long employeeId) {
        resumeBreak(employeeId);
        return buildResponse(getActiveSession(employeeId));
    }

    public AttendanceResponse endWorkAndBuildResponse(Long employeeId) {
        return buildResponse(endWork(employeeId));
    }

    /* ================= READ APIs ================= */

    public AttendanceResponse getTodayAttendance(Long employeeId) {

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        return repo.findTodayLatestSession(employeeId, start, end)
                .map(this::buildResponse)
                .orElseGet(() -> {
                    AttendanceResponse r = new AttendanceResponse();
                    r.setStatus("notstarted");
                    return r;
                });

    }

    public Map<String, String> getCalendarData(Long employeeId, int year, int month) {

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
       
        Map<String, String> data = repo
                .findByEmployeeIdAndDateRange(employeeId, start, end)
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getStartTime().toLocalDate().toString(),
                        s -> s.getStatus().name(),
                        (a, b) -> a
                ));

        int daysInMonth = start.toLocalDate().lengthOfMonth();
        LocalDate today = LocalDate.now();


        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = start.toLocalDate().withDayOfMonth(day);
            String key = date.toString();

            if (!data.containsKey(key)) {
                if (date.isAfter(today)) {
                    data.put(key, "FUTURE");
                } else {
                    data.put(key, "ABSENT");
                }
            }
        }

        return data;
    }


    public Map<String, Object> getMonthlyAttendance(Long employeeId, int year, int month) {

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        List<AttendanceSession> sessions =
                repo.findByEmployeeIdAndDateRange(employeeId, start, end);

        long presentDays = sessions.stream()
                .filter(s -> s.getStatus() != AttendanceStatus.ABSENT)
                .count();

        return Map.of(
                "totalDays", sessions.size(),
                "presentDays", presentDays,
                "sessions", sessions
        );
    }

    public Map<String, Object> getAttendanceSummary(Long employeeId) {
        return Map.of(
                "employeeId", employeeId,
                "message", "Summary logic ready"
        );
    }

    /* ================= STUBS (EXTEND LATER) ================= */

    public void saveTimesheet(Long employeeId) {

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        AttendanceSession session = repo.findTodayLatestSession(employeeId, start, end)
                .orElseThrow(() ->
                        new BusinessException("No attendance session found for today"));

        if (session.getEndTime() == null) {
            throw new BusinessException("Cannot submit timesheet before ending work");
        }

        // Future:
        // session.setTimesheetSubmitted(true);
    }


    public void submitLeaveRequest(LeaveRequest request) {

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BusinessException("Leave start and end dates are required");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("Leave start date cannot be after end date");
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new BusinessException("Leave reason is required");
        }

        // Production HRMS:
        // 1. Persist leave request in leave_requests table
        // 2. Mark related attendance as LEAVE after approval
        // 3. Trigger notification to manager

        // Stub ends here intentionally
    }


    @PostMapping("/attendance/correction-request")
    public ResponseEntity<ApiResponse<String>> submitCorrectionRequest(
            @Valid @RequestBody AttendanceCorrectionRequest request
    ) {
        attendanceService.submitCorrectionRequest(request);
        return ResponseEntity.ok(
            ApiResponse.success("Correction request submitted successfully")
        );
    }



    public List<AttendanceSession> getAttendanceHistory(
            Long employeeId, LocalDateTime start, LocalDateTime end) {

        return repo.findByEmployeeIdAndDateRange(employeeId, start, end);
    }

}
