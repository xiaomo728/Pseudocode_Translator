
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Array;
import translateUnits.Function;
import translateUnits.Line;
import translateUnits.Variable;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ExecuteTranslator extends Translator implements TranslatorInterface {
    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public ExecuteTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (!this.line.contain("AS")) {
            // EXECUTE functionName(parameter)
            String orgSign = this.line.component(2, -1).trim();
            if (orgSign.endsWith(")")) {
                String funcName = orgSign.split("\\(")[0];
                System.out.println("Function name:" + funcName);
                String funcParam = orgSign.split("\\(")[1].substring(0, orgSign.split("\\(")[1].length() - 1);

                boolean isTranslated = false;
                for (Function f: TranslateSystem.functions) {
                    if (f.getName().equalsIgnoreCase(funcName)) {
                        if (f.hasParameter()) {
                            if (funcParam.equals("")) {
                                super.reportError("Function parameters are not consistent with its definition");
                                return;
                            }

                            String params[] = funcParam.split(",");
                            if (f.numOfParameter() == params.length) {
                                for (int i = 0; i < params.length; i++) {
                                    String param = TranslateSystem.expressionConverter(params[i].trim(), language);
                                    if (param.equals(Line.ERROR_MSG)) {
                                        super.reportError("Function parameters has error syntax");
                                        return;
                                    } else {
                                        params[i] = param;
                                    }
                                }

                                super.translateResult = f.getName() + "(";
                                for (int i = 0; i < params.length; i++) {
                                    super.translateResult += params[i];
                                    if (i != params.length - 1) {
                                        super.translateResult += ", ";
                                    }
                                }
                                super.translateResult += ");";
                                super.translate();
                                isTranslated = true;

                            } else {
                                super.reportError("Function parameters are not consistent with its definition");
                                return;
                            }

                        } else {
                            // no parameters.
                            if (orgSign.equalsIgnoreCase(f.getName() + "()")) {
                                super.translateResult = f.getName() + "();";
                                super.translate();
                                isTranslated = true;
                            } else {
                                super.reportError("Function parameters are not consistent with its definition");
                                return;
                            }
                        }
                    }
                }

                if (!isTranslated) {
                    super.reportError("Function may not be defined first");
                }

            } else {
                super.reportError("Function signature expression error");
            }

        } else {
            // EXECUTE functionName(parameter) AS variable
            String orgSign = this.line.component(2, this.line.componentOf("AS") - 1).trim();
            String sign = "";
            String retValue = "";
            if (orgSign.endsWith(")")) {
                String funcName = orgSign.split("\\(")[0];
                System.out.println("Function name:" + funcName);
                String funcParam = orgSign.split("\\(")[1].substring(0, orgSign.split("\\(")[1].length() - 1);

                boolean isTranslated = false;
                for (Function f: TranslateSystem.functions) {
                    if (f.getName().equalsIgnoreCase(funcName)) {
                        if (f.getReturnValue().equals("")) {
                            super.reportError("Cannot get a value from a void function");
                        } else {
                            retValue = f.getReturnValue();
                            if (f.hasParameter()) {
                                if (funcParam.equals("")) {
                                    super.reportError("Function parameters are not consistent with its definition");
                                    return;
                                }

                                String params[] = funcParam.split(",");
                                if (f.numOfParameter() == params.length) {
                                    for (int i = 0; i < params.length; i++) {
                                        String param = TranslateSystem.expressionConverter(params[i].trim(), language);
                                        if (param.equals(Line.ERROR_MSG)) {
                                            super.reportError("Function parameters has error syntax");
                                            return;
                                        } else {
                                            params[i] = param;
                                        }
                                    }

                                    sign = f.getName() + "(";
                                    for (int i = 0; i < params.length; i++) {
                                        sign += params[i];
                                        if (i != params.length - 1) {
                                            sign += ", ";
                                        }
                                    }
                                    sign += ")";
                                    isTranslated = true;
                                } else {
                                    super.reportError("Function parameters are not consistent with its definition");
                                    return;
                                }

                            } else {
                                // no parameters.
                                if (orgSign.equalsIgnoreCase(f.getName() + "()")) {
                                    sign = f.getName() + "()";
                                    isTranslated = true;
                                } else {
                                    super.reportError("Function parameters are not consistent with its definition");
                                    return;
                                }
                            }
                        }
                    }
                }

                if (!isTranslated) {
                    super.reportError("Function may not be defined first");
                } else {
                    String trueRetVar = "";
                    System.out.println(">>> retValue = " + retValue);
                    switch (retValue.toUpperCase()) {
                        case "NUMBER":
                            if (TranslateSystem.intMode) {
                                trueRetVar = "int";
                            } else {
                                trueRetVar = "double";
                            }
                            break;
                        case "STRING":
                            if (language.equals("Java")) {
                                trueRetVar = "String";
                            } else {
                                trueRetVar = "char *";
                            }
                            break;
                        case "BOOLEAN":
                            if (language.equals("Java")) {
                                trueRetVar = "boolean";
                            } else {
                                trueRetVar = "int";
                            }
                            break;
                        case "NUMBER ARRAY":
                            if (language.equals("Java")) {
                                if (TranslateSystem.intMode) {
                                    trueRetVar = "int[]";
                                } else {
                                    trueRetVar = "double[]";
                                }
                            } else {
                                if (TranslateSystem.intMode) {
                                    trueRetVar = "int *";
                                } else {
                                    trueRetVar = "double *";
                                }
                            }
                            break;
                        case "STRING ARRAY":
                            if (language.equals("Java")) {
                                trueRetVar = "String[]";
                            } else {
                                trueRetVar = "char **";
                            }
                            break;
                        case "BOOLEAN ARRAY":
                            if (language.equals("Java")) {
                                trueRetVar = "boolean[]";
                            } else {
                                trueRetVar = "int *";
                            }
                            break;
                        case "": trueRetVar = "void"; break;
                    }

                    String variable1 = this.line.component(-1).trim();
                    String variable2 = this.line.component(this.line.componentOf("AS") + 1).trim();
                    if (variable1.equals(variable2)) {
                        if (retValue.toUpperCase().contains("ARRAY")) {
                            for (Array a: TranslateSystem.arrays) {
                                if (a.getName().equalsIgnoreCase(variable1)) {
                                    if (a.getDataType().equalsIgnoreCase(retValue.toUpperCase().replace(" ARRAY", " ").trim())) {
                                        super.translateResult = a.getName() + " = " + sign + ";";
                                        super.translate();
                                    } else {
                                        super.reportError("Value's type is not consistent with function's return value type");
                                    }
                                    return;
                                }
                            }

                        } else {
                            for (Variable v: TranslateSystem.variables) {
                                if (v.getName().equalsIgnoreCase(variable1)) {
                                    if (v.getDataType().equalsIgnoreCase(retValue.trim())) {
                                        super.translateResult = v.getName() + " = " + sign + ";";
                                        super.translate();
                                    } else {
                                        super.reportError("Value's type is not consistent with function's return value type");
                                    }
                                    return;
                                }
                            }

                        }

                        // Not define the variable at first, define it.
                        if (retValue.toUpperCase().contains("ARRAY")) {
                            Array a = new Array(variable1, retValue.toUpperCase().replace(" ARRAY", "").trim(), "1,1");
                            a.setTrueDataType(a.determineTrueDataType());
                            if (a.isValidName() && a.isValidDataType() && !a.getTrueDataType().equals("ERROR")) {
                                TranslateSystem.arrays.add(new Array(variable1, a.getDataType(), a.getLength(), new LinkedHashMap<String, String>(), a.getTrueDataType()));
                            } else if (!a.isValidName()){
                                super.reportError("As array's name is illegal or it has been defined");
                                return;
                            } else if (!a.isValidDataType()) {
                                super.reportError("As array's data type is illegal");
                                return;
                            }

                        } else {

                            Variable v = new Variable(variable1, retValue, "", false);
                            if (v.isValidName() && v.isValidDataType() && !v.getTrueDataType().equals("ERROR")) {
                                TranslateSystem.variables.add(new Variable(variable1, v.getDataType(), v.getValue(), false, v.getTrueDataType()));
                            } else if (!v.isValidName()){
                                super.reportError("As variable's name is illegal or it has been defined");
                                return;
                            } else if (!v.isValidDataType()) {
                                super.reportError("As variable's data type is illegal");
                                return;
                            } else if (v.getTrueDataType().equals("ERROR")) {
                                super.reportError("As variable's data type is contradict with its value");
                                return;
                            }
                        }

                        super.translateResult = trueRetVar + " " + variable1 + ";" + "\r\n"
                                + this.line.indentation() + variable1 + " = " + sign + ";";
                        super.translate();

                    } else {
                        super.reportError("Syntax error on execute as statement");
                    }
                }

            } else {
                super.reportError("Function signature expression error");
            }

        }
    }

}
