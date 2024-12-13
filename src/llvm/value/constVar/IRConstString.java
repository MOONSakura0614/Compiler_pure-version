package llvm.value.constVar;

import llvm.type.IRType;
import llvm.value.IRValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 郑悦
 * @Description: ""类型，或者{i8,'a',12}等char[]的元素
 * @date 2024/12/10 9:52
 */
public class IRConstString extends IRConst {
    public static int[] convertStrToAscii(String input) {
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            // 检查是否是转义序列的开始
            if (currentChar == '\\' && i + 1 < input.length()) {
                char nextChar = input.charAt(i + 1);
                int asciiValue;

                // 处理各种转义字符
                switch (nextChar) {
                    case 'a':  // 响铃(BEL)
                        asciiValue = 7;
                        break;
                    case 'b':  // 退格(BS)
                        asciiValue = 8;
                        break;
                    case 't':  // 水平制表(HT)
                        asciiValue = 9;
                        break;
                    case 'n':  // 换行(LF)
                        asciiValue = 10;
                        break;
                    case 'v':  // 垂直制表(VT)
                        asciiValue = 11;
                        break;
                    case 'f':  // 换页(FF)
                        asciiValue = 12;
                        break;
                    case '"':  // 双引号
                        asciiValue = 34;
                        break;
                    case '\'': // 单引号
                        asciiValue = 39;
                        break;
                    case '\\': // 反斜杠
                        asciiValue = 92;
                        break;
                    case '0':  // NULL字符
                        asciiValue = 0;
                        break;
                    default:   // 如果不是特殊的转义字符，保留原字符
                        asciiValue = (int) nextChar;
                        break;
                }

                result.add(asciiValue % 128);
                i++; // 跳过转义序列的第二个字符

            } else {
                // 非转义字符直接转换
                result.add((int) currentChar % 128);
            }
        }

        // 添加字符串结束符 \0
        result.add(0);

        // 将List转换为int数组
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    public static int[] convertStrToAscii(String input, int length) {
        int[] result = new int[length];  // 创建指定长度的数组
        int arrayIndex = 0;  // 当前数组填充位置

        // 遍历输入字符串，直到达到字符串末尾或数组已填满
        for (int i = 0; i < input.length() && arrayIndex < length; i++) {
            char currentChar = input.charAt(i);

            // 检查是否是转义序列的开始
            if (currentChar == '\\' && i + 1 < input.length()) {
                char nextChar = input.charAt(i + 1);
                int asciiValue;

                // 处理各种转义字符
                switch (nextChar) {
                    case 'a':  asciiValue = 7;   break;  // 响铃(BEL)
                    case 'b':  asciiValue = 8;   break;  // 退格(BS)
                    case 't':  asciiValue = 9;   break;  // 水平制表(HT)
                    case 'n':  asciiValue = 10;  break;  // 换行(LF)
                    case 'v':  asciiValue = 11;  break;  // 垂直制表(VT)
                    case 'f':  asciiValue = 12;  break;  // 换页(FF)
                    case '"':  asciiValue = 34;  break;  // 双引号
                    case '\'': asciiValue = 39;  break;  // 单引号
                    case '\\': asciiValue = 92;  break;  // 反斜杠
                    case '0':  asciiValue = 0;   break;  // NULL字符
                    default:   asciiValue = (int) nextChar;  break;
                }

                result[arrayIndex++] = asciiValue % 128;
                i++; // 跳过转义序列的第二个字符

            } else {
                // 非转义字符直接转换
                result[arrayIndex++] = ((int) currentChar) % 128;
            }
        }

        // 如果数组还有空间,添加字符串结束符 \0
        if (arrayIndex < length) {
            result[arrayIndex] = 0;
        }

        // 数组剩余部分已经默认为0，不需要额外处理
        return result;
    }

}
