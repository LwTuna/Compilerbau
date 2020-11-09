package de.thm.mni.compilerbau.phases._04a_tablebuild;

import de.thm.mni.compilerbau.phases._05_varalloc.VarAllocator;
import de.thm.mni.compilerbau.table.*;
import de.thm.mni.compilerbau.types.PrimitiveType;

import java.util.List;

final class TableInitializer {
    private TableInitializer() {
    }

    /**
     * Creates a new SymbolTable and enters entries for all predefined types and procedures.
     * @return A new instance of the symbol table representing the global definition scope.
     */
    static SymbolTable initializeGlobalTable() {
        SymbolTable table = new SymbolTable();
        enterPredefinedTypes(table);
        enterPredefinedProcedures(table);
        return table;
    }


    private static void enterPredefinedTypes(SymbolTable table) {
        table.enter(new TypeEntry(new Identifier("int"), PrimitiveType.intType));
    }

    private static void enterPredefinedProcedures(SymbolTable table) {
        // printi(i: int)
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("printi"), List.of(
                new ParameterType(PrimitiveType.intType, false, 0)),
                PrimitiveType.intType.byteSize));

        // printc(i: int)
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("printc"), List.of(
                new ParameterType(PrimitiveType.intType, false, 0)),
                PrimitiveType.intType.byteSize));
        // readi(ref i: int)
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("readi"), List.of(
                new ParameterType(PrimitiveType.intType, true, 0)),
                VarAllocator.REFERENCE_BYTESIZE));
        // readc(ref i: int)
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("readc"), List.of(
                new ParameterType(PrimitiveType.intType, true, 0)),
                VarAllocator.REFERENCE_BYTESIZE));
        // exit()
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("exit"), List.of(), 0));
        // time(ref i: int)
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("time"), List.of(
                new ParameterType(PrimitiveType.intType, true, 0)),
                VarAllocator.REFERENCE_BYTESIZE));
        // clearAll(color: int)
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("clearAll"), List.of(
                new ParameterType(PrimitiveType.intType, false, 0)),
                PrimitiveType.intType.byteSize));
        // setPixel(x: int, y: int, color: int)
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("setPixel"), List.of(
                new ParameterType(PrimitiveType.intType, false, 0),
                new ParameterType(PrimitiveType.intType, false, PrimitiveType.intType.byteSize),
                new ParameterType(PrimitiveType.intType, false, 2 * PrimitiveType.intType.byteSize)),
                3 * PrimitiveType.intType.byteSize));
        // drawLine(x1: int, y1: int, x2: int, y2: int, color: int)
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("drawLine"), List.of(
                new ParameterType(PrimitiveType.intType, false, 0),
                new ParameterType(PrimitiveType.intType, false, PrimitiveType.intType.byteSize),
                new ParameterType(PrimitiveType.intType, false, 2 * PrimitiveType.intType.byteSize),
                new ParameterType(PrimitiveType.intType, false, 3 * PrimitiveType.intType.byteSize),
                new ParameterType(PrimitiveType.intType, false, 4 * PrimitiveType.intType.byteSize)),
                5 * PrimitiveType.intType.byteSize));
        // drawCircle(x0: int, y0: int, radius: int, color: int)
        table.enter(ProcedureEntry.predefinedProcedureEntry(new Identifier("drawCircle"), List.of(
                new ParameterType(PrimitiveType.intType, false, 0),
                new ParameterType(PrimitiveType.intType, false, PrimitiveType.intType.byteSize),
                new ParameterType(PrimitiveType.intType, false, 2 * PrimitiveType.intType.byteSize),
                new ParameterType(PrimitiveType.intType, false, 3 * PrimitiveType.intType.byteSize)),
                4 * PrimitiveType.intType.byteSize));
    }

}
