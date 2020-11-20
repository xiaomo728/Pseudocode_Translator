
package translateUnits;

import programInterfaces.Identifier;
import translateProgram.TranslateSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Array implements Identifier {
	// Object Array.
	
    private String name;
    private String dataType;
    private String length;
    private String trueDataType;
    private LinkedHashMap<String, String> values;

    private int currentRow = 0;
    private int currentCol = 0;

    // constructors.
    public Array(String name, String dataType, String length) {
        this.name = name.toLowerCase();
        this.dataType = dataType;
        this.length = length; // length = 1,1 as a array param in function.

        this.values = new LinkedHashMap<String, String>();
    }

    public Array(String name, String dataType, String length, LinkedHashMap<String, String> values, String trueDataType) {
        this.name = name.toLowerCase();
        this.dataType = dataType;
        this.length = length;

        this.values = values;
        this.trueDataType = trueDataType;
    }

    public boolean isValidName() {
        if (this.getName().equals(Line.ERROR_MSG)) return false;
        boolean isValid = true;
        boolean isIllegal = Pattern.matches("^[A-Za-z][A-Za-z0-9_]*$", this.getName());
        // only contains English alphabet, numbers and underline, and must start with alphabet.
        if (isIllegal) {
        	// check whether name has been defined.
            for (String keyword: TranslateSystem.keywords) {
                if (keyword.equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Array '" + this.getName() + "' is NOT valid on name.");
                    break;
                }
            }

            for (Variable v: TranslateSystem.variables) {
                if (v.getName().equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Array '" + this.getName() + "' has been defined!");
                    break;
                }
            }

            for (Array a: TranslateSystem.arrays) {
                if (a.getName().equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Array '" + this.getName() + "' has been defined!");
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
        	// check whether name has been defined.
            for (String keyword: TranslateSystem.keywords) {
                if (keyword.equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Array '" + this.getName() + "' is NOT valid on name.");
                    break;
                }
            }

            for (Variable v: variables) {
                if (v.getName().equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Array '" + this.getName() + "' has been defined!");
                    break;
                }
            }

            for (Array a: arrays) {
                if (a.getName().equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Array '" + this.getName() + "' has been defined!");
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
        System.out.println("Array name: " + this.getName());
        boolean isValid = false;
        if (this.getDataType().equalsIgnoreCase("string") || this.getDataType().equalsIgnoreCase("boolean")
                || this.getDataType().equalsIgnoreCase("number") || this.getDataType().equalsIgnoreCase("array")) {
            isValid = true;
        }
        return isValid;
    }

    public int getTotalCapacity() {
    	// length == "x,x"
        String rowCol[] = this.length.split(",");
        int rowLength = 0, colLength = 0;
        try {
            rowLength = Integer.parseInt(rowCol[0]);
            colLength = Integer.parseInt(rowCol[1]);
        } catch (NumberFormatException e) {
            System.out.println(">>>Error occurred in defining arrays.");
            return -1;
        }

        return rowLength * colLength;
    }

    public String determineTrueDataType() {
        String datatype = "";
        if (this.dataType.equalsIgnoreCase("String")) {
            return "String";
        } else if (this.dataType.equalsIgnoreCase("Boolean")) {
            if (this.values.size() == 0) {
                return "boolean";
            } else {
                for (Map.Entry<String, String> v: values.entrySet()) {
                    if (!(v.getValue().equalsIgnoreCase("true") || v.getValue().equalsIgnoreCase("false"))) {
                        System.out.println(">>>Error data type in element of array for boolean.");
                        return "ERROR";
                    } else {
                        datatype = "boolean";
                    }
                }
            }
        } else if (this.dataType.equalsIgnoreCase("Number")) {
            if (this.values.size() == 0) {
                if (TranslateSystem.intMode) {
                    return "int";
                } else {
                    return "double";
                }
            } else {
                for (Map.Entry<String, String> v: values.entrySet()) {
                    try {
                        Integer.parseInt(v.getValue());
                        System.out.println(">>>A int exist in Array.");
                        if (!datatype.equals("double")) {
                            datatype = "int";
                        }
                    } catch (NumberFormatException intException) {
                        try {
                            Double.parseDouble(v.getValue());
                            System.out.println(">>>A double exist in Array.");
                            datatype = "double";
                        } catch (NumberFormatException doubleException) {
                            // NaN.
                            System.out.println(">>>Non-number exist in Array.");
                            return "ERROR";
                        }
                    }
                }
            }

        } else {
            System.out.println(">>>Error Data type of Array.");
            return "ERROR";
        }
        if (datatype.equals("double") && TranslateSystem.intMode) {
            return "ERROR";
        }

        return datatype;
    }

    public boolean addValue(String value) {
        int capacity = this.getTotalCapacity();
        if (this.values.size() >= capacity) {
            // Array is full.
            return false;
        } else {
            // String.
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            String pos = String.valueOf(currentRow) + "," + String.valueOf(currentCol);
            this.values.put(pos, value);
            currentCol ++;
            return true;
        }
    }

    public boolean addValueByAddInto(String value) {
        if (this.currentRow == Integer.parseInt(this.length.split(",")[0])) {
            // Array is full.
            return false;
        }
        if (this.currentCol == Integer.parseInt(this.length.split(",")[1])) {
            this.currentCol = 0;
            this.currentRow ++;
        }

        System.out.println(">>>Current value: " + value);
        int capacity = this.getTotalCapacity();
        if (this.values.size() >= capacity) {
            // Array is full.
            return false;
        } else {
            if (this.trueDataType.equalsIgnoreCase("boolean")) {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    String pos = String.valueOf(this.currentRow) + "," + String.valueOf(this.currentCol);
                    this.values.put(pos, value);
                    this.currentCol ++;
                } else {
                    System.out.println(">>>Wrong on adding value, data type collision.");
                    return false;
                }
            } else if (this.trueDataType.equalsIgnoreCase("int")) {
                try {
                    Integer.parseInt(value);
                    String pos = String.valueOf(this.currentRow) + "," + String.valueOf(this.currentCol);
                    this.values.put(pos, value);
                    this.currentCol ++;
                } catch (NumberFormatException e) {
                    System.out.println(">>>Wrong on adding value, data type collision.");
                    return false;
                }

            } else if (this.trueDataType.equalsIgnoreCase("double")) {
                try {
                    Double.parseDouble(value);
                    String pos = String.valueOf(this.currentRow) + "," + String.valueOf(this.currentCol);
                    this.values.put(pos, value);
                    this.currentCol ++;
                } catch (NumberFormatException e) {
                    System.out.println(">>>Wrong on adding value, data type collision.");
                    return false;
                }
            } else {
                // String.
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                String pos = String.valueOf(this.currentRow) + "," + String.valueOf(this.currentCol);
                this.values.put(pos, value);
                this.currentCol ++;
            }

            return true;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getDataType() {
        return this.dataType;
    }

    public void setTrueDataType(String trueDataType) {
        this.trueDataType = trueDataType;
    }

    public String getTrueDataType() {
        return this.trueDataType;
    }

    public String getLength() {
        return length;
    }

    public LinkedHashMap<String, String> getValues() {
        return values;
    }

    public int getCurrentCol() {
        return this.currentCol;
    }

    public int getCurrentRow() {
        return this.currentRow;
    }

    public int getCurrentColMinus1() {
        return this.currentCol - 1;
    }

    public void nextRow() {
        this.currentRow ++;
    }

    public String getArrayValue(String language) {
    	// output the translated list of array.
        String value = "{";
        int index = 1;
        for (Map.Entry<String, String> e: this.getValues().entrySet()) {

            if (this.getTrueDataType().equalsIgnoreCase("String")) {
                value += "\"";
            }
            if (this.getTrueDataType().equalsIgnoreCase("boolean") && language.equals("C") && e.getValue().equalsIgnoreCase("true")) {
                value += "1";
            } else if (this.getTrueDataType().equalsIgnoreCase("boolean") && language.equals("C") && e.getValue().equalsIgnoreCase("false")) {
                value += "0";
            } else {
                value += e.getValue();
            }
            if (this.getTrueDataType().equalsIgnoreCase("String")) {
                value += "\"";
            }
            if (index != this.getValues().size()) {
                value += ", ";
            }
            index ++;
        }
        value += "}";

        return value;
    }

    public String getArrayValue2D(String language) {
    	// output the translated list of 2D array.
        String rowCol[] = this.length.split(",");
        int rowLength = Integer.parseInt(rowCol[0]);
        int colLength = Integer.parseInt(rowCol[1]);

        String value = "{ {";
        int colIndex = 1, rowIndex = 1;
        for (Map.Entry<String, String> e: this.getValues().entrySet()) {
            if (this.getTrueDataType().equalsIgnoreCase("String")) {
                value += "\"";
            }
            if (this.getTrueDataType().equalsIgnoreCase("boolean") && language.equals("C") && e.getValue().equalsIgnoreCase("true")) {
                value += "1";
            } else if (this.getTrueDataType().equalsIgnoreCase("boolean") && language.equals("C") && e.getValue().equalsIgnoreCase("false")) {
                value += "0";
            } else {
                value += e.getValue();
            }
            if (this.getTrueDataType().equalsIgnoreCase("String")) {
                value += "\"";
            }

            if (colIndex != colLength) {
                value += ", ";
            } else if (colIndex == colLength && rowIndex != rowLength) {
                value += "}, {";
                rowIndex ++;
            } else if (colIndex == colLength && rowIndex == rowLength) {
                value += "}";
            }
            colIndex ++;
            if (colIndex > colLength) {
                colIndex = 1;
            }
        }
        value += " }";

        return value;
    }

    public boolean is2DArray() {
        return !this.length.startsWith("1,");
    }

}
