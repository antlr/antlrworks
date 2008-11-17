grammar ignore_rules;

COMMENT 	:   	'/*' ( options {greedy=false;} : . )* '*/' { $channel=HIDDEN; }
		|	'**' ~( '\n' | '\r' )* '\r'? {$channel=HIDDEN;} ;
WHITESPACE	:	( ' ' | '\t' ) { $channel=HIDDEN; };
OTHER	:	( ' ' | '\t' ) { skip(); };
NEWLINE		:	( '\r' | '\u000C' | '\n' ) ;
