package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;

public class CodeGenerator extends VisitorAdaptor {

	private int mainPc;

	public int getMainPc() {
		return mainPc;
	}

	public void visit(PrintStmt printStmt) {
		if (printStmt.getExpr().struct == Tab.intType) {
			Code.loadConst(5);
			Code.put(Code.print);
		} else {
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
	}

	public void visit(NumFactor cnst) {
		Obj con = new Obj(Obj.Con, "$", cnst.struct, cnst.getN1() /* value for constants */, 0);
		Code.load(con);
	}

	public void visit(CharFactor cnst) {
		Obj con = new Obj(Obj.Con, "$", cnst.struct, cnst.getC1() /* value for constants */, 0);
		Code.load(con);
	}

	// TODO: add one for bool const

	public void visit(MethodTypeName methodTypeName) {

		if ("main".equalsIgnoreCase(methodTypeName.getMethName())) {
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

	public void visit(MethodDecl methodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	public void visit(Assignment assignment) {
		Code.store(assignment.getDesignator().obj);
	}

//	public void visit(Designator designator) {
//		SyntaxNode parent = designator.getParent();
//
//		if (Assignment.class != parent.getClass() && FuncCallFactor.class != parent.getClass()
//				&& ProcCall.class != parent.getClass()) {
//			Code.load(designator.obj);
//		}
//	}
	
	public void visit(DesignatorFactor designator) {
		Code.load(designator.getDesignator().obj);
	}

	public void visit(FuncCallFactor funcCall) {
		Obj functionObj = funcCall.getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);

		Code.put2(offset);
	}

	public void visit(ProcCall procCall) {
		Obj functionObj = procCall.getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		if (procCall.getDesignator().obj.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
	}

	public void visit(ReturnExpr returnExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	public void visit(ReturnNoExpr returnNoExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	public void visit(AddopExpr expr) {
		Addop addop = expr.getAddop();
		if (addop.getClass() == AddopPlus.class) {
			Code.put(Code.add);
		} else if (addop.getClass() == AddopMinus.class) {
			Code.put(Code.sub);
		}
	}

	public void visit(MulopExpr expr) {
		Mulop mulop = expr.getMulop();
		if (mulop.getClass() == MulopMul.class) {
			Code.put(Code.mul);
		} else if (mulop.getClass() == MulopDiv.class) {
			Code.put(Code.div);
		} else if (mulop.getClass() == MulopMod.class) {
			Code.put(Code.rem);
		}

	}
	
	public void visit(NegatedFactor factor) {
		Code.put(Code.neg);
	}
	
	public void visit(Increment inc) {
		Code.load(inc.getDesignator().obj);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(inc.getDesignator().obj);
	}
	
	public void visit(Decrement dec) {
		Code.load(dec.getDesignator().obj);
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(dec.getDesignator().obj);
	}

}
