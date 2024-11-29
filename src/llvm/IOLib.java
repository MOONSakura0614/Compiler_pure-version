package llvm;

import frontend.symbol.Symbol;
import frontend.symbol.SymbolType;
import llvm.type.*;
import llvm.value.IRFunction;
import llvm.value.IRValue;

import java.util.ArrayList;

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
    private IRValue ioFuncValue;
    private Symbol ioFuncSym;

    IOLib(String content) {
        // 这个地方是外联的库函数，所以形参表只说明类型，没有%1之类的虚拟寄存器占位
        this.content = content;
        IRValue irValue;
        ArrayList<IRType> arg_list = new ArrayList<>();
        switch (content) {
//            case GETCHAR8 -> {
            case "declare i32 @getchar()" -> {
                irValue = new IRFunction("getchar",
                        new IRFunctionType(IRIntType.intType, new ArrayList<>()), new ArrayList<>()); // 空参
                ioFuncSym = new Symbol("getchar", SymbolType.CharFunc, irValue);
                this.ioFuncValue = irValue;
            }
            case "declare i32 @getint()" -> {
                irValue = new IRFunction("getint",
                        new IRFunctionType(IRIntType.intType, new ArrayList<>()), new ArrayList<>()); // 空参
                ioFuncSym = new Symbol("getint", SymbolType.IntFunc, irValue);
                this.ioFuncValue = irValue;
            }
            case "declare void @putint(i32)" -> {
                arg_list.add(IRIntType.intType);
                irValue = new IRFunction("putint",
                        new IRFunctionType(IRVoidType.voidType, arg_list), arg_list); // i32参
                ioFuncSym = new Symbol("putint", SymbolType.IntFunc, irValue);
                this.ioFuncValue = irValue;
            }
            case "declare void @putch(i32)" -> {
                arg_list.add(IRIntType.intType);
                irValue = new IRFunction("putch",
                        new IRFunctionType(IRVoidType.voidType, arg_list), arg_list); // i32参
                ioFuncSym = new Symbol("putch", SymbolType.IntFunc, irValue);
                this.ioFuncValue = irValue;
            }
            case "declare void @putstr(i8*)" -> {
                arg_list.add(IRPointerType.i8PointType);
                // ArrayList.add()方法返回的是boolean，所以需要在外面构建list
                irValue = new IRFunction("putstr",
                        new IRFunctionType(IRVoidType.voidType, arg_list), arg_list); // i8*参
                // 注意记录形参
                ioFuncSym = new Symbol("putstr", SymbolType.IntFunc, irValue);
                this.ioFuncValue = irValue;
            }
        }
    }

    public IRValue getIoFuncValue() {
        return ioFuncValue;
    }

    public Symbol getIoFuncSym() { // 获取函数符号，用于CompUnit中第一次加入符号表
        return ioFuncSym;
    }

    public String getContent() {
        return content;
    }

    public void setIoFuncValue(IRValue ioFuncValue) {
        this.ioFuncValue = ioFuncValue;
    }

    public void setIoFuncSym(Symbol ioFuncSym) {
        this.ioFuncSym = ioFuncSym;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean match(Symbol funcSym) {
        String name = funcSym.getIdentName();
        return false;
    }

    @Override
    public String toString() {
        return this.content;
    }

    public static void main(String[] args) {
        System.out.println(IRIntType.intType);
        System.out.println(new IRFunctionType(IRIntType.intType, new ArrayList<>()));
        System.out.println(new IRFunctionType(IRIntType.intType, new ArrayList<>()).getRet_type());
        System.out.println(GETCHAR8.ioFuncValue.getIrType());
        // 这个时候ret_type还是无法成为有重写toString方法的intType（不知道为什么）--所以紧急在IRGenerator里改了
        // 显示的是一个普通的irType，哈希地址
        System.out.println(PUT_INT_32.ioFuncValue.getIrType());
//        System.out.println((IRIntType) (((IRFunctionType) GETCHAR8.ioFuncValue.getIrType()).getRet_type())); // 显示类型不对
    }
}
