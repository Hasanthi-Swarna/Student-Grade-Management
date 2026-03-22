package com.hasanthi.grades.dto;

import com.hasanthi.grades.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

// ── Auth ──────────────────────────────────────────────
public class AuthDtos {

    @Getter @Setter
    public static class RegisterRequest {
        @NotBlank public String name;
        @Email @NotBlank public String email;
        @NotBlank @Size(min = 6) public String password;
        @NotNull public User.Role role;
    }

    @Getter @Setter
    public static class LoginRequest {
        @Email @NotBlank public String email;
        @NotBlank public String password;
    }

    @Getter @Setter @AllArgsConstructor
    public static class AuthResponse {
        public String token;
        public String role;
        public String name;
    }
}

// ── Student ───────────────────────────────────────────
class StudentDtos {

    @Getter @Setter
    public static class StudentRequest {
        @NotBlank public String name;
        @NotBlank public String rollNumber;
        @NotBlank public String department;
        @Min(1) @Max(4) public int year;
    }

    @Getter @Setter @AllArgsConstructor
    public static class StudentResponse {
        public Long id;
        public String name;
        public String rollNumber;
        public String department;
        public int year;
        public Double gpa;
    }
}

// ── Grade ─────────────────────────────────────────────
class GradeDtos {

    @Getter @Setter
    public static class GradeRequest {
        @NotBlank public String subject;
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") public Double marks;
    }

    @Getter @Setter @AllArgsConstructor
    public static class GradeResponse {
        public Long id;
        public String subject;
        public Double marks;
        public String grade;
    }
}

// ── Analytics ─────────────────────────────────────────
class AnalyticsDtos {

    @Getter @Setter @AllArgsConstructor
    public static class ClassAnalytics {
        public double classAverage;
        public double highestScore;
        public double lowestScore;
        public String topPerformer;
        public List<SubjectAverage> subjectAverages;
    }

    @Getter @Setter @AllArgsConstructor
    public static class SubjectAverage {
        public String subject;
        public double average;
    }
}
