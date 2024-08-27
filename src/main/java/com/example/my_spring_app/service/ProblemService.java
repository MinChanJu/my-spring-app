package com.example.my_spring_app.service;

import com.example.my_spring_app.model.Example;
import com.example.my_spring_app.model.Problem;
import com.example.my_spring_app.repository.ExampleRepository;
import com.example.my_spring_app.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProblemService {
    
    @Autowired
    private ProblemRepository problemRepository;
    @Autowired
    private ExampleRepository exampleRepository;
    @Autowired
    private ExampleService exampleService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Problem> getProblemById(Long id) {
        return problemRepository.findById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Problem createProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Problem updateProblem(Long id, Problem problemDetails) {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new RuntimeException("Problem not found"));

        problem.setContestName(problemDetails.getContestName());
        problem.setProblemName(problemDetails.getProblemName());

        return problemRepository.save(problem);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new RuntimeException("Problem not found"));
        problemRepository.delete(problem);
        List<Example> examples = exampleRepository.findByProblemId(id.intValue());
        for (Example example : examples) {
            exampleService.deleteExample(example.getId());
        }
    }
}
