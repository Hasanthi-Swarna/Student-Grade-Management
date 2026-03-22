package com.hasanthi.grades.controller;

import com.hasanthi.grades.exception.ResourceNotFoundException;
import com.hasanthi.grades.model.Grade;
import com.hasanthi.grades.model.Student;
import com.hasanthi.grades.repository.GradeRepository;
import com.hasanthi.grades.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Grade management and CSV export")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('TEACHER')")
public class GradeController {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    @Operation(summary = "Get all grades for a student")
    public List<GradeResponse> getByStudent(@PathVariable Long studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(g -> new GradeResponse(g.getId(), g.getSubject(), g.getMarks(), g.getGrade()))
                .collect(Collectors.toList());
    }

    @PostMapping("/student/{studentId}")
    @Operation(summary = "Add grade for a student (TEACHER only)")
    public ResponseEntity<GradeResponse> addGrade(@PathVariable Long studentId,
                                                   @Valid @RequestBody GradeRequest req) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        Grade grade = Grade.builder()
                .subject(req.subject)
                .marks(req.marks)
                .student(student)
                .build();
        Grade saved = gradeRepository.save(grade);
        return ResponseEntity.ok(new GradeResponse(saved.getId(), saved.getSubject(),
                saved.getMarks(), saved.getGrade()));
    }

    @PutMapping("/{gradeId}")
    @Operation(summary = "Update a grade (TEACHER only)")
    public ResponseEntity<GradeResponse> updateGrade(@PathVariable Long gradeId,
                                                      @Valid @RequestBody GradeRequest req) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + gradeId));
        grade.setSubject(req.subject);
        grade.setMarks(req.marks);
        Grade saved = gradeRepository.save(grade);
        return ResponseEntity.ok(new GradeResponse(saved.getId(), saved.getSubject(),
                saved.getMarks(), saved.getGrade()));
    }

    @DeleteMapping("/{gradeId}")
    @Operation(summary = "Delete a grade (TEACHER only)")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long gradeId) {
        gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + gradeId));
        gradeRepository.deleteById(gradeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}/export/csv")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    @Operation(summary = "Export student grades as CSV")
    public void exportCsv(@PathVariable Long studentId, HttpServletResponse response)
            throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=grades_" + student.getRollNumber() + ".csv");

        PrintWriter writer = response.getWriter();
        writer.println("Student Name,Roll Number,Subject,Marks,Grade");
        for (Grade g : gradeRepository.findByStudentId(studentId)) {
            writer.printf("%s,%s,%s,%.2f,%s%n",
                    student.getName(), student.getRollNumber(),
                    g.getSubject(), g.getMarks(), g.getGrade());
        }
        writer.flush();
    }

    // ── Inner DTOs ──────────────────────────────────
    @Getter @Setter
    static class GradeRequest {
        @NotBlank public String subject;
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") public Double marks;
    }

    @Getter @Setter @AllArgsConstructor
    static class GradeResponse {
        public Long id;
        public String subject;
        public Double marks;
        public String grade;
    }
}
