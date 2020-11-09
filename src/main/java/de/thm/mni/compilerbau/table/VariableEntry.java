package de.thm.mni.compilerbau.table;

import de.thm.mni.compilerbau.types.Type;

/**
 * Represents the table entry for variable- and parameter-declarations in SPL.
 * There are no separate entry classes for parameters, as they behave identically in respect to code generation.
 */
public class VariableEntry extends Entry {
    public final Type type;
    public final boolean isReference;
    public int offset; // This value has to be set in phase 5

    /**
     * Creates a new {@link Entry} representing a declared SPL variable. This variable can be a local variable or the
     * parameter of a procedure.
     * @param name        The name of the variable/parameter.
     * @param type        The semantic type of the variable. Calculated by looking at the respective type expression.
     * @param isReference If the variable is a reference.
     *                    Only ever true for reference parameters, false for non-reference parameters and local variable.
     */
    public VariableEntry(Identifier name, Type type, boolean isReference) {
        super(name);
        this.type = type;
        this.isReference = isReference;
    }

    @Override
    public String toString() {
        return String.format("var: %s%s", isReference ? "ref " : "", type);
    }
}
