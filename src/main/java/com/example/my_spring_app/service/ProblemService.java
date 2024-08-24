package com.example.my_spring_app.service;

import com.example.my_spring_app.model.Problem;
import com.example.my_spring_app.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProblemService {
    
    @Autowired
    private ProblemRepository problemRepository;

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Optional<Problem> getProblemById(Long id) {
        return problemRepository.findById(id);
    }

    public List<Problem> getProblemsByContestId(Integer contestId) {
        return problemRepository.findByContestId(contestId);
    }

    public Problem createProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    public Problem updateProblem(Long id, Problem problemDetails) {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new RuntimeException("Problem not found"));

        problem.setContestName(problemDetails.getContestName());
        problem.setProblemName(problemDetails.getProblemName());

        return problemRepository.save(problem);
    }

    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new RuntimeException("Problem not found"));
        problemRepository.delete(problem);
    }
}
