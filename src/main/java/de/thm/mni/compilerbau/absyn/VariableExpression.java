package de.thm.mni.compilerbau.absyn;

import de.thm.mni.compilerbau.absyn.visitor.Visitor;

/**
 * This class represents a variable that is used as an {@link Expression}.
 * Example: 3 * i
 *
 * In this example, the named variable with i as a identifier is used as the right hand side of the
 * arithmetic expression. When using a {@link VariableExpression} the value that a variable holds is requested.
 * The semantic type of a {@link VariableExpression} depends on the type of its {@link Variable}.
 */
public class VariableExpression extends Expression {
    public final Variable variable;

    /**
     * Creates a new node representing the value of a variable.
     * @param position The position of the variable in the source code.
     * @param variable The variable whose value is used as a value for this expression.
     */
    public VariableExpression(Position position, Variable variable) {
        super(position);
        this.variable = variable;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("VariableExpression", variable);
    }
}
