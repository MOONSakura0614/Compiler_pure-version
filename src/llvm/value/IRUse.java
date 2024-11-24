package llvm.value;

/**
 * @author 郑悦
 * @Description: 使用关系
 * @date 2024/11/18 21:00
 */
public class IRUse {
    private IRValue value; // 被用
    private IRUser user; // 使用者
    private int pos; // pos表示该value是user的第几个操作数

    public IRUse() {}

    public IRUse(IRUser user, IRValue value) {
        this.user = user;
        this.value = value;
    }

    public IRUse(IRUser user, IRValue value, int pos) {
        this.user = user;
        this.value = value;
        this.pos = pos;
    }

    public IRValue getValue() {
        return value;
    }

    public IRUser getUser() {
        return user;
    }

    public int getPos() {
        return pos;
    }
}
