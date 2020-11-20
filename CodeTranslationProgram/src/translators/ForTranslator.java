
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Array;
import translateUnits.Line;
import translateUnits.Variable;

import java.util.ArrayList;

public class ForTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public ForTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (this.line.component(3).equalsIgnoreCase("FROM") && this.line.contain("TO")) {
        	// FOR i FROM x TO y
            String numVar = this.line.component(2);
            String startIdx = this.line.component(4, this.line.componentOf("TO") - 1);
            String endIdx = this.line.component(this.line.componentOf("TO") + 1, -1);
            Variable num = new Variable(numVar, "Number", startIdx, false);
            if (num.isValidName()) {
                if (TranslateSystem.valueJudge(startIdx, "Number") == 1 && TranslateSystem.valueJudge(endIdx, "Number") == 1) {
                    startIdx = TranslateSystem.expressionConverter(startIdx, language);
                    endIdx = TranslateSystem.expressionConverter(endIdx, language);
                    if (!startIdx.equals(Line.ERROR_MSG) && !endIdx.equals(Line.ERROR_MSG)) {
                        Variable v = new Variable(numVar, "Number", startIdx, false, "int");
                        TranslateSystem.variables.add(v);
                        TranslateSystem.forVariablesIndex.push(TranslateSystem.variables.size() - 1);

                        TranslateSystem.forCounter ++;
                        TranslateSystem.uniqueId ++;
                        super.translateResult = "for (int " + numVar + " = " + startIdx + "; " + numVar +" <= " + endIdx + "; " + numVar + "++) {";
                        super.translate();

                    } else {
                        super.reportError("Index in for statement illegal");
                    }

                } else {
                    super.reportError("Index can be only the integer");
                }

            } else {
                super.reportError("Index variable name is illegal or it has been defined");
            }

        } else if (this.line.startsWith("FOR EACH")) {
            // Special cases for loop.
        	// E.g. FOR EACH WEEK OF A YEAR
            if (this.line.component(4).equalsIgnoreCase("OF") && this.line.component(5).equalsIgnoreCase("A") && this.line.component(7).equals(Line.ERROR_MSG)) {
                // Time cases.
                String formerUnit = this.line.component(3);
                String laterUnit = this.line.component(6);

                System.out.println("formerUnit = " + formerUnit + "; laterUnit = " + laterUnit);

                int convertNum = 0;
                switch (formerUnit.toUpperCase()) {
                    case "YEAR":
                        if (laterUnit.toUpperCase().equals("YEAR")) convertNum = 1;
                        break;
                    case "MONTH":
                        if (laterUnit.toUpperCase().equals("YEAR")) convertNum = 12;
                        if (laterUnit.toUpperCase().equals("MONTH")) convertNum = 1;
                        break;
                    case "WEEK":
                        if (laterUnit.toUpperCase().equals("YEAR")) convertNum = 52;
                        if (laterUnit.toUpperCase().equals("MONTH")) convertNum = 4;
                        if (laterUnit.toUpperCase().equals("WEEK")) convertNum = 1;
                        break;
                    case "DAY":
                        if (laterUnit.toUpperCase().equals("YEAR")) convertNum = 365;
                        if (laterUnit.toUpperCase().equals("MONTH")) convertNum = 30;
                        if (laterUnit.toUpperCase().equals("APRIL") || laterUnit.toUpperCase().equals("JUNE")
                                || laterUnit.toUpperCase().equals("SEPTEMBER") || laterUnit.toUpperCase().equals("NOVEMBER"))
                            convertNum = 30;
                        if (laterUnit.toUpperCase().equals("JANUARY") || laterUnit.toUpperCase().equals("MARCH") || laterUnit.toUpperCase().equals("MAY")
                                || laterUnit.toUpperCase().equals("JULY") || laterUnit.toUpperCase().equals("AUGUST") || laterUnit.toUpperCase().equals("OCTOBER")
                                || laterUnit.toUpperCase().equals("DECEMBER")) convertNum = 31;
                        if (laterUnit.toUpperCase().equals("FEBRUARY")) convertNum = 28;
                        if (laterUnit.toUpperCase().equals("WEEK")) convertNum = 7;
                        if (laterUnit.toUpperCase().equals("DAY")) convertNum = 1;
                        break;
                    case "HOUR":
                        if (laterUnit.toUpperCase().equals("YEAR")) convertNum = 365 * 24;
                        if (laterUnit.toUpperCase().equals("MONTH")) convertNum = 30 * 24;
                        if (laterUnit.toUpperCase().equals("WEEK")) convertNum = 7 * 24;
                        if (laterUnit.toUpperCase().equals("DAY")) convertNum = 24;
                        if (laterUnit.toUpperCase().equals("HOUR")) convertNum = 1;
                        break;
                    case "MINUTE":
                        if (laterUnit.toUpperCase().equals("YEAR")) convertNum = 365 * 24 * 60;
                        if (laterUnit.toUpperCase().equals("MONTH")) convertNum = 30 * 24 * 60;
                        if (laterUnit.toUpperCase().equals("WEEK")) convertNum = 7 * 24 * 60;
                        if (laterUnit.toUpperCase().equals("DAY")) convertNum = 24 * 60;
                        if (laterUnit.toUpperCase().equals("HOUR")) convertNum = 60;
                        if (laterUnit.toUpperCase().equals("MINUTE")) convertNum = 1;
                        break;
                    case "SECOND":
                        if (laterUnit.toUpperCase().equals("YEAR")) convertNum = 365 * 24 * 60 * 60;
                        if (laterUnit.toUpperCase().equals("MONTH")) convertNum = 30 * 24 * 60 * 60;
                        if (laterUnit.toUpperCase().equals("WEEK")) convertNum = 7 * 24 * 60 * 60;
                        if (laterUnit.toUpperCase().equals("DAY")) convertNum = 24 * 60 * 60;
                        if (laterUnit.toUpperCase().equals("HOUR")) convertNum = 60 * 60;
                        if (laterUnit.toUpperCase().equals("MINUTE")) convertNum = 60;
                        if (laterUnit.toUpperCase().equals("SECOND")) convertNum = 1;
                        break;
                }

                if (convertNum == 0) {
                    super.reportError("Invalid expression of for each statement");
                } else {
                    TranslateSystem.forCounter++;
                    TranslateSystem.uniqueId++;
                    TranslateSystem.forVariablesIndex.push(-1);
                    String index = "times" + TranslateSystem.uniqueId;
                    super.translateResult = "for (int " + index + " = 1; " + index + " <= " + convertNum + "; " + index + "++) {";
                    super.translate();

                }

            } else if (this.line.startsWith("FOR EACH ELEMENT OF") || this.line.startsWith("FOR EACH ELEMENT OF ARRAY")) {
                // go through the array.
                String arrayName = this.line.component(-1);
                boolean findArray = false;
                for (Array a : TranslateSystem.arrays) {
                    if (a.getName().equalsIgnoreCase(arrayName)) {
                        findArray = true;
                        if (a.is2DArray()) {
                            super.reportError("'For each element of' statement cannot used in 2D array");
                        } else {
                            TranslateSystem.forCounter++;
                            TranslateSystem.uniqueId++;
                            TranslateSystem.forVariablesIndex.push(-1);
                            String index = "arrayIndex" + TranslateSystem.uniqueId;
                            if (language.equals("Java")) {
                                super.translateResult = "for (int " + index + " = 0; " + index + " < " + a.getName() + ".length; " + index + "++) {";
                            } else {
                                super.translateResult = "for (int " + index + " = 0; " + index + " < sizeof(" + a.getName() + "); " + index + "++) {";
                            }
                                super.translate();

                        }
                        break;
                    }
                }
                if (!findArray) {
                    super.reportError("Array has not been defined");
                }
            } else if (this.line.startsWith("FOR EACH CHARACTER OF") || this.line.startsWith("FOR EACH CHARACTER OF STRING")) {
                // go through string.
                String stringName = this.line.component(-1);
                boolean findString = false;
                for (Variable v : TranslateSystem.variables) {
                    if (v.getName().equalsIgnoreCase(stringName) && v.getDataType().equalsIgnoreCase("String")) {
                        findString = true;
                        TranslateSystem.forCounter++;
                        TranslateSystem.uniqueId++;
                        TranslateSystem.forVariablesIndex.push(-1);
                        String index = "stringIndex" + TranslateSystem.uniqueId;
                        if (language.equals("Java")) {
                            super.translateResult = "for (int " + index + " = 0; " + index + " < " + v.getName() + ".length(); " + index + "++) {";
                        } else {
                            super.translateResult = "for (int " + index + " = 0; " + index + " < sizeof(" + v.getName() + "); " + index + "++) {";
                        }
                        super.translate();

                        break;
                    }
                }
                if (!findString) {
                    super.reportError("String has not been defined");
                }

            } else {
                super.reportError("Unknown or mistake expression for each statement format");
            }

        } else {
            super.reportError("Unknown or illegal for statement format");
        }
    }

}
