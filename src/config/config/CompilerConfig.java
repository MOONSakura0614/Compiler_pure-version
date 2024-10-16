package config.config;

import java.io.IOException;

/**
 * @author 郑悦
 * @Description: 服务于不同状态要求对编译，如是否输出词法分析结果
 * @date 2024/9/24 9:27
 */
public class CompilerConfig {
    public static String file_input = "testfile.txt";
    public static String file_output = "output.txt";
    public static String file_error = "error.txt";

    public static Boolean isLexer = Boolean.FALSE;
    public static Boolean isParser = true;

    public static void init() throws IOException {
        isLexer = false;
        isParser = false;
    }
}
