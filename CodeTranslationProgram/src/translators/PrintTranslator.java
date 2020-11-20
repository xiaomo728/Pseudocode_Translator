
package translators;

import translateProgram.TranslateSystem;
import translateUnits.Array;
import translateUnits.Line;
import programInterfaces.TranslatorInterface;

import java.util.ArrayList;

public class PrintTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public PrintTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (!this.line.getPureContent().equalsIgnoreCase("PRINT")) {
            if (this.line.startsWith("PRINT ALL ELEMENTS OF ")) {
                // print array elements.
                String printArrUpper = this.line.getPureContent().toUpperCase().replace("PRINT ALL ELEMENTS OF ", "").trim();
                String printArr = this.line.component(5);
                boolean find = false;
                if (printArr.toUpperCase().equalsIgnoreCase(printArrUpper)) {
                    for (Array a: TranslateSystem.arrays) {
                        if (a.getName().equalsIgnoreCase(printArr)) {
                            find = true;
                            if (language.equals("Java")) {
                                if (a.is2DArray()) {
                                    super.translateResult =             "for (int arrayRowIndex = 0; arrayRowIndex < " + a.getName() + ".length; arrayRowIndex++) {"                             + "\r\n"
                                            + this.line.indentation() + "    for (int arrayColumnIndex = 0; arrayColumnIndex < " + a.getName() + "[arrayRowIndex].length; arrayColumnIndex++) {" + "\r\n"
                                            + this.line.indentation() + "        System.out.print(" + a.getName() + "[arrayRowIndex][arrayColumnIndex] + \" \");"                                + "\r\n"
                                            + this.line.indentation() + "    }"                                                                                                                  + "\r\n"
                                            + this.line.indentation() + "    System.out.println();"                                                                                              + "\r\n"
                                            + this.line.indentation() + "}";

                                } else {
                                    super.translateResult =             "for (int arrayIndex = 0; arrayIndex < " + a.getName() + ".length; arrayIndex++) {" + "\r\n"
                                            + this.line.indentation() + "    System.out.print(" + a.getName() + "[arrayIndex] + \" \");"                    + "\r\n"
                                            + this.line.indentation() + "}"                                                                                 + "\r\n"
                                            + this.line.indentation() + "System.out.println();";
                                }
                            } else {
                                String t = "";
                                if (a.getTrueDataType().equalsIgnoreCase("String")) {
                                    t = "s";
                                } else if (a.getTrueDataType().equalsIgnoreCase("double")) {
                                    t = "f";
                                } else {
                                    t = "d";
                                }
                                if (a.is2DArray()) {
                                    super.translateResult =             "for (int arrayRowIndex = 0; arrayRowIndex < " + a.getName() + ".length; arrayRowIndex++) {"                             + "\r\n"
                                            + this.line.indentation() + "    for (int arrayColumnIndex = 0; arrayColumnIndex < " + a.getName() + "[arrayRowIndex].length; arrayColumnIndex++) {" + "\r\n"
                                            + this.line.indentation() + "        printf(\"%" + t + "\", " + a.getName() + "[arrayRowIndex][arrayColumnIndex] + \" \");"                          + "\r\n"
                                            + this.line.indentation() + "    }"                                                                                                                  + "\r\n"
                                            + this.line.indentation() + "    printf(\"\\n\");"                                                                                                   + "\r\n"
                                            + this.line.indentation() + "}";

                                } else {
                                    super.translateResult =             "for (int arrayIndex = 0; arrayIndex < " + a.getName() + ".length; arrayIndex++) {" + "\r\n"
                                            + this.line.indentation() + "    printf(\"%" + t + "\", " + a.getName() + "[arrayIndex] + \" \");"              + "\r\n"
                                            + this.line.indentation() + "}"                                                                                 + "\r\n"
                                            + this.line.indentation() + "printf(\"\\n\");";
                                }
                            }
                            super.translate();

                            break;
                        }
                    }

                    if (!find) {
                        super.reportError("Cannot find the target array");
                    }

                } else {
                    super.reportError("Error expression in print format");
                }

            } else {
            	// just print.
                String printWords = this.line.getPureContent().substring(this.line.getPureContent().indexOf(" ") + 1);
                if (language.equals("Java")) {
                    super.translateResult = "System.out.println(\"" + printWords + "\");";
                } else {
                    super.translateResult = "printf(\"" + printWords + "\");";
                }
                super.translate();
            }

        } else {
            super.reportError("No words are recognized to be print");
        }
    }

}
