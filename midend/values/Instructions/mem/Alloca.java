package midend.values.Instructions.mem;

import midend.types.PointerType;
import midend.types.ValueType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.util.HashSet;

public class Alloca extends IRInstruction {

    public Alloca(String name, ValueType allocatedType, BasicBlock basicBlock) {
        super(name, new PointerType(allocatedType), basicBlock);
    }


    @Override
    public String toString() {
        return this.getValueName() + " = alloca " + ((PointerType)getValueType()).getPointedType().toString();
    }
}
