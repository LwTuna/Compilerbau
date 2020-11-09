package de.thm.mni.compilerbau.table;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the table entry for procedure declarations in SPL.
 */
public class ProcedureEntry extends Entry {
    public final SymbolTable localTable;
    public final List<ParameterType> parameterTypes;
    public int argumentAreaSize, outgoingAreaSize, localVarAreaSize; // This values have to be set in phase 5

    /**
     * Creates a new {@link Entry} representing a declared SPL procedure.
     * @param name           The name of the procedure.
     * @param localTable     The local table containing all local scope specific entries (parameters, local variables).
     * @param parameterTypes A list describing the parameters of the procedure.
     *                       See {@link ParameterType} for more information.
     */
    public ProcedureEntry(Identifier name, SymbolTable localTable, List<ParameterType> parameterTypes) {
        super(name);
        this.localTable = localTable;
        this.parameterTypes = parameterTypes;
    }

    /**
     * This static method is reserved for the creation of entries for predefined procedures, where the calculations of
     * phase 5 have to be performed manually.
     * @param name The name of the procedure.
     * @param parameterTypes A list describing the parameters of the procedure.
     * @param argumentAreaSize The size in byte needed on the stack frame to store all arguments of the procedure.
     * @return A ProcedureEntry containing all necessary information from phase 5 manually computed.
     */
    public static ProcedureEntry predefinedProcedureEntry(Identifier name, List<ParameterType> parameterTypes, int argumentAreaSize) {
        final var procedureEntry = new ProcedureEntry(name, null, parameterTypes);
        procedureEntry.argumentAreaSize = argumentAreaSize;
        return procedureEntry;
    }

    @Override
    public String toString() {
        return String.format("proc: %s", this.parameterTypes.stream().map(Objects::toString).collect(Collectors.joining(", ")));
    }
}
