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

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.lang.reflect.Method;

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
                result = javaCompile(code, problem.getProblemExampleInput(), problem.getProblemExampleOutput());
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
                    result = javaCompile(code, example.getExampleInput(), example.getExampleOutput());
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

    // javaCompile
    public String javaCompile(String code, String exampleInput, String exampleOutput) throws Exception {
        String result;
        String[] expectedOutput = exampleOutput.split("\n");

        // 메모리 내에서 Java 소스 파일을 작성합니다.
        JavaFileObject javaFile = new InMemoryJavaFileObject("Main", code);

        // Java 컴파일러 API를 사용하여 컴파일
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(diagnostics, null, null);

        // 메모리에서 컴파일된 클래스 파일을 저장할 Map
        Map<String, ByteArrayOutputStream> classFiles = new HashMap<>();

        // In-memory file manager를 사용하여 컴파일된 결과를 메모리에 저장
        JavaFileManager fileManager = new ForwardingJavaFileManager<>(stdFileManager) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                classFiles.put(className, baos);
                return new SimpleJavaFileObject(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind) {
                    @Override
                    public OutputStream openOutputStream() {
                        return baos;
                    }
                };
            }
        };

        // 컴파일 작업을 설정하고 실행
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, Arrays.asList(javaFile));
        boolean success = task.call();

        if (success) {
            try {
                // 컴파일된 클래스를 로드하고 실행
                InMemoryClassLoader classLoader = new InMemoryClassLoader(classFiles);
                Class<?> cls = classLoader.loadClass("Main");
                Method mainMethod = cls.getDeclaredMethod("main", String[].class);

                // 실행 결과를 캡처하기 위해 PrintStream을 사용
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(outputStream);
                PrintStream oldOut = System.out;
                System.setOut(ps);

                // System.in을 가짜 InputStream으로 설정하여 Scanner 입력 시뮬레이션
                InputStream oldIn = System.in;
                InputStream inputStream = new ByteArrayInputStream(exampleInput.getBytes());
                System.setIn(inputStream);

                // 메인 메서드 실행
                mainMethod.invoke(null, (Object) new String[]{});

                // 실행 후 원래 System.in 및 System.out 복구
                System.setOut(oldOut);
                System.setIn(oldIn);

                // 출력 결과를 비교
                String output = outputStream.toString().trim();
                String[] actualOutput = output.split("\n");

                boolean matches = true;
                int i = 0;
                while (i < actualOutput.length && i < expectedOutput.length) {
                    if (!actualOutput[i].trim().equals(expectedOutput[i].trim())) {
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
            } catch (Exception e) {
                result = "process run fail: " + e.getMessage();
            }
        } else {
            // 컴파일 오류 수집
            StringBuilder errorMsg = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                errorMsg.append(diagnostic.getMessage(null)).append("\n");
            }
            result = "compile fail: " + errorMsg.toString();
        }

        return result;
    }

    // 메모리 내에서 Java 소스를 나타내는 클래스
    static class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private final String code;

        public InMemoryJavaFileObject(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    // 메모리 내에서 컴파일된 클래스를 로드하는 클래스 로더
    static class InMemoryClassLoader extends ClassLoader {
        private final Map<String, ByteArrayOutputStream> classFiles;

        public InMemoryClassLoader(Map<String, ByteArrayOutputStream> classFiles) {
            this.classFiles = classFiles;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            ByteArrayOutputStream baos = classFiles.get(name);
            if (baos == null) {
                return super.findClass(name);
            }
            byte[] bytes = baos.toByteArray();
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
