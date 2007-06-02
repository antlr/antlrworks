
import java.io.*;
import org.antlr.runtime.*;
import org.antlr.runtime.debug.DebugEventSocketProxy;

$import$

public class $class_name$ {

    public static void main(String args[]) throws Exception {
        $java_parser_lexer$ lex = new $java_parser_lexer$(new ANTLRFileStream("$input_file$"));
        CommonTokenStream tokens = new CommonTokenStream(lex);

        $java_parser$ g = new $java_parser$(tokens, $port$);
        try {
            g.$start_symbol$();
        } catch (RecognitionException e) {
            e.printStackTrace();
        }
    }
}
