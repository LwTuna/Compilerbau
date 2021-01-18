package de.thm.mni.compilerbau.phases._05_varalloc;

import de.thm.mni.compilerbau.absyn.CallStatement;
import de.thm.mni.compilerbau.absyn.ProcedureDeclaration;
import de.thm.mni.compilerbau.absyn.Program;
import de.thm.mni.compilerbau.absyn.Statement;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.absyn.visitor.Visitable;
import de.thm.mni.compilerbau.table.ProcedureEntry;
import de.thm.mni.compilerbau.table.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class VarAllocProcVisitor extends DoNothingVisitor {


    private SymbolTable symbolTable;

    public VarAllocProcVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public void visit(Program program) {
        for(Visitable visitable: program.declarations){
            if(visitable instanceof ProcedureDeclaration){
                visitable.accept(this);
            }
        }
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        ProcedureEntry procedureEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
        SymbolTable localTable = ((ProcedureEntry) symbolTable.lookup(procedureDeclaration.name)).localTable;

        List<CallStatement> callStatements = new ArrayList<>();

        for(Statement s : procedureDeclaration.body){
            if(s instanceof CallStatement){
                callStatements.add((CallStatement) s);
            }
        }

        if(callStatements.size() <= 0){
            procedureEntry.outgoingAreaSize = -1;
        }else{
            procedureEntry.outgoingAreaSize = callStatements.stream().mapToInt(callStatement -> ((ProcedureEntry)symbolTable.lookup(callStatement.procedureName)).argumentAreaSize).max().getAsInt();
        }


    }
}
