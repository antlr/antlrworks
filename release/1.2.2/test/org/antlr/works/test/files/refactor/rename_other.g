grammar references;

options
{
	tokenVocab=DataViewExpressions;
	output=AST;
	ASTLabelType=CommonTree;
}

tokens {
	FOO='a';
	OTHER_2;
	LAST='b';
}

rule_a	:	FOO BAR OTHER_2
	;

BAR 	:
	;
