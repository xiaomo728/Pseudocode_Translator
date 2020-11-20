
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Array;
import translateUnits.Line;
import translateUnits.Variable;

import java.util.ArrayList;

public class AssignTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public AssignTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        String values = "", variable = "";
        boolean legalFormat = false;
        if (this.line.contain("TO") && this.line.component(this.line.componentOf("TO") + 2).equals(Line.ERROR_MSG)) {
            values = this.line.component(2, this.line.componentOf("TO") - 1);
            variable = this.line.component(this.line.componentOf("TO") + 1);
            legalFormat = true;

        } else if (this.line.component(2).equals("=")) {
            values = this.line.component(this.line.componentOf("=") + 1, -1);
            variable = this.line.component(1);
            legalFormat = true;

        } else {
            super.reportError("Assignment format error");
        }

        System.out.println(">>>Variable: " + variable);
        System.out.println(">>>Value: " + values);

        if (legalFormat) {
            boolean validVar = false;
            if (!variable.contains(",")) {
                // Only one variable will be assigned.
                System.out.println(">>>Assign value to one variable.");
                String dataType = "";
                int pDataType = 1;
                for (Variable v : TranslateSystem.variables) {
                    if (v.getName().equals(variable)) {
                        if (!v.isConstant()) {
                            dataType = v.getDataType();
                            if (v.getTrueDataType().equalsIgnoreCase("double")) pDataType = 2;
                            validVar = true;
                        } else {
                            super.reportError("Constant's value cannot be changed");
                            return;
                        }
                        break;
                    }
                }

                if (!validVar) {
                    for (Array a : TranslateSystem.arrays) {
                        if (variable.startsWith(a.getName() + "[") && variable.endsWith("]")) {
                            if (TranslateSystem.expressionConverter(variable, language).equals(Line.ERROR_MSG)) {
                                super.reportError("Array variable with invalid format");
                                return;
                            } else {
                                dataType = a.getDataType();
                                if (a.getTrueDataType().equalsIgnoreCase("double")) pDataType = 2;
                                validVar = true;
                            }
                            break;
                        }
                    }
                }

                // check variable end.
                if (validVar) {
                    int vDataType = TranslateSystem.valueJudge(values, dataType);
                    if (vDataType != 0) {
                        String valueResult = TranslateSystem.expressionConverter(values, language);
                        if (!valueResult.equals(Line.ERROR_MSG)) {
                            if (vDataType > pDataType) {
                                super.translateResult = variable + " = (int) (" + valueResult + ");";
                            } else {
                                super.translateResult = variable + " = " + valueResult + ";";
                            }
                            super.translate();

                        } else {
                            super.reportError("Assign a illegal value to the variable");
                        }

                    } else {
                        super.reportError("Assigned value is not consistent variable's data type");
                    }

                } else {
                    super.reportError("Variable may not be defined first");
                }

            } else {
                // Many variables will be assigned.
                System.out.println(">>>Assign value to many variables.");
                ArrayList<String> dataTypes = new ArrayList<String>();
                ArrayList<Integer> pDataTypes = new ArrayList<Integer>();
                String variables[] = variable.split(",");
                int validNum = 0;
                for (String var: variables) {
                    for (Variable v : TranslateSystem.variables) {
                        if (v.getName().equals(var)) {
                            if (!v.isConstant()) {
                                validNum ++;
                                dataTypes.add(new String(v.getDataType()));
                                if (v.getTrueDataType().equalsIgnoreCase("double")) {
                                    pDataTypes.add(2);
                                } else {
                                    pDataTypes.add(1);
                                }
                            } else {
                                super.reportError("Constant's value cannot be changed");
                                return;
                            }
                        }
                    }
                }

                for (String var: variables) {
                    for (Array a : TranslateSystem.arrays) {
                        if (var.startsWith(a.getName() + "[") && var.endsWith("]")) {
                            if (TranslateSystem.expressionConverter(variable, language).equals(Line.ERROR_MSG)) {
                                super.reportError("Array variable with invalid format");
                                return;
                            } else {
                                validNum ++;
                                dataTypes.add(new String(a.getDataType()));
                                if (a.getTrueDataType().equalsIgnoreCase("double")) {
                                    pDataTypes.add(2);
                                } else {
                                    pDataTypes.add(1);
                                }
                            }
                        }
                    }
                }

                if (validNum == variables.length) {
                    validVar = true;
                }

                // check variable end.
                if (validVar) {
                    boolean validTypes = true;
                    int vDataType = 0;
                    for (String d: dataTypes) {
                        vDataType = TranslateSystem.valueJudge(values, d);
                        if (vDataType == 0) {
                            super.reportError("Assigned value is not consistent variable's data type");
                            validTypes = false;
                            break;
                        }
                    }
                    if (validTypes) {
                        String valueResult = TranslateSystem.expressionConverter(values, language);
                        if (!valueResult.equals(Line.ERROR_MSG)) {
                            for (int i = 0; i < variables.length; i++) {
                                if (vDataType > pDataTypes.get(i)) {
                                    super.translateResult = variables[i] + " = (int) (" + valueResult + ");";
                                } else {
                                    super.translateResult = variables[i] + " = " + valueResult + ";";
                                }
                                super.translate();
                            }
                        } else {
                            super.reportError("Assign a illegal value to the variable");
                        }
                    } else {
                        super.reportError("Assigned value is not consistent variable's data type");
                    }

                } else {
                    super.reportError("Variable may not be defined first");
                }
            }
        }

    }

}
