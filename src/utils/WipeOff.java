package utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * @author 郑悦
 * @Description: 文法有害规则清除
 * @date 2024/10/24 12:04
 */
public class WipeOff {
    private LinkedList<String> Vn = new LinkedList<>();
    private LinkedList<String> Vt = new LinkedList<>();
    private LinkedList<Pair<String, String>> P = new LinkedList<>();
    private HashMap<String, Boolean> map = new HashMap<>();
    private String Dad;

    private void removeSelf() {
        P.removeIf(p -> {
            if (p.getKey().equals(p.getValue())) {
                System.out.println("remove: " + p.getKey() + " ::= " + p.getValue());
                return true;
            }
            return false;
        });
    }


    private void removeInactive() {
        LinkedList<String> inActive = new LinkedList<>(Vn);
        // 扫描规则找出不活动字符
        for (int j = 0; j < P.size(); j++) {
            for (Pair<String, String> p : P) {
                String k = p.getKey();
                if (map.get(k)) {
                    continue;
                }
                String str = p.getValue();
                int length = str.length();
                boolean flag = true;
                for (int i = 0; i < length; i++) {
                    if (!map.get(str.substring(i, i + 1))) {
                        flag = false;
                        break;
                    }
                }
                map.replace(p.getKey(), flag);
                if (flag) {
                    inActive.remove(p.getKey());
                }
            }
        }
        // 删除含有不活动字符的规则
        P.removeIf(p -> {
            if (inActive.contains(p.getKey())) {
                System.out.println("remove: " + p.getKey() + " ::= " + p.getValue());
                return true;
            }
            for (int i = 0; i < p.getValue().length(); i++) {
                if (inActive.contains(p.getValue().substring(i, i + 1))) {
                    System.out.println("remove: " + p.getKey() + " ::= " + p.getValue());
                    return true;
                }
            }
            return false;
        });
    }

    private void removeUnreachable() {
        Iterator<Pair<String, String>> iterator = P.iterator();
        P.removeIf(p -> {
            if(p.getKey().equals(Dad)) {
                return false;
            }
            boolean flag = false;
            for (Pair<String, String> p2 : P) {
                if (p2.getValue().contains(p.getKey())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                System.out.println("remove: " + p.getKey() + " ::= " + p.getValue());
                return true;
            }
            return false;
        });
    }

    public static void main(String[] args) throws IOException {
        // 输入重定向
        System.setIn(new FileInputStream("in2.txt"));

        WipeOff wipeOff = new WipeOff();
        Scanner sc = new Scanner(System.in);
        // input Dad
        System.out.println("Please input Dad");
        wipeOff.Dad = sc.nextLine();

        // input the List of Vn
        System.out.println("Please input the list of Vn");
        List<String> list = Arrays.asList(sc.nextLine().split(" "));
        wipeOff.Vn = new LinkedList<>(list);
        Iterator<String> iterator = wipeOff.Vn.iterator();
        while (iterator.hasNext()) {
            wipeOff.map.put(iterator.next(), false);
        }

        // input the List of Vt
        System.out.println("Please input the list of Vt");
        list = Arrays.asList(sc.nextLine().split(" "));
        wipeOff.Vt = new LinkedList<>(list);
        iterator = wipeOff.Vt.iterator();
        while (iterator.hasNext()) {
            wipeOff.map.put(iterator.next(), true);
        }

        // input the num of P
        System.out.println("Please input the num of P");
        int numRules = sc.nextInt();
        // input the list of P
        String[] str;
        System.out.println("Please input the list of P");
        sc.nextLine();
        while (numRules-- > 0) {
            str = sc.nextLine().split(" ");
            wipeOff.P.push(new Pair<>(str[0], str[1]));
        }

        wipeOff.removeSelf();
        // 去除不活动字符
        wipeOff.removeInactive();
        // 去除不可达字符
        wipeOff.removeUnreachable();

        System.setOut(new PrintStream(new FileOutputStream("out.txt")));
        for (Pair<String, String> p : wipeOff.P) {
            System.out.println(p.getKey()+" ::= "+p.getValue());
        }
    }
}