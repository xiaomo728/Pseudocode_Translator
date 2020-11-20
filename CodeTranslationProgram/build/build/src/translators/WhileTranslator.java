
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Line;

import java.util.ArrayList;

public class WhileTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public WhileTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (this.line.component(2).equalsIgnoreCase("WHEN")) {
        	// WHILE WHEN expression
            String condition = this.line.component(3, -1);
            String tranCondition = TranslateSystem.expressionConverter(condition, language);
            if (TranslateSystem.isConditionExpression(condition) && !tranCondition.equals(Line.ERROR_MSG)) {
                TranslateSystem.whileCounter ++;
                super.translateResult = "while (" + tranCondition + ") {";
                super.translate();

            } else {
                super.reportError("Syntax error of while loop expression");
            }

        } else {
        	// WHILE expression
        	// WHEN is not necessary.
            String condition = this.line.component(2, -1);
            String tranCondition = TranslateSystem.expressionConverter(condition, language);
            if (TranslateSystem.isConditionExpression(condition) && !tranCondition.equals(Line.ERROR_MSG)) {
                TranslateSystem.whileCounter ++;
                super.translateResult = "while (" + tranCondition + ") {";
                super.translate();

            } else {
                super.reportError("Syntax error of while loop expression");
            }
        }
    }

}
