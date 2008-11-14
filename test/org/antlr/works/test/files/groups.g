grammar groups;

tokens {
    COMMENTED_TEXT;
}

@lexer::members {
boolean ultPic = false;

    private static final int MAX_EMIT_COUNT = 2;

    private Token [] myToken = new Token[MAX_EMIT_COUNT];
    private int add_idx = 0;
    private int next_idx = 0;

    public void emit(Token t) {
        token = t;
        myToken[add_idx++] = t;
    }

    public Token nextToken() {
        while (true) {
    	if ( add_idx == next_idx ) {
    	    token = null;
                add_idx = 0;
                next_idx = 0;
                channel = Token.DEFAULT_CHANNEL;
                tokenStartCharIndex = input.index();
                tokenStartCharPositionInLine = input.getCharPositionInLine();
                tokenStartLine = input.getLine();
                text = null;
                if (input.LA(1) == CharStream.EOF) {
            	return Token.EOF_TOKEN;
                }
                try {
            	mTokens();
            	if (token == null) {
            	    emit();
            	} else if (token == Token.SKIP_TOKEN) {
            	    continue;
            	}
                } catch (RecognitionException re) {
            	reportError(re);
            	recover(re);
                }
    	} else {
                Token result = myToken[next_idx++];
                if ( result != Token.SKIP_TOKEN || result != null ) {
                    return result;
                }
            }
        }
    }

}
// $<Program Unit

compilationUnit
	:	programUnit
	;


programUnit
	: 	identificationDivision
	;

identificationDivision
	:	ID_DIVISION
		( programIDPragraph | classIDPragraph | factoryIDPragraph | objectPragraph | methodIDPragraph )
		((AUTHOR|INSTALLATION|DATE_WRITTEN|DATE_COMPILED|SECURITY|REMARKS) COMMENTED_TEXT)*
		'environment'
	;

programIDPragraph
	:	'program-id' DOT? programName (programType)? DOT
	;

programName
	:	identifier
	;

programType
	:	'is'? ('recursive' | 'common' 'initial'? | 'initial' 'common'? ) 'program'?
	;

classIDPragraph
	:	'class-id' DOT className inheritsClause DOT
	;

className
	:	identifier
	;

inheritsClause
	:	'inherits' classNameRef
	;

classNameRef
	:	identifier
	;

factoryIDPragraph
	:	'factory' DOT
	;

objectPragraph
	:	'object' DOT
	;

methodIDPragraph
	:	'method-id' DOT? methodName DOT?
	;

methodName
	:	identifier
	;

// $>


identifier
	:	IDENTIFIER
	;

DOT	: '.'
	;

ID_DIVISION
	:	{getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}? ('id' | 'identification') (F_WS|F_NL)+ 'division' (F_WS|F_NL)* F_DOT
	;


AUTHOR
	:	{getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?=> 'author'
		{
			this.type = AUTHOR;
            emit();
            this.tokenStartCharIndex = getCharIndex();
		}
		(options {greedy=false;} : .)* {getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?
		{
			this.type = COMMENTED_TEXT;
            emit();
            this.tokenStartCharIndex = getCharIndex();
            input.mark();
		}
		('environment'|'data'|'procedure'|'author'|'installation'|'date-written'|'date-compiled'|'security'|'remarks')
		{input.rewind();}
	;

INSTALLATION
	:	{getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?=> 'installation'
		{
			this.type = INSTALLATION;
            emit();
            this.tokenStartCharIndex = getCharIndex();
		}
		(options {greedy=false;} : .)* {getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?
		{
			this.type = COMMENTED_TEXT;
            emit();
            this.tokenStartCharIndex = getCharIndex();
            input.mark();
		}
		('environment'|'data'|'procedure'|'author'|'installation'|'date-written'|'date-compiled'|'security'|'remarks')
		{input.rewind();}
	;

DATE_WRITTEN
	:	{getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?=> 'date-written'
		{
			this.type = DATE_WRITTEN;
            emit();
            this.tokenStartCharIndex = getCharIndex();
		}
		(options {greedy=false;} : .)* {getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?
		{
			this.type = COMMENTED_TEXT;
            emit();
            this.tokenStartCharIndex = getCharIndex();
            input.mark();
		}
		('environment'|'data'|'procedure'|'author'|'installation'|'date-written'|'date-compiled'|'security'|'remarks')
		{input.rewind();}
	;


DATE_COMPILED
	:	{getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?=> 'date-compiled'
		{
			this.type = DATE_COMPILED;
            emit();
            this.tokenStartCharIndex = getCharIndex();
		}
		(options {greedy=false;} : .)* {getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?
		{
			this.type = COMMENTED_TEXT;
            emit();
            this.tokenStartCharIndex = getCharIndex();
            input.mark();
		}
		('environment'|'data'|'procedure'|'author'|'installation'|'date-written'|'security'|'remarks')
		{input.rewind();}
	;


SECURITY
	:	{getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?=> 'security'
		{
			this.type = SECURITY;
            emit();
            this.tokenStartCharIndex = getCharIndex();
		}
		(options {greedy=false;} : .)* {getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?
		{
			this.type = COMMENTED_TEXT;
            emit();
            this.tokenStartCharIndex = getCharIndex();
            input.mark();
		}
		('environment'|'data'|'procedure'|'author'|'installation'|'date-written'|'date-compiled'|'remarks')
		{input.rewind();}
	;

REMARKS
	:	{getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?=> 'remarks'
		{
			this.type = SECURITY;
            emit();
            this.tokenStartCharIndex = getCharIndex();
		}
		(options {greedy=false;} : .)* {getCharPositionInLine() >= 7 && getCharPositionInLine() <= 10}?
		{
			this.type = COMMENTED_TEXT;
            emit();
            this.tokenStartCharIndex = getCharIndex();
            input.mark();
		}
		('environment'|'data'|'procedure'|'author'|'installation'|'date-written'|'date-compiled'|'security')
		{input.rewind();}
	;

AREA	:	'area' | 'areas'
	;

RECORD	:	'record' | 'records'
	;

ZERO	:	'zero'|'zeros'|'zeroes'
	;

JUST	:	'just'|'justified'
	;

SYNC	:	'sync'|'synchronized'
	;

COMP	:	('comp'|'computational') ('-' '1'..'5')?
	;

CORR	:	'corr'|'corresponding'
	;

DISPLAY	:	'display'|'display-1'
	;

NULL	:	'null'| 'nulls'
	;

SPACE	:	'space'|'spaces'
	;

HIGH_VALUE
	:	'high-value' | 'high-values'
	;

LOW_VALUE
	:	'low-value'|'low-values'
	;

QUOTE
	:	'quote' | 'quotes'
	;

PIC	:	'pic'| 'picture'
{
ultPic = true;
}
	;

PIC_VALUE
        :{ultPic}? ('+'|'-'|'$'|'a'|'b'|'/'|','|'e'|('0'..'9')|'x'|'s'|'v'|'z'|'*'|'('|')'|{ input.LA(2) != ' ' && input.LA(2) != '\n' && input.LA(2) != '\r' }?=> DOT)+
        {
                ultPic = false;
        }
        ;


IDENTIFIER
	: (DIGIT|'-')* LETTER ('-' | LETTER|DIGIT)*
	;

WS      : (' ' | '\t' )+
	{ token = Token.SKIP_TOKEN; }
        ;

NL 	: ('\r' | '\n')+
	{ token = Token.SKIP_TOKEN; }
    ;

fragment
F_WS      : (' ' | '\t' )
        ;

fragment
F_NL 	: ('\r' | '\n')
    ;

fragment
F_DOT 	: '.'
    ;

fragment
LETTER
	: 'a'..'z' | 'A'..'Z'
	;

fragment
DIGIT
	: '0'..'9'
	;
