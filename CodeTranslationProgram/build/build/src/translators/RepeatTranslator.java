
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Line;

import java.util.ArrayList;

public class RepeatTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public RepeatTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (this.line.getPureContent().equalsIgnoreCase("REPEAT")) {
        	// REPEAT ...
            int index = this.lineNum;
            TranslateSystem.repeatCounter++;
            TranslateSystem.uniqueId ++;
            TranslateSystem.repeatLine.push(index);
            if (language.equals("Java")) {
                super.translateResult = "while (true) {";
            } else {
                super.translateResult = "while (1) {";
            }
            super.translate();

        } else if (this.line.component(2).equalsIgnoreCase("WHEN")) {
        	// REPEAT WHEN ...
            String condition = this.line.component(3, -1);
            String tranCondition = TranslateSystem.expressionConverter(condition, language);
            if (TranslateSystem.isConditionExpression(condition) && !tranCondition.equals(Line.ERROR_MSG)) {
                TranslateSystem.repeatCounter++;
                TranslateSystem.uniqueId ++;
                TranslateSystem.repeatLine.push(-1);
                super.translateResult = "while (" + tranCondition + ") {";
                super.translate();

            } else {
                super.reportError("Syntax error of repeat loop expression");
            }

        } else if (this.line.component(2).equalsIgnoreCase("UNTIL")) {
        	// REPEAT UNTIL ...
            String condition = this.line.component(3, -1);
            if (condition.toUpperCase().trim().startsWith("LOOP RUN") && (condition.toUpperCase().trim().endsWith("TIMES") || condition.toUpperCase().trim().endsWith("TIME"))
                    && this.line.component(7).equals(Line.ERROR_MSG)) {
            	// UNTIL LOOP RUN x TIMES
                String number = this.line.component(this.line.componentOf("RUN") + 1);
                try {
                    Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    super.reportError("Cannot find suitable integer number at statement loop run [number] times");
                    return;
                }
                TranslateSystem.repeatCounter++;
                TranslateSystem.uniqueId ++;
                TranslateSystem.repeatLine.push(-1);
                String index = "loopTime" + TranslateSystem.uniqueId;
                super.translateResult = "for (int " + index + " = 0; " + index + " < " + number + "; " + index + "++) {";
                super.translate();

            } else {
            	// UNTIL expression
                String tranCondition = TranslateSystem.expressionConverter(condition, language);
                if (TranslateSystem.isConditionExpression(condition) && !tranCondition.equals(Line.ERROR_MSG)) {
                    TranslateSystem.repeatCounter++;
                    TranslateSystem.uniqueId ++;
                    TranslateSystem.repeatLine.push(-1);
                    if (language.equals("Java")) {
                        super.translateResult =             "while (true) {"                   + "\r\n"
                                + this.line.indentation() + "    if (" + tranCondition + ") {" + "\r\n"
                                + this.line.indentation() + "        break;"                   + "\r\n"
                                + this.line.indentation() + "    }";
                    } else {
                        super.translateResult =             "while (1) {"                      + "\r\n"
                                + this.line.indentation() + "    if (" + tranCondition + ") {" + "\r\n"
                                + this.line.indentation() + "        break;"                   + "\r\n"
                                + this.line.indentation() + "    }";
                    }
                    super.translate();

                } else {
                    super.reportError("Syntax error of repeat loop expression");
                }
            }

        } else if (this.line.component(1).equalsIgnoreCase("UNTIL")) {
        	// UNTIL ... in REPEAT structure.
            String condition = this.line.component(2, -1);
            String tranCondition = TranslateSystem.expressionConverter(condition, language);
            if (TranslateSystem.isConditionExpression(condition) && !tranCondition.equals(Line.ERROR_MSG)) {
                if (TranslateSystem.repeatLine.peek() != -1) {
                    super.translateResult =             "    if (" + tranCondition + ") {" + "\r\n"
                            + this.line.indentation() + "        break;"                   + "\r\n"
                            + this.line.indentation() + "    }";
                    super.translate();

                    TranslateSystem.repeatLine.pop();
                    TranslateSystem.repeatLine.push(-1);

                } else {
                    super.reportError("Redundant until in repeat loop");
                }

            } else {
                super.reportError("Syntax error of repeat loop expression");
            }

        } else {
            super.reportError("Syntax error of repeat and you should only write on repeat/repeat when/repeat until");
        }
    }

}
