// generated with ast extension for cup
// version 0.8
// 7/0/2019 21:1:24


package rs.ac.bg.etf.pp1.ast;

public interface Visitor { 

    public void visit(Designator Designator);
    public void visit(Factor Factor);
    public void visit(Mulop Mulop);
    public void visit(DesignatorStatement DesignatorStatement);
    public void visit(ActualParamList ActualParamList);
    public void visit(FactorList FactorList);
    public void visit(Expr Expr);
    public void visit(FormalParamList FormalParamList);
    public void visit(FormPars FormPars);
    public void visit(VarDeclList VarDeclList);
    public void visit(EnumDeclList EnumDeclList);
    public void visit(Access Access);
    public void visit(Addop Addop);
    public void visit(FormalParamDecl FormalParamDecl);
    public void visit(MethodDeclList MethodDeclList);
    public void visit(Statement Statement);
    public void visit(TermList TermList);
    public void visit(Enumerations Enumerations);
    public void visit(StatementList StatementList);
    public void visit(Enumeration Enumeration);
    public void visit(ActualPars ActualPars);
    public void visit(MulopMod MulopMod);
    public void visit(MulopDiv MulopDiv);
    public void visit(MulopMul MulopMul);
    public void visit(AddopMinus AddopMinus);
    public void visit(AddopPlus AddopPlus);
    public void visit(AccessArray AccessArray);
    public void visit(AccessField AccessField);
    public void visit(AccessDesignator AccessDesignator);
    public void visit(NamedDesignator NamedDesignator);
    public void visit(ActualParam ActualParam);
    public void visit(ActualParams ActualParams);
    public void visit(NoActuals NoActuals);
    public void visit(Actuals Actuals);
    public void visit(CharFactor CharFactor);
    public void visit(NumFactor NumFactor);
    public void visit(ParenthesisExpr ParenthesisExpr);
    public void visit(OperatorNewArray OperatorNewArray);
    public void visit(OperatorNew OperatorNew);
    public void visit(FuncCallFactor FuncCallFactor);
    public void visit(DesignatorFactor DesignatorFactor);
    public void visit(NegatedFactor NegatedFactor);
    public void visit(SingleFactor SingleFactor);
    public void visit(MulopExpr MulopExpr);
    public void visit(Term Term);
    public void visit(SingleTermExpr SingleTermExpr);
    public void visit(AddopExpr AddopExpr);
    public void visit(ArithmeticExpr ArithmeticExpr);
    public void visit(Decrement Decrement);
    public void visit(Increment Increment);
    public void visit(Assignment Assignment);
    public void visit(ProcCall ProcCall);
    public void visit(ReturnNoExpr ReturnNoExpr);
    public void visit(ReturnExpr ReturnExpr);
    public void visit(PrintStmt PrintStmt);
    public void visit(ErrorStmt ErrorStmt);
    public void visit(DesignatorStmt DesignatorStmt);
    public void visit(NoStmt NoStmt);
    public void visit(Statements Statements);
    public void visit(EnumDefault EnumDefault);
    public void visit(EnumNoDefault EnumNoDefault);
    public void visit(SingleEnum SingleEnum);
    public void visit(MultipleEnums MultipleEnums);
    public void visit(EnumDecl EnumDecl);
    public void visit(NoEnumDeclaration NoEnumDeclaration);
    public void visit(EnumDeclarations EnumDeclarations);
    public void visit(SingleFormalParamDecl SingleFormalParamDecl);
    public void visit(FormalParamDecls FormalParamDecls);
    public void visit(NoFormParam NoFormParam);
    public void visit(FormParams FormParams);
    public void visit(MethodTypeName MethodTypeName);
    public void visit(MethodDecl MethodDecl);
    public void visit(NoMethodDecl NoMethodDecl);
    public void visit(MethodDeclarations MethodDeclarations);
    public void visit(Type Type);
    public void visit(VarDecl VarDecl);
    public void visit(NoVarDecl NoVarDecl);
    public void visit(VarDeclarations VarDeclarations);
    public void visit(ProgName ProgName);
    public void visit(Program Program);

}
