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

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.concurrent.TimeUnit;

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
            return runCode(code, lang, actualProblem, examples);
        } else {
            return "서버 에러";
        }
    }

    public String runCode(String code, String lang, Problem problem, List<Example> examples) {
        int total = examples.size() + 1;
        int count = 0;

        if (lang.equals("Python")) {
            return "Python 제출";
        } else if (lang.equals("C")) {
            return "C 언어 제출";
        } else if (lang.equals("JAVA")) {
            String re = "";
            String result;
            try {
                result = JavaCompile(code, problem.getProblemExampleInput(), problem.getProblemExampleOutput());
                re += result;
                if (result.equals("성공")) {
                    count++;
                } else if (result.equals("컴파일 실패")) {
                    return "컴파일 오류";
                } else if (result.equals("시간초과")) {
                    return "시간초과";
                }
            } catch (Exception e) {
                e.printStackTrace(); // 예외 발생 시 스택 트레이스를 출력하여 문제를 진단
                return "서버 에러: " + e.getMessage();
            }
            for (Example example : examples) {
                try {
                    result = JavaCompile(code, example.getExampleInput(), example.getExampleOutput());
                    re += result;
                    if (result.equals("성공")) {
                        count++;
                    } else if (result.equals("컴파일 실패")) {
                        return "컴파일 오류";
                    } else if (result.equals("시간초과")) {
                        return "시간초과";
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // 예외 발생 시 스택 트레이스를 출력하여 문제를 진단
                    return "서버 에러: " + e.getMessage();
                }
            }
            return String.format("%.1f", ((double) count / (double) total) * 100) + "  " + re;
        } else {
            return "제출 오류";
        }
    }

    public String JavaCompile(String code, String exampleInput, String exampleOutput) throws Exception {
        String result;
        String[] list = exampleOutput.split("\n");

        // 문자열 코드를 파일로 저장
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = tempDir + "Main.java";
        File javaFile = new File(fileName);
        try (FileWriter fileWriter = new FileWriter(javaFile)) {
            fileWriter.write(code);
        }

        // Java 컴파일러 API를 사용하여 컴파일
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int compile = compiler.run(null, null, null, fileName);

        if (compile == 0) {

            // 컴파일된 클래스를 실행
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", tempDir, "Main");
            Process process = processBuilder.start();

            // 프로세스에 입력 값 전달
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(exampleInput + "\n");
                writer.flush();
            }

            // 프로세스의 출력을 읽어오기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int i = 0;
            String line;
            boolean matches = true;

            // 시간 제한 설정 (5초)
            long timeout = 5;
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                result = "시간초과";
                process.destroy(); // 프로세스 강제 종료
            } else {
                // 정상적으로 종료된 경우에만 결과를 확인
                while ((line = reader.readLine()) != null && i < list.length) {
                    if (!line.equals(list[i])) {
                        matches = false;
                        break;
                    }
                    i++;
                }

                if (matches && i == list.length) {
                    result = "성공";
                } else {
                    result = "실패";
                }
            }

            // 컴파일된 클래스 파일 삭제
            File classFile = new File(tempDir + "Main.class");
            if (classFile.exists()) {
                if (classFile.delete()) {
                    System.out.println("컴파일된 클래스 파일 삭제 성공!");
                } else {
                    System.out.println("컴파일된 클래스 파일 삭제 실패.");
                }
            }

        } else {
            result = "컴파일 실패";
        }

        // 원본 Java 파일 삭제
        if (javaFile.exists()) {
            if (javaFile.delete()) {
                System.out.println("원본 Java 파일 삭제 성공!");
            } else {
                System.out.println("원본 Java 파일 삭제 실패.");
            }
        }

        return result;
    }
}
