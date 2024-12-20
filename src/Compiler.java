import config.CompilerConfig;
import frontend.lexer.Lexer;
import frontend.parser.Parser;
import frontend.visitor.Visitor;
import llvm.IRGenerator;
import utils.IOUtils;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/9/20 17:14
 */
public class Compiler {

    public static Lexer lexer = Lexer.getInstance();
    public static Parser parser = Parser.getInstance();
    public static Visitor visitor = Visitor.getInstance();
    public static IRGenerator irGenerator = IRGenerator.getInstance();

//    public static Boolean llvm_ir_gen = Boolean.FALSE;

    public static void main(String[] args) {
        IOUtils.fileInit();

        CompilerConfig.isLexer = false;
        lexer.lexicalAnalysis();

        // Syntax
        CompilerConfig.isLexer = false;
        CompilerConfig.isParser = true;

        parser.parse();
//        parser.printSyntaxResult();

        // Semantic
        visitor.visitAst(); // 正常语义分析+错误处理
//        visitor.printSymbolTables();

        // 错误处理：保证进入LLVM IR生成阶段没错
        if (!(Visitor.isSemanticCorrect && Parser.isSyntaxCorrect && Lexer.getInstance().getIsLexicalCorrect())) {
            IOUtils.writeError();
            return;
        }

        // LLVM IR生成阶段新建一个中间代码的符号表
//        llvm_ir_gen = Boolean.TRUE;
        IRGenerator.setLlvm_ir_gen(Boolean.TRUE);
        // 服务于AST符号表的insertSym和IR过程中的符号表的区分操作
        irGenerator.generateIR();
        irGenerator.getIrModule().printIR();
    }
}
