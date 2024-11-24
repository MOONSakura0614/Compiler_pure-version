package llvm;

import frontend.parser.Parser;
import frontend.parser.syntaxUnit.*;
import frontend.symbol.SymbolTable;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: LLVM IR中间代码生成器
 * @date 2024/11/13 17:07
 */
public class IRGenerator {
    private static IRGenerator irGenerator; // 单例模式
    private CompUnit ast; // 语法树的根节点 compUnit
    private static Parser parser;
    private SymbolTable symbolTable; // SSA
    // 记录的应该是当前的符号表——curSymTable
    private IRModule irModule;
    public static int cur_ir_symTable_scope;

//    public static SymbolTable constSymTable; // 每个符号表中的不同符号代表有标记常量和变量==>不用每个作用域两个表

    static {
        parser = Parser.getInstance();
//        constSymTable = new SymbolTable();
        // 常量表没有fatherTable，scope默认为0，但是不太影响(?
        // 常量表是都能用？？
        cur_ir_symTable_scope = 0; // 最外层是全局
    }

    private IRGenerator() {
        symbolTable = new SymbolTable();
    }

    public static IRGenerator getInstance() {
        if (irGenerator == null) {
            irGenerator = new IRGenerator();
        }

        return irGenerator;
    }

    public void generateIR() {
        if (parser == null)
            parser = Parser.getInstance();
        ast = parser.getAst();
        if (irModule == null)
            irModule = IRModule.getInstance();

        if (ast == null) {
            return; // 出错
        }

        // 全局变量：GlobalVariable
        for (Decl decl: ast.getDeclList()) {
            visitDecl(decl);
        }

        // todo lib外部静态链接的IO函数需要在哪输出？
        // GlobalValue：自定义函数
        for (FuncDef funcDef: ast.getFuncDefList()) {
            visitFuncDef(funcDef);
        }
        
        if (ast.getMainFuncDef() != null) {
            visitMainFuncDef(ast.getMainFuncDef());
        }
    }

    private void visitMainFuncDef(MainFuncDef mainFuncDef) {
    }

    private void visitFuncDef(FuncDef funcDef) {
    }

    public void visitDecl(Decl decl) {
        if (decl.getVarDecl() == null)
            return;
        if (decl.getIsConst()) {
            if (decl.getConstDecl() == null)
                return;            
            visitConstDecl(decl.getConstDecl());
        } else {
            if (decl.getVarDecl() == null)
                return;
            visitVarDecl(decl.getVarDecl());
        }
    }

    private void visitVarDecl(VarDecl varDecl) {
//        varDecl.generateIR();
    }

    public void visitConstDecl(ConstDecl constDecl) {
    }

}
