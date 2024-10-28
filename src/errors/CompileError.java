package errors;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/9/25 22:47
 */
public class CompileError implements Comparable<CompileError> {
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

    @Override
    public String toString() {
        return Integer.toString(lineNum) + ' ' + errorType.getErrorTypeCode().toString() + '\n';
    }

    @Override
    public int compareTo(CompileError other) {
        return Integer.compare(this.lineNum, other.lineNum);
    }
}