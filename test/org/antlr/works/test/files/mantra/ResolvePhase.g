/** Walk class, method, var defs and set their missing types.
 *    The order is important
 *  classes, methods, then variable definitions.  Setting the type on
 *  a class means set it's superclass, which sets the enclosingScope for looking
 *  up types.
 *
 *  I'd prefer to pass in a tree that's really a list of all entity definitions
 *  and walk a subset of the tree for efficiency, but...ANTLR can't do subtree
 *  skipping yet...
 *
 *  This is harder than I thought to get the symtab right.  E.g., I need this
 *  resolve type phases rather than doing "forward" decl-style management where
 *  I simply define and fill in later when I have the def. Turns out, I don't know
 *  what scope to put class defs in.  Might be a nested class rather than a class
 *  in the same package.  I suppose one could put it in limbo w/o a scope until
 *  it is found, but still it's ambiguous: there might be two in nested scopes.
 *  Easier to just do another pass.
 *
 *  Terence Parr and Jean Bovet
 */
tree grammar ResolvePhase;

options {
  tokenVocab = Mantra;
  ASTLabelType = MantraAST;
}

@header {
package mantra.compiler;
import mantra.sym.*;
}

@members {
SymbolTable symtab;
public ResolvePhase(TreeNodeStream input, SymbolTable symtab) {
    this(input);
    this.symtab = symtab;
}
}

compilationUnit
	:	^(UNIT packageDefinition? importDefinition* typeDefinition+)
	|	^(PROGRAM importDefinition* statement+)
	;

packageDefinition
	:	^('package' classname)
	;

importDefinition
	:	^('import' packagename)
	;

// A type definition in a file is either a class or interface definition.
typeDefinition
	:	classDefinition
	|	interfaceDefinition
	;

classDefinition
@init {
ClassSymbol cs=null;
}
	:	^(	'class' cname=ID modifiers* {cs = ((ClassSymbol)$cname.symbol);}
			(	^('extends' sup=typename)
				{
				// resolve superclass
				ClassSymbol t = (ClassSymbol)$sup.start.scope.resolve($sup.text);
				cs.superClass = t;
				}
			|	{cs.superClass = SymbolTable.getPredefinedType("object");}
			)
			(
				^('implements' {cs.interfaceImplList = new ArrayList();}
			  	  (	impl=typename
			  	    {
					// resolve interface
					ClassSymbol t = (ClassSymbol)$impl.start.scope.resolve($impl.text);
					cs.interfaceImplList.add(t);
					}
			  	  )+
			  	 )
			)?
		    fieldDefinition* (methodDefinition|ctorDefinition)*
		 )
	;

interfaceDefinition 
	:	^(	'interface' ID modifiers* (^('extends' typename))?
		     fieldDefinition* methodDefinition*
		 )
	;

methodDefinition
scope {
  String name;
}
	:	^(METHOD mname=ID modifiers? t=typename formalArgs?
		  {
		  // we now can look up the return type and store it in the MethodSymbol
		  MethodSymbol m = (MethodSymbol)$ID.symbol;
		  m.type = $t.type;
		  System.out.println("RESOLVE: enter method "+m);
		  }
		  compoundStatement?
		  )
	;

ctorDefinition
scope {
  String name;
}
	:	^(CTOR mname=ID modifiers? formalArgs?
		  {
		  // we now can look up the return type and store it in the MethodSymbol
		  MethodSymbol m = (MethodSymbol)$ID.symbol;
		  System.out.println("RESOLVE: enter method "+m);
		  }
		  compoundStatement
		  )
	;

	
formalArgs
	:	(
			^(ARG t=typename ID)
			{
			// we now can look up the type and store it in the VariableSymbol
			VariableSymbol v = (VariableSymbol)$ID.symbol;
			v.type = $t.type;	
			}
		)+
	;

/** A field or local variable */
variableDefinition
	:	^(VARIABLE ID modifiers? t=typename completeExpression?)
		{
		// we now can look up the type and store it in the VariableSymbol
		VariableSymbol v = (VariableSymbol)$ID.symbol;
		v.type = $t.type;	
		}
	;

fieldDefinition
	:	^(FIELD ID modifiers? t=typename completeExpression?)
		{
		// we know can look up the type and store it in the VariableSymbol
		VariableSymbol v = (VariableSymbol)$ID.symbol;
		v.type = $t.type;;	
		}
	;

modifiers
	:	^(MODIFIER modifier+)
	;

modifier
	:	'public'
	|	'static'
	|	'const'
	|	'abstract'
	;

typename returns [ClassSymbol type]
@init {
MantraAST root = (MantraAST)input.LT(1);
$type = null;
}
@after {
root.symbol = $type;
if ( $type==null ) {
    Tool.error("unknown type reference: "+root.toStringTree());
}
}
	: classname		{$type = (ClassSymbol)$classname.start.scope.resolve($classname.text);}
	| builtInType	{$type = $builtInType.type;}
	;

builtInType returns [ClassSymbol type]
@init {
	$type = SymbolTable.getPredefinedType(((MantraAST)input.LT(1)).getText());
}
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
	:	^(SLIST statement*)
	|	SLIST
	;

statement
	:	^('++' primary)
	|	^('--' primary)
	|	assignment
	|	apply_stat
	|	stream_sink
	|	completeExpression
	|	variableDefinition
	|	^('if' completeExpression statement statement?)
	|	^('return' completeExpression)
	|	^('print' completeExpression) // allow for now; debugging
	|	compoundStatement
	|	'break'
	;

expressionList
	:	^(ELIST completeExpression+)
	|	ELIST
	;

assignment
	:	^(assign_op lvalue completeExpression)
	;

assign_op
	:	'='|'+='|'-='|'*='|'/='
	;

/** lhs of an assignment */
lvalue
	:	completeExpression
	;

completeExpression
	:	^(EXPR expression)
	;
	
expression
	:	^(binary_op expression expression)
	|	^(unary_op expression)
	|	^(CALL expression expressionList)
	|	^(INDEX expression expression)
	|	^(FIELDACCESS expression primary)
	|	^(APPLY completeExpression closure)
	|	primary
	;

binary_op
	:	OR|AND|BOR|BAND|XOR|RSHIFT|LSHIFT|PLUS|MINUS|MULT|DIV|MOD|LT|GE|LE|GT|NEQ|EQ|IS|ISNT
	;

unary_op
	:	UNARY_MINUS|UNARY_PLUS|UNARY_NOT|UNARY_BNOT
	;

primary
    :	ID
 	|	^('new' typename expressionList)
    |   mapliteral
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
    |   closure
	;

closure
	:	^(CLOSURE
             ^(ARG t=typename ID)
			 {
			 // we now can look up the type and store it in the VariableSymbol
			 VariableSymbol v = (VariableSymbol)$ID.symbol;
			 v.type = $t.type;	
			 }
             (^(FILTER completeExpression))?
          	 (^(SLIST statement+))?
         )
	;
	
apply_stat
	:	^(	APPLY_STAT
		  	completeExpression
		  	^(CLOSURE
          	   ^(ARG t=typename arg=ID)
				{
			 	// we now can look up the type and store it in the VariableSymbol
			 	VariableSymbol v = (VariableSymbol)$ID.symbol;
			 	v.type = $t.type;	
			 	}
          	   (^(FILTER completeExpression))?
          	   (^(SLIST statement+))?
          	 )
         )
    ;
   
stream_sink
	:	^(SINK completeExpression+)
	;
	
listliteral
    :   ^(LIST expressionList)
    ;

mapliteral
    :   ^(MAP mapelement+)
    ;

mapelement
    :   ^('=' completeExpression completeExpression)
    ;

classname :	qid ;

packagename	: qid ;
	
/** A fully-qualified name such as mantra::object (class) or mantra::util (package) */
qid	:	^('::' qid ID)
	|	ID
	;
