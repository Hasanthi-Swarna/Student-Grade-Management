package com.hasanthi.grades.controller;

import com.hasanthi.grades.exception.ResourceNotFoundException;
import com.hasanthi.grades.model.Student;
import com.hasanthi.grades.repository.GradeRepository;
import com.hasanthi.grades.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student CRUD operations")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;

    @GetMapping
    @Operation(summary = "Get all students (TEACHER + STUDENT)")
    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        return ResponseEntity.ok(toResponse(s));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Add a new student (TEACHER only)")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest req) {
        if (studentRepository.existsByRollNumber(req.rollNumber)) {
            return ResponseEntity.badRequest().build();
        }
        Student s = Student.builder()
                .name(req.name)
                .rollNumber(req.rollNumber)
                .department(req.department)
                .year(req.year)
                .build();
        return ResponseEntity.ok(toResponse(studentRepository.save(s)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update student details (TEACHER only)")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody StudentRequest req) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        s.setName(req.name);
        s.setDepartment(req.department);
        s.setYear(req.year);
        return ResponseEntity.ok(toResponse(studentRepository.save(s)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete student (TEACHER only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        studentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private StudentResponse toResponse(Student s) {
        Double gpa = gradeRepository.findGpaByStudentId(s.getId());
        return new StudentResponse(s.getId(), s.getName(), s.getRollNumber(),
                s.getDepartment(), s.getYear(), gpa != null ? Math.round(gpa * 100.0) / 100.0 : 0.0);
    }

    // ── Inner DTOs ──────────────────────────────────
    @Getter @Setter
    static class StudentRequest {
        @NotBlank public String name;
        @NotBlank public String rollNumber;
        @NotBlank public String department;
        @Min(1) @Max(4) public int year;
    }

    @Getter @Setter @AllArgsConstructor
    static class StudentResponse {
        public Long id;
        public String name;
        public String rollNumber;
        public String department;
        public int year;
        public Double gpa;
    }
}
