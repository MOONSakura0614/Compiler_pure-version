package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import frontend.symbol.SymbolTable;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 郑悦
 * @Description: 根节点，编译单元
 * CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef
 */
public class CompUnit extends SyntaxNode { // 根节点要不要implements SyntaxNode
    ArrayList<Decl> declList;
    ArrayList<FuncDef> funcDefList;
    MainFuncDef mainFuncDef;

    public CompUnit() {
        super("CompUnit");
//        children = new ArrayList<>();

        // 推导式 蕴含语法成分初始化
        declList = new ArrayList<>();
        funcDefList = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        // Decl FuncDef MainFuncDef
        Decl decl;
        while (isDecl()) {
            // 进入Decl的判断
            decl = new Decl();
            decl.unitParser();
            declList.add(decl);
        }
        FuncDef funcDef;
        while (isFuncDef()) {
            //
            funcDef = new FuncDef();
            funcDef.unitParser();
            funcDefList.add(funcDef);
        }
        // main函数解析
        if (isMainFuncDef()) {
            mainFuncDef = new MainFuncDef();
            mainFuncDef.unitParser();
        } else {
            // 注意SysY要求必须有main函数
            throw new RuntimeException("CompUnit解析失败：无法找到MainFuncDef");
        }
    }

    @Override
    public void print() {
        if (!declList.isEmpty()) {
            for (Decl decl: declList) {
                decl.print();
            }
        }
        if (!funcDefList.isEmpty()) {
            for (FuncDef funcDef: funcDefList) {
                funcDef.print();
            }
        }
        mainFuncDef.print();
        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() { // 深搜：后序遍历AST
        SymbolTable table = initSymbolTable(); // 函数中已经包括add to symbolTableList

        // 添加新符号
        // Decl
        for (Decl decl: declList) {
            decl.insertSymbol(table); // 是否需要传入对应的SymbolTable
        }
        // FuncDef
        for (FuncDef funcDef: funcDefList) {
            funcDef.insertSymbol(table);
            // 每个FuncDef有自己的作用域（包括每个Block）
            funcDef.visit();
            // 每个FuncDef内部作用域需要赋值外层符号表（fatherSymbolTable
        }
        // MainFuncDef
        if (mainFuncDef != null) {
            mainFuncDef.visit();
        }
    }
}
