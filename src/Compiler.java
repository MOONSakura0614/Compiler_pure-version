import config.CompilerConfig;
import frontend.lexer.Lexer;
import frontend.parser.Parser;
import frontend.visitor.Visitor;
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
        visitor.visitAst();
        visitor.printSymbolTables();
    }
}
