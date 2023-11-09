import java.io.IOException;
import java.util.ArrayList;

import IR.IRModule;
import backend.Components.MipsModule;
import backend.MipsGenerator;
import frontend.ASTNode.CompUnitNode;
import frontend.Lexer;
import frontend.Parser;
import frontend.tokens.Token;
import IR.Visitor;
import Error.ErrorHandler;
import utils.InOututils;

public class Compiler {
    public static void main(String[] args) {
        try {
            String fileContent = InOututils.read();
            /*LEXER*/
            Lexer lexer = new Lexer(fileContent);
            lexer.tokenize();
            ArrayList<Token> tokens = lexer.getTokens();

            /*PARSER*/
            Parser parser = new Parser(tokens);
            CompUnitNode compUnitNode = parser.parseCompUnit();
            //compUnitNode.print();

            /*ERROR HANDLING*/
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            InOututils.write(errorHandler.toString(),"error.txt");

            /*LLVM GENERATION*/
            Visitor visitor = new Visitor();
            visitor.visitCompUnit(compUnitNode);
            IRModule irModule = IRModule.getInstance();
            InOututils.write(irModule.toString(),"llvm_ir.txt");

            /*MIPS GENERATION*/
            MipsGenerator mipsGenerator = new MipsGenerator();
            mipsGenerator.genModule();
            InOututils.write(MipsModule.getInstance().toString(),"mips.txt");

            /*OPTIMIZATION*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
