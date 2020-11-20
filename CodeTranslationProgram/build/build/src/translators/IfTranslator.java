
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Line;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class IfTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public IfTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (this.line.startsWith("IF") && this.line.component(-1).equalsIgnoreCase("THEN")) {
            // IF condition THEN

            String conditions = this.line.component(2, this.line.componentOf("THEN") - 1).trim();
            String expression = TranslateSystem.expressionConverter(conditions, language);
            if (!expression.equals(Line.ERROR_MSG) && TranslateSystem.isConditionExpression(expression)) {
                TranslateSystem.ifCounter ++;
                super.translateResult = "if (" + expression + ") {";
                super.translate();
            } else {
                super.reportError("Syntax error in if condition expression");
            }

            // above are the line type with "if"

            // below are the line type with "else"

        } else if (this.line.startsWith("ELSE IF") && this.line.component(-1).equalsIgnoreCase("THEN")) {
            // IF.. ELSE IF condition THEN
            String conditions = this.line.component(3, this.line.componentOf("THEN") - 1).trim();
            String expression = TranslateSystem.expressionConverter(conditions, language);
            if (!expression.equals(Line.ERROR_MSG) && TranslateSystem.isConditionExpression(expression)) {
                super.translateResult = "} else if (" + expression + ") {";
                super.translate();
            } else {
                super.reportError("Syntax error in if condition expression");
            }

        } else if (this.line.getPureContent().equalsIgnoreCase("ELSE")) {
            // IF.. ELSE..
            super.translateResult = "} else {";
            super.translate();

        } else {
            super.reportError("Illegal if-else condition format");
        }

    }

}
