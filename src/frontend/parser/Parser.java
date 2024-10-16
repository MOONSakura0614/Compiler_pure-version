package frontend.parser;

import errors.CompileError;
import frontend.lexer.LexIterator;
import frontend.parser.syntaxUnit.CompUnit;
import utils.IOUtils;

/**
 * @author 郑悦
 * @Description: 语法分析
 * @date 2024/10/11 9:04
 */
public class Parser {
    private CompUnit compUnit; // AST-后序遍历（终结符再依次往上，无需实时输出）
    public static Boolean isSyntaxCorrect;
    // 在每个syntaxUnit解析的错误处理的时候就给它赋值了？
    public static LexIterator lexIterator = LexIterator.getInstance();
//    public static
    private static Parser parser;

    private Parser() {
        compUnit = new CompUnit();
        isSyntaxCorrect = Boolean.TRUE;
        lexIterator = LexIterator.getInstance();
    }

    public static Parser getInstance() {
        if (parser == null) {
            parser = new Parser();
        }

        return parser;
    }

    public void parse() {
        if (compUnit != null) {
            compUnit.unitParser();
        }
    }

    public void printSyntaxResult() {
        if (isSyntaxCorrect) {
            // 输出tokenList
            if (compUnit != null)
                compUnit.print();
        } else {
            IOUtils.writeError();
        }
    }

    public static void setError(CompileError error) {
        IOUtils.compileErrors.add(error);
        isSyntaxCorrect = Boolean.FALSE;
    }

    public static void main(String[] args) {
        Parser parser = Parser.getInstance();
        // 测试输出语法树的各节点
    }
}
