package com.example.my_spring_app.repository;

import com.example.my_spring_app.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    // contestId로 문제들을 조회
    List<Problem> findByContestId(Integer contestId);
}
