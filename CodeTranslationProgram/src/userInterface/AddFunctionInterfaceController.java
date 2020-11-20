
package userInterface;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import translateUnits.Array;
import translateUnits.Function;
import translateUnits.Line;
import translateUnits.Variable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

public class AddFunctionInterfaceController {
	// Controller of add funtion interface.
    @FXML
    private TextField functionName;
    @FXML
    private TextField parameters;
    @FXML
    private TextField returnValue;
    @FXML
    private TextArea functionBody;
    @FXML
    private Button helpBtn;
    @FXML
    private Button finishBtn;

    public static boolean addFunction;

    private Stage functionStage;

    public AddFunctionInterfaceController() {
        addFunction = false;
    }

    public void setFunctionStage (Stage stage) {
        this.functionStage = stage;
    }

    public void onHelpBtnClick(ActionEvent event) {
    	// open pop-up help window 
        System.out.println(">>> [Action] User click [help] label.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add function...");
        alert.setHeaderText("What should you fill in these text areas?");
        alert.setContentText("You must give a name for the function and some contents in its body, and other text areas are all alternative.\n\n" +
                "If the function you defined has no parameters or no return value, just let the corresponding text areas be blank.\n\n" +
                "Please pay more attention on your function's return value, the system will not check the correction of its use.");
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));

        Optional<ButtonType> result = alert.showAndWait();
    }

    public void onFinishBtnClick(ActionEvent event) {
    	// finish adding function.
        System.out.println(">>> [Action] User click [finish define] label.");
        String name = functionName.getText().trim();
        String parameter = parameters.getText().trim();
        String body = functionBody.getText().trim();
        String retValue = returnValue.getText().trim();
        asNewFunction(name, parameter, body, retValue, true);
    }


    private void errorMsg(String header, String content) {
    	// pop up window for reporting error.
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Add function...");
        alert.setHeaderText(header);
        alert.setContentText(content);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(ok);
        Optional<ButtonType> result = alert.showAndWait();
    }

    public boolean asNewFunction(String name, String parameter, String body, String retValue, boolean addFunc) {
        // replace the blank information.
        if (name.replace(" ", "").equals("")) name = "";
        if (parameter.replace(" ","").equals("")) parameter = "";
        if (body.replace(" ","").equals("")) body = "";
        if (retValue.replace(" ", "").equals("")) retValue = "";

        if (name.equals("")) {
            if (addFunc) errorMsg("Function name is NULL now!", "You must give a name for the function.");
            return false;

        } else {
            // deal with return value.
            if (!(retValue.equals("") || retValue.equalsIgnoreCase("NUMBER") || retValue.equalsIgnoreCase("STRING") || retValue.equalsIgnoreCase("BOOLEAN")
                    || retValue.equalsIgnoreCase("NUMBER ARRAY") || retValue.equalsIgnoreCase("STRING ARRAY") || retValue.equalsIgnoreCase("BOOLEAN ARRAY"))) {
                if (addFunc) errorMsg("Return value is invalid!", "You must define the return value with one of the following:\n\n" +
                                        "number / string / boolean / number array / string array / boolean array or no return value (with null)");
                return false;
            } else {
                if (body.toUpperCase().contains("RETURN ") && retValue.equals("")) {
                    if (addFunc) errorMsg("Unexpected return value in body.", "You does not define a return value type but declare a return statement in function's content. It is not valid for defining.");
                    return false;
                } else if (!body.toUpperCase().contains("RETURN ") && !retValue.equals("")) {
                    if (addFunc) errorMsg("Unexpected return type exists.", "You does not declare a return statement in function's content but define a return value type. It is not valid for defining.");
                    return false;
                }
            }

            if (body.equals("")) {
                if (addFunc) errorMsg("Function's body has no contents!", "Such the function will be meaningless. Please enter several body contents into this function.");
                return false;
            }
            // Has name.
            ArrayList<Variable> variables = new ArrayList<Variable>();
            ArrayList<Array> arrays = new ArrayList<Array>();

            Function f;
            LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
            if (parameter.equals("")) {
                f = new Function(name, body, retValue);
                if (f.isValidName()) {
                    MainInterfaceController.functions.add(f);
                    if (addFunc) {
                        addFunction = true;
                        this.functionStage.close();
                    }
                } else {
                    if (addFunc) errorMsg("Function name is invalid!", "Function name is illegal, please do not use keywords or defined name as function name.");
                    return false;
                }

            } else {
                if (!parameter.contains(",")) {
                    // only one parameter
                    Line param = new Line(parameter);
                    if (param.contain("AS") && param.componentOf("AS") == 2) {
                        String p = param.component(1).trim();
                        String dtp = param.component(param.componentOf("AS") + 1, -1).trim();
                        if (dtp.equalsIgnoreCase("NUMBER") || dtp.equalsIgnoreCase("STRING") || dtp.equalsIgnoreCase("BOOLEAN")) {
                            Variable v = new Variable(p, dtp, "", false);
                            if (v.isValidNameInFunction(variables, arrays)) {
                                params.put(p, v.getTrueDataType());
                                variables.add(v);
                                f = new Function(name, body, params, parameter, retValue);
                                if (f.isValidName()) {
                                    MainInterfaceController.functions.add(f);
                                    if (addFunc) {
                                        addFunction = true;
                                        this.functionStage.close();
                                    }
                                } else {
                                    if (addFunc) errorMsg("Function name is invalid!", "Function name is illegal, please do not use keywords or defined name as function name.");
                                    return false;
                                }

                            } else {
                                if (addFunc) errorMsg("Parameter's name is invalid!", "Parameter's name is illegal in this function, please check its expression or that not a keyword.");
                                return false;
                            }

                        } else if (dtp.equalsIgnoreCase("NUMBER ARRAY") || dtp.equalsIgnoreCase("STRING ARRAY") || dtp.equalsIgnoreCase("BOOLEAN ARRAY")) {
                            Array a = new Array(p, dtp.split(" ")[0], "1,1");
                            if (a.isValidNameInFunction(variables, arrays)) {
                                params.put(p, a.determineTrueDataType() + "[]");
                                arrays.add(a);
                                f = new Function(name, body, params, parameter, retValue);
                                if (f.isValidName()) {
                                    MainInterfaceController.functions.add(f);
                                    if (addFunc) {
                                        addFunction = true;
                                        this.functionStage.close();
                                    }
                                } else {
                                    if (addFunc) errorMsg("Function name is invalid!", "Function name is illegal, please do not use keywords or defined name as function name.");
                                    return false;
                                }

                            } else {
                                if (addFunc) errorMsg("Parameter's name is invalid!", "Parameter's name is illegal in this function, please check its expression or that not a keyword.");
                                return false;
                            }

                        } else {
                            if (addFunc) errorMsg("Function parameters has error!", "Data type of the parameter is invalid. You must define the data type with one of the following:\n\n" +
                                                    "number / string / boolean / number array / string array / boolean array or no parameters (with null)");
                            return false;
                        }

                    } else {
                        if (addFunc) errorMsg("Function parameters has error!", "System cannot understand the parameters that you has written. Please check the syntax of use and re-enter.");
                        return false;
                    }

                } else {
                    // multi-parameters
                    String[] ps = parameter.split(",");
                    for (String eachP: ps) {
                        Line param = new Line(eachP.trim());
                        if (param.contain("AS") && param.componentOf("AS") == 2) {
                            String p = param.component(1).trim();
                            String dtp = param.component(param.componentOf("AS") + 1, -1).trim();
                            if (dtp.equalsIgnoreCase("NUMBER") || dtp.equalsIgnoreCase("STRING") || dtp.equalsIgnoreCase("BOOLEAN")) {
                                Variable v = new Variable(p, dtp, "", false);
                                if (v.isValidNameInFunction(variables, arrays)) {
                                    params.put(p, v.getTrueDataType());
                                    variables.add(v);
                                } else {
                                    if (addFunc) errorMsg("Parameter's name is invalid!", "One of parameter's name is illegal in this function, please check its expression or if it has been multi-defined.");
                                    return false;
                                }

                            } else if (dtp.equalsIgnoreCase("NUMBER ARRAY") || dtp.equalsIgnoreCase("STRING ARRAY") || dtp.equalsIgnoreCase("BOOLEAN ARRAY")) {
                                Array a = new Array(p, dtp.split(" ")[0], "1,1");
                                if (a.isValidNameInFunction(variables, arrays)) {
                                    params.put(p, a.determineTrueDataType() + "[]");
                                    arrays.add(a);
                                } else {
                                    if (addFunc) errorMsg("Parameter's name is invalid!", "One of parameter's name is illegal in this function, please check its expression or if it has been multi-defined.");
                                    return false;
                                }

                            } else {
                                if (addFunc) errorMsg("Function parameters has error!", "Data type of the parameter is invalid. You must define the data type with one of the following:\n\n" +
                                                        "number / string / boolean / number array / string array / boolean array or no parameters (with null)");
                                return false;
                            }

                        } else {
                            if (addFunc) errorMsg("Function parameters has error!", "System cannot understand the parameters that you has written. Please check the syntax of use and re-enter.");
                            return false;
                        }
                    }
                    f = new Function(name, body, params, parameter, retValue);
                    if (f.isValidName()) {
                        MainInterfaceController.functions.add(f);
                        if (addFunc) {
                            addFunction = true;
                            this.functionStage.close();
                        }
                    } else {
                        if (addFunc) errorMsg("Function name is invalid!", "Function name is illegal, please do not use keywords or defined name as function name.");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
