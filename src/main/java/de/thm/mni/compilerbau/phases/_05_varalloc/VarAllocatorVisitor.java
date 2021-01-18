package de.thm.mni.compilerbau.phases._05_varalloc;

import de.thm.mni.compilerbau.absyn.ProcedureDeclaration;
import de.thm.mni.compilerbau.absyn.Program;
import de.thm.mni.compilerbau.absyn.VariableDeclaration;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.absyn.visitor.Visitable;
import de.thm.mni.compilerbau.table.ParameterType;
import de.thm.mni.compilerbau.table.ProcedureEntry;
import de.thm.mni.compilerbau.table.SymbolTable;
import de.thm.mni.compilerbau.table.VariableEntry;

public class VarAllocatorVisitor extends DoNothingVisitor {

    private SymbolTable symbolTable;
    private VarAllocProcVisitor varAllocProcVisitor;


    public VarAllocatorVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        varAllocProcVisitor = new VarAllocProcVisitor(symbolTable);
    }

    @Override
    public void visit(Program program) {


        for(Visitable visitable: program.declarations){
            if(visitable instanceof ProcedureDeclaration){
                visitable.accept(this);
            }
        }

        program.accept(varAllocProcVisitor);
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        ProcedureEntry procedureEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
        SymbolTable localTable = ((ProcedureEntry) symbolTable.lookup(procedureDeclaration.name)).localTable;

        var varOffset = 0;

        for(VariableDeclaration variableDeclaration : procedureDeclaration.variables){
            VariableEntry variableEntry = (VariableEntry) localTable.lookup(variableDeclaration.name);
            varOffset -=  variableEntry.type.byteSize;
            variableEntry.offset = varOffset;
        }

        var argOffset = 0;
        for(int i=0;i<procedureEntry.parameterTypes.size();i++){
            ParameterType parameterType = procedureEntry.parameterTypes.get(i);

            parameterType.offset = argOffset;
            VariableEntry paramEntry = (VariableEntry) localTable.lookup(procedureDeclaration.parameters.get(i).name);
            paramEntry.offset = argOffset;
            argOffset += paramEntry.isReference ? VarAllocator.REFERENCE_BYTESIZE: parameterType.type.byteSize;
        }


        procedureEntry.argumentAreaSize = argOffset;
        procedureEntry.localVarAreaSize = -varOffset;


    }
}
