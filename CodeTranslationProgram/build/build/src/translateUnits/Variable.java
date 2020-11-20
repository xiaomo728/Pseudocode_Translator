
package translateUnits;

import programInterfaces.Identifier;
import translateProgram.TranslateSystem;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Variable implements Identifier {
	// Object Variable.
	
    private String name;
    private String dataType;
    private String value;
    private boolean isConstant;
    private String trueDataType;

    // constructors.
    public Variable(String name, String dataType, String value, boolean isConstant) {
        this.name = name.toLowerCase();
        this.dataType = dataType;
        this.value = value;
        this.isConstant = isConstant;
        this.trueDataType = this.determineTrueDataType();
    }

    public Variable(String name, String dataType, String value, boolean isConstant, String trueDataType) {
        this.name = name.toLowerCase();
        this.dataType = dataType;
        this.value = value;
        this.isConstant = isConstant;
        this.trueDataType = trueDataType;
    }

    public boolean isValidName() {
        if (this.getName().equals(Line.ERROR_MSG)) return false;
        System.out.println("Variable name: " + this.getName());
        boolean isValid = true;
        boolean isIllegal = Pattern.matches("^[A-Za-z][A-Za-z0-9_]*$", this.getName());
        // only contains English alphabet, numbers and underline, and must start with alphabet.
        if (isIllegal) {
        	// check whether the name has been defined.
            for (String keyword: TranslateSystem.keywords) {
                if (keyword.equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Variable '" + this.getName() + "' is NOT valid on name.");
                    break;
                }
            }

            for (Variable v: TranslateSystem.variables) {
                if (v.getName().equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Variable '" + this.getName() + "' has been defined!");
                    break;
                }
            }

            for (Array a: TranslateSystem.arrays) {
                if (a.getName().equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Variable '" + this.getName() + "' has been defined!");
                    break;
                }
            }

        } else {
            isValid = false;
        }

        return isValid;
    }

    public boolean isValidNameInFunction(ArrayList<Variable> variables, ArrayList<Array> arrays) {
        if (this.getName().equals(Line.ERROR_MSG)) return false;
        System.out.println("Variable name: " + this.getName());
        boolean isValid = true;
        boolean isIllegal = Pattern.matches("^[A-Za-z][A-Za-z0-9_]*$", this.getName());
        // only contains English alphabet, numbers and underline, and must start with alphabet.
        if (isIllegal) {
        	// check whether the name has been defined.
            for (String keyword: TranslateSystem.keywords) {
                if (keyword.equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Variable '" + this.getName() + "' is NOT valid on name.");
                    break;
                }
            }

            for (Variable v: variables) {
                if (v.getName().equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Variable '" + this.getName() + "' has been defined!");
                    break;
                }
            }

            for (Array a: arrays) {
                if (a.getName().equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Variable '" + this.getName() + "' has been defined!");
                    break;
                }
            }

        } else {
            isValid = false;
        }

        return isValid;
    }

    public boolean isValidDataType() {
        if (this.getDataType().equals(Line.ERROR_MSG)) return false;
        boolean isValid = false;
        if (this.getDataType().equalsIgnoreCase("string") || this.getDataType().equalsIgnoreCase("boolean")
                || this.getDataType().equalsIgnoreCase("number") || this.getDataType().equalsIgnoreCase("array")) {
            isValid = true;
        }
        return isValid;
    }

    public String determineTrueDataType() {
        String datatype = "";
        if (this.getValue().equals("")) {
            System.out.println(">>>No value has been assigned to Variable: " + this.getName());
            if (this.getDataType().equalsIgnoreCase("Number")) {
                this.setValue("0");
                if (TranslateSystem.intMode) {
                    return "int";
                } else {
                    return "double";
                }
            } else if (this.getDataType().equalsIgnoreCase("String")) {
                this.setValue("");
                return "String";
            } else if (this.getDataType().equalsIgnoreCase("Boolean")) {
                this.setValue("false");
                return "boolean";
            } else {
                return "ERROR";
            }

        } else {

            String handledString = TranslateSystem.stringBuilder(this.getValue());
            System.out.println(">>>Handled Line: " + handledString);
            if (handledString.equals(Line.ERROR_MSG)) return "ERROR";

            String[] val = handledString.split(" ");
            if (val.length == 0) {
                // value is null.
                System.out.println(">>>Null value");
                if (this.getDataType().equalsIgnoreCase("Number")) {
                    return "int";
                } else if (this.getDataType().equalsIgnoreCase("String")) {
                    return "String";
                } else if (this.getDataType().equalsIgnoreCase("Boolean")) {
                    return "boolean";
                } else {
                    return "ERROR";
                }

            } else {
                System.out.println(">>>Value contains at least one word");
                if (this.getDataType().equalsIgnoreCase("Number")) {
                    System.out.println(">>>User declare variable: " + this.getName() + " is a [Number]");
                    // if a variable declare is a [Number]:
                    // for operators, it only contains: PLUS(+), MINUS(-), TIMES(*), DIVS(/), MOD(%), delete them.
                    // for variables, it only contains variable with Number type and String str.LENGTH, String str.INDEX
                    // for arrays, it only contains arr[i] with Number array, arr.LENGTH.
                    // for maths, it all contains.
                    // check the remaining, it will only contains numbers.
                    for (String o : TranslateSystem.operators.keySet()) {
                        switch (o.toUpperCase()) {
                            case "PLUS":
                            case "+":
                            case "MINUS":
                            case "-":
                            case "TIMES":
                            case "*":
                            case "DIVS":
                            case "/":
                            case "MOD":
                            case "%":
                                for (int i = 0; i < val.length; i++) {
                                    if (val[i].equalsIgnoreCase(o)) {
                                        val[i] = "$";
                                    }
                                }
                                break;
                            default:
                                if (handledString.contains(" " + o + " ")) {
                                    System.out.println(">>>Wrong operators exist.");
                                    System.out.println(">>> o : " + o);
                                    return "ERROR";
                                }
                        }
                    }
                    System.out.println(">>>After checking operators, " + java.util.Arrays.toString(val));

                    for (Variable var : TranslateSystem.variables) {
                        for (int i = 0; i < val.length; i++) {
                            if (val[i].equalsIgnoreCase(var.getName())) {
                                if (!var.getDataType().equalsIgnoreCase("Number")) {
                                    System.out.println(">>>Contains non-number variable.");
                                    return "ERROR";
                                } else {
                                    val[i] = "$";
                                    String numType = var.getTrueDataType();
                                    if (numType.equalsIgnoreCase("double")) {
                                        datatype = "double";
                                    } else if (numType.equalsIgnoreCase("int")) {
                                        if (datatype.equals("")) {
                                            datatype = "int";
                                        }
                                    }
                                }
                            }
                            if (val[i].toUpperCase().equals(var.getName().toUpperCase() + ".LENGTH") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".INDEX")) {
                                if (var.getDataType().equalsIgnoreCase("String")) {
                                    val[i] = "$";
                                    if (!datatype.equals("double")) {
                                        datatype = "int";
                                    } else {
                                        datatype = "double";
                                    }
                                } else {
                                    System.out.println(">>>Wrong use on str.LENGTH, str.INDEX");
                                    return "ERROR";
                                }
                            }
                            if (val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".[") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + "[")
                                    || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".UPPERCASE") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".LOWERCASE")) {
                                System.out.println(">>>Wrong use on str.[], str[], str.UPPERCASE, str.LOWERCASE");
                                return "ERROR";
                            }
                        }
                    }
                    System.out.println(">>>After checking variables, " + java.util.Arrays.toString(val));

                    for (Array arr : TranslateSystem.arrays) {
                        for (int i = 0; i < val.length; i++) {
                            if (val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + "[")) {
                                if (!arr.getDataType().equalsIgnoreCase("Number")) {
                                    System.out.println(">>>Non-number array exist.");
                                    return "ERROR";
                                } else {
                                    val[i] = "$";
                                    String numType = arr.getTrueDataType();
                                    if (!datatype.equals("double")) {
                                        datatype = numType;
                                    }
                                }
                            }
                            if (val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".LENGTH")) {
                                val[i] = "$";
                                if (!datatype.equals("double")) {
                                    datatype = "int";
                                }
                            }
                            if (val[i].equalsIgnoreCase(arr.getName()) || val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".[")) {
                                System.out.println(">>>Wrong array use.");
                                return "ERROR";
                            }
                        }
                    }
                    System.out.println(">>>After checking arrays, " + java.util.Arrays.toString(val));

                    for (String m : TranslateSystem.maths.keySet()) {
                        for (int i = 0; i < val.length; i++) {
                            if (val[i].toUpperCase().startsWith(m)) {
                                val[i] = "$";
                                if ((m.equals("MAX") || m.equals("MIN") || m.equals("ABS") || m.equals("MAX_8BIT") || m.equals("MIN_8BIT")
                                        || m.equals("MAX_16BIT") || m.equals("MIN_16BIT") || m.equals("MAX_32BIT") || m.equals("MIN_32BIT")) && TranslateSystem.intMode) {
                                    datatype = "int";
                                } else {
                                    if (!datatype.equals("double")) {
                                        datatype = "double";
                                    }
                                }
                            }
                        }
                    }
                    System.out.println(">>>After checking maths, " + java.util.Arrays.toString(val));

                    for (String s : val) {
                        System.out.println("s:" + s);
                        if (!s.equals("$")) {
                            try {
                                Integer.parseInt(s);
                                System.out.println(">>>A int exist.");
                                if (!datatype.equals("double")) {
                                    datatype = "int";
                                }
                            } catch (NumberFormatException intException) {
                                try {
                                    Double.parseDouble(s);
                                    System.out.println(">>>A double exist.");
                                    datatype = "double";
                                } catch (NumberFormatException doubleException) {
                                    // NaN.
                                    if (s.equals("(") || s.equals(")")) {
                                        if (datatype.equals("double")) {
                                            datatype = "double";
                                        } else {
                                            datatype = "int";
                                        }
                                    } else if (s.startsWith("(") || s.endsWith(")")) {
                                        s = s.replace("(", "").replace(")", "");
                                        int type = TranslateSystem.valueJudge(s, "Number");
                                        if (type == 1) {
                                            return "int";
                                        } else if (type == 2) {
                                            return "double";
                                        } else {
                                            return "ERROR";
                                        }

                                    } else {
                                        System.out.println(">>>Non-number exist.");
                                        return "ERROR";
                                    }
                                }
                            }
                        }
                    }
                    System.out.println(">>>Checking result datatype = " + datatype);


                } else if (this.getDataType().equalsIgnoreCase("String")) {
                    System.out.println(">>>User declare variable: " + this.getName() + " is a [String]");
                    // if a variable declare is a [String]:
                    // for operators, it should contain nothing.
                    // for variables, it will contain String variables, String str[i], str.[i..j], str.UPPERCASE, str.LOWERCASE
                    // for arrays, it will contain arr[i] with String array.
                    // for maths, it should contain nothing.
                    // check the remaining, it should only has "xxx" thing.
                    for (String o : TranslateSystem.operators.keySet()) {
                        if (handledString.contains(o)) {
                            return "ERROR";
                        }
                    }

                    for (Variable var : TranslateSystem.variables) {
                        for (int i = 0; i < val.length; i++) {
                            if (val[i].equalsIgnoreCase(var.getName())) {
                                if (!var.getDataType().equalsIgnoreCase("String")) {
                                    System.out.println(">>>Non-string variable exist.");
                                    return "ERROR";
                                } else {
                                    val[i] = "$";
                                    datatype = "String";
                                }
                            }
                            if (val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".[") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + "[")
                                    || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".UPPERCASE") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".LOWERCASE")) {
                                if (var.getDataType().equalsIgnoreCase("String")) {
                                    val[i] = "$";
                                    datatype = "String";
                                } else {
                                    System.out.println(">>>Wrong use str.xxx");
                                    return "ERROR";
                                }
                            }
                            if (val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".LENGTH") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".INDEX[")) {
                                System.out.println(">>>Wrong use on str.LENGTH, str.INDEX");
                                return "ERROR";
                            }
                        }
                    }
                    System.out.println(">>>After checking variables, " + java.util.Arrays.toString(val));

                    for (Array arr : TranslateSystem.arrays) {
                        for (int i = 0; i < val.length; i++) {
                            if (val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + "[")) {
                                if (!arr.getDataType().equalsIgnoreCase("String")) {
                                    System.out.println(">>>Non-String array exist.");
                                    return "ERROR";
                                } else {
                                    val[i] = "$";
                                    datatype = "String";
                                }
                            }
                            if (val[i].equalsIgnoreCase(arr.getName()) || val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".[")
                                    || val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".LENGTH")) {
                                System.out.println(">>>Wrong array use.");
                                return "ERROR";
                            }
                        }
                    }
                    System.out.println(">>>After checking arrays, " + java.util.Arrays.toString(val));

                    for (String m : TranslateSystem.maths.keySet()) {
                        for (String s : val) {
                            if (s.toUpperCase().startsWith(m)) {
                                System.out.println(">>>Wrong use Math.");
                                return "ERROR";
                            }
                        }
                    }

                    for (String s : val) {
                        if (!s.equals("$")) {
                            if (!(s.startsWith("\"") && s.endsWith("\""))) {
                                return "ERROR";
                            }
                        }
                    }
                    datatype = "String";
                    System.out.println(">>>Checking result datatype = " + datatype);

                } else if (this.getDataType().equalsIgnoreCase("Boolean")) {
                    System.out.println(">>>User declare variable: " + this.getName() + " is a [Boolean]");
                    // if a variable declare is a [Boolean]:
                    // for operators, it may contain all operators, jump.
                    // for variables, it will contain all variables, jump.
                    // for arrays, it will contain all possible array values, but not arr, arr.[i..j].
                    // for maths, it may contain all operators, jump.
                    // boolean is difficult to check.
                    boolean hasJudgement = false;
                    if (handledString.toUpperCase().contains("AND") || handledString.toUpperCase().contains("OR") || handledString.toUpperCase().contains("NOT")
                            || handledString.toUpperCase().contains("TRUE") || handledString.toUpperCase().contains("FALSE") || handledString.toUpperCase().contains("IS DIVISIBLE BY")) hasJudgement = true;
                    for (String o : TranslateSystem.operators.keySet()) {
                        switch (o.toUpperCase()) {
                            case "!=":
                            case "==":
                            case ">":
                            case ">=":
                            case "<":
                            case "<=":
                                if (handledString.contains(o)) hasJudgement = true;
                                break;
                            default:
                        }
                    }

                    for (String o : TranslateSystem.complexOperators.keySet()) {
                        if (handledString.toUpperCase().contains(o)) hasJudgement = true;
                    }

                    if (!hasJudgement) {
                        System.out.println(">>> No judgment expression.");
                        return "ERROR";
                    }

                    for (Array arr : TranslateSystem.arrays) {
                        for (int i = 0; i < val.length; i++) {
                            if (val[i].equalsIgnoreCase(arr.getName()) || val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".[")) {
                                System.out.println(">>>Wrong array use.");
                                return "ERROR";
                            }
                        }
                    }
                    datatype = "boolean";
                    System.out.println(">>>Checking result datatype = " + datatype);
                } else {
                    System.out.println(">>>User declares a wrong data type.");
                    return "ERROR";
                }
            }
        }
        if (datatype.equals("double") && TranslateSystem.intMode) {
            return "ERROR";
        }

        return datatype;
    }

    public void setUnavailable() {
        this.name = "";
    }

    public String getName() {
        return this.name;
    }

    public String getDataType() {
        return this.dataType;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isConstant() {
        return this.isConstant;
    }

    public String getTrueDataType() {
        return this.trueDataType;
    }

}
