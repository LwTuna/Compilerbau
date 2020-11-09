package de.thm.mni.compilerbau.table;

/**
 * Represents a table entry for a declaration in SPL.
 */
public abstract class Entry {
    public final Identifier name;

    /**
     * @param name The name of the defined symbol.
     */
    Entry(Identifier name) {
        this.name = name;
    }
}
