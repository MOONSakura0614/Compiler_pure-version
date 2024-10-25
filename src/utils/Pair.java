package utils;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/10/24 12:04
 */
public class Pair<T, T1> {
    public String s1;
    public String s2;

    public Pair(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public T1 getKey() {
        return (T1) s1;
    }

    public T1 getValue() {
        return (T1) s2;
    }
}
