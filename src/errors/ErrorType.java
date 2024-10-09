package errors;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/9/23 23:45
 */
public enum ErrorType {
    IllegalSymbol('a'),
    ReIdentify('b'),
    UndefinedIdent('c'),
    FuncParamsNumMismatch('d'),
    FuncParamTypeMismatch('e'),
    ReturnValueInVoidFunc('f'),
    FuncLackReturn('g'),
    ConstChange('h'),
    LackSemiCN('i'), /*lack;*/
    LackRPARENT('j'), /*lack)*/
    LackRBRACK('k'), /*lack]*/
    FormatMismatchInPrintf('l'),
    MisuseBCInNonCircle('m');

    private Character errorTypeCode;
    private Integer lineNum;

    ErrorType() {}

    ErrorType(char errorTypeCode) {
        this.errorTypeCode = errorTypeCode;
    }

    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }

    public Character getErrorTypeCode() {
        return errorTypeCode;
    }

    public Integer getLineNum() {
        return lineNum;
    }
}
