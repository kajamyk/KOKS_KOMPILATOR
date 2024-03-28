import java.util.Stack;

public class LLVMGenerator {

    static String header_text = "";
    static String main_text = "";
    static String buffer = "";
    static int main_tmp = 1;
    static int reg = 1;
    static int br = 0;
    static Stack<Integer> brstack = new Stack<Integer>();

    static void assignInt(String id, String value) {
        buffer += "store i32 " + value + ", i32* " + id + "\n";
    }

    static void assignFloat(String id, String value) {
        buffer += "store double " + value + ", double* " + id + "\n";
    }

    static void declareInt(String id, Boolean global) {
        if (global) {
            header_text += "@" + id + " = global i32 0\n";
        } else {
            buffer += "%" + id + " = alloca i32\n";
        }
    }

    static void declareFloat(String id, Boolean global) {
        if (global) {
            header_text += "@" + id + " = global double 0.0\n";
        } else {
            buffer += "%" + id + " = alloca double\n";
        }
    }

    static void printConstantInt(String value) {
        buffer += "%__internal__tmp = alloca i32\n";
        buffer += "store i32 " + value + ", i32* %__internal__tmp\n";
        buffer += "%" + reg + " = load i32, i32* %__internal__tmp\n";
        reg++;
        buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strp, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printConstantFloat(String value) {
        buffer += "%__internal__tmp = alloca double\n";
        buffer += "store double " + value + ", double* %__internal__tmp\n";
        buffer += "%" + reg + " = load double, double* %__internal__tmp\n";
        reg++;
        buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printInt(String id) {
        buffer += "%" + reg + " = load i32, i32* " + id + "\n";
        reg++;
        buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strp, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printFloat(String id) {
        buffer += "%" + reg + " = load double, double* " + id + "\n";
        reg++;
        buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";
        reg++;
    }

    static void scanInt(String id) {
        buffer += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* " + id + ")\n";
        reg++;
    }

    static void scanFloat(String id) {
        buffer += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strsd, i32 0, i32 0), double* " + id + ")\n";
        reg++;
    }


    static void addInt(String v1, String v2) {
        buffer += "%" + reg + " = add i32 " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void addFloat(String v1, String v2) {
        buffer += "%" + reg + " = fadd double " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void subInt(String v1, String v2) {
        buffer += "%" + reg + " = sub i32 " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void subFloat(String v1, String v2) {
        buffer += "%" + reg + " = fsub double " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void multInt(String v1, String v2) {
        buffer += "%" + reg + " = mul i32 " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void multFloat(String v1, String v2) {
        buffer += "%" + reg + " = fmul double " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void divInt(String v1, String v2) {
        buffer += "%" + reg + " = sdiv i32 " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void divFloat(String v1, String v2) {
        buffer += "%" + reg + " = fdiv double " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void declareIntArray(String id, String len, Boolean global) {
        if (global) {
            header_text += "@" + id + " = global [" + len + " x i32] [i32 0";
            for (int i = 1; i < Integer.parseInt(len); i++) {
                header_text += ", i32 0";
            }
            header_text += "] \n";
        } else {
            buffer += "%" + id + " = alloca [" + len + " x i32]\n";
        }
    }

    static void declareFloatArray(String id, String len, Boolean global) {
        if (global) {
            header_text += "@" + id + " = global [" + len + " x double] [double 0.0";
            for (int i = 1; i < Integer.parseInt(len); i++) {
                header_text += ", double 0.0";
            }
            header_text += "] \n";
        } else {
            buffer += "%" + id + " = alloca [" + len + " x double]\n";
        }
    }

    static void assignArrayIntElement(String value, String arrayId, String elemId, String len) {
        buffer += "%" + reg + " = getelementptr [" + len + " x i32], [" + len + " x i32]* " + arrayId + ", i32 0, i32 " + elemId + "\n";
        buffer += "store i32 " + value + ", i32* %" + reg + "\n";
        reg++;
    }

    static void assignArrayFloatElement(String value, String arrayId, String elemId, String len) {
        buffer += "%" + reg + " = getelementptr [" + len + " x double], [" + len + " x double]* " + arrayId + ", double 0, double " + elemId + "\n";
        buffer += "store double " + value + ", double* %" + reg + "\n";
        reg++;
    }

    static int loadInt(String id) {
        buffer += "%" + reg + " = load i32, i32* " + id + "\n";
        reg++;
        return reg - 1;
    }

    static int loadFloat(String id) {
        buffer += "%" + reg + " = load double, double* " + id + "\n";
        reg++;
        return reg - 1;
    }

    static int loadIntArrayValue(String id, String arrId, String len) {
        buffer += "%" + reg + " = getelementptr [" + len + " x i32], [" + len + " x i32]* " + id + ", i32 0, i32 " + arrId + "\n";
        reg++;
        buffer += "%" + reg + " = load i32, i32* %" + (reg - 1) + "\n";
        reg++;
        return reg - 1;
    }

    static int loadFloatArrayValue(String id, String arrId, String len) {
        buffer += "%" + reg + " = getelementptr [" + len + " x double], [" + len + " x double]* " + id + ", double 0, double " + arrId + "\n";
        reg++;
        buffer += "%" + reg + " = load double, double* %" + (reg - 1) + "\n";
        reg++;
        return reg - 1;
    }

    static void icmp_constant(String id, String value, String type, String cond) {
        buffer += "%" + reg + " = load " + type + ", " + type + "* " + id + "\n";
        reg++;
        buffer += "%" + reg + " = icmp " + cond + " " + type + " %" + (reg - 1) + ", " + value + "\n";
        reg++;
    }

    static void icmp_vars(String id1, String id2, String type, String cond) {
        buffer += "%" + reg + " = load " + type + ", " + type + "* " + id1 + "\n";
        reg++;
        buffer += "%" + reg + " = load " + type + ", " + type + "* " + id2 + "\n";
        reg++;
        buffer += "%" + reg + " = icmp " + cond + " " + type + " %" + (reg - 2) + ", %" + (reg - 1) + "\n";
        reg++;
    }

    static void ifstart() {
        br++;
        buffer += "br i1 %" + (reg - 1) + ", label %true" + br + ", label %false" + br + "\n";
        buffer += "true" + br + ":\n";
        brstack.push(br);
    }

    static void ifend() {
        int b = brstack.pop();
        buffer += "br label %end" + b + "\n";
        buffer += "false" + b + ":\n";
        brstack.push(b);
    }

    static void elseend() {
        int b = brstack.pop();
        buffer += "br label %end" + b + "\n";
        buffer += "end" + b + ":\n";
    }

    static void loopstart(String id) {
        br++;
        buffer += "br label %cond" + br + "\n";
        buffer += "cond" + br + ":\n";

        int tmp = loadInt(id);
        addInt("%" + Integer.toString(tmp), "1");
        assignInt(id, "%" + Integer.toString(reg-1));
    }

    static void loopblockstart() {
        buffer += "br i1 %" + (reg-1) + ", label %true" + br + ", label %false" + br + "\n";
        buffer += "true" + br + ":\n";
        brstack.push(br);
    }

    static void loopend() {
        int b = brstack.pop();
        buffer += "br label %cond" + b + "\n";
        buffer += "false" + b + ":\n";
    }
    
    static void functionstart(String id, String type) {
        main_text += buffer;
        main_tmp = reg;
        buffer = "define " + type + " @" + id + "(";
        reg = 1;
    }

    static void functionparams(String id, String type, boolean last) {
        buffer += type + "* %" + id;
        if (!last) {
            buffer += ", ";
        } else {
            buffer += ") nounwind {\n";
        }
    }

    static void functionend(String type) {
        buffer += "ret " + type + " %" + (reg - 1) + "\n";
        buffer += "}\n";
        header_text += buffer;
        buffer = "";
        reg = main_tmp;
    }

    static void call(String id, String type) {
        buffer += "%" + reg + " = call " + type + " @" + id + "(";
    }

    static void callparams(String id, String type, boolean last) {
        buffer += type + "* " + id;
        if (!last) {
            buffer += ", ";
        } else {
            buffer += ")\n";
        }
    }

    static void callfinal(String id, String type) {
        buffer += "store " + type + " %" + reg + ", " + type + "* " + id + "\n";
        reg++;
    }

    static void close_main() {
        main_text += buffer;
    }

    static String generate() {
        String text = "";
        text += "declare i32 @printf(i8*, ...)\n";
        text += "declare i32 @__isoc99_scanf(i8*, ...)\n";
        text += "@strp = constant [4 x i8] c\"%d\\0A\\00\"\n";
        text += "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n";
        text += "@strs = constant [3 x i8] c\"%d\\00\"\n";
        text += "@strsd = constant [4 x i8] c\"%lf\\00\"\n";
        text += header_text;
        text += "define i32 @main() nounwind{\n";
        text += main_text;
        text += "ret i32 0 }\n";
        return text;
    }
}
