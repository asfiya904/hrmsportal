package com.hireconnect.dto.request;

import java.time.LocalDate;

import com.hireconnect.entity.User.EmploymentType;
import com.hireconnect.entity.User.Gender;
import com.hireconnect.entity.User.Role;
import com.hireconnect.entity.User.ShiftType;
import com.hireconnect.entity.User.Status;

import lombok.Data;

@Data
public class AdminCreateEmployeeRequest {

    private String employeeId;
    private String fullName;
    private String email;
    private String password;

    private String mobile;
    private String emergencyContact;

    private String department;
    private String designation;
    private EmploymentType employmentType;
    private String reportingManager;
    private String workLocation;
    private LocalDate joiningDate;
    private ShiftType shiftType;

    private LocalDate dob;
    private Gender gender;

    private Role role;
    private Status status;
}
