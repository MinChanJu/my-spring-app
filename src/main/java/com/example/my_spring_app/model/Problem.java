package com.example.my_spring_app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "problems", schema = "public")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contest_id", nullable = true)
    private Integer contestId;

    @Column(name = "contest_name", length = 100, nullable = false)
    private String contestName;

    @Column(name = "problem_name", length = 100, nullable = false, unique = true)
    private String problemName;

    @Column(name = "problem_description", columnDefinition = "TEXT")
    private String problemDescription;

    @Column(name = "problem_input_description", columnDefinition = "TEXT", nullable = false)
    private String problemInputDescription;

    @Column(name = "problem_output_description", columnDefinition = "TEXT", nullable = false)
    private String problemOutputDescription;

    @Column(name = "problem_example_input", columnDefinition = "TEXT", nullable = false)
    private String problemExampleInput;

    @Column(name = "problem_example_output", columnDefinition = "TEXT", nullable = false)
    private String problemExampleOutput;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "user_id", columnDefinition = "TEXT")
    private String userId;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getProblemInputDescription() {
        return problemInputDescription;
    }

    public void setProblemInputDescription(String problemInputDescription) {
        this.problemInputDescription = problemInputDescription;
    }

    public String getProblemOutputDescription() {
        return problemOutputDescription;
    }

    public void setProblemOutputDescription(String problemOutputDescription) {
        this.problemOutputDescription = problemOutputDescription;
    }

    public String getProblemExampleInput() {
        return problemExampleInput;
    }

    public void setProblemExampleInput(String problemExampleInput) {
        this.problemExampleInput = problemExampleInput;
    }

    public String getProblemExampleOutput() {
        return problemExampleOutput;
    }

    public void setProblemExampleOutput(String problemExampleOutput) {
        this.problemExampleOutput = problemExampleOutput;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
