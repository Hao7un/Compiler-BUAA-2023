package IR;

import IR.types.*;
import IR.values.Const.*;
import IR.values.Instructions.mem.Alloca;
import IR.values.Instructions.mem.GetElementPtr;
import IR.values.Instructions.mem.Load;
import IR.values.Instructions.terminator.Ret;
import frontend.ASTNode.*;
import IR.values.BasicBlock;
import IR.values.Value;
import IR.IRModule;
import frontend.tokens.Token;

import java.util.ArrayList;
import java.util.HashMap;

/*Visitor类的功能是遍历AST*/
public class Visitor {

    private Function curFunction = null;
    private BasicBlock curBasicBlock = null;
    private BasicBlock curTrueBlock = null;
    private BasicBlock curFalseBlock = null;
    private BasicBlock curChangeBlock = null;
    private BasicBlock curExitBlock = null;
    private boolean isBuildCall = false;
    private ArrayList<Value> curArrayElements = null;
    private boolean isTop = false;  // the top level of recurrent
    private Value curValue = null;
    private ValueType curValueType = null;
    private ArrayList<ValueType> curValueTypeList = null;

    private Boolean isConst = false;  /*代表是否需要计算为常量*/

    /*单例模式Module*/
    private final IRModule irModule = IRModule.getInstance();

    public Visitor() {
        /*Push A Symbol When Initialize*/
        pushSymbolTable();
    }

    /*----------------------Symbol Table----------------------*/
    private final ArrayList<HashMap<String, Value>> symbolTables = new ArrayList<>();

    //在符号表中查找某个ident
    private Value find(String ident) {
        int length = symbolTables.size();
        for (int i = length - 1; i >= 0; i--) {
            HashMap<String,Value> symbolTable = symbolTables.get(i);
            Value value = symbolTable.getOrDefault(ident,null);
            if (value != null) {
                return value;
            }
        }
        //在symbolTable中没找到
        return null;
    }

    private void pushSymbol(String ident, Value value) {
        int length = symbolTables.size();
        symbolTables.get(length-1).put(ident,value);
    }

    private void pushSymbolTable() {
        symbolTables.add(new HashMap<>());
    }

    private void popSymbolTable() {
        assert !symbolTables.isEmpty();
        int length = symbolTables.size();
        symbolTables.remove(length - 1);
    }

    public Boolean isGlobalDef() {
        return symbolTables.size() == 1;
    }

    /*----------------------Visitor---------------------*/
    public void visitCompUnit(CompUnitNode compUnitNode) {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        ArrayList<DeclNode> declNodes = compUnitNode.getDeclNodes();
        ArrayList<FuncDefNode> funcDefNodes = compUnitNode.getFuncDefNodes();
        MainFuncDefNode mainFuncDefNode = compUnitNode.getMainFuncDefNode();

        for (DeclNode declNode : declNodes) {
            visitDecl(declNode);
        }

        /*Lib Function*/
        IRBuilder.buildLibFunction(irModule,symbolTables);

        for (FuncDefNode funcDefNode : funcDefNodes) {
            visitFuncDef(funcDefNode);
        }
        visitMainFuncDef(mainFuncDefNode);
    }

    /*--------------------Decl & Def-------------------------------*/
    public void visitDecl(DeclNode declNode) {
        // Decl → ConstDecl | VarDecl
        ConstDeclNode constDeclNode = declNode.getConstDeclNode();
        VarDeclNode varDeclNode = declNode.getVarDeclNode();

        if (constDeclNode != null) {
            visitConstDecl(constDeclNode);
        } else {
            visitVarDecl(varDeclNode);
        }
    }

    public void visitConstDecl(ConstDeclNode constDeclNode) {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        ArrayList<ConstDefNode> constDefNodes = constDeclNode.getConstDefNodes();
        for (ConstDefNode constDefNode : constDefNodes) {
            visitConstDef(constDefNode);
        }
    }

    public void visitVarDecl(VarDeclNode varDeclNode) {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        ArrayList<VarDefNode> varDefNodes = varDeclNode.getVarDefNodes();
        for (VarDefNode varDefNode : varDefNodes) {
            visitVarDef(varDefNode);
        }
    }

    public void visitConstDef(ConstDefNode constDefNode) {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        Token ident = constDefNode.getIdent();
        ArrayList<ConstExpNode> constExpNodes = constDefNode.getConstExpNodes();
        ConstInitValNode constInitValNode = constDefNode.getConstInitValNode();
        String name = ident.getValue();

        if (constExpNodes.isEmpty()) {
            // ConstDef -> Ident '=' ConstInitVal
            isConst=true;
            visitConstInitVal(constInitValNode);
            isConst=false;
            /*new definition  push into symbol table*/
            if (isGlobalDef()) {
                /*Global const*/
                int value = Integer.parseInt(curValue.getValueName());
                GlobalVar globalVar = IRBuilder.buildGlovalVar("@"+name,new IntegerType(32),
                        new ConstInteger(32,value),true);
                pushSymbol(name,new ConstInteger(32,value)); // push into symbol table
                //Module.getInstance().addGlobalVar(globalVar);
            } else {
                /*local const*/
                //Alloca alloca = IRBuildFactory.buildAlloca(new IntegerType(32),curBasicBlock);
                int value = Integer.parseInt(curValue.getValueName());
                pushSymbol(name,new ConstInteger(32,value));
                /*Build store*/
                //IRBuildFactory.buildStore(curValue,alloca,curBasicBlock);
            }
        } else {
            // Array
            /*Get Dimension Information*/
            ArrayList<Integer> dimensions = new ArrayList<>();
            for (ConstExpNode constExpNode : constExpNodes) {
                isConst = true;
                visitConstExp(constExpNode);
                isConst = false;
                dimensions.add(Integer.parseInt(curValue.getValueName()));
            }
            ValueType arrayType = new IntegerType(32);
            for (int i = dimensions.size(); i > 0; i--) {
                arrayType = new ArrayType(arrayType, dimensions.get(i - 1));
            }
            if (isGlobalDef()) {
                /*Global Array*/
                curArrayElements = new ArrayList<>(); //refresh
                isTop = true;
                isConst = true;
                visitConstInitVal(constInitValNode);
                isConst = false;
                ConstArray constArray = new ConstArray(arrayType,curArrayElements);
                GlobalVar globalVar = IRBuilder.buildGlovalVar("@"+name,arrayType,constArray,true);
                pushSymbol(name,globalVar);
                IRModule.getInstance().addGlobalVar(globalVar);
            } else {
                /*Local Array*/
                /*Build Alloc*/
                Alloca alloca = IRBuilder.buildAlloca(arrayType,curBasicBlock);
                pushSymbol(name,alloca);
                curArrayElements = new ArrayList<>(); //refresh
                isTop = true;
                isConst = false;
                visitConstInitVal(constInitValNode);

                /*Build GetElementPtr and Store*/
                ArrayList<Value> constArray = new ArrayList<>();
                for (Value value : curArrayElements) {
                    if (value.getValueType() instanceof IntegerType) {
                        constArray.add(value);
                    } else {
                        for (Value  value1 : ((ConstArray)value).getOperands()) {
                            constArray.add(value1);
                        }
                    }
                }
                IRBuilder.constantArrayIndex = 0;
                IRBuilder.buildLocalArray(constArray,alloca,dimensions,curBasicBlock,1);
            }
        }
    }

    public void visitConstInitVal(ConstInitValNode constInitValNode) {
        //ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        ConstExpNode constExpNode = constInitValNode.getConstExpNode();
        if (constExpNode != null) {
            visitConstExp(constExpNode);
        } else {
            /*need recursion*/
            boolean curIsTop;
            if (isTop) {
                curIsTop = true;
                isTop = false;
            } else {
                curIsTop = false;
            }
            ArrayList<Value> values = new ArrayList<>();
            ArrayList<ConstInitValNode> constInitValNodes = constInitValNode.getConstInitValNodes();
            int size = 0;
            for (ConstInitValNode constInitValNode1 : constInitValNodes) {
                visitConstInitVal(constInitValNode1);
                values.add(curValue);
                size ++;
            }
            if (curIsTop) {
                curArrayElements = values;
            } else {
                curValue = new ConstArray(new ArrayType(new IntegerType(32),size),values);
            }
        }
    }

    public void visitVarDef(VarDefNode varDefNode) {
        // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        Token ident = varDefNode.getIdent();
        String name = ident.getValue();
        ArrayList<ConstExpNode> constExpNodes = varDefNode.getConstExpNodes();
        InitValNode initValNode = varDefNode .getInitValNode();

        if (constExpNodes.isEmpty()) {
            if (initValNode == null) {
                if (isGlobalDef()) {
                    /*无初始值 & 全局*/
                    GlobalVar globalVar = IRBuilder.buildGlovalVar("@"+name,new IntegerType(32),
                            new ConstInteger(32,0),false);
                    pushSymbol(name,globalVar); // push into symbol table
                    IRModule.getInstance().addGlobalVar(globalVar);
                } else {
                    /*无初始值 & 局部*/
                    Alloca alloca = IRBuilder.buildAlloca(new IntegerType(32),curBasicBlock);
                    pushSymbol(name,alloca);
                }
            } else {
                if (isGlobalDef()) {
                    /*有初始值 & 全局*/
                    isConst =true;
                    visitInitVal(initValNode);
                    isConst = false;
                    int value = Integer.parseInt(curValue.getValueName());
                    GlobalVar globalVar = IRBuilder.buildGlovalVar("@"+name,new IntegerType(32),new ConstInteger(32,value),false);
                    pushSymbol(name,globalVar); // push into symbol table
                    IRModule.getInstance().addGlobalVar(globalVar);
                } else {
                    /*有初始值 & 局部*/
                    Alloca alloca = IRBuilder.buildAlloca(new IntegerType(32),curBasicBlock);
                    pushSymbol(name,alloca);

                    /*Build store*/
                    visitInitVal(initValNode);
                    IRBuilder.buildStore(curValue,alloca,curBasicBlock);
                }
            }

        } else {
            /* Array */
            /*Get Dimension Information*/
            ArrayList<Integer> dimensions = new ArrayList<>();
            for (ConstExpNode constExpNode : constExpNodes) {
                isConst = true;
                visitConstExp(constExpNode);
                isConst = false;
                dimensions.add(Integer.parseInt(curValue.getValueName()));
            }
            ValueType arrayType = new IntegerType(32);
            for (int i = dimensions.size(); i > 0; i--) {
                arrayType = new ArrayType(arrayType, dimensions.get(i - 1));
            }

            if(isGlobalDef()) {
                /*Global Array*/
                curArrayElements = new ArrayList<>(); //refresh
                if (initValNode != null) {
                    /*Has Initial Value*/
                    isTop = true;
                    isConst = true;
                    visitInitVal(initValNode);
                    isConst = false;
                    ConstArray constArray = new ConstArray(arrayType,curArrayElements);
                    GlobalVar globalVar = IRBuilder.buildGlovalVar("@"+name,arrayType,constArray,false);
                    pushSymbol(name,globalVar);
                    IRModule.getInstance().addGlobalVar(globalVar);
                } else {
                    /*No initial value-> set to zero*/
                    ConstArray constArray = new ConstArray(arrayType);
                    GlobalVar globalVar = IRBuilder.buildGlovalVar("@"+name,arrayType,constArray,false);
                    pushSymbol(name,globalVar);
                    IRModule.getInstance().addGlobalVar(globalVar);
                }
            } else {
                /*Local Array*/
                Alloca alloca = IRBuilder.buildAlloca(arrayType,curBasicBlock);
                pushSymbol(name,alloca);
                if (initValNode != null) {
                    curArrayElements = new ArrayList<>(); //refresh
                    isTop = true;
                    isConst = false;
                    visitInitVal(initValNode);
                    /*Build GetElementPtr and Store*/
                    ArrayList<Value> constArray = new ArrayList<>();
                    for (Value value : curArrayElements) {
                        if (value.getValueType() instanceof IntegerType) {
                            constArray.add(value);
                        } else {
                            for (Value  value1 : ((ConstArray)value).getOperands()) {
                                constArray.add(value1);
                            }
                        }
                    }
                    IRBuilder.constantArrayIndex = 0;
                    IRBuilder.buildLocalArray(constArray,alloca,dimensions,curBasicBlock,1);
                }
            }
        }
    }

    public void visitInitVal(InitValNode initValNode) {
        // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        ExpNode expNode = initValNode.getExpNode();
        if (expNode != null) {
            visitExp(expNode);
        } else {
            /*need recursion*/
                boolean curIsTop;
                if (isTop) {
                    curIsTop = true;
                    isTop = false;
                } else {
                    curIsTop = false;
                }
                ArrayList<Value> values = new ArrayList<>();
                ArrayList<InitValNode> initValNodes = initValNode.getInitValNodes();
                int size = 0;
                for (InitValNode initValNode1 : initValNodes) {
                    visitInitVal(initValNode1);
                    values.add(curValue);
                    size ++;
                }
                if (curIsTop) {
                    curArrayElements = values;
                } else {
                    curValue = new ConstArray(new ArrayType(new IntegerType(32),size),values);
                }
            }

    }

    public void visitFuncDef(FuncDefNode funcDefNode) {
        // FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
        curValueTypeList  = new ArrayList<>(); //refresh
        /*visit FuncFParams FIRST*/
        FuncFParamsNode funcFParamsNode = funcDefNode.getFuncFParamsNode();
        if (funcFParamsNode != null) {
            visitFuncFParams(funcFParamsNode);
        }

        /*Build Function*/
        String name = funcDefNode.getIdent().getValue();
        FunctionType type;
        if(funcDefNode.getFuncTypeNode().getFuncType().getValue().equals("int")) {
            type = new FunctionType(new IntegerType(32),curValueTypeList);
        } else {
            type = new FunctionType(new VoidType(),curValueTypeList);
        }
        Function function = IRBuilder.buildFunction(name, type, irModule);

        curFunction = function;
        pushSymbol(name,function);

        /*Build Basic Block*/
        BasicBlock basicBlock = IRBuilder.buildBasicBlock(function);
        curBasicBlock = basicBlock;

        pushSymbolTable();

        if (funcFParamsNode != null) {
            int cnt = 0;
            for (ValueType valueType : curValueTypeList) {
                Alloca alloca = IRBuilder.buildAlloca(valueType, curBasicBlock);
                IRBuilder.buildStore(curFunction.getArguments().get(cnt),alloca,basicBlock);
                pushSymbol(funcFParamsNode.getFuncFParams().get(cnt).getIdent().getValue(),alloca);
                cnt++;
            }
        }
        /*Visit Block*/
        BlockNode blockNode = funcDefNode.getBlockNode();
        visitBlock(blockNode);

        /* Void , if there is no return statement in the original code,
        *  add "ret void" into LLVM code
        * */
        if(((FunctionType)function.getValueType()).getReturnType() instanceof VoidType) {
            if (blockNode.getBlockItemNodes().isEmpty() ||              // No statement
                blockNode.getLastStmt() == null ||
                    blockNode.getLastStmt().getReturnToken() == null) {     // no return ;
                    IRBuilder.buildRet(curBasicBlock);
            }
        }
    }

    public void visitFuncFParams(FuncFParamsNode funcFParamsNode) {
        ArrayList<ValueType> paramTypes = new ArrayList<>();
        for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParams()) {
            visitFuncFParam(funcFParamNode);
            paramTypes.add(curValueType);
        }
        curValueTypeList = paramTypes;
    }
    public void visitFuncFParam(FuncFParamNode funcFParamNode) {
        // FuncFParam  → BType Ident ['[' ']' { '[' ConstExp ']' }]
        if(funcFParamNode.getLeftBracks().isEmpty()) {
            // not array
            curValueType = new IntegerType(32);
        } else {
            ArrayList<ConstExpNode> constExpNodes = funcFParamNode.getConstExpNodes();
            ValueType valueType = new IntegerType(32);
            for (int i = constExpNodes.size() - 1; i>=0 ; i--) {
                isConst = true;
                visitConstExp(constExpNodes.get(i));
                isConst = false;
                valueType = new ArrayType(valueType,Integer.parseInt(curValue.getValueName()));
            }
            curValueType = new PointerType(valueType);
        }
    }

    public void visitMainFuncDef(MainFuncDefNode mainFuncDefNode) {
        // MainFuncDef → 'int' 'main' '(' ')' Block

        /*Build Function*/
        String name = mainFuncDefNode.getMainToken().getValue();
        FunctionType type = new FunctionType(new IntegerType(32),new ArrayList<>());
        Function function = IRBuilder.buildFunction(name, type, irModule);
        curFunction = function;

        /*Build Basic Block*/
        BasicBlock basicBlock = IRBuilder.buildBasicBlock(function);
        curBasicBlock = basicBlock;

        pushSymbolTable();
        /*Visit Block*/
        BlockNode blockNode = mainFuncDefNode.getBlockNode();
        visitBlock(blockNode);
    }

    public void visitBlock(BlockNode blockNode) {
        // Block → '{' { BlockItem } '}'

        ArrayList<BlockItemNode> blockItemNodes = blockNode.getBlockItemNodes();
        for (BlockItemNode blockItemNode : blockItemNodes) {
            visitBlockItem(blockItemNode);
        }
        /*pop out symbol table*/
        popSymbolTable();
    }

    public void visitBlockItem(BlockItemNode blockItemNode) {
        // BlockItem → Decl | Stmt
        if (blockItemNode.getStmtNode() != null) {
            visitStmt(blockItemNode.getStmtNode());
        } else {
            visitDecl(blockItemNode.getDeclNode());
        }
    }
    /*---------------------Stmt---------------------------------------*/
    /*Stmt:
    * 1. assignStmt
    * 2. expStmt
    * 3. blockStmt
    * 4. ifStmt
    * 5. forStmt
    * 6. breakStmt
    * 7. continueStmt
    * 8. returnStmt
    * 9. getintStmt
    * 10. printfStmt
    * */
    public void visitStmt(StmtNode stmtNode) {
        if (stmtNode.isAssignStmt()) {
            visitAssignStmt(stmtNode);
        } else if (stmtNode.isExpStmt()) {
            visitExpStmt(stmtNode);
        } else if (stmtNode.isBlockStmt()) {
            visitBlockStmt(stmtNode);
        } else if (stmtNode.isIfStmt()) {
            visitIfStmt(stmtNode);
        } else if (stmtNode.isForStmt()) {
            visitForStmt(stmtNode);
        } else if (stmtNode.isBreakStmt()) {
            visitBreakStmt(stmtNode);
        } else if (stmtNode.isContinueStmt()) {
            visitContinueStmt(stmtNode);
        } else if (stmtNode.isReturnStmt()) {
            visitReturnStmt(stmtNode);
        } else if (stmtNode.isGetintStmt()) {
            visitGetintStmt(stmtNode);
        } else if (stmtNode.isPrintfStmt()) {
            visitPrintfStmt(stmtNode);
        }
    }

    public void visitAssignStmt(StmtNode stmtNode) {
        //Stmt -> LVal '=' Exp ';'
        LValNode lValNode = stmtNode.getlValNode();
        visitLval(lValNode);
        Value operand = curValue;
        ExpNode expNode = stmtNode.getExpNode();
        visitExp(expNode);
        Value storeValue = curValue;
        IRBuilder.buildStore(storeValue,operand,curBasicBlock);
    }

    public void visitExpStmt(StmtNode stmtNode) {
        ExpNode expNode = stmtNode.getExpNode();
        if (expNode != null) {
            visitExp(expNode);
        }
    }

    public void visitBlockStmt(StmtNode stmtNode) {
        pushSymbolTable();
        BlockNode blockNode  = stmtNode.getBlockNode();
        visitBlock(blockNode);
    }

    public void visitIfStmt(StmtNode stmtNode) {
        ArrayList<StmtNode> stmtNodes = stmtNode.getStmtNodes();
        BasicBlock trueBlock = IRBuilder.buildBasicBlock(curFunction);
        BasicBlock falseBlock = IRBuilder.buildBasicBlock(curFunction);
        BasicBlock exitBlock = stmtNodes.size() == 1 ? falseBlock : IRBuilder.buildBasicBlock(curFunction);
        boolean ifEndWithRet = false;  // Delete The Last Block

        curTrueBlock = trueBlock;
        curFalseBlock = falseBlock;

        visitCond(stmtNode.getCondNode());

        curBasicBlock = trueBlock;
        visitStmt(stmtNodes.get(0));

        IRBuilder.buildBr(curBasicBlock,exitBlock);

        if (!curBasicBlock.getInstructions().isEmpty() && curBasicBlock.getInstructions().get(curBasicBlock.getInstructions().size()-1) instanceof Ret) {
            ifEndWithRet = true;
        }

        /*Visit Else*/
        if (stmtNodes.size() == 2) {
            curBasicBlock = falseBlock;
            visitStmt(stmtNodes.get(1));
            IRBuilder.buildBr(curBasicBlock,exitBlock);
            if (ifEndWithRet && !curBasicBlock.getInstructions().isEmpty() &&
                    curBasicBlock.getInstructions().get(curBasicBlock.getInstructions().size()-1) instanceof Ret) {
                curFunction.getBasicBlocks().remove(exitBlock);
            }
        }
        curBasicBlock = exitBlock;
    }

    public void visitForStmt(StmtNode stmtNode) {
        StmtNode forStmtNode1,forStmtNode2;
        StmtNode stmtBodyNode = stmtNode.getStmtNode();
        CondNode condNode = stmtNode.getCondNode();
        /*Transform For Statement Node into Normal Statement Node*/
        if (stmtNode.getForStmtNode1() != null) {
            forStmtNode1 = new StmtNode(stmtNode.getForStmtNode1().getlValNode(),stmtNode.getForStmtNode1().getAssignToken(),
                    stmtNode.getForStmtNode1().getExpNode(),null);
        } else {
            forStmtNode1 = null;
        }
        if (stmtNode.getForStmtNode2() != null) {
            forStmtNode2 = new StmtNode(stmtNode.getForStmtNode2().getlValNode(),stmtNode.getForStmtNode2().getAssignToken(),
                    stmtNode.getForStmtNode2().getExpNode(),null);
        } else {
            forStmtNode2 = null;
        }

        BasicBlock forBodyBlock =  IRBuilder.buildBasicBlock(curFunction); // The Body Of For Statement
        BasicBlock condBlock =  IRBuilder.buildBasicBlock(curFunction);
        BasicBlock changeBlock = IRBuilder.buildBasicBlock(curFunction);
        BasicBlock exitBlock = IRBuilder.buildBasicBlock(curFunction);

        /*Visit forStmt1*/
        if (forStmtNode1 != null) {
            visitAssignStmt(forStmtNode1);
        }
        IRBuilder.buildBr(curBasicBlock,condBlock);

        /*Visit Stmt Body*/
        curBasicBlock = forBodyBlock;
        curChangeBlock = changeBlock;
        curExitBlock  = exitBlock;
        visitStmt(stmtBodyNode);
        IRBuilder.buildBr(curBasicBlock,changeBlock);

        /*Visit Cond*/
        if (condNode != null) {
            curBasicBlock = condBlock;
            curTrueBlock = forBodyBlock;
            curFalseBlock = exitBlock;
            visitCond(condNode);
            curBasicBlock  = condBlock;
            IRBuilder.buildBr(curBasicBlock,forBodyBlock);
        } else {
            curBasicBlock = condBlock;
            IRBuilder.buildBr(curBasicBlock,forBodyBlock);
        }

        /*Visit For Stmt2*/
        if (forStmtNode2 != null) {
            curBasicBlock = changeBlock;
            visitAssignStmt(forStmtNode2);
            curBasicBlock = changeBlock;
            IRBuilder.buildBr(curBasicBlock,condBlock);
        } else {
            curBasicBlock = changeBlock;
            IRBuilder.buildBr(curBasicBlock,condBlock);
        }

        curBasicBlock = exitBlock;
    }

    public void visitBreakStmt(StmtNode stmtNode) {
        IRBuilder.buildBr(curBasicBlock,curExitBlock);
    }

    public void visitContinueStmt(StmtNode stmtNode) {
        IRBuilder.buildBr(curBasicBlock,curChangeBlock);
    }

    public void  visitReturnStmt(StmtNode stmtNode) {
        ExpNode expNode = stmtNode.getExpNode();
        if (expNode == null) {
            //void
            IRBuilder.buildRet(curBasicBlock);
        } else {
            //int 32
            visitExp(expNode);
            IRBuilder.buildRet(curBasicBlock,curValue);
        }
    }

    public void visitGetintStmt(StmtNode stmtNode) {
        // Stmt → LVal '=' 'getint''('')'';'
        /*LVal*/
        LValNode lValNode = stmtNode.getlValNode();
        visitLval(lValNode);
        Value operand = curValue;

        /*Getint Function*/
        String functionName = "getint";
        FunctionType functionType = new FunctionType(new IntegerType(32),new ArrayList<>());
        Function function = (Function) find(functionName);
        ArrayList<Value> arguments = new ArrayList<>();

        /*Build Call*/
        curValue = IRBuilder.buildCall(function,functionType,curBasicBlock,arguments);

        /*Build Store*/
        IRBuilder.buildStore(curValue,operand,curBasicBlock);
    }

    public void visitPrintfStmt(StmtNode stmtNode) {
        int expIndex = 0;
        ArrayList<Value> exps = new ArrayList<>();
        String formatString = stmtNode.getFormatString().getValue();
        formatString = formatString.substring(1,formatString.length()-1);
        for (ExpNode expNode : stmtNode.getExpNodes()) {
            visitExp(expNode);
            exps.add(curValue);
        }
        for (int i = 0; i < formatString.length(); i++) {
               if (formatString.charAt(i) == '%' && formatString.charAt(i + 1) == 'd') {
                   /* %d */
                   /*putint function*/
                   String functionName = "putint";
                   ArrayList<ValueType> parametersType  = new ArrayList<>();
                   parametersType.add(new IntegerType(32));
                   FunctionType functionType = new FunctionType(new VoidType(),parametersType);
                   Function function = (Function) find(functionName);
                   ArrayList<Value> arguments = new ArrayList<>();
                   arguments.add(exps.get(expIndex));

                   /*Build Call*/
                   curValue = IRBuilder.buildCall(function,functionType,curBasicBlock,arguments);
                   expIndex++;
                   i++;
               } else{
                   /*Characters*/
                   /*putch Function*/
                   String functionName = "putch";
                   ArrayList<ValueType> parametersType  = new ArrayList<>();
                   parametersType.add(new IntegerType(32));
                   FunctionType functionType = new FunctionType(new VoidType(),parametersType);
                   Function function = (Function) find(functionName);
                   ArrayList<Value> arguments = new ArrayList<>();
                   if(formatString.charAt(i) == '\\') {
                       arguments.add(new ConstInteger(32,10));
                       i++;
                   } else {
                       arguments.add(new ConstInteger(32,formatString.charAt(i)));
                   }
                   /*Build Call*/
                   curValue = IRBuilder.buildCall(function,functionType,curBasicBlock,arguments);
               }
        }

    }

    /*---------------------EXP--------------------------------*/
    public void visitConstExp(ConstExpNode constExpNode) {
        //  ConstExp → AddExp
        AddExpNode addExpNode = constExpNode.getAddExpNode();
        curValue = null;
        visitAddExp(addExpNode,null);
    }
    public void visitExp(ExpNode expNode) {
        AddExpNode addExpNode = expNode.getAddExpNode();
        curValue = null;
        visitAddExp(addExpNode,null);
    }

    public void visitAddExp(AddExpNode addExpNode,String formerOperation){
        // AddExp -> MulExp | MulExp ('+' | '−') AddExp
        AddExpNode addExpNode1 = addExpNode.getAddExpNode();
        MulExpNode mulExpNode = addExpNode.getMulExpNode();

        if (isConst) {
            /*直接将值计算出来*/
            Value lastValue = curValue;
            curValue = null;
            visitMulExp(mulExpNode,null);

            if(formerOperation != null) { //  not the first mul
                int leftValue = Integer.parseInt(lastValue.getValueName());
                int rightValue = Integer.parseInt(curValue.getValueName());
                if (formerOperation.equals("+")) {
                    curValue = new ConstInteger(32,leftValue + rightValue);
                } else if (formerOperation.equals("-")) {
                    curValue = new ConstInteger(32,leftValue - rightValue);
                }
            }
            if (addExpNode1 != null) {
                visitAddExp(addExpNode1,addExpNode.getOperator().getValue());
            }
        } else {
            Value lastValue = curValue;
            curValue = null;
            visitMulExp(mulExpNode,null);
            if(formerOperation != null) {
                if (formerOperation.equals("+")) {
                    curValue = IRBuilder.buildAdd(curBasicBlock,lastValue,curValue);
                } else if (formerOperation.equals("-")) {
                    curValue = IRBuilder.buildSub(curBasicBlock,lastValue,curValue);
                }
            }
            if (addExpNode1 != null) {
                visitAddExp(addExpNode1,addExpNode.getOperator().getValue());
            }
        }
    }

    public void visitMulExp(MulExpNode mulExpNode,String formerOperation){
        // MulExp -> UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
        MulExpNode mulExpNode1 = mulExpNode.getMulExpNode();
        UnaryExpNode unaryExpNode = mulExpNode.getUnaryExpNode();

        if (isConst) {
            Value lastValue = curValue;
            curValue = null;
            visitUnaryExp(unaryExpNode);
            if(formerOperation != null) { //  not the first mul
                int leftValue = Integer.parseInt(lastValue.getValueName());
                int rightValue = Integer.parseInt(curValue.getValueName());
                switch (formerOperation) {
                    case "*" -> curValue = new ConstInteger(32, leftValue * rightValue);
                    case "/" -> curValue = new ConstInteger(32, leftValue / rightValue);
                    case "%" -> curValue = new ConstInteger(32, leftValue % rightValue);
                }
            }
            if (mulExpNode1 != null) {
                visitMulExp(mulExpNode1,mulExpNode.getOperator().getValue());
            }
        } else {
            Value lastValue = curValue;
            curValue = null;
            visitUnaryExp(unaryExpNode);
            if(formerOperation != null) {
                switch (formerOperation) {
                    case "*" -> curValue = IRBuilder.buildMul(curBasicBlock, lastValue, curValue);
                    case "/" -> curValue = IRBuilder.buildSdiv(curBasicBlock, lastValue, curValue);
                    case "%" -> curValue = IRBuilder.buildSrem(curBasicBlock, lastValue, curValue);
                }
            }
            if (mulExpNode1 != null) {
                visitMulExp(mulExpNode1,mulExpNode.getOperator().getValue());
            }
        }
    }

    public void visitUnaryExp(UnaryExpNode unaryExpNode) {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        PrimaryExpNode primaryExpNode = unaryExpNode.getPrimaryExpNode();
        Token ident = unaryExpNode.getIdent();
        UnaryOpNode unaryOpNode = unaryExpNode.getUnaryOpNode();

        if (isConst) {
            if(ident != null) {
                /*Function*/
                /*should not reach here*/
            } else if(primaryExpNode != null) {
                visitPrimaryExp(primaryExpNode);
            } else {
                Token operator = unaryOpNode.getOperator();
                UnaryExpNode unaryExpNode1 = unaryExpNode.getUnaryExpNode();
                visitUnaryExp(unaryExpNode1);
                if (operator.getValue().equals("-")) {
                    int value = Integer.parseInt(curValue.getValueName());
                    value = value * -1;
                    curValue = new ConstInteger(32,value);
                } else if (operator.getValue().equals("!")) {
                    if (curValue.getValueName().equals("0")) {
                        curValue = new ConstInteger(32,1);
                    } else {
                        curValue = new ConstInteger(32,0);
                    }
                }
            }
        } else {
            if (ident != null) {
                String name = ident.getValue();
                Function function = (Function) find(name);
                // call function
                ArrayList<Value> arguments = new ArrayList<>();
                ArrayList<ValueType> argTypes = ((FunctionType) function.getValueType()).getParametersType();
                FuncRParamsNode funcRParamsNode = unaryExpNode.getFuncRParamsNode();
                int cnt = 0;
                if (funcRParamsNode != null) {
                    for (ExpNode expNode : funcRParamsNode.getExps()) {
                        if (! (argTypes.get(cnt) instanceof IntegerType)) {
                            isBuildCall =true;
                        }
                        visitExp(expNode);
                        isBuildCall = false;
                        /*current value is parameter*/
                        arguments.add(curValue);
                        cnt ++;
                    }
                }

                assert function != null;
                FunctionType functionType = (FunctionType) function.getValueType();
                curValue = IRBuilder.buildCall(function,functionType,curBasicBlock,arguments);

            } else if (primaryExpNode != null) {
                visitPrimaryExp(primaryExpNode);
            } else {
                Token operator = unaryOpNode.getOperator(); /*'+' | '−' | '!'*/
                UnaryExpNode unaryExpNode1 = unaryExpNode.getUnaryExpNode();
                visitUnaryExp(unaryExpNode1);
                if (operator.getValue().equals("-")) {
                    curValue = IRBuilder.buildSub(curBasicBlock,new ConstInteger(32,0),curValue);
                } else if (operator.getValue().equals("!")) {
                    curValue = IRBuilder.buildIcmp(curBasicBlock,"eq",new ConstInteger(32,0),curValue);
                }
                /*No need to tackle '+'*/
            }
        }
    }

    public void visitPrimaryExp(PrimaryExpNode primaryExpNode) {
        // PrimaryExp → '(' Exp ')' | LVal | Number
        ExpNode expNode = primaryExpNode.getExpNode();
        LValNode lValNode = primaryExpNode.getlValNode();
        NumberNode numberNode = primaryExpNode.getNumberNode();
        if(expNode != null) {
            visitExp(expNode);
        } else if (lValNode != null) {
            if (isBuildCall) {
                isBuildCall = false;
                visitLval(lValNode);
            } else {
                visitLval(lValNode);
                if (curValue.getValueType() instanceof  IntegerType) {
                    /*Integer */
                    return;
                }
                if (curValue.getValueType() instanceof PointerType) {
                    if ((((PointerType) curValue.getValueType()).getPointedType() instanceof IntegerType)) {
                        curValue = IRBuilder.buildLoad(new IntegerType(32),curValue,curBasicBlock);
                    }
                }
            }
        } else {
            visitNumber(numberNode);
        }
    }

    public void visitLval(LValNode lValNode) {
        Token ident = lValNode.getIdent();
        String name = ident.getValue();
        Value value = find(name);
        ArrayList<ExpNode> expNodes = lValNode.getExpNodes();
        assert value != null;
        if (value.getValueType() instanceof  IntegerType) {
            /* Integer Type*/
            curValue = value;
        } else if (value.getValueType() instanceof PointerType) {
            /*Pointer Type*/
            ValueType pointedType = ((PointerType) value.getValueType()).getPointedType();
            if (pointedType instanceof IntegerType) {
                /*alloca i32*/
                curValue = value;
            } else if (pointedType instanceof PointerType) {
                /*Pointing at other array*/
                if (expNodes.isEmpty()) {
                    PointerType pointerType = ((PointerType) pointedType);
                    curValue = IRBuilder.buildLoad(pointerType, value, curBasicBlock);
                } else {
                    PointerType pointerType = ((PointerType) pointedType);
                    Load load = IRBuilder.buildLoad(pointerType, value, curBasicBlock);
                    visitExp(expNodes.get(0));
                    ArrayList<Value> indices = new ArrayList<>();
                    indices.add(curValue);
                    GetElementPtr gep = IRBuilder.buildGetElementPtr(load, indices, curBasicBlock);
                    for (int i = 1; i < expNodes.size(); i++) {
                        visitExp(expNodes.get(i));
                        indices = new ArrayList<>();
                        indices.add(new ConstInteger(32, 0));
                        indices.add(curValue);
                        gep = IRBuilder.buildGetElementPtr(gep, indices, curBasicBlock);
                    }
                    if (expNodes.size() == 1 && pointerType.getPointedType() instanceof ArrayType) {
                        indices = new ArrayList<>();
                        indices.add(new ConstInteger(32, 0));
                        indices.add(new ConstInteger(32, 0));
                        gep = IRBuilder.buildGetElementPtr(gep, indices, curBasicBlock);
                    }
                    curValue = gep;
                }
            } else if (pointedType instanceof ArrayType) {
                if (isConst) {
                    /* const int arrayA[2] = {1,2};
                     * const int array_b[2][1] = {{1},{arrayA[1]}};
                     */
                    if (expNodes.size() ==1) {
                        visitExp(expNodes.get(0));
                        int dim1 = Integer.parseInt(curValue.getValueName());
                        value = ((ConstArray)((GlobalVar)value).getVarValue()).getOperand(dim1);
                        curValue = value;
                    } else if (expNodes.size() == 2) {
                        visitExp(expNodes.get(0));
                        int dim1 = Integer.parseInt(curValue.getValueName());
                        visitExp(expNodes.get(1));
                        int dim2 = Integer.parseInt(curValue.getValueName());
                        value = ((ConstArray)((ConstArray)((GlobalVar)value).getVarValue()).getOperand(dim1)).getOperand(dim2);
                        curValue = value;
                    }
                } else {
                    ArrayType arrayType = (ArrayType) pointedType;
                    if (expNodes.isEmpty()) {
                        ArrayList<Value> indices = new ArrayList<>();
                        indices.add(new ConstInteger(32, 0));
                        indices.add(new ConstInteger(32, 0));
                        curValue = IRBuilder.buildGetElementPtr(value, indices, curBasicBlock);
                    } else {
                        for (ExpNode expNode : expNodes) {
                            visitExp(expNode);
                            ArrayList<Value> indices = new ArrayList<>();
                            indices.add(new ConstInteger(32, 0));
                            indices.add(curValue);
                            value = IRBuilder.buildGetElementPtr(value, indices, curBasicBlock);
                        }
                        if (expNodes.size() == 1 && arrayType.getElementType() instanceof ArrayType) {
                            ArrayList<Value> indices = new ArrayList<>();
                            indices.add(new ConstInteger(32, 0));
                            indices.add(new ConstInteger(32, 0));
                            value = IRBuilder.buildGetElementPtr(value, indices, curBasicBlock);
                        }
                        curValue = value;
                }
                }
            }
        }
    }
    public void visitNumber(NumberNode numberNode) {
        int numberValue = Integer.parseInt(numberNode.getIntConst().getValue());
        curValue = new ConstInteger(32,numberValue);
    }

    /*---------------------------Cond----------------------------*/
    public void visitCond(CondNode condNode) {
        //Cond → LOrExp
        LOrExpNode lOrExpNode = condNode.getlOrExpNode();
        visitLOrExp(lOrExpNode);
    }

    public void visitLOrExp(LOrExpNode lOrExpNode) {
        //LOrExp → LAndExp | LAndExp '||' LOrExp
        LAndExpNode lAndExpNode = lOrExpNode.getlAndExpNode();
        LOrExpNode lOrExpNode1 = lOrExpNode.getlOrExpNode();

        BasicBlock trueBlock = curTrueBlock;
        BasicBlock falseBlock = curFalseBlock;
        BasicBlock nextBlock;
        if (lOrExpNode1 != null) {
            /*haven't reached the end*/
            nextBlock = IRBuilder.buildBasicBlock(curFunction);
        } else {
            /*reach the end*/
            nextBlock = falseBlock;
        }
        curFalseBlock = nextBlock;
        visitLAndExp(lAndExpNode);

        /*restore Blocks*/
        curTrueBlock = trueBlock;
        curFalseBlock = falseBlock;

        /*visit LOrExp*/
        if (lOrExpNode1 != null) {
            curBasicBlock = nextBlock;
            visitLOrExp(lOrExpNode1);
        }
    }

    public void visitLAndExp(LAndExpNode lAndExpNode) {
        // LAndExp → EqExp | EqExp '&&' LAndExp
        LAndExpNode lAndExpNode1 = lAndExpNode.getlAndExpNode();
        EqExpNode eqExpNode = lAndExpNode.getEqExpNode();

        BasicBlock trueBlock = curTrueBlock;
        BasicBlock falseBlock = curFalseBlock;
        BasicBlock nextBlock;

        if (lAndExpNode1 != null) {
            /*haven't reached the end*/
            nextBlock = IRBuilder.buildBasicBlock(curFunction);
        } else {
            /*reach the end*/
            nextBlock = trueBlock;
        }
        curTrueBlock = nextBlock;
        visitEqExp(eqExpNode,null);

        /*Build Br*/
        IRBuilder.buildBr(curBasicBlock,curTrueBlock,curFalseBlock,curValue);

        /*restore Blocks*/
        curTrueBlock = trueBlock;
        curFalseBlock = falseBlock;

        /*visit EqExp*/
        if (lAndExpNode1 != null) {
            curBasicBlock = nextBlock;
            visitLAndExp(lAndExpNode1);
        }
    }

    public void visitEqExp(EqExpNode eqExpNode , String formerOperation) {
        // EqExp → RelExp | RelExp ('==' | '!=') EqExp
        EqExpNode eqExpNode1 = eqExpNode.getEqExpNode();
        RelExpNode relExpNode = eqExpNode.getRelExpNode();
        if (formerOperation != null) {
            /*Build icmp*/
            Value operand1 =curValue;
            visitRelExp(relExpNode,null);
            Value operand2 =curValue;
            curValue = IRBuilder.buildIcmp(curBasicBlock,formerOperation,operand1,operand2);
        } else {
            /*The First RelExp*/
            visitRelExp(relExpNode,null);
        }
        if (eqExpNode1 == null && formerOperation == null) {
            /*Single Value in this case*/
            curValue = IRBuilder.buildIcmp(curBasicBlock,"ne",curValue,new ConstInteger(32,0));
        }
        if (eqExpNode1 != null) {
            String nowOperation = eqExpNode.getOperator().getValue().equals("==") ? "eq" : "ne";
            visitEqExp(eqExpNode1,nowOperation);
        }
    }

    public void visitRelExp(RelExpNode relExpNode , String formerOperation) {
        // RelExp -> AddExp | AddExp ('<' | '>' | '<=' | '>=') RelExp

        AddExpNode addExpNode = relExpNode.getAddExpNode();
        RelExpNode relExpNode1 = relExpNode.getRelExpNode();

        if(formerOperation != null) {
            /*Build icmp*/
            Value operand1 = curValue;
            curValue = null;
            visitAddExp(addExpNode,null);
            Value operand2 = curValue;
            curValue = IRBuilder.buildIcmp(curBasicBlock,formerOperation,operand1,operand2);
        } else {
            /*The First AddExp*/
            curValue = null;
            visitAddExp(addExpNode,null);
        }
        if (relExpNode1 != null) {
               String op = relExpNode.getOperator().getValue();
               String nowOperation = null;
               if (op.equals("<")) {
                   nowOperation = "slt";
               } else if(op.equals("<=")) {
                    nowOperation = "sle";
               } else if (op.equals(">")) {
                    nowOperation = "sgt";
               } else if (op.equals(">=")) {
                   nowOperation = "sge";
               }
               visitRelExp(relExpNode1,nowOperation);
        }
    }
}
