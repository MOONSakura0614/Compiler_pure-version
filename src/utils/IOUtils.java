package utils;

import errors.CompileError;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * @author 郑悦
 * @Description: 这个方法往下传递的是一整个文件每行经管\n拼接的超长字符串
 *               我的疑惑在于，不会有相关长度限制吗
 * @date 2024/9/24 22:11
 */
public class IOUtils {
    public static ArrayList<CompileError> compileErrors = new ArrayList<>();
    private static final String input = "testfile.txt";
    private static final String output = "output.txt";
    private static final String error = "error.txt";
    public static final String ir = "llvm_ir.txt";

    public static String read(String filename) throws IOException {
        InputStream inputStream = new BufferedInputStream(Files.newInputStream(Paths.get(filename)));
        Scanner scanner = new Scanner(inputStream);
        StringJoiner stringJoiner = new StringJoiner("\n");
        while (scanner.hasNextLine()) {
            stringJoiner.add(scanner.nextLine());
        }
        scanner.close();
        inputStream.close();
        return stringJoiner.toString();
    }

    public static void write(String filename, String content) throws IOException {
        File file = new File(filename);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(content);
    }

    public static void fileInit() {
        // 把输出文件中原本的内容清空
        clearFile("parser.txt");
        clearFile("error.txt");
        clearFile("symbol.txt");
        clearFile(ir);
    }

    // 由于懒得之后de外部IO链接库的bug：这里执行干脆都加上的政策
    public static void initIROutput() {
        writeLLVMIR("declare i32 @getint()\n" +
                "declare i32 @getchar()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)\n" +
                "declare void @putstr(i8*)\n");
    }

    public static void clearFile(String filePath) {
        File file = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // 文件内容将被清空，因为 FileOutputStream 在写入时会截断文件
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 实现一次write一行的函数，记得及时关闭文件流
    public static void writeCorrectLine(String content) {
        try {
            try (FileWriter fileWriter = new FileWriter("parser.txt", true)) {
                fileWriter.write(content); // 直接添加换行符
                fileWriter.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeSymbol(String content) {
        try {
            try (FileWriter fileWriter = new FileWriter("symbol.txt", true)) {
                fileWriter.write(content); // 直接添加换行符
                fileWriter.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeLLVMIR(String content) {
        try (FileWriter fileWriter = new FileWriter(ir, true)) {
                fileWriter.write(content); // 直接添加换行符
                fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeError() {
        // 错误是按照行号从小到大输出
        Collections.sort(compileErrors);
        System.out.println(compileErrors);

        compileErrors = clearifyError(compileErrors);
        System.out.println(compileErrors);

        try {
            FileWriter fileWriter = new FileWriter("error.txt",true);
            if (!compileErrors.isEmpty()) {
                for (CompileError error: compileErrors) {
                    System.out.println(error);
                    fileWriter.write(error.toString());
                }
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<CompileError> clearifyError(ArrayList<CompileError> compileErrors) {
        ArrayList<CompileError> errors = new ArrayList<>();
        if (compileErrors.isEmpty()) {
            return errors;
        }
        CompileError tmp = compileErrors.get(0);
        errors.add(tmp);
        for (int i = 1; i < compileErrors.size(); i++) {
            if (tmp.getLineNum() != compileErrors.get(i).getLineNum()) {
                tmp = compileErrors.get(i);
                errors.add(tmp);
            }
        }

        return errors;
    }


    public static void main(String[] args) throws IOException {
        String testString = IOUtils.read("D:\\YUE\\JCode\\JavaCompiler\\src\\file\\testfile.txt");
        int lineNum = 0, curPos = 0;
        char c = testString.charAt(curPos);
        int len = testString.length();
        boolean lineStart = true;
        while (curPos < len) {
            if (lineStart) {
                lineStart = false;
                System.out.printf("lineNum %d: ", lineNum);
            }
            c = testString.charAt(curPos++);
            System.out.printf("%c", c);
            if (c == '\n') {
                lineStart = true;
                lineNum++;
            }
        }

        try (FileWriter fileWriter = new FileWriter("testIO.txt", true)) {
            fileWriter.write("file:\n"); // 直接添加换行符
            fileWriter.flush();
        }
        try (FileWriter fileWriter = new FileWriter("testIO.txt", true)) {
            fileWriter.write("append mode ?\n"); // 直接添加换行符
            fileWriter.flush();
        }
    }
}
