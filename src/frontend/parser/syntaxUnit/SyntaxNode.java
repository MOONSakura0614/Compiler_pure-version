package frontend.parser.syntaxUnit;

import frontend.lexer.LexType;
import frontend.lexer.Token;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 各语法成分（非终结符）
 * @date 2024/10/11 9:08
 */
public abstract class SyntaxNode implements Comparable<SyntaxNode> {
    // 要不要重构成按照结束的lineNum排序：因为<syntaxName>是在这部分输出之后再输出，和加入ArrayList的顺序不一致？

    // 共同接口，给其他的非终结符
    private String syntaxName;
    ArrayList<SyntaxNode> children; // 大概只用于记录直接子节点
    ArrayList<Token> tokenList;
    protected int lineNum_begin, lineNum_end;

    protected SyntaxNode(String name) {
        children = new ArrayList<>();
        tokenList = new ArrayList<>();
        syntaxName = name;
    }

    SyntaxNode() {}

    public String getSyntaxName() {
        return syntaxName;
    }

    abstract public void unitParser();

    @Override
    public int compareTo(SyntaxNode o) {
        // 可能考虑用lineNum写？暂时马住，主要是考虑到AST树结构会被排序破坏--但是链表又感觉差不多
        return 0;
    }

    @Override
    public String toString() {
        return '<' + syntaxName + '>' + '\n';
    }

    // static方法在统一继承的抽象类中加入判断语法成分的方法
    public static boolean isMainFuncDef() {
        if (lexIterator.curPos < lexIterator.tokenCount - 1)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.INTTK
                    && lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() == LexType.MAINTK;
        // int main开头（主要判断main保留字—至少需要两个词）
        return false;
    }
    public static boolean isDecl() {
        // 在判断的时候不改变curPos
        return isConstDecl() || isVarDecl(); // const/int/char/void开始，并且不接main
    }

    public static boolean isFuncDef() {
        if (!isFuncType())
            return false;
        // 注意语法分析中只有缺少右括号的问题，左括号应该是保证有的
        if (lexIterator.curPos < lexIterator.tokenCount - 2) // 第一个token判断FuncType，下面不用重复判断
            return /*lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.INTTK
                    &&*/ lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() == LexType.IDENFR
                    && lexIterator.tokenList.get(lexIterator.curPos + 2).getTokenType() == LexType.LPARENT;
        return false;
    }

    public static boolean isFuncType() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.VOIDTK
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.INTTK
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.CHARTK;
        return false;
    }

    public static boolean isConstDecl() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.CONSTTK;
        return false; // 判断const
    }

    public static boolean isVarDecl() {
        // 左递归文法要求：规则右式first集不重合，那是要提前提取重复的token，还是？
        /*if (!isBType())
            return false;
        return (!isMainFuncDef()) && (!isFuncDef());*/
        return isBType() && !(isMainFuncDef() || isFuncDef());
        // BType开头，但不是int main也不是funcDef
    }

    public static boolean isVarDef() {
        /* ; = [
        if (lexIterator.curPos < lexIterator.tokenCount - 1) // 如果标识符后面还有token
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.IDENFR
                    && (lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() != LexType.LPARENT || lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() != LexType.ASSIGN);
            // 不是Func就行，可以是{, VarDef}，也可以是[]，也可以是=，或者;
        else*/ if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.IDENFR;
            // 只有标识符，没有下一个token，说明很可能缺少分号，到语法单元内部处理
        } // varDef后面也可能就是归并到前面VarDecl的后一个token：';'
        return false;
    }

    public static boolean isInitVal() {
        // 是不是这个static方法放到abstract里面，然后让每个语法成分自己重构？（但是就不能复用）
        return isStringConst() || isArrayInit() || isExp();
    }

    public static boolean isBType() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.INTTK
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.CHARTK;
        return false; // int/char
    }

    public static boolean isConstDef() { // 要求必须assign
        // 感觉constDef不用额外判断，ConstDecl
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.IDENFR;
            // 只有标识符，没有下一个token，说明很可能缺少分号，到语法单元内部处理
        }
        return false; // Ident标识符类型开头，之后a.[]为一维数组; b.普通变量——但是都要求有=赋值
    }

    public static boolean isConstExp() {
        return isAddExp(); // 注：使用的 Ident 必须是常量
    }

    public static boolean isExp() {
        return isAddExp();
    }

    // TODO: 2024/10/14 输出顺序AddExp和MulExp
    public static boolean isAddExp() {
        return isMulExp();
    }

    /* AddExp ('+' | '−') MulExp*/
    public static boolean isAddOperator() {
        // +，-
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.PLUS
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.MINU;
        }
        return false;
    }

    // TODO: 2024/10/14 输出顺序MulExp和UnaryExp
    public static boolean isMulExp() {
        return isUnaryExp();
    }

    /*  MulExp ('*' | '/' | '%') UnaryExp */
    public static boolean isMulOperator() {
        // *,/,%
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.MULT
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.DIV
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.MOD;
        }
        return false;
    }

    public static boolean isUnaryExp() {
        // 比FuncDef缺少了一个FuncType： Ident '(' [FuncRParams] ')'——是否这个函数的返回值是Exp需要的 应该是语义分析考虑的内容
        return isPrimaryExp() || isUnaryOp() || isFuncCall();
    }

    // TODO: 2024/10/15 输出顺序RelExp和AddExp
    public static boolean isRelExp() {
        return isAddExp(); // 注意 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp 颠倒造成的语法成分输出顺序
    }

    /* RelExp ('<' | '>' | '<=' | '>=') AddExp */
    public static boolean isRelOperator() {
        // *,/,%
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.LSS
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.LEQ
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.GRE
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.GEQ;
        }
        return false;
    }

    // TODO: 2024/10/15 输出顺序EqExp和RelExp
    public static boolean isEqExp() {
        return isRelExp();
    }

    /* EqExp ('==' | '!=') RelExp */
    public static boolean isEqOperator() {
        // *,/,%
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.EQL
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.NEQ;
        }
        return false;
    }

    // TODO: 2024/10/15 输出顺序LAndExp和EqExp
    public static boolean isLAndExp() {
        return isEqExp();
    }

    public static boolean isCond() {
        return isLOrExp(); // 条件表达式
    }

    // TODO: 2024/10/15 输出顺序LOrExp和LAndExp
    public static boolean isLOrExp() {
        return isLAndExp();
    }

    public static boolean isOrOP() {
        if (lexIterator.curPos < lexIterator.tokenCount - 1) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.OR;
        }
        return false;
    }

    public static boolean isAndOP() {
        if (lexIterator.curPos < lexIterator.tokenCount - 1) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.AND;
        }
        return false;
    }

    public static boolean isFuncCall() {
        // 函数调用的判断
        if (lexIterator.curPos < lexIterator.tokenCount - 1) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.IDENFR
                    && lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() == LexType.LPARENT;
        }
        return false;
    }

    public static boolean isUnaryOp() {
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.PLUS
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.MINU
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.NOT;
        }
        return false;
    }

    public static boolean isPrimaryExp() {
        if (isLVal()) {
            return true;
        }
        // PrimaryExp的右式就是终结符→ '(' Exp ')' | LVal | Number | Character
        // 感觉 Exp → PrimaryExp; PrimaryExp → '(' Exp ')' 莫名有死循环风险，要不直接'('判断（只取第一个token算了？？——不然判断函数必然死循环了
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return isNumber() || isCharacter()
                    ||lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.LPARENT;
        }
        return false;
    }

    /*ForStmt → LVal(可能是Ident或者Ident[Exp] '=' Exp*/
    public static boolean isForStmt() { // for的第一个和第三个位置，注意没有分号
        if (lexIterator.curPos < lexIterator.tokenCount - 1) {
            if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.IDENFR) {
                if (lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() == LexType.ASSIGN) {
                    return true;
                } else if (lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() == LexType.LBRACK) {
                    // 检验Exp
                    int curPos0 = lexIterator.curPos;
                    // 切记，return之前要复原
                    lexIterator.curPos = curPos0 + 2;
                    // 假定到'['(原lexIterator.curPos + 1位置的token)已经解析完毕：分析完再复原给Parser
                    if (!isExp()) {
                        lexIterator.curPos = curPos0;
                        return false;
                    } else {
                        // 分析Assign:怎么确定目前的位置？
                        // 暂时解析一下Exp罢，用一个临时变量exp_tmp，之后引用销毁
                        Exp exp_tmp = new Exp();
                        exp_tmp.unitParser(); // 从现在的curPos开始解析
                        // curPos被顺利移动到Exp后，即]= 、 =  // [] 可能存在缺少右中括号
                        if (lexIterator.iterator().hasNext()) {
                            if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.ASSIGN) {
                                lexIterator.curPos = curPos0;
                                return true;
                            }
                            if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.RBRACK) {
                                if (lexIterator.curPos < lexIterator.tokenCount - 1) { // 庆幸只有一维数组
                                    if (lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() == LexType.ASSIGN) {
                                        lexIterator.curPos = curPos0;
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    lexIterator.curPos = curPos0;
                    /*if (lexIterator.curPos < lexIterator.tokenCount - 4) { // Exp需要检验吗
                        return lexIterator.tokenList.get(lexIterator.curPos + 4).getTokenType() == LexType.ASSIGN;
                    } else if (lexIterator.curPos < lexIterator.tokenCount - 3) {
                        return lexIterator.tokenList.get(lexIterator.curPos + 3).getTokenType() == LexType.ASSIGN;
                    }*/
                    // else顺延向下执行，默认返回false
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    // 注意：左值是指可以在赋值号左边出现的词，不是一定要出现在等号（=不一定要出现）左边！
    public static boolean isLVal() {
        // 感觉不太全面，这个左值主要是先排除Func之后的结果
        if (isFuncCall()) {
            return false;
        }
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.IDENFR;
        }
        // 是否需要接着判断可选的[Exp] //1.普通变量、常量 2.一维数组
        return false;
        // LVal出现在Stmt，ForStmt，PrimaryExp的右式中
        /* Stmt → LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';' | LVal '=' Exp ';'
        * ForStmt → LVal '=' Exp
        * PrimaryExp → '(' Exp ')' | LVal | Number | Character */
    }

    public static boolean isNumber() {
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.INTCON;
        }
        return false;
    }
    public static boolean isCharacter() {
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.CHRCON;
        }
        return false;
    }

    public static boolean isStringConst() {
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.STRCON;
        }
        return false;
    }

    // 下面是数组定义名时[]的标志识别
    public static boolean isArray() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.LBRACK;
        return false; // [
    }

    // 下面是数组、多个元素同种初始化时{}的标志识别
    public static boolean isArrayInit() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.LBRACE;
        return false; // {
    }

    public static boolean isAssign() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.ASSIGN;
        return false; // =
    }

    public static boolean isComma() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.COMMA;
        return false; // ,
    }

    public static boolean isSemicn() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.SEMICN;
        return false; // ;
    }

    public static boolean isBlock() { // 和判断ArrayInit一致，都是找{}
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.LBRACE;
        return false; // {
    }

    public static boolean isBlockItem() {
        return isDecl() || isStmt();
    }

    public static boolean isStmt() {
        // LVal后面成分还需要继续判断吗，但是
        return isLVal() || isExp() || isSemicn() || isBlock() || isPrint()
                || isIf() || isFor() || isBreakOrContinue() || isReturn();
    }

    private static boolean isReturn() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.RETURNTK;

        return false;
    }

    private static boolean isBreakOrContinue() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.BREAKTK
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.CONTINUETK;
        return false;
    }

    public static boolean isPrint() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.PRINTFTK;
        return false; // printf
    }

    public static boolean isIf() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.IFTK;
        return false; // if
    }

    public static boolean isFor() {
        if (lexIterator.curPos < lexIterator.tokenCount)
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.FORTK;
        return false; // for
    }

    // 函数相关
    public static boolean isRParams() {
        return isExp();
    }

    public static boolean isIdent() {
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.IDENFR;
        }
        return false;
    }

    public static boolean isInputFunc() {
        // 判断是不是读取输入（get）
        if (lexIterator.curPos < lexIterator.tokenCount) {
            return lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.GETCHARTK
                    || lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.GETINTTK;
        }
        return false;
    }

    public static boolean isFParams() {
        return isFParam();
    }

    public static boolean isFParam() {
        return isVarDecl(); // BType开头，且不是func
    }

    // TODO: 2024/10/16 用于区分Stmt中的 Exp 和 LVal （是否后面紧跟着=
    public static boolean isLValAssign() {
            // Ident ['[' + Exp + ']']

        if (lexIterator.curPos < lexIterator.tokenCount - 1) {
            if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.IDENFR) {
                if (lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() == LexType.ASSIGN) {
                    return true;
                } else if (lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() == LexType.LBRACK) {
                    // 检验Exp
                    int curPos0 = lexIterator.curPos;
                    // 切记，return之前要复原
                    lexIterator.curPos = curPos0 + 2;
                    // 假定到'['(原lexIterator.curPos + 1位置的token)已经解析完毕：分析完再复原给Parser
                    if (!isExp()) {
                        lexIterator.curPos = curPos0;
                        return false;
                    } else {
                        // 分析Assign:怎么确定目前的位置？
                        // 暂时解析一下Exp罢，用一个临时变量exp_tmp，之后引用销毁
                        /*Exp exp_tmp = new Exp();
                        exp_tmp.unitParser(); // 从现在的curPos开始解析*/
                        new Exp().unitParser();
                        // curPos被顺利移动到Exp后，即]= 、 =  // [] 可能存在缺少右中括号
                        if (lexIterator.iterator().hasNext()) {
                            if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.ASSIGN) {
                                lexIterator.curPos = curPos0;
                                return true;
                            }
                            if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.RBRACK) {
                                if (lexIterator.curPos < lexIterator.tokenCount - 1) { // 庆幸只有一维数组
                                    if (lexIterator.tokenList.get(lexIterator.curPos + 1).getTokenType() == LexType.ASSIGN) {
                                        lexIterator.curPos = curPos0;
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    lexIterator.curPos = curPos0;
                }
            }
        }
        return false;
    }

    // 每个不同的语法成分输出方式不同（因为组成成分也不同），所以在每个里面单独打印，而不是直接后序遍历AST？
    public abstract void print();
}
