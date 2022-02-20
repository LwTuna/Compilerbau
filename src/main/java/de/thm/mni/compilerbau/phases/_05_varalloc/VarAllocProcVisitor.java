package de.thm.mni.compilerbau.phases._05_varalloc;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.absyn.visitor.Visitable;
import de.thm.mni.compilerbau.table.ProcedureEntry;
import de.thm.mni.compilerbau.table.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class VarAllocProcVisitor extends DoNothingVisitor {


    private SymbolTable symbolTable;

    private int maxCall;

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

    public void visit(CompoundStatement statement){
        for(Statement s:statement.statements){
            s.accept(this);
        }
    }
    public void visit(IfStatement statement){
        statement.thenPart.accept(this);
        statement.elsePart.accept(this);
    }
    public void visit(WhileStatement statement){
        statement.body.accept(this);
    }
    public void visit(CallStatement statement){
        ProcedureEntry procedureEntry= (ProcedureEntry) symbolTable.lookup(statement.procedureName);
        maxCall = Math.max(maxCall,procedureEntry.argumentAreaSize);
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        ProcedureEntry procedureEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
        SymbolTable localTable = ((ProcedureEntry) symbolTable.lookup(procedureDeclaration.name)).localTable;

        maxCall = -1;
        for(Statement s : procedureDeclaration.body){
            s.accept(this);
        }

        procedureEntry.outgoingAreaSize = maxCall;



    }
}
