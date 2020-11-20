
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Function;
import translateUnits.Line;

import java.util.ArrayList;

public class ReturnTranslator extends Translator implements TranslatorInterface {
    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public ReturnTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (this.line.component(2).equalsIgnoreCase("EXECUTE")) {
            // return a function's return value.
            String orgSign = this.line.component(3, -1).trim();
            if (orgSign.endsWith(")")) {
                String funcName = orgSign.split("\\(")[0];
                System.out.println("Function name:" + funcName);
                String funcParam = orgSign.split("\\(")[1].substring(0, orgSign.split("\\(")[1].length() - 1);

                boolean isTranslated = false;
                String sign = "";
                for (Function f: TranslateSystem.functions) {
                    if (f.getName().equalsIgnoreCase(funcName)) {
                        if (f.getReturnValue().equals("")) {
                            super.reportError("Cannot return a value from a void function");
                        } else {
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
                    super.translateResult = "return " + sign + ";";
                    super.translate();
                }

            } else {
                super.reportError("Function signature expression error in return statement");
            }


        } else {
        	System.out.println("return value = " + this.line.component(2, -1));
            String returnValue = TranslateSystem.expressionConverter(this.line.component(2, -1), language);
            if (!returnValue.equals(Line.ERROR_MSG) && TranslateSystem.isConditionExpression(returnValue)) {
                super.translateResult = "return " + returnValue + ";";
                super.translate();
            } else {
                super.reportError("Syntax error in expression of return value");
            }
        }

    }

}
