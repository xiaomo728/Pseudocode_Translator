
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Line;
import translateUnits.Variable;

import java.util.ArrayList;

public class AppendTranslator extends Translator implements TranslatorInterface {
    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public AppendTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (this.line.contain("TO")) {
            String appends = this.line.component(2, this.line.componentOf("TO") - 1).trim();
            String orgString1 = this.line.component(-1).trim();
            String orgString2 = this.line.component(this.line.componentOf("TO") + 1).trim();
            if (!orgString1.equals(orgString2)) {
                super.reportError("Cannot find the original string in append to statement");
            } else {
                boolean canAppend = false;
                for (Variable v : TranslateSystem.variables) {
                    if (v.getName().equalsIgnoreCase(orgString1)) {
                        if (v.getTrueDataType().equalsIgnoreCase("String")) {
                            if (!v.isConstant()) {
                                canAppend = true;
                            } else {
                                super.reportError("Cannot append on a constant string");
                            }
                        } else {
                            super.reportError("Cannot append with non-string variable");
                        }
                        break;
                    }
                }
                if (canAppend) {
                    if (appends.contains(",")) {
                        // multiple append.
                        String appendStrings[] = appends.split(",");
                        if (language.equals("Java")) {
                            super.translateResult = orgString1 + " += ";
                            for (int i = 0; i < appendStrings.length; i++) {
                                String apd = TranslateSystem.expressionConverter(appendStrings[i].trim(), language);
                                if (TranslateSystem.valueJudge(appendStrings[i].trim(), "String") == 1 && !apd.equals(Line.ERROR_MSG)) {
                                    super.translateResult += apd;
                                    if (i == appendStrings.length - 1) {
                                        translateResult += ";";
                                    } else {
                                        translateResult += " + ";
                                    }
                                } else {
                                    super.reportError("One of the appending string has syntax expression error or it is not a string");
                                    return;
                                }
                            }
                            super.translate();
                        } else {
                            super.translateResult = "\r\n";
                            for (int i = 0; i < appendStrings.length; i++) {
                                String apd = TranslateSystem.expressionConverter(appendStrings[i].trim(), language);
                                if (TranslateSystem.valueJudge(appendStrings[i].trim(), "String") == 1 && !apd.equals(Line.ERROR_MSG)) {
                                    super.translateResult += this.line.indentation() + "strcat(" + orgString1 + ", " + apd + ");" + "\r\n";
                                } else {
                                    super.reportError("One of the appending string has syntax expression error or it is not a string");
                                    return;
                                }
                            }
                        }

                    } else {
                        // single append.
                        String apd = TranslateSystem.expressionConverter(appends.trim(), language);
                        if (TranslateSystem.valueJudge(appends.trim(), "String") == 1 && !apd.equals(Line.ERROR_MSG)) {
                            if (language.equals("Java")) {
                                super.translateResult = orgString1 + " = " + orgString1 + " + " + apd + ";";
                                super.translate();
                            } else {
                                super.translateResult = "strcat(" + orgString1 + ", " + apd + ");";
                            }
                        } else {
                            super.reportError("Appending string has syntax expression error or it is not a string");
                        }
                    }

                } else {
                    // cannot append.
                    super.reportError("Variable that been appended may have not been defined yet");
                }
            }

        } else {
            super.reportError("Syntax error on append to statement");
        }
    }

}
