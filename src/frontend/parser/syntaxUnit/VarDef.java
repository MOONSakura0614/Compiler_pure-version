package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import frontend.symbol.Symbol;
import frontend.symbol.SymbolTable;
import frontend.symbol.SymbolType;
import frontend.symbol.VarSymbol;
import llvm.IRGenerator;
import llvm.value.IRGlobalVar;
import llvm.value.IRValue;
import llvm.value.constVar.IRConstChar;
import llvm.value.constVar.IRConstInt;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 变量定义-包含普通常量、一维数组定义
 * VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
 */
public class VarDef extends SyntaxNode {
    private Token ident_token; // 标识符
    private Token left_bracket_token;
    private ConstExp constExp;
    private Token right_bracket_token;
    private Token assign_token;
    private InitVal initVal;
    private Boolean isArray;
    private Boolean isAssigned;

    public VarDef() {
        super("VarDef");
        isArray = Boolean.FALSE;
        isAssigned = Boolean.FALSE;
    }

    @Override
    public void unitParser() {
        Token token;
        if (lexIterator.iterator().hasNext()) {
            token = lexIterator.iterator().next();
            lineNum_begin = token.getLineNum();
            if (token.getTokenType() == LexType.IDENFR) {
                ident_token = token;
            } else {
                throw new RuntimeException("VarDef解析出错：Ident标识符无法解析\n此token实际为："+token);
            }
        }
        if (isArray()) {
            // 注意是不是[]中括号只会在数组中出现
            isArray = Boolean.TRUE;
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.iterator().next();
                if (token.getTokenType() == LexType.LBRACK) {
                    left_bracket_token = token;
                } else {
                    throw new RuntimeException("VarDef解析出错：[无法解析\n此token实际为："+token);
                }
            }
            // 数组为定长，下面解析ConstExp
            constExp = new ConstExp();
            constExp.unitParser();
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.iterator().next();
                if (token.getTokenType() == LexType.RBRACK) {
                    right_bracket_token = token;
                } else {
                    lexIterator.retract(); // 回退
                    CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRBRACK);
                    IOUtils.compileErrors.add(error);
                    Parser.isSyntaxCorrect = Boolean.FALSE;
                }
            } else {
                lexIterator.retract(); // 回退
                CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRBRACK);
                IOUtils.compileErrors.add(error);
                Parser.isSyntaxCorrect = Boolean.FALSE;
            }
        }
        if (isAssign()) {
            isAssigned = Boolean.TRUE;
            // 注意赋值是否合法（数组和单变量赋值）应该在语义分析汇总判断
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.iterator().next();
                if (token.getTokenType() == LexType.ASSIGN) {
                    assign_token = token;
                } else {
                    throw new RuntimeException("VarDef解析出错：=无法解析\n此token实际为："+token);
                }
                initVal = new InitVal();
                initVal.unitParser();
            }
        }
    }

    @Override
    public void print() {
        if (ident_token != null) {
            IOUtils.writeCorrectLine(ident_token.toString());

            if (isArray) {
                if (left_bracket_token != null)
                    IOUtils.writeCorrectLine(left_bracket_token.toString());
                if (constExp != null)
                    constExp.print();
                if (right_bracket_token != null)
                    IOUtils.writeCorrectLine(right_bracket_token.toString());
            }

            if (isAssigned) {
                // 防止访问null，先在构造器里初始化了
                if (assign_token != null)
                    IOUtils.writeCorrectLine(assign_token.toString());
                if (initVal != null)
                    initVal.print();
            }

            IOUtils.writeCorrectLine(toString());
        } else {
            throw new RuntimeException("无VarDef成员变量，无法正确输出语法分析结果");
        }
    }

    @Override
    public void insertSymbol(SymbolTable symbolTable) { // 默认插入int
        if (this.ident_token == null)
            return;
        Symbol symbol = new VarSymbol(this, ident_token, symbolTable.getScope());
        if (isArray) {
            symbol.setSymbolType(SymbolType.IntArray);
            symbol.setIsArray();
        }
        else
            symbol.setSymbolType(SymbolType.Int);

        symbolTable.insertSymbol(symbol);

        // 在中间代码生成阶段
        if (IRGenerator.llvm_ir_gen) {
            int val = 0;
            if (initVal != null) {
                val = initVal.getIntValue();
            }
            // TODO: 2024/11/28 下面这个地方设Value的new有点莫名其妙，全局就在if中重新set了；普通的局部，也应该是用 alloca那条吧（从内存使用的时候再load
            IRValue value = builder.buildInt(ident_token.getTokenValue());
            symbol.setIrValue(value);
            symbol.setIntValue(val);
            if (IRGenerator.globalVar_gen) {
                IRGlobalVar globalVar = builder.buildIRGlobalVar(value);
                IRGenerator.globalVars.add(globalVar);
                globalVar.setInt_value(val);
                symbol.setIrValue(globalVar);
            }
        }

        if (!IRGenerator.llvm_ir_gen && initVal != null)
            initVal.visit();
    }

    public void insertCharSymbol(SymbolTable symbolTable) {
        if (this.ident_token == null)
            return;
        Symbol symbol = new VarSymbol(this, ident_token, symbolTable.getScope()); // 包括ConstInitVal
        if (isArray) {
            symbol.setSymbolType(SymbolType.CharArray);
            symbol.setIsArray();
        }
        else
            symbol.setSymbolType(SymbolType.Char);

        symbolTable.insertSymbol(symbol);

        // 在中间代码生成阶段
        if (IRGenerator.llvm_ir_gen) {
            // TODO: 2024/11/26 没有完成数组的
            int val = 0;
            if (initVal != null) {
                val = initVal.getIntValue();
            }
            IRValue value = builder.buildChar(ident_token.getTokenValue()); // 全局变量采用自己的标志符做名字？
            symbol.setIrValue(value);
            symbol.setIntValue(val);
            if (IRGenerator.globalVar_gen) {
                IRGlobalVar globalVar = builder.buildIRGlobalVar(value);
                IRGenerator.globalVars.add(globalVar);
                globalVar.setInt_value(val);
                // V2:全局变量的irValue改成globalVar
                symbol.setIrValue(globalVar);
            }
        }

        if (!IRGenerator.llvm_ir_gen && initVal != null) // 在中间代码生成阶段，不再语义visit
            initVal.visit();
    }

    public String getIdentName() {
        return ident_token.getTokenValue();
    }

    public Boolean getIsArray() {
        return isArray;
    }

    public InitVal getInitVal() {
        return initVal;
    }
}
