package IR.values.Instructions.mem;

import IR.types.PointerType;
import IR.types.ValueType;
import IR.values.BasicBlock;
import IR.values.Instructions.IRInstruction;

public class Alloca extends IRInstruction {

    public Alloca(String name, ValueType allocatedType, BasicBlock basicBlock) {
        super(name, new PointerType(allocatedType), basicBlock);
    }

    @Override
    public String toString() {
        return this.getValueName() + " = alloca " + ((PointerType)getValueType()).getPointedType().toString();
    }
}
