package de.thm.mni.compilerbau.phases._04b_semant;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.absyn.visitor.Visitable;
import de.thm.mni.compilerbau.table.*;
import de.thm.mni.compilerbau.types.ArrayType;
import de.thm.mni.compilerbau.types.PrimitiveType;
import de.thm.mni.compilerbau.utils.SplError;

public class NodeVisitorSemant extends DoNothingVisitor {

    ProcedureBodyChecker procedureBodyChecker;
    SymbolTable symbolTable;

    public NodeVisitorSemant(ProcedureBodyChecker procedureBodyChecker, SymbolTable symbolTable) {
        this.procedureBodyChecker = procedureBodyChecker;
        this.symbolTable = symbolTable;
    }

    @Override
    public void visit(Program program) {
        Entry e = symbolTable.lookup(new Identifier("main"));
        if(e == null){
            throw SplError.MainIsMissing();
        }
        if(!(e instanceof ProcedureEntry)){
            throw SplError.MainIsNotAProcedure();
        }
        if(((ProcedureEntry) e).parameterTypes.size() > 0){
            throw SplError.MainMustNotHaveParameters();
        }

        for(Visitable visitable: program.declarations){
            if(visitable instanceof ProcedureDeclaration){
                visitable.accept(this);
            }
        }

    }

    public void visit(ProcedureDeclaration procedureDeclaration){
        ProcedureEntry procEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
        NodeVisitorSemant localVisitor = new NodeVisitorSemant(procedureBodyChecker,procEntry.localTable);
        for (Statement statement:procedureDeclaration.body) {
            statement.accept(localVisitor);
        }
    }

    public void visit(CallStatement callStatement){
        if(!(symbolTable.getUpperLevel().orElseThrow().lookup(callStatement.procedureName,SplError.UndefinedProcedure(callStatement.position,callStatement.procedureName)) instanceof ProcedureEntry)){
            throw SplError.CallOfNonProcedure(callStatement.position,callStatement.procedureName);
        }
        ProcedureEntry procedureEntry = (ProcedureEntry) symbolTable.getUpperLevel().orElseThrow().lookup(callStatement.procedureName);
        if(procedureEntry.parameterTypes.size() < callStatement.argumentList.size()){
            throw SplError.TooManyArguments(callStatement.position,callStatement.procedureName);
        }
        if(procedureEntry.parameterTypes.size() > callStatement.argumentList.size()){
            throw SplError.TooFewArguments(callStatement.position,callStatement.procedureName);
        }

        for(int i=0;i<callStatement.argumentList.size();i++){
            callStatement.argumentList.get(i).accept(this);

            if(procedureEntry.parameterTypes.get(i).isReference){
                if(!(callStatement.argumentList.get(i) instanceof VariableExpression)){
                    throw SplError.ArgumentMustBeAVariable(callStatement.argumentList.get(i).position,callStatement.procedureName,i);
                }
            }
            procedureBodyChecker.checkType(procedureEntry.parameterTypes.get(i).type,callStatement.argumentList.get(i).dataType,SplError.ArgumentTypeMismatch(callStatement.argumentList.get(i).position,callStatement.procedureName, i));
        }
    }

    public void visit(IfStatement ifStatement){
        ifStatement.condition.accept(this);

        procedureBodyChecker.checkType(PrimitiveType.boolType, ifStatement.condition.dataType,SplError.IfConditionMustBeBoolean(ifStatement.position));
        ifStatement.thenPart.accept(this);
        ifStatement.elsePart.accept(this);
    }

    public void visit(WhileStatement whileStatement){
        whileStatement.condition.accept(this);

        procedureBodyChecker.checkType(PrimitiveType.boolType, whileStatement.condition.dataType,SplError.WhileConditionMustBeBoolean(whileStatement.position));
        whileStatement.body.accept(this);
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        arrayAccess.index.accept(this);
        arrayAccess.array.accept(this);


        procedureBodyChecker.checkType(PrimitiveType.intType,arrayAccess.index.dataType,SplError.IndexingWithNonInteger(arrayAccess.position));

        if(arrayAccess.array.dataType instanceof ArrayType){
            arrayAccess.dataType = ((ArrayType) arrayAccess.array.dataType).baseType;
        }else{
            throw SplError.IndexingNonArray(arrayAccess.position);
        }



    }
    public void visit(NamedVariable namedVariable){

        Entry e = symbolTable.lookup(namedVariable.name,SplError.UndefinedVariable(namedVariable.position,namedVariable.name));
        if(!(e instanceof VariableEntry)){
            throw SplError.NotAVariable(namedVariable.position,namedVariable.name);
        }
        namedVariable.dataType = ((VariableEntry) e).type;
    }

    public void visit(AssignStatement assignStatement){
        assignStatement.target.accept(this);
        assignStatement.value.accept(this);



        procedureBodyChecker.checkType(PrimitiveType.intType,assignStatement.target.dataType,SplError.AssignmentRequiresIntegers(assignStatement.position));
        procedureBodyChecker.checkType(assignStatement.target.dataType,assignStatement.value.dataType,SplError.AssignmentHasDifferentTypes(assignStatement.position));
    }

    public void visit(VariableExpression variableExpression){
        variableExpression.variable.accept(this);
        variableExpression.dataType = variableExpression.variable.dataType;
    }

    public void visit(CompoundStatement compoundStatement){
        for(Statement statement:compoundStatement.statements){
            statement.accept(this);
        }
    }

    public void visit(BinaryExpression binaryExpression){
        binaryExpression.leftOperand.accept(this);
        binaryExpression.rightOperand.accept(this);

        procedureBodyChecker.checkType(binaryExpression.leftOperand.dataType,binaryExpression.rightOperand.dataType,SplError.OperatorDifferentTypes(binaryExpression.position));
        if(binaryExpression.operator.isComparison()){
            procedureBodyChecker.checkType(PrimitiveType.intType,binaryExpression.leftOperand.dataType,SplError.ComparisonNonInteger(binaryExpression.leftOperand.position));
            procedureBodyChecker.checkType(PrimitiveType.intType,binaryExpression.rightOperand.dataType,SplError.ComparisonNonInteger(binaryExpression.rightOperand.position));
            binaryExpression.dataType = PrimitiveType.boolType;
        }else{
            procedureBodyChecker.checkType(PrimitiveType.intType,binaryExpression.leftOperand.dataType,SplError.ArithmeticOperatorNonInteger(binaryExpression.leftOperand.position));
            procedureBodyChecker.checkType(PrimitiveType.intType,binaryExpression.rightOperand.dataType,SplError.ArithmeticOperatorNonInteger(binaryExpression.rightOperand.position));
            binaryExpression.dataType = PrimitiveType.intType;
        }
    }
    public void visit(IntLiteral intLiteral){
        intLiteral.dataType = PrimitiveType.intType;
    }

}
