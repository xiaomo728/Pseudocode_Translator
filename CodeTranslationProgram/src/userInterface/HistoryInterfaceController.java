
package userInterface;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import translateProgram.SystemEntrance;
import java.util.ArrayList;

public class HistoryInterfaceController {
	// Controller of history recording interface.
    @FXML
    private TextArea h1;
    @FXML
    private TextArea h2;
    @FXML
    private TextArea h3;
    @FXML
    private TextArea h4;
    @FXML
    private TextArea h5;
    @FXML
    private TextArea h6;
    @FXML
    private TextArea h7;
    @FXML
    private TextArea h8;
    @FXML
    private TextArea h9;
    @FXML
    private TextArea h10;
    @FXML
    private Button clearHistoryBtn;
    @FXML
    private Button displayBtn;

    public static String chosenHistory;
    public static boolean isDisplay;

    private Stage historyStage;

    public HistoryInterfaceController() {
        chosenHistory = "";
        isDisplay = false;
    }

    public void setHistoryStage (Stage stage) {
        this.historyStage = stage;
    }

    public void setHistoryRecords() {
        ArrayList<String> records = SystemEntrance.recentRecordByRecordingSystem();
        if (records != null) {
            for (String record: records) {
                if (h1.getText().equals("")) {
                    h1.setText(record);
                } else if (h2.getText().equals("")) {
                    h2.setText(record);
                } else if (h3.getText().equals("")) {
                    h3.setText(record);
                } else if (h4.getText().equals("")) {
                    h4.setText(record);
                } else if (h5.getText().equals("")) {
                    h5.setText(record);
                } else if (h6.getText().equals("")) {
                    h6.setText(record);
                } else if (h7.getText().equals("")) {
                    h7.setText(record);
                } else if (h8.getText().equals("")) {
                    h8.setText(record);
                } else if (h9.getText().equals("")) {
                    h9.setText(record);
                } else if (h10.getText().equals("")) {
                    h10.setText(record);
                }
            }
        } else {
        	// no records.
            h1.setText("");
            h2.setText("");
            h3.setText("");
            h4.setText("");
            h5.setText("");
            h6.setText("");
            h7.setText("");
            h8.setText("");
            h9.setText("");
            h10.setText("");
            chosenHistory = "";
        }

        if (h1.getText().equals("")) {
            displayBtn.setDisable(true);
        } else {
            chosenHistory = h1.getText();
            displayBtn.setDisable(false);
        }
    }

    public void onDisplayBtnClick() {
        isDisplay = true;
        String historyArray[] = chosenHistory.split("(\\r\\n|\\r|\\n|\\n\\r)");
        System.out.println(">>> User choose to display the history at " + historyArray[0]);
        String history = "";
        for (int i = 1; i < historyArray.length; i++) {
            history += historyArray[i] + "\r\n";
        }
        chosenHistory = history;
        this.historyStage.close(); // listener -> close.
    }

    public void onClearHistoryBtnClick() {
        SystemEntrance.clearHistory();
        setHistoryRecords();
    }

    // on any text area click, copy the contents.
    public void onH1Click() {
        chosenHistory = h1.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public void onH2Click() {
        chosenHistory = h2.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public void onH3Click() {
        chosenHistory = h3.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public void onH4Click() {
        chosenHistory = h4.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public void onH5Click() {
        chosenHistory = h5.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public void onH6Click() {
        chosenHistory = h6.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public void onH7Click() {
        chosenHistory = h7.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public void onH8Click() {
        chosenHistory = h8.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public void onH9Click() {
        chosenHistory = h9.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public void onH10Click() {
        chosenHistory = h10.getText();
        if (!chosenHistory.equals("")) {
            displayBtn.setDisable(false);
        } else {
            displayBtn.setDisable(true);
        }
    }

    public Button getDisplayBtn() {
        return this.displayBtn;
    }

}
