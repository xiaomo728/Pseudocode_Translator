
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Array;
import translateUnits.Line;

import java.util.ArrayList;

public class AddListTranslator extends Translator implements TranslatorInterface {
    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public AddListTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        if (this.line.getPureContent().toUpperCase().startsWith("ADD")) {
        	// Add an element into an array.
            if (this.line.component(3).equalsIgnoreCase("INTO") && this.line.component(5).equals(Line.ERROR_MSG)) {
            	// ADD x INTO array
                String value = this.line.component(2).trim();
                String array = this.line.component(4).trim();
                boolean validArray = false;
                for (Array a: TranslateSystem.arrays) {
                    if (a.getName().equalsIgnoreCase(array)) {
                        if (!a.is2DArray()) {
                            boolean successfulAdd = a.addValueByAddInto(value);
                            if (successfulAdd) {
                                super.translateResult = a.getName() + "[" + a.getCurrentColMinus1() + "] = " + value + ";";
                                super.translate();
                                validArray = true;
                            } else {
                                super.reportError("Value cannot be added into array because of its datatype or array is full");
                                return;
                            }
                        } else {
                            boolean successfulAdd = a.addValueByAddInto(value);
                            if (successfulAdd) {
                                super.translateResult = a.getName() + "[" + a.getCurrentRow() + "][" + a.getCurrentColMinus1() + "] = " + value + ";";
                                super.translate();
                                validArray = true;
                            } else {
                                super.reportError("Value cannot be added into array because of its datatype or array is full");
                                return;
                            }
                        }
                        break;
                    }
                }

                if (!validArray) super.reportError("Array may be not defined yet");

            } else {
                super.reportError("Syntax error on add into statement");
            }

        } else if (this.line.getPureContent().toUpperCase().startsWith("LIST")) {
        	// LIST x = [q,q,q]
            if (this.line.component(3).equals("=")) {
                String array = this.line.component(2).trim();
                String values = this.line.component(4, -1).trim();
                for (Array a: TranslateSystem.arrays) {
                    if (a.getName().equalsIgnoreCase(array)) {
                        // get the length of array.
                        String rowCol[] = a.getLength().split(",");
                        int rowLength = Integer.parseInt(rowCol[0]);
                        int colLength = Integer.parseInt(rowCol[1]);
                        System.out.println(">>> List array, with row: " + rowLength + ", col: " + colLength);

                        if (values.startsWith("[") && values.endsWith("]") && !values.startsWith("[[") && !values.endsWith("]]")) {
                            // 1D array.
                            if (rowLength != 1) {
                                super.reportError("Cannot assign a 1D value to a 2D array");
                            } else {
                                String elements[] = values.substring(1, values.length() - 1).split(",");
                                if (elements.length != colLength) {
                                    super.reportError("List number of elements is not consistent with that of array defined");
                                } else {
                                    super.translateResult = "";
                                    for (String e: elements) {
                                        boolean addSuccessful = a.addValueByAddInto(e);
                                        if (!addSuccessful) {
                                            super.reportError("Element cannot be added into array because of its datatype or array is full");
                                            return;
                                        } else {
                                            if (a.getDataType().equalsIgnoreCase("String")) {
                                                e = "\"" + e + "\"";
                                            }
                                            if (super.translateResult.equals("")) {
                                                // first line.
                                                super.translateResult += a.getName() + "[" + a.getCurrentColMinus1() + "] = " + e + ";" + "\r\n";
                                            } else {
                                                super.translateResult += this.line.indentation() + a.getName() + "[" + a.getCurrentColMinus1() + "] = " + e + ";" + "\r\n";
                                            }
                                        }
                                    }
                                    super.translate();
                                }
                            }

                        } else if (values.startsWith("[[") && values.endsWith("]]")) {
                            // 2D array.
                            String rowElements[] = values.substring(2, values.length() - 2).split("],(\\s*)\\[");
                            String col1Elements[] = rowElements[0].split(",");
                            if (col1Elements.length != colLength || rowElements.length != rowLength) {
                                super.reportError("List number of elements is not consistent with that of array defined");
                            } else {
                                super.translateResult = "";
                                for (String re: rowElements) {
                                    String colElement[] = re.split(",");
                                    for (String ce: colElement) {
                                        boolean addSuccessful = a.addValueByAddInto(ce);
                                        if (!addSuccessful) {
                                            super.reportError("Too many elements has been assigned into array");
                                            return;
                                        } else {
                                            if (a.getDataType().equalsIgnoreCase("String")) {
                                                ce = "\"" + ce + "\"";
                                            }
                                            if (super.translateResult.equals("")) {
                                                // first line.
                                                super.translateResult += a.getName() + "[" + a.getCurrentRow() + "][" + a.getCurrentColMinus1() + "] = " + ce + ";" + "\r\n";
                                            } else {
                                                super.translateResult += this.line.indentation() + a.getName() + "[" + a.getCurrentRow() + "][" + a.getCurrentColMinus1() + "] = " + ce + ";" + "\r\n";
                                            }
                                        }
                                    }
                                }
                                super.translate();
                            }


                        } else {
                            super.reportError("Syntax error on value of array");
                        }
                        break;
                    }
                }

            } else {
                super.reportError("Syntax error on list statement");
            }

        } else {
            super.reportError("Unknown pseudo-code syntax");
        }
    }

}
