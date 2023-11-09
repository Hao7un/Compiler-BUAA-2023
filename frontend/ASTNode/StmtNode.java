package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class StmtNode {
    /*
     Stmt → LVal '=' Exp ';'
     | [Exp] ';'
     | Block
     | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
     | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
     | 'break' ';'
     | 'continue' ';'
     | 'return' [Exp] ';'
     | LVal '=' 'getint''('')'';'
     | 'printf''('FormatString{','Exp}')'';'
     */

    private int stmtType;
    private LValNode lValNode;
    private Token assignToken;
    private ExpNode expNode;
    private ArrayList<ExpNode> expNodes;
    private Token leftBrack;
    private Token rightBrack;
    private BlockNode blockNode;
    private Token ifToken;
    private Token leftParent;
    private CondNode condNode;
    private Token rightParent;
    private StmtNode stmtNode;
    private ArrayList<StmtNode> stmtNodes;
    private Token elseToken;
    private Token forToken;
    private ForStmtNode forStmtNode1;
    private ForStmtNode forStmtNode2;
    private Token semicnToken;
    private ArrayList<Token> semicnTokens;
    private ArrayList<Token> commas;
    private Token breakAndContinueToken;
    private Token returnToken;
    private Token getintToken;
    private Token printfToken;
    private Token formatString;


    public StmtNode(LValNode lValNode, Token assignToken, ExpNode expNode, Token semicnToken) {
        // Stmt → LVal '=' Exp ';'
        this.lValNode = lValNode;
        this.assignToken = assignToken;
        this.expNode = expNode;
        this.semicnToken = semicnToken;
        this.stmtType = 0;
    }

    public StmtNode(ExpNode expNode, Token semicnToken) {
        // Stmt → [Exp] ';'
        this.expNode = expNode;
        this.semicnToken = semicnToken;
        this.stmtType = 1;
    }

    public StmtNode(BlockNode blockNode) {
        // Stmt → Block
        this.blockNode = blockNode;
        this.stmtType = 2;
    }

    public StmtNode(Token ifToken, Token leftParent, CondNode condNode, Token rightParent, ArrayList<StmtNode> stmtNodes, Token elseToken) {
        // Stmt -> 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        this.ifToken = ifToken;
        this.leftParent = leftParent;
        this.condNode = condNode;
        this.rightParent = rightParent;
        this.stmtNodes = stmtNodes;
        this.elseToken = elseToken;
        this.stmtType = 3;
    }

    public StmtNode(Token forToken, Token leftParent, ForStmtNode forStmtNode1, ForStmtNode forStmtNode2, ArrayList<Token> semicnTokens,
                    CondNode condNode, Token rightParent, StmtNode stmtNode) {
        // Stmt -> 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        this.forToken = forToken;
        this.leftParent = leftParent;
        this.rightParent = rightParent;
        this.forStmtNode1 = forStmtNode1;
        this.forStmtNode2 = forStmtNode2;
        this.semicnTokens = semicnTokens;
        this.condNode = condNode;
        this.stmtNode = stmtNode;
        this.stmtType = 4;
    }

    public StmtNode(Token breakAndContinueToken, Token semicnToken) {
        // Stmt -> 'break' ';' | 'continue' ';'
        this.breakAndContinueToken = breakAndContinueToken;
        this.semicnToken = semicnToken;
        this.stmtType = 5;
    }

    public StmtNode(Token returnToken, ExpNode expNode, Token semicnToken) {
        //Stmt -> 'return' [Exp] ';'
        this.returnToken = returnToken;
        this.expNode = expNode;
        this.semicnToken = semicnToken;
        this.stmtType = 6;
    }

    public StmtNode(LValNode lValNode, Token assignToken, Token getintToken,
                    Token leftParent, Token rightParent, Token semicnToken) {
        // Stmt -> LVal '=' 'getint''('')'';'
        this.lValNode = lValNode;
        this.assignToken = assignToken;
        this.getintToken = getintToken;
        this.leftParent = leftParent;
        this.rightParent = rightParent;
        this.semicnToken = semicnToken;
        this.stmtType = 7;
    }

    public StmtNode(Token printfToken, Token leftParent, Token rightParent, Token semicnToken, Token formatString,
                    ArrayList<Token> commas, ArrayList<ExpNode> expNodes) {
        // Stmt -> 'printf''('FormatString{','Exp}')'';'
        this.printfToken = printfToken;
        this.leftParent = leftParent;
        this.rightParent = rightParent;
        this.formatString = formatString;
        this.semicnToken = semicnToken;
        this.commas = commas;
        this.expNodes = expNodes;
        this.stmtType = 8;
    }

    public ArrayList<Token> getReturnTokens() {
        if (returnToken != null && expNode != null) {
            ArrayList<Token> returnTokens = new ArrayList<>();
            returnTokens.add(returnToken);
            return  returnTokens;
        } else if (blockNode != null) {
            return blockNode.getReturnTokens();
        } else if (stmtNode != null) { //for
            return stmtNode.getReturnTokens();
        } else if (stmtNodes != null) { // if
            ArrayList<Token> returnTokens = new ArrayList<>();
            for (StmtNode stmtNode : stmtNodes) {
                ArrayList<Token> temp = stmtNode.getReturnTokens();
                returnTokens.addAll(temp);
            }
            return returnTokens;
        } else {
            return new ArrayList<>();
        }
    }

    public String getStmtType() {
     /*
     Stmt → LVal '=' Exp ';'
     | [Exp] ';'
     | Block
     | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
     | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
     | 'break' ';' | 'continue' ';'
     | 'return' [Exp] ';'
     | LVal '=' 'getint''('')'';'
     | 'printf''('FormatString{','Exp}')'';'
     */
        return null;
    }

    public Token getReturnToken() {
        return returnToken;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }

    public Token getFormatString() {
        return formatString;
    }

    public ArrayList<StmtNode> getStmtNodes() {
        return stmtNodes;
    }

    public CondNode getCondNode() {
        return condNode;
    }

    public ForStmtNode getForStmtNode1() {
        return forStmtNode1;
    }

    public ForStmtNode getForStmtNode2() {
        return forStmtNode2;
    }

    public StmtNode getStmtNode() {
        return stmtNode;
    }

    public LValNode getlValNode() {
        return lValNode;
    }
    public void print() throws IOException {
        if (printfToken != null) {
            InOututils.write(printfToken.toString(),"output.txt");
            InOututils.write(leftParent.toString(),"output.txt");
            InOututils.write(formatString.toString(),"output.txt");
            if(commas != null) {
                int i;
                for(i = 0; i < commas.size(); i++) {
                    InOututils.write(commas.get(i).toString(),"output.txt");
                    expNodes.get(i).print();
                }
            }
            InOututils.write(rightParent.toString(),"output.txt");
            InOututils.write(semicnToken.toString(),"output.txt");
        } else if (returnToken != null) {
            InOututils.write(returnToken.toString(),"output.txt");
            if (expNode != null) {
                expNode.print();
            }
            InOututils.write(semicnToken.toString(),"output.txt");
        } else if (breakAndContinueToken != null) {
            InOututils.write(breakAndContinueToken.toString(),"output.txt");
            InOututils.write(semicnToken.toString(),"output.txt");
        } else if(ifToken != null) {
            InOututils.write(ifToken.toString(),"output.txt");
            InOututils.write(leftParent.toString(),"output.txt");
            condNode.print();
            InOututils.write(rightParent.toString(),"output.txt");
            stmtNodes.get(0).print();
            if (elseToken != null) {
                InOututils.write(elseToken.toString(),"output.txt");
                stmtNodes.get(1).print();
            }
        } else if(forToken != null) {
            InOututils.write(forToken.toString(),"output.txt");
            InOututils.write(leftParent.toString(),"output.txt");
            if (forStmtNode1 != null) {
                forStmtNode1.print();
            }
            InOututils.write(semicnTokens.get(0).toString(),"output.txt");
            if (condNode != null) {
                condNode.print();
            }
            InOututils.write(semicnTokens.get(1).toString(),"output.txt");
            if (forStmtNode2 != null) {
                forStmtNode2.print();
            }
            InOututils.write(rightParent.toString(),"output.txt");
            stmtNode.print();
        } else if(getintToken != null) {
            lValNode.print();
            InOututils.write(assignToken.toString(),"output.txt");
            InOututils.write(getintToken.toString(),"output.txt");
            InOututils.write(leftParent.toString(),"output.txt");
            InOututils.write(rightParent.toString(),"output.txt");
            InOututils.write(semicnToken.toString(),"output.txt");
        } else if (blockNode != null) {
            blockNode.print();
        } else if (lValNode != null) {
            lValNode.print();
            InOututils.write(assignToken.toString(),"output.txt");
            expNode.print();
            InOututils.write(semicnToken.toString(),"output.txt");
        } else {
            if (expNode != null) {
                expNode.print();
            }
            InOututils.write(semicnToken.toString(),"output.txt");
        }
        InOututils.write("<Stmt>","output.txt");
    }

    /* Stmt Types*/
    public boolean isAssignStmt() {
        return stmtType == 0;
    }

    public boolean isExpStmt() {
        return stmtType == 1;
    }

    public boolean isBlockStmt() {return stmtType == 2;}

    public boolean isIfStmt() {
        return stmtType == 3;
    }

    public boolean isForStmt() {
        return stmtType == 4;
    }

    public boolean isBreakStmt() {
        return stmtType == 5 && breakAndContinueToken.getValue().equals("break");
    }

    public boolean isContinueStmt() {
        return stmtType == 5 && breakAndContinueToken.getValue().equals("continue");
    }

    public boolean isReturnStmt() {
        return stmtType == 6;
    }

    public boolean isGetintStmt() {
        return stmtType == 7;
    }

    public boolean isPrintfStmt() {
        return stmtType == 8;
    }
}
