
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Line;
import translateUnits.Variable;

import java.util.ArrayList;

public class OutputTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public OutputTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (!this.line.getPureContent().equalsIgnoreCase("OUTPUT")) {
            String outputWords = this.line.getPureContent().substring(this.line.getPureContent().indexOf(" ") + 1).replaceAll(" ", "");

            ArrayList<String> type = new ArrayList<String>();
            boolean existVar = false;
            if (outputWords.contains(",")) {
            	// output more one variables.
                String outputs[] = outputWords.split(",");
                int correctIdx = 0;
                int totalIdx = outputs.length;
                for (Variable v: TranslateSystem.variables) {
                    for (String o: outputs) {
                        if (o.equalsIgnoreCase(v.getName())) {
                            type.add(v.getTrueDataType());
                            correctIdx ++;
                        }
                    }
                }
                if (correctIdx == totalIdx) {
                    existVar = true;
                }

                if (existVar) {
                    if (language.equals("Java")) {
                        super.translateResult = "System.out.println(\"" ;
                        for (int i = 0; i < outputs.length; i++) {
                            super.translateResult += outputs[i] + " = \" + " + outputs[i];
                            if (i != outputs.length - 1) {
                                super.translateResult += " + \", ";
                            }
                        }
                    } else {
                        super.translateResult = "printf(\"";
                        for (int i = 0; i < outputs.length; i++) {
                            String t = "";
                            if (type.get(i).equalsIgnoreCase("String")) {
                                t = "s";
                            } else if (type.get(i).equalsIgnoreCase("double")) {
                                t = "f";
                            } else {
                                t = "d";
                            }
                            super.translateResult += outputs[i] + " = %" + t;
                            if (i != outputs.length - 1) {
                                super.translateResult += ", ";
                            }
                        }
                        super.translateResult += "\", ";
                        for (int i = 0; i < outputs.length; i++) {
                            super.translateResult += outputs[i];
                            if (i != outputs.length - 1) {
                                super.translateResult += ", ";
                            }
                        }
                    }
                    super.translateResult += ");";
                    super.translate();


                } else {
                    super.reportError("One of output variable is not exist");
                }

            } else {
            	// output only one variable.
                for (Variable v: TranslateSystem.variables) {
                    if (outputWords.equalsIgnoreCase(v.getName())) {
                        type.add(v.getTrueDataType());
                        existVar = true;
                        break;
                    }
                }

                if (existVar) {

                    if (language.equals("Java")) {
                        super.translateResult = "System.out.println(\"" + outputWords + " = \" + " + outputWords + ");";
                    } else {
                        String t = "";
                        if (type.get(0).equalsIgnoreCase("String")) {
                            t = "s";
                        } else if (type.get(0).equalsIgnoreCase("double")) {
                            t = "f";
                        } else {
                            t = "d";
                        }
                        super.translateResult = "printf(\"" + outputWords + " = %" + t + "\", " + outputWords + ");";
                    }
                    super.translate();

                } else {
                    super.reportError("Output variable is not exist");
                }
            }

        } else {
            super.reportError("No variables are recognized to be output");
        }
    }

}
