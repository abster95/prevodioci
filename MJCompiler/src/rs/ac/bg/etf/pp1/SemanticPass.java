package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticPass extends VisitorAdaptor {

	int printCallCount = 0;
	int varDeclCount = 0;
	Obj currentMethod = null;
	Obj currentEnum = null;
	int lastEnumValue = 0;
	public static final int EnumObj = 7; // missing in the Obj.class specification
	boolean returnFound = false;
	boolean errorDetected = false;
	int nVars;

	Logger log = Logger.getLogger(getClass());

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" at line ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" at line ").append(line);
		log.info(msg.toString());
	}

	public void visit(VarDecl varDecl) {
		Obj existing = Tab.find(varDecl.getVarName());
		if (existing != Tab.noObj) {
			report_error("Variable with name '" + varDecl.getVarName() + "' already declared in current scope!",
					varDecl);
			return;
		}
		varDeclCount++;
		report_info("Variable declaration: " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), varDecl.getType().struct);
	}

	public void visit(PrintStmt print) {
		printCallCount++;
	}

	public void visit(ProgName progName) {
		progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
		Tab.openScope();
	}

	public void visit(Program program) {
		nVars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}

	public void visit(Type type) {
		Obj typeNode = Tab.find(type.getTypeName());
		if (typeNode == Tab.noObj) {
			report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola! ", type);
			type.struct = Tab.noType;
		} else {
			if (Obj.Type == typeNode.getKind()) {
				type.struct = typeNode.getType();
			} else {
				report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip!", type);
				type.struct = Tab.noType;
			}
		}
	}

	public void visit(MethodTypeName methodTypeName) {
		currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMethName(), methodTypeName.getType().struct);
		methodTypeName.obj = currentMethod;
		Tab.openScope();
		report_info("Obradjuje se funkcija " + methodTypeName.getMethName(), methodTypeName);
	}

	public void visit(MethodDecl methodDecl) {
		if (!returnFound && currentMethod.getType() != Tab.noType) {
			report_error("Semanticka greska na liniji " + methodDecl.getLine() + ": funkcija " + currentMethod.getName()
					+ " nema return iskaz!", null);
		}
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();

		returnFound = false;
		currentMethod = null;
	}

	public void visit(NamedDesignator designator) {
		Obj obj = Tab.find(designator.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName()
					+ " nije deklarisano! ", null);
		}
		designator.obj = obj;
	}

	public void visit(ReturnExpr returnExpr) {
		returnFound = true;
		Struct currMethType = currentMethod.getType();
		if (!currMethType.compatibleWith(returnExpr.getExpr().struct)) {
			report_error("Greska na liniji " + returnExpr.getLine() + " : "
					+ "tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije "
					+ currentMethod.getName(), null);
		}
	}
	
	public void visit(NumFactor numFactor) {
		numFactor.struct = Tab.intType;
	}
	
	public void visit(CharFactor charFactor) {
		charFactor.struct = Tab.charType;
	}
	
	public void visit(DesignatorFactor designatorFactor) {
		//Obj designator = Tab.find(designatorFactor.getDesignator())
	}
	
	public void visit(EnumDecl enumDecl) {
		report_info("Enum declarations: " + currentEnum.getType().getMembersTable().toString(), enumDecl);
		enumDecl.obj = currentEnum;
		currentEnum = null;
		lastEnumValue=0;
	}
	
	public void visit(EnumName enumName) {
		currentEnum = Tab.insert(EnumObj, enumName.getEnumName(), new Struct(Struct.Enum));
		lastEnumValue=0;
	}
	
	public void visit(EnumNoDefault enumNoDefault) {
		if(null == currentEnum) {
			report_error("Found an enum outside of enum decl..", enumNoDefault);
		}
		enumNoDefault.obj = Tab.insert(Obj.Con, enumNoDefault.getId(), Tab.intType);
		enumNoDefault.obj.setAdr(lastEnumValue++);
		boolean success = currentEnum.getType().getMembersTable().insertKey(enumNoDefault.obj);
		if(!success) {
			report_error("Name conflict: there's an enumeration with the same name", enumNoDefault);
		}
	}
	
	public void visit(EnumDefault enumDefault) {
		if(null == currentEnum) {
			report_error("Found an enum outside of enum decl..", enumDefault);
		}
		enumDefault.obj = Tab.insert(Obj.Con, enumDefault.getId(), Tab.intType);
		enumDefault.obj.setAdr(enumDefault.getVal());
		lastEnumValue = enumDefault.getVal();
		boolean success = currentEnum.getType().getMembersTable().insertKey(enumDefault.obj);
		if(!success) {
			report_error("Name conflict: there's an enumeration with the same name", enumDefault);
		}
	}

	public boolean passed() {
		return !errorDetected;
	}

}
