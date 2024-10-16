package frontend.lexer;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/9/24 9:18
 */
public enum LexType {
    IDENFR, /*Ident:标识符，用户定义可能多种情况*/
    INTCON, /*IntConst:常量区，也是无数可能，同理下面两种常量*/
    STRCON, /*StringConst*/
    CHRCON, /*CharConst*/
    MAINTK, /*main*/
    CONSTTK, /*const*/
    INTTK, /*int*/
    CHARTK, /*char*/
    BREAKTK, /*break*/
    CONTINUETK, /*continue*/
    IFTK, /*if*/
    ELSETK, /*else*/
    NOT, /*!*/
    AND, /*&&*/
    OR, /*||*/
    FORTK, /*for*/
    GETINTTK,
    GETCHARTK,
    PRINTFTK,
    RETURNTK,
    PLUS,
    MINU,
    VOIDTK,
    MULT,
    DIV,
    MOD, /*%*/
    LSS, /*<*/
    LEQ, /*<=*/
    GRE, /*>*/
    GEQ, /*>=*/
    EQL, /*==*/
    NEQ, /*!=*/
    ASSIGN, /*=*/
    SEMICN, /*;*/
    COMMA, /*,*/
    LPARENT, /*(*/
    RPARENT, /*)*/
    LBRACK, /*[*/
    RBRACK, /*]*/
    LBRACE, /*{*/
    RBRACE; /*}*/

    public static void main(String[] args) {
        System.out.println(PRINTFTK);
    }
}
