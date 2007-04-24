grammar calc;

command
	:	line+;
	
line
	:	x=expr '\n'		{ System.out.println($x.value); }
	;
	
expr					returns [int value] @init { $value = 0; }
	:	x=term		{ $value = $x.value; }
		( '+' x=term		{ $value += $x.value; }
		| '-' x=term		{ $value -= $x.value; }
		)*
	;
	
term					returns [int value] @init { $value = 0; }
	:	x=factor		{ $value = $x.value; }
		( '*' x=factor		{ $value *= $x.value; }
		| '/' x=factor		{ $value /= $x.value; }
		)*
	;
	
factor					returns [int value ] @init { $value = 0; }
	:	i=INT			{ $value=Integer.parseInt($i.getText()); }
	|	'(' x=expr		{ $value = $x.value; }
		')'
	;
	
INT	:	('0'..'9')+
	;
	
VAR	:	('a'..'z'|'A'..'Z')+
	;

WS	:   	(' '|'\t'|'\r'|'\n')+	{ skip(); }
	; 
