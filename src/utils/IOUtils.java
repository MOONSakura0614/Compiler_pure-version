package utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * @author 郑悦
 * @Description: 这个方法往下传递的是一整个文件每行经管\n拼接的超长字符串
 *               我的疑惑在于，不会有相关长度限制吗
 * @date 2024/9/24 22:11
 */
public class IOUtils {
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
    }
}
