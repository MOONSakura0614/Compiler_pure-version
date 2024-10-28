package errors;

import frontend.visitor.Visitor;
import utils.IOUtils;

/**
 * @author 郑悦
 * @Description: 主要用于语义分析中的错误处理
 * @date 2024/10/17 8:10
 */
public class ErrorHandler {
    // 之前的错误处理（词法+语法）都嵌套太深，在这边想把语义部分的错误统一处理
    public static void redefineErrorHandle(int lineNum) { // 报错行数为ident所在行数
        Visitor.isSemanticCorrect = Boolean.FALSE;
        CompileError error = new CompileError(lineNum, ErrorType.ReIdentify);
        IOUtils.compileErrors.add(error);
    }

    public static void undefineErrorHandle(int lineNum) {
        Visitor.isSemanticCorrect = Boolean.FALSE;
        CompileError error = new CompileError(lineNum, ErrorType.UndefinedIdent);
        IOUtils.compileErrors.add(error);
    }

    public static void funcParamsNumErrorHandle(int lineNum) {
        Visitor.isSemanticCorrect = Boolean.FALSE;
        CompileError error = new CompileError(lineNum, ErrorType.FuncParamsNumMismatch);
        IOUtils.compileErrors.add(error);
    }

    public static void funcParamsTypeErrorHandle(int lineNum) { // 在错误传入实参的函数调用那一行报错
        Visitor.isSemanticCorrect = Boolean.FALSE;
        CompileError error = new CompileError(lineNum, ErrorType.FuncParamTypeMismatch);
        IOUtils.compileErrors.add(error);
    }

    public static void returnExpForVoidErrorHandle(int lineNum) {
        Visitor.isSemanticCorrect = Boolean.FALSE;
        CompileError error = new CompileError(lineNum, ErrorType.ReturnValueInVoidFunc);
        IOUtils.compileErrors.add(error);
    }

    public static void funcLackReturnValueErrorHandle(int lineNum) {
        Visitor.isSemanticCorrect = Boolean.FALSE;
        CompileError error = new CompileError(lineNum, ErrorType.FuncLackReturn);
        IOUtils.compileErrors.add(error);
    }

    public static void constChangeErrorHandle(int lineNum) {
        Visitor.isSemanticCorrect = Boolean.FALSE;
        CompileError error = new CompileError(lineNum, ErrorType.ConstChange);
        IOUtils.compileErrors.add(error);
    }

    public static void printfErrorHandle(int lineNum) {
        Visitor.isSemanticCorrect = Boolean.FALSE;
        CompileError error = new CompileError(lineNum, ErrorType.FormatMismatchInPrintf);
        IOUtils.compileErrors.add(error);
    }

    public static void nonLoopErrorHandle(int lineNum) {
        Visitor.isSemanticCorrect = Boolean.FALSE;
        CompileError error = new CompileError(lineNum, ErrorType.MisuseBCInNonLoop);
        IOUtils.compileErrors.add(error);
    }
}
