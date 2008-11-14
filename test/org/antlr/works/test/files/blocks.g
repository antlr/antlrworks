grammar demo;

tokens {
 FOO="a";
 OTHER;
 LAST; }

options
{
	tokenVocab=DataViewExpressions;
	output=AST;
	ASTLabelType=CommonTree;
}
