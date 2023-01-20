package adianov.sergei.covidrisk;

import java.io.File;
import java.io.IOException;
import adianov.sergei.covidrisk.model.Person;
import adianov.sergei.covidrisk.model.PersonListWrapper;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class MainApp extends Application {
    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<Person> personData = FXCollections.observableArrayList();

    public MainApp(){
        personData.add(new Person("Случайнов Поликарп Евгеньевич"));
        personData.add(new Person("Рандомов Александр Васильевич"));
    }

    public ObservableList<Person> getPersonData(){
        return personData;
    }

    @Override
    public void start(Stage primaryStage){
        this.primaryStage= primaryStage;
        this.primaryStage.setTitle("Расчёт риска заболевания COVID");

        initRootLayout();

        showPersonOverview();
    }

    public void initRootLayout(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        }



    public void showPersonOverview(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("PersonOverview.fxml"));
            AnchorPane personOverview = (AnchorPane) loader.load();

            rootLayout.setCenter(personOverview);

            PersonOverviewController controller = loader.getController();
            controller.setMainApp(this);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean showPersonEditDialog(Person person){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("PersonEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактировать данные");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            PersonEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        }
        catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args){
        launch();
    }

    public void loadPersonDataFromFile(File file){
        try{
            OpenWorkbook.Open();

        }
        catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    public void savePersonDataToFile(File file) {
        try {
            CreateWorkbook.Create();
            

            // Сохраняем путь к файлу в реестре.
        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());

            alert.showAndWait();
        }
    }
}

