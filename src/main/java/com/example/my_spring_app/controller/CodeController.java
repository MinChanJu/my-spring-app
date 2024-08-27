package com.example.my_spring_app.controller;

import com.example.my_spring_app.model.CodeDTO;
import com.example.my_spring_app.model.Example;
import com.example.my_spring_app.service.ExampleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/code")
public class CodeController {

    @Autowired
    private ExampleService exampleService;

    @PostMapping
    public String submitCode(@RequestBody CodeDTO codeDTO) {
        String code = codeDTO.getCode();
        String lang = codeDTO.getLang();
        Integer problemId = codeDTO.getProblemId();
        List<Example> examples = exampleService.getExamplesByProblemId(problemId);
        System.out.println(examples);
        return code+lang;
    }
}
