package IR.values;

import IR.Use;
import IR.types.ValueType;

import java.util.ArrayList;

public class Value {
    private String valueName;              // value 的 name
    private ValueType valueType;           // value 的类型
    private ArrayList<Use> uses;           // value的User
    private int valueId;                   //value的Id
    private static int valueIdCounter = 0; // 用来生成value的Id

    public Value(String valueName, ValueType valueType) {
        this.valueId = valueIdCounter;
        valueIdCounter ++;

        this.valueName = valueName;
        this.valueType = valueType;
        this.uses = new ArrayList<>();
    }

    public String getValueName() {
        return valueName;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public ArrayList<Use> getUseList() {
        return uses;
    }

    public void addUse(Use use) {
        this.uses.add(use);
    }

}
