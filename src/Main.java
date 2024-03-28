import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ANTLRFileStream input = new ANTLRFileStream(args[0]);

        SPEEDYLexer lexer = new SPEEDYLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SPEEDYParser parser = new SPEEDYParser(tokens);

        ParseTree tree = parser.program();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new LLVMActions(), tree);
    }
}
