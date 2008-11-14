/** Generate code using StringTemplate.
 *
 *  Terence Parr and Jean Bovet
 */
tree grammar CodeGenPhase;

options {
  output=template;
  rewrite=true;
  tokenVocab = Mantra;
  ASTLabelType = MantraAST;
}

@header {
package mantra.compiler;
import mantra.sym.*;
}

@rulecatch {
catch (RecognitionException re) {
    reportError(re);
    recover(input,re);
}
catch (Throwable _t) {
    _t.printStackTrace(System.err);
}
}

@members {
TokenRewriteStream tokens;
SymbolTable symtab;
CodeGenerator generator;
public CodeGenPhase(TreeNodeStream input,
					TokenRewriteStream tokens,
					SymbolTable symtab,
					CodeGenerator generator)
{
    this(input);
    this.symtab = symtab;
    this.tokens = tokens;
    this.generator = generator;
}

	/** Given list of retvals, get rewritten text between start/stop index */
/* 3.0b6 made this unnecessary
	public List toText(List retvals) {
		if ( retvals==null ) return null;
		List strings = new ArrayList(retvals.size());
		for (int i=0; i<retvals.size(); i++) {
			TreeRuleReturnScope r = (TreeRuleReturnScope)retvals.get(i);
			MantraAST t = (MantraAST)r.start;
			strings.add(tokens.toString(t.startIndex, t.stopIndex));
		}
		return strings;
	}
*/

}

compilationUnit
	:	^(UNIT packageDefinition? importDefinition* typeDefs)
	|	^(PROGRAM importDefinition* statement[false]+)
	;

packageDefinition
	:	^('package' classname)
	;

importDefinition
	:	^('import' packagename)
	;

typeDefs
@init {
MantraAST root = (MantraAST)input.LT(1);
tokens.insertBefore(root.startIndex,%default_imports().toString());
}
	:	typeDefinition+
	;	


// A type definition in a file is either a class or interface definition.
typeDefinition
	:	classDefinition
	|	interfaceDefinition
	;

classDefinition
	:	^(	'class' cname=ID modifiers*
			(	^('extends' typename)
			|	{tokens.insertAfter($cname.token,
			                        %extend_root().toString());
			    }
			)
			(^('implements' typename+))?
		    fieldDefinition* (methodDefinition|ctorDefinition)*
		 )
	;

interfaceDefinition 
	:	^(	'interface' ID modifiers* (^('extends' typename))?
		     fieldDefinition* methodDefinition*
		 )
	;

methodDefinition
	:	^(METHOD mname=ID modifiers? typename formalArgs?
		  compoundStatement?
		 )
	;

ctorDefinition
	:	^(CTOR mname=ID modifiers? formalArgs?
		  compoundStatement
		 )
	;

formalArgs
	:	(
			^(ARG typename ID)
		)+
	;

/** A field or local variable */
variableDefinition
	:	^(VARIABLE ID modifiers? typename completeExpression? )
	;

fieldDefinition
	:	^(FIELD ID modifiers? typename completeExpression?)
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

typename
@init {
java.util.Map m = templateLib.getMap("mantraToJavaTypeMap");
StringTemplate translatedType = (StringTemplate)m.get($text);
}
	:	(	classname
		|	b=builtInType
    	|	'void'
    	)
    	-> {translatedType!=null}? {translatedType}
    	-> {%{$text}}
	;

builtInType
	:	'object'
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
	:	^(SLIST statement[false]*)
	|	SLIST
	;

statement[boolean inClosure]
	:	^('++' primary)
	|	^('--' primary)
	|	assignment
	|	a=apply_stat
	|	s=stream_sink
	|	completeExpression -> isolated_expr(e={$completeExpression.st})
	|	variableDefinition
	|	^('if' completeExpression statement[false] statement[false]?)

		// replace with result assignment if in {...} closure
	|	^(ret='return' rv=completeExpression)
		-> {$inClosure}? closure_return(v={$rv.st})
		-> {%{$statement.text}}

	|	^('print' pe=completeExpression) // allow for now; debugging
		-> print(e={$pe.st})
	|	compoundStatement
	|	'break'
	;

expressionList
	:	^(ELIST (a+=completeExpression)+)
	|	ELIST
	;

assignment
options {k=5;} // see down to INDEX
	:	// special case "a[i] = expr;"
		^('=' ^(EXPR ^(INDEX a=expression i=expression)) rhs=completeExpression)
		-> indexed_assignment(list={$a.st}, index={$i.st}, rhs={$rhs.st})
	|	^('=' lvalue completeExpression)
		-> assignment(
				lhs={$lvalue.text},
				rhs={$completeExpression.st})
	|	^(assign_op lvalue completeExpression)
		-> assignment_with_op(
				type={$assign_op.start.type.name},
				op={$assign_op.start.getText()},
				lhs={$lvalue.text},
				rhs={$completeExpression.st})
	;

assign_op
	:	'+='|'-='|'*='|'/='
	;

/** lhs of an assignment */
lvalue
	:	completeExpression
	;

completeExpression
	:	(
		^(EXPR expression)
		-> {$start.promoteTo == SymbolTable.getPredefinedType("stream")}?
		   stream_promote(v={$expression.st})
		-> {$start.promoteTo == SymbolTable.getPredefinedType("sink")}?
		   sink_promote(v={$expression.st})
		-> {$start.promoteTo == SymbolTable.getPredefinedType("list")}?
		   list_promote(v={$expression.st})
		-> {$start.promoteTo!=null}? promote(type={$start.promoteTo},
		                                     v={$expression.st})
		-> {$expression.st}
		)
		-> {$start.castTo!=null}? cast(type={$start.castTo},v={$st})
		-> {$st}
	;
	
expression
	:
	(	^(op=binary_op x=expression y=expression)
			-> ({generator.getOpST($op.start.token, $op.start.type)})(
					op={$op.start.getText()}, // can't use $op.text!
					type={$op.start.type.name},
					x={$x.st},
					y={$y.st})
	|	^(eop=equality_op x=expression y=expression)
			-> equality(
					op={$eop.start.getText()},
					x={$x.st},
					y={$y.st})
	|	^(peop=ptr_equality_op x=expression y=expression)
			-> ptr_equality(
					op={$peop.start.getText()},
					x={$x.st},
					y={$y.st})
	|	^(unary_op expression)
	|	^(CALL c=expression args=expressionList) -> call(m={$c.st},args={$args.text})
	|	^(INDEX a=expression i=expression)		 -> index(list={$a.st},e={$i.st})
	|	^(FIELDACCESS o=expression f=primary) 	 -> field(obj={$o.st},field={$f.text})
	|	apply_closure							 -> {$apply_closure.st}
	|	p=primary								 -> {%{$p.text}}
	)
	-> {$start.promoteTo!=null}? promote(type={$start.promoteTo},v={$st})
	-> {$st}
	;

binary_op
	:	OR|AND|BOR|BAND|XOR|RSHIFT|LSHIFT|PLUS|MINUS|MULT|DIV|MOD|LT|GE|LE|GT
	;

equality_op
	:	NEQ|EQ
	;

ptr_equality_op
	:	IS|ISNT
	;
	
unary_op
	:	UNARY_MINUS|UNARY_PLUS|UNARY_NOT|UNARY_BNOT
	;

primary
    :	ID
 	|	^('new' typename args=expressionList)
 		-> new(type={$typename.st},args={$args.text})
    |   mapliteral
    |   listliteral
	|	NUM_INT		-> int_literal(v={$NUM_INT.text})
	|	NUM_FLOAT	-> float_literal(v={$NUM_FLOAT.text})
	|	STRING		-> string_literal(s={$STRING.text})
	|	CHAR   		-> char_literal(c={$CHAR.text})
	|	'null'
	|	'true' 		-> boolean_literal(t={"true"})
	|	'false' 	-> boolean_literal(t={"false"})
	|	'super'
	|	'this'
    |   closure
	;

apply_closure
	:	^(	APPLY
		  	stream=completeExpression
		  	^(CLOSURE
          	   ^(ARG t=typename arg=ID)
          	   (^(FILTER cond=completeExpression))?
          	   (^(SLIST (s+=statement[true])+))?
          	 )
         )
         -> apply(stream={$stream.st},arg={$arg.text},type={$t.st},
                  condition={cond==null?null:cond.st},
                  statements={$s})
    ;
    
apply_stat
	:	^(	APPLY_STAT
		  	stream=completeExpression
		  	^(CLOSURE
          	   ^(ARG t=typename arg=ID)
          	   (^(FILTER cond=completeExpression))?
          	   (^(SLIST (s+=statement[true])+))?
          	 )
         )
         -> apply_stat(stream={$stream.st},arg={$arg.text},type={$t.st},
                  condition={cond==null?null:cond.st}, // use \$cond when antlr bug fixed
                  statements={$s})
    ;
    
closure
	:	^(CLOSURE
        	^(ARG typename ID)
            (^(FILTER completeExpression))?
            (^(SLIST statement[true]+))?
         )
	;
	
stream_sink
	:	^(SINK a=completeExpression b=completeExpression) -> shunt(stream={$a.st},sink={$b.st})
	;
	
listliteral
    :   ^(LIST expressionList) -> list_literal(elist={$expressionList.text})
    ;

mapliteral
    :   ^(MAP (e+=mapelement)+) -> map_literal(mlist={$e})
    ;

mapelement
    :   ^('=' k=completeExpression v=completeExpression)
    	-> map_element(key={$k.st}, value={$v.st})
    ;

classname :	qid ;

packagename	: qid ;
	
/** A fully-qualified name such as mantra::object (class) or mantra::util (package) */
qid	:	^('::' qid ID)
	|	ID
	;
