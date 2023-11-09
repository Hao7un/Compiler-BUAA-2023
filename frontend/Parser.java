package frontend;

import frontend.ASTNode.*;
import frontend.ASTNode.NumberNode;
import frontend.tokens.Token;
import frontend.tokens.TokenCode;
import static frontend.tokens.TokenCode.*;
import frontend.Symbol.SymbolTable;
import java.util.ArrayList;
import Error.ErrorHandler;
import frontend.Symbol.FuncSymbol;
import frontend.Symbol.Symbol;
import frontend.Symbol.VarSymbol;

public class Parser {
    private final ArrayList<Token> tokens;
    private int index; /*当前Parser读到的token的index*/
    private Token currentToken;
    private SymbolTable currentSymbolTable;
    private int loopLevel;


    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.index = -1;
        this.currentToken = null;
        this.currentSymbolTable = new SymbolTable(null);
        this.loopLevel = 0;
    }

    public void getNextToken() {
        index ++;
        if (index < tokens.size()) {
            currentToken = tokens.get(index);
        }
    }

    public Token preReadToken(int stride) {
        if (index + stride < tokens.size()) {
            return tokens.get(index + stride);
        } else {
            /*处理错误*/
            return null;
        }
    }

    public CompUnitNode parseCompUnit() {
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        getNextToken();
        ArrayList<DeclNode> declNodes = new ArrayList<>();
        ArrayList<FuncDefNode> funcDefNodes = new ArrayList<>();

        /*处理{Decl}*/
        while(currentToken.getTokenCode() == INTTK || currentToken.getTokenCode() == CONSTTK){
            if (currentToken.getTokenCode() == CONSTTK) {
                DeclNode declNode = parseDecl();
                declNodes.add(declNode);
            } else if (currentToken.getTokenCode() == INTTK) {
                Token token1 = preReadToken(1); //预读一个词，
                Token token2 = preReadToken(2);
                if (token1.getTokenCode() == IDENFR && token2.getTokenCode() == LPARENT) {
                    break;
                } else if (token1.getTokenCode() ==MAINTK) {
                    break;
                } else {
                    DeclNode declNode = parseDecl();
                    declNodes.add(declNode);
                }
            }
        }

        /*处理{FuncDef}*/
        while(currentToken.getTokenCode() == INTTK || currentToken.getTokenCode() == VOIDTK){
            if (currentToken.getTokenCode() == VOIDTK) {
                FuncDefNode funcDefNode = parseFuncDef();
                funcDefNodes.add(funcDefNode);
            } else if (currentToken.getTokenCode() == INTTK) {
                Token token1 = preReadToken(1); //预读一个词，
                if(token1.getTokenCode() == MAINTK) {
                    break;
                } else {
                    FuncDefNode funcDefNode = parseFuncDef();
                    funcDefNodes.add(funcDefNode);
                }
            }
        }
        /*MainFuncDef*/
        MainFuncDefNode mainFuncDefNode = parseMainFuncDef();

        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }

    public DeclNode parseDecl() {
        // Decl → ConstDecl | VarDecl
        ConstDeclNode constDeclNode;
        VarDeclNode varDeclNode;
        DeclNode declNode;
        if (currentToken.getTokenCode() == CONSTTK) {
            constDeclNode = parseConstDecl();
            declNode = new DeclNode(constDeclNode);
        } else {
            varDeclNode = parseVarDecl();
            declNode = new DeclNode(varDeclNode);
        }
        return declNode;
    }

    public ConstDeclNode parseConstDecl() {
        // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
        Token constToken = checkError(CONSTTK);

        ArrayList<ConstDefNode> constDefNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();

        BTypeNode bTypeNode = parseBType();
        ConstDefNode constDefNode = parseConstDef();
        constDefNodes.add(constDefNode);
        while(currentToken.getTokenCode() == COMMA) {
            commas.add(currentToken);
            getNextToken();
            constDefNode = parseConstDef();
            constDefNodes.add(constDefNode);
        }
        //处理I类型错误
        Token semicnToken = checkError(SEMICN);
        return new ConstDeclNode(constToken, bTypeNode, constDefNodes,commas,semicnToken);
    }

    public BTypeNode parseBType() {
        // BType → 'int'
        Token intToken = checkError(INTTK);

        return new BTypeNode(intToken);
    }

    public ConstDefNode parseConstDef() {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal

        Token ident = checkError(IDENFR);

        // 检查B类错误
        ErrorHandler.handleBError(currentSymbolTable,ident);

        ArrayList<Token> leftBracks = new ArrayList<>();
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        ArrayList<Token> rightBracks = new ArrayList<>();
        while(currentToken.getTokenCode() == LBRACK) {
            leftBracks.add(currentToken);
            getNextToken();
            constExpNodes.add(parseConstExp());
            rightBracks.add(checkError(RBRACK));
        }

        /*加入到符号表*/
        currentSymbolTable.addSymbol(new VarSymbol(ident.getValue(),1, constExpNodes.size()));

        Token assignToken = checkError(ASSIGN);
        ConstInitValNode constInitValNode = parseConstInitVal();
        return new ConstDefNode(ident, constExpNodes,leftBracks,rightBracks,assignToken, constInitValNode);
    }

    public ConstInitValNode parseConstInitVal() {
        // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if (currentToken.getTokenCode() == LBRACE) {
            Token leftBrace = currentToken;
            getNextToken();
            ArrayList<ConstInitValNode> constInitValNodes = new ArrayList<>();
            ArrayList<Token> commas = new ArrayList<>();
            if (currentToken.getTokenCode() != RBRACE) {
                constInitValNodes.add(parseConstInitVal());
                while (currentToken.getTokenCode() == COMMA) {
                    commas.add(currentToken);
                    getNextToken();
                    constInitValNodes.add(parseConstInitVal());
                }
            }
            Token rightBrace = checkError(RBRACE);
            return new ConstInitValNode(leftBrace, constInitValNodes,commas,rightBrace);
        } else {
            ConstExpNode constExpNode = parseConstExp();
            return new ConstInitValNode(constExpNode);
        }
    }

    public VarDeclNode parseVarDecl() {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        BTypeNode bTypeNode = parseBType();
        ArrayList<VarDefNode> varDefNodes = new ArrayList<>();
        varDefNodes.add(parseVarDef());

        ArrayList<Token> commas = new ArrayList<>();
        while(currentToken.getTokenCode() == COMMA) {
            commas.add(currentToken);
            getNextToken();
            varDefNodes.add(parseVarDef());
        }

        Token semicnToken = checkError(SEMICN);

        return new VarDeclNode(bTypeNode, varDefNodes,commas,semicnToken);
    }

    public VarDefNode parseVarDef() {
        // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal

        Token ident = checkError(IDENFR);

        //检查B类错误
        ErrorHandler.handleBError(currentSymbolTable,ident);

        ArrayList<Token> leftBracks = new ArrayList<>();
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        ArrayList<Token> rightBracks = new ArrayList<>();

        while(currentToken.getTokenCode() == LBRACK) {
            leftBracks.add(currentToken);
            getNextToken();
            constExpNodes.add(parseConstExp());
            rightBracks.add(checkError(RBRACK));
        }

        /*加入到符号表*/
        currentSymbolTable.addSymbol(new VarSymbol(ident.getValue(),0, constExpNodes.size()));

        if (currentToken.getTokenCode() == ASSIGN) {
             Token assignToken = currentToken;
             getNextToken();
             InitValNode initValNode = parseInitVal();
             return new VarDefNode(ident,leftBracks, constExpNodes,rightBracks,assignToken, initValNode);
        } else {
            return new VarDefNode(ident,leftBracks, constExpNodes,rightBracks);
        }
    }

    public InitValNode parseInitVal() {
        // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        if (currentToken.getTokenCode() == LBRACE) {
            // '{' [ InitVal { ',' InitVal } ] '}'
            Token leftBrace = currentToken;
            getNextToken();

            ArrayList<InitValNode> initValNodes = new ArrayList<>();
            ArrayList<Token> commas = new ArrayList<>();
            if (currentToken.getTokenCode() != RBRACE) {
                initValNodes.add(parseInitVal());
                while (currentToken.getTokenCode() == COMMA) {
                    commas.add(currentToken);
                    getNextToken();
                    initValNodes.add(parseInitVal());
                }
            }
            Token rightBrace = checkError(RBRACE);
            return new InitValNode(leftBrace, initValNodes,commas,rightBrace);
        } else {
            // Exp
            ExpNode expNode = parseExp();
            return new InitValNode(expNode);
        }
    }

    public FuncDefNode parseFuncDef() {
        //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncTypeNode funcTypeNode = parseFuncType();
        Token ident = checkError(IDENFR);

        //处理B类错误
        ErrorHandler.handleBError(currentSymbolTable,ident);

        int rettype = funcTypeNode.getFuncType().getValue().equals("int") ? 0 : 1;
        ArrayList<Symbol> paramTypeList = new ArrayList<>(); //等分析完再进行填写
        /*加入到符号表*/
        currentSymbolTable.addSymbol(new FuncSymbol(ident.getValue(),rettype,paramTypeList));
        /*解析函数内部的参数*/
        currentSymbolTable = new SymbolTable(currentSymbolTable);

        Token leftParent = checkError(LPARENT);

        FuncFParamsNode funcFParamsNode = null;
        Token rightParent;
        if (currentToken.getTokenCode() == LBRACE) {
            /*无参数时J类错误*/
            //补全')‘,选用的行数为（的行数
            rightParent = checkError(RPARENT);
        } else if (currentToken.getTokenCode() == RPARENT) {
            rightParent = checkError(RPARENT);
        } else {
            /*有参数时   J类错误 | 正常情况*/
            funcFParamsNode = parseFuncFParams();
            /*处理paramTypeList*/
            for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParams()) {
                paramTypeList.add(new VarSymbol(funcFParamNode.getIdent().getValue()
                        ,0, funcFParamNode.getDimension()));
            }
            rightParent = checkError(RPARENT);
        }
        BlockNode blockNode = parseBlock();
        currentSymbolTable = currentSymbolTable.getParentTable();
        ((FuncSymbol)currentSymbolTable.getFuncSymbol(ident.getValue())).setParamTypeList(paramTypeList);

        /*处理F、G类型错误*/
        ErrorHandler.handleFError(funcTypeNode.getFuncType(), blockNode);  //returnToken
        ErrorHandler.handleGError(funcTypeNode.getFuncType(), blockNode);   //函数末尾的'}'
        return new FuncDefNode(funcTypeNode,ident,leftParent, funcFParamsNode,rightParent, blockNode);
    }

    public MainFuncDefNode parseMainFuncDef() {
        // MainFuncDef → 'int' 'main' '(' ')' Block

        Token intToken = checkError(INTTK);
        Token mainToken = checkError(MAINTK);
        Token leftParent = checkError(LPARENT);
        Token rightParent = checkError(RPARENT);

        currentSymbolTable = new SymbolTable(currentSymbolTable);
        BlockNode blockNode = parseBlock();
        currentSymbolTable = currentSymbolTable.getParentTable();
        ErrorHandler.handleGError(intToken, blockNode);
        return new MainFuncDefNode(intToken,mainToken,leftParent,rightParent, blockNode);
    }

    public FuncTypeNode parseFuncType() {
        // FuncType → 'void' | 'int'
        Token funcType;
        if (currentToken.getTokenCode() == VOIDTK) {
            funcType = checkError(VOIDTK);
        } else {
            funcType = checkError(INTTK);
        }
        return new FuncTypeNode(funcType);
    }

    public FuncFParamsNode parseFuncFParams() {
        //FuncFParams → FuncFParam { ',' FuncFParam }
        ArrayList<FuncFParamNode> funcFParamNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();

        funcFParamNodes.add(parseFuncFParam());

        while(currentToken.getTokenCode() == COMMA) {
            commas.add(currentToken);
            getNextToken();
            funcFParamNodes.add(parseFuncFParam());
        }
        return new FuncFParamsNode(funcFParamNodes,commas);
    }

    public FuncFParamNode parseFuncFParam() {
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        BTypeNode bTypeNode = parseBType();
        Token ident = checkError(IDENFR);
        //处理B类错误
        ErrorHandler.handleBError(currentSymbolTable,ident);

        ArrayList<Token> leftBracks = new ArrayList<>();
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        ArrayList<Token> rightBracks = new ArrayList<>();

        if (currentToken.getTokenCode() == LBRACK) {
            leftBracks.add(currentToken);
            getNextToken();
            rightBracks.add(checkError(RBRACK));

            while(currentToken.getTokenCode() == LBRACK) {
                leftBracks.add(currentToken);
                getNextToken();
                constExpNodes.add(parseConstExp());
                rightBracks.add(checkError(RBRACK));
            }
        }
        /*加入符号表*/
        currentSymbolTable.addSymbol(new VarSymbol(ident.getValue(),0,leftBracks.size()));
        return new FuncFParamNode(bTypeNode,ident,leftBracks,rightBracks, constExpNodes);
    }

    public BlockNode parseBlock() {
        // Block → '{' { BlockItem } '}'
        Token leftBrace = checkError(LBRACE);
        ArrayList<BlockItemNode> blockItemNodes = new ArrayList<>();
        while (currentToken.getTokenCode() != RBRACE) {
            blockItemNodes.add(parseBlockItem());
        }
        Token rightBrace = checkError(RBRACE);
        return new BlockNode(leftBrace, blockItemNodes,rightBrace);
    }

    public BlockItemNode parseBlockItem() {
        // BlockItem → Decl | Stmt
        if (currentToken.getTokenCode() == CONSTTK || currentToken.getTokenCode() == INTTK) {
            DeclNode declNode = parseDecl();
            return new BlockItemNode(declNode);
        } else {
            StmtNode stmtNode = parseStmt();
            return new BlockItemNode(stmtNode);
        }
    }

    public StmtNode parseStmt() {
        /*
         Stmt → LVal '=' Exp ';'
         | [Exp] ';' //有无Exp两种情况
         | Block
         | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
         | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
         | 'break' ';' | 'continue' ';'
         | 'return' [Exp] ';'
         | LVal '=' 'getint''('')'';'
         | 'printf''('FormatString{','Exp}')'';'
         */
        if (currentToken.getTokenCode() == PRINTFTK) {
            /*'printf''('FormatString{','Exp}')'';'*/
            Token printfToken = currentToken;
            getNextToken();
            Token leftParent = checkError(LPARENT);
            Token strCon = checkError(STRCON);

            /*处理A类错误*/
            ErrorHandler.handleAError(strCon);

            ArrayList<ExpNode> expNodes = new ArrayList<>();
            ArrayList<Token> commas = new ArrayList<>();
            while(currentToken.getTokenCode() == COMMA) {
                commas.add(currentToken);
                getNextToken();
                expNodes.add(parseExp());
            }
            /*处理L类错误*/
            ErrorHandler.handleLError(strCon,printfToken, expNodes);
            Token rightParent = checkError(RPARENT);
            Token semicnToken = checkError(SEMICN);
            return new StmtNode(printfToken,leftParent,rightParent,semicnToken,strCon,commas, expNodes);
        } else if (currentToken.getTokenCode() == RETURNTK) {
            //   'return' [Exp] ';'
            Token returnToken = currentToken;
            getNextToken();
            ExpNode expNode = null;
            if (currentToken.getTokenCode() != SEMICN) {
                expNode = parseExp();
            }
            Token semicnToken = checkError(SEMICN);
            return new StmtNode(returnToken, expNode,semicnToken);
        } else if (currentToken.getTokenCode() == BREAKTK || currentToken.getTokenCode() == CONTINUETK) {
            // 'break' ';' | 'continue' ';'
            Token breakOrContinueToken = currentToken;
            ErrorHandler.handleMError(breakOrContinueToken,loopLevel);
            getNextToken();
            Token semicnToken = checkError(SEMICN);
            return new StmtNode(breakOrContinueToken,semicnToken);
        } else if (currentToken.getTokenCode() == LBRACE) {
            // Block
            currentSymbolTable = new SymbolTable(currentSymbolTable);
            BlockNode blockNode = parseBlock();
            currentSymbolTable = new SymbolTable(currentSymbolTable);
            return new StmtNode(blockNode);
        } else if (currentToken.getTokenCode() == FORTK) {
            /*'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt*/
            Token forToken = currentToken;
            getNextToken();
            Token leftParent = checkError(LPARENT);
            ForStmtNode forStmtNode1 = null;
            ForStmtNode forStmtNode2 = null;
            ArrayList<Token> semicnTokens = new ArrayList<>();

            if (currentToken.getTokenCode() != SEMICN) {
                forStmtNode1 = parseForStmt();
            }
            semicnTokens.add(checkError(SEMICN));
            CondNode condNode = null;
            if (currentToken.getTokenCode() != SEMICN) {
                condNode = parseCond();
            }
            semicnTokens.add(checkError(SEMICN));
            if (currentToken.getTokenCode() != RPARENT) {
                forStmtNode2 = parseForStmt();
            }
            Token rightParent = checkError(RPARENT);
            loopLevel += 1;
            StmtNode stmtNode = parseStmt();
            loopLevel -= 1;
            return new StmtNode(forToken,leftParent, forStmtNode1, forStmtNode2,semicnTokens, condNode,rightParent, stmtNode);
        } else if (currentToken.getTokenCode() == IFTK) {
            //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            Token ifToken = currentToken;
            getNextToken();
            Token leftParent = checkError(LPARENT);
            CondNode condNode = parseCond();
            Token rightParent = checkError(RPARENT);

            ArrayList<StmtNode> stmtNodes = new ArrayList<>();
            stmtNodes.add (parseStmt());
            Token elseToken = null;
            if (currentToken.getTokenCode() == ELSETK) {
                elseToken = currentToken;
                getNextToken();
                stmtNodes.add(parseStmt());
            }
            return new StmtNode(ifToken,leftParent, condNode,rightParent, stmtNodes,elseToken);
        } else {
            /*
                Stmt →
                LVal '=' Exp ';'
                | LVal '=' 'getint''('')'';'
                | [Exp] ';'
            */
            if (currentToken.getTokenCode() != IDENFR) { //只能是 Stmt -> [Exp] ';'
                ExpNode expNode = null;
                if(currentToken.getTokenCode() != SEMICN) {
                    expNode = parseExp();
                }
                Token semicnToken = checkError(SEMICN);
                return new StmtNode(expNode,semicnToken);
            } else {
                /*采用解析Expr的方式解析Lval*/
                ExpNode expNode = parseExp();
                if (currentToken.getTokenCode() == SEMICN) {
                    // [Exp] ';'
                    Token semicnToken = currentToken;
                    getNextToken();
                    return new StmtNode(expNode,semicnToken);
                } else if (currentToken.getTokenCode() == ASSIGN) {
                    Token assignToken = currentToken;
                    getNextToken();
                    LValNode lValNode = expNode.getAddExpNode().getMulExpNode().getUnaryExpNode().getPrimaryExpNode().getlValNode();
                    Token ident = lValNode.getIdent();
                    ErrorHandler.handleHError(ident,currentSymbolTable);
                    if (currentToken.getTokenCode() == GETINTTK) {
                        // LVal '=' 'getint''('')'';'
                        Token getintToken = currentToken;
                        getNextToken();
                        Token leftParent = checkError(LPARENT);
                        Token rightParent = checkError(RPARENT);
                        Token semicnToken = checkError(SEMICN);
                        return new StmtNode(lValNode,assignToken,getintToken,leftParent,rightParent,semicnToken);
                  } else {
                        // LVal '=' Exp ';'
                        ExpNode expNode1 = parseExp();
                        Token semicnToken = checkError(SEMICN);
                        return new StmtNode(lValNode,assignToken, expNode1,semicnToken);
                  }
                } else {
                    //error
                    return null;
                }
            }
        }
    }

    public ForStmtNode parseForStmt() {
        // ForStmt → LVal '=' Exp
        LValNode lValNode = parseLVal();
        Token assignToken = checkError(ASSIGN);

        ExpNode expNode = parseExp();

        return new ForStmtNode(lValNode,assignToken, expNode);
    }

    public ExpNode parseExp() {
        // Exp → AddExp
        AddExpNode addExpNode = parseAddExp();
        return new ExpNode(addExpNode);
    }

    public CondNode parseCond() {
        // Cond → LOrExp
        LOrExpNode lOrExpNode = parseLOrExp();
        return new CondNode(lOrExpNode);
    }

    public LValNode parseLVal() {
        // LVal → Ident {'[' Exp ']'}

        Token ident = checkError(IDENFR);

        //处理C类错误
        ErrorHandler.handleCError(currentSymbolTable,ident);

        ArrayList<Token> leftBracks = new ArrayList<>();
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        ArrayList<Token> rightBracks = new ArrayList<>();

        while(currentToken.getTokenCode() == LBRACK) {
            leftBracks.add(currentToken);
            getNextToken();
            expNodes.add(parseExp());
            rightBracks.add(checkError(RBRACK));
        }
        return new LValNode(ident,leftBracks,rightBracks, expNodes);
    }

    public PrimaryExpNode parsePrimaryExp() {
        // PrimaryExp → '(' Exp ')' | LVal | Number
        if (currentToken.getTokenCode() == LPARENT) {
            Token leftParent = currentToken;
            getNextToken();
            ExpNode expNode = parseExp();
            Token rightParent = checkError(RPARENT);
            return new PrimaryExpNode(leftParent, expNode,rightParent);
        } else if (currentToken.getTokenCode() == INTCON) {
            NumberNode numberNode = parseNumber();
            return new PrimaryExpNode(numberNode);
        } else {
            LValNode lValNode = parseLVal();
            return new PrimaryExpNode(lValNode);
        }
    }

    public NumberNode parseNumber() {
        // Number → IntConst
        Token intConst = checkError(INTCON);
        return new NumberNode(intConst);
    }

    public UnaryExpNode parseUnaryExp() {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (currentToken.getTokenCode() == PLUS || currentToken.getTokenCode() == MINU ||
        currentToken.getTokenCode() == NOT) {
            UnaryOpNode unaryOpNode = parseUnaryOp();
            UnaryExpNode unaryExpNode = parseUnaryExp();
            return new UnaryExpNode(unaryOpNode, unaryExpNode);
        } else if (currentToken.getTokenCode() == IDENFR && preReadToken(1).getTokenCode() == LPARENT) {
            Token ident = currentToken;

            //处理C类错误
            Boolean hasCError = ErrorHandler.handleCError(currentSymbolTable,ident);

            getNextToken();
            Token leftParent = currentToken;
            getNextToken();
            FuncRParamsNode funcRParamsNode = null;
            if (currentToken.getTokenCode() != RPARENT && currentToken.getTokenCode() != SEMICN) {
                funcRParamsNode = parseFuncRParams();
                if (!hasCError) { //没有C类错误，才对D类错误进行处理
                    //处理D类错误：函数参数个数不匹配
                    Boolean hasDError = ErrorHandler.handleDError(funcRParamsNode,ident,currentSymbolTable);
                    if (!hasDError) {
                        //处理E类错误： 参数类型不匹配
                        ErrorHandler.handleEError(funcRParamsNode,ident,currentSymbolTable);
                    }
                }
            }
            Token rightParent = checkError(RPARENT);
            return new UnaryExpNode(ident,leftParent, funcRParamsNode,rightParent);
        } else {
               PrimaryExpNode primaryExpNode = parsePrimaryExp();
               return new UnaryExpNode(primaryExpNode);
        }
    }

    public UnaryOpNode parseUnaryOp() {
        // UnaryOp → '+' | '−' | '!'
        if (currentToken.getTokenCode() != PLUS && currentToken.getTokenCode() != MINU &&
                currentToken.getTokenCode() != NOT) {
            /*error*/
        }
        Token operator = currentToken;
        getNextToken();
        return new UnaryOpNode(operator);
    }

    public FuncRParamsNode parseFuncRParams() {
        // FuncRParams → Exp { ',' Exp }
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();

        expNodes.add(parseExp());
        while (currentToken.getTokenCode() == COMMA) {
            commas.add(currentToken);
            getNextToken();
            expNodes.add(parseExp());
        }
        return new FuncRParamsNode(expNodes,commas);
    }

    public MulExpNode parseMulExp() {
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        // transform into MulExp → UnaryExp | UnaryExp ('*' | '/' | '%') MulExp

        UnaryExpNode unaryExpNode = parseUnaryExp();
        if (currentToken.getTokenCode() ==MULT || currentToken.getTokenCode() ==DIV ||
                currentToken.getTokenCode() ==MOD) {
            Token operator = currentToken;
            getNextToken();
            MulExpNode mulExpNode = parseMulExp();
            return new MulExpNode(unaryExpNode,operator, mulExpNode);
        } else {
            return new MulExpNode(unaryExpNode);
        }
    }

    public AddExpNode parseAddExp() {
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        //tranform into AddExp → MulExp | MulExp ('+' | '−')  AddExp
        MulExpNode mulExpNode = parseMulExp();
        if (currentToken.getTokenCode() == PLUS || currentToken.getTokenCode() == MINU) {
            Token operator = currentToken;
            getNextToken();
            AddExpNode addExpNode = parseAddExp();
            return new AddExpNode(mulExpNode,operator, addExpNode);
        } else {
            return new AddExpNode(mulExpNode);
        }
    }

    public RelExpNode parseRelExp() {
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        // transform into RelExp → AddExp | AddExp  ('<' | '>' | '<=' | '>=') RelExp

        AddExpNode addExpNode = parseAddExp();
        if (currentToken.getTokenCode() == LSS || currentToken.getTokenCode() == LEQ  ||
                currentToken.getTokenCode() == GRE || currentToken.getTokenCode() == GEQ) {
            Token operator = currentToken;
            getNextToken();
            RelExpNode relExpNode = parseRelExp();
            return new RelExpNode(addExpNode,operator, relExpNode);
        } else {
            return new RelExpNode(addExpNode);
        }
    }

    public EqExpNode parseEqExp() {
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        //transform into EqExp → RelExp | RelExp ('==' | '!=')  EqExp

        RelExpNode relExpNode = parseRelExp();
        if (currentToken.getTokenCode() == EQL || currentToken.getTokenCode() == NEQ) {
            Token operator = currentToken;
            getNextToken();
            EqExpNode eqExpNode = parseEqExp();
            return new EqExpNode(relExpNode,operator, eqExpNode);
        } else {
            return new EqExpNode(relExpNode);
        }

    }

    public LAndExpNode parseLAndExp() {
        // LAndExp → EqExp | LAndExp '&&' EqExp
        //transform into LAndExp → EqExp | EqExp '&&' LAndExp

        EqExpNode eqExpNode = parseEqExp();
        if (currentToken.getTokenCode() == AND) {
            Token operator = currentToken;
            getNextToken();
            LAndExpNode lAndExpNode = parseLAndExp();
            return new LAndExpNode(eqExpNode,operator, lAndExpNode);
        } else {
            return new LAndExpNode(eqExpNode);
        }
    }

    public LOrExpNode parseLOrExp() {
        // LOrExp → LAndExp | LOrExp '||' LAndExp
        // transform into // LOrExp → LAndExp | LAndExp '||' LOrExp
        LAndExpNode lAndExpNode = parseLAndExp();
        if (currentToken.getTokenCode() == OR) {
            Token operator = currentToken;
            getNextToken();
            LOrExpNode lOrExpNode = parseLOrExp();
            return new LOrExpNode(lAndExpNode,operator, lOrExpNode);
        } else {
            return new LOrExpNode(lAndExpNode);
        }
    }

    public ConstExpNode parseConstExp() {
        // ConstExp → AddExp
        AddExpNode addExpNode = parseAddExp();
        return new ConstExpNode(addExpNode);
    }

    public Token checkError(TokenCode tokenCode) {
        if (currentToken.getTokenCode() == tokenCode) {
            /*匹配成功*/
            Token current = currentToken;
            getNextToken(); //读取下一个token
            return current;
        }
        /*i,j,k三种情况*/
        switch (tokenCode) {
            case SEMICN :
                ErrorHandler.handleIError(tokens,index);
                return null;
            case RPARENT:
                ErrorHandler.handleJError(tokens,index);
                return null;
            case RBRACK:
                ErrorHandler.handleKError(tokens,index);
                return null;
            default:
                return null;
        }
    }
}

