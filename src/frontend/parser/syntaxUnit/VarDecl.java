package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import frontend.symbol.SymbolTable;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 变量声明
 * VarDecl → BType VarDef { ',' VarDef } ';'
 */
public class VarDecl extends SyntaxNode {
    private BType bType;
    private VarDef varDef;
    private ArrayList<Comma_VarDef> comma_varDefList;
    private Token semicn_token;

    public VarDecl() {
        super("VarDecl");

        comma_varDefList = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        Token token; // 临时变量
        if (isBType()) {
            bType = new BType();
            bType.unitParser();
            // 在bType的解析中curPos前进
        } else {
            throw new RuntimeException("VarDecl解析失败：非BType");
        }
        if (isVarDef()) {
            varDef = new VarDef();
            varDef.unitParser();
        }
        VarDef varDef1;
        Comma_VarDef comma_varDef;
        while (isComma()) {
            if (lexIterator.iterator().hasNext()) {
                // 先把逗号解析出来
                token = lexIterator.iterator().next();
                if (isVarDef()) {
                    varDef1 = new VarDef();
                    varDef1.unitParser(); // 在unit里解析到下个逗号or分号前面，便于再次进入判断
                } else {
                    throw new RuntimeException("没有下一个定义的标识符，多余逗号");
                }
                comma_varDef = new Comma_VarDef(token, varDef1);
                comma_varDefList.add(comma_varDef);
            }
        }
        if (isSemicn()) {
            if (lexIterator.iterator().hasNext()) {
                // 解析分号
                semicn_token = lexIterator.iterator().next();
            }
        } else {
            // errorHandle:发生缺失分号错误-i
            Parser.isSyntaxCorrect = Boolean.FALSE;
            // 注意这个报错
            token = lexIterator.nowToken(); // 最近一次解析的token
            CompileError error = new CompileError(token.getLineNum(), ErrorType.LackSemiCN);
            IOUtils.compileErrors.add(error);
        }
    }

    @Override
    public void print() {
        if (bType != null) {
            bType.print();
        }
        if (varDef != null) {
            varDef.print();
        }
        if (!comma_varDefList.isEmpty()) {
            // 先打印逗号，再打印VarDef语法成分
            for (Comma_VarDef comma_varDef: comma_varDefList) {
                // 逐个打印
                comma_varDef.print();
            }
        }
        if (semicn_token != null)
            IOUtils.writeCorrectLine(semicn_token.toString());

        IOUtils.writeCorrectLine(this.toString()); // 打印<VarDecl>
    }

    public class Comma_VarDef {
        Token comma_token;
        VarDef varDef;
        public Comma_VarDef(Token comma, VarDef varDef) {
            comma_token = comma;
            this.varDef = varDef;
        }

        public void print() {
            IOUtils.writeCorrectLine(comma_token.toString());
            if (varDef != null) {
                varDef.print();
            }
        }
    }

    @Override
    public void insertSymbol(SymbolTable symbolTable) {
        Boolean isInt = Boolean.TRUE;
        if (bType != null) {
            isInt = bType.getIsInt();
        }
        if (varDef != null) {
            // 添加开头的varDef
            if (isInt)
                varDef.insertSymbol(symbolTable);
            else
                varDef.insertCharSymbol(symbolTable);
        }
        for (Comma_VarDef comma_varDef: comma_varDefList) {
            if (comma_varDef.varDef != null) {
                if (isInt)
                    comma_varDef.varDef.insertSymbol(symbolTable);
                else
                    comma_varDef.varDef.insertCharSymbol(symbolTable);
            }
        }
    }

    public LexType getVarType() {
        if (bType == null)
            return null;

        return bType.basic_type_token.getTokenType();
    }

    public ArrayList<VarDef> getVarDefs() {
        ArrayList<VarDef> varDefs = new ArrayList<>();
        if (varDef != null)
            varDefs.add(varDef);
        for (Comma_VarDef comma_varDef: comma_varDefList) {
            varDefs.add(comma_varDef.varDef);
        }
        return varDefs;
    }
}
