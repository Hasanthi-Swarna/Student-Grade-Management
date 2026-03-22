package com.hasanthi.grades;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasanthi.grades.model.User;
import com.hasanthi.grades.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GradeManagementApplicationTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static String teacherToken;
    private static String studentToken;
    private static Long createdStudentId;

    // ── Auth Tests ────────────────────────────────────

    @Test @Order(1)
    void registerTeacher_shouldReturn200() throws Exception {
        var body = Map.of("name", "Test Teacher", "email", "teacher@test.com",
                "password", "pass123", "role", "TEACHER");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test @Order(2)
    void registerStudent_shouldReturn200() throws Exception {
        var body = Map.of("name", "Test Student", "email", "student@test.com",
                "password", "pass123", "role", "STUDENT");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test @Order(3)
    void loginTeacher_shouldReturnToken() throws Exception {
        var body = Map.of("email", "teacher@test.com", "password", "pass123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        teacherToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test @Order(4)
    void loginStudent_shouldReturnToken() throws Exception {
        var body = Map.of("email", "student@test.com", "password", "pass123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        studentToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    // ── Student CRUD Tests ────────────────────────────

    @Test @Order(5)
    void createStudent_asTeacher_shouldReturn200() throws Exception {
        var body = Map.of("name", "Alice Kumar", "rollNumber", "CS001",
                "department", "Computer Science", "year", 2);
        MvcResult result = mockMvc.perform(post("/api/students")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value("CS001"))
                .andReturn();
        createdStudentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test @Order(6)
    void createStudent_asStudent_shouldReturn403() throws Exception {
        var body = Map.of("name", "Bob", "rollNumber", "CS002",
                "department", "CS", "year", 1);
        mockMvc.perform(post("/api/students")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test @Order(7)
    void getAllStudents_asStudent_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/students")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── Grade Tests ───────────────────────────────────

    @Test @Order(8)
    void addGrade_asTeacher_shouldReturn200() throws Exception {
        var body = Map.of("subject", "Mathematics", "marks", 87.5);
        mockMvc.perform(post("/api/grades/student/" + createdStudentId)
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("A"));
    }

    @Test @Order(9)
    void getGrades_asStudent_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/grades/student/" + createdStudentId)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── Analytics Tests ───────────────────────────────

    @Test @Order(10)
    void getClassAnalytics_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/analytics/class")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classAverage").exists())
                .andExpect(jsonPath("$.topPerformer").exists());
    }

    // ── Unauthenticated Tests ─────────────────────────

    @Test @Order(11)
    void getStudents_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());
    }
}
