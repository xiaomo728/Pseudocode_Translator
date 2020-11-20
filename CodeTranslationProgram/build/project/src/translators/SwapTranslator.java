
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Array;
import translateUnits.Line;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class SwapTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public SwapTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (this.line.component(2, -1).contains(",")) {
            String[] elements = this.line.component(2, -1).trim().split(",");
            String e1 = elements[0].trim();
            String e2 = elements[1].trim();
            if (e1.contains("[") && e1.endsWith("]") && e2.contains("[") && e2.endsWith("]")) {
                String array1 = e1.substring(0, e1.indexOf("["));
                String array2 = e2.substring(0, e2.indexOf("["));
                if (array1.equalsIgnoreCase(array2)) {
                    for (Array a: TranslateSystem.arrays) {
                        if (a.getName().equalsIgnoreCase(array1)) {
                            if ((a.is2DArray() && e1.contains("][") && e2.contains("][")) || (!a.is2DArray() && !e1.contains("][") && !e2.contains("]["))) {
                                String a1 = TranslateSystem.expressionConverter(e1, language);
                                String a2 = TranslateSystem.expressionConverter(e2, language);
                                if (!a1.equals(Line.ERROR_MSG) && !a2.equals(Line.ERROR_MSG)) {
                                    super.translateResult = a.getTrueDataType() + " temporary = " + a1 + ";" + "\r\n"
                                            + this.line.indentation() + a1 + " = " + a2 + ";"                + "\r\n"
                                            + this.line.indentation() + a2 + " = temporary;";
                                    super.translate();

                                } else {
                                    super.reportError("Array expression has error");
                                }
                            } else {
                                super.reportError("Array expression is collision with its type");
                            }
                            break;
                        }
                    }
                } else {
                    super.reportError("Swap must be operated in one array");
                }
            } else {
                super.reportError("Swap must be operated on array elements");
            }

        } else {
            super.reportError("Syntax error on swap statement");
        }

    }

}
