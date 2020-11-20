
package userInterface;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;
import translateProgram.SystemEntrance;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;


public class Main extends Application {
	// Main class of the project.
    private Stage primaryStage;
    private AnchorPane pane;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Pseudo-Code Translator");
        this.primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));

        initializeWindow();
    }

    // initialize main page.
    public void initializeWindow() {
    	// will not show in user interface. program information.
        System.out.println("********** Beginning of the Program. **********");
        System.out.println("Project: COMP390 From pseudo-code to code");
        System.out.println("         Developed by Lmo");
        System.out.println("Code-Translation System\nVersion 3.0.3");
        System.out.println("Last update: 2020/04/06 00:30");
        System.out.println("\n>>> Initializing... (0/4)");

        try {
            FXMLLoader programLoader = new FXMLLoader(getClass().getResource("mainInterface.fxml"));
            this.pane = (AnchorPane) programLoader.load();
            this.primaryStage.setScene(new Scene(pane));
            this.primaryStage.setResizable(false);

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            this.primaryStage.setX(bounds.getMinX() + 300);
            this.primaryStage.setY(bounds.getMinY() + 100);
            this.primaryStage.setWidth(1260);
            this.primaryStage.setHeight(720);
            //this.primaryStage.setWidth(bounds.getWidth() / 2);
            //this.primaryStage.setHeight(bounds.getHeight() / 1.5);
            //primaryStage.setFullScreen(true);
            this.primaryStage.show();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Exit System");
                    alert.setHeaderText("Are you sure to exit the program?");
                    alert.setContentText("The program will lose your unsaved data.");
                    Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    alertStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
                    
                    ButtonType exit = new ButtonType("Exit", ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(exit, cancel);
                    
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == exit){
                        System.exit(0);
                    } else {
                        event.consume();
                    }
                }
            });

            MainInterfaceController mic = programLoader.getController();
            mic.setPrimaryStage(this.primaryStage);

            System.out.println(">>> Initialize the program main interface successful... (1/4)");

            SystemEntrance.initialize();
            System.out.println(">>> User can use the program now.");
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            System.out.println(">>> [Date] " + sdf.format(date) + "\n");
            System.out.println("********** Waiting for User Action. **********");

        } catch (IOException e) {
            System.out.println(">>> Initialize the program failed.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args); // start of the whole program.
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }
}

