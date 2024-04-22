import java.util.*;

class Value {
    public String type;
    public String value;

    public Value(String type, String value) {
        this.type = type;
        this.value = value;
    }
}

public class LLVMActions extends SPEEDYBaseListener {
    HashMap<String, String> variables = new HashMap<String, String>();
    HashMap<String, String> globalVariables = new HashMap<String, String>();
    HashSet<String> types = new HashSet<String>() {{
        add("int");
        add("float");
    }};
    HashSet<String> definedFunctions = new HashSet<String>() {{
        add("print");
        add("scan");
    }};

    HashMap<String, ArrayList<String>> functions = new HashMap<String, ArrayList<String>>();

    List<Value> argumentsList = new ArrayList<Value>();
    Stack<Value> stack = new Stack<Value>();

    Boolean isGlobal;

    @Override
    public void enterProgram(SPEEDYParser.ProgramContext ctx) {
        isGlobal = true;
    }

    @Override
    public void exitProgram(SPEEDYParser.ProgramContext ctx) {
        LLVMGenerator.close_main();
        System.out.println(LLVMGenerator.generate());
    }

    @Override
    public void exitDeclAssign(SPEEDYParser.DeclAssignContext ctx) {
        String ID = ctx.declaration().getChild(1).getText();
        String ArrayOperation = ctx.operation().getChild(0).getText();
        if (ArrayOperation.charAt(0) != '[') {
            if (!variables.containsKey(ID) && !globalVariables.containsKey(ID)) {
                error(ctx.getStart().getLine(), "variable not declared");
            }
            Value v = stack.pop();
            if (variables.containsKey(ID)) {
                if (!v.type.equals(variables.get(ID))) {
                    error(ctx.getStart().getLine(), "assignment type mismatch");
                }
            } else {
                if (!v.type.equals(globalVariables.get(ID))) {
                    error(ctx.getStart().getLine(), "assignment type mismatch");
                }
            }
            if (v.type.equals("int")) {
                LLVMGenerator.assignInt(getScope(ID), v.value);
            }
            if (v.type.equals("float")) {
                LLVMGenerator.assignFloat(getScope(ID), v.value);
            }
        } else {
            try {
                ID = ctx.declaration().getChild(2).getText();
                String arrType = variables.get(ID);
                if (arrType == null) {
                    arrType = globalVariables.get(ID);
                }
                String[] split_array_type = arrType.split("\\[");
                String type = split_array_type[0];
                String len = split_array_type[1].split("\\]")[0];
                List<String> values = new ArrayList<>();

                if (argumentsList.size() > Integer.parseInt(len)) {
                    error(ctx.getStart().getLine(), "array is bigger than declared");
                }
                for (Value v : argumentsList) {

                    if (v.type.equals("ID") && ((variables.containsKey(v.value) && variables.get(v.value).contains(type))
                            || (globalVariables.containsKey(v.value) && globalVariables.get(v.value).contains(type)))) {
                        if (type.equals("int")) {
                            values.add("%" + LLVMGenerator.loadInt(getScope(v.value)));
                        } else if (type.equals("float")) {
                            values.add("%" + LLVMGenerator.loadFloat(getScope(v.value)));
                        }
                    } else if (v.type.equals("ARRAY_ID") && ((variables.containsKey(v.value) && variables.get(v.value).contains(type))
                            || (globalVariables.containsKey(v.value) && globalVariables.get(v.value).contains(type)))) {
                        String[] split_array_id = v.value.split("\\[");
                        String id = split_array_id[0];
                        String arrId = split_array_id[1].split("\\]")[0];
                        if (type.equals("int")) {
                            values.add("%" + LLVMGenerator.loadIntArrayValue(getScope(id), arrId, len));
                        } else if (type.equals("float")) {
                            values.add("%" + LLVMGenerator.loadFloatArrayValue(getScope(id), arrId, len));
                        }
                    } else if ((v.type.equals("int") || v.type.equals("float")) && v.type.contains(type)) {
                        values.add(v.value);
                    }
                }
                if (values.size() != Integer.parseInt(len)) {
                    error(ctx.getStart().getLine(), "variables in the array are not the same type. Expected :" + type);
                }
                for (int i = 0; i < values.size(); i++) {
                    if (type.equals("int")) {
                        LLVMGenerator.assignArrayIntElement(values.get(i), getScope(ID), Integer.toString(i), len);
                    } else if (type.equals("float")) {
                        LLVMGenerator.assignArrayFloatElement(values.get(i), getScope(ID), Integer.toString(i), len);
                    }
                }
                argumentsList.clear();
            } catch (ArrayIndexOutOfBoundsException e) {
                error(ctx.getStart().getLine(), "variable is not an array");
            }
        }

    }

    @Override
    public void exitIdAssign(SPEEDYParser.IdAssignContext ctx) {
        String ID = ctx.ID().getText();

        if (!variables.containsKey(ID) && !globalVariables.containsKey(ID)) {
            error(ctx.getStart().getLine(), "variable not declared");
        }
        String ArrayOperation = ctx.operation().getChild(0).getText();
        if (ArrayOperation.charAt(0) != '[') {
            Value v = stack.pop();
            if (variables.containsKey(ID)) {
                if (!v.type.equals(variables.get(ID))) {
                    error(ctx.getStart().getLine(), "assignment type mismatch");
                }
            } else {
                if (!v.type.equals(globalVariables.get(ID))) {
                    error(ctx.getStart().getLine(), "assignment type mismatch");
                }
            }
            if (v.type.equals("int")) {
                LLVMGenerator.assignInt(getScope(ID), v.value);
            }
            if (v.type.equals("float")) {
                LLVMGenerator.assignFloat(getScope(ID), v.value);
            }
        } else {
            try {
                String arrType = variables.get(ID);
                if (arrType == null) {
                    arrType = globalVariables.get(ID);
                }
                String[] split_array_type = arrType.split("\\[");
                String type = split_array_type[0];
                String len = split_array_type[1].split("\\]")[0];
                List<String> values = new ArrayList<>();
                if (argumentsList.size() != Integer.parseInt(len)) {
                    error(ctx.getStart().getLine(), "array size mismatch");
                }
                for (Value v : argumentsList) {

                    if (v.type.equals("ID") && (
                            (variables.containsKey(v.value) && variables.get(v.value).contains(type))
                                    || (globalVariables.containsKey(v.value) && globalVariables.get(v.value).contains(type)))
                    ) {
                        if (type.equals("int")) {
                            values.add("%" + LLVMGenerator.loadInt(getScope(v.value)));
                        } else if (type.equals("float")) {
                            values.add("%" + LLVMGenerator.loadFloat(getScope(v.value)));
                        }
                    } else if (v.type.equals("ARRAY_ID") && (
                            (variables.containsKey(v.value) && variables.get(v.value).contains(type))
                                    || (globalVariables.containsKey(v.value) && globalVariables.get(v.value).contains(type))
                    )) {
                        String[] split_array_id = v.value.split("\\[");
                        String id = split_array_id[0];
                        String arrId = split_array_id[1].split("\\]")[0];
                        if (type.equals("int")) {
                            values.add("%" + LLVMGenerator.loadIntArrayValue(getScope(id), arrId, len));
                        } else if (type.equals("float")) {
                            values.add("%" + LLVMGenerator.loadFloatArrayValue(getScope(id), arrId, len));
                        }
                    } else if ((v.type.equals("int") || v.type.equals("float")) && v.type.contains(type)) {
                        values.add(v.value);
                    }
                }
                if (values.size() != Integer.parseInt(len)) {
                    error(ctx.getStart().getLine(), "variables in the array are not the same type. Expected: " + type);
                }
                for (int i = 0; i < values.size(); i++) {
                    if (type.equals("int")) {
                        LLVMGenerator.assignArrayIntElement(values.get(i), getScope(ID), Integer.toString(i), len);
                    } else if (type.equals("float")) {
                        LLVMGenerator.assignArrayFloatElement(values.get(i), getScope(ID), Integer.toString(i), len);
                    }
                }
                argumentsList.clear();
            } catch (ArrayIndexOutOfBoundsException e) {
                error(ctx.getStart().getLine(), "variable is not an array");
            }
        }

    }

    @Override
    public void exitArrayIdAssign(SPEEDYParser.ArrayIdAssignContext ctx) {
        String ARRAY_ID = ctx.ARRAY_ID().getText();
        String[] split_array_id = ARRAY_ID.split("\\[");
        String id = split_array_id[0];
        String arrId = split_array_id[1].split("\\]")[0];
        if (!variables.containsKey(id) && !globalVariables.containsKey(id)) {
            error(ctx.getStart().getLine(), "variable not declared");
        }
        String arrType = variables.get(id);
        if (arrType == null) {
            arrType = globalVariables.get(id);
        }
        String[] split_array_type = arrType.split("\\[");
        String type = split_array_type[0];
        String len = split_array_type[1].split("\\]")[0];

        if (Integer.parseInt(arrId) >= Integer.parseInt(len) || Integer.parseInt(arrId) < 0) {
            error(ctx.getStart().getLine(), "allocation fail :)");
        }

        Value v = stack.pop();
        if (!v.type.equals(type)) {
            error(ctx.getStart().getLine(), "arrayId assignment type mismatch");
        }
        if (v.type.equals("int")) {
            LLVMGenerator.assignArrayIntElement(v.value, getScope(id), arrId, len);
        }
        if (v.type.equals("float")) {
            LLVMGenerator.assignArrayFloatElement(v.value, getScope(id), arrId, len);
        }
    }

    @Override
    public void exitDeclaration(SPEEDYParser.DeclarationContext ctx) {
        String ID = ctx.ID().getText();
        String TYPE = ctx.type().getText();

        boolean isVariableGlobal = ctx.GLOBAL() == null ? isGlobal : true;


        if ((!variables.containsKey(ID) && !isVariableGlobal) || (!globalVariables.containsKey(ID) && isVariableGlobal)) {
            if (types.contains(TYPE)) {
                try {
                    String ARRAY_LEN = ctx.array_declaration().getChild(1).getText();
                    if (isVariableGlobal) {
                        globalVariables.put(ID, TYPE + '[' + ARRAY_LEN + ']');
                        System.out.println(globalVariables.get(ID));
                    } else {
                        variables.put(ID, TYPE + '[' + ARRAY_LEN + ']');
                    }
                    if (TYPE.equals("int")) {
                        LLVMGenerator.declareIntArray(ID, ARRAY_LEN, isVariableGlobal);
                    } else if (TYPE.equals("float")) {
                        LLVMGenerator.declareFloatArray(ID, ARRAY_LEN, isVariableGlobal);
                    }
                } catch (NullPointerException ex) {
                    if (isVariableGlobal) {
                        globalVariables.put(ID, TYPE);
                    } else {
                        variables.put(ID, TYPE);
                    }
                    if (TYPE.equals("int")) LLVMGenerator.declareInt(ID, isVariableGlobal);
                    else if (TYPE.equals("float")) LLVMGenerator.declareFloat(ID, isVariableGlobal);
                }
            } else {
                System.err.println("Line " + ctx.getStart().getLine() + ", unknown variable type: " + TYPE);
            }
        } else {
            System.err.println("Line " + ctx.getStart().getLine() + ", variable already defined: " + ID);
        }
    }

    @Override
    public void exitFunction_call(SPEEDYParser.Function_callContext ctx) {
        String FUNC_NAME = ctx.function_name().getText();
        if (FUNC_NAME.equals("print")) {
            if (argumentsList.size() == 1) {
                Value argument = argumentsList.get(0);
                String ID = argument.value;
                String type = variables.get(ID);
                if (type == null) {
                    type = globalVariables.get(ID);
                }
                if (type != null) {
                    if (type.equals("int")) {
                        LLVMGenerator.printInt(getScope(ID));
                    } else if (type.equals("float")) {
                        LLVMGenerator.printFloat(getScope(ID));
                    }
                } else {
                    if (argument.type.equals("int")) {
                        LLVMGenerator.printConstantInt(ID);
                    } else if (argument.type.equals("float")) {
                        LLVMGenerator.printConstantFloat(ID);
                    } else {
                        ctx.getStart().getLine();
                        System.err.println("Line " + ctx.getStart().getLine() + ", unknown variable: " + ID);
                    }
                }
            } else {
                if (argumentsList.size() == 0) {
                    ctx.getStart().getLine();
                    System.err.println("line " + ctx.getStart().getLine() + ", no arguments in function print, Expected 1, got 0");
                } else {
                    ctx.getStart().getLine();
                    System.err.println("line " + ctx.getStart().getLine() + ", too many argument in function print, Expected 1, got: " + argumentsList.size());
                }
            }
        } else if (FUNC_NAME.equals("scan")) {
            if (argumentsList.size() == 1) {
                Value argument = argumentsList.get(0);
                String ID = argument.value;
                String type = variables.get(ID);
                if (type == null) {
                    type = globalVariables.get(ID);
                }
                if (type != null) {
                    if (type.equals("int")) {
                        LLVMGenerator.scanInt(getScope(ID));
                    } else if (type.equals("float")) {
                        LLVMGenerator.scanFloat(getScope(ID));
                    }
                } else {
                    System.err.println("Line " + ctx.getStart().getLine() + ", unknown variable: " + ID);
                }
            } else {
                if (argumentsList.size() == 0) {
                    System.err.println("line " + ctx.getStart().getLine() + ", no arguments in function scan, Expected 1, got 0");
                } else {
                    System.err.println("line " + ctx.getStart().getLine() + ", too many argument in function scan, Expected 1, got: " + argumentsList.size());
                }
            }
        } else {
            ArrayList<String> args = functions.get(FUNC_NAME);
            if (args == null) {
                error(ctx.getStart().getLine(), ", no such function: " + FUNC_NAME);
            }
            if (argumentsList.size() != args.size() - 1) {
                error(ctx.getStart().getLine(), ", wrong number of arguments");
            }
            if (args.get(0).equals("int")) {
                LLVMGenerator.call(FUNC_NAME, "i32");
            } else if (args.get(0).equals("float")) {
                LLVMGenerator.call(FUNC_NAME, "double");
            } else {
                error(ctx.getStart().getLine(), ", invalid type");
            }
            boolean last = false;
            for (int i = 0; i < argumentsList.size(); i++) {
                if (i == argumentsList.size() - 1) {
                    last = true;
                }
                Value argument = argumentsList.get(i);
                String argType = variables.get(argument.value);
                if (argType == null) {
                    argType = globalVariables.get(argument.value);
                }
                String requiredArg = args.get(i + 1);
                if (argType.equals(requiredArg)) {
                    if (argType.equals("int")) {
                        argType = "i32";
                    } else if (argType.equals("float")) {
                        argType = "double";
                    } else {
                        error(ctx.getStart().getLine(), "wrong argument type");
                    }
                    LLVMGenerator.callparams(getScope(argument.value), argType, last);
                }
            }
        }
        argumentsList.clear();
    }

    @Override
    public void exitFuncAssign(SPEEDYParser.FuncAssignContext ctx) {
        String id = ctx.ID().getText();
        String type = variables.get(id);
        if (type == null) {
            type = globalVariables.get(id);
        }
        if (type == null) {
            error(ctx.getStart().getLine(), "variable not defined");
        }
        if (type.equals("int")) {
            LLVMGenerator.callfinal(getScope(id), "i32");
        } else if (type.equals("float")) {
            LLVMGenerator.callfinal(getScope(id), "double");
        } else {
            error(ctx.getStart().getLine(), "wrong function type");
        }
    }

    @Override
    public void exitValue(SPEEDYParser.ValueContext ctx) {
        try {
            argumentsList.add(new Value("ID", ctx.ID().getText()));
        } catch (NullPointerException e) {
        }

        try {
            argumentsList.add(new Value("int", ctx.INT().getText()));
        } catch (NullPointerException e) {
        }

        try {
            argumentsList.add(new Value("float", ctx.FLOAT().getText()));
        } catch (NullPointerException e) {
        }

        try {
            argumentsList.add(new Value("ARRAY_ID", ctx.ARRAY_ID().getText()));
        } catch (NullPointerException e) {
        }
    }

    @Override
    public void exitInt(SPEEDYParser.IntContext ctx) {
        stack.push(new Value("int", ctx.INT().getText()));
    }

    @Override
    public void exitFloat(SPEEDYParser.FloatContext ctx) {
        stack.push(new Value("float", ctx.FLOAT().getText()));
    }

    @Override
    public void exitId(SPEEDYParser.IdContext ctx) {
        String ID = ctx.ID().getText();
        if (variables.containsKey(ID) || globalVariables.containsKey(ID)) {
            String type = variables.get(ID);
            if (type == null) {
                type = globalVariables.get(ID);
            }
            int reg = -1;
            if (type.equals("int")) {
                reg = LLVMGenerator.loadInt(getScope(ID));
            } else if (type.equals("float")) {
                reg = LLVMGenerator.loadFloat(getScope(ID));
            }
            stack.push(new Value(type, "%" + reg));
        } else {
            error(ctx.getStart().getLine(), "no such variable");
        }
    }

    @Override
    public void exitArray_id(SPEEDYParser.Array_idContext ctx) {
        String ARRAY_ID = ctx.ARRAY_ID().getText();
        String[] split_array_id = ARRAY_ID.split("\\[");
        String id = split_array_id[0];
        String arrId = split_array_id[1].split("\\]")[0];
        if (variables.containsKey(id) || globalVariables.containsKey(id)) {
            String arrType = variables.get(id);
            if (arrType == null) {
                arrType = globalVariables.get(id);
            }
            String[] split_array_type = arrType.split("\\[");
            String type = split_array_type[0];
            String len = split_array_type[1].split("\\]")[0];
            int reg = -1;
            if (type.equals("int")) {
                reg = LLVMGenerator.loadIntArrayValue(getScope(id), arrId, len);
            } else if (type.equals("float")) {
                reg = LLVMGenerator.loadFloatArrayValue(getScope(id), arrId, len);
            }
            stack.push(new Value(type, "%" + reg));
        } else {
            error(ctx.getStart().getLine(), "no such array");
        }
    }

    @Override
    public void exitAdd(SPEEDYParser.AddContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type.equals(v2.type)) {
            if (v1.type.equals("int")) {
                LLVMGenerator.addInt(v1.value, v2.value);
                stack.push(new Value("int", "%" + (LLVMGenerator.reg - 1)));
            }
            if (v1.type.equals("float")) {
                LLVMGenerator.addFloat(v1.value, v2.value);
                stack.push(new Value("float", "%" + (LLVMGenerator.reg - 1)));
            }
        } else {
            error(ctx.getStart().getLine(), "addition type mismatch");
        }
    }

    @Override
    public void exitSub(SPEEDYParser.SubContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type.equals(v2.type)) {
            if (v1.type.equals("int")) {
                LLVMGenerator.subInt(v2.value, v1.value);
                stack.push(new Value("int", "%" + (LLVMGenerator.reg - 1)));
            }
            if (v1.type.equals("float")) {
                LLVMGenerator.subFloat(v2.value, v1.value);
                stack.push(new Value("float", "%" + (LLVMGenerator.reg - 1)));
            }
        } else {
            error(ctx.getStart().getLine(), "subtraction type mismatch");
        }
    }

    @Override
    public void exitMult(SPEEDYParser.MultContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type.equals(v2.type)) {
            if (v1.type.equals("int")) {
                LLVMGenerator.multInt(v1.value, v2.value);
                stack.push(new Value("int", "%" + (LLVMGenerator.reg - 1)));
            }
            if (v1.type.equals("float")) {
                LLVMGenerator.multFloat(v1.value, v2.value);
                stack.push(new Value("float", "%" + (LLVMGenerator.reg - 1)));
            }
        } else {
            error(ctx.getStart().getLine(), "multiplication type mismatch");
        }
    }

    @Override
    public void exitDiv(SPEEDYParser.DivContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type.equals(v2.type)) {
            if (v1.type.equals("int")) {
                LLVMGenerator.divInt(v2.value, v1.value);
                stack.push(new Value("int", "%" + (LLVMGenerator.reg - 1)));
            }
            if (v1.type.equals("float")) {
                LLVMGenerator.divFloat(v2.value, v1.value);
                stack.push(new Value("float", "%" + (LLVMGenerator.reg - 1)));
            }
        } else {
            error(ctx.getStart().getLine(), "division type mismatch");
        }
    }

    @Override
    public void enterBlockif(SPEEDYParser.BlockifContext ctx) {
        LLVMGenerator.ifstart();
    }

    @Override
    public void exitBlockif(SPEEDYParser.BlockifContext ctx) {
        LLVMGenerator.ifend();
    }

    @Override
    public void exitBlockelse(SPEEDYParser.BlockelseContext ctx) {
        LLVMGenerator.elseend();
    }

    @Override
    public void exitCondition(SPEEDYParser.ConditionContext ctx) {
        String ID = ctx.ID().getText();
        String operation = ctx.if_operation().getText();
        String value = ctx.comparable_value().getText();

        if (value.matches("^[a-zA-Z]+$")) {
            if (globalVariables.containsKey(ID) || variables.containsKey(ID) && (globalVariables.containsKey(value) || variables.containsKey(value))) {
                String type1 = "";

                if (globalVariables.containsKey(ID)) {
                    type1 = globalVariables.get(ID);
                } else if (variables.containsKey(ID)) {
                    type1 = variables.get(ID);
                }

                String type2 = "";

                if (globalVariables.containsKey(value)) {
                    type2 = globalVariables.get(value);
                } else if (variables.containsKey(value)) {
                    type2 = variables.get(value);
                }

                if (type1.equals(type2)) {
                    String operation_text = "";
                    switch (operation) {
                        case "==":
                            operation_text = "eq";
                            break;
                        case "!=":
                            operation_text = "ne";
                            break;
                        case "<":
                            operation_text = "slt";
                            break;
                        case ">":
                            operation_text = "sgt";
                            break;
                        case ">=":
                            operation_text = "sge";
                            break;
                        case "<=":
                            operation_text = "sle";
                            break;
                        default:
                            operation_text = "error";
                            break;
                    }
                    if (operation_text.equals("error")) {
                        error(ctx.getStart().getLine(), "unsupported operation");
                    }
                    if (type1.equals("int")) {
                        LLVMGenerator.icmp_vars(getScope(ID), getScope(value), "i32", operation_text);
                    } else if (type1.equals("float")) {
                        LLVMGenerator.icmp_vars(getScope(ID), getScope(value), "double", operation_text);
                    } else {
                        error(ctx.getStart().getLine(), "unsupported type");
                    }
                } else {
                    error(ctx.getStart().getLine(), "variables have different types");
                }
            } else {
                error(ctx.getStart().getLine(), "variable not defined");
            }

        } else {

            if (globalVariables.containsKey(ID) || variables.containsKey(ID)) {
                String type = "";
                if (globalVariables.containsKey(ID)) {
                    type = globalVariables.get(ID);
                } else if (variables.containsKey(ID)) {
                    type = variables.get(ID);
                }

                if ((type.equals("int") && value.contains("\\.")) || (type.equals("real") && !value.contains("\\."))) {
                    error(ctx.getStart().getLine(), "wrong type comparison");
                }
                String operation_text = "";
                switch (operation) {
                    case "==":
                        operation_text = "eq";
                        break;
                    case "!=":
                        operation_text = "ne";
                        break;
                    case "<":
                        operation_text = "ult";
                        break;
                    case ">":
                        operation_text = "ugt";
                        break;
                    case ">=":
                        operation_text = "uge";
                        break;
                    case "<=":
                        operation_text = "ule";
                        break;
                    default:
                        operation_text = "error";
                        break;
                }
                if (operation_text.equals("error")) {
                    error(ctx.getStart().getLine(), "unsupported operation");
                }
                if (type.equals("int")) {
                    LLVMGenerator.icmp_constant(getScope(ID), value, "i32", operation_text);
                } else if (type.equals("float")) {
                    LLVMGenerator.icmp_constant(getScope(ID), value, "double", operation_text);
                } else {
                    error(ctx.getStart().getLine(), "unsupported type");
                }
            } else {
                error(ctx.getStart().getLine(), "variable not defined");
            }
        }
    }


    @Override
    public void enterLoopblock(SPEEDYParser.LoopblockContext ctx) {
        String ID = ctx.condition().getChild(0).getText();
        LLVMGenerator.loopstart(getScope(ID));
    }

    @Override
    public void enterBlockfor(SPEEDYParser.BlockforContext ctx) {
        LLVMGenerator.loopblockstart();
    }

    public void exitBlockfor(SPEEDYParser.BlockforContext ctx) {
        LLVMGenerator.loopend();
    }

    @Override
    public void enterFunction(SPEEDYParser.FunctionContext ctx) {
        isGlobal = false;
        String id = ctx.ID().getText();
        String type = ctx.type().getText();
        functions.put(id, new ArrayList<String>());
        functions.get(id).add(type);
        if (type.equals("int")) {
            type = "i32";
        } else if (type.equals("float")) {
            type = "double";
        } else {
            error(ctx.getStart().getLine(), "unsupported return parameter");
        }
        LLVMGenerator.functionstart(id, type);
        SPEEDYParser.FparamsContext fp = ctx.fparams();
        while (fp != null) {
            SPEEDYParser.FparamsContext nfp = fp.fparams();
            String paramId = fp.ID().getText();
            String paramType = fp.type().getText();
            variables.put(paramId, paramType);
            functions.get(id).add(paramType);
            boolean last = false;
            if (nfp == null) {
                last = true;
            }
            if (paramType.equals("int")) {
                paramType = "i32";
            } else if (paramType.equals("float")) {
                paramType = "double";
            } else {
                error(ctx.getStart().getLine(), "unsupported function parameter");
            }
            LLVMGenerator.functionparams(paramId, paramType, last);
            fp = nfp;
        }
    }

    @Override
    public void exitReturn(SPEEDYParser.ReturnContext ctx) {
        String ID = ctx.ID().getText();
        String TYPE = variables.get(ID);
        if (TYPE == null) {
            error(ctx.getStart().getLine(), "variable not defined");
        }
        if (TYPE.equals("int")) {
            LLVMGenerator.loadInt(getScope(ID));
            TYPE = "i32";
        } else if (TYPE.equals("float")) {
            LLVMGenerator.loadFloat(getScope(ID));
            TYPE = "double";
        } else {
            error(ctx.getStart().getLine(), "unsupported return parameter");
        }
        LLVMGenerator.functionend(TYPE);
        variables = new HashMap<String, String>();
        isGlobal = true;
    }

    public String getScope(String ID) {
        String scopedID;
        if (isGlobal) {
            scopedID = "@" + ID;
        } else {
            if (!variables.containsKey(ID)) {
                scopedID = "@" + ID;
            } else {
                scopedID = "%" + ID;
            }
        }
        return scopedID;
    }


    private void error(int location, String msg) {
        System.out.println("Error at line " + location + ": " + msg);
    }

}
