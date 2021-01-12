package de.thm.mni.compilerbau.phases._04a_tablebuild;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.table.*;
import de.thm.mni.compilerbau.types.ArrayType;
import de.thm.mni.compilerbau.utils.SplError;

import java.util.ArrayList;
import java.util.List;

public class NodeVisitor extends DoNothingVisitor {

    SymbolTable symbolTable;

    boolean showTables;

    public NodeVisitor(SymbolTable symbolTable,boolean showTables) {
        this.symbolTable = symbolTable;
        this.showTables = showTables;
    }



    public void visit(TypeDeclaration typeDeclaration){
        typeDeclaration.typeExpression.accept(this);
        symbolTable.enter(new TypeEntry(typeDeclaration.name,typeDeclaration.typeExpression.dataType),SplError.RedeclarationAsType(typeDeclaration.position,typeDeclaration.name));
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        SymbolTable localTable = new SymbolTable(symbolTable);
        NodeVisitor localVisitor = new NodeVisitor(localTable,showTables);
        List<ParameterType> parameterTypeList = new ArrayList<>();

        procedureDeclaration.parameters.forEach(item -> item.accept(localVisitor));
        procedureDeclaration.parameters.forEach(item -> parameterTypeList.add(new ParameterType(item.typeExpression.dataType,item.isReference)));

        procedureDeclaration.variables.forEach(item -> item.accept(localVisitor));

        symbolTable.enter(new ProcedureEntry(procedureDeclaration.name,localTable,parameterTypeList),SplError.RedeclarationAsProcedure(procedureDeclaration.position,procedureDeclaration.name));

        if(showTables){
            System.out.printf("symbol table at end of procedure '%s':\n" +localTable.toString()+"\n",procedureDeclaration.name);
        }
    }


    public void visit(VariableDeclaration variableDeclaration){
        variableDeclaration.typeExpression.accept(this);
        symbolTable.enter(new VariableEntry(variableDeclaration.name,variableDeclaration.typeExpression.dataType,false),SplError.RedeclarationAsVariable(variableDeclaration.position,variableDeclaration.name));
    }

    public void visit(ParameterDeclaration parameterDeclaration){
        NodeVisitor globalVisitor = new NodeVisitor(symbolTable.getUpperLevel().orElseThrow(),showTables);
        parameterDeclaration.typeExpression.accept(globalVisitor);

        if(!parameterDeclaration.isReference && parameterDeclaration.typeExpression.dataType instanceof ArrayType){
            throw SplError.MustBeAReferenceParameter(parameterDeclaration.position,parameterDeclaration.name);
        }

        symbolTable.enter(new VariableEntry(parameterDeclaration.name,parameterDeclaration.typeExpression.dataType,parameterDeclaration.isReference),SplError.RedeclarationAsParameter(parameterDeclaration.position,parameterDeclaration.name));
    }

    public void visit(ArrayTypeExpression arrayTypeExpression){
        arrayTypeExpression.baseType.accept(this);
        arrayTypeExpression.dataType = new ArrayType(arrayTypeExpression.baseType.dataType,arrayTypeExpression.arraySize);
    }

    public void visit(NamedTypeExpression namedTypeExpression){
        Entry e =symbolTable.find(namedTypeExpression.name).orElseThrow(() -> SplError.UndefinedType(namedTypeExpression.position,namedTypeExpression.name));
        if(!(e instanceof TypeEntry)){
            throw SplError.NotAType(namedTypeExpression.position,namedTypeExpression.name);
        }else{
            TypeEntry te = (TypeEntry) e;
            namedTypeExpression.dataType = te.type;
        }
    }
}
