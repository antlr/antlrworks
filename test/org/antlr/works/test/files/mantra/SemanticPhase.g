/** Do type promotion as necessary and compute expression types,
 *  storing into nodes for use by code gen phase.
 *
 *  Resolve ID reference nodes to point to proper Symbol.  Then
 *  we know their type.  This must be done after we've resolved
 *  types in a previous phase.
 *
 *  Terence Parr and Jean Bovet
 */
tree grammar SemanticPhase;

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
ClassSymbol enclosingMethodResultType = null;
Scope currentScope;
ClassSymbol currentClassDef; // bug in scopes; must do manually
public SemanticPhase(TreeNodeStream input, SymbolTable symtab) {
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
	:	^(	'class' cname=ID {currentClassDef = (ClassSymbol)$cname.symbol;}
			modifiers*
			(^('extends' sup=typename))?
			(^('implements' typename+))?
		    fieldDefinition* (methodDefinition|ctorDefinition)*
		 )
	;

interfaceDefinition 
	:	^(	'interface' iname=ID  {currentClassDef = (ClassSymbol)$iname.symbol;}
			modifiers* (^('extends' typename))?
		    fieldDefinition* methodDefinition*
		 )
	;

methodDefinition
	:	^(METHOD mname=ID
		  {enclosingMethodResultType = ((MethodSymbol)$mname.symbol).type;}
		  modifiers? typename formalArgs? compoundStatement? )
	;

ctorDefinition
	:	^(CTOR mname=ID
		  {enclosingMethodResultType = ((MethodSymbol)$mname.symbol).type;}
		  modifiers? formalArgs? compoundStatement?
		  )
	;

formalArgs
	:	(^(ARG typename ID))+
	;

/** A field or local variable */
variableDefinition
	:	^((VARIABLE|FIELD) ID modifiers? t=typename
		(	i=completeExpression
			{Object o=$i.start;} // fake ANTLR (remove assignment when fixed)
			{
			TypeSystem.assignment($t.start, $t.type,
		                          $i.start, $i.type);
		    }
		|
		)
		)
	;

fieldDefinition
	:	variableDefinition
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
}
	: classname		{$type = (ClassSymbol)$classname.start.scope.resolve($classname.text);}
	| builtInType	{$type = $builtInType.type;}
	;

builtInType returns [ClassSymbol type]
@init {
	$type = SymbolTable.getPredefinedType($builtInType.start.getText());
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
options {k=1;}
@init {
MantraAST start = (MantraAST)input.LT(1);
System.out.println("BEFORE STATEMENT "+start.toStringTree());
}
@after {
System.out.println("AFTER STATEMENT "+start.toStringTree());
}
	:	^('++' primary)
	|	^('--' primary)
	|	assignment
	|	apply_stat
	|	stream_sink
	|	completeExpression
	|	variableDefinition
	|	^('if' completeExpression statement statement?)
	|	^('return' rv=completeExpression)
		{
		TypeSystem.assignment(null, enclosingMethodResultType,
                          	  (MantraAST)$rv.start, $rv.type);
		}
	|	^('print' completeExpression) // allow for now; debugging
	|	compoundStatement
	|	'break'
	;

expressionList returns [List types]
@init {
$types=new ArrayList();
}
	:	^(ELIST (t=completeExpression {$types.add($t.type);})+)
	|	ELIST
	;

assignment returns [ClassSymbol type]
@init {
$type=null;
MantraAST root = (MantraAST)input.LT(1);
}
@after {
root.type = $type;
}
	:	^(assign_op x=lvalue y=completeExpression)
		{
		$type =
		  TypeSystem.assignment($x.start, $x.type,
		                        $y.start, $y.type);
		}
	;

assign_op
	:	'='|'+='|'-='|'*='|'/='
	;

/** lhs of an assignment */
lvalue returns [ClassSymbol type]
@init {$type=null;}
	:	completeExpression {$type = $completeExpression.type;}
	;

completeExpression returns [ClassSymbol type]
	:	^(EXPR expression {/* $expression.start fake out antlr to fix bug */})
		{$type = $expression.type;}
	;
	
expression returns [ClassSymbol type]
@init {
$type=null;
MantraAST root = (MantraAST)input.LT(1);
}
@after {
root.type = $type;
}
	:	^(bop=binary_op x=expression y=expression)
		{
		String tx = TypeSystem.promoteLeft($x.type.name,$bop.start.getType(),$y.type.name);
		if ( tx!=null && !tx.equals($x.type.name) ) {
			System.out.println("must promote "+$x.type.name+" to "+tx);
			$x.start.promoteTo = SymbolTable.getPredefinedType(tx);
		}
		String ty = TypeSystem.promoteRight($x.type.name,$bop.start.getType(),$y.type.name);
		if ( ty!=null && !ty.equals($y.type.name) ) {
			System.out.println("must promote "+$y.type.name+" to "+ty);
			$y.start.promoteTo = SymbolTable.getPredefinedType(ty);
		}
		String rt = TypeSystem.result(tx,$bop.start.getType(),ty);
		if ( rt!=null ) {
			$type = SymbolTable.getPredefinedType(rt);
			//System.out.println("result type is "+$type.name);
		}
		}
	|	^(uop=unary_op e=expression)
		{
		String rt = TypeSystem.result($uop.start.getType(), $e.type.name);
		if ( rt!=null ) {
			$type = SymbolTable.getPredefinedType(rt);
			//System.out.println("unary result type is "+$type.name);
		}
		}
	|	^(CALL m=expression args=expressionList)
		{
		if ( !($m.type instanceof MethodTypeClassSymbol) ) {
		 	Tool.error("not a method: "+$CALL.getChild(0).toStringTree());
		}
		else {
			MethodTypeClassSymbol mtype = (MethodTypeClassSymbol)$m.type;
			List promotedArgTypes = TypeSystem.promoteArgs(mtype.method, $args.types);
			//System.out.println("promoted types: "+promotedArgTypes);
			$type=mtype.method.type; // type of call is return type of method
			System.out.println("method return type: "+$type);
		}
		}
	|	^(i=INDEX a=expression expression)
		{
		if ( !TypeSystem.implementsInterface($a.type,SymbolTable.getPredefinedType("list")) &&
		     !TypeSystem.implementsInterface($a.type,SymbolTable.getPredefinedType("map")) )
		{
			Tool.error("not list/map type: "+$i.toStringTree());
		}    
		$type = SymbolTable.getPredefinedType("object"); // untyped data structures at this point
		}
	|	^(FIELDACCESS o=expression f=ID)
		{
		if ( !($o.type instanceof ClassSymbol) ) {
			System.err.println("not class symbol: "+$FIELDACCESS.getChild(0).toStringTree());
		}
		else {
			ClassSymbol fieldType = null;
			Symbol fieldSymbol = ((ClassSymbol)$o.type).resolve($f.text);
			if ( fieldSymbol==null ) {
				Tool.error("unknown member: "+$FIELDACCESS.getChild(1).toStringTree());
			}
			else {
     			if ( fieldSymbol instanceof MethodSymbol ) {
     				// it's a method; return instance of method type
	     			fieldType = new MethodTypeClassSymbol((MethodSymbol)fieldSymbol);
	     		}
	     		else {
    	 			// it's a var, get it's type
     				fieldType = fieldSymbol.type;
     			}
     		}
    		$type = fieldType;
		}
		}
	|	^(APPLY str=completeExpression code=closure)
		{
		if ( !($code.type instanceof MethodTypeClassSymbol) ) {
		 	Tool.error("not a closure block or method: "+$APPLY.getChild(1).toStringTree());
		}
		ClassSymbol streamType = SymbolTable.getPredefinedType("stream");
		$type = streamType; // result is always stream
		if ( $str.type != streamType ) {
			$str.start.promoteTo = streamType;
		}
    	System.out.println("semantics: stream type is "+$str.type);
		}
	|	primary {$type = $primary.type;}
	;

binary_op
	:	OR|AND|BOR|BAND|XOR|RSHIFT|LSHIFT|PLUS|MINUS|MULT|DIV|MOD|LT|GE|LE|GT|NEQ|EQ|IS|ISNT
	;

unary_op
	:	UNARY_MINUS|UNARY_PLUS|UNARY_NOT|UNARY_BNOT
	;

primary returns [ClassSymbol type]
@init {type=null; MantraAST root=(MantraAST)input.LT(1);}
@after {
System.out.println("primary "+root.toStringTree()+" type "+$type);
}
    :	ID
    	{
    	// we can now look up this symbol to find its def
    	Symbol s = $ID.scope.resolve($ID.text);
    	if ( s==null ) {
    		Tool.error("unknown symbol: "+$ID.text);
    	}
	    else {
	    	$ID.symbol = s; // make AST node point into symbol table
    		//System.out.println("semantics: found ref to "+$ID.symbol);
	    	if ( s instanceof MethodSymbol ) {
   				$type = new MethodTypeClassSymbol((MethodSymbol)s);
	    	}
	    	else {
    			$type = $ID.symbol.type;
    		}
    	}
    	}
	|	^('new' t=typename expressionList) {$type=$t.type;}
    |   mapliteral	{$type = $mapliteral.type;}
    |   listliteral	{$type = $listliteral.type;}
	|	NUM_INT		{$type = SymbolTable.getPredefinedType("int");}
	|	NUM_FLOAT	{$type = SymbolTable.getPredefinedType("float");}
	|	STRING		{$type = SymbolTable.getPredefinedType("string");}
	|	CHAR		{$type = SymbolTable.getPredefinedType("char");}
	|	'null'		{$type = SymbolTable.getPredefinedType("object");}
	|	'true'		{$type = SymbolTable.getPredefinedType("boolean");}
	|	'false'		{$type = SymbolTable.getPredefinedType("boolean");}
	|	'super'		{$type = currentClassDef.superClass;}
	|	'this'		{$type = currentClassDef;}
    |   closure		{$type = $closure.type;}
	;

closure returns [ClassSymbol type]
@init {
$type = new MethodTypeClassSymbol();
ClassSymbol save = enclosingMethodResultType;
enclosingMethodResultType = SymbolTable.getPredefinedType("object");
}
@after {
enclosingMethodResultType = save;
}
	:	^(CLOSURE
             ^(ARG typename ID)
             (^(FILTER completeExpression))?
             (^(SLIST statement+))?
         )
	;
	
apply_stat
@init {
ClassSymbol save = enclosingMethodResultType;
enclosingMethodResultType = SymbolTable.getPredefinedType("object");
}
@after {
enclosingMethodResultType = save;
}
	:	^(	APPLY_STAT
		  	str=completeExpression
		  	^(CLOSURE
          	   ^(ARG t=typename arg=ID)
          	   (^(FILTER cond=completeExpression))?
               (^(SLIST statement+))?
          	 )
         )
		{
		ClassSymbol streamType = SymbolTable.getPredefinedType("stream");
		if ( $str.type != streamType ) {
			$str.start.promoteTo = streamType;
		}
		}
    ;
    
stream_sink
	:	^(SINK str=completeExpression sink=completeExpression)
		{
		ClassSymbol streamType = SymbolTable.getPredefinedType("stream");
		ClassSymbol sinkType = SymbolTable.getPredefinedType("sink");
		if ( $str.type != streamType ) {
			$str.start.promoteTo = streamType;
		}
		if ( $sink.type != sinkType ) {
			$sink.start.promoteTo = sinkType;
		}
		}
	;

listliteral returns [ClassSymbol type]
@init {type=SymbolTable.getPredefinedType("list");}
    :   ^(LIST expressionList)
    ;

mapliteral returns [ClassSymbol type]
@init {type=SymbolTable.getPredefinedType("map");}
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

