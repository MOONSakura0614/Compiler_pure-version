import config.config.CompilerConfig;
import errors.CompileError;
import frontend.lexer.Lexer;
import frontend.parser.Parser;
import utils.IOUtils;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/9/20 17:14
 */
public class Compiler {

    public static Lexer lexer = Lexer.getInstance();
    public static Parser parser = Parser.getInstance();

    public static void main(String[] args) {
        IOUtils.fileInit();

//        CompilerConfig.isLexer = true;
        CompilerConfig.isLexer = false;
//        Lexer lexer = Lexer.getInstance();
        lexer.lexicalAnalysis();

        // Syntax
        CompilerConfig.isLexer = false;
        CompilerConfig.isParser = true;
//        Parser parser = Parser.getInstance();

        parser.parse();
        parser.printSyntaxResult();

        /*if (!(lexer.getIsLexicalCorrect() && Parser.isSyntaxCorrect)) {
            IOUtils.writeError();
        } else {
            parser.
        }*/
    }
}
