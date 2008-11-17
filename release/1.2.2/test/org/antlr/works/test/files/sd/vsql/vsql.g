//-----------------------------------------------------------------------------
// Define a Parser
//-----------------------------------------------------------------------------
grammar VSQL_Parser;

options 
{
    k			= 2; 
//    backtrack	= true; 
//    memoize		= true;
    language	= C;
}


//-----------------------------------------------------------------------------
// This is list of imaginary tokens. This is such tokens that exists logically,
// but not reflected in some keyword of grammar. So we enter them manually into
// grammar.
// 
tokens
{
	ALTER_TRIGGER;
	BINDING;
	CASE_SIMPLE;
	CASE_SEARCHED;
	COLUMN_CONSTRAINT;
	COLUMN_CONSTRAINT_LIST;
	COLUMN_DEF;
	COLUMN_NAME_LIST;
	COLUMN_REFERENCE;
	CONST_STR;
	CONST_STR_HEX;
	CONST_NEG_UINT;
	CONST_NEG_DOUBLE;
	CONST_UINT;
	CONST_DOUBLE;
	CONST_DATE;
	CONST_DATETIME;
	CONST_TIME;
	CONST_TIMESTAMP;
	COLUMNS_LENS_LIST;
	COMPARE;
	CREATE_INDEX;
	CREATE_VIEW;
	CREATE_BINARY_LINK;
	CREATE_TRIGGER;
	CREATE_PROCEDURE;
	CREATE_FUNC;
	DATA_TYPE;
	DATABASE_CREATE;
	DATABASE_DROP;
	DECLARE_CURSOR;
	DECLARE_VARIABLE;
	DROP_INDEX;
	DROP_LINK;
	DROP_VIEW;
	DROP_TRIGGER;
	DROP_PROCEDURE;
	EXPR;
	EVENT_CREATE;
	EVENT_DROP;
	EVENT_ALTER;
	EXCEPTION_ERRORCODE;
	FUNC;
	VIEW_NAME_LIST;
	IN_PREDICATE;
	INSERT_INTO_TABLE;
	TBL_ELEM_LIST;
	TBL_CONSTRAINT_DEF;
	INDEX_CONSTRAINT_DEF;
	TABLE_DOT_COLUMN;
	TABLE_DOT_COLUMN_LIST;
	TABLE_NAME_LIST;
	TABLE_NAME_REFERENCE;
	TABLE_VALUE_CONSTRUCTOR;
	TRIM_ARGS;
	MAX_LENGTH;
	VARIABLE_NAME_LIST;
	NON_JOIN_TABLE;
	NULLS_ORDER_FIRST;
	SQL_STATEMENT;
	SCALE;
	SEARCH_CONDITION;
	SET_CLAUSE_LIST;
	SET_PROPERTY;
	SELECT_LIST;
	SELECT_ELEM_LIST;
	SHOW_COLUMNS;
	SHOW_LINKS;
	SHOW_STATUS;
	EVENT_SHOW;
	PRECISION;
	PRED_BOOL_TERM;
	PRED_BOOL_FACTOR;
	PRED_BOOL_TEST;
	PIVOT_FLAG;
	PROPERTY_NAME_LIST;
	RAISE_ERROR;
	ROW_EXPR;
	ROW_ELEM_LIST;
	ROLE_CREATE;
	ROLE_DROP;
	REGEX;
	UNARY_MINUS;
	QUERY_EXPRESSION;
	ZONE;
	LAST_IMAGINARY_TOKEN;		// name to determinate the end of this enum.
}


//---4.21-----------------------------------------------------------------------
// START POINT of SQL command.
sql
	:	sql_single ( SEMI! (sql_single)?  )* EOF!
	|	EOF!
	;

sql_single
	:	sql_statement //-> ^(SQL_STATEMENT  sql_statement )
	;

sql_statement
	:	sql_schema_statement
	|	sql_data_statement
	|	( ('set' 'transaction') | 'commit' | 'rollback' )=>sql_transaction_statement
	|	valentina_extensions
	;

sql_data_statement
	:	direct_select_statement_multiple_rows
	|	sql_data_change_statement
	|	with_common_table_expression
	;

sql_data_change_statement
	:	insert_statement
	|	delete_statement_searched
	|	update_statement_searched
	|	call_statement
	|	assign_statement
	|	v_link_records
	|	v_unlink_records
	;


//---5.2-----------------------------------------------------------------------
// #pragma mark 5.2 regular_identifier
regular_identifier
	:	IDENT
	;

delimited_identifier
	:	DELIMITED
	;


//---5.3-----------------------------------------------------------------------
// #pragma mark 5.3 literal
literal
	:	signed_numeric_literal
	|	general_literal
	;

unsigned_literal
	:	unsigned_numeric_literal
	|	general_literal
	;

general_literal
	:	datetime_literal
	|	character_string_literal
	|	hex_string_literal
	;

character_string_literal
@init{ STD::string st; }
	:	
		(	
			:s=STRING_LITERAL { st+=$s.text; } 
		)+
	;

hex_string_literal
	:	'x' s=character_string_literal //-> ^(CONST_STR_HEX, $s.text)
	;

signed_numeric_literal
	//( sign )? unsigned_numeric_literal
	:	(PLUS |  ) unsigned_numeric_literal
	|	MINUS
		(	u=UINT	 	//-> ^(CONST_NEG_UINT,   $u.text )	 
		|	f=NUM_FLOAT	//-> ^(CONST_NEG_DOUBLE, $f.text )   
		) 
	;

unsigned_numeric_literal!
	:	u=UINT			//-> ^(CONST_UINT,   $u.text )   
	|	f=NUM_FLOAT		//-> ^(CONST_DOUBLE, $f.text ) 
	;

datetime_literal
	:	date_literal
	|	time_literal
	|	date_time_literal
	|	timestamp_literal
	;

date_literal
	:	'date' s=STRING_LITERAL //-> ^(CONST_DATE, $s.text )
	;

time_literal
	:	'time' s=STRING_LITERAL //-> ^(CONST_TIME, $s.text ) 
	;

date_time_literal
	:	'datetime' s=STRING_LITERAL //-> ^(CONST_DATETIME, $s.text )
	;

timestamp_literal
	:	'timestamp' s=STRING_LITERAL //-> ^(CONST_TIMESTAMP, $s.text }
	;


//---5.4-----------------------------------------------------------------------
// #pragma mark 5.4 identifier
identifier
	:	actual_identifier
	;

actual_identifier
	:	regular_identifier
	|	delimited_identifier
	;

db_name
	:	identifier
	;

table_name
	:	identifier
	;

event_name
	:	identifier
	;


name
	:	identifier
	;

variable_name
	:	identifier 
	|	'x' { $type=IDENT; }  // hack - we want using X in variables
	;

view_name
	:	identifier
	;

trigger_name
	:	identifier
	;

proc_name
	:	identifier
	;

func_name
	:	identifier
	;

label_name
	:	identifier
	;

cursor_name
	:	identifier
	;

param_name
	:	identifier
	;

expression_name
	:	identifier
	;


table_alias
	:	identifier
	;


link_name
	:	identifier
	;

column_name
	:	identifier
	;

old_col_name
	:	identifier
	;

index_name
	:	identifier
	;

constraint_name
	:	identifier
	;

collation_name
	:	identifier
	;

correlation_name
	:	identifier
	;

local_table_name
	:	identifier
	;

property_name
	:	identifier
	;

authorization_identifier
	:	role_name
//	  |	user_identifier   -  it's IDENT too
	;

role_name
	:	identifier
	;

user_identifier
	:	identifier
	;

//---6  Scalar expressions-----------------------------------------------------
//---6.1-----------------------------------------------------------------------
// #pragma mark 6.1 data_type
data_type
	:	character_string_type
	|	numeric_type
	|	datetime_type
	|	blob_type
	|	objectptr_type
	;


character_string_type
@init{
   VALUE_TYPE res;
   std::string st = '1022'
}
	:	( 'character' | 'char' ) 
			(	'varying' LPAREN i=UINT RPAREN  	//-> ^(DATA_TYPE, 'kTypeVarChar' ^(MAX_LENGTH, $i.text) )
			|	LPAREN i=UINT RPAREN				//-> ^(DATA_TYPE, 'kTypeString'  ^(MAX_LENGTH, $i.text) )
			|	/*empty*/							//-> ^(DATA_TYPE, 'kTypeString'  ^(MAX_LENGTH, '1')     )
			)

	|		( 'varchar' (LPAREN i=UINT RPAREN)? 	//-> ^(DATA_TYPE, 'kTypeVarChar' ^(MAX_LENGTH, $i.text) )
			| 'string'  (LPAREN i=UINT RPAREN)?		//-> ^(DATA_TYPE, 'kTypeString'  ^(MAX_LENGTH, $i.text) )
			) 
	;


numeric_type
	:	exact_numeric_type
	|	approximate_numeric_type
	;

exact_numeric_type
@init{
	ulong res;
	bool sc = false;
}
	: 
		// ( LPAREN UINT RPAREN )?  
		// MySQL display width -> is used to left-pad the display of values having a width less than 
		// the width specified for the column.  ZERROFILL -> 00025
	(	'bit'		{ res = kTypeBoolean; 	} ( LPAREN UINT RPAREN )?
	|	'tinyint'	{ res = kTypeByte; 		} ( LPAREN UINT RPAREN )? ('unsigned' { res = kTypeByte;    } )?  ('zerofill')?
	|	'smallint'	{ res = kTypeShort; 	} ( LPAREN UINT RPAREN )? ('unsigned' { res = kTypeUShort;  } )?  ('zerofill')?
	|	'mediumint'	{ res = kTypeMedium; 	} ( LPAREN UINT RPAREN )? ('unsigned' { res = kTypeUMedium; } )?  ('zerofill')?
	|	'integer'	{ res = kTypeLong; 		} ( LPAREN UINT RPAREN )? ('unsigned' { res = kTypeULong;   } )?  ('zerofill')?
	|	'int'		{ res = kTypeLong; 		} ( LPAREN UINT RPAREN )? ('unsigned' { res = kTypeULong;   } )?  ('zerofill')?
	|	'bigint'	{ res = kTypeLLong; 	} ( LPAREN UINT RPAREN )? ('unsigned' { res = kTypeULLong;  } )?  ('zerofill')?
			
			// Valentina Types:
	|	'boolean'	{ res = kTypeBoolean; 	}
	|	'byte'		{ res = kTypeByte; 		}
	|	'uchar'		{ res = kTypeByte; 		}
	|	'short'		{ res = kTypeShort; 	}
	|	'ushort'		{ res = kTypeUShort;	}
	|	'medium'		{ res = kTypeMedium; 	}	
	|	'umedium'	{ res = kTypeUMedium;	}
	|	'long'		{ res = kTypeLong; 		}
	|	'ulong'		{ res = kTypeULong; 	}
	|	'llong'		{ res = kTypeLLong; 	}
	|	'ullong'		{ res = kTypeULLong; 	}
	)	
//		-> ^( DATA_TYPE ) 
	
	
	|	( 'numeric' | 'decimal' | 'dec' ) 
			( LPAREN precision ( COMMA scale )? RPAREN ) =>	LPAREN p=precision ( COMMA s=scale {sc = true;} )? RPAREN 
	;


approximate_numeric_type
@init{
	bool sc = false;
//	  bool sc2 = false;
}	
	:	'float' 
		(	( LPAREN precision ( COMMA scale )? RPAREN ) => LPAREN p=precision ( COMMA s=scale {sc = true;} )? RPAREN 
			|	
		)	('unsigned')?  ('zerofill')?
		
	|	'real' ('unsigned')? ('zerofill')?
	
	|	'double' ('precision')?
		(	( LPAREN precision ( COMMA scale )? RPAREN ) => LPAREN p2=precision ( COMMA s2=scale {sc = true;} )? RPAREN 
			|	
		)	('unsigned')?  ('zerofill')?
	;


datetime_type
@init{
	bool zone = false;
}
	:	'date'		 
	| ( 'datetime' | 'timestamp' ) 
		(	( LPAREN timestamp_precision RPAREN ) => LPAREN dp=timestamp_precision RPAREN ( 'with' 'time' 'zone' {zone = true;} )?
		|	( 'with' 'time' 'zone' {zone = true;})?
		)

	| 'time'
		(	( LPAREN time_precision RPAREN ) => LPAREN p=time_precision RPAREN ( 'with' 'time' 'zone' {zone = true;} )?
		|	( 'with' 'time' 'zone' {zone = true;})?
		)
	;

blob_type
@init
{
	VALUE_TYPE res;
	std::string st1 = '2044'
}
	:	'fixedbinary'   { res = kTypeFixedBinary; } 
				LPAREN p=UINT RPAREN
	|	'varbinary'	 { res = kTypeVarBinary; } 
				(LPAREN p1=UINT RPAREN {st1 = p1->getText();} )?
				
	|	 (  'blob'		{ res = kTypeBLOB; }
			| 'picture'	 	{ res = kTypePicture; }
			| 'tinytext'	{ res = kTypeText; }
			| 'text'		{ res = kTypeText; }
			| 'mediumtext'  { res = kTypeText; }
			| 'longtext'	{ res = kTypeText; }
		)
			(LPAREN p2=UINT RPAREN {st2 = p2->getText();} )?
	;


objectptr_type!
	:	'objectptr'	
	;


precision
	:	UINT
	;

scale
	:	UINT
	;

count
	:	UINT
	;

integer_value
	:	UINT
	;

time_precision
	:	time_fractional_seconds_precision
	;

timestamp_precision
	:	time_fractional_seconds_precision
	;

time_fractional_seconds_precision
	:	UINT
	;



//---6.2-----------------------------------------------------------------------
// #pragma mark 6.2 unsigned_value_specification
unsigned_value_specification
	:	unsigned_literal
	|	general_value_specification
	;

general_value_specification
	:	dynamic_parameter_specification
	|	'user'
	|	'current_user'
	|	'session_user'
	|	'system_user'
	|	'value'
	;

dynamic_parameter_specification!
	:	COLON u=UINT {  ## = #[BINDING, u->getText()]; }
	|	QUESTION
			{ 
/*				bindQuestionPos++;
				char buf[11 + 1];
				ulong len = sprintf(buf, "lu", bindQuestionPos );*/
			}
	;


//---6.3-----------------------------------------------------------------------
// #pragma mark 6.3 column_name_list
column_name_list
	:	column_name ( COMMA! column_name )*
		// ' ## = #([COLUMN_NAME_LIST,"COLUMN_NAME_LIST'
	;


view_name_list
	:	name ( COMMA! name )*
		// ' ## = #([VIEW_NAME_LIST,"VIEW_NAME_LIST'
	;


table_name_list
	:	table_name ( COMMA! table_name )*
		// ' ## = #([TABLE_NAME_LIST,"TABLE_NAME_LIST'
	;


variable_name_list
	:	variable_name ( COMMA! variable_name )*
		// ' ## = #([VARIABLE_NAME_LIST,"VARIABLE_NAME_LIST'
	;


table_dot_column_list
	:	table_dot_column ( COMMA! table_dot_column )*
		// ' ## = #([TABLE_DOT_COLUMN_LIST,"TABLE_DOT_COLUMN_LIST'
	;


//---6.4-----------------------------------------------------------------------
// #pragma mark 6.4 column_reference

// column_reference
//	  : ( qualifier DOT )? column_name
//
// RZ: Since this is VERY often used rule, we do not want to use the syntax predicate here.
// Trick is to convert the order of optional branches and do a manual building of AST tree 
// with the correct token types. 
// 
column_reference
	:	(	identifier 
			(	DOT! identifier ( PTR! identifier )*
			|	(PTR identifier)* 
			)
		|	('new' | 'old') DOT! identifier // for triggers
		)
		// ' ## = #([COLUMN_REFERENCE,"COLUMN_REFERENCE'
	;

table_dot_column
	:	table_name DOT! column_name
		// ' ## = #([TABLE_DOT_COLUMN,"TABLE_DOT_COLUMN'
	;

/*
	RZ: It is commented because of above trick with column_reference rule.
 
qualifier
	:	table_name
	|	correlation_name	// commented because the same IDENT
	;
*/


//---6.5-----------------------------------------------------------------------
// #pragma mark 6.5 set_function_specification
set_function_specification
	:	( 'count' LPAREN STAR ) => 'count'^ LPAREN! STAR RPAREN!	// count(*)
	|	general_set_function
	|	general_bit_function
	|	general_statistic_function
	|	xml_aggregate
	;

general_set_function
	:	sf=set_function_type!
			LPAREN! ( set_quantifier )? expr RPAREN!
	;

general_statistic_function
	:	( 'std'^ | 'stddev'^ | 'stddev_pop'^ )
			LPAREN! ( set_quantifier )? expr RPAREN!
		| ( 'corr'^ | 'covar'^ )
			LPAREN! expr COMMA! expr RPAREN!
	;

general_bit_function
	:	( 'bit_and'^ | 'bit_or'^ | 'bit_xor'^ )
			LPAREN! expr RPAREN!
	;


//---6.6-----------------------------------------------------------------------
// #pragma mark 6.6 numeric_value_function
numeric_value_function
	:	position_expression
	|	length_expression
	;

position_expression
	:	'position'^ LPAREN! expr
			'in'! expr RPAREN!
	;

length_expression
	:	char_length_expression
	|	octet_length_expression
	|	bit_length_expression
	;

char_length_expression
	:	( 'char_length'^ | 'chararacter_length'^ ) LPAREN! expr RPAREN!
	;

octet_length_expression
	:	'octet_length'^ LPAREN! expr RPAREN!
	;

bit_length_expression
	:	'bit_length'^ LPAREN! expr RPAREN!
	;


//---6.7-----------------------------------------------------------------------
// #pragma mark 6.7 string_value_function
string_value_function
	:	character_value_function
	;

character_value_function
	:	character_substring_function
	|	fold
	|	trim_function
	;

character_substring_function
	:	'substring'^ LPAREN! expr 'from'! start_position ( 'for'! string_length )? RPAREN!
	;

fold
	:	( 'upper'^ | 'lower'^ ) LPAREN! expr RPAREN!
	;

trim_function
	:	'trim'^ LPAREN! ( trim_specification )? trim_operands  RPAREN!
	;

// [ [ <trim character> ] FROM ] <trim source> // e1:expr ( "from" e2:expr )?
trim_operands!  // put source first and trim character second
	:	e1=expr
		(	'from' e2=expr  // ' ## = #([TRIM_ARGS,"TRIM_ARGS'
		)
	;

trim_specification
	:	'leading'
	|	'trailing'
	|	'both'
	;

start_position
	:	expr
	;

string_length
	:	expr
	;


//---6.8-----------------------------------------------------------------------
// #pragma mark 6.8 datetime_value_function
datetime_value_function
	:	current_date_value_function
	|	current_time_value_function
	|	current_timestamp_value_function
	;

current_date_value_function
	:	'current_date'
	;

current_time_value_function
	:	'current_time'
			(	(LPAREN time_precision RPAREN) => 
					LPAREN! time_precision! RPAREN!
			|	)
	;

current_timestamp_value_function
	:	'current_timestamp'
			(	(LPAREN! timestamp_precision RPAREN!) => 
						LPAREN! timestamp_precision! RPAREN! 
				|	)
	;



//---6.9-----------------------------------------------------------------------
// #pragma mark 6.9 case_expression
case_expression
	:	case_abbreviation
	|	case_specification
	;

case_abbreviation
	:	'nullif'^ LPAREN! expr COMMA!
			expr RPAREN!
	|	'coalesce'^ LPAREN! expr
			( COMMA! expr )* RPAREN!
	;

case_specification
	:	'case'^
			(	simple_case
			|	searched_case
			)
		'end'!
	;

simple_case
	:	case_operand
			( simple_when_clause )+
			( else_clause )?
	;

searched_case
	:	( searched_when_clause )+
			( else_clause )?
	;

simple_when_clause
	:	'when'^ when_operand 'then'! result
	;

searched_when_clause
	:	'when'^ search_condition 'then'! result
	;

else_clause
	:	'else'! result
	;

case_operand
	:	expr
	;

when_operand
	:	expr
	;

result
	:	expr | 'null'
	;


if_expression
	:	'if' LPAREN! 
				search_condition COMMA! expr COMMA! expr 
			 RPAREN! 
	;

//---6.10----------------------------------------------------------------------
// #pragma mark 6.10 cast_specification
cast_specification
	:	'cast'^ LPAREN! cast_operand 'as'! cast_target RPAREN!
	;

cast_operand
	:	expr
	|	'null'
	;

cast_target
	:	data_type
	;


//---6.11----------------------------------------------------------------------
// #pragma mark 6.11 expr
//
// We have join parts 6.11 6.12 and 6.13 of standard into single part
// toresolve non-determinism. Payment for this -- we do not have now 
// difference between numeric and character expression. We have just 'expr'. 
// Responibility to check types of operands must be placed on later phases of parsing.
//
expr
	:	expr2 ( (SHL^ | SHR^) expr2 )*
		'	## = #([EXPR,"EXPR'
	;

expr2
	:	term ( (PLUS^ | MINUS^ | CONCAT^) term )*
	;

term
	:	factor ( ( STAR^ | DIV^ | PERCENT^ ) factor )*
	;

factor
	:	( PLUS! | MINUS^ {#MINUS->setType(UNARY_MINUS);})? primary
	;

primary										  // was numeric_primary
	:	( (identifier | 'left' | 'right' | 'not' | 'replace'
		) LPAREN! ) => valentina_func
	|	value_expression_primary
	|	numeric_value_function
	|	string_value_function
	|	xml_value_function
	;

valentina_func
	:	(identifier 
		| 'left'	{##->setType(IDENT);}
		| 'right'	{##->setType(IDENT);}
		| 'not'	 {##->setType(IDENT);}
		| 'replace'	 {##->setType(IDENT);}
		) LPAREN! 
				( expr ( COMMA! expr )* 
				|
				)
			RPAREN! 
			'	## = #([FUNC,"FUNC'
	;

value_expression_primary
	:	unsigned_value_specification
	|	truth_value
	|	column_reference
	|	set_function_specification
	|	case_expression
	|	if_expression
	|	cast_specification
	|	(subquery) => subquery 
	|	LPAREN! expr /*( COMMA expr )**/ RPAREN!	// maybe EXPR_LIST | EXPR
	;


//---7  Query expressions------------------------------------------------------
//---7.1-----------------------------------------------------------------------
// #pragma mark 7.1 row_expr
row_expr
	:	(	(LPAREN! row_list_element COMMA) =>
				LPAREN! row_list_element ( COMMA! row_list_element )+ RPAREN!
				// ' ## = #([ROW_ELEM_LIST,"ROW_ELEM_LIST'
		|	(LPAREN! expr RPAREN!) =>	// for just one param in the () - (inParam1)
				LPAREN! expr RPAREN!
		|	row_list_element
		)
	;

row_list_element
	:	expr
	|	'null'
	|	'default'
		//	  INSERT INTO Table14 VALUES( NULL )
	|	( LPAREN 'null' RPAREN ) => LPAREN! 'null' RPAREN!
			//	  INSERT INTO Table14 VALUES( DEFAULT )
	|	( LPAREN 'default' RPAREN ) => LPAREN! 'default' RPAREN!
	;


//---7.2-----------------------------------------------------------------------
// #pragma mark 7.2 table_value_constructor
table_value_constructor_list
	:	row_expr ( COMMA! row_expr )*
	;


//---7.3-----------------------------------------------------------------------
// #pragma mark 7.3 table_expression
table_expression  returns[bool pivotFlag]
 	:	fc = from_clause { $pivotFlag = $fc.value; }
		where_clause ?
		group_by_clause ?
		having_clause ?
	;

//---7.4-----------------------------------------------------------------------
// #pragma mark 7.4 from_clause
//
// IF at least one table is PIVOT then falg pivotFlag becomes TRUE.
//
from_clause  returns[bool pivotFlag]
@init{ bool res; pivotFlag = false; }
	:	'from'^ res = table_reference[false] { if(res) pivotFlag = true; } 
			( COMMA! res = table_reference[false]  { if(res) pivotFlag = true; } )*
	;


//---7.5-----------------------------------------------------------------------
// #pragma mark 7.5 table_reference + join_table
table_reference[ bool IsFrom_query_primary ]  returns[bool pivotFlag]
@init{
	bool isNonJoin = true;
	bool isSuquery = false;
	bool tmp = false;
	pivotFlag = false;
}
	:	isSuquery = non_join_table
		( ( n='natural'! )? (t=join_type!)? 'join'^ tmp = non_join_table ( join_specification )?
			{ ##->addChild( #n ); ##->addChild( #t ); #n = antlr::nullAST; #t = antlr::nullAST; }
			{ isNonJoin = false; }
		|	'pivot'^ pivot_clause 'as'! table_alias  { pivotFlag = true; }
		)*
		{ 
			if( IsFrom_query_primary )
			{
				if( isNonJoin && !isSuquery )
					throw antlr::NoViableAltException(LT(1), getFilename());
			}
		}
	;

non_join_table returns[bool res]
@init{
	res = false;
}
	:	( table_name ( ('as'!)? table_alias |  )
	|	subquery ( ('as'!)? correlation_name | ) { res = true; }
	)	'	## = #([NON_JOIN_TABLE,"NON_JOIN_TABLE'
	;

//pivoted_table
//	  :	table_reference "pivot" pivot_clause "as" table_alias
//	  ;


pivot_clause
	:	LPAREN! general_set_function 'for'! column_name 
				'in'! LPAREN! reference_column_list RPAREN! 
		RPAREN!
	;

join_specification
	:	'on'^ 
			(	(link_name)=>link_name		  // valentina extension: link
			|	search_condition
			)
	|	'using'^ LPAREN! column_name_list RPAREN!
	;

join_type
	:	('left' | 'right' | 'full' ) ( 'outer'! )?
	|	'inner'
// TODO |	"union"
// TODO |	"cross"
	;


//---7.6-----------------------------------------------------------------------
// #pragma mark 7.6 where_clause
where_clause
	:	'where'^ search_condition
	;

//---7.7-----------------------------------------------------------------------
// #pragma mark 7.7 group_by_clause
group_by_clause
	:	'group'^ 'by'! refs=grouping_column_reference_list! ('with'! 'rollup')?
		// we move info about rollup forward to refs, to simplify job in the TreeParser.
		{ 
			##->addChild(#refs); 
			#refs = antlr::nullAST;
		}
	;

grouping_column_reference_list
	:	grouping_column_reference ( COMMA! grouping_column_reference )*
	;

grouping_column_reference
	:	column_reference
	;

//---7.8-----------------------------------------------------------------------
// #pragma mark 7.8 having_clause
having_clause
	:	'having'^ search_condition
	;


//---7.9-----------------------------------------------------------------------
// #pragma mark 7.9 select_list
select_list
	:	select_sublist ( COMMA! select_sublist )*
		'	## = #([SELECT_LIST,"SELECT_LIST'
	;

select_sublist
	:	((table_name DOT! (STAR | DSTAR)) => table_name DOT! (STAR | DSTAR)
	|	STAR
	|	DSTAR
	|	'null' ( ( 'as'! )? ( character_string_literal | identifier ) )?
	|	expr ( ( 'as'! )? ( character_string_literal | identifier ) )? 
	)	'	## = #([SELECT_ELEM_LIST,"SELECT_ELEM_LIST'
	;


//---7.10----------------------------------------------------------------------
// #pragma mark 7.10 query_expression
query_expression
	:	query_term (( 'union'^ | 'except'^ ) ( 'all' )? query_term)*
		'	## = #([QUERY_EXPRESSION,"QUERY_EXPRESSION'
	;

query_term
	:	query_primary ('intersect'^ ( 'all' )?  query_primary)*
	;

query_primary
@init{ bool pivotFlag; bool dummy; }
	:	'values'^ table_value_constructor_list
	|	'table'^ table_name
//	  |	"link"^ link_name		// valentina extension
	|   show_statement
	|	'select'^ ( set_quantifier )? select_list
		('into' variable_name_list)? 
			( pivotFlag = table_expression  
				' if(pivotFlag) ##->addChild( #[PIVOT_FLAG,"PIVOT_FLAG'
			)?
	|	dummy = table_reference[true]
	;


//---7.11----------------------------------------------------------------------
// #pragma mark 7.11 subquery
subquery
	:	LPAREN! query_expression RPAREN!
	;


//---8  Predicates-------------------------------------------------------------
//---8.2-----------------------------------------------------------------------
// #pragma mark 8.2 comparison_predicate

comp_op
	:	EQ
	|	NE
	|	NEC
	|	LT_
	|	GT
	|	LE
	|	GE
	|	OL
	|	OR
	|	OF
	;


//---8.4-----------------------------------------------------------------------
// #pragma mark 8.4 in_predicate
in_predicate_value
	:	( (LPAREN 'select') => subquery
	|	LPAREN! expr ( COMMA! expr )* RPAREN!
	)	// ' ## = #(#[IN_PREDICATE,"IN_PREDICATE'
	;


//---8.7----------------------------------------------------------------------
// #pragma mark 8.7 quantified_comparison_predicate

quantifier
	:	'all' | 'some' | 'any'
	;

//---8.12----------------------------------------------------------------------
// #pragma mark 8.12 search_condition
search_condition
	:	boolean_term ( 'or'! boolean_term )*
	'	## = #([SEARCH_CONDITION,"SEARCH_CONDITION'
	;

boolean_term
	:	boolean_factor ( 'and'! boolean_factor )*
	'	## = #([PRED_BOOL_TERM,"PRED_BOOL_TERM'
	;

boolean_factor
	//	  TODO: we have warning here because in valentina_func we add "not"
	:	( 'not' )? boolean_test
	'	## = #([PRED_BOOL_FACTOR,"PRED_BOOL_FACTOR'
	;

boolean_test
	:	boolean_primary ( 'is' ( 'not' )? truth_value )?
	'	## = #([PRED_BOOL_TEST,"PRED_BOOL_TEST'
	;

truth_value
	:	'true'
	|	'false'
	|	'unknown'
	;

boolean_primary
	:	'exists'^ subquery		  //exists_predicate
	|	'unique'^ subquery		  //unique_predicate
	|	(row_expr (comp_op | 'is' | 'overlaps' | 'match' | (('not')? ('between' | 'in')) ) ) => 
		row_expr 
		(
			co=comp_op! 
				(	(quantifier) => quantifier subquery //quantified_comparison_predicate
				|	row_expr							//comparison_predicate
				) // ' ## = #( #[COMPARE,"COMPARE'
			| 'is'! ( 'not' )? 'null'^	  //null_predicate
			| 'overlaps'^ row_expr //overlaps_predicate
			| 'match'^ ( 'unique' )? ( 'partial' | 'full' )? subquery //match_predicate
			| ( 'not' )? 
				(	'between'^ row_expr 'and'! row_expr //between_predicate
				|	'in'^ in_predicate_value	//in_predicate
				)
		)
	|	('false')=>'false'
	|	(expr)=> expr
				(	('not')? 
						( 'like'^ expr ( 'escape'! expr )? //like_predicate
						| ( 'regex'! | 'regexp'! ) expr // ' ##=#( [REGEX,"REGEX'//regex_predicate
						)
					|
				)
	|	LPAREN! search_condition RPAREN!
	;


//---10.6----------------------------------------------------------------------
constraint_name_definition
	:	'constraint'^ constraint_name
	;

//---10.6------<specific routine designator>------------------------------------
/*specific_routine_designator
	:	"specific" routine_type specific_name
	|	routine_type member_name ( "for" schema_resolved_user_defined_type_name )?
	;

routine_type
	:	"routine"
	|	"function"
	|	"procedure"
	|	( "instance" | "static" | "constructor" )? "method"
	;

member_name 
	:	member_name_alternatives  ( data_type_list )?
	;

member_name_alternatives
	:	schema_qualified_routine_name
	|	method_name
	;

data_type_list
	:	LPAREN! ( data_type (COMMA! data_type)* )? RPAREN!
	;
*/

//---11.2----------------------------------------------------------------------
// #pragma mark 11.2 drop_behavior
drop_behavior
	:	'cascade' | 'restrict'
	;


//---11.3----------------------------------------------------------------------
// #pragma mark 11.3 table_definition
table_definition
	:	'create'^ ( ( 'global' | 'local' )? 'temporary' )? (('ram') | ('system'))?
			'table'! ('if'! 'not'! 'exists')? table_name 
				(	table_element_list
						( 'on'! 'commit'! ( 'delete' | 'preserve' ) 'rows'! )?
				|	'as'! direct_select_statement_multiple_rows
				)
	;

table_element_list
	:	LPAREN! table_element ( COMMA! table_element )* RPAREN!
		// ' ## = #(#[TBL_ELEM_LIST,"TBL_ELEM_LIST'
	;

table_element
	:	column_definition
	|	table_constraint_definition
	;


//---11.4----------------------------------------------------------------------
// #pragma mark 11.4 column_definition
column_definition
	:	column_name data_type
//		  ( default_clause )?		 we need to break the sql92 standart, because MySql do this :(
//								  and we have problem when do MySql db import
//								  def not null	<->	not null def  
		( column_constraint_definition )?
		// ' ## = #(#[COLUMN_DEF, "COLUMN_DEF'
	;

column_constraint_definition
	:	( column_constraint )+
		// ' ## = #(#[COLUMN_CONSTRAINT_LIST, "COLUMN_CONSTRAINT_LIST'
	;

column_constraint
	:	( constraint_name_definition )?
	(	'not' 'null'!
	|	'null'
	|	'indexed'
	|	'words'
	|	'compressed'
	|	'autoincrement'
	|	'auto_increment'
	|	'identity'
	|	default_clause	  // hack - described above
	|	unique_specification
	|	references_specification
	|	check_constraint_definition
	|	  'collate' collation_name
	|	  'on' 'update' default_option  /*NON STANDARD -- for mySQL dump parsing*/
	|	valentina_method )
	// ' ## = #(#[COLUMN_CONSTRAINT, "COLUMN_CONSTRAINT'
	;

valentina_method
	:	'method' LPAREN! STRING_LITERAL RPAREN!
	;


//---11.5----------------------------------------------------------------------
// #pragma mark 11.5 default_clause
default_clause
	:	'default'^ default_option
	;

default_option
	:	literal 
	|	datetime_value_function
	|	'user'
	|	'current_user'
	|	'session_user'
	|	'system_user'
	|	'null'
	;

//---11.6----------------------------------------------------------------------
// #pragma mark 11.6 table_constraint_definition
table_constraint_definition
	:	table_constraint
	;

table_constraint
	:	(	constraint_name_definition )?
		(	unique_constraint_definition
		|	referential_constraint_definition
		|	check_constraint_definition
		|	index_constraint_definition
		)	// ' ## = #(#[TBL_CONSTRAINT_DEF, "TBL_CONSTRAINT_DEF'
	;

index_constraint_definition
	:	('index'! | 'key'! ) (index_name)? index_column_list
		// ' ## = #(#[INDEX_CONSTRAINT_DEF, "INDEX_CONSTRAINT_DEF'
	;


index_column_list
	:	LPAREN! 
			column_name ( LPAREN! UINT RPAREN! )?
			(COMMA! column_name ( LPAREN! UINT RPAREN! )? )* 
		RPAREN!
		// ' ## = #(#[COLUMNS_LENS_LIST, "COLUMNS_LENS_LIST'
	;

//---11.7----------------------------------------------------------------------
// #pragma mark 11.7 unique_constraint_definition
unique_constraint_definition
	:	( 'unique'^ (index_name)? 
		| 'primary'^ 'key'!)
			index_column_list
	;

unique_specification
	:	'unique'
	|	'primary' 'key'!
	;


//---11.8----------------------------------------------------------------------
// #pragma mark 11.8 referential_constraint_definition
referential_constraint_definition
	:	'foreign'^ 'key'!
		LPAREN! referencing_columns RPAREN!
		references_specification
	;

references_specification
	:	'references'^ referenced_table_and_columns
		( 'match'! match_type )?
		( referential_triggered_action )?
	;

match_type
	:	'full'
	|	'partial'
	;

referencing_columns
	:	reference_column_list
	;

referenced_table_and_columns
	// TODO may be possible to optimise ?
	:	table_name 
		(	(LPAREN reference_column_list RPAREN) => LPAREN! reference_column_list RPAREN! 
			| 
		)
	;

reference_column_list
	:	column_name_list
	;

referential_triggered_action
	// in tree must be checked that update_rule | delete_rule only once time
	: 'on'! 
		(	'update' referential_action (('on' 'delete') => ('on'! 'delete' referential_action) | )
		|	'delete' referential_action (('on' 'update') => ('on'! 'update' referential_action) | )
		)
	;

referential_action
	:	'cascade'
	|	'set'! ( 'null' | 'default' )
	|	'no'! 'action'
	|	'restrict'
	;


//---11.9----------------------------------------------------------------------
// #pragma mark 11.9 check_constraint_definition
check_constraint_definition
	:	'check'^ LPAREN! search_condition RPAREN!
	;

//---11.10---------------------------------------------------------------------
// #pragma mark 11.10 alter_table_statement
alter_table_statement
	:	'alter'^ 'table'! table_name alter_table_action ( COMMA! alter_table_action )*
	;

alter_table_action	  // "add" ("column")? (column_definition, column_definition,...)
					// "add" (table_constraint_definition, table_constraint_definition,...)
	:	'add'^ 
		(	'column'! 
				(	column_definition 
				|	LPAREN! column_definition ( COMMA! column_definition )* RPAREN!
				)
		|	(	column_definition
			|	table_constraint_definition 
			|	LPAREN! 
					( column_definition ( COMMA! column_definition )*
					| table_constraint_definition ( COMMA! table_constraint_definition )* )
				RPAREN!
			)
		)
	|	'alter'^ ( 'column'! )? column_name alter_column_action
	|	'drop'^ 
			(	( 'column'! )? column_name ( drop_behavior )?
			|	'primary' 'key'!
			|	'index' index_name
			|	constraint_name_definition
			)
	|	'change'^ ( 'column'! )? old_col_name column_definition
	|	'modify'^ ( 'column'! )? column_definition
	|	'rename'^ ('as'!)? table_name
	;


//---11.12---------------------------------------------------------------------
// #pragma mark 11.12 alter_column_definition
alter_column_action
	:	set_column_default_clause
	|	drop_column_default_clause
	;


//---11.13---------------------------------------------------------------------
// #pragma mark 11.13 set_column_default_clause
set_column_default_clause
	:	'set'^ default_clause
	;


//---11.14---------------------------------------------------------------------
// #pragma mark 11.14 drop_column_default_clause
drop_column_default_clause
	:	'drop' 'default'!
	;


//---11.18---------------------------------------------------------------------
// #pragma mark 11.18 add_column_definition
drop_table_statement
	:	'drop'^ 'table'! ( 'if'! 'exists' )? table_name ( drop_behavior )?
	;


//---11.19---------------------------------------------------------------------
// #pragma mark 11.19 view_definition
view_definition
	:	'create'! ( 'or'! 'replace' )? 'view'! view_name ( LPAREN! column_name_list RPAREN! )?
		'as'! query_expression ( 'with'! 'check'! 'option'! )?
		// ' ## = #(#[CREATE_VIEW, "CREATE_VIEW'
	;


//---11.20---------------------------------------------------------------------
// #pragma mark 11.20 <drop view statement>
drop_view_statement
	:	'drop'! 'view'! ( 'if'! 'exists' )? view_name_list
		(drop_behavior)?
		// ' ## = #(#[DROP_VIEW, "DROP_VIEW'
	;

//------------------------------------------------------------------------------
// #pragma mark create_index
create_index
	: 'create'! ('unique')? 'index'! index_name 'on'! table_name index_column_list
	// ' ## = #(#[CREATE_INDEX, "CREATE_INDEX'
	;

//------------------------------------------------------------------------------
// #pragma mark v_create_binary_link   valentina extension
v_create_binary_link
	:	'create'! ( 'or'! 'replace' )? 'binary'! 'link'! ('if'! 'not'! 'exists')?
			link_name 'on'! 'tables'! LPAREN! table_name COMMA! table_name RPAREN!
			'as'! link_type 'to'! link_type ( referential_triggered_action_for_link )?
			('owner'! table_name)?
		// ' ## = #(#[CREATE_BINARY_LINK, "CREATE_BINARY_LINK'
	;


link_type
	:	'one' | 'many' | UINT
	;

referential_triggered_action_for_link
	// in tree must be checked that update_rule | delete_rule only once time
	: 'on'! 'delete'! referential_action
	;

//------------------------------------------------------------------------------
// #pragma mark drop_index
drop_index
	: 'drop'! 'index'! index_name 'on'! table_name
	// ' ## = #(#[DROP_INDEX, "DROP_INDEX'
	;


//------------------------------------------------------------------------------
// #pragma mark v_drop_link   valentina extension
v_drop_link
	:	'drop'! 'link'! link_name
	// ' ## = #(#[DROP_LINK, "DROP_LINK'
	;




//---12.4 Access control ------------------------------------------------------

//---12.1 <grant statement>----------------------------------------------------
// #pragma mark 12.1 grant statement
privilege_def
	:	'grant' privileges 'to' grantee_list (with_option)?
	;

privileges
	:	object_privileges 'on' object_name
	;

object_name
	:	('table')? table_name
	;

object_privileges
	:	'all' 'privileges'
	|	action ( COMMA! action )*
	;

action
	:	'select'
	|	'select'		LPAREN! privilege_column_list RPAREN!
//	  |	"select"		LPAREN! privilege_method_list RPAREN!
	|	'delete'
	|	'insert'		(LPAREN! privilege_column_list RPAREN!)?
	|	'update'		(LPAREN! privilege_column_list RPAREN!)?
	|	'references'	(LPAREN! privilege_column_list RPAREN!)?
	|	'usage'
	|	'triger'
	|	'under'
	|	'execute'
	;

with_option
	:	'with' ('grant' | 'admin') 'option'
	;

//with_admin_option
//	  :	"with" "admin" "option"
//	  ;


//privilege_method_list
//	  :	specific_routine_designator (COMMA! specific_routine_designator)*
//	  ;

privilege_column_list
	:	column_name_list
	;

grantee_list
	:	grantee ( COMMA! grantee )*
	;

grantee
	:	'public'
	|	authorization_identifier
	;


//---privilege_revoke----------------------------------------------------
privilege_revoke
	:	revoke_privilege_statement
	|	revoke_role_statement
	;

revoke_privilege_statement
	:	'revoke' (revoke_option_extension)? privileges
			'from' grantee_list ('granted' 'by' grantor)?
			drop_behavior
	;

revoke_option_extension
	:	'grant' 'option' 'for'
//	  |	"hierarchy" "option" "for"
	;

revoke_role_statement
	:	'revoke' ('admin' 'option' 'for')? role_revoked_list
			'from' grantee_list ('granted' 'by' grantor)?
			drop_behavior
	;

role_revoked_list
	:	role_revoked (COMMA! role_revoked)*
	;

role_revoked
	:	role_name
	;

grantor
	:	'current_user'
	|	'current_role'
	;



//---11.39 <trigger definition>------------------------------------------------
// #pragma mark 11.39 <trigger definition>
trigger_definition
	:	'create'! ( 'or'! 'replace' )? 'trigger'!
		('if'! 'not'! 'exists')? trigger_name 
		trigger_action_time
		trigger_event_list
		'on'! table_or_view_name ( 'referencing' transition_table_list )?
		'for' 'each'! ( 'row' ( 'when'! search_condition )? | 'statement'! )
		triggered_SQL_statement
			// ' ## = #(#[CREATE_TRIGGER, "CREATE_TRIGGER'
	;

trigger_action_time
	:	'before'
	|	'after'
	|	'instead' 'of'!
	;

trigger_event_list
	:	trigger_event ( ('or'! | COMMA!) trigger_event )*
	;

trigger_event
	:	'insert'
	|	'delete'
	|	'update' ( 'of'! column_name_list )?
	;

triggered_SQL_statement
	:	compound_statement
	;


transition_table_list
	:	( transition_table )+
	;

transition_table
	:	'old' ( 'as'! )? old_row_name
	|	'new' ( 'as'! )? new_row_name
	;

old_row_name
	:	identifier
	;

new_row_name
	:	identifier
	;

table_or_view_name
	:	identifier
	;

//-------------------------------------------------------------------------------
// #pragma mark drop_trigger_statement
drop_trigger_statement
	:	'drop'! 'trigger'! ( 'if'! 'exists' )? trigger_name
		// ' ## = #(#[DROP_TRIGGER, "DROP_TRIGGER'
	;

//-------------------------------------------------------------------------------
// #pragma mark drop_trigger_statement
alter_trigger_statement
	:	'alter'! 'trigger'! trigger_name ( 'enable' | 'disable' )
		// ' ## = #(#[ALTER_TRIGGER, "ALTER_TRIGGER'
	;


//---11.50   <SQL-invoked routine> ------------------------------------------------
// #pragma mark 11.50 <procedure definition>
procedure_definition
	:	'create'! ( 'or'! 'replace' )? 'procedure'! proc_name proc_param_list
			compound_statement
		// ' ## = #(#[CREATE_PROCEDURE, "CREATE_PROCEDURE'
	;

function_definition
	:	'create'! ( 'or'! 'replace' )? 'function'! proc_name func_param_list
			'returns' data_type
			compound_statement
		// ' ## = #(#[CREATE_FUNC, "CREATE_FUNC'
	;

proc_param_list
	:	LPAREN! ( proc_parameter (COMMA! proc_parameter )* )? RPAREN!
	;

func_param_list
	:	LPAREN! ( func_parameter (COMMA! func_parameter )* )? RPAREN!
	;

proc_parameter 
	:	( 'in' | 'out' | 'inout' )? param_name data_type
	;

func_parameter 
	:	param_name data_type
	;

routine_statement
	:   sql_statement
	|	cursor_open
	|	cursor_cloase
	|	cursor_fetch
	;

assign_statement
	:	( 'set'! )? 
		(	( variable_name_list ( EQ! | EQP! ) 'select') => variable_name_list ( EQ! | EQP! ) query_expression	
		|	column_reference ( EQ! | EQP! ) ('null' |  expr) 
		)
	;
	
cursor_open
	:	'open'^ cursor_name
	;

cursor_cloase
	:	'close'^ cursor_name
	;

cursor_fetch
	:	'fetch'^ (fetch_direction)? ('from'!)? cursor_name 'into'! variable_name_list
	;

fetch_direction
	:	'next'
	|	'prior'
	|	'first'
	|	'last'
	|	'absolute' count
	|	'relative' count
	|	'forward'
	|	'backward'
	;

compound_statement
	:	'begin'^ 
			statement_list 
			(exception_statement)?
		'end'!
	;

statement_list
	:	( statement_in_cs )*
	;

statement_in_cs
	:   routine_statement SEMI!
	|   control_statement
	|   local_declaration SEMI!
	|   compound_statement
	|   raising_error SEMI!
	;

control_statement
	:	flow_control_statement
	|	return_statement SEMI!
	;

call_statement
	:	'call'^ proc_name LPAREN! ( expr ( COMMA! expr )* )? RPAREN!
	;

local_declaration
	:	('declare' cursor_name 'cursor') => cursor_def
	|	variable_declaration
	;

cursor_def
	:	'declare'! cursor_name 'cursor'! 'for'! direct_select_statement_multiple_rows
		// ' ## = #(#[DECLARE_CURSOR, "DECLARE_CURSOR'
	;

variable_declaration!
	:	'declare' vlist=variable_name_list dt=data_type ( dc=default_clause )?
//		// ' ## = #(#[DECLARE_VARIABLE, "DECLARE_VARIABLE'
	;

raising_error
	:	(  'raise'! | 'raiseerror'! ) 
			exception_error_code
			(COMMA! character_string_literal)?
		// ' ## = #(#[RAISE_ERROR, "RAISE_ERROR'
	;

exception_statement
	:	'exception'^ 
			( 'when' 
				exception_error_code (('or'! | COMMA!) exception_error_code)* 'then'! statement_list 
			)*
			( 'when'! 'others' 'then'! statement_list )?
	;

exception_error_code!
@init{
	STD::string strErrorCode;
}
	:	//( UINT | NUM_HEX) 
		// NUM_FLOAT - for 'e' in exponent 001e5
		(	(u=UINT  { strErrorCode = $u.text; } | f=NUM_FLOAT { strErrorCode = $f.text; })  
			(i=IDENT { strErrorCode = strErrorCode + $i.text; } )? 
			| i2=IDENT { strErrorCode = $i2.text; }
		) { ## = #[EXCEPTION_ERRORCODE, strErrorCode]; }
	;


flow_control_statement
	:	if_statement
	|	case_statement
	|	while_statement
	|	repeat_statement
	|	loop_statement
	|	iterate_statement
	|	leave_statement
	;

if_statement
	:	'if'^ search_condition 'then'! statement_list 
		 ( 'elseif' search_condition 'then'! statement_list )*
		 ( 'else' statement_list )?
		 'end'! 'if'!
	;

case_statement
	:	'case'! case_operand
		 ( 'when' when_operand 'then'! statement_list )+
		 ( 'else' statement_list )?
		 'end'! 'case'!
			// ' ## = #(#[CASE_SIMPLE, "CASE_SIMPLE'

	|	'case'!
		 ( 'when' search_condition 'then'! statement_list )+
		 ( 'else' statement_list )?
		 'end'! 'case'!
			// ' ## = #(#[CASE_SEARCHED, "CASE_SEARCHED'
	;

while_statement
	:	'while'^ search_condition 'do'! statement_list 'end'! 'while'!
	;

repeat_statement
	:	'repeat'^ statement_list 'until'! search_condition 'end'! 'repeat'!
	;


loop_statement
	:	'loop'^ statement_list 'end'! 'loop'!
	;


iterate_statement
	:	'iterate'^ (label_name)?
	;


leave_statement
	:	'leave'^ (label_name)?
	;
	
return_statement
	:   'return' (expr)?
	;	

//-------------------------------------------------------------------------------
drop_procedure_statement
	:	'drop'! ('procedure'! | 'function'!) ( 'if'! 'exists' )? proc_name
		// ' ## = #(#[DROP_PROCEDURE, "DROP_PROCEDURE'
	;
		
//---12.4 <role definition>----------------------------------------------------
// #pragma mark 12.4 role definition
create_user_statement
	:	'create'! 'role'! role_name
	// ' ## = #(#[ROLE_CREATE, "ROLE_CREATE'
	;

delete_user_statement
	:	'drop'! 'role'! role_name
	// ' ## = #(#[ROLE_DROP, "ROLE_DROP'
	;


//---12.5----------------------------------------------------------------------
// #pragma mark 12.5 sql_schema_statement
sql_schema_statement
	:	sql_schema_definition_statement
	|	sql_schema_manipulation_statement
	;

sql_schema_definition_statement
	:	('create'^ ( ( 'global' | 'local' )? 'temporary' )? ('ram' | 'system')? 
			'table'!)=>table_definition
	|	('create' ( 'or'! 'replace' )? 'trigger') => trigger_definition
	|	('create' ( 'or'! 'replace' )? 'procedure') => procedure_definition
	|	('create' ( 'or'! 'replace' )? 'function') => function_definition
	|	view_definition
	|	create_index
	|	v_create_binary_link
	|	create_user_statement
	|	privilege_def
	;

sql_schema_manipulation_statement
	:	alter_table_statement
	|	drop_table_statement
	|	drop_view_statement
	|	drop_trigger_statement
	|	drop_procedure_statement
	|	alter_trigger_statement
	|	drop_index
	|	v_drop_link
	|	delete_user_statement
	|	privilege_revoke
	;


sql_transaction_statement
	:	set_transaction_statement
	|	commit_statement
	|	rollback_statement
	;


//---13 Data manipulation------------------------------------------------------
//---13.1----------------------------------------------------------------------
// #pragma mark 13.1 Data manipulation
order_by_clause
	:	'order'^ 'by'! sort_specification_list
	;

sort_specification_list
	:	sort_specification ( COMMA! sort_specification )*
	;

sort_specification
	:	sort_key ( ordering_specification )?  (null_order)?
	;

sort_key
	:	column_reference
	|	UINT
	;

ordering_specification
	:	'asc' | 'desc'
	;

null_order
	:	'nulls' 
			( 'first' //-> ^(NULLS_ORDER_FIRST)
			| 'last' 
			) 
	;


//---13.7----------------------------------------------------------------------
// #pragma mark 13.7 delete_statement_searched
delete_statement_searched
	:	'delete'^ 'from'! table_name
			( 'where'! search_condition )?
	;


//---13.8----------------------------------------------------------------------
// #pragma mark 13.8 insert_statement
insert_statement
	:	'insert'! ('into'!)? 
			table_name insert_columns_and_source
			// ' ## = #(#[INSERT_INTO_TABLE, "INSERT_INTO_TABLE'
	;

insert_columns_and_source
	:	'default' 'values'!
		// TODO : optimase syntax predicat (maybe catch COMMA ??)
//	  |	( LPAREN! insert_column_list RPAREN! ) => 
	|	( LPAREN! column_name (COMMA | RPAREN ) ) => 
			LPAREN! insert_column_list RPAREN! query_expression
	|	query_expression
	;

insert_column_list
	:	column_name_list
	;


//-----------------------------------------------------------------------------
// #pragma mark link unlink
v_link_records
	:	'link'^ ('record'! | 'records'! )? v_link_list_of_records 'of'! table_name
			'with'! ('each')? ('record'! | 'records'! )? v_link_list_of_records 'of'! table_name
			( 'using' link_name )?
	;

v_unlink_records
	:	'unlink'^ ('record'! | 'records'! )? v_link_list_of_records 'of'! table_name
			'from'! ('each')? ('record'! | 'records'! )? v_link_list_of_records 'of'! table_name
			( 'using' link_name )?
	;

v_link_value
	 :	UINT
	 |	dynamic_parameter_specification
	;

v_link_list_of_records
	 :	  LPAREN! v_link_value ( COMMA! v_link_value )* RPAREN!
	 |	  subquery
	;


//v_link_list_of_records
//	  :	LPAREN! UINT ( COMMA! UINT )* RPAREN!
//	  |	subquery
//	  ;


//---13.9----------------------------------------------------------------------
// #pragma mark 13.9 set_clause_list
set_clause_list
	:	set_clause ( COMMA! set_clause )*
	// ' ## = #(#[SET_CLAUSE_LIST,"SET_CLAUSE_LIST'
	;

set_clause
	:	object_column EQ^ update_source
	;

update_source
	:	expr
	|	'null'
	|	'default'
	;

object_column
	:	column_name
	;


//---13.10---------------------------------------------------------------------
// #pragma mark 13.10 update_statement_searched
update_statement_searched
	:	'update'^ table_name 
			'set'! set_clause_list 
			( 'where'! search_condition )?
	;


//---14 Transaction management-------------------------------------------------
//---14.1----------------------------------------------------------------------
// #pragma mark 14.1 set_transaction_statement
set_transaction_statement
	:	'set'^ 'transaction'! transaction_mode ( COMMA! transaction_mode )*
	;

transaction_mode
	:	isolation_level
	|	transaction_access_mode
	;

transaction_access_mode
	:	'read'! ( 'only' | 'write' )
	;

isolation_level
	:	'isolation'! 'level'! level_of_isolation
	;

level_of_isolation
	:	'read'! ( 'uncommitted' | 'committed' )
	|	'repeatable' 'read'!
	|	'serializable'
	;


//---14.3----------------------------------------------------------------------
// #pragma mark 14.3 commit_statement
commit_statement
	:	'commit' ( 'work'! )?
	;


//---14.4----------------------------------------------------------------------
// #pragma mark 14.4 rollback_statement
rollback_statement
	:	'rollback' ( 'work'! )?
	;

//---14.?----------------------------------------------------------------------
xml_value_expression
	:	value_expression_primary
	|	xml_value_function
	;

// #pragma mark 14.? SQL/XML standard
xml_value_function
	:	xml_element
	|	xml_forest
	;


// #pragma mark xml_element
xml_element
	:	'xmlelement'^ 
		LPAREN! 
			// "name"! identifier  !hack! we don't want to use "name" as resolved token
			i=IDENT! 
				// if( i->getText() != "name' 
			identifier
//			  ( COMMA! xml_namespace_declaration )?
			( COMMA! xml_attributes )?
				( COMMA! xml_element_content )*
//				  ( "option" xml_content_option )? 
//			  ( xml_returning_clause )?
		RPAREN!
	;

// #pragma mark xml_attributes
xml_attributes
	:	'xmlattributes'^ LPAREN! xml_attributes_list RPAREN!
	;

xml_attributes_list
	:	xml_attribute (COMMA! xml_attribute)*
	;

xml_attribute
	:	xml_attribute_value ('as'! identifier)?
	;

xml_attribute_value
	:	primary
	;

xml_element_content
	:	primary
	;

//xml_content_option
//	  :	"null" "on" "null"
//	  |	"empty" "on" "null"
//	  |	"absent" "on" "null"
//	  |	"nil" "on" ( "null" | "no" "content")
//	  ;


// #pragma mark xml_forest
xml_forest
	:	'xmlforest'^ 
		LPAREN!
//			  ( xml_namespace_declaration COMMA! )?
			forest_element_list 
//			  ( "option" xml_content_option )?
//			  ( xml_returning_clause )?
		RPAREN!
	;

forest_element_list
	:	forest_element (COMMA! forest_element)*
	;

forest_element
	:	forest_element_value ('as' identifier)?
	;

forest_element_value
	:	primary
	;


// #pragma mark xml_aggregate
xml_aggregate
	:	'xmlagg' 
		LPAREN!
			xml_value_expression
			( 'order' 'by' sort_specification_list )?
//			  ( xml_namespace_declaration COMMA! )?
//			  ( xml_returning_clause )?
		RPAREN!
	;



//---20.2----------------------------------------------------------------------
// #pragma mark 20.2 direct_select_statement_multiple_rows
direct_select_statement_multiple_rows
	:	query_expression ( order_by_clause )? (select_limit)?
	;


//---21 Information Schema and Definition Schema-------------------------------
//-----------------------------------------------------------------------------
// #pragma mark 21 set_function_type
set_quantifier
	:	'distinct' | 'all'
	;

set_function_type
	:	'avg' | 'max' | 'min' | 'sum' | 'count'
	;


//--- MS SQL 2005 Extensions
//-----------------------------------------------------------------------------
// #pragma mark 
with_common_table_expression
	:	'with'^ common_table_expression
	;

common_table_expression
	:	expression_name LPAREN! column_name_list RPAREN!
		'as'! LPAREN! cte_query_definition RPAREN!
		direct_select_statement_multiple_rows
	;

cte_query_definition
	:	query_expression
	;

//--- Valentina Extensions ----------------------------------------------------
//-----------------------------------------------------------------------------
// #pragma mark valentina_extensions
//
valentina_extensions
	:	diagnose
	|	repair
	|	reindex
	|	set_property
	|	get_property
//	|	show_statement
	|	compact
	|	defragment
	|	convertRDBLink
	|	cloneDB
	|	backupDB
	|	useDB
	|	print_expr
	|	valentina_extensions_events
	|	valentina_extensions_database
	|   vext_copy_links
	;

diagnose
	:	'diagnose'^
		(	'database'! (verbose_mode)? (path)?
		|	'table' table_name_list (verbose_mode)? (path)?
		|	'field' table_dot_column_list (verbose_mode)? (path)?
		)
	;

verbose_mode
	:	('verbose'!)?
		(	'none'
		|	'low'
		|	'normal'
		|	'high'
		|	'veryhigh'
		)
	;

path
	:	STRING_LITERAL
	;

repair
	:	'repair'^
		(	'database'!
		|	'table' table_name_list
		|	'field' table_dot_column_list
		)
	;

reindex
	:	'reindex'^
		(	'database'!
		|	'table' table_name_list
		|	'field' table_dot_column_list
		)
	;

//-----------------------------------------------------------------------------
set_property
	:	'set'! ('property'!)? props_name_list ('of'! props_of_list)? 'to'! values_list
		// ' ## = #([SET_PROPERTY,"SET_PROPERTY'
	;

get_property
	:	'get'^ 'property'! props_name_list ( 'of'! props_of_list )?
	;

values_list
	:	value_elem ( COMMA! value_elem )*
	;

value_elem
	:	character_string_literal
	|	signed_numeric_literal
	|	'true'	  '  ## = #[CONST_UINT, "1'
	|	'false'	 '  ## = #[CONST_UINT, "0'
	|	'null'
	;

props_name_list
	:	property_name ( COMMA! property_name )*
		// ' ## = #([PROPERTY_NAME_LIST,"PROPERTY_NAME_LIST'
	;

props_of_list
	:	'field'	 column_reference
	|	'table'	 identifier
	|	'link'	  identifier
	|	'database'	  (identifier)?
	|	'server'
	|	'connection'
	;

//-----------------------------------------------------------------------------
compact
	:	'compact'^
	;

defragment
	:	'defragment'^
	;


// convert RDBLINK tableB.fld_ptr = tableA.fld_key into [tableB.]fld_OPtr
convertRDBLink
	:	'convert'! 'rdblink'^ table_dot_column EQ! table_dot_column 
		'into'! column_reference
	;

vext_copy_links
	:   'copy'! 'links'^ 'from'! link_name ('and'! link_name)? 'to' link_name ('and'! link_name)?
	;

cloneDB
	:	'clone'^ 'database'! ('data'! | 'structure')? 'to'! path
	;


backupDB
	:	'backup'^ ('database'!)? ('to'! path )? ('with'! 'diagnose')?
	;


useDB
	:	'use'^ ('database'!)? db_name
	;

//-----------------------------------------------------------------------------
show_statement
	:	'show'!
		(	'databases'^ 
		|	'tables'^ ( ('from'! |'of'!) db_name)? 

		|	('columns'! | 'fields'!) ('from'! |'of'!) table_name (('from'! |'of'!) db_name)? 
				// ' ## = #(#[SHOW_COLUMNS, "SHOW_COLUMNS'

		|	'events'! ('from'! |'of'!) db_name
				// ' ## = #([EVENT_SHOW,"EVENT_SHOW'

		|	'vidents'^ ( ('from'! |'of'!) db_name)? 

//		  |	"indexes"^ ("from"! identifier)? (("from"! |"of"!) identifier)?
		|	'index'! 'styles'^ (('from'! |'of'!) db_name)?

		|	'links'! (('from'! |'of'!) identifier)? (('from'! |'of'!) identifier)?
				// ' ## = #(#[SHOW_LINKS, "SHOW_LINKS'

		|	'vkeywords'^

		|	'procedures'^ (('from'! |'of'!) db_name)?

		|	'properties'^ ('of'! props_of_list)?

		|	'triggers'^ (('from'! |'of'!) identifier)? (('from'! |'of'!) identifier)?

		|	('status'! | 'statistic'!) 
				(
					('for'! | 'of'!) 
					(	'server'
					|	'database' (IDENT)?
					|	( 'index' | 'link' | 'table' ) IDENT		// obj_type obj_name
					|	( 'field' | 'column' ) table_dot_column
					)
				)? 
				// ' ## = #(#[SHOW_STATUS, "SHOW_STATUS'

		|	'users'^
		)
	;

//-----------------------------------------------------------------------------
select_limit
	:	'limit'^ UINT 
			(	COMMA! UINT		 // MySQL:   [LIMIT [offset,] rows]
			|	'offset' UINT   // Postgre: [LIMIT rows [OFFSET offset]
			|
			)
	;


//-----------------------------------------------------------------------------
valentina_extensions_database
	:	create_database
	|	drop_database
	;

create_database
	:	'create'! 'database'! ('if'! 'not'! 'exists')? db_name 
	;

drop_database
	:	'drop'! 'database'! ( 'if'! 'exists' )? db_name
	;


//-----------------------------------------------------------------------------
valentina_extensions_events
	:	event_definition_statement
	|	drop_event_statement
	|	alter_event_statement
	;

event_definition_statement
	:	'create'! 'event'! ('if'! 'not'! 'exists')? event_name 
		'for'! ('database'!)? db_name
		'on'! 'schedule'! schedule_definition
		('on'! 'completion' ( 'not' )? 'preserve'!)?
		( 'enabled' | 'disabled' )?
		( 'comment' character_string_literal)?
		'do'! sql_statement
	;


drop_event_statement
	:	'drop'! 'event'! ( 'if'! 'exists' )? event_name
	;


alter_event_statement
	:	'alter'! 'event'! event_name
		( 'on'! 'schedule'! schedule_definition )?
		( 'rename' 'to'! event_name )?
		( 'on'! 'completion' ( 'not' )? 'preserve'! )?
		( 'comment' character_string_literal )?
		( 'enabled' | 'disabled' )?
		( 'do'! sql_statement )?
	;

print_expr
	:	'print'^ expr
	;

schedule_definition
	:	'at'! timestamp (PLUS 'interval'! interval_value)?
	|	'every' interval_value
		(	'starts' timestamp
			(	'ends' timestamp
			|
			)
		|	'ends' timestamp
		|
		)
	;

interval_value
	:	integer_value time_keyword
	;

time_keyword
	:	// "year" | "month" | "week" | "day" | "hour" | "minute" | "second"
		// !hack! we don't want to use ... as resolved token
		identifier		  // see treeParser

	;

timestamp
	:	timestamp_literal | current_timestamp_value_function
	;


/****************************************************************************/
// This Lexer is used for Parser of Expression. 
// Expression is single line -- which use function calls, operators and fields. 
// We use Expr_Parser to parse calculation fields -- BaseObject Methods.
//


//------------------------------------------------------------------------------
// OPERATORS 
//------------------------------------------------------------------------------

	// String literals
DQUOTE	: '"'   ;
QUOTE	: '\''   ;
BQUOTE	: '`'   ;

	// Group operators
LPAREN	: '('   ;
RPAREN	: ')'   ;
LBRACK	: '['   ;
RBRACK	: ']'   ;
LCURLY	: '{'   ;
RCURLY	: '}'   ;

	// Punctuations:
//DOT	: '.'   ;
PTR		: '->'  ;		// for fld_ptr->fld.
COMMA	: ','   ;
COLON	: ':'   ;
SEMI	: ';'   ;

	// Ariphmetic:
PLUS	: '+'   ;
MINUS	: '-'   ;
DIV		: '/'   ;
STAR	: '*'   ;
        
SHR		: '>>'  ;
SHL		: '<<'  ;

PERCENT	: '%'   ;
//USCORE : '_'   ;

	// Comparision:
EQ		: '='   ;
EQP		: ':='  ;
NE		: '<>'  ;
NEC		: '!='  ;
GT		: '>'   ;
GE		: '>='  ;
LT_		: '<'   ;
LE		: '<='  ;
OR		: '=*'  ;
OL		: '*='  ;
OF		: '*=*' ;

	// Other
QUESTION	: '?'   ;
//AT		: '@'   ;

	// String concatenation
CONCAT	  	: '||'  ;

	// SQL extension for Valentina.
DSTAR	: '**'  ;


//------------------------------------------------------------------------------
// Whitespace -- ignored
WS
	:	(	' '
	|		'\t'
	|		'\f'

			// handle newlines
	|		(		'\r\n'	  // Evil DOS
				|	'\r'		// Macintosh
				|	'\n'		// Unix (the right way)
			)
			{ newline(); }
		)
		{$channel=HIDDEN;}
	;
	
//------------------------------------------------------------------------------
/* Single line comments */

SL_COMMENT 
	:	// allow STANDARD: "--" or C++ style "//"
    	( '//' | '--' ) ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
	;
	
// multiple-line comments
ML_COMMENT
	:	'/' '*' ( options {greedy=false;} : . )* '*' '/' {$channel=HIDDEN;}
	;


//------------------------------------------------------------------------------
// a numeric literal

// a couple of protected methods to assist in matching floating point numbers
fragment
DIGIT   : ('0'..'9');


//------------------------------------------------------------------------------
// String literals:

// caseSensitive = false, so we use only small chars.
fragment
LETTER
	:	'a'..'z'
	|   '@'
	;


STRING_LITERAL
	:	QUOTE! ( ESCAPE_SEQUENCE | ~('\'' | '\\') | (QUOTE QUOTE!) )* QUOTE!
	;

fragment
ESCAPE_SEQUENCE
	:	'\\' ( QUOTE | '_' | '%' ) 
	;

//------------------------------------------------------------------------------
// an identifier. Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
	:	( LETTER | '_' ) ( LETTER | '_' | DIGIT )*
	;

DELIMITED		// delimited_identifier
	:
	(	DQUOTE! ( ~(DQUOTE) | DQUOTE DQUOTE! )+ DQUOTE!
	|	BQUOTE! ( ~(BQUOTE) | BQUOTE BQUOTE! )+ BQUOTE!
			
	|	LBRACK! ( ~(']') )+ RBRACK!	 // valentina extension   [asasas '' " sd "]	
	)	
		{ _ttype = IDENT; }
	;


//------------------------------------------------------------------------------
//  a numeric literal
UINT
	:	'.'{ _ttype = DOT; }	(('0'..'9')+ (EXPONENT)? { _ttype = NUM_FLOAT; })?
	
	|	('0'..'9')+ 	
		(   '.' ('0'..'9')* (EXPONENT)? { _ttype = NUM_FLOAT; }
		|	EXPONENT { _ttype = NUM_FLOAT; }
		)?
	;

// a couple protected methods to assist in matching floating point numbers
fragment
EXPONENT
	:	'e' ('+'|'-')? ('0'..'9')+
	;

fragment
DOT	 :;

fragment
NUM_FLOAT :;

fragment
NUM_HEX :;

