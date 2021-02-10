package de.thm.mni.compilerbau.phases._06_codegen;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.table.ProcedureEntry;
import de.thm.mni.compilerbau.table.SymbolTable;
import de.thm.mni.compilerbau.table.VariableEntry;
import de.thm.mni.compilerbau.types.ArrayType;

public class CodeVisitor extends DoNothingVisitor {

    private final CodePrinter output;
    private SymbolTable symbolTable;
    private Program program;

    private Register register;

    public CodeVisitor(CodePrinter output, SymbolTable symbolTable, Program program) {
        this.output = output;
        this.symbolTable = symbolTable;
        this.program = program;
        register = new Register(8);
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {

        output.emit(".export "+procedureDeclaration.name);
        output.emitLabel(procedureDeclaration.name.toString());
        ProcedureEntry procedureEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);



        for(Statement statement :procedureDeclaration.body){
            statement.accept(this);
        }

    }


    public void visit(ArrayAccess arrayAccess){


        if(!(arrayAccess.dataType instanceof ArrayType)) return;
        int arraySize = ((ArrayType) arrayAccess.dataType).arraySize;

        instruction("add",register.toString(),"$25",);

        register = register.next(1);
        arrayAccess.index.accept(this);

        instruction("add",register.next(1).toString(),"$0",String.valueOf(arraySize));
        instruction("bgeu", register.toString(),register.next(1).toString(),"_indexError");
        register = register.previous(1);


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

        if( symbolTable.lookup(namedVariable.name) instanceof VariableEntry) {

            VariableEntry variableEntry = (VariableEntry) symbolTable.lookup(namedVariable.name);
            instruction("add",register.toString(),"$25",String.valueOf(variableEntry.offset));

        }

    }

    public void visit(VariableExpression variableExpression){
        register = register.next(1);
        variableExpression.variable.accept(this);
        instruction("ldw",register.toString(),register.toString(),"0");

    }

    public void visit(AssignStatement assignStatement){
        assignStatement.target.accept(this);
        assignStatement.value.accept(this);
        instruction("stw",register.toString(),register.previous(1).toString(),"0");
        register = register.previous(1);
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
