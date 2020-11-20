
package translateUnits;

import programInterfaces.Identifier;
import translateProgram.TranslateSystem;
import userInterface.MainInterfaceController;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class Function implements Identifier {
	// Object Function. 

    private String name;
    private String contents;
    private LinkedHashMap<String, String> parameters;
    private String originalParam;
    private String returnValue;

    // constructors.
    public Function() {

    }

    public Function (String name, String contents, String returnValue) {
        this.name = name.toLowerCase();
        this.contents = contents;
        this.parameters = null;
        this.returnValue = returnValue;
    }

    public Function (String name, String contents, LinkedHashMap<String, String> parameters, String originalParam, String returnValue) {
        this.name = name.toLowerCase();
        this.contents = contents;
        this.parameters = parameters;
        this.originalParam = originalParam;
        this.returnValue = returnValue;
    }

    public boolean isValidName() {
        if (this.getName().equals(Line.ERROR_MSG)) return false;
        System.out.println("Function name: " + this.getName());
        boolean isValid = true;
        boolean isIllegal = Pattern.matches("^[A-Za-z][A-Za-z0-9_]*$", this.getName());
        // only contains English alphabet, numbers and underline, and must start with alphabet.
        if (isIllegal) {
            for (String keyword: TranslateSystem.keywords) {
                if (keyword.equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Function '" + this.getName() + "' is NOT valid on name.");
                    break;
                }
            }

            for (Function f: MainInterfaceController.functions) {
                if (f.getName().equalsIgnoreCase(this.getName())) {
                    isValid = false;
                    System.out.println("! Function '" + this.getName() + "' has been defined!");
                    break;
                }
            }

        } else {
            isValid = false;
        }

        return isValid;
    }

    public boolean isValidDataType() {
        return false;
    }

    public String determineTrueDataType() {
        return "";
    }

    public String getName() {
        return this.name;
    }

    public String getContents() {
        return this.contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public LinkedHashMap<String, String> getParameters() {
        return this.parameters;
    }

    public String getOriginalParam() {
        return this.originalParam;
    }

    public String getReturnValue() {
        return this.returnValue.trim();
    }

    public boolean hasParameter() {
        if (parameters == null) {
            return false;
        } else {
            return true;
        }
    }

    public int numOfParameter() {
        if (parameters == null) {
            return 0;
        } else {
            return parameters.size();
        }
    }
}
