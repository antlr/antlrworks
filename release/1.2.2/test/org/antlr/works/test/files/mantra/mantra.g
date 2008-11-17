/** Parse Mantra
 *
 *  Goals: build ASTs and create symbol table, but only add definitions
 *  in this phase.
 *
 *  Terence Parr and Jean Bovet
 */
grammar mantra;
options {
	output=AST;
	ASTLabelType = MantraAST;
}

tokens {
	UNIT;
	MODIFIER;
	FIELD;
	VARIABLE;
	METHOD;
	ARG;
	SLIST; // statement list
	ELIST; // expression list; args of method call, array ref
	CALL;
	INDEX;
	FIELDACCESS; // o.field
	CLOSURE; // x:{...}
    LIST; // [1,2]
    MAP; // [x=y, a="hi"]
    APPLY; // apply code to expression
    APPLY_STAT; // a:{...}; as isolated statement
    FILTER; // filter for closure x>0 in this: {int x, x>0 | ...}

    UNARY_MINUS; // unary operators
    UNARY_PLUS;
    UNARY_NOT;
	UNARY_BNOT;

	// define operators tokens for use in type system computations
	OR='||'; // arithmetic result operators
	AND='&&';
	BOR='|';
	BAND='&';
	XOR='^';
	RSHIFT='>>';
	LSHIFT='<<';
	PLUS='+';
	MINUS='-';
	MULT='*';
	DIV='/';
	MOD='%';
	ASSIGN='='; // assignment operators
	PLUS_EQ='+=';
	MINUS_EQ='-=';
	MULT_EQ='*=';
	DIV_EQ='/=';

	NEQ='!='; // boolean result operators
	EQ='==';
	GT='>';
	LT='<';
	GE='>=';
	LE='<=';
	IS='is';
	ISNT='isnt'; // last element used by TypeSystem.END_BOOL_OPS

	EXPR; // indicates root of an expression
	PROGRAM; // indicates direct execution of code; usually main

	CTOR; // constructor def

	SINK;
}

scope method {
  String name;
}

@header {
package mantra.compiler;
import mantra.sym.*;
import java.util.Set;
import java.util.HashSet;
}

@lexer::header {
package mantra.compiler;
}

@rulecatch {
catch (RecognitionException re) {
    reportError(re);
    recover(input,re);
}
catch (Throwable t) {
    t.printStackTrace(System.err);
}
}

@members {
Scope currentScope;
ClassSymbol enclosingClass;
SymbolTable symtab;
boolean inMethod = false;
Set referencedTypeNames = new HashSet();
public Set externallyDefinedTypeNames;
Set definedTypeNames = new HashSet();

public MantraParser(TokenRewriteStream input, SymbolTable symtab) {
	this(input);
	this.symtab = symtab;
}
}

compilationUnit
@after {
	referencedTypeNames.removeAll(definedTypeNames);
	externallyDefinedTypeNames = referencedTypeNames;
}
	:	{currentScope = symtab.getDefaultPkg();}
		packageDefinition?
		importDefinition*
		typeDefinition+
		-> ^(UNIT packageDefinition? importDefinition* typeDefinition*)
	|	{
		// technically this method lives in default package
		MethodSymbol m = new MethodSymbol("main",
							 SymbolTable.getPredefinedType("void"),
							 symtab.getDefaultPkg());
		// list args is predefined
		VariableSymbol args =
			new VariableSymbol("args", SymbolTable.getPredefinedType("list"));
		m.define("args", args);
		currentScope = m;
		currentScope = new LocalScope(currentScope);
		inMethod = true;
		}
		importDefinition* (s+=statement)+
		{
		currentScope = currentScope.getEnclosingScope(); // back to method
		currentScope = currentScope.getEnclosingScope(); // back to package
		}
		-> ^(PROGRAM $s+)
	;

packageDefinition
	:	'package' classname ';' -> ^('package' classname)
	;

importDefinition
	:	'import' packagename ';' -> ^('import' packagename)
	;

// A type definition in a file is either a class or interface definition.
typeDefinition
	:	modifiers! classDefinition[$modifiers.tree]
	|	modifiers! interfaceDefinition[$modifiers.tree]
	;

classDefinition[MantraAST mod]
scope {
  String name;
}
@init {
Scope saveScope=currentScope;
ClassSymbol cs=null;
}
@after {
((MantraAST)$tree.getChild(0)).scope = cs;
((MantraAST)$tree.getChild(0)).symbol = cs;
}
	:	'class' cname=ID
		('extends' sup=typename)?
		('implements' i+=typename (',' i+=typename)*)?
		{
		cs = new ClassSymbol($cname.text, null, (PackageScope)currentScope);
		$classDefinition::name = $cname.text;
		currentScope.define($cname.text, cs); // define in current scope
		currentScope = cs; // move current scope to class scope
		enclosingClass = cs;
		definedTypeNames.add($cname.text);
		}
		'{'
		(	variableDefinition
		|	methodDefinition[true]
		|	ctorDefinition
		)*
		'}'
		{
		System.out.println(cs);
		// back to either class or package; can't use getEnclosingScope
		// as that will yield superclass
		currentScope = saveScope;
		enclosingClass = null;
		}
		-> ^('class' ID {$mod} ^('extends' $sup)? ^('implements' $i+)?
		     variableDefinition* ctorDefinition* methodDefinition*
		    )
	;

interfaceDefinition[MantraAST mod]
@init {
Scope saveScope=currentScope;
ClassSymbol cs=null;
}
@after {
((MantraAST)$tree.getChild(0)).scope = cs;
((MantraAST)$tree.getChild(0)).symbol = cs;
}
	:	'interface' iname=ID ('extends' sup=classname)?
		{
		cs = new ClassSymbol($iname.text, null, (PackageScope)currentScope);
		currentScope.define($iname.text, cs); // define in current scope
		currentScope = cs; // move current scope to class scope
		enclosingClass = cs;
		definedTypeNames.add($iname.text);
		}
		'{'
		(	variableDefinition
		|	methodDefinition[false]
		)*
		'}'
		{
		System.out.println(cs);
		// back to either class or package; can't use getEnclosingScope
		// as that will yield superclass
		currentScope = saveScope;
		enclosingClass = null;
		}
		-> ^('interface' ID {$mod} ^('extends' $sup)?
			 variableDefinition* methodDefinition*
		    )
	;

methodDefinition[boolean needBody]
scope method;
@init {
MethodSymbol m=null;
inMethod = true;
}
@after {
((MantraAST)$tree.getChild(0)).symbol = m; // gross!
inMethod = false;
}
	:	modifiers typename mname=ID {$method::name=$ID.text;}
		{
		m = new MethodSymbol($ID.text,
							 null,
							 (ClassSymbol)currentScope);
		currentScope.define(m.name, m); // define in class sclope
		currentScope = m; // push arg scope
		}
		'(' formalArgs ')'
		(	{needBody}?  compoundStatement
		|	{!needBody}? ';'
		)
		{currentScope = currentScope.getEnclosingScope();} // pop arg scope
		-> ^(METHOD ID modifiers? typename formalArgs? compoundStatement?)
	;

ctorDefinition
scope method;
@init {
MethodSymbol m=null;
inMethod = true;
}
@after {
((MantraAST)$tree.getChild(0)).symbol = m; // gross!
inMethod = false;
}
	:	modifiers mname=ID
		{$mname.text.equals($classDefinition::name)}?
		{
		$method::name=$ID.text;
		m = new MethodSymbol($ID.text,
							 null,
							 (ClassSymbol)currentScope);
		currentScope.define(m.name, m); // define in class sclope
		currentScope = m; // push arg scope
		}
		'(' formalArgs ')' compoundStatement
		{currentScope = currentScope.getEnclosingScope();} // pop arg scope
		-> ^(CTOR ID modifiers? formalArgs? compoundStatement)
	;

formalArgs
	:	formalArg (',' formalArg)* -> formalArg+
	|
	;

formalArg
@init {
VariableSymbol a = null;
}
@after {
((MantraAST)$tree.getChild(1)).symbol = a;
}
	:	typename d=declarator
		{
		a = new VariableSymbol($d.text, null);
		currentScope.define(a.name, a);
		}
		-> ^(ARG typename declarator)
	;

declarator
	:	ID
	;

/** A field or local variable */
variableDefinition
@init {
VariableSymbol v=null;
}
@after {
// save symbol ptr in tree
((MantraAST)$tree.getChild(0)).symbol = v; // syntax is gross!
}
	:	modifiers typename vname=ID ('=' completeExpression)? ';'
		{
		v = new VariableSymbol($vname.text, null);
		currentScope.define(v.name, v);
		}
		-> {inMethod}? ^(VARIABLE ID modifiers? typename completeExpression?)
		-> 			   ^(FIELD ID modifiers? typename completeExpression?)
	;

modifiers
	:	modifier+ -> ^(MODIFIER modifier+)
	|
	;

modifier
	:	'public'
	|	'static'
	|	'const'
	|	'abstract'
	;

typename
@after {
// track scope for all type refs for future resolving after defs found
$typename.tree.scope = currentScope;
}
	: classname {referencedTypeNames.add($classname.text);}
	| builtInType
	;

builtInType
	:	'object'
    |   'void'
    |	'char'
	|	'boolean'
	|	'int'
	|	'float'
	|	'long'
	|	'double'
	|	'stream'
	|	'string'
	|	datastructure
	;


datastructure
	:	'set'
	|	'linkedlist'
	|	'list'
	|	'map'
	;

compoundStatement
@init {
Scope localScope=null;
}
@after {
$tree.scope = localScope;
}
	:	{currentScope = localScope = new LocalScope(currentScope);}
		lc='{' statement* '}'
		{currentScope = currentScope.getEnclosingScope();}
		-> ^(SLIST[$lc] statement*)
	;

statement
options {k=1;}
	:	(primary '++')=> primary '++'^ ';'!
	|	(primary '--')=> primary '--'^ ';'!
	|	(assignment)=> assignment
	|	(apply_closure) => apply_closure // isolated closure; make FOR loop
	|	(stream_sink)=> stream_sink
	|	(completeExpression ';')=> completeExpression ';'! // can this only be a function call?
	|	variableDefinition
	|	'if' '(' equalityExpression ')' s1=statement
		(	'else' s2=statement	-> ^('if' ^(EXPR equalityExpression) $s1 $s2)
		|						-> ^('if' ^(EXPR equalityExpression) $s1)
		)
	|	'return'^ completeExpression ';'!
	|	'break' ';' -> 'break'
	|	'print'^ completeExpression ';'! // allow for now; debugging
	|	compoundStatement
	|	';'!
	;

expressionList
	:	completeExpression (',' completeExpression)* -> ^(ELIST completeExpression+)
	|	-> ELIST
	;

assignment
	:	lvalue
		(	'='^
        |   '+='^
        |   '-='^
        |   '*='^
        |   '/='^
        )
		completeExpression
		';'!
	;

/** lhs of an assignment */
lvalue
	:	postfixExpression -> ^(EXPR postfixExpression)
	;

completeExpression
	:	expression
		-> ^(EXPR expression)
	;

expression
	:	conditionalExpression
	;

conditionalExpression
	:	logicalOrExpression
//		( '?'^ conditionalExpression ':'! conditionalExpression )?
// do we really need this?  Ambig with apply closure ':'
	;

logicalOrExpression
	:	logicalAndExpression ('||'^ logicalAndExpression)*
	;

logicalAndExpression
	:	inclusiveOrExpression ('&&'^ inclusiveOrExpression)*
	;

inclusiveOrExpression
	:	exclusiveOrExpression ('|'^ exclusiveOrExpression)*
	;

exclusiveOrExpression
	:	andExpression ('^'^ andExpression)*
	;

// bitwise or non-short-circuiting and (&)
andExpression
	:	equalityExpression ('&'^ equalityExpression)*
	;

equalityExpression
	:	relationalExpression (('!='^ | '=='^ | 'is'^ | 'isnt'^) relationalExpression)*
	;

relationalExpression
	:	shiftExpression
		(	(	(	'<'^
				|	'>'^
				|	'<='^
				|	'>='^
				)
				shiftExpression
			)*
		)
	;

shiftExpression
	:	additiveExpression (('<<'^ | '>>'^) additiveExpression)*
	;

additiveExpression
	:	multiplicativeExpression (('+'^ | '-'^) multiplicativeExpression)*
	;

postfixExpression
	:	(primary->primary) // set return tree
		(	lp='(' args=expressionList ')' 	-> ^(CALL $postfixExpression $args)
		|	lb='[' ie=expression ']' 		-> ^(INDEX $postfixExpression $ie)
		|	dot='.' p=primary      		   	-> ^(FIELDACCESS $postfixExpression $p)
		|	c=':' cl=closure[false]    	  	-> ^(APPLY ^(EXPR $postfixExpression) $cl)
		)*
	;

unaryExpression
	:	'-' unaryExpression -> ^(UNARY_MINUS unaryExpression)
	|	'+' unaryExpression -> ^(UNARY_PLUS unaryExpression)
	|	'!' unaryExpression -> ^(UNARY_NOT unaryExpression)
	|	'~' unaryExpression -> ^(UNARY_BNOT unaryExpression)
	|	postfixExpression
	;

multiplicativeExpression
	:	unaryExpression (('*'^ | '/'^ | '%'^) unaryExpression)*
	;

primary
options {k=1;}
    :	ID {$ID.tree.scope = currentScope;} // track scope for future resolve operation
	|	'new' typename '(' expressionList ')' -> ^('new' typename expressionList)
    |   ( '[' mapelement ) => mapliteral
    |   listliteral
	|	NUM_INT
	|	NUM_FLOAT
	|	STRING
	|	CHAR
	|	'null'
	|	'true'
	|	'false'
	|	'super'
	|	'this'
	|	'(' e=expression ')' -> $e
    |   closure[false]
	;


closure[boolean isolatedStatement]
@init {
Scope localScope=null;
}
@after {
$tree.scope = localScope;
}
	:	{currentScope = localScope = new ClosureScope(currentScope,isolatedStatement);}
		lc='{'
		typename d=declarator
		{
		VariableSymbol v = new VariableSymbol($d.text, null);
		currentScope.define(v.name, v);
		$d.tree.symbol = v;
		}
		(',' equalityExpression)?
		('|' statement+
        	-> ^(CLOSURE[$lc]
         	    ^(ARG typename declarator)
         	    ^(FILTER ^(EXPR equalityExpression))?
         	    ^(SLIST statement+)
         	   )
		|	-> ^(CLOSURE[$lc]
            	 ^(ARG typename declarator)
            	 ^(FILTER ^(EXPR equalityExpression))?
            	)
		)
		'}'
		{currentScope = currentScope.getEnclosingScope();}
	;

/** An isolated apply-closure statement like names:{string n | ...}; */
apply_closure
	:	primary ':' closure[true] ';' -> ^(APPLY_STAT ^(EXPR primary) closure)
	;

stream_sink
	:	completeExpression '=>' completeExpression ';' -> ^(SINK completeExpression+)
	;

listliteral
    :   '[' expressionList ']' -> ^(LIST expressionList)
    ;

mapliteral
    :   '[' mapelement (',' mapelement)* ']'  -> ^(MAP mapelement+)
    ;

mapelement
    :   completeExpression '='^ completeExpression
    ;

classname :	qid {$qid.tree.scope = currentScope;} ;

packagename	: qid {$qid.tree.scope = currentScope;} ;

/** A fully-qualified name such as mantra::object (class) or mantra::util (package) */
qid	:	ID ('::'^ ID)*
	;

ID	:	('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$')*
	;

NUM_INT
    : DECIMAL_LITERAL
    | HEX_LITERAL
    | OCTAL_LITERAL
    ;

fragment
DECIMAL_LITERAL: '1'..'9' ('0'..'9')* ('l'|'L')? ;

fragment
HEX_LITERAL: '0' ('x'|'X') ('0'..'9'|'a'..'f'|'A'..'F')+ ('l'|'L')? ;

fragment
OCTAL_LITERAL: '0' ('0'..'7')* ('l'|'L')? ;

NUM_FLOAT
    :     DIGITS '.' (DIGITS)? (EXPONENT_PART)? (FLOAT_TYPE_SUFFIX)?
    | '.' DIGITS (EXPONENT_PART)? (FLOAT_TYPE_SUFFIX)?
    |     DIGITS EXPONENT_PART FLOAT_TYPE_SUFFIX
    |     DIGITS EXPONENT_PART
    |     DIGITS FLOAT_TYPE_SUFFIX
    ;

fragment
DIGITS : ('0'..'9')+ ;

fragment
EXPONENT_PART: ('e'|'E') ('+'|'-')? DIGITS ;

fragment
FLOAT_TYPE_SUFFIX :   ('f'|'F'|'d'|'D') ;

STRING
    : '\"'
      ( options {greedy=false;}
      :	ESCAPE_SEQUENCE
      | ~'\\'
      )*
      '\"'
    ;

CHAR
    : '\''
      ( ESCAPE_SEQUENCE
      | ~'\''
      )
      '\''
    ;

fragment
ESCAPE_SEQUENCE
    :	'\\' 'b'
    |   '\\' 't'
    |   '\\' 'n'
    |   '\\' 'f'
    |   '\\' 'r'
    |   '\\' '\"'
    |   '\\' '\''
    |   '\\' '\\'
    |	'\\' '0'..'3' OCTAL_DIGIT OCTAL_DIGIT
    |   '\\' OCTAL_DIGIT OCTAL_DIGIT
    |   '\\' OCTAL_DIGIT
	|	UNICODE_CHAR
	;

fragment
HEX_DIGIT
	:	'0'..'9'|'a'..'f'|'A'..'F'
	;

fragment
OCTAL_DIGIT
	:	'0'..'7'
	;

fragment
UNICODE_CHAR
	:	'\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	;

SL_COMMENT
	:	'//' ~'\n'* '\n' {$channel=HIDDEN; }
	;

ML_COMMENT
	:	'/*'
		( options {greedy=false;} : . )*
		'*/'
		{$channel=HIDDEN;}
	;

WS	:	(	' '
		|	'\t'
		|	'\r'? '\n'
		)+
		{ $channel=HIDDEN; }
	;
