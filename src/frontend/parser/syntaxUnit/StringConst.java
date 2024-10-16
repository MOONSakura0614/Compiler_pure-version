package frontend.parser.syntaxUnit;

/**
 * @author 郑悦
 * @Description: 字符串常量，其实就是char[]，char类型的一维数组
 * 
 */
public class StringConst extends SyntaxNode {
     // 这个疑似是不需要输出的终结符
    @Override
    public void unitParser() {

    }

    @Override
    public void print() {

    }

    /*同理integer-const，CharConst，Ident都不输出
    * identifier → identifier-nondigit | identifier identifier-nondigit | identifier digit
    * 整型常量 integer-const → decimal-const | 0
    *   decimal-const → nonzero-digit | decimal-const digit
    *   nonzero-digit 为1至9的数字。
    *
    * CharConst → '\''ASCII'\'' \\这里的 '\'' 表示单引号 '
    * StringConst → '"' {ASCII} '"'*/
}
