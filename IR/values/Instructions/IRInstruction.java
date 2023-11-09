package IR.values.Instructions;

import IR.types.ValueType;
import IR.values.BasicBlock;
import IR.values.User;

public class IRInstruction extends User {
    private BasicBlock basicBlock;  // Instruction 所处在的Basic Block
    public IRInstruction(String name, ValueType valueType, BasicBlock basicBlock) {
        super(name, valueType);
        this.basicBlock = basicBlock;
    }
}
