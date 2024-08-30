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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
                if (result.equals("success")) {
                    count++;
                } else if (!result.equals("fail")) {
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace(); // 예외 발생 시 스택 트레이스를 출력하여 문제를 진단
                return "서버 에러: " + e.getMessage();
            }
            for (Example example : examples) {
                try {
                    result = JavaCompile(code, example.getExampleInput(), example.getExampleOutput());
                    re += result;
                    if (result.equals("success")) {
                        count++;
                    } else if (!result.equals("fail")) {
                        return result;
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
        String[] expectedOutput = exampleOutput.split("\n");
        System.out.println(exampleInput);
        System.out.println(exampleOutput);

        // data 폴더에 있는 Main.java 파일의 경로를 지정합니다.
        Path filePath = Paths.get("src/main/java/com/example/my_spring_app/data/Main.java");
        try {
            // 파일 내용을 완전히 지우고 새 내용으로 덮어씁니다.
            Files.write(filePath, ("package com.example.my_spring_app.data;\n\n"+code).getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            System.out.println("file write success.");
        } catch (IOException e) {
            System.out.println("file write fail: " + e.getMessage());
            return "파일 작성 오류";
        }

        // Java 컴파일러 API를 사용하여 컴파일
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        int compile = compiler.run(null, null, new PrintStream(errStream), filePath.toString());

        if (compile == 0) {
            try {
                // 컴파일된 클래스를 실행
                ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", "src/main/java", "com.example.my_spring_app.data.Main");
                Process process = processBuilder.start();

                // 프로세스에 입력 값 전달
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                writer.write(exampleInput + "\n");
                writer.flush();

                // 프로세스의 출력을 읽어오기
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                int i = 0;
                String line;
                boolean matches = true;

                // 시간 제한 설정 (5초)
                long timeout = 5;
                if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                    result = "time out";
                    process.destroy(); // 프로세스 강제 종료
                } else {
                    // 정상적으로 종료된 경우에만 결과를 확인
                    while ((line = reader.readLine()) != null && i < expectedOutput.length) {
                        if (!line.equals(expectedOutput[i])) {
                            matches = false;
                            break;
                        }
                        i++;
                    }

                    if (matches && i == expectedOutput.length) {
                        result = "success";
                    } else {
                        result = "fail";
                    }
                }
            } catch (IOException | InterruptedException e) {
                result = "process run fail: " + e.getMessage();
            }

        } else {
            result = "compile fail: " + errStream.toString();
        }

        return result;
    }
}
