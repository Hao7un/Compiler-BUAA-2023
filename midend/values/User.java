package midend.values;

import midend.Use;
import midend.types.ValueType;

import java.util.ArrayList;
import java.util.Iterator;

public class User extends Value{

    private ArrayList<Value> operands;
    public User(String valueName, ValueType valueType) {
        super(valueName, valueType);
        this.operands = new ArrayList<>();
    }

    public void addOperand(Value value) {

        this.operands.add(value);
        if (value != null) {
            value.addUse(this);
        }
    }

    public void setOperand(Value newValue,int idx) {
        this.operands.set(idx,newValue);
    }

    public Value getOperand(int index) {
        return operands.get(index);
    }

    public void replaceOperand(Value oldValue,Value newValue) {
        assert operands.contains(oldValue);
        // operands中包含oldValue,要在相同位置替换为newValue
        int idx = operands.indexOf(oldValue);
        oldValue.removeUser(this);
        operands.set(idx,newValue);
        if (newValue != null) {
            newValue.addUse(this);
        }
    }


    public ArrayList<Value> getOperands() {
        return operands;
    }

}
