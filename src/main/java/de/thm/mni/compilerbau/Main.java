package de.thm.mni.compilerbau;

import de.thm.mni.compilerbau.absyn.Program;
import de.thm.mni.compilerbau.phases._01_scanner.Scanner;
import de.thm.mni.compilerbau.phases._02_03_parser.Parser;
import de.thm.mni.compilerbau.phases._02_03_parser.Sym;
import de.thm.mni.compilerbau.phases._04a_tablebuild.TableBuilder;
import de.thm.mni.compilerbau.phases._04b_semant.ProcedureBodyChecker;
import de.thm.mni.compilerbau.phases._05_varalloc.VarAllocator;
import de.thm.mni.compilerbau.phases._06_codegen.CodeGenerator;
import de.thm.mni.compilerbau.utils.SplError;
import java_cup.runtime.DefaultSymbolFactory;
import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import static java.lang.System.exit;

class Main {

    /**
     * CUP encourages you to use {@link java_cup.runtime.ComplexSymbolFactory} as a {@link SymbolFactory} which we
     * don't need. The default implementation provides more than enough information for our needs.
     */
    @SuppressWarnings("deprecation")
    private static final SymbolFactory symbolFactory = new DefaultSymbolFactory();

    private static void showToken(Symbol token) {
        String formatted = String.format("TOKEN = %s in line %d, column %d",
                Sym.terminalNames[token.sym],
                token.left,
                token.right);

        if (token.value != null) {
            formatted += String.format(", value = \"%s\"", token.value);
        }

        System.out.println(formatted);
    }

    private static void showUsage(PrintStream out) {
        out.println("Usage: spl [options] <input file> <output file>");
        out.println();
        out.println("Executes all compiler phases up to (and including) the specified one.");
        out.println("If no flag is specified, all phases are run and code is written to the output file.");
        out.println("Options:");
        out.println("  --tokens     Phase 1: Scans for tokens and prints them.");
        out.println("  --parse      Phase 2: Parses the stream of tokens to check for syntax errors.");
        out.println("  --absyn      Phase 3: Creates an abstract syntax tree from the input tokens and prints it.");
        out.println("  --tables     Phase 4a: Builds a symbol table and prints its entries.");
        out.println("  --semant     Phase 4b: Performs the semantic analysis.");
        out.println("  --vars       Phase 5: Allocates memory space for variables and prints the amount of allocated memory.");
        out.println("  --help       Show this help.");
    }

    private static void usageError(String format, Object... args) {
        System.err.printf("Usage: " + format, args);
        System.err.println();
        showUsage(System.err);
        exit(1);
    }

    public static void main(String[] args) {
        boolean optionTokens = false;
        boolean optionParse = false;
        boolean optionAbsyn = false;
        boolean optionTables = false;
        boolean optionSemant = false;
        boolean optionVars = false;
        String inFilename = "";
        String outFilename = "";

        for (String name : args) {
            switch (name) {
                case "--tokens":
                    optionTokens = true;
                    break;
                case "--parse":
                    optionParse = true;
                    break;
                case "--absyn":
                    optionAbsyn = true;
                    break;
                case "--tables":
                    optionTables = true;
                    break;
                case "--semant":
                    optionSemant = true;
                    break;
                case "--vars":
                    optionVars = true;
                    break;
                case "--help":
                    showUsage(System.out);
                    exit(0);
                default:
                    if (!name.startsWith("--")) {
                        if (inFilename.isEmpty()) inFilename = name;
                        else if (outFilename.isEmpty()) outFilename = name;
                        else usageError("Only one output file is allowed!");
                    } else {
                        usageError("Unknown option '%s'!", name);
                    }
            }
        }

        // final check if filenames present
        if (inFilename.isEmpty()) usageError("No input file!");
        // Only display usage if compiler is expected to run the code-generation phase
        if (outFilename.isEmpty() &&
                !(optionTokens || optionParse || optionAbsyn || optionTables || optionSemant || optionVars))
            usageError("No output file!");

        try (FileReader input = new FileReader(inFilename)) {
            Scanner scanner = new Scanner(input);

            if (optionTokens) {
                Symbol token;
                do {
                    token = scanner.next_token();
                    showToken(token);
                }
                while (token.sym != Sym.EOF);
                exit(0);
            }

            //Parse errors are caught by the below exception handler
            Program program = (Program) new Parser(scanner, symbolFactory).parse().value;

            if (optionParse) {
                System.out.println("Input parsed successfully!");
                exit(0);
            }

            if (optionAbsyn) {
                System.out.println(program);
                exit(0);
            }

            final var table = new TableBuilder(optionTables).buildSymbolTable(program);
            if (optionTables) exit(0);

            new ProcedureBodyChecker().procedureCheck(program, table);
            if (optionSemant) {
                System.out.println("No semantic errors found!");
                exit(0);
            }

            new VarAllocator(optionVars).allocVars(program, table);
            if (optionVars) exit(0);

            try (PrintWriter out = new PrintWriter(new FileWriter(outFilename))) {
                new CodeGenerator(out).generateCode(program, table);
            }

        } catch (SplError error) {
            System.err.println("An error occurred:");
            System.err.printf("Line %d, Column %d: %s\n", error.position.line, error.position.column, error.getMessage());
            exit(error.errorCode);
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
    }
}
