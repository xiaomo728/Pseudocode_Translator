
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Line;

import java.util.ArrayList;

public class CaseTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public CaseTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (this.line.component(-1).equalsIgnoreCase("OF")) {
            String expression = this.line.component(2, this.line.componentOf("OF") - 1).trim();
            // expression should be number or string.
            String expType = "";
            if (TranslateSystem.valueJudge(expression, "Number") == 1) {
                expType = "Integer";
                TranslateSystem.caseType.push(expType);
            } else if (TranslateSystem.valueJudge(expression, "String") == 1) {
                expType = "String";
                TranslateSystem.caseType.push(expType);
            } else {
                super.reportError("Cannot use case statement to handle such datatype without integer or string");
            }

            if (!expType.equals("")) {
                if (!TranslateSystem.expressionConverter(expression, language).equals(Line.ERROR_MSG)) {
                    TranslateSystem.caseCounter ++;
                    TranslateSystem.lastLineIsCaseOf = true;
                    super.translateResult = "switch (" + TranslateSystem.expressionConverter(expression, language) + ") {";
                    super.translate();

                } else {
                    super.reportError("Expression in case invalid");
                }
            }

        } else {
            // enter by default "constant_var:"
            // must be a constant word in the expression.
            if (this.line.getPureContent().charAt(this.line.getPureContent().length() - 1) == ':' && TranslateSystem.caseCounter > 0
                    && !this.line.getPureContent().replace(" ", "").equalsIgnoreCase("OTHERS:")) {
                String type = TranslateSystem.caseType.peek();
                String value = this.line.getPureContent().substring(0, this.line.getPureContent().length() - 1);

                if (!TranslateSystem.lastLineIsCaseOf) {
                    super.translateResult = "break;\r\n";
                } else {
                    super.translateResult = "";
                }

                if (type.equals("Integer")) {
                    if (TranslateSystem.valueJudge(value, "Number") == 1 && !TranslateSystem.expressionConverter(value, language).equals(Line.ERROR_MSG)) {
                        if (!TranslateSystem.lastLineIsCaseOf) super.translateResult += this.line.indentation();
                        super.translateResult += "case " + TranslateSystem.expressionConverter(value, language) + ":";
                        super.translate();

                    } else {
                        super.reportError("Case value is illegal");
                    }

                } else if (type.equals("String")) {
                    if (TranslateSystem.valueJudge(value, "String") == 1 && !TranslateSystem.expressionConverter(value, language).equals(Line.ERROR_MSG)) {
                        if (!TranslateSystem.lastLineIsCaseOf) super.translateResult += this.line.indentation();
                        super.translateResult += "case " + TranslateSystem.expressionConverter(value, language) + ":";
                        super.translate();

                    } else {
                        super.reportError("Case value is illegal");
                    }

                } else {
                    super.reportError("Such data type cannot be used in case statement");
                }

            } else if (this.line.getPureContent().replace(" ", "").equalsIgnoreCase("OTHERS:") && TranslateSystem.caseCounter > 0) {
                if (!TranslateSystem.lastLineIsCaseOf) {
                    super.translateResult = "break;\r\n";
                } else {
                    super.translateResult = "";
                }

                if (!TranslateSystem.lastLineIsCaseOf) super.translateResult += this.line.indentation();
                super.translateResult += "default:";
                super.translate();

            } else {
                super.reportError("Illegal expression format");
            }
        }
    }

}
