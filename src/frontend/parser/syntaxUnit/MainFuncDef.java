package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 主函数（main）定义
 *  MainFuncDef → 'int' 'main' '(' ')' Block
 */
public class MainFuncDef extends SyntaxNode {
    // 按照推导规则，设置独特的成员变量
    Token int_token;
    Token main_token;
    Token leftParent_token;
    Token rightParent_token;
    Block block;

    public MainFuncDef() {
        super("MainFuncDef");
    }

    @Override
    public void unitParser() {
        if (isMainFuncDef()) {
            // 获取int main
            if (lexIterator.iterator().hasNext()) {
                int_token = lexIterator.iterator().next();
            }
            if (lexIterator.iterator().hasNext()) {
                main_token = lexIterator.iterator().next();
            }
            Token token;
            // 判断()：不在isMainFuncDef()的预先判断范围内，需要判断LexType
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.tokenList.get(lexIterator.curPos);
                if (token.getTokenType() == LexType.LPARENT) {
                    leftParent_token = lexIterator.iterator().next();
                } else {
                    throw new RuntimeException("MainFuncDef解析异常：无法找到{");
                }
            }
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.tokenList.get(lexIterator.curPos);
                if (token.getTokenType() == LexType.RPARENT) {
                    rightParent_token = lexIterator.iterator().next();
                } else {
                    CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                    IOUtils.compileErrors.add(error);
                    Parser.isSyntaxCorrect = Boolean.FALSE;
                }
            }
        }
        if (isBlock()) {
            block = new Block();
            block.unitParser();
        } else {
            throw new RuntimeException("MainFuncDef解析异常：无法解析Block");
        }
    }

    @Override
    public void print() {
        if (int_token != null)
            IOUtils.writeCorrectLine(int_token.toString());
        if (main_token != null)
            IOUtils.writeCorrectLine(main_token.toString());
        if (leftParent_token != null)
            IOUtils.writeCorrectLine(leftParent_token.toString());
        if (rightParent_token != null)
            IOUtils.writeCorrectLine(rightParent_token.toString());
        if (block != null)
            block.print();

        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() {
        if (block != null) {
            block.visit(); // 这个函数就会新建符号表了
            block.checkReturn0();
        }
    }

    public Block getBlock() {
        return block;
    }
}
