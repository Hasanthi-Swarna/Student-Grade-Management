package com.hasanthi.grades.controller;

import com.hasanthi.grades.repository.GradeRepository;
import com.hasanthi.grades.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Class-wide grade analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/class")
    @Operation(summary = "Get class-wide analytics — average, top/bottom performers, subject breakdown")
    public ResponseEntity<ClassAnalytics> getClassAnalytics() {
        double avg     = gradeRepository.findClassAverage();
        double highest = gradeRepository.findHighestScore();
        double lowest  = gradeRepository.findLowestScore();

        String topPerformer = studentRepository.findAllOrderByGpaDesc()
                .stream().findFirst()
                .map(s -> s.getName() + " (" + s.getRollNumber() + ")")
                .orElse("N/A");

        List<SubjectAverage> subjectAverages = gradeRepository.findAverageBySubject()
                .stream()
                .map(row -> new SubjectAverage(
                        (String) row[0],
                        Math.round((Double) row[1] * 100.0) / 100.0))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ClassAnalytics(
                Math.round(avg * 100.0) / 100.0,
                highest,
                lowest,
                topPerformer,
                subjectAverages
        ));
    }

    @GetMapping("/student/{studentId}/gpa")
    @Operation(summary = "Get GPA for a specific student")
    public ResponseEntity<GpaResponse> getStudentGpa(@PathVariable Long studentId) {
        Double gpa = gradeRepository.findGpaByStudentId(studentId);
        return ResponseEntity.ok(new GpaResponse(studentId, gpa != null ?
                Math.round(gpa * 100.0) / 100.0 : 0.0));
    }

    // ── Inner DTOs ──────────────────────────────────
    @Getter @Setter @AllArgsConstructor
    static class ClassAnalytics {
        public double classAverage;
        public double highestScore;
        public double lowestScore;
        public String topPerformer;
        public List<SubjectAverage> subjectAverages;
    }

    @Getter @Setter @AllArgsConstructor
    static class SubjectAverage {
        public String subject;
        public double average;
    }

    @Getter @Setter @AllArgsConstructor
    static class GpaResponse {
        public Long studentId;
        public double gpa;
    }
}
