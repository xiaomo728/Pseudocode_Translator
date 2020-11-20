
package userInterface;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import translateProgram.SystemEntrance;
import translateUnits.Function;
import translateUnits.Line;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class MainInterfaceController {
	// Controller class for main interface.
    @FXML
    private Button instructionBtn;
    @FXML
    private Button translateBtn;
    @FXML
    private Button copyBtn;
    @FXML
    private Button runBtn;
    @FXML
    private Button historyBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Button addFunctionBtn;
    @FXML
    private ComboBox codeTypeCb;
    @FXML
    private TextArea pseudoCodeTxt;
    @FXML
    private TextArea codeTxt;
    @FXML
    private Label infoLabel;
    @FXML
    private Button functionList;
    @FXML
    private Button helpRunBtn;
    @FXML
    private CheckBox intMode;

    private Stage primaryStage;

    public static ArrayList<Function> functions;

    public MainInterfaceController() {
        functions = new ArrayList<Function>();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // when open the history window, we need to lock the main page.
    public void setLock(boolean lock) {
        if (lock) {
            instructionBtn.setDisable(true);
            translateBtn.setDisable(true);
            copyBtn.setDisable(true);
            historyBtn.setDisable(true);
            clearBtn.setDisable(true);
            addFunctionBtn.setDisable(true);
            codeTypeCb.setDisable(true);
            pseudoCodeTxt.setEditable(false);
            codeTxt.setEditable(false);
            functionList.setDisable(true);
            helpRunBtn.setDisable(true);
            intMode.setDisable(true);
        } else {
            instructionBtn.setDisable(false);
            translateBtn.setDisable(false);
            copyBtn.setDisable(false);
            historyBtn.setDisable(false);
            clearBtn.setDisable(false);
            addFunctionBtn.setDisable(false);
            codeTypeCb.setDisable(false);
            pseudoCodeTxt.setEditable(true);
            codeTxt.setEditable(true);
            functionList.setDisable(false);
            helpRunBtn.setDisable(false);
            intMode.setDisable(false);
        }
    }

    public void onTranslateBtnClick(ActionEvent event) {
        System.out.println(">>> [Action] User click [translate] button.");
        infoLabel.setText("[SYSTEM] Translating...");
        infoLabel.setTextFill(Color.GREEN);

        codeTxt.setText(""); // clear

        String language = String.valueOf(codeTypeCb.getValue());
        if (language.equals("Java") || language.equals("C")) {

            System.out.println(">>> Select to translate to " + language);
            String pseudoCode = pseudoCodeTxt.getText();
            ArrayList<String> translatedCode = new ArrayList<String>();
            boolean hasTranslated = false;

            if (pseudoCode.replace(" ", "").equals("")) {
                System.out.println(">>> User has input nothing but click translate.");
                infoLabel.setText("[WARNING] No pseudo-code sentences have been input!");
                infoLabel.setTextFill(Color.YELLOW);
                runBtn.setDisable(true);

            } else {
                System.out.println(">>> User has input as following:\n" + pseudoCode);
                translatedCode = SystemEntrance.startTranslateSystem(pseudoCode, functions, language, intMode.isSelected());
                // send to system entrance.
                hasTranslated = true;
            }
            
            // if all validation pass
            if (hasTranslated) {
                System.out.println(">>> Receive the translation result.");
                String lastLine = "";
                if (translatedCode.size() != 0) {
                    lastLine = translatedCode.get(translatedCode.size() - 1);
                } else {
                    infoLabel.setText("[ERROR] Unknown error occurred in program. Please contact the developer to get help.");
                    infoLabel.setTextFill(Color.RED);
                }

                if (lastLine.startsWith("[ERROR]")) {
                    String errorMsg[] = lastLine.split(",");
                    infoLabel.setText("[ERROR] Syntax error in Line " + errorMsg[1] + ": " + errorMsg[2]);
                    infoLabel.setTextFill(Color.RED);
                    runBtn.setDisable(true);

                } else if (lastLine.startsWith("[ERROR|function")) {
                    String errorMsg[] = lastLine.split(",");
                    String function = errorMsg[0].split("\\|")[2];
                    infoLabel.setText("[ERROR] Syntax error in Line " + errorMsg[1] + " of Function " + function + "(): " + errorMsg[2]);
                    infoLabel.setTextFill(Color.RED);
                    runBtn.setDisable(true);

                } else {
                    String codeResult = "";
                    for (String t: translatedCode) {
                        codeResult += t + "\r\n";
                    }
                    codeTxt.setText(codeResult);
                    infoLabel.setText("[SYSTEM] Translate program running successful.");
                    infoLabel.setTextFill(Color.GREEN);

                    System.out.println(">>> Translate System has worked successful, now recording system will record translation.");
                    Date date = new Date();
                    SystemEntrance.recordToRecordingSystem(date, pseudoCode, functions); // comm to record system
                    if (language.equals("Java")) {
                        runBtn.setDisable(false);
                    } else {
                        runBtn.setDisable(true);
                    }
                }
            }

        } else {
            System.out.println(">>> User does not select a language.");
            infoLabel.setText("[WARNING] Please select a language that you want to translate to!");
            infoLabel.setTextFill(Color.YELLOW);
            runBtn.setDisable(true);
        }
    }

    public void onInstructionBtnClick(ActionEvent event) {
    	// open the instruction.
        System.out.println(">>> [Action] User click [syntax instruction] button.");
        infoLabel.setText("[SYSTEM] Open the instruction guide...");
        infoLabel.setTextFill(Color.GREEN);
        try {
            Desktop.getDesktop().open(new File("resources/files/instruction.pdf"));
            System.out.println(">>> Open instruction successful.");
        } catch (IOException e) {
            System.out.println(">>> Open instruction failed.");
            infoLabel.setText("[ERROR] Failed to open instruction. Please contact the developer to get help.");
            infoLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    public void onCopyBtnClick(ActionEvent event) {
    	// copy result.
        System.out.println(">>> [Action] User click [copy] button.");
        String contents = codeTxt.getText();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = new StringSelection(contents);
        clipboard.setContents(trans, null);

        System.out.println(">>> Contents have been copied.");
        infoLabel.setText("[SYSTEM] Copy the code contents successful.");
        infoLabel.setTextFill(Color.GREEN);
    }

    public void onHelpRunBtnClick(ActionEvent event) {
    	// get help.
        System.out.println(">>> [Action] User click [help] label.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Code Execution System");
        alert.setHeaderText("Run the translated code in [Java] compilers.");
        alert.setContentText("Please confirm that you computer has installed JVM environment correctly before running, or the running may be failed.\n\n" +
                "Also, it may occur runtime errors or some errors that the translate system doesn't check before running (e.g., null pointer, array bound extend, " +
                "function parameters or return value error, function not exist in non-strict translation, etc.) and make the running failed. " +
                "If occurs, please check the error message from the command window." +
                "So far, system (ver.3.0.0) does not support to run C code.");
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(ok);
        Optional<ButtonType> result = alert.showAndWait();
    }

    public void onClearBtnClick(ActionEvent event) {
    	// clear the contents in text area.
        System.out.println(">>> [Action] User click [clear all] button.");
        pseudoCodeTxt.setText("");
        codeTxt.setText("");

        functions = new ArrayList<Function>();

        System.out.println(">>> Contents have been cleared.");
        infoLabel.setText("[SYSTEM] Clear the pseudo-code contents.");
        infoLabel.setTextFill(Color.GREEN);
    }

    public void onHistoryBtnClick(ActionEvent event) {
    	// open history recording system.
        System.out.println(">>> [Action] User click [history record] button.");
        FXMLLoader programLoader = new FXMLLoader(getClass().getResource("historyInterface.fxml"));
        AnchorPane pane = new AnchorPane();
        try {
            pane = (AnchorPane) programLoader.load();
            System.out.println(">>> Open history record successful.");
            infoLabel.setText("[SYSTEM] Open history record system successful.");
            infoLabel.setTextFill(Color.GREEN);
        } catch (IOException e) {
            System.out.println(">>> Open history record failed.");
            infoLabel.setText("[ERROR] Failed to open history record. Please contact the developer to get help.");
            infoLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }

        Stage historyStage = new Stage();
        historyStage.setTitle("History Records");
        historyStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        historyStage.setScene(new Scene(pane));
        historyStage.setResizable(false);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        historyStage.setX(bounds.getMinX() + 500);
        historyStage.setY(bounds.getMinY() + 250);
        historyStage.setWidth(600);
        historyStage.setHeight(540);
        //primaryStage.setFullScreen(true);

        historyStage.show();
        HistoryInterfaceController hic = programLoader.getController();
        hic.setHistoryRecords();
        hic.setHistoryStage(historyStage);
        setLock(true);

        historyStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                setLock(false);
            }
        });

        // windows has been closed by click "display".
        historyStage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                setLock(false);
                if (HistoryInterfaceController.isDisplay) {
                    pseudoCodeTxt.setText("");
                    functions = new ArrayList<Function>();
                    System.out.println(">>> Chosen History:\n" + HistoryInterfaceController.chosenHistory);
                 
                    String displayInMain = "";
                    // load functions.
                    String funcName = "", funcParam = "", funcContent = "", funcRet = "";
                    boolean functionLines = false;
                    for (String line: HistoryInterfaceController.chosenHistory.split("(\r\n|\r|\n|\n\r)")) {
                        System.out.println(">>> History line at [" + line + "]");
                        if (line.trim().startsWith("DEFINE FUNCTION")) {
                            Line defineFunc = new Line(line);
                            funcName = defineFunc.component(3).trim();
                            funcParam = defineFunc.component(6, -1).trim();
                            if (funcParam.equals("null")) funcParam = "";
                            functionLines = true;
                        } else if (line.trim().startsWith("END DEFINE AND RETURN")) {
                            Line defineFunc = new Line(line);
                            funcRet = defineFunc.component(5, -1).trim();
                            if (funcRet.equals(Line.ERROR_MSG)) funcRet = "";
                            AddFunctionInterfaceController afic = new AddFunctionInterfaceController();
                            System.out.println(">>> Find function with: funcName -" + funcName + "; funcParam -" + funcParam + "; funcRet -" + funcRet + "\r\n" + funcContent);
                            boolean addFuncSuccess = afic.asNewFunction(funcName, funcParam, funcContent, funcRet, false);
                            if (addFuncSuccess) {
                                System.out.println(">>> Recorded Function " + funcName + " Added");
                                funcName = ""; funcParam = ""; funcContent = ""; funcRet = "";
                            } else {
                                System.out.println(">>> Error in recorded function, record may be damaged.");
                            }
                        } else {
                            if (!functionLines) {
                                displayInMain += line + "\r\n";
                            } else {
                                if (!line.replace(" ", "").equals("")) {
                                    funcContent += line + "\r\n";
                                }
                            }
                        }


                    }
                    pseudoCodeTxt.setText(displayInMain);
                    codeTxt.setText("");
                    infoLabel.setText("[SYSTEM] Display history record successful.");
                    infoLabel.setTextFill(Color.GREEN);
                }
            }
        });
    }

    public void onAddFunctionBtnClick(ActionEvent event) {
    	// open window to add functions.
        System.out.println(">>> [Action] User click [+ add function] button.");
        FXMLLoader programLoader = new FXMLLoader(getClass().getResource("addFunctionInterface.fxml"));
        AnchorPane pane = new AnchorPane();
        try {
            pane = (AnchorPane) programLoader.load();
            System.out.println(">>> Open + add function window successful.");
            infoLabel.setText("[SYSTEM] Add function into pseudo-code.");
            infoLabel.setTextFill(Color.GREEN);
        } catch (IOException e) {
            System.out.println(">>> Open + add function window failed.");
            infoLabel.setText("[ERROR] Failed to open function addition window. Please contact the developer to get help.");
            infoLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }

        Stage functionStage = new Stage();
        functionStage.setTitle("Add Function by Pseudo-code");
        functionStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        functionStage.setScene(new Scene(pane));
        functionStage.setResizable(false);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        functionStage.setX(bounds.getMinX() + 500);
        functionStage.setY(bounds.getMinY() + 250);
        functionStage.setWidth(800);
        functionStage.setHeight(560);
        //primaryStage.setFullScreen(true);

        functionStage.show();
        AddFunctionInterfaceController afic = programLoader.getController();
        afic.setFunctionStage(functionStage);
        setLock(true);

        functionStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                setLock(false);
                infoLabel.setText("[SYSTEM] Add function window has been closed.");
                infoLabel.setTextFill(Color.GREEN);
            }
        });

        functionStage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                setLock(false);
                if (AddFunctionInterfaceController.addFunction) {
                    infoLabel.setText("[SYSTEM] Add new function successful, you can check the function in the left list.");
                    infoLabel.setTextFill(Color.GREEN);
                }
            }
        });
    }

    public void onFunctionListBtnClick(ActionEvent event) {
    	// open window to check function list.
        System.out.println(">>> [Action] User click [current function list] button.");
        if (functions.size() == 0) {
        	infoLabel.setText("[WARNING] No functions have been added into current pseudo-code now!");
            infoLabel.setTextFill(Color.YELLOW);
        } else {
            FXMLLoader programLoader = new FXMLLoader(getClass().getResource("functionListInterface.fxml"));
            AnchorPane pane = new AnchorPane();
            try {
                pane = (AnchorPane) programLoader.load();
                System.out.println(">>> Open function list window successful.");
                infoLabel.setText("[SYSTEM] Check function of current pseudo-code.");
                infoLabel.setTextFill(Color.GREEN);
            } catch (IOException e) {
                System.out.println(">>> Open function list window failed.");
                infoLabel.setText("[ERROR] Failed to open function list window. Please contact the developer to get help.");
                infoLabel.setTextFill(Color.RED);
                e.printStackTrace();
            }

            Stage listStage = new Stage();
            listStage.setTitle("Check Function Details");
            listStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
            listStage.setScene(new Scene(pane));
            listStage.setResizable(false);

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            listStage.setX(bounds.getMinX() + 280);
            listStage.setY(bounds.getMinY() + 400);
            listStage.setWidth(800);
            listStage.setHeight(560);
            //primaryStage.setFullScreen(true);

            listStage.show();
            functionList.setDisable(true);

            listStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    functionList.setDisable(false);
                    infoLabel.setText("");
                }
            });
        }
    }

    public void onIntModeClick(ActionEvent event) {
    	// open/close int mode.
        System.out.println(">>> [Action] User click [math integer mode] checkbox.");
        if (intMode.isSelected()) {
            infoLabel.setText("[SYSTEM] Current Translation Mode: All Number to Integer.");
            if (functions.size() != 0) {
                for (Function f: functions) {
                    if (f.hasParameter()) {
                        for (Map.Entry<String, String> p: f.getParameters().entrySet()) {
                            if (p.getValue().equals("double")) {
                                p.setValue("int");
                            } else if (p.getValue().equals("double[]")) {
                                p.setValue("int[]");
                            }
                        }
                    }
                }
            }
        } else {
            infoLabel.setText("[SYSTEM] Current Translation Mode: Classic Number Translation.");
            if (functions.size() != 0) {
                for (Function f: functions) {
                    if (f.hasParameter()) {
                        for (Map.Entry<String, String> p: f.getParameters().entrySet()) {
                            if (p.getValue().equals("int")) {
                                p.setValue("double");
                            } else if (p.getValue().equals("int[]")) {
                                p.setValue("double[]");
                            }
                        }
                    }
                }
            }
        }
        infoLabel.setTextFill(Color.GREEN);
    }

    public void onRunCodeBtnClick(ActionEvent event) {
    	// run code.
        System.out.println(">>> [Action] User click [run the code] button.");
        String code = codeTxt.getText();
        if (code.equals("")) {
            System.out.println(">>> User has translated nothing but click run code.");
            infoLabel.setText("[WARNING] No (Java) code sentences have been translated, cannot run the code now!");
            infoLabel.setTextFill(Color.YELLOW);

        } else {
            System.out.println("[SYSTEM] Check if type of code language is Java");
            String language = "";
            if (code.startsWith("import") || code.startsWith("public")) {
                language = "Java";
            } else if (code.startsWith("#include")) {
                infoLabel.setText("[ERROR] Sorry, version 3.0.0 system is not supported to run C code.");
                infoLabel.setTextFill(Color.YELLOW);
            } else {
                infoLabel.setText("[ERROR] System cannot recognize the language of translation result. Please contact the developer to find help.");
                infoLabel.setTextFill(Color.RED);
            }

            if (language.equals("Java")) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Code Execution System");
                alert.setHeaderText("System will try to run the following codes in Java compiler...");
                alert.setContentText(code + "\n\n");
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));

                ButtonType Ok = new ButtonType("Confirm, run it in Command.");
                ButtonType NotOk = new ButtonType("Stop! I don't want to run it.", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(Ok, NotOk);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == Ok){
                    System.out.println("[SYSTEM] Start to run the code in Java!");
                    infoLabel.setText("[SYSTEM] Code is running...");
                    infoLabel.setTextFill(Color.GREEN);

                    boolean successful = SystemEntrance.runCodesByCodeExecutionSystem(code);
                    if (successful) {
                        infoLabel.setText("[SYSTEM] Code run process successful. Please check the information on command window.");
                        infoLabel.setTextFill(Color.GREEN);
                    } else {
                        infoLabel.setText("[ERROR] Code run process failed dut to unexpected error. Please contact the developer to find help.");
                        infoLabel.setTextFill(Color.RED);
                    }

                } else {
                    System.out.println(">>> User stop the running progress.");
                    event.consume();
                }
            }
        }
    }
}
