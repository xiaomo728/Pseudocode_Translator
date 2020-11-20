
package translators;

import programInterfaces.TranslatorInterface;
import translateProgram.TranslateSystem;
import translateUnits.Array;
import translateUnits.Line;
import translateUnits.Variable;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DefineTranslator extends Translator implements TranslatorInterface {

    private int lineNum;
    private Line line;
    private ArrayList<String> preLines;

    public DefineTranslator(int lineNum, Line line, ArrayList<String> preLines) {
        super(lineNum, line, preLines);
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        String uniqueDataType = this.line.component(-1).trim();

        if (this.line.startsWith("DEFINE A CONSTANT")) {
            // DEFINE A CONSTANT variable = value AS NUMBER
            System.out.println(">>>Define a constant...");

            if (this.line.component(5).equals("=")) {

                String vName = this.line.component(4).trim();
                String vDatatype = this.line.component(this.line.componentOf("AS") + 1).trim();
                if (!uniqueDataType.equalsIgnoreCase(vDatatype)) {
                    super.reportError("Syntax error on define statement grammar");
                    return;
                }
                String vValue = this.line.component(this.line.componentOf("=") + 1, this.line.componentOf("AS") - 1).trim();
                Variable v = new Variable(vName, vDatatype, vValue, true);

                if (v.isValidName() && v.isValidDataType() && !v.getTrueDataType().equals("ERROR")) {
                    TranslateSystem.variables.add(new Variable(vName, vDatatype, vValue, true, v.getTrueDataType()));
                    if (!TranslateSystem.expressionConverter(vValue, language).equals(Line.ERROR_MSG)) {
                        if (language.equals("Java")) {
                            super.translateResult = "final " + v.getTrueDataType() + " " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                            super.translate();
                        } else {
                            if (v.getTrueDataType().equalsIgnoreCase("String")) {
                                super.translateResult = "const char " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                super.translate();
                            } else if (v.getTrueDataType().equalsIgnoreCase("boolean")) {
                                super.translateResult = "const int " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                super.translate();
                            } else {
                                super.translateResult = "const " + v.getTrueDataType() + " " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                super.translate();
                            }
                        }

                    } else {
                        super.reportError("Illegal identifier exist in assigned value");
                    }

                } else if (!v.isValidName()){
                    super.reportError("Constant's name is illegal or it has been defined");
                } else if (!v.isValidDataType()) {
                    super.reportError("Constant's data type is illegal");
                } else if (v.getTrueDataType().equals("ERROR")) {
                    super.reportError("Constant's data type is contradict with its value");
                }

            } else if (!this.line.contain("=")) {
                super.reportError("Constant's value is null");

            } else {
                super.reportError("Constant name is illegal or wrong define type in define keyword");
            }

        } else if (this.line.startsWith("DEFINE ARRAY")) {
            // DEFINE ARRAY array[i..j] AS NUMBER
            System.out.println(">>>Define an array...");

            String arrayName = this.line.component(3);
            if (arrayName.contains("[1..") && arrayName.endsWith("]") && !arrayName.contains("][1..")) {
                // Define an array but don't assign value.

                if (this.line.component(4).toUpperCase().equals("AS")) {
                    System.out.println(">>>Define an array... but don't assign value");
                    int index = 0;
                    for (int i = 0; i < arrayName.length(); i++) {
                        char c = arrayName.charAt(i);
                        if (c == '[') {
                            index = i;
                            break;
                        }
                    }

                    if (index == 0) {
                        super.reportError("Illegal array define exist");
                    } else {
                        String aName = arrayName.substring(0, index).trim();
                        String aDataType = this.line.component(this.line.componentOf("AS") + 1).trim();
                        if (!uniqueDataType.equalsIgnoreCase(aDataType)) {
                            super.reportError("Syntax error on define statement grammar");
                            return;
                        }
                        String aLength = "";
                        try {
                            aLength = "1," + String.valueOf(arrayName.charAt(index + 4));
                        } catch (NumberFormatException e) {
                            super.reportError("Array's capacity is illegally defined");
                        }

                        Array a = new Array(aName, aDataType, aLength);
                        a.setTrueDataType(a.determineTrueDataType());

                        if (a.isValidName() && a.isValidDataType() && !a.getTrueDataType().equals("ERROR")) {
                            TranslateSystem.arrays.add(new Array(aName, aDataType, aLength, new LinkedHashMap<String, String>(), a.getTrueDataType()));
                            String length[] = aLength.split(",");
                            if (language.equals("Java")) {
                                super.translateResult = a.getTrueDataType() + "[] " + aName + " = " + "new " + a.getTrueDataType() + "[" + length[1] + "];";
                                super.translate();
                            } else {
                                if (a.getTrueDataType().equalsIgnoreCase("String")) {
                                    super.translateResult = "char *" + aName + "[" + length[1] + "];";
                                    super.translate();
                                } else if (a.getTrueDataType().equalsIgnoreCase("boolean")) {
                                    super.translateResult = "int " + aName + "[" + length[1] + "];";
                                    super.translate();
                                } else {
                                    super.translateResult = a.getTrueDataType() + " " + aName + "[" + length[1] + "];";
                                    super.translate();
                                }
                            }

                        } else if (!a.isValidName()){
                            super.reportError("Array's name is illegal or it has been defined");
                        } else if (!a.isValidDataType()) {
                            super.reportError("Array's data type is illegal");
                        }
                    }


                } else {
                    super.reportError("Array's name is illegal or invalid define type");
                }

            } else if (!arrayName.contains("[1..") && !arrayName.endsWith("]") && !arrayName.contains("][1..") && this.line.component(4).equals("=")) {
                // Define an array and assign values.
                System.out.println(">>>Define an array... and assign values");

                if (this.line.component(5).startsWith("[") && this.line.component(this.line.componentOf("AS") - 1).endsWith("]")
                        && !this.line.component(5).startsWith("[[") && !this.line.component(this.line.componentOf("AS") - 1).endsWith("]]")) {
                    System.out.println(">>>1D array");
                    int index1 = 0, index2 = 0;
                    for (int i = 0; i < this.line.getPureContent().length(); i++) {
                        char c = this.line.getPureContent().charAt(i);
                        if (c == '[') {
                            index1 = i + 1;
                        }
                        if (c == ']') {
                            index2 = i;
                            break;
                        }
                    }

                    String aName = arrayName.trim();
                    String aDataType = this.line.component(this.line.componentOf("AS") + 1).trim();
                    if (!uniqueDataType.equalsIgnoreCase(aDataType)) {
                        super.reportError("Syntax error on define statement grammar");
                        return;
                    }
                    if (index1 == 0 || index2 == 0 || index2 <= index1) {
                        super.reportError("Illegal array define exist");
                    } else {
                        String elements[] = this.line.getPureContent().substring(index1, index2).split(",");
                        String aLength = "1," + String.valueOf(elements.length);

                        Array a = new Array(aName, aDataType, aLength);
                        for (String e: elements) {
                            boolean addSuccessful = a.addValue(e);
                            if (!addSuccessful) {
                                super.reportError("Too many elements has been assigned into array");
                            }
                        }
                        a.setTrueDataType(a.determineTrueDataType());

                        if (a.isValidName() && a.isValidDataType() && !a.getTrueDataType().equals("ERROR")) {
                            TranslateSystem.arrays.add(new Array(aName, aDataType, aLength, a.getValues(), a.getTrueDataType()));
                            if (language.equals("Java")) {
                                super.translateResult = a.getTrueDataType() + "[] " + aName + " = " + a.getArrayValue(language) + ";";
                                super.translate();
                            } else {
                                if (a.getTrueDataType().equalsIgnoreCase("String")) {
                                    super.translateResult = "char *" + aName + "[] = " + a.getArrayValue(language) + ";";
                                    super.translate();
                                } else if (a.getTrueDataType().equalsIgnoreCase("boolean")) {
                                    super.translateResult = "int " + aName + "[] = " + a.getArrayValue(language) + ";";
                                    super.translate();
                                } else {
                                    super.translateResult = a.getTrueDataType() + " " + aName + "[] = " + a.getArrayValue(language) + ";";
                                    super.translate();
                                }
                            }

                        } else if (!a.isValidName()){
                            super.reportError("Array's name is illegal or it has been defined");
                        } else if (!a.isValidDataType()) {
                            super.reportError("Array's data type is illegal");
                        } else if (a.getTrueDataType().equals("ERROR")) {
                            super.reportError("One of array element's data type is illegal");
                        }
                    }


                } else if (this.line.component(5).startsWith("[[") && this.line.component(this.line.componentOf("AS") - 1).endsWith("]]")) {
                    System.out.println(">>>2D array");
                    int index1 = 0, index2 = 0;
                    for (int i = 0; i < this.line.getPureContent().length(); i++) {
                        if (i == this.line.getPureContent().length() - 3) break;
                        char c1 = this.line.getPureContent().charAt(i);
                        char c2 = this.line.getPureContent().charAt(i + 1);
                        if (c1 == '[' && c2 == '[') {
                            index1 = i + 2;
                        }
                        if (c1 == ']' && c2 == ']') {
                            index2 = i;
                            break;
                        }
                    }

                    String aName = arrayName.trim();
                    String aDataType = this.line.component(this.line.componentOf("AS") + 1).trim();
                    if (!uniqueDataType.equalsIgnoreCase(aDataType)) {
                        super.reportError("Syntax error on define statement grammar");
                        return;
                    }
                    if (index1 == 0 || index2 == 0 || index2 <= index1) {
                        super.reportError("Illegal array define exist");
                    } else {
                        String rowElements[] = this.line.getPureContent().substring(index1, index2).split("],(\\s*)\\[");
                        String col1Elements[] = rowElements[0].split(",");
                        String aLength = String.valueOf(rowElements.length) + "," + String.valueOf(col1Elements.length);

                        Array a = new Array(aName, aDataType, aLength);
                        for (String re: rowElements) {
                            String colElement[] = re.split(",");
                            for (String ce: colElement) {
                                boolean addSuccessful = a.addValue(ce);
                                if (!addSuccessful) {
                                    super.reportError("Too many elements has been assigned into array");
                                }
                            }
                            a.nextRow();
                        }
                        a.setTrueDataType(a.determineTrueDataType());

                        if (a.isValidName() && a.isValidDataType() && !a.getTrueDataType().equals("ERROR")) {
                            TranslateSystem.arrays.add(new Array(aName, aDataType, aLength, a.getValues(), a.getTrueDataType()));
                            if (language.equals("Java")) {
                                super.translateResult = a.getTrueDataType() + "[][] " + aName + " = " + a.getArrayValue2D(language) + ";";
                                super.translate();
                            } else {
                                if (a.getTrueDataType().equalsIgnoreCase("String")) {
                                    super.translateResult = "char *" + aName + "[][] = " + a.getArrayValue2D(language) + ";";
                                    super.translate();
                                } else if (a.getTrueDataType().equalsIgnoreCase("boolean")) {
                                    super.translateResult = "int " + aName + "[][] = " + a.getArrayValue2D(language) + ";";
                                    super.translate();
                                } else {
                                    super.translateResult = a.getTrueDataType() + " " + aName + "[][] = " + a.getArrayValue2D(language) + ";";
                                    super.translate();
                                }
                            }

                        } else if (!a.isValidName()){
                            super.reportError("Array's name is illegal or it has been defined");
                        } else if (!a.isValidDataType()) {
                            super.reportError("Array's data type is illegal");
                        } else if (a.getTrueDataType().equals("ERROR")) {
                            super.reportError("One of array element's data type is illegal");
                        }
                    }

                } else {
                    super.reportError("Illegal array define format");
                }

            } else if (arrayName.contains("[1..") && arrayName.endsWith("]") && arrayName.contains("][1..")) {
                // define a two-dimension array but don't assign value.

                if (this.line.component(4).toUpperCase().equals("AS")) {
                    System.out.println(">>>Define an array(2D)... but don't assign value");
                    int index = 0;
                    for (int i = 0; i < arrayName.length(); i++) {
                        char c = arrayName.charAt(i);
                        if (c == '[') {
                            index = i;
                            break;
                        }
                    }

                    if (index == 0) {
                        super.reportError("Illegal array define exist");
                    } else {
                        String aName = arrayName.substring(0, index).trim();
                        String aDataType = this.line.component(this.line.componentOf("AS") + 1).trim();
                        if (!uniqueDataType.equalsIgnoreCase(aDataType)) {
                            super.reportError("Syntax error on define statement grammar");
                            return;
                        }
                        String aLength = "";
                        try {
                            aLength = String.valueOf(arrayName.charAt(index + 4)) + "," + String.valueOf(arrayName.charAt(index + 10));
                        } catch (NumberFormatException e) {
                            super.reportError("Array's capacity is illegally defined");
                        }

                        Array a = new Array(aName, aDataType, aLength);
                        a.setTrueDataType(a.determineTrueDataType());

                        if (a.isValidName() && a.isValidDataType() && !a.getTrueDataType().equals("ERROR")) {
                            TranslateSystem.arrays.add(new Array(aName, aDataType, aLength, new LinkedHashMap<String, String>(), a.getTrueDataType()));
                            String length[] = aLength.split(",");
                            if (language.equals("Java")) {
                                super.translateResult = a.getTrueDataType() + "[][] " + aName + " = " + "new " + a.getTrueDataType() + "[" + length[0] + "][" + length[1] + "];";
                                super.translate();
                            } else {
                                if (a.getTrueDataType().equalsIgnoreCase("String")) {
                                    super.translateResult = "char *" + aName + "[" + length[0] + "][" + length[1] + "];";
                                    super.translate();
                                } else if (a.getTrueDataType().equalsIgnoreCase("boolean")) {
                                    super.translateResult = "int " + aName + "[" + length[0] + "][" + length[1] + "];";
                                    super.translate();
                                } else {
                                    super.translateResult = a.getTrueDataType() + " " + aName + "[" + length[0] + "][" + length[1] + "];";
                                    super.translate();
                                }
                            }

                        } else if (!a.isValidName()){
                            super.reportError("Array's name is illegal or it has been defined");
                        } else if (!a.isValidDataType()) {
                            super.reportError("Array's data type is illegal");
                        }
                    }


                } else {
                    super.reportError("Array's name is illegal or invalid define type");
                }

            } else {
                super.reportError("Illegal array define format");
            }

        } else if (this.line.startsWith("DEFINE FUNCTION")) {
            System.out.println(">>>Define a function in main function");
            super.reportError("Please define function in system's add function (+) area");

        } else if (this.line.startsWith("DEFINE") && !this.line.startsWith("DEFINE ARRAY") && !this.line.startsWith("DEFINE A CONSTANT")) {
            // DEFINE variable = value AS NUMBER;
            System.out.println(">>>Define a variable...");

            if (this.line.component(3).equals("=")) {

                if (this.line.component(2).contains(",")) {
                    // Define multiple variables at same time.
                    System.out.println(">>>Define multiple variables at same time");

                    String vNames[] = this.line.component(2).split(",");
                    for (String s: vNames) {
                        String vName = s.trim();
                        String vDatatype = this.line.component(this.line.componentOf("AS") + 1).trim();
                        if (!uniqueDataType.equalsIgnoreCase(vDatatype)) {
                            super.reportError("Syntax error on define statement grammar");
                            return;
                        }
                        String vValue = this.line.component(this.line.componentOf("=") + 1, this.line.componentOf("AS") - 1).trim();
                        Variable v = new Variable(vName, vDatatype, vValue, false);

                        if (v.isValidName() && v.isValidDataType() && !v.getTrueDataType().equals("ERROR")) {
                            TranslateSystem.variables.add(new Variable(vName, vDatatype, vValue, false, v.getTrueDataType()));
                            if (!TranslateSystem.expressionConverter(vValue, language).equals(Line.ERROR_MSG)) {
                                if (language.equals("Java")) {
                                    super.translateResult = v.getTrueDataType() + " " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                    super.translate();
                                } else {
                                    if (v.getTrueDataType().equalsIgnoreCase("String")) {
                                        super.translateResult = "char[] " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                        super.translate();
                                    } else if (v.getTrueDataType().equalsIgnoreCase("boolean")) {
                                        super.translateResult = "int " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                        super.translate();
                                    } else {
                                        super.translateResult = v.getTrueDataType() + " " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                        super.translate();
                                    }
                                }

                            } else {
                                super.reportError("Illegal identifier exist in assigned value");
                            }

                        } else if (!v.isValidName()){
                            super.reportError("Variable's name is illegal or it has been defined");
                        } else if (!v.isValidDataType()) {
                            super.reportError("Variable's data type is illegal");
                        } else if (v.getTrueDataType().equals("ERROR")) {
                            super.reportError("Variable's data type is contradict with its value");
                        }
                    }

                } else if (!this.line.component(2).contains(",")) {
                    // Define a variable merely.
                    System.out.println(">>>Define a variable merely");

                    String vName = this.line.component(2).trim();
                    String vDatatype = this.line.component(this.line.componentOf("AS") + 1).trim();
                    if (!uniqueDataType.equalsIgnoreCase(vDatatype)) {
                        super.reportError("Syntax error on define statement grammar");
                        return;
                    }
                    String vValue = this.line.component(this.line.componentOf("=") + 1, this.line.componentOf("AS") - 1).trim();
                    Variable v = new Variable(vName, vDatatype, vValue, false);

                    if (v.isValidName() && v.isValidDataType() && !v.getTrueDataType().equals("ERROR")) {
                        TranslateSystem.variables.add(new Variable(vName, vDatatype, vValue, false, v.getTrueDataType()));
                        if (!TranslateSystem.expressionConverter(vValue, language).equals(Line.ERROR_MSG)) {
                            if (language.equals("Java")) {
                                super.translateResult = v.getTrueDataType() + " " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                super.translate();
                            } else {
                                if (v.getTrueDataType().equalsIgnoreCase("String")) {
                                    super.translateResult = "char[] " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                    super.translate();
                                } else if (v.getTrueDataType().equalsIgnoreCase("boolean")) {
                                    super.translateResult = "int " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                    super.translate();
                                } else {
                                    super.translateResult = v.getTrueDataType() + " " + vName + " = " + TranslateSystem.expressionConverter(vValue, language) + ";";
                                    super.translate();
                                }
                            }

                        } else {
                            super.reportError("Illegal identifier exist in assigned value");
                        }

                    } else if (!v.isValidName()){
                        super.reportError("Variable's name is illegal or it has been defined");
                    } else if (!v.isValidDataType()) {
                        super.reportError("Variable's data type is illegal");
                    } else if (v.getTrueDataType().equals("ERROR")) {
                        super.reportError("Variable's data type is contradict with its value");
                    }
                }

            } else if (!this.line.contain("=")) {
                // Define variables without assignment.
                System.out.println(">>>Define variables without assignment");

                if (this.line.component(3).equalsIgnoreCase("AS")) {

                    if (this.line.component(2).contains(",")) {
                        // Define multiple variables at same time.
                        System.out.println(">>>Define multiple variables at same time");

                        String vNames[] = this.line.component(2).split(",");
                        for (String s: vNames) {
                            String vName = s.trim();
                            String vDatatype = this.line.component(4).trim();
                            if (!uniqueDataType.equalsIgnoreCase(vDatatype)) {
                                super.reportError("Syntax error on define statement grammar");
                                return;
                            }
                            Variable v = new Variable(vName, vDatatype, "", false);

                            if (v.isValidName() && v.isValidDataType() && !v.getTrueDataType().equals("ERROR")) {
                                TranslateSystem.variables.add(new Variable(vName, vDatatype, v.getValue(), false, v.getTrueDataType()));
                                if (language.equals("Java")) {
                                    super.translateResult = v.getTrueDataType() + " " + vName + ";";
                                    super.translate();
                                } else {
                                    if (v.getTrueDataType().equalsIgnoreCase("String")) {
                                        super.translateResult = "char[] " + vName + ";";
                                        super.translate();
                                    } else if (v.getTrueDataType().equalsIgnoreCase("boolean")) {
                                        super.translateResult = "int " + vName + ";";
                                        super.translate();
                                    } else {
                                        super.translateResult = v.getTrueDataType() + " " + vName + ";";
                                        super.translate();
                                    }
                                }

                            } else if (!v.isValidName()){
                                super.reportError("Variable's name is illegal or it has been defined");
                            } else if (!v.isValidDataType()) {
                                super.reportError("Variable's data type is illegal");
                            } else if (v.getTrueDataType().equals("ERROR")) {
                                super.reportError("Variable's data type is contradict with its value");
                            }
                        }

                    } else if (!this.line.component(2).contains(",")) {
                        // Define a variable merely.
                        System.out.println(">>>Define a variable merely");

                        String vName = this.line.component(2).trim();
                        String vDatatype = this.line.component(4).trim();
                        if (!uniqueDataType.equalsIgnoreCase(vDatatype)) {
                            super.reportError("Syntax error on define statement grammar");
                            return;
                        }
                        Variable v = new Variable(vName, vDatatype, "", false);

                        if (v.isValidName() && v.isValidDataType() && !v.getTrueDataType().equals("ERROR")) {
                            TranslateSystem.variables.add(new Variable(vName, vDatatype, v.getValue(), false, v.getTrueDataType()));
                            if (language.equals("Java")) {
                                super.translateResult = v.getTrueDataType() + " " + vName + ";";
                                super.translate();
                            } else {
                                if (v.getTrueDataType().equalsIgnoreCase("String")) {
                                    super.translateResult = "char[] " + vName + ";";
                                    super.translate();
                                } else if (v.getTrueDataType().equalsIgnoreCase("boolean")) {
                                    super.translateResult = "int " + vName + ";";
                                    super.translate();
                                } else {
                                    super.translateResult = v.getTrueDataType() + " " + vName + ";";
                                    super.translate();
                                }
                            }

                        } else if (!v.isValidName()){
                            super.reportError("Variable's name is illegal or it has been defined");
                        } else if (!v.isValidDataType()) {
                            super.reportError("Variable's data type is illegal");
                        } else if (v.getTrueDataType().equals("ERROR")) {
                            super.reportError("Variable's data type is contradict with its value");
                        }
                    }

                } else {
                    super.reportError("Variable name is illegal");
                }

            } else {
                super.reportError("Variable name is illegal or wrong define type in define keyword");
            }

        } else {
            super.reportError("Illegal expression with define keyword");
        }
    }

}
