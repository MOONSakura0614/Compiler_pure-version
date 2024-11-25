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
        switch (this) {
            case GETCHAR8 -> {
                irValue = new IRFunction("getchar",
                        new IRFunctionType(IRIntType.intType, new ArrayList<>()), new ArrayList<>()); // 空参
                ioFuncSym = new Symbol("getchar", SymbolType.CharFunc, irValue);
                this.ioFuncValue = irValue;
                break;
            }
            case GETINT32 -> {
                irValue = new IRFunction("getint",
                        new IRFunctionType(IRIntType.intType, new ArrayList<>()), new ArrayList<>()); // 空参
                ioFuncSym = new Symbol("getint", SymbolType.IntFunc, irValue);
                this.ioFuncValue = irValue;
            }
            case PUT_INT_32 -> {
                arg_list.add(IRIntType.intType);
                irValue = new IRFunction("putint",
                        new IRFunctionType(IRVoidType.voidType, arg_list), arg_list); // i32参
                ioFuncSym = new Symbol("putint", SymbolType.IntFunc, irValue);
                this.ioFuncValue = irValue;
            }
            case PUT_CH -> {
                arg_list.add(IRIntType.intType);
                irValue = new IRFunction("putch",
                        new IRFunctionType(IRVoidType.voidType, arg_list), arg_list); // i32参
                ioFuncSym = new Symbol("putch", SymbolType.IntFunc, irValue);
                this.ioFuncValue = irValue;
            }
            case PUT_STR -> {
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

    @Override
    public String toString() {
        return this.content;
    }
}
