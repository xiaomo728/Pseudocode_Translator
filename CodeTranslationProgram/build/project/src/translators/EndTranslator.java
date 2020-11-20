
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Line;

import java.util.ArrayList;

public class EndTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public EndTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        String lineType = this.line.getPureContent().toUpperCase().trim();
        boolean validEnd = true;
        switch (lineType) {
            case "END IF":
                if (TranslateSystem.ifCounter > 0) {
                    TranslateSystem.ifCounter --;
                } else {
                    super.reportError("End if condition without if statement");
                    validEnd = false;
                }
                break;

            case "END CASE":
                if (TranslateSystem.caseCounter > 0) {
                    TranslateSystem.caseCounter --;
                    TranslateSystem.caseType.pop();
                } else {
                    super.reportError("End case condition without case statement");
                    validEnd = false;
                }
                break;

            case "END REPEAT":
                if (TranslateSystem.repeatCounter > 0) {
                    TranslateSystem.repeatCounter --;
                    TranslateSystem.uniqueId --; // uniqueId - 1
                    if (TranslateSystem.repeatLine.peek() != -1) {
                        super.reportError("End repeat condition without when and until keywords");
                        validEnd = false;
                    } else {
                        TranslateSystem.repeatLine.pop();
                    }
                } else {
                    super.reportError("End repeat condition without repeat statement");
                    validEnd = false;
                }
                break;

            case "END WHILE":
                if (TranslateSystem.whileCounter > 0) {
                    TranslateSystem.whileCounter --;
                } else {
                    super.reportError("End while condition without while statement");
                    validEnd = false;
                }
                break;

            case "END FOR":
                if (TranslateSystem.forCounter > 0) {
                    TranslateSystem.forCounter --;
                    TranslateSystem.uniqueId --; // UniqueId -1
                    int index = TranslateSystem.forVariablesIndex.pop();
                    if (index != -1) {
                        TranslateSystem.variables.get(index).setUnavailable();
                    }
                } else {
                    super.reportError("End for condition without for statement");
                    validEnd = false;
                }
                break;

            case "END WHENEVER":
                if (TranslateSystem.wheneverCounter > 0) {
                    TranslateSystem.wheneverCounter --;
                } else {
                    super.reportError("End whenever condition without whenever statement");
                    validEnd = false;
                }
                break;

            default:
                super.reportError("Illegal END expression");
                validEnd = false;
        }

        if (validEnd) {
            super.translateResult = "}";
            super.translate();
        }

    }

}
