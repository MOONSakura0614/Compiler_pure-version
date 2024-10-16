package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 常量声明
 * ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
 */
public class ConstDecl extends SyntaxNode{
    // 这边SyntaxNode要有成员变量不能只做interface，但是又单继承，继承Decl也不太对的感觉
    // 好在BType和Decl，BlockItem不用输出-->但是是不是要在语法树中体现-->AST是不是node一对多
    private Token const_token;
    BType bType;
    ConstDef constDef; // 至少有一个Ident做常量定义后面
//    ArrayList<ConstDef> constDefList;
    ArrayList<Comma_constDef> comma_constDef_list;
    private Token semicn_token;

    public ConstDecl() {
        super("ConstDecl");
        comma_constDef_list = new ArrayList<>();
        // list的长度为0时，就是花括号重复零次的情况
    }

    @Override
    public void unitParser() { // 进入ConstDecl解析的至少能确定第一个token是const打头
        Token token;
        if (lexIterator.iterator().hasNext()) {
            token = lexIterator.iterator().next();
            if (token.getTokenType() == LexType.CONSTTK) {
                const_token = token;
            } else {
                throw new RuntimeException("ConstDef解析失败: 无法获取const token");
            }
        }
        if (isBType()) {
            bType = new BType();
            bType.unitParser();
        } else {
            throw new RuntimeException("ConstDef解析失败: 无法获取BType");
        }
        if (isConstDef()) {
            constDef = new ConstDef();
            constDef.unitParser();
        } else {
            throw new RuntimeException("ConstDef解析失败: 无法获取ConstDef");
        }
        ConstDef constDef1;
        Comma_constDef comma_constDef;
        while (isComma()) {
            if (lexIterator.iterator().hasNext()) {
                // 先把逗号解析出来
                token = lexIterator.iterator().next(); // 注意进入while的条件，保证token是逗号
                if (isConstDef()) {
                    constDef1 = new ConstDef();
                    constDef1.unitParser();
                } else {
                    throw new RuntimeException("逗号后没有下一个定义的标识符，多余");
                }
                comma_constDef = new Comma_constDef(token, constDef1);
                comma_constDef_list.add(comma_constDef);
            }
        }
        /*while (isVarDef()) {
            // 这里注意，进入是逗号才可以，是分号就退出
        }*/
        if (isSemicn()) {
            /*if (lexIterator.iterator().hasNext()) {
                // 解析分号
                semicn_token = lexIterator.iterator().next();
            }*/
            semicn_token = lexIterator.iterator().next();
        } else {
            // errorHandle:发生缺失分号错误-i
//            Parser.isSyntaxCorrect = Boolean.FALSE;
            // 注意这个报错
            token = lexIterator.nowToken(); // 最近一次解析的token
            CompileError error = new CompileError(token.getLineNum(), ErrorType.LackSemiCN);
            IOUtils.compileErrors.add(error);
            Parser.isSyntaxCorrect = Boolean.FALSE;
        }
    }

    @Override
    public void print() {
        if (const_token != null) {
            IOUtils.writeCorrectLine(const_token.toString());
        }
        if (bType != null) {
            bType.print();
        }
        if (constDef != null) {
            constDef.print();
        }
        if (!comma_constDef_list.isEmpty()) {
            // 由于至少有一个constDef，所以是先打印上面 ，然后看list里还有没有（同理arrayInit）
            for(Comma_constDef comma_constDef: comma_constDef_list) {
                // 终结符需要直接写到文件中，非终结符有自己的print方法（关于规则的详细拆解）
                IOUtils.writeCorrectLine(comma_constDef.comma_token.toString());
                comma_constDef.constDef.print();
            }
        }
        if (semicn_token != null)
            IOUtils.writeCorrectLine(semicn_token.toString());
        IOUtils.writeCorrectLine(this.toString());
    }

    static class Comma_constDef {
        Token comma_token;
        ConstDef constDef;

        Comma_constDef(Token comma, ConstDef constDef) {
            comma_token = comma;
            this.constDef = constDef;
        }
    }
}
