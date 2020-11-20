
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Array;
import translateUnits.Line;
import translateUnits.Variable;

import java.util.ArrayList;

public class ByTranslator extends Translator implements TranslatorInterface {
    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public ByTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        String number = this.line.component(4);
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException e) {
            super.reportError("Cannot find the appropriate number that to increment/decrement");
            return;
        }
        String variable = this.line.component(1);

        boolean hasVariable = false;
        for (Variable v: TranslateSystem.variables) {
            if (v.getName().equals(variable) && v.getDataType().equalsIgnoreCase("Number")) {
                if (!v.isConstant()) {
                    switch (this.line.component(2).toUpperCase().trim()) {
                        case "INCREMENT":
                            if (number.equals("1")) {
                                super.translateResult = variable + " ++;";
                            } else {
                                super.translateResult = variable + " += " + number + ";";
                            }
                            super.translate();
                            hasVariable = true;
                            break;

                        case "DECREMENT":
                            if (number.equals("1")) {
                                super.translateResult = variable + " --;";
                            } else {
                                super.translateResult = variable + " -= " + number + ";";
                            }
                            super.translate();
                            hasVariable = true;
                            break;

                        case "MULTI_INCREMENT":
                            super.translateResult = variable + " *= " + number + ";";
                            super.translate();
                            hasVariable = true;
                            break;

                        case "DIV_DECREMENT":
                            super.translateResult = variable + " /= " + number + ";";
                            super.translate();
                            hasVariable = true;
                            break;

                        default:
                            super.reportError("Syntax error on increment/decrement statement");
                    }

                } else {
                    super.reportError("Constant's value cannot be changed");
                    return;
                }
                break;

            }
        }

        if (!hasVariable) {
            for (Array a: TranslateSystem.arrays) {
                if (variable.startsWith(a.getName() + "[") && variable.endsWith("]")) {
                    if (TranslateSystem.expressionConverter(variable, language).equals(Line.ERROR_MSG)) {
                        super.reportError("Array variable with invalid format");
                        return;
                    } else {
                        if (a.getDataType().equalsIgnoreCase("Number")) {
                            switch (this.line.component(2).toUpperCase().trim()) {
                                case "INCREMENT":
                                    if (number.equals("1")) {
                                        super.translateResult = variable + " ++;";
                                    } else {
                                        super.translateResult = variable + " += " + number + ";";
                                    }
                                    super.translate();
                                    hasVariable = true;
                                    break;

                                case "DECREMENT":
                                    if (number.equals("1")) {
                                        super.translateResult = variable + " --;";
                                    } else {
                                        super.translateResult = variable + " -= " + number + ";";
                                    }
                                    super.translate();
                                    hasVariable = true;
                                    break;

                                case "MULTI_INCREMENT":
                                    super.translateResult = variable + " *= " + number + ";";
                                    super.translate();
                                    hasVariable = true;
                                    break;

                                case "DIV_DECREMENT":
                                    super.translateResult = variable + " /= " + number + ";";
                                    super.translate();
                                    hasVariable = true;
                                    break;

                                default:
                                    super.reportError("Syntax error on increment/decrement statement");
                            }

                        } else {
                            super.reportError("Variable is not a number by using increment/decrement");
                        }
                    }
                    break;
                }
            }
        }

        if (!hasVariable) {
            super.reportError("Variable has not be defined or it is not a number by using increment/decrement");
        }
    }

}
