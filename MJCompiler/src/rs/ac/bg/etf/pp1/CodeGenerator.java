package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;

public class CodeGenerator extends VisitorAdaptor {

private int mainPc;
	
	public int getMainPc(){
		return mainPc;
	}
	
	public void visit(PrintStmt printStmt){
		if(printStmt.getExpr().struct == Tab.intType){
			Code.loadConst(5);
			Code.put(Code.print);
		}else{
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
	}
	
	public void visit(NumFactor cnst){
		Obj con = new Obj(Obj.Con, "$", cnst.struct, cnst.getN1() /*value for constants*/, 0);
		Code.load(con);
	}
	
	public void visit(CharFactor cnst) {
		Obj con = new Obj(Obj.Con, "$", cnst.struct, cnst.getC1() /*value for constants*/, 0);
		Code.load(con);
	}
	
	// TODO: add one for bool const
	
	public void visit(MethodTypeName methodTypeName){
		
		if("main".equalsIgnoreCase(methodTypeName.getMethName())){
			mainPc = Code.pc;
		}
		methodTypeName.obj.setAdr(Code.pc);
		// Collect arguments and local variables
		SyntaxNode methodNode = methodTypeName.getParent();
	
		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);
		
		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);
		
		// Generate the entry
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(fpCnt.getCount() + varCnt.getCount());
	
	}
	
	public void visit(MethodDecl methodDecl){
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(Assignment assignment){
		Code.store(assignment.getDesignator().obj);
	}
	
	public void visit(Designator designator){
		SyntaxNode parent = designator.getParent();
		
		if(Assignment.class != parent.getClass() && FuncCallFactor.class != parent.getClass() && ProcCall.class != parent.getClass()){
			Code.load(designator.obj);
		}
	}
	
	public void visit(FuncCallFactor funcCall){
		Obj functionObj = funcCall.getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);
		
		Code.put2(offset);
	}
	
	public void visit(ProcCall procCall){
		Obj functionObj = procCall.getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		if(procCall.getDesignator().obj.getType() != Tab.noType){
			Code.put(Code.pop);
		}
	}
	
	public void visit(ReturnExpr returnExpr){
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(ReturnNoExpr returnNoExpr){
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(AddopPlus addop){
		Code.put(Code.add);
	}
	
	public void visit(AddopMinus addop) {
		Code.put(Code.sub);
	}
	
	public void visit(MulopMul mulop) {
		Code.put(Code.mul);
	}
	
	public void visit(MulopDiv mulop) {
		Code.put(Code.div);
	}
	
	public void visit(MulopMod mulop) {
		Code.put(Code.rem);
	}
}
