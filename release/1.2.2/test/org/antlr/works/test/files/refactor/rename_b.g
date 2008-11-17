grammar test;

foo
 : (n_conditionalOrExpr -> n_conditionalOrExpr) (
    '=' r=n_conditionalOrExpr -> ^(N_ASSIGN_EXPR $foo $r)
   )?
 ;

n_conditionalOrExpr
	:
	;

N_ASSIGN_EXPR
	:
	;

