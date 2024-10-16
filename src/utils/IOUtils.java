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
    }



    /*public static void clearFile(String filename) {
        File file = new File(filename);

        // 检查文件是否存在
        if (file.exists()) {
            // 删除文件
            boolean isDeleted = file.delete();
            if (!isDeleted) {
                throw new RuntimeException("Failed to delete file: " + filename);
            }
        }

        // 使用 FileWriter 创建一个新文件
        try (FileWriter writer = new FileWriter(file)) {
            // 不需要写入任何内容，文件已经被清空
        } catch (IOException e) {
            throw new RuntimeException("Failed to create new file: " + filename, e);
        }
    }*/


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
//            clearFile("parser.txt"); // 只写一行，肯定不能每次清理，统一在Compiler的开头init即可
            /*
            // 下面这个方法，每次会覆写文件，没法做到追加
            FileWriter fileWriter = new FileWriter("parser.txt");
            fileWriter.write(content);
            // token.getTokenType().toString() + ' ' + token.getTokenValue()+'\n'
            fileWriter.flush();
            fileWriter.close();*/
            // 使用 try-with-resources 语句自动管理 FileWriter 资源
            try (FileWriter fileWriter = new FileWriter("parser.txt", true)) {
                fileWriter.write(content); // 直接添加换行符
                fileWriter.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeError() {
        // 错误是按照行号从小到大输出
        Collections.sort(compileErrors);

        try {
//            clearFile("error.txt"); // 在init中执行
            FileWriter fileWriter = new FileWriter("error.txt",true);
            if (!compileErrors.isEmpty()) {
                for (CompileError error: compileErrors) {
                    fileWriter.write(error.toString());
                }
            }
            // Integer.toString(error.getLineNum()) + ' ' + error.getErrorType().getErrorTypeCode().toString() + '\n'
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        String testString = IOUtils.read("D:\\YUE\\JCode\\JavaCompiler\\src\\file\\testfile.txt");
        int lineNum = 0, curPos = 0;
        char c = testString.charAt(curPos);
        // 是不是长度超过了就是most line， eof了
        int len = testString.length();
        boolean lineStart = true;
//        System.out.println(testString.charAt(len)); //这个会java.lang.StringIndexOutOfBoundsException
//        System.out.println(testString.charAt(len-1));
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
            /*while (c != '\n') {
                c = testString.charAt(curPos++);
//             好像又不用判断，照例一个一个打印即可  ——主要是判断换行和line
                System.out.printf("%c", c);
            }*/
//            lineNum++;
//            System.out.println();
//            curPos++;
        }
        /*while (c != '\0') {
//            System.out.printf("lineNum %d: %c", lineNum, testString.charAt(curPos));
            System.out.printf("lineNum %d: ", lineNum);
            while (c != '\n') {
                System.out.print(c);
                c = testString.charAt(curPos++);
            }
            // 感觉java和c不太一样，是不是越界，没有\0判断的
            if ()
            *//*if (c == '\n') {
                lineNum++;
//                c = testString.charAt(curPos);
            }*//*
//            curPos++;
//            c = testString.charAt(curPos);
        }*/

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
