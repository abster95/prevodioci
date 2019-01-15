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

	// VARIABLES

	public void visit(VarSimpleDecl varDecl) {
		Obj existing = Tab.currentScope.findSymbol(varDecl.getVarName());
		if (null != existing && existing != Tab.noObj) {
			report_error("ERROR: Variable with name '" + varDecl.getVarName() + "' already declared in current scope!",
					varDecl);
			return;
		}
		if (Tab.noType == varDecl.getType().struct) {
			report_error("ERROR: It seems like you're trying to declare a variable of type 'void' for variable '"
					+ varDecl.getVarName() + "'. Allowed  types for variables are 'int' and 'char'.", varDecl);
			return;
		}
		varDeclCount++;
		report_info("Variable declaration: " + varDecl.getVarName(), varDecl);
		if(varDecl.getType().struct.getKind() == Struct.Enum) {
			Tab.insert(Obj.Var, varDecl.getVarName(), Tab.intType);
		}
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), varDecl.getType().struct);
	}

	public void visit(VarArrayDecl varDecl) {
		Obj existing = Tab.currentScope.findSymbol(varDecl.getVarName());
		if (null != existing && existing != Tab.noObj) {
			report_error("ERROR: Variable with name '" + varDecl.getVarName() + "' already declared in current scope!",
					varDecl);
			return;
		}
		if (Tab.noType == varDecl.getType().struct) {
			report_error("ERROR: It seems like you're trying to declare an array of type 'void' for variable '"
					+ varDecl.getVarName() + "'. Allowed  types for arrays are 'int' and 'char'.", varDecl);
			return;
		}
		varDeclCount++;
		report_info("Variable declaration: " + varDecl.getVarName(), varDecl);
		Tab.insert(Obj.Var, varDecl.getVarName(), new Struct(Struct.Array, varDecl.getType().struct));
	}

	public void visit(PrintStmt print) {
		printCallCount++;
		if(print.getExpr().struct != Tab.intType && print.getExpr().struct != Tab.charType) {
			report_error("Can only print chars and ints", print);
		}
	}
	
	public void visit(PrintWidthStmt print) {
		printCallCount++;
		if(print.getExpr().struct != Tab.intType && print.getExpr().struct != Tab.charType) {
			report_error("Can only print chars and ints", print);
		}
	}
	
	public void visit(ReadStmt read) {
		if(read.getDesignator().obj.getKind() != Obj.Var && read.getDesignator().obj.getKind() != Obj.Elem) {
			report_error("Can only read into var or elem", read);
		}
		if(read.getDesignator().obj.getType() != Tab.intType && read.getDesignator().obj.getType() != Tab.charType) {
			report_error("Can only read ints or chars", read);
		}
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

	// TYPES

	public void visit(ExistingType type) {
		Obj typeNode = Tab.find(type.getTypeName());
		if (typeNode == Tab.noObj) {
			report_error("ERROR: Type " + type.getTypeName() + " not present in symbol table ", type);
			type.struct = Tab.noType;
		} else {
			if (Obj.Type == typeNode.getKind()) {
				type.struct = typeNode.getType();
			} else {
				report_error("ERROR: Name " + type.getTypeName() + " is not a type!", type);
				type.struct = Tab.noType;
			}
		}
	}

	public void visit(VoidType type) {
		type.struct = Tab.noType;
	}

	// METHODS

	public void visit(MethodTypeName methodTypeName) {
		currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMethName(), methodTypeName.getType().struct);
		methodTypeName.obj = currentMethod;
		Tab.openScope();
		report_info("INFO: Processing function " + methodTypeName.getMethName(), methodTypeName);
	}

	public void visit(MethodDecl methodDecl) {
		if (!returnFound && currentMethod.getType() != Tab.noType) {
			report_error(
					"ERROR: Semantic error : function " + currentMethod.getName() + " doesn't have return statement!",
					methodDecl);
		}
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();

		returnFound = false;
		currentMethod = null;
	}

	// DESIGNATORS
	
	public void visit(NamedDesignator designator) {
		Obj obj = Tab.find(designator.getName());
		if (obj == Tab.noObj) {
			report_error("ERROR: Undeclared name " + designator.getName(), designator);
		}
		designator.obj = obj;
	}
	
	public void visit(AccessArray designator) {
		Obj arrayObj = Tab.find(designator.getArrayName().getI1());
		if (Tab.noObj == arrayObj) {
			report_error("Trying to use undeclared variable '" + designator.getArrayName() + "' as an array.",
					designator);
		}
		if (Struct.Array != arrayObj.getType().getKind()) {
			report_error("Trying to use variable '" + designator.getArrayName()
					+ "' as an array, but it's type is different", designator);
		}
		if (Tab.intType != designator.getExpr().struct) {
			report_error("Expression must be of int type when trying to access the array elements!",
					designator);
		}
		designator.obj = new Obj(Obj.Elem, "elem", arrayObj.getType().getElemType());
	}
	
	public void visit(AccessField accessField) {
		Obj enumObj = Tab.find(accessField.getVarName());
		if (null == enumObj || Tab.noObj == enumObj) {
			report_error("ERROR: Using undeclared enumeration '" + accessField.getVarName() + "'",
					accessField);
		}
		if (Obj.Type != enumObj.getKind()) {
			report_error(
					"ERROR: Name '" + accessField.getVarName() + "' used as an enum, but it's not of that kind.",
					accessField);
		}
		Obj constantObj = enumObj.getType().getMembersTable().searchKey(accessField.getField());
		if (null == constantObj || Tab.noObj == constantObj) {
			report_error("ERROR: Constant '" + accessField.getField() + "' is not part of '"
					+ accessField.getVarName() + "' enumeration!", accessField);
			constantObj = Tab.noObj;
		}
		accessField.obj = constantObj;
	}
	
	public void visit(ArrayName arrayName) {
		arrayName.obj =Tab.find(arrayName.getI1());
	}

	public void visit(ReturnExpr returnExpr) {
		returnFound = true;
		Struct currMethType = currentMethod.getType();
		if (!currMethType.compatibleWith(returnExpr.getExpr().struct)) {
			report_error("ERROR: Return type not compatible with function " + currentMethod.getName(), returnExpr);
		}
	}

	// ENUMERATIONS

	public void visit(EnumDecl enumDecl) {
		enumDecl.obj = currentEnum;
		currentEnum = null;
		lastEnumValue = 0;
	}

	public void visit(EnumName enumName) {
		currentEnum = Tab.insert(Obj.Type, enumName.getEnumName(), new Struct(Struct.Enum));
		lastEnumValue = 0;
	}

	public void visit(EnumNoDefault enumNoDefault) {
		if (null == currentEnum) {
			report_error("Found an enum outside of enum decl..", enumNoDefault);
		}
		enumNoDefault.obj = Tab.insert(Obj.Con, enumNoDefault.getId(), Tab.intType);
		enumNoDefault.obj.setAdr(lastEnumValue++);
		boolean success = currentEnum.getType().getMembersTable().insertKey(enumNoDefault.obj);
		if (!success) {
			report_error("Name conflict: there's an enumeration with the same name", enumNoDefault);
		}
	}

	public void visit(EnumDefault enumDefault) {
		if (null == currentEnum) {
			report_error("Found an enum outside of enum decl..", enumDefault);
		}
		enumDefault.obj = Tab.insert(Obj.Con, enumDefault.getId(), Tab.intType);
		enumDefault.obj.setAdr(enumDefault.getVal());
		lastEnumValue = enumDefault.getVal();
		boolean success = currentEnum.getType().getMembersTable().insertKey(enumDefault.obj);
		if (!success) {
			report_error("Name conflict: there's an enumeration with the same name", enumDefault);
		}
	}

	// EXPRESSIONS

	public void visit(ArithmeticExpr expr) {
		expr.struct = expr.getTermList().struct;
	}

	public void visit(SingleTermExpr expr) {
		expr.struct = expr.getTerm().struct;
	}

	public void visit(Term t) {
		t.struct = t.getFactorList().struct;
	}

	public void visit(AddopExpr expr) {
		TermList tl = expr.getTermList();
		Term t = expr.getTerm();
		// If it's an addop we need all terms to be ints
		boolean areInts = true;
		if (null != tl) {
			areInts = areInts && (tl.struct == Tab.intType);
		}
		if (null != t) {
			areInts = areInts && (t.struct == Tab.intType);
		}

		if (areInts) {
			expr.struct = t.struct;
			report_info("Found an addop expression.", expr);
		} else {
			expr.struct = Tab.noType;
			report_error("ERROR: Using an addop expression on non-int types", expr);
		}
	}

	public void visit(FuncCallFactor factor) {
		Designator functionDesignator = factor.getDesignator();
		if (!(functionDesignator instanceof NamedDesignator)) {
			report_error("ERROR: Only global functions supported for now!", factor);
			factor.struct = Tab.noType;
			return;
		}
		NamedDesignator namedDesignator = (NamedDesignator) functionDesignator;
		Obj funcObj = Tab.find(namedDesignator.getName());
		if (null == funcObj || Tab.noObj == funcObj) {
			report_error("ERROR: Trying to call an undeclared function!", factor);
			factor.struct = Tab.noType;
			return;
		}
		if (funcObj.getKind() != Obj.Meth) {
			report_error(
					"ERROR: Name " + namedDesignator.getName() + "used as a function call, but it's not a function",
					factor);
			factor.struct = Tab.noType;
			return;
		}
		factor.struct = funcObj.getType();
	}

	public void visit(ParenthesisExpr expr) {
		expr.struct = expr.getExpr().struct;
	}

	public void visit(NegatedFactor neg) {
		if (Tab.intType != neg.getFactor().struct) {
			report_error("ERROR: Trying to negate a non int factor!", neg);
			neg.struct = Tab.noType;
			return;
		}
		neg.struct = Tab.intType;
	}

	public void visit(SingleFactor factor) {
		factor.struct = factor.getFactor().struct;
	}

	public void visit(MulopExpr expr) {
		FactorList fl = expr.getFactorList();
		Factor f = expr.getFactor();
		// If it's an mulop we need all factors to be ints
		boolean areInts = true;
		if (null != fl) {
			areInts = areInts && (fl.struct == Tab.intType);
		}
		if (null != f) {
			areInts = areInts && (f.struct == Tab.intType);
		}

		if (areInts) {
			expr.struct = f.struct;
			report_info("Found a mulop expression.", expr);
		} else {
			expr.struct = Tab.noType;
			report_error("ERROR: Using mulop expression on non-int types", expr);
		}
	}

	public void visit(NumFactor numFactor) {
		numFactor.struct = Tab.intType;
	}

	public void visit(CharFactor charFactor) {
		charFactor.struct = Tab.charType;
	}

	public void visit(DesignatorFactor designatorFactor) {
		Designator designator = designatorFactor.getDesignator();
		if (designator.getClass() == NamedDesignator.class) {
			NamedDesignator namedDesignator = (NamedDesignator) designator;
			Obj varObj = namedDesignator.obj;
			if (Obj.Var != varObj.getKind()) {
				report_error("ERROR: Name '" + namedDesignator.getName()
						+ "' used as a variable, but it's not of that kind.", designatorFactor);
				designatorFactor.struct = Tab.noType;
				return;
			}
			designatorFactor.struct = varObj.getType();
		} else if (designator.getClass() == AccessField.class) {
			// Only enum access currently supported
			AccessField accessField = (AccessField) designator;
			Obj constantObj = Tab.find(accessField.getField());
			designatorFactor.struct = constantObj.getType();
		} else {
			AccessArray arrayAccess = (AccessArray) designator;
			designatorFactor.struct = arrayAccess.obj.getType();
		}
	}

	public void visit(OperatorNew operator) {
		report_error("Not supported: only built in types and arrays supported", operator);
		operator.struct = Tab.noType;
	}

	public void visit(OperatorNewArray operator) {
		if (operator.getExpr().struct != Tab.intType) {
			report_error("When instancing an array, the number of elements must be an integer", operator);
			operator.struct = Tab.noType;
			return;
		}
		if (Tab.noType == operator.getType().struct) {
			report_error(
					"Seems like you're trying to create an instance of void array. Supported types are int and char",
					operator);
			operator.struct = Tab.noType;
			return;
		}
		report_info("Allocating array", operator);
		operator.struct = new Struct(Struct.Array, operator.getType().struct);
	}
	
	public void visit(Assignment assignment) {
		if(assignment.getDesignator().getClass() == AccessField.class) {
			// We currently don't support fields so this is ok
			report_error("Can't assign values to enums!", assignment);
		}
		else if(assignment.getDesignator().getClass() == AccessArray.class) {
			AccessArray accessArray = (AccessArray) assignment.getDesignator();
			if(!accessArray.obj.getType().compatibleWith(assignment.getExpr().struct)) {
				report_error("Expression is not compatible with array type!", assignment);
			}
		}
		else {
			NamedDesignator named = (NamedDesignator) assignment.getDesignator();
			if(named.obj.getKind() != Obj.Var) {
				report_error("Trying to assign expression to something that isn't a variable: '" + named.getName() +"'", assignment);
			} else if (!named.obj.getType().compatibleWith(assignment.getExpr().struct)) {
				report_error("Incompatible types for variable '" + named.getName() + "' and the expression", assignment);
			}
		}
		
	}
	
	public void visit(Increment increment) {
		if(increment.getDesignator().getClass() == AccessField.class) {
			// We currently don't support fields so this is ok
			report_error("Can't increment enums!", increment);
		} else if(increment.getDesignator().getClass() == AccessArray.class) {
			AccessArray accessArray = (AccessArray) increment.getDesignator();
			if(accessArray.obj.getType() != Tab.intType) {
				report_error("Can't increment non-int array", increment);
			}
		} else {
			NamedDesignator named = (NamedDesignator) increment.getDesignator();
			if(named.obj.getKind() != Obj.Var) {
				report_error("Trying to increment something that isn't a variable: '" + named.getName() +"'", increment);
			} else if (named.obj.getType() != Tab.intType) {
				report_error("Incompatible types for variable '" + named.getName() + "'. Can only increment integers", increment);
			}
		}
	}
	
	public void visit(Decrement decrement) {
		if(decrement.getDesignator().getClass() == AccessField.class) {
			// We currently don't support fields so this is ok
			report_error("Can't increment enums!", decrement);
		} else if(decrement.getDesignator().getClass() == AccessArray.class) {
			AccessArray accessArray = (AccessArray) decrement.getDesignator();
			if(accessArray.obj.getType() != Tab.intType) {
				report_error("Can't increment non-int array", decrement);
			}
		} else {
			NamedDesignator named = (NamedDesignator) decrement.getDesignator();
			if(named.obj.getKind() != Obj.Var) {
				report_error("Trying to increment something that isn't a variable: '" + named.getName() +"'", decrement);
			} else if (named.obj.getType() != Tab.intType) {
				report_error("Incompatible types for variable '" + named.getName() + "'. Can only increment integers", decrement);
			}
		}
	}

	public boolean passed() {
		return !errorDetected;
	}

}
