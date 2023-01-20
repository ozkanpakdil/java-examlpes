package adianov.sergei.covidrisk;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;

public class RootLayoutController {
    private MainApp mainApp;

    public void setMainApp(MainApp mainApp){
        this.mainApp = mainApp;
    }

    @FXML
    private void handleNew(){
        mainApp.getPersonData().clear();

    }

    @FXML
    private void handleOpen(){
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (.xml)","*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if(file != null){
            mainApp.loadPersonDataFromFile(file);
        }
    }

    @FXML
    private void handleSave(){



    }

    @FXML
    private void handleSaveAs(){
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            mainApp.savePersonDataToFile(file);
        }
    }

    @FXML
    private void handleAbout(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Расчёт риска заболевания COVID");
        alert.setHeaderText("Помощь");
        alert.setContentText("Автор: Адианов Сергей adianov.s.g@gmail.com");

        alert.showAndWait();
    }

    @FXML
    private void handleExit(){
        System.exit(0);
    }
}
