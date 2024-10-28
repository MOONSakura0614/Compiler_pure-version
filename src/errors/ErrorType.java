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
    MisuseBCInNonLoop('m');

    private Character errorTypeCode;
    private Integer lineNum;

    ErrorType() {}

    ErrorType(char errorTypeCode) {
        this.errorTypeCode = errorTypeCode;
    }
    // 这边只记录了错误码，是不是把错误原因也保留下来会更好，便于后面的语义分析？
    // TODO: 2024/10/11 或者说是在CompileError类中添加具体的错误原因 

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
