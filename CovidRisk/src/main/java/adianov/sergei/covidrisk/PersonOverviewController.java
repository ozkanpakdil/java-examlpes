package adianov.sergei.covidrisk;

import adianov.sergei.covidrisk.model.Person;
import adianov.sergei.covidrisk.util.DateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;

public class PersonOverviewController {

    @FXML
    private TableView<Person> personTable;
    @FXML
    private TableColumn<Person, String> fioColumn;

    @FXML
    private Label fioLabel;
    @FXML
    Label birthdayLabel;

    private MainApp mainApp;

    public PersonOverviewController() {
    }

    @FXML
    private void initialize() {
        fioColumn.setCellValueFactory(cellData -> cellData.getValue().fioProperty());

        showPersonDetails(null);

        personTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPersonDetails(newValue)
        );
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        personTable.setItems(mainApp.getPersonData());
    }

    private void showPersonDetails(Person person) {
        if (person != null) {
            fioLabel.setText(person.getFio());
            birthdayLabel.setText(DateUtil.format(person.getBirthday()));
        } else {
            fioLabel.setText("");
            birthdayLabel.setText("");
        }
    }

    @FXML
    private void handleDeletePerson() {
        int selectedIndex = personTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            personTable.getItems().remove(selectedIndex);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("Ничего не выбрано!");
            alert.setHeaderText("Пациент не выбран");
            alert.setContentText("Пожалуйста, выберите пациента из списка");

            alert.showAndWait();
        }
    }

    @FXML
    private void handleNewPerson(){
        Person tempPerson = new Person();
        boolean okClicked = mainApp.showPersonEditDialog(tempPerson);
        if(okClicked){
            mainApp.getPersonData().add(tempPerson);
        }
    }

    @FXML
    private void handleEditPerson(){
        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if(selectedPerson != null){
            boolean okClicked = mainApp.showPersonEditDialog(selectedPerson);
            if (okClicked){
                showPersonDetails(selectedPerson);
            }
            else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.initOwner(mainApp.getPrimaryStage());
                alert.setTitle("No Selection");
                alert.setHeaderText("No Person Selected");
                alert.setContentText("Please select a person in the table.");

                alert.showAndWait();
            }
        }
    }
}
