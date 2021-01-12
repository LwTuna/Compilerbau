package de.thm.mni.compilerbau.phases._04b_semant;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.table.*;
import de.thm.mni.compilerbau.types.ArrayType;
import de.thm.mni.compilerbau.types.PrimitiveType;
import de.thm.mni.compilerbau.types.Type;
import de.thm.mni.compilerbau.utils.NotImplemented;
import de.thm.mni.compilerbau.utils.SplError;

/**
 * This class is used to check if the currently compiled SPL program is semantically valid.
 * Every statement and expression has to be checked, to ensure that every type is correct.
 * <p>
 * Calculated {@link Type}s can be stored in and read from the dataType field of the {@link Expression},
 * {@link TypeExpression} or {@link Variable} classes.
 */
public class ProcedureBodyChecker {

    public void procedureCheck(Program program, SymbolTable globalTable) {
        NodeVisitorSemant nodeVisitorSemant = new NodeVisitorSemant(this,globalTable);
        program.accept(nodeVisitorSemant);
    }


    protected void checkType(Type expected, Type actual, SplError error) throws SplError {
        if(expected != actual) throw error;
    }
}
