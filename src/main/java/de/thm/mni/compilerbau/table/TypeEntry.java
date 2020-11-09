package de.thm.mni.compilerbau.table;

import de.thm.mni.compilerbau.types.Type;

/**
 * Represents the table entry for type-declarations in SPL.
 */
public class TypeEntry extends Entry {
    public final Type type;

    /**
     * Creates a new {@link Entry} representing a declared SPL {@link Type}.
     * @param name The name of declared type
     * @param type The "meaning" of the type declaration.
     *             Determined by the type expression on the right of the type declaration.
     *             See {@link Type} and its subclasses.
     */
    public TypeEntry(Identifier name, Type type) {
        super(name);
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("type: %s", type);
    }
}
