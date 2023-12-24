package Error;

import frontend.ASTNode.*;
import frontend.tokens.Token;
import frontend.tokens.TokenCode;
import frontend.Symbol.FuncSymbol;
import frontend.Symbol.Symbol;
import frontend.Symbol.SymbolTable;
import frontend.Symbol.VarSymbol;

import java.util.ArrayList;
import java.util.Comparator;

/*静态类*/
public class ErrorHandler {
    private static final ErrorHandler errorHandler = new ErrorHandler(); /*单例模式*/

    private static ArrayList<Error> errors = new ArrayList<>(); //记录errors
    public static ErrorHandler getInstance() {
        return errorHandler;
    }

    public static void addError(Error error) {
        errors.add(error);
    }

    public static Boolean handleAError(Token formatString) {
        // 非法符号
        String s = formatString.getValue();
        s = s.substring(1,s.length()-1);
        boolean hasError = false; //判断是否存在A类问题
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 92 && (i == s.length() - 1|| s.charAt(i+1) != 'n')) {
                hasError = true;
            } else if (c != 32 && c != 33 && !(c >= 40 && c <= 126)) { //不是NormalChar
                if (c == '%' && i + 1 < s.length() && s.charAt(i+1) == 'd') { //是FormatChar
                    i++;
                    continue;
                } else {
                    hasError = true;
                    break;
                }
            }

        }
        if (hasError) {
            Error error = new Error(formatString.getLineNumber(), ErrorType.a);
            addError(error);
            return true;
        } else {
            return false;
        }
    }
    public static Boolean handleBError(SymbolTable currentSymbolTable, Token token) {
        //名字重定义
        // token 为 Ident
        String name = token.getValue();
        if (currentSymbolTable.getSymbols().containsKey(name)) {
            Error error = new Error(token.getLineNumber(), ErrorType.b);
            addError(error);
            return true;
        }
        return false;
    }
    public static Boolean handleCError(SymbolTable currentSymbolTable,Token token) {
        //未定义的名字
        String name = token.getValue();
        if (currentSymbolTable.getSymbols().containsKey(name)) {
            return false;
        }
        SymbolTable parentTable = currentSymbolTable.getParentTable();
        while (parentTable != null) {
            if (parentTable.getSymbols().containsKey(name)) {
                return false;
            }
            parentTable = parentTable.getParentTable();
        }
        /*没找到定义的*/
        Error error = new Error(token.getLineNumber(),ErrorType.c);
        addError(error);
        return true;
    }

    public static Boolean handleDError(FuncRParamsNode funcRParamsNode, Token ident, SymbolTable currentSymbolTable) {
        //函数参数个数不匹配
        int number1;
        if (funcRParamsNode == null) {
            number1 = 0;
        } else {
            number1 = funcRParamsNode.getExps().size();          //实参个数
        }
        String funcName = ident.getValue();
        FuncSymbol funcSymbol = (FuncSymbol) currentSymbolTable.getFuncSymbol(funcName);
        if (funcSymbol == null) {
            return  false;
        }
        int number2 = funcSymbol.getParamTypeList().size();  //形参个数
        if (number1 != number2) {
            Error error = new Error(ident.getLineNumber(), ErrorType.d);
            addError(error);
            return true;
        } else {
            return false;
        }
    }

    public static Boolean handleEError(FuncRParamsNode funcRParamsNode, Token ident, SymbolTable currentSymbolTable) {
        /*函数参数类型不匹配错误*/
        /*参数可能的情况：整数(0)、一维数组(1)、二维数组(2)、数组的部分维度、void(-1)*/
        /*funcRParams是实参*/
        ArrayList<ExpNode> realExpNodes = funcRParamsNode.getExps();
        String funcName = ident.getValue();
        /*Token是调用函数的函数名*/
        SymbolTable tempTable = currentSymbolTable;
        /*编译时保存的函数声明信息*/
        FuncSymbol funcSymbol = (FuncSymbol) tempTable.getFuncSymbol(funcName);
        if (funcSymbol == null) {
            return false;
        }
        /*形参维度信息*/
        ArrayList<Symbol> paramTypeList = funcSymbol.getParamTypeList();

        int paramNumber  = paramTypeList.size();
        for (int i = 0; i < paramNumber; i ++) {
            VarSymbol fVarSymbol = (VarSymbol) paramTypeList.get(i);   /*形参中的Symbol*/
            ExpNode realExpNode = realExpNodes.get(i);              /*实参中的表达式*/
            int realExpDimension = realExpNode.getDimension(currentSymbolTable); /*实参的维度，a[1] - > 1*/
            if (realExpDimension == -1 ) { /*代表传入的参数是函数*/
                Token func = realExpNode.getAddExpNode().getMulExpNode().getUnaryExpNode().getIdent();
                /*获取函数的类型*/
                FuncSymbol funcSymbol1 = (FuncSymbol)currentSymbolTable.getFuncSymbol(func.getValue());
                int rettype = funcSymbol1.getRetype();
                if (rettype == 0) { //返回值int类型的函数
                    realExpDimension = 0;
                } else {            //返回值为void类型
                    Error error = new Error(ident.getLineNumber(), ErrorType.e);
                    addError(error);
                    return true;
                }
            }
            int fVarDimension = fVarSymbol.getDimension(); /*形参的维度 a[] -> 1 , a-> 0*/
            if (realExpDimension != fVarDimension) {
                /*realExp.getDimension()是实参的维度 = 声明维度 - 调用维度*/
                /*fVarSymbol.getDimension()是形参的维度*/
                Error error = new Error(ident.getLineNumber(), ErrorType.e);
                addError(error);
                return true;
            }
        }
        return false;
    }

    public static Boolean handleFError(Token funcType, BlockNode blockNode) {
        String type = funcType.getValue();
        if (type.equals("int")) { //int 类型函数
            return false;
        } else { //void 类型函数
            StmtNode stmtNode = blockNode.getLastStmt();
            if (stmtNode == null) {
                return false;
            }
            Token returnToken = stmtNode.getReturnToken();
            ExpNode expNode = stmtNode.getExpNode();
            if (returnToken != null && expNode != null) { //都不为null
                Error error = new Error(returnToken.getLineNumber(),ErrorType.f);
                addError(error);
                return true;
            } else {
                return false;
            }
        }
    }
    public static Boolean handleGError(Token funcType, BlockNode blockNode) {
        String type = funcType.getValue();
        if (type.equals("void")) { //void 类型函数
            return false;
        } else { //int 类型函数
            StmtNode stmtNode = blockNode.getLastStmt();
            if (stmtNode == null) { //最后一个甚至不是stmt，而是decl
                Error error = new Error(blockNode.getRightBrace().getLineNumber(), ErrorType.g);
                addError(error);
                return true;
            }
            Token returnToken = stmtNode.getReturnToken();
            ExpNode expNode = stmtNode.getExpNode();
            if (returnToken != null && expNode != null) {
                return false;
            } else {
                Error error = new Error(blockNode.getRightBrace().getLineNumber(), ErrorType.g);
                addError(error);
                return true;
            }
        }
    }

    public static Boolean handleHError(Token ident, SymbolTable currentSymbolTable) {
        String varName = ident.getValue();
        VarSymbol varSymbol = (VarSymbol) currentSymbolTable.getVarSymbol(varName);
        if (varSymbol == null) {
            return false;
        }
        int isConst = varSymbol.getCon();
        if (isConst == 1) { //是const
            Error error = new Error(ident.getLineNumber(),ErrorType.h);
            addError(error);
            return true;
        } else {
            return false;
        }
    }

    public static void handleIError(ArrayList<Token> tokens,int index) {
        //缺少分号
        Token token = tokens.get(index);
        if (!token.getTokenCode().equals(TokenCode.SEMICN)) {
            Error error = new Error(tokens.get(index - 1).getLineNumber(),ErrorType.i);
            addError(error);
        }
    }

    public static void handleJError(ArrayList<Token> tokens,int index) {
        //缺少右小括号’)’
        Token token = tokens.get(index);
        if (!token.getTokenCode().equals(TokenCode.RPARENT)) {
            Error error = new Error(tokens.get(index - 1).getLineNumber(),ErrorType.j);
            addError(error);
        }
    }

    public static void handleKError(ArrayList<Token> tokens,int index) {
        //缺少右中括号’]’
        Token token = tokens.get(index);
        if (!token.getTokenCode().equals(TokenCode.RBRACK)) {
            Error error = new Error(tokens.get(index - 1).getLineNumber(),ErrorType.k);
            addError(error);
        }
    }

    public static Boolean handleLError(Token formatString,Token printToken, ArrayList<ExpNode> expNodes) {
        //printf中格式字符与表达式个数不匹配
        // exps中的变量个数
        int number1 = expNodes.size();
        // formatString中%d的个数
        String s = formatString.getValue();
        String [] spiltedS = s.split("%d");
        int number2 = spiltedS.length - 1;
        if (number1 != number2) {
            Error error = new Error(printToken.getLineNumber(),ErrorType.l);
            addError(error);
            return true;
        }
        return false;
    }

    public static Boolean handleMError(Token breakAndContinueToken, int loopLevel) {
        if (loopLevel == 0) {
            Error error = new Error(breakAndContinueToken.getLineNumber(), ErrorType.m);
            addError(error);
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        errors.sort(new Comparator<Error>() {
            @Override
            public int compare(Error o1, Error o2) {
                return o1.getLineNumber() - o2.getLineNumber();
            }
        });
        for (Error error : errors) {
            sb.append(error.toString());
        }
        return sb.toString();
    }
}
