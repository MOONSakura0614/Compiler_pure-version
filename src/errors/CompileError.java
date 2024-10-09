package errors;

import errors.ErrorType;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/9/25 22:47
 */
public class CompileError {
    private int lineNum;
    private ErrorType errorType;

    public CompileError(int lineNum, ErrorType errorType) {
        this.lineNum = lineNum;
        this.errorType = errorType;
    }

    public int getLineNum() {
        return lineNum;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
