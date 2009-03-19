$grammar_type$ $grammar_name$;

$if(lex_id)$
ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

$endif$

$if(lex_int)$
INT :	'0'..'9'+
    ;

$endif$

$if(lex_float)$
FLOAT
    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT
    ;

$endif$

$if(lex_comment)$
COMMENT
$if(lex_slcomment)$
    :   '//' ~('\n'|'\r')* '\r'? '\n' {\$channel=HIDDEN;}
$if(lex_mlcomment)$
    |   '/*' ( options {greedy=false;} : . )* '*/' {\$channel=HIDDEN;}

$endif$
    ;

$else$
    :   '/*' ( options {greedy=false;} : . )* '*/' {\$channel=HIDDEN;}
    ;

$endif$
$endif$

$if(lex_ws)$
WS  :   ( ' '
$if(lex_tab)$
        | '\t'

$endif$
$if(lex_cr)$
        | '\r'

$endif$
$if(lex_lf)$
        | '\n'

$endif$
        ) {\$channel=HIDDEN;}
    ;

$endif$

$if(lex_string_single)$
STRING
    :  '\'' ( ESC_SEQ | ~('\\\\'|'\'') )* '\''
    ;

$endif$

$if(lex_string_double)$
STRING
    :  '"' ( ESC_SEQ | ~('\\\\'|'"') )* '"'
    ;

$endif$

$if(lex_char)$
CHAR:  '\'' ( ESC_SEQ | ~('\''|'\\\\') ) '\''
    ;

$endif$

$if(lex_float)$
fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

$endif$

$if(has_string_char_literal)$
fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\\\' ('0'..'7') ('0'..'7')
    |   '\\\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

$endif$