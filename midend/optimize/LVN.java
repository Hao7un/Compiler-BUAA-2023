package midend.optimize;

import midend.IRModule;
import midend.types.FunctionType;
import midend.types.IntegerType;
import midend.types.VoidType;
import midend.values.Argument;
import midend.values.BasicBlock;
import midend.values.Const.ConstInteger;
import midend.values.Const.Function;
import midend.values.Const.GlobalVar;
import midend.values.Instructions.IRInstruction;
import midend.values.Instructions.binary.*;
import midend.values.Instructions.mem.GetElementPtr;
import midend.values.Instructions.mem.Store;
import midend.values.Instructions.others.Call;
import midend.values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LVN {
    private final IRModule module;

    private HashMap<BasicBlock,HashMap<String , IRInstruction>> hashMap;
    public LVN(IRModule module) {
        this.module = module;
    }

    public void run() {
        for (Function function : module.getFunctions()) {
            initialize(function);
            LVNOptimization(function);
        }
    }

    public void initialize(Function function) {
        this.hashMap = new HashMap<>();
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            hashMap.put(basicBlock,new HashMap<>());
        }
    }

    public void LVNOptimization(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            constantFolding(basicBlock); //先对basicblock进行一个常量折叠
            Iterator<IRInstruction>iterator = basicBlock.getInstructions().iterator();
            while(iterator.hasNext()) {
                IRInstruction instr = iterator.next();
                if (!canEliminate(instr)) {
                    continue;
                }
                String hashValue = getHashValue(instr); //对每条指令，获取它的操作数和操作符，根据操作数和操作符计算哈希值
                if (hashMap.get(basicBlock).containsKey(hashValue)) {
                    // 如果哈希表中已经存在该值，那么直接将basicblock里后续用到这个value的地方全部换成哈希表中存的value
                    instr.replaceAllUseWithValue(hashMap.get(basicBlock).get(hashValue));
                    iterator.remove();
                } else {
                    hashMap.get(basicBlock).put(hashValue,instr);
                }
            }
        }
    }

    private void constantFolding(BasicBlock basicBlock) {
        Iterator<IRInstruction> iterator = basicBlock.getInstructions().iterator();
        while(iterator.hasNext()) {
            IRInstruction instr = iterator.next();
            if (instr instanceof Add || instr instanceof Sub || instr instanceof Sdiv || instr instanceof Mul ||instr instanceof Srem) {
                Value leftOperand = instr.getOperand(0);
                Value rightOperand = instr.getOperand(1);
                int constNumber  = 0;
                if (leftOperand instanceof ConstInteger) {
                    constNumber ++;
                }
                if (rightOperand instanceof ConstInteger) {
                    constNumber++;
                }
                Value newValue = calculateNewValue(leftOperand,rightOperand,constNumber,instr);
                if (newValue != null) {
                    instr.replaceAllUseWithValue(newValue);
                    iterator.remove();
                }
            }
        }
    }

    private Value calculateNewValue(Value leftOperand,Value rightOperand,int constNumber,IRInstruction instr) {
        int result = 0;
        if (constNumber == 2) {
            int leftNumber = Integer.parseInt(leftOperand.getValueName());
            int rightNumber = Integer.parseInt(rightOperand.getValueName());
            //2个constant，可以直接进行计算
            if (instr instanceof Add) {
                result = leftNumber + rightNumber;
            } else if (instr instanceof Sub) {
                result = leftNumber - rightNumber;
            } else if (instr instanceof Mul) {
                result = leftNumber * rightNumber;
            } else if (instr instanceof Sdiv) {
                result = leftNumber / rightNumber;
            } else if (instr instanceof Srem) {
                result = leftNumber % rightNumber;
            }
            return new ConstInteger(32,result);
        } else if (constNumber == 1) {
            if (instr instanceof Add) {
                if (leftOperand instanceof ConstInteger constantInteger && constantInteger.getValue() == 0) {
                    // 0 + a = a
                    return rightOperand;
                } else if (rightOperand instanceof ConstInteger constInteger && constInteger.getValue() == 0) {
                    // a + 0 = a;
                    return leftOperand;
                }
            } else if (instr instanceof Sub) {
                if (rightOperand instanceof ConstInteger constInteger && constInteger.getValue() == 0) {
                    // a - 0 = a;
                    return leftOperand;
                }
            } else if (instr instanceof Mul) {
                if ((rightOperand instanceof ConstInteger constInteger && constInteger.getValue() == 0)) {
                    // a * 0 = 0;
                    return new ConstInteger(32,0);
                } else if ((leftOperand instanceof ConstInteger constInteger && constInteger.getValue() == 0)) {
                    // 0 * a = 0;
                    return new ConstInteger(32,0);
                } else if (rightOperand instanceof ConstInteger constInteger && constInteger.getValue() == 1) {
                    // a * 1 = a;
                    return leftOperand;
                } else if (leftOperand instanceof ConstInteger constInteger && constInteger.getValue()==1) {
                    // 1 * a = a;
                    return rightOperand;
                }
            } else if (instr instanceof Sdiv) {
                if (rightOperand instanceof ConstInteger constInteger && constInteger.getValue() == 1) {
                    // a / 1 = a;
                    return leftOperand;
                }
            } else if (instr instanceof Srem) {
                if (rightOperand instanceof ConstInteger constInteger && constInteger.getValue() == 1) {
                    // a % 1 = 0;
                    return new ConstInteger(32,0);
                }
            }
        } else if (constNumber == 0) {
            if (instr instanceof Srem) {
                // a % a = 0;
                if (leftOperand.equals(rightOperand)) {
                    return new ConstInteger(32,0);
                }
            } else if (instr instanceof Sdiv) {
                if (leftOperand.equals(rightOperand)) {
                    return new ConstInteger(32,1);
                }
            }
        }
        return null;    //无法优化
    }

    private boolean canEliminate(IRInstruction instr) {
        if( instr instanceof GetElementPtr || instr instanceof Add || instr instanceof Sub || instr instanceof Sdiv || instr instanceof Srem ||
                instr instanceof Mul || instr instanceof Icmp) {
            return true;
        } else if (instr instanceof Call) {
            //这里需要对call指令进行以下条件的判断，都满足的时候才可以进行
            //1. 不存在对函数的调用
            //2. 形参没有数组->只有int done
            //3. 有返回值 done
            //4. 纯函数判断 ： 不对全局变量进行修改 && 没有I/O
            Call callInstr = (Call) instr;
            Function calledFunction = (Function) callInstr.getOperand(0);
            if (calledFunction.isLibFunction()) {   //不能是库函数
                return false;
            }
            if (((FunctionType)calledFunction.getValueType()).getReturnType() instanceof VoidType) {    //不能没有返回值
                return false;
            }
            ArrayList<Argument> arguments = calledFunction.getArguments();
            for (Argument argument : arguments) {
                if (! (argument.getValueType() instanceof IntegerType)) {   //参数只能是i32
                    return false;
                }
            }
            for (BasicBlock basicBlock : calledFunction.getBasicBlocks()) {
                for (IRInstruction instruction : basicBlock.getInstructions()) {
                    if (instruction instanceof Call) {  //不能函数调用
                        return false;
                    }
                    if (instruction instanceof Store store) {
                        if (store.getToValue() instanceof GlobalVar) { //对全局变量进行了修改
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private String getHashValue(IRInstruction instr) {
        /*计算指令对应的hash值*/
        if (instr instanceof Add || instr instanceof Mul) {
            /*左右可以替换*/
            String leftOperand = instr.getOperand(0).getValueName();
            String rightOperand = instr.getOperand(1).getValueName();
            String op;
            if (instr instanceof Add) {
                op = "+";
            } else {
                op = "*";
            }
            //按照字典序
            if (leftOperand.compareTo(rightOperand) < 0) {
                return leftOperand + op + rightOperand;
            } else {
                return rightOperand + op + leftOperand;
            }
        } else if (instr instanceof Srem || instr instanceof Sub || instr instanceof Sdiv) {
            String leftOperand = instr.getOperand(0).getValueName();
            String rightOperand = instr.getOperand(1).getValueName();
            String op = null;
            if (instr instanceof Sub) {
                op = "-";
            } else if (instr instanceof Sdiv) {
                op = "/";
            } else if (instr instanceof Srem) {
                op = "%";
            }
            return leftOperand + op + rightOperand;
        } else if (instr instanceof Icmp) {
            String leftOperand = instr.getOperand(0).getValueName();
            String rightOperand = instr.getOperand(1).getValueName();
            String op = ((Icmp) instr).getIcmpOp();
            return leftOperand + op + rightOperand;
        } else if (instr instanceof GetElementPtr) {
            StringBuilder sb = new StringBuilder();
            for (Value operand : instr.getOperands()) {
                sb.append(operand.getValueName());
                sb.append("-");
            }
            sb.delete(sb.length()-1,sb.length());
            return sb.toString();
        } else if (instr instanceof Call callInstr) {
            ArrayList<Value> operands = callInstr.getOperands();
            StringBuilder sb = new StringBuilder();
            sb.append(operands.get(0).getValueName());
            sb.append("(");
            for (int i =1; i < operands.size(); i++) {
                sb.append(operands.get(i).getValueName());
                if (i != operands.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
        return null;
    }

}
