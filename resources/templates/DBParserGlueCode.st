
import java.io.*;
import org.antlr.runtime.*;
import org.antlr.runtime.debug.DebugEventSocketProxy;

$import$

public class $class_name$ {

    public static void main(String args[]) throws Exception {
        $java_lexer$ lex = new $java_lexer$(new ANTLRFileStream("$input_file$", "UTF8"));
        CommonTokenStream tokens = new CommonTokenStream(lex);

        $java_parser$ g = new $java_parser$(tokens, $port$, null);
        try {
            g.$start_symbol$();
        } catch (RecognitionException e) {
            e.printStackTrace();
        }
    }
}
