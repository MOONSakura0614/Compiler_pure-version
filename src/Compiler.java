import frontend.Lexer;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/9/20 17:14
 */
public class Compiler {
    public static void main(String[] args) {
        Lexer lexer = Lexer.getInstance();
        lexer.lexicalAnalysis();
    }
}
