package frontend.symbol;

import frontend.lexer.Token;
import frontend.parser.syntaxUnit.Decl;
import frontend.parser.syntaxUnit.FuncDef;
import frontend.parser.syntaxUnit.SyntaxNode;
import llvm.type.IRFunctionType;
import llvm.type.IRIntType;
import llvm.type.IRType;
import llvm.value.IRValue;
import llvm.value.constVar.IRConstArray;

/**
 * @author 郑悦
 * @Description: 符号表管理中识别出的符号
 * @date 2024/10/16 22:31
 */
public class Symbol {
    protected int tableId; // 当前单词所在的符号表编号（多个符号表？）：是不是作用域序号
    protected String identName;
    // 由于错误处理（重定义）需要lineNum
    protected Token identToken;
    // 注意，作为父类给子类继承使用的话，只能protect
    protected Boolean isConst;
    protected Boolean isArray = Boolean.FALSE; // 注意SysY最多数组只支持一维
    protected SymbolType symbolType;
    // TODO: 2024/10/26 是否需要将Exp表示的值计算出来，防止数组越界？

    // 下面是支持中间代码生成
    public IRValue irValue; // 代码生成的符号表
    // TODO: 2024/11/15 还是说代码生成应该重新整一个符号表-->因为这边在frontend下都被protected了！
    private int intValue = 0; // 对于i8和i32变量——可以从SymbolType判断变量的实值类型
    // 初始化变量的值为0（数组变量为全0）
//    private char charValue = 0; // 对于i8和i32变量 --> 统一用int记录了
    private String pointerReg;

    public Symbol() {
        symbolType = SymbolType.Int;
    }

    public Symbol(String ident_name) {
        identName = ident_name;
        isArray = Boolean.FALSE;
        symbolType = SymbolType.Int;
    }

    public Symbol(String ident_name, IRValue value) {
        identName = ident_name;
        isArray = Boolean.FALSE;
        symbolType = SymbolType.Int;
        irValue = value; // value中包含irType
    }

    public Symbol(String ident_name, SymbolType type, IRValue value) {
        identName = ident_name;
        isArray = Boolean.FALSE;
        symbolType = type;
        irValue = value; // value中包含irType
    }

    public Symbol(Token ident_token, int id) {
        this.identToken = ident_token;
        isArray = Boolean.FALSE;
        symbolType = SymbolType.Int;
        tableId = id; // 感觉每个符号还是有自己的id比较好（区分不同作用域的同名符号）
        // 注意不能让string做符号表key的标识符为null
        if (ident_token != null)
            identName = ident_token.getTokenValue();
    }

    public Symbol(SyntaxNode node) { // Decl 或 FuncDef
        if (!(node instanceof Decl || node instanceof FuncDef)) {
            throw new RuntimeException("创建符号失败，不应传入" + node.getSyntaxName() + "语法成分进行创建符号");
        }
        symbolType = SymbolType.Int;
    }

    public void setSymbolType(SymbolType type) {
        this.symbolType = type;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public boolean isConstSymbol() {
        return symbolType.equals(SymbolType.ConstChar) || symbolType.equals(SymbolType.ConstInt)
                || symbolType.equals(SymbolType.ConstCharArray) || symbolType.equals(SymbolType.ConstIntArray);
    }

    public boolean isFunc() {
        return symbolType.equals(SymbolType.CharFunc) || symbolType.equals(SymbolType.IntFunc) || symbolType.equals(SymbolType.VoidFunc);
    }

    public void setIsArray() { // 默认false，调用就是设为true
        isArray = Boolean.TRUE;
    }

    public Boolean getIsArray() {
        return isArray;
    }

    /* 加入数组维护 */
    public IRConstArray initArray;

    public void setIrValue(IRValue irValue) {
        this.irValue = irValue;
        if (irValue instanceof IRConstArray) {
            initArray = (IRConstArray) irValue;
            // 大概不维护，只用常量不变，变量数组不好维护（运行时决定值
        }
//        System.out.println(irValue);
    }

    public IRValue getIrValue() {
//        System.out.println(irValue);
        return irValue;
    }

    public int getIntValue() {
        isArray = Boolean.FALSE;
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public String getIdentName() {
        return identName;
    }

    public void setPointerReg(String pointerReg) {
        this.pointerReg = pointerReg;
    }

    public void setRetType(IRType type) {
        ((IRFunctionType) irValue.getIrType()).setRet_type(type);
    }

    private int[] arrayValue;
    public void setIntArrayValue(int[] vals) {
        isArray = Boolean.TRUE;
        // 设置数组值
        arrayValue = vals;
    }
    public int getArrayElementValueByIndex(int index) {
        if (index < arrayValue.length) {
            return arrayValue[index];
        } else {
            throw new RuntimeException("超出数组边界");
        }
    }
}
