package com.hasanthi.grades.repository;

import com.hasanthi.grades.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

@Repository
interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    boolean existsByRollNumber(String rollNumber);

    @Query("SELECT s FROM Student s ORDER BY " +
           "(SELECT COALESCE(AVG(g.marks), 0) FROM Grade g WHERE g.student = s) DESC")
    List<Student> findAllOrderByGpaDesc();
}

@Repository
interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);

    @Query("SELECT COALESCE(AVG(g.marks), 0) FROM Grade g WHERE g.student.id = :studentId")
    Double findGpaByStudentId(Long studentId);

    @Query("SELECT g.subject, COALESCE(AVG(g.marks), 0) FROM Grade g GROUP BY g.subject")
    List<Object[]> findAverageBySubject();

    @Query("SELECT COALESCE(AVG(g.marks), 0) FROM Grade g")
    Double findClassAverage();

    @Query("SELECT COALESCE(MAX(g.marks), 0) FROM Grade g")
    Double findHighestScore();

    @Query("SELECT COALESCE(MIN(g.marks), 0) FROM Grade g")
    Double findLowestScore();
}
