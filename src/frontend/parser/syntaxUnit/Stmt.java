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
 * @Description:
 * Stmt → LVal '=' Exp ';'
 *  | [Exp] ';' // 这个[]就代表有/无，注意两种情况内即含空语句
 *  | Block
 *  | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
 *  | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省，1种情况 2.
 * ForStmt与Cond中缺省一个，3种情况 3. ForStmt与Cond中缺省两个，3种情况 4. ForStmt与Cond全部
 * 缺省，1种情况
 * | 'break' ';' | 'continue' ';'
 * | 'return' [Exp] ';' // 1.有Exp 2.无Exp
 * | LVal '=' 'getint''('')'';'
 * | LVal '=' 'getchar''('')'';'
 * | 'printf''('StringConst {','Exp}')'';' // 1.有Exp 2.无Exp
 */
public class Stmt extends SyntaxNode {
    private Integer chosen_plan = null; // 按数字标记解析方案
    private Token semicn_token; // 分号是除了Block和If之外语句共用的

    // 1. LVal '=' Exp ';'
    private LVal lVal;
    private Token assign_token;
    private Exp exp;

    // 2. [Exp] ';'
    private Exp singleExp;

    // 3. Block
    private Block block;

    // 4. 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    private Token if_token;
    // 左右括号和Cond与for共用
    private Token lParent_token;
    private Cond cond;
    private Token rParent_token;
    private Stmt ifStmt;
    private Boolean hasElse;
    private Token else_token;
    private Stmt elseStmt;

    // 5. 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    private Token for_token;
    private ForStmt forStmt1;
    private Token semicn1;
    private Boolean hasCond; // (Cond) 和4.if-else共用
    private Token semicn2;
    private ForStmt forStmt2;
    private Stmt stmt;

    // 6. 'break' ';' | 'continue' ';'
    private Token break_continue_token;
    private Boolean isBreak;

    // 7. 'return' [Exp] ';' // 1.有Exp 2.无Exp
    private Token return_token;
    private Boolean hasReturnExp;

    // 8. LVal '=' 'getint''('')'';'
    // 9. LVal '=' 'getchar''('')'';'
    private Token bType_token;
    private Token inputFunc_token;
//    private LVal lVal; // lVal与1.LVal '=' Exp ';'共用
    // assign_token也共用 // 左右括号也是方案共用

    // 10. 'printf''('StringConst {','Exp}')'';' // 1.有Exp 2.无Exp
    private Boolean hasPrintExp;
    private Token print_token;
    private Token string_token;
    // 是否存在{ ',' Exp }
    private ArrayList<InitVal.Comma_Exp> comma_exp_list;

    public Stmt() {
        super("Stmt");
        hasElse = Boolean.FALSE;
        hasCond = Boolean.FALSE;
        isBreak = Boolean.FALSE;
        hasReturnExp = Boolean.FALSE;
        hasPrintExp = Boolean.FALSE;

        comma_exp_list = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        Token token;
        if (lexIterator.iterator().hasNext()) {
            token = lexIterator.tokenList.get(lexIterator.curPos);

            if (token.getTokenType() == LexType.IFTK) {
                chosen_plan = 4;
                if_token = lexIterator.iterator().next(); // 往前读

                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.tokenList.get(lexIterator.curPos);
                    if (token.getTokenType() == LexType.LPARENT) {
                        lParent_token = lexIterator.iterator().next();

                        cond = new Cond();
                        cond.unitParser();

                        if (lexIterator.iterator().hasNext()) {
                            token = lexIterator.tokenList.get(lexIterator.curPos);
                            if (token.getTokenType() == LexType.RPARENT) {
                                rParent_token = lexIterator.iterator().next();

                                ifStmt = new Stmt();
                                ifStmt.unitParser();

                                if (lexIterator.iterator().hasNext()) {
                                    token = lexIterator.tokenList.get(lexIterator.curPos);
                                    if (token.getTokenType() == LexType.ELSETK) {
                                        hasElse = Boolean.TRUE;
                                        else_token = lexIterator.iterator().next();

                                        elseStmt = new Stmt();
                                        elseStmt.unitParser();
                                    }
                                }
                            } else {
                                Parser.isSyntaxCorrect = Boolean.FALSE;
                                CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                                IOUtils.compileErrors.add(error);
                            }
                        } else {
                            Parser.isSyntaxCorrect = Boolean.FALSE;
                            CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                            IOUtils.compileErrors.add(error);
                        }
                    }
                }
            } else if (token.getTokenType() == LexType.FORTK) {
                chosen_plan = 5;
                for_token = lexIterator.iterator().next();

                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.tokenList.get(lexIterator.curPos);
                    if (token.getTokenType() == LexType.LPARENT) {
                        lParent_token = lexIterator.iterator().next();

                        if (isForStmt()) {
                            // 解析是否有forStmt
                            forStmt1 = new ForStmt();
                            forStmt1.unitParser();
                        }

                        // 注意这下面的SEMICN应该也是属于Stmt缺少的报错范围！
                        if (isSemicn())
                            semicn1 = lexIterator.iterator().next();
                        else {
                            Parser.isSyntaxCorrect = Boolean.FALSE;
                            CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                            IOUtils.compileErrors.add(error);
                        }

                        if (isCond()) {
                            cond = new Cond();
                            cond.unitParser();
                        }

                        if (isSemicn())
                            semicn2 = lexIterator.iterator().next();
                        else {
                            Parser.isSyntaxCorrect = Boolean.FALSE;
                            CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                            IOUtils.compileErrors.add(error);
                        }

                        if (isForStmt()) {
                            forStmt2 = new ForStmt();
                            forStmt2.unitParser();
                        }

                        if (lexIterator.iterator().hasNext()) {
                            token = lexIterator.tokenList.get(lexIterator.curPos);
                            if (token.getTokenType() == LexType.RPARENT) {
                                rParent_token = lexIterator.iterator().next();
                            } else {
                                Parser.isSyntaxCorrect = Boolean.FALSE;
                                CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                                IOUtils.compileErrors.add(error);
                            }
                        } else {
                            Parser.isSyntaxCorrect = Boolean.FALSE;
                            CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                            IOUtils.compileErrors.add(error);
                        }

                        if (isStmt()) {
                            stmt = new Stmt();
                            stmt.unitParser();
                        }
                    }
                }
            } else if (token.getTokenType() == LexType.LBRACE) {
                // 有大括号包裹的Stmt，直接判断左{
                chosen_plan = 3;
                block = new Block();
                block.unitParser();
            } else if (token.getTokenType() == LexType.PRINTFTK) {
                chosen_plan = 10;
                print_token = lexIterator.iterator().next();

                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.tokenList.get(lexIterator.curPos);
                    if (token.getTokenType() == LexType.LPARENT) {
                        lParent_token = lexIterator.iterator().next();

                        if (lexIterator.iterator().hasNext()) {
                            token = lexIterator.tokenList.get(lexIterator.curPos);
                            if (token.getTokenType() == LexType.STRCON) {
                                string_token = lexIterator.iterator().next();

                                Exp exp1;
                                InitVal.Comma_Exp comma_exp;
                                while (isComma()) {
                                    hasPrintExp = Boolean.TRUE;

                                    token = lexIterator.iterator().next();
                                    if (isExp()) {
                                        exp1 = new Exp();
                                        exp1.unitParser();

                                        comma_exp = new InitVal.Comma_Exp(token, exp1);
                                        comma_exp_list.add(comma_exp);
                                    }
                                }

                                if (lexIterator.iterator().hasNext()) {
                                    token = lexIterator.tokenList.get(lexIterator.curPos);
                                    if (token.getTokenType() == LexType.RPARENT) {
                                        rParent_token = lexIterator.iterator().next();

                                    } else {
                                        Parser.isSyntaxCorrect = Boolean.FALSE;
                                        CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                                        IOUtils.compileErrors.add(error);
                                    }
                                } else {
                                    Parser.isSyntaxCorrect = Boolean.FALSE;
                                    CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                                    IOUtils.compileErrors.add(error);
                                }

                                if (lexIterator.iterator().hasNext()) {
                                    token = lexIterator.tokenList.get(lexIterator.curPos);
                                    if (token.getTokenType() == LexType.SEMICN) {
                                        semicn_token = lexIterator.iterator().next();
                                    } else {
                                        Parser.isSyntaxCorrect = Boolean.FALSE;
                                        CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                                        IOUtils.compileErrors.add(error);
                                    }
                                } else {
                                    Parser.isSyntaxCorrect = Boolean.FALSE;
                                    CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                                    IOUtils.compileErrors.add(error);
                                }
                            }
                        }
                    }
                }
            } else if (token.getTokenType() == LexType.RETURNTK) {
                chosen_plan = 7;
                return_token = lexIterator.iterator().next();

                if (isExp()) {
                    hasReturnExp = Boolean.TRUE;
                    exp = new Exp();
                    exp.unitParser();
                }

                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.tokenList.get(lexIterator.curPos);
                    if (token.getTokenType() == LexType.SEMICN) {
                        semicn_token = lexIterator.iterator().next();
                    } else {
                        Parser.isSyntaxCorrect = Boolean.FALSE;
                        CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                        IOUtils.compileErrors.add(error);
                    }
                } else {
                    Parser.isSyntaxCorrect = Boolean.FALSE;
                    CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                    IOUtils.compileErrors.add(error);
                }
            } else if (token.getTokenType() == LexType.BREAKTK
                        || token.getTokenType() == LexType.CONTINUETK) {
                chosen_plan = 6;
                break_continue_token = lexIterator.iterator().next();

                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.tokenList.get(lexIterator.curPos);
                    if (token.getTokenType() == LexType.SEMICN) {
                        semicn_token = lexIterator.iterator().next();
                    } else {
                        Parser.isSyntaxCorrect = Boolean.FALSE;
                        CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                        IOUtils.compileErrors.add(error);
                    }
                } else {
                    Parser.isSyntaxCorrect = Boolean.FALSE;
                    CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                    IOUtils.compileErrors.add(error);
                }
            } else { // 注意由于Exp也能判断出LVal，所以先公共把LVal拿出来
                // 只有单个等式也算是Exp？
                // 剩余的情况仅需判断当前需要解析的Stmt是LVal和Exp：LVal开头的推导右式后面都跟着=！
                if (isLValAssign()) {
                    // 说明先遇到=
                    lVal = new LVal();
                    lVal.unitParser();

                    assign_token = lexIterator.iterator().next(); // 在isLValAssign中分析了，存在=

                    if (isExp()) {
                        chosen_plan = 1;

                        exp = new Exp();
                        exp.unitParser();

                        /*if (lexIterator.iterator().hasNext()) {
                            token = lexIterator.tokenList.get(lexIterator.curPos);
                            if (token.getTokenType() == LexType.SEMICN) {
                                semicn_token = lexIterator.iterator().next();
                            } else {
                                Parser.isSyntaxCorrect = Boolean.FALSE;
                                CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                                IOUtils.compileErrors.add(error);
                            }
                        } else {
                            Parser.isSyntaxCorrect = Boolean.FALSE;
                            CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                            IOUtils.compileErrors.add(error);
                        }*/
                    } else if (isInputFunc()) {
                        chosen_plan = 8; // 8和9一样的打印思路
                        inputFunc_token = lexIterator.iterator().next();

                        if (lexIterator.iterator().hasNext()) {
                            token = lexIterator.tokenList.get(lexIterator.curPos);
                            if (token.getTokenType() == LexType.LPARENT) {
                                lParent_token = lexIterator.iterator().next();

                                if (lexIterator.iterator().hasNext()) {
                                    token = lexIterator.tokenList.get(lexIterator.curPos);
                                    if (token.getTokenType() == LexType.RPARENT) {
                                        rParent_token = lexIterator.iterator().next();

                                        if (lexIterator.iterator().hasNext()) {
                                            token = lexIterator.tokenList.get(lexIterator.curPos);
                                            if (token.getTokenType() == LexType.SEMICN) {
                                                semicn_token = lexIterator.iterator().next();
                                            } else {
                                                Parser.isSyntaxCorrect = Boolean.FALSE;
                                                CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                                                IOUtils.compileErrors.add(error);
                                            }
                                        } else {
                                            Parser.isSyntaxCorrect = Boolean.FALSE;
                                            CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                                            IOUtils.compileErrors.add(error);
                                        }

                                        return; // 有右括号就得单独分析';'再直接return
                                    } else {
                                        Parser.isSyntaxCorrect = Boolean.FALSE;
                                        CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                                        IOUtils.compileErrors.add(error);
                                    }
                                } else {
                                    Parser.isSyntaxCorrect = Boolean.FALSE;
                                    CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                                    IOUtils.compileErrors.add(error);
                                }
                            }
                        }
                    }
                } else {
                    chosen_plan = 2;
                    // 即为[ Exp ] ; 情况
                    if (isExp()) {
                        exp = new Exp();
                        exp.unitParser();
                    }
                }

                // 感觉stmt最后的分号可以统一分析
                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.tokenList.get(lexIterator.curPos);
                    if (token.getTokenType() == LexType.SEMICN) {
                        semicn_token = lexIterator.iterator().next();
                    } else {
                        Parser.isSyntaxCorrect = Boolean.FALSE;
                        CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                        IOUtils.compileErrors.add(error);
                    }
                } else {
                    // 不可能是空白吧））下一个token就是}了？如果没有Exp是不是没有；也不用报错
                    // 但是为什么能进Stmt分析
                    Parser.isSyntaxCorrect = Boolean.FALSE;
                    CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackSemiCN);
                    IOUtils.compileErrors.add(error);
                }
            }
        }
    }

    @Override
    public void print() {
        if (chosen_plan == null) {
            return;
        }
        switch (chosen_plan) {
            case 1:
                if (lVal != null)
                    lVal.print();
                if (assign_token != null)
                    IOUtils.writeCorrectLine(assign_token.toString());
                if (exp != null)
                    exp.print();
                if (semicn_token != null)
                    IOUtils.writeCorrectLine(semicn_token.toString());
                break;
            case 2:
                if (exp != null)
                    exp.print();
                if (semicn_token != null)
                    IOUtils.writeCorrectLine(semicn_token.toString());
                break;
            case 3:
                if (block != null) {
                    block.print();
                }
                break;
            case 4:
                if (if_token != null) {
                    IOUtils.writeCorrectLine(if_token.toString());
                }
                if (lParent_token != null) {
                    IOUtils.writeCorrectLine(lParent_token.toString());
                }
                if (cond != null) {
                    cond.print();
                }
                if (rParent_token != null) {
                    IOUtils.writeCorrectLine(rParent_token.toString());
                }
                if (ifStmt != null) {
                    ifStmt.print();
                }
                if (hasElse) {
                    if (else_token != null) {
                        IOUtils.writeCorrectLine(else_token.toString());
                    }
                    if (elseStmt != null) {
                        elseStmt.print();
                    }
                }
                break;
            case 5:
                if (for_token != null) {
                    IOUtils.writeCorrectLine(for_token.toString());
                }
                if (lParent_token != null) {
                    IOUtils.writeCorrectLine(lParent_token.toString());
                }
                if (forStmt1 != null) {
                    forStmt1.print();
                }
                if (semicn1 != null) {
                    IOUtils.writeCorrectLine(semicn1.toString());
                }
                if (cond != null) {
                    cond.print();
                }
                if (semicn2 != null) {
                    IOUtils.writeCorrectLine(semicn2.toString());
                }
                if (forStmt2 != null) {
                    forStmt2.print();
                }
                if (rParent_token != null) {
                    IOUtils.writeCorrectLine(rParent_token.toString());
                }
                if (stmt != null) {
                    stmt.print();
                }
                break;
            case 6:
                if (break_continue_token != null)
                    IOUtils.writeCorrectLine(break_continue_token.toString());
                if (semicn_token != null)
                    IOUtils.writeCorrectLine(semicn_token.toString());
                break;
            case 7:
                if (return_token != null)
                    IOUtils.writeCorrectLine(return_token.toString());
                if (hasReturnExp) {
                    if (exp != null)
                        exp.print();
                }
                if (semicn_token != null)
                    IOUtils.writeCorrectLine(semicn_token.toString());
                break;
            case 8:
            case 9:
                if (lVal != null)
                    lVal.print();
                if (assign_token != null)
                    IOUtils.writeCorrectLine(assign_token.toString());
                if (inputFunc_token != null)
                    IOUtils.writeCorrectLine(inputFunc_token.toString());
                if (lParent_token != null)
                    IOUtils.writeCorrectLine(lParent_token.toString());
                if (rParent_token != null)
                    IOUtils.writeCorrectLine(rParent_token.toString());
                if (semicn_token != null)
                    IOUtils.writeCorrectLine(semicn_token.toString());
                break;
            case 10:
                if (print_token != null)
                    IOUtils.writeCorrectLine(print_token.toString());
                if (lParent_token != null)
                    IOUtils.writeCorrectLine(lParent_token.toString());
                if (string_token != null)
                    IOUtils.writeCorrectLine(string_token.toString());
                if (hasPrintExp) {
                    if (!comma_exp_list.isEmpty()) {
                        for (InitVal.Comma_Exp comma_exp: comma_exp_list) {
                            comma_exp.print();
                        }
                    }
                }
                if (rParent_token != null)
                    IOUtils.writeCorrectLine(rParent_token.toString());
                if (semicn_token != null)
                    IOUtils.writeCorrectLine(semicn_token.toString());
                break;
        }

        IOUtils.writeCorrectLine(toString());
    }

    public static void main(String[] args) {
//        Integer in = null;
        Integer in = null;
        if (in == null) {
            System.out.println("POP");
        }
        in = 1;
        if (in != null) {
            System.out.println("NON");
        }
        switch (in) {
            case 0:
                System.out.println("0");
            case 1:
                System.out.println("1");
            case 2:
                System.out.println("2");
                break;
            case 3:
                System.out.println("3");
                break;
        }
    }
}
