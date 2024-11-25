package frontend.visitor;

import frontend.lexer.Lexer;
import frontend.parser.Parser;
import frontend.parser.syntaxUnit.CompUnit;
import frontend.parser.syntaxUnit.SyntaxNode;
import frontend.symbol.SymbolTable;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 郑悦
 * @Description: 语义分析过程：构建符号表的遍历器
 * @date 2024/10/16 22:27
 */
public class Visitor {
    private static Visitor visitor;
    public static Boolean isSemanticCorrect;
    private CompUnit ast; // 语法树的根节点 compUnit
    private static Parser parser;
    // 作用域序号，与层次无关
    public static int scope; // 纯递增，用于给新建的符号表进行作用域序号赋值
    public static SymbolTable curTable;
    public static int curScope;
    public static List<SymbolTable> symbolTableList;
    // 错误处理：在非循环块中使用 break 和 continue 语句
    public static int loopCount = 0; /*全局维护一个 loopCount，初始为 0，每次进入循环就加一，退出循环就减一*/
    public static Boolean inVoidFunc = Boolean.FALSE;

    static {
        parser = Parser.getInstance();
        scope = 0;
        symbolTableList = new ArrayList<>();
        isSemanticCorrect = Boolean.TRUE;
        // 类变量统一用静态代码初始化好了
    }

    private Visitor() {
        // 语义正确/错误
        scope = 0;
        curScope = 0;
        /*关于作用域序号，即进入该作用域之前进入的作用域数量加1。
        进入全局作用域时进入的作用域数量为0，因此全局作用域序号为1。*/

        if (parser == null) {
            parser = Parser.getInstance();
        }
    }

    public static Visitor getInstance() {
        if (visitor == null) {
            visitor = new Visitor();
        }

        return visitor;
    }

    public void visitAst() {
        if (parser == null)
            parser = Parser.getInstance();
        // 得到ast结果
        ast = parser.getAst();

        // 从CompUnit开始检验
        if (ast != null) {
            ast.visit();
        }
    }

    public void printSymbolTables() {
        if (isSemanticCorrect && Parser.isSyntaxCorrect && Lexer.getInstance().getIsLexicalCorrect()) {
            // 输出tokenList
            for (SymbolTable symbolTable: symbolTableList) {
                if (symbolTable.isSymbolTableEmpty()) {
//                    System.out.println("EMPTY");
                    continue;
                }
                symbolTable.print();
            }
        } else {
            IOUtils.writeError();
        }
    }
}
