grammar arguments;

multExpr["double-quoted-string"]
    :   atom ('*'^ atom)*
    ;

atom:   INT
    |   ID
    ;

ID  :   ('a'..'z'|'A'..'Z')+ ;
INT :   '0'..'9'+ ;
NEWLINE:'\r'? '\n' ;
WS  :   (' '|'\t'|'\n'|'\r')+ {skip();} ;
