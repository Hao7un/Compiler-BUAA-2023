package IR.values;

import IR.types.ValueType;

import java.util.ArrayList;

public class User extends Value{

    private ArrayList<Value> operands;
    public User(String valueName, ValueType valueType) {
        super(valueName, valueType);
        this.operands = new ArrayList<>();
    }

    public void addOperand(Value value) {
        this.operands.add(value);
    }

    public Value getOperand(int index) {
        return operands.get(index);
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }

}
