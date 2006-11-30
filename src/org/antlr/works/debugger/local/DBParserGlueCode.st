
import java.io.*;
import org.antlr.runtime.*;

$import$

public class $class_name$ {

    public static void main(String args[]) throws Exception {
		//System.out.println("Parser launched");

        $java_parser_lexer$ lex = new $java_parser_lexer$(new ANTLRFileStream("$input_file$"));
        CommonTokenStream tokens = new CommonTokenStream(lex);

        $java_parser$ g = new $java_parser$(tokens);

        try {
            g.$start_symbol$();
        } catch (RecognitionException e) {
            e.printStackTrace();
        }

		//System.out.println("Parser stopped");
    }
}
