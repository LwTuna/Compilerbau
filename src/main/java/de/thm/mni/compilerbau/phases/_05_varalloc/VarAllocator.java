package de.thm.mni.compilerbau.phases._05_varalloc;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.table.ParameterType;
import de.thm.mni.compilerbau.table.ProcedureEntry;
import de.thm.mni.compilerbau.table.SymbolTable;
import de.thm.mni.compilerbau.table.VariableEntry;
import de.thm.mni.compilerbau.utils.NotImplemented;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is used to calculate the memory needed for variables and stack frames of the currently compiled SPL program.
 * Those value have to be stored in their corresponding fields in the {@link ProcedureEntry}, {@link VariableEntry} and
 * {@link ParameterType} classes.
 */
public class VarAllocator {
    public static final int REFERENCE_BYTESIZE = 4;

    private final boolean showVarAlloc;

    public VarAllocator(boolean showVarAlloc) {
        this.showVarAlloc = showVarAlloc;
    }

    public void allocVars(Program program, SymbolTable table) {
        //TODO (assignment 5): Allocate stack slots for all parameters and local variables

        throw new NotImplemented();

        //TODO: Uncomment this when the above exception is removed!
        //if (showVarAlloc) System.out.println(formatVars(program, table));
    }

    /**
     * Formats the variable allocation to a human readable format
     *
     * @param program The abstract syntax tree of the program
     * @param table   The symbol table containing all symbols of the spl program
     * @return A human readable string describing the allocated memory
     */
    private String formatVars(Program program, SymbolTable table) {
        return program.declarations.stream().filter(dec -> dec instanceof ProcedureDeclaration).map(dec -> (ProcedureDeclaration) dec).map(procDec -> {
            ProcedureEntry entry = (ProcedureEntry) table.lookup(procDec.name);

            return String.format("Variable allocation for procedure '%s'\n%s\nsize of argument area = %s\n%s%ssize of localvar area = %d\nsize of outgoing area = %d\n",
                    procDec.name,
                    IntStream.range(0, entry.parameterTypes.size()).mapToObj(i -> String.format("arg %d: sp + %d", i, entry.parameterTypes.get(i).offset)).collect(Collectors.joining()),
                    entry.argumentAreaSize,
                    procDec.parameters.stream().map(parDec -> String.format("param '%s': fp + %d\n", parDec.name, ((VariableEntry) entry.localTable.lookup(parDec.name)).offset)).collect(Collectors.joining()),
                    procDec.variables.stream().map(varDec -> String.format("var '%s': fp - %d\n", varDec.name, -((VariableEntry) entry.localTable.lookup(varDec.name)).offset)).collect(Collectors.joining()),
                    entry.localVarAreaSize,
                    entry.outgoingAreaSize
            );
        }).collect(Collectors.joining("\n"));
    }
}
