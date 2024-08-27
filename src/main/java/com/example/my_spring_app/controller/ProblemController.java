package com.example.my_spring_app.controller;

import com.example.my_spring_app.model.Example;
import com.example.my_spring_app.model.ExampleDTO;
import com.example.my_spring_app.model.Problem;
import com.example.my_spring_app.model.ProblemDTO;
import com.example.my_spring_app.service.ExampleService;
import com.example.my_spring_app.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemService problemService;
    @Autowired
    private ExampleService exampleService;

    @PostMapping
    public List<Problem> getAllProblems() {
        return problemService.getAllProblems();
    }

    @PostMapping("/{id}")
    public ResponseEntity<Problem> getProblemById(@PathVariable Long id) {
        return problemService.getProblemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public Problem createProblem(@RequestBody ProblemDTO problemDTO) {
        Problem problem = new Problem();
        problem.setContestId(problemDTO.getContestId());
        problem.setContestName(problemDTO.getContestName());
        problem.setUserId(problemDTO.getUserId());
        problem.setProblemName(problemDTO.getProblemName());
        problem.setProblemDescription(problemDTO.getProblemDescription());
        problem.setProblemInputDescription(problemDTO.getProblemInputDescription());
        problem.setProblemOutputDescription(problemDTO.getProblemOutputDescription());
        problem.setProblemExampleInput(problemDTO.getProblemExampleInput());
        problem.setProblemExampleOutput(problemDTO.getProblemExampleOutput());

        Problem curProblem = problemService.createProblem(problem);

        for (ExampleDTO exampleDTO : problemDTO.getExamples()) {
            Example example = new Example();
            example.setExampleInput(exampleDTO.getExampleInput());
            example.setExampleOutput(exampleDTO.getExampleOutput());
            example.setProblemId(curProblem.getId().intValue());
            exampleService.createExample(example);
        }

        return curProblem;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Problem> updateProblem(@PathVariable Long id, @RequestBody Problem problemDetails) {
        return ResponseEntity.ok(problemService.updateProblem(id, problemDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.noContent().build();
    }
}
