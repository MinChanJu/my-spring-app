package com.example.my_spring_app.controller;

import com.example.my_spring_app.model.Problem;
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

    @PostMapping("/contest/{contestId}")
    public List<Problem> getProblemsByContestId(@PathVariable Integer contestId) {
        return problemService.getProblemsByContestId(contestId);
    }

    // @PostMapping
    // public Problem createProblem(@RequestBody Problem problem) {
    //     return problemService.createProblem(problem);
    // }

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
