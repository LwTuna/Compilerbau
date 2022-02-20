package de.thm.mni.compilerbau.phases._06_codegen;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.table.Entry;
import de.thm.mni.compilerbau.table.ProcedureEntry;
import de.thm.mni.compilerbau.table.SymbolTable;
import de.thm.mni.compilerbau.table.VariableEntry;
import de.thm.mni.compilerbau.types.ArrayType;

public class CodeVisitor extends DoNothingVisitor {

    private final CodePrinter output;
    private SymbolTable symbolTable;

    private Register register;

    private int labelCounter;

    public CodeVisitor(CodePrinter output, SymbolTable symbolTable,int labelCounter) {
        this.output = output;
        this.symbolTable = symbolTable;
        this.labelCounter = labelCounter;
        register = new Register(7);
    }


    @Override
    public void visit(Program program) {

        for(GlobalDeclaration globalDeclaration:program.declarations){
            globalDeclaration.accept(this);
        }
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {

        output.emit(".export "+procedureDeclaration.name);
        output.emitLabel(procedureDeclaration.name.toString());
        ProcedureEntry procedureEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
        boolean hasCall = procedureEntry.outgoingAreaSize >= 0;

        int frameSize = hasCall
                ? procedureEntry.localVarAreaSize + procedureEntry.outgoingAreaSize +8
                : procedureEntry.localVarAreaSize +4;
        int returnOffset = -(procedureEntry.localVarAreaSize +8);
        int framePointerOffset = hasCall
                ? procedureEntry.outgoingAreaSize +4
                : 0;
        instruction("sub","$29","$29",String.valueOf(frameSize));
        instruction("stw","$25","$29",String.valueOf(framePointerOffset));
        instruction("add","$25","$29",String.valueOf(frameSize));
        if(hasCall) instruction("stw","$31","$25",String.valueOf(returnOffset));
        //BODY
        CodeVisitor codeVisitor = new CodeVisitor(output,procedureEntry.localTable,this.labelCounter);
        for(Statement statement :procedureDeclaration.body){

            statement.accept(codeVisitor);
        }
        this.labelCounter = codeVisitor.labelCounter;
        if(hasCall) instruction("ldw","$31","$25",String.valueOf(returnOffset));
        instruction("ldw","$25","$29",String.valueOf(framePointerOffset));
        instruction("add","$29","$29",String.valueOf(frameSize));

        instruction("jr","$31");


    }


    @Override
    public void visit(IfStatement ifStatement) {
        String elseLabel = "L"+(labelCounter++);
        String endLabel = !(ifStatement.elsePart instanceof EmptyStatement)
                ? "L"+(labelCounter++)
                : elseLabel;
        BinaryExpression binaryExpression = (BinaryExpression) ifStatement.condition;


        generateCondition(binaryExpression,elseLabel);
        ifStatement.thenPart.accept(this);

        if(!(ifStatement.elsePart instanceof EmptyStatement)){
            instruction("j",endLabel);
            output.emitLabel(elseLabel);
            ifStatement.elsePart.accept(this);
        }
        output.emitLabel(endLabel);


    }

    public void visit(CallStatement callStatement){
        ProcedureEntry procedureEntry = (ProcedureEntry) symbolTable.lookup(callStatement.procedureName);
        for(int i=0;i<procedureEntry.parameterTypes.size();i++){
            if(procedureEntry.parameterTypes.get(i).isReference){
                ((VariableExpression)callStatement.argumentList.get(i)).variable.accept(this);
            }else {
                callStatement.argumentList.get(i).accept(this);
            }

            instruction("stw",register.toString(),"$29","0");
            register = register.previous(1);
        }

        instruction("jal",callStatement.procedureName.toString());
    }

    public void visit(WhileStatement whileStatement){
        String condLabel = "L"+(labelCounter++);
        String endLabel = "L"+(labelCounter++);
        output.emitLabel(condLabel);

        BinaryExpression binaryExpression = (BinaryExpression) whileStatement.condition;
        generateCondition(binaryExpression,endLabel);

        whileStatement.body.accept(this);
        instruction("j",condLabel);
        output.emitLabel(endLabel);

    }

    public void generateCondition(BinaryExpression binaryExpression,String label){
        binaryExpression.leftOperand.accept(this);
        binaryExpression.rightOperand.accept(this);
        String mnemonic="";
        switch (binaryExpression.operator.flipComparison()){
            case EQU:
                mnemonic = "beq";
                break;
            case NEQ:
                mnemonic = "bne";
                break;
            case LST:
                mnemonic = "blt";
                break;
            case LSE:
                mnemonic = "ble";
                break;
            case GRT:
                mnemonic = "bgt";
                break;
            case GRE:
                mnemonic = "bge";
                break;
        }


        instruction(mnemonic,register.previous(1).toString(),register.toString(),label);

        register = register.previous(2);
    }

    public void visit(CompoundStatement compoundStatement){
        for(Statement statement:compoundStatement.statements){
            statement.accept(this);
        }
    }

    public void visit(ArrayAccess arrayAccess){
        int arraySize = ((ArrayType) arrayAccess.array.dataType).arraySize;

        arrayAccess.array.accept(this);

        arrayAccess.index.accept(this);

        instruction("add",register.next(1).toString(),"$0",String.valueOf(arraySize));
        instruction("bgeu", register.toString(),register.next(1).toString(),"_indexError");
        instruction("mul",register.toString(),register.toString(),String.valueOf(((ArrayType) arrayAccess.array.dataType).baseType.byteSize));

        register = register.previous(1);
        instruction("add",register.toString(),register.toString(),register.next(1).toString());

    }

    @Override
    public void visit(IntLiteral intLiteral) {
        register = register.next(1);
        instruction("add",register.toString(), "$0",String.valueOf(intLiteral.value));

    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        binaryExpression.leftOperand.accept(this);

        binaryExpression.rightOperand.accept(this);

        if(binaryExpression.operator.isArithmetic()){
            switch (binaryExpression.operator){
                case ADD:
                    instruction("add",register.previous(1).toString(),register.previous(1).toString(),register.toString());
                    break;
                case SUB:
                    instruction("sub",register.previous(1).toString(),register.previous(1).toString(),register.toString());
                    break;
                case MUL:
                    instruction("mul",register.previous(1).toString(),register.previous(1).toString(),register.toString());
                    break;
                case DIV:
                    instruction("div",register.previous(1).toString(),register.previous(1).toString(),register.toString());
                    break;
            }
            register = register.previous(1);
        }

    }

    public void visit(NamedVariable namedVariable){
        register = register.next(1);
        VariableEntry variableEntry = (VariableEntry) symbolTable.lookup(namedVariable.name);
        instruction("add",register.toString(),"$25",String.valueOf(variableEntry.offset));
        if(variableEntry.isReference){
            instruction("ldw",register.toString(),register.toString(),"0");
        }
    }

    public void visit(VariableExpression variableExpression){
        variableExpression.variable.accept(this);
        instruction("ldw",register.toString(),register.toString(),"0");

    }

    public void visit(AssignStatement assignStatement){
        assignStatement.target.accept(this);
        assignStatement.value.accept(this);
        instruction("stw",register.toString(),register.previous(1).toString(),"0");
        register = register.previous(2);
    }



    /**
     *
     * @param mnemonic The Operation to be performed
     * @param operands The Operands, must be >=1
     */
    private void instruction(String mnemonic,String... operands){
        if(operands.length <1) throw new IllegalArgumentException("Operands.length must be >=1");
        String formatString =String.format("\t"+mnemonic+"\t"+("%s,".repeat(operands.length-1))+"%s",operands);
        output.emit(formatString);
    }


}
