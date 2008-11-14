grammar references;

options
{
	tokenVocab=DataViewExpressions;
	output=AST;
	ASTLabelType=CommonTree;
}

tokens {
	FOO='a';
	OTHER;
	LAST='b';
}

rule_a	:	FOO BAR OTHER
	;

BAR 	:
	;
