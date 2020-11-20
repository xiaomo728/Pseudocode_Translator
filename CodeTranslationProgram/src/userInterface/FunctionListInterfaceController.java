
package userInterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import translateUnits.Function;

import java.util.ArrayList;
import java.util.Optional;

public class FunctionListInterfaceController {
	// Controller of function list display interface.
    @FXML
    private TextField functionName;
    @FXML
    private TextField parameters;
    @FXML
    private TextField returnValue;
    @FXML
    private TextArea functionBody;
    @FXML
    private ComboBox<String> functionList;
    @FXML
    private Button checkBtn;
    @FXML
    private Button saveBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Button deleteBtn;

    public FunctionListInterfaceController() {

    }

    public void onListClick() {
    	// refresh the function list.
        System.out.println("[Action] User click [list] to check functions.");
        System.out.println(">>> Here are " + MainInterfaceController.functions.size() + " function(s) in the pseudo-code now:");
        ArrayList<String> names = new ArrayList<String>();
        for (Function f: MainInterfaceController.functions) {
            names.add(f.getName());
            System.out.println(">>>     -- " + f.getName() + "()");
        }
        ObservableList<String> options = FXCollections.observableArrayList(names);
        functionList.setItems(options);
    }

    public void onCheckBtnClick(ActionEvent event) {
    	// click to check details.
        System.out.println("[Action] User click [check] button.");
        String funcName = functionList.getValue();
        if (funcName == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Check Function Details");
            warning.setHeaderText("You haven't chosen any function yet.");
            warning.setContentText("You can choose a function by open the left combo-box and then check its detail; if you cannot see any function in list, that means there has no saved functions in system now.\n\n" +
                                   "If you don't see the function you just added, click list combo-box again to refresh the list.");
            Stage alertStage = (Stage) warning.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
            ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            warning.getButtonTypes().setAll(ok);
            Optional<ButtonType> result = warning.showAndWait();

        } else {
            System.out.println(">>> Function choose: " + funcName);
            boolean hasFunction = false;
            for (Function f: MainInterfaceController.functions) {
                if (f.getName().equalsIgnoreCase(funcName)) {
                    functionName.setText(f.getName());
                    parameters.setText(f.getOriginalParam());
                    returnValue.setText(f.getReturnValue());
                    functionBody.setText(f.getContents());
                    hasFunction = true;
                    break;
                }
            }
            if (hasFunction) {
                functionBody.setEditable(true);
                clearBtn.setDisable(false);
                deleteBtn.setDisable(false);
                saveBtn.setDisable(false);

            } else {
                Alert warning = new Alert(Alert.AlertType.WARNING);
                warning.setTitle("Check Function Details");
                warning.setHeaderText("Cannot find this function in system's data.");
                warning.setContentText("The function --" + funcName + "() may have been deleted but system hasn't been refreshed, please choose to check other functions.");
                Stage alertStage = (Stage) warning.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
                ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                warning.getButtonTypes().setAll(ok);
                Optional<ButtonType> result = warning.showAndWait();
            }
        }
    }

    public void onClearBtnClick(ActionEvent event) {
    	// clear all contents in main body of function.
        System.out.println("[Action] User click [clear body] button.");
        functionBody.setText("");
        System.out.println(">>> Clear the body contents of function successful.");
    }

    public void onDeleteBtnClick(ActionEvent event) {
    	// delete current function.
        System.out.println("[Action] User click [delete] button.");
        String funcName = functionName.getText();
        boolean hasFunction = false;
        for (int i = 0; i < MainInterfaceController.functions.size(); i++) {
            if (MainInterfaceController.functions.get(i).getName().equalsIgnoreCase(funcName)) {
                MainInterfaceController.functions.remove(i);

                Alert warning = new Alert(Alert.AlertType.WARNING);
                warning.setTitle("Check Function Details");
                warning.setHeaderText("Function has been deleted.");
                warning.setContentText("The function --" + funcName + "() has been delete from system's data.");
                Stage alertStage = (Stage) warning.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
                ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                warning.getButtonTypes().setAll(ok);
                Optional<ButtonType> result = warning.showAndWait();

                functionName.setText("");
                parameters.setText("");
                returnValue.setText("");
                functionBody.setText("");
                hasFunction = true;
                break;
            }
        }
        if (hasFunction) {
            functionBody.setEditable(false);
            clearBtn.setDisable(true);
            deleteBtn.setDisable(true);
            saveBtn.setDisable(true);

            if (MainInterfaceController.functions.size() == 0) {
                Alert warning = new Alert(Alert.AlertType.WARNING);
                warning.setTitle("Check Function Details");
                warning.setHeaderText("All Function has been deleted.");
                warning.setContentText("Here are no functions in the current pseudo-code now.");
                Stage alertStage = (Stage) warning.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
                ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                warning.getButtonTypes().setAll(ok);
                Optional<ButtonType> result = warning.showAndWait();
            }

        } else {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Check Function Details");
            warning.setHeaderText("Cannot find this function in system's data.");
            warning.setContentText("The function --" + funcName + " may have been deleted but system hasn't been refreshed, please close the window to refresh.");
            Stage alertStage = (Stage) warning.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
            ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            warning.getButtonTypes().setAll(ok);
            Optional<ButtonType> result = warning.showAndWait();
        }
    }

    public void onSaveBtnClick(ActionEvent event) {
    	// save the change.
        System.out.println("[Action] User click [save] button.");
        String funcName = functionName.getText();
        boolean hasFunction = false;
        for (Function f: MainInterfaceController.functions) {
            if (f.getName().equalsIgnoreCase(funcName)) {
                String contents = functionBody.getText();
                if (contents.replace(" ","").equals("")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Check Function Details");
                    alert.setHeaderText("Function's body has no contents!");
                    alert.setContentText("Such the function will be meaningless. Please enter several body contents into this function and the save.");
                    Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
                    ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    alert.getButtonTypes().setAll(ok);
                    Optional<ButtonType> result = alert.showAndWait();

                } else {
                    saveBtn.setDisable(true);
                    f.setContents(contents);
                    hasFunction = true;
                }
                break;
            }
        }
        if (!hasFunction) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Check Function Details");
            warning.setHeaderText("Cannot find this function in system's data.");
            warning.setContentText("The function --" + funcName + " may have been deleted but system hasn't been refreshed; system cannot save the function currently, please close the window to refresh.");
            Stage alertStage = (Stage) warning.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
            ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            warning.getButtonTypes().setAll(ok);
            Optional<ButtonType> result = warning.showAndWait();
        }
    }

    public void onFunctionBodyClick() {
    	// enable the save btn
        if (functionBody.isEditable()) {
            saveBtn.setDisable(false);
        }
    }

}
