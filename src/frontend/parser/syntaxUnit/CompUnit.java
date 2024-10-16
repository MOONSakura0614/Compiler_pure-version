package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import utils.IOUtils;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: 根节点，编译单元
 * CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef
 */
public class CompUnit extends SyntaxNode { // 根节点要不要implements SyntaxNode
//    ArrayList<SyntaxNode> children; // 是不是每个节点都应该有？
    // 是否需要把每个语法成分对应的token存起来？？
    ArrayList<Token> tokens; // 关于此语法成分，不涉及递归向下的（向下的到时候按链表往下读取？

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

    /*@Override
    void unitParser() {
        Token token = null;
        Iterator<Token> iterator = Parser.lexIterator.iterator();
        if (iterator.hasNext()) {
            token = iterator.next();
        }
        if (token == null) {
            return; // 能不能直接return呢还是说要实时打印
        }

        // 递归下降分析子程序
        // 但是需要注意，可能是Functype不是DefType，然后需要返回，是不是children的添加
        // 采取传到Unit里的ArrayList，判断成功后方法更合适？
        IOUtils.writeCorrectLine(token.toString());
        lineNum_begin = token.getLineNum();
        if (token.getTokenType() == LexType.CONSTTK) {
            Decl declNode = new Decl();
            children.add(declNode);
            declNode.unitParser();
        } else if (token.getTokenType() == LexType.VOIDTK) {
            FuncDef funcDefNode = new FuncDef();
            children.add(funcDefNode);
            funcDefNode.unitParser();
        } else if (token.getTokenType() == LexType.INTTK) {
            // 可能是VarDecl，也可能是FuncDef，也可能是MainFuncDef
            if (!iterator.hasNext()) {
                // int之后就没有后文
                Parser.isSyntaxCorrect = false;
                return;
            }
            Token newToken = iterator.next();
            if (newToken.getTokenType() == LexType.MAINTK) { // int之前打印过了
                IOUtils.writeCorrectLine(newToken.toString());
                // 注意由于FuncDef中还有FuncType的非终结符，所以不能急急地统一把newToken写出去
                MainFuncDef mainFuncDefNode = new MainFuncDef();
                children.add(mainFuncDefNode);
                mainFuncDefNode.unitParser();
            } else {
                // 不管是变量声明还是函数声明下一个都是标识符
                // 就看再下一个是不是函数形参的左小括号了
                Token oldToken = newToken; // 标记符
                if (isFuncDef(iterator)) {
                    // 是函数定义
                    FuncType funcTypeNode = new FuncType();
//                    children.add(funcTypeNode); // 此处涉及token究竟如何存储的问题
                    //funcTypeNode.tokenList.add(token); // 最开始进入compUnit分析的第一个token
                    funcTypeNode.unitParser();
                    IOUtils.writeCorrectLine(funcTypeNode.toString());
                    IOUtils.writeCorrectLine(oldToken.toString()); // 命名
                    newToken = Parser.lexIterator.nowToken(); // 此时应该是左括号
                    IOUtils.writeCorrectLine(newToken.toString());

                    FuncDef funcDefNode = new FuncDef();
                    children.add(funcDefNode);
                    funcDefNode.children.add(funcTypeNode); // 注意直接父节点不要弄错
                    funcDefNode.tokenList.add(token); // 返回值类型
                    funcDefNode.tokenList.add(oldToken); // 函数名
                    funcDefNode.tokenList.add(newToken); // 左小括号
                } else {
                    // 是变量定义
                    IOUtils.writeCorrectLine(oldToken.toString()); // 命名
                    // 应该先判断为Decl（Decl虽然不用输出，但是需要保留分析
                    Decl declNode = new Decl();
                    declNode.tokenList.add(token);
                    declNode.tokenList.add(oldToken); // 名字
                    // 接下来判断 标识符 后面的字符
                    // BType 也不用输出
                    newToken = Parser.lexIterator.nowToken();
                    // 1. [ - 变量数组：记住中括号中可能包含ConstExp;
                    // 2. = - 赋值，不是语法成分，只是VarDef推导右式的一种选择;
                    // 3. ; 结束VarDecl
                    // 疑似可以进入VarDecl再仔细分析:为什么不把varDecl放到Decl的子parse中？
                    // 还是在纠结到底token要给到多少信息，才方便语义分析
                    VarDecl varDeclNode = new VarDecl(); // 如果语法树的结点不存
                    varDeclNode.tokenList.add(token);
                    varDeclNode.tokenList.add(oldToken);

                }
            }
        } else if (token.getTokenType() == LexType.CHARTK) {
            // FuncDef || VarDef
            if (!iterator.hasNext()) {
                Parser.isSyntaxCorrect = false;
                return;
            }
            Token newToken = iterator.next();
            if (newToken.getTokenType() != LexType.IDENFR) {
                Parser.isSyntaxCorrect = false;
                return;
            }
            IOUtils.writeCorrectLine(newToken.toString());
            if (!iterator.hasNext()) {
                Parser.isSyntaxCorrect = false;
                return;
            }
            newToken = iterator.next();
            if (newToken.getTokenType() == LexType.LPARENT) {
                // 目前遍历到的token要不要设为全局变量？（比如设给Parser的public static类变量
                FuncDef funcDefNode = new FuncDef();
                children.add(funcDefNode);
                funcDefNode.unitParser();
            }
        }

        Token endToken = Parser.lexIterator.nowToken();

        if (endToken != null) {
            this.lineNum_end = endToken.getLineNum();
        }

        IOUtils.writeCorrectLine(toString());
    }*/

    /*@Override
    public void setSyntaxName() {
        syntaxName = "CompUnit";
    }*/

    public static void main(String[] args) {
        CompUnit compUnit = new CompUnit();
        if (compUnit.children != null) {
            System.out.println("new ArrayList so not null");
//            System.out.println("new ArrayList but real null");
            // 错误的，new了就不是null
            // 如果ArrayList<SyntaxNode> children = new ArrayList<>()就不是null，就是实例化了，只有不赋值的最开始才是？
        } else {
            System.out.println("new ArrayList but real null");
        }
    }
}
