package com.example.my_spring_app.service;

import com.example.my_spring_app.model.Example;
import com.example.my_spring_app.repository.ExampleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExampleService {
    
    @Autowired
    private ExampleRepository exampleRepository;

    public List<Example> getAllExamples() {
        return exampleRepository.findAll();
    }

    public List<Example> getExamplesByProblemId(Integer problemId) {
        return exampleRepository.findByProblemId(problemId);
    }

}
