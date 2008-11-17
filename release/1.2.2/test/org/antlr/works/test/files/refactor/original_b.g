grammar test;

n_expression
 : (n_conditionalOrExpr -> n_conditionalOrExpr) (
    '=' r=n_conditionalOrExpr -> ^(N_ASSIGN_EXPR $n_expression $r)
   )?
 ;

n_conditionalOrExpr
	:
	;

N_ASSIGN_EXPR
	:
	;

