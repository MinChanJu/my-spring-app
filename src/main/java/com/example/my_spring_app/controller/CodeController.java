package com.example.my_spring_app.controller;

import com.example.my_spring_app.model.CodeDTO;
import com.example.my_spring_app.model.Example;
import com.example.my_spring_app.model.Problem;
import com.example.my_spring_app.service.ExampleService;
import com.example.my_spring_app.service.ProblemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/code")
public class CodeController {

    @Autowired
    private ExampleService exampleService;
    @Autowired
    private ProblemService problemService;

    @PostMapping
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String submitCode(@RequestBody CodeDTO codeDTO) {
        String code = codeDTO.getCode();
        String lang = codeDTO.getLang();
        Integer problemId = codeDTO.getProblemId();
        List<Example> examples = exampleService.getExamplesByProblemId(problemId);
        Optional<Problem> problem = problemService.getProblemById(problemId.longValue());
        if (problem.isPresent()) {
            Problem actualProblem = problem.get();
            return code+lang+examples+actualProblem;
        } else {
            return "서버 에러";
        }
    }
}
