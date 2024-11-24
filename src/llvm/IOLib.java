package llvm;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/13 22:54
 */
public enum IOLib {
    // input
    GETINT32("declare i32 @getint()"),
    GETCHAR8("declare i32 @getchar()"),
    // output
    PUT_INT_32("declare void @putint(i32)"),
    PUT_CH("declare void @putch(i32)"),
    PUT_STR("declare void @putstr(i8*)");
    private String content;

    IOLib(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return this.content;
    }
}
