package adianov.sergei.covidrisk;

import adianov.sergei.covidrisk.model.Person;
import adianov.sergei.covidrisk.util.DateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PersonEditDialogController {
    @FXML
    private TextField fioTextField;
    @FXML TextField birthdayTextField;

    private Stage dialogStage;
    private Person person;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    public void setPerson(Person person){
        this.person = person;

        fioTextField.setText(person.getFio());
        birthdayTextField.setText(DateUtil.format(person.getBirthday()));
        birthdayTextField.setPromptText("dd.mm.yyyy");
    }

    public boolean isOkClicked(){
        return okClicked;
    }

    @FXML
    private void handleOk(){
        if(isInputValid()){
           person.setFio(fioTextField.getText());
           person.setBirthday(DateUtil.parseDate(birthdayTextField.getText()));

           okClicked=true;
           dialogStage.close();
        }
    }

    @FXML
    private void handleCancel(){
        dialogStage.close();
    }

    private boolean isInputValid(){
        String errorMessage = "";

        if(fioTextField.getText() == null || fioTextField.getText().length()==0){
            errorMessage += "Не введено ФИО!\n";
        }
        if(birthdayTextField.getText() == null || birthdayTextField.getText().length() == 0){
            errorMessage += "Не введена дата рождения!\n";
        }
        else {
            if (!DateUtil.validDate((birthdayTextField.getText()))) {
                errorMessage += "Неверно введена дата рождения! Используйте формат дд.мм.гггг!\n";
            }
        }

        if(errorMessage.length() == 0){
            return true;
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Неправильные данные в полях!");
            alert.setHeaderText("Пожалуйста, исправьте некорректные данные");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}
