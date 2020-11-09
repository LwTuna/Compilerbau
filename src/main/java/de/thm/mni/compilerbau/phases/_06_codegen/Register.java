package de.thm.mni.compilerbau.phases._06_codegen;

class Register {
    final int number;

    Register(int number) {
        this.number = number;
    }

    /**
     * Checks if the register is available for free use, so a value can be stored in it.
     * Only a few of the registers in the ECO32 system, are available for free use. Other registers hold special values
     * like the stack or frame pointer registers or are reserved for the systems use only.
     * @return true is available for free use.
     */
    boolean isFreeUse() {
        return number >= 8 && number <= 23;
    }

    /**
     * Returns the previous register as the pedant of a pop-instruction for the register stack.
     * @param offset The amount of registers to go back.
     * @return The register preceding the current one by the given offset.
     */
    Register previous(int offset) {
        return new Register(number - offset);
    }

    @Override
    public String toString() {
        return "$" + number;
    }
}
