
package programInterfaces;

public interface Identifier {
	// Interface for translate units, Variable, Array and Function.
	
    public boolean isValidName();

    public boolean isValidDataType();

    public String determineTrueDataType();
}
