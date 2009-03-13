import sys
import antlr3
from $grammar_name$Lexer import $grammar_name$Lexer
from $grammar_name$Parser import $grammar_name$Parser


cStream = antlr3.FileStream('$input_file$')
lexer = $grammar_name$Lexer(cStream)
tokens = antlr3.CommonTokenStream(lexer)
parser = $grammar_name$Parser(tokens)
parser.$start_symbol$()
