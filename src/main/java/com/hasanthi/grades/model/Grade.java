package com.hasanthi.grades.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "grades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String subject;

    @DecimalMin("0.0") @DecimalMax("100.0")
    @Column(nullable = false)
    private Double marks;

    private String grade;   // A, B, C, D, F — computed on save

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @PrePersist
    @PreUpdate
    public void computeGrade() {
        if (marks >= 90)      this.grade = "A+";
        else if (marks >= 80) this.grade = "A";
        else if (marks >= 70) this.grade = "B";
        else if (marks >= 60) this.grade = "C";
        else if (marks >= 50) this.grade = "D";
        else                  this.grade = "F";
    }
}
