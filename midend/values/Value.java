package midend.values;

import midend.Use;
import midend.types.ValueType;

import java.util.ArrayList;
import java.util.Iterator;

public class Value {
    private String valueName;              // value 的 name
    private ValueType valueType;           // value 的类型
    private ArrayList<Use> uses;           // value的User
    private int valueId;                    //value的Id
    private static int valueIdCounter = 0; // 用来生成value的Id

    public Value(String valueName, ValueType valueType) {
        this.valueId = valueIdCounter;
        valueIdCounter ++;

        this.valueName = valueName;
        this.valueType = valueType;
        this.uses = new ArrayList<>();
    }
    public void addUse(User user) {
        Use use = new Use(this, user);
        uses.add(use);
    }

    public void replaceAllUseWithValue(Value newValue) {
        ArrayList<User> users = new ArrayList<>();
        for (Use use : uses) {
            users.add(use.getUser());
        }
        for (User user : users) {
            user.replaceOperand(this,newValue);
        }
    }

    public void removeUser(User user) {
        Iterator<Use> iterator = uses.iterator();
        while(iterator.hasNext()) {
            Use use = iterator.next();
            if (use.getUser().equals(user)) {
                iterator.remove();
                break;
            }
        }
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


}
