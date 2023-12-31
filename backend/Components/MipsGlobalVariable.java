package backend.Components;

import midend.types.ArrayType;
import midend.types.IntegerType;
import midend.types.PointerType;
import midend.values.Const.ConstArray;
import midend.values.Const.ConstInteger;
import midend.values.Const.ConstString;
import midend.values.Const.GlobalVar;
import midend.values.Value;

import java.util.ArrayList;

public class MipsGlobalVariable {
    /*MIPS 全局变量*/
    private GlobalVar globalVar;
    public MipsGlobalVariable(GlobalVar globalVar) {
        this.globalVar = globalVar;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (globalVar.getVarValue() instanceof ConstInteger) {
            sb.append(globalVar.getValueName().substring(1));  // remove @
            sb.append(":");
            sb.append("\t");
            sb.append(".word");
            sb.append("\t");
            sb.append(((ConstInteger) globalVar.getVarValue()).getValue());
        } else if (globalVar.getVarValue() instanceof ConstArray) {
            sb.append(globalVar.getValueName().substring(1));  // remove @
            sb.append(":");
            sb.append("\t");
            sb.append(".word");
            sb.append("\t");
            if (!((ConstArray) globalVar.getVarValue()).hasInitialValue()) {
                /*Do not have initial value*/
                int intContains = ((ArrayType)globalVar.getVarValue().getValueType()).getIntContains();
                for (int i = 0; i < intContains; i++) {
                    sb.append("0, ");
                }
                sb.delete(sb.length()-2,sb.length());
            } else {
                if(((ArrayType)((PointerType)globalVar.getValueType()).getPointedType()).getElementType() instanceof IntegerType) {
                    /*1D Array*/
                    ArrayList<Value> values = ((ConstArray)globalVar.getVarValue()).getOperands();
                    for (Value value : values) {
                        sb.append(((ConstInteger)value).getValue());
                        sb.append(", ");
                    }
                    sb.delete(sb.length()-2,sb.length());
                } else {
                    /*2D Array*/
                    ArrayList<Value> arrays = ((ConstArray)globalVar.getVarValue()).getOperands();
                    for (Value array : arrays) {
                        ArrayList<Value> values = ((ConstArray)array).getOperands();
                        for (Value value : values) {
                            sb.append(((ConstInteger)value).getValue());
                            sb.append(", ");
                        }
                    }
                    sb.delete(sb.length()-2,sb.length());
                }
            }

        } else if (globalVar.getVarValue() instanceof ConstString) {
            sb.append(globalVar.getValueName().substring(1));  // remove @
            sb.append(":");
            sb.append("\t");
            sb.append(".asciiz");
            sb.append("\t");
            sb.append("\"");
            sb.append(globalVar.getVarValue().getValueName());
            sb.append("\"");
        }
        return sb.toString();
    }

}
