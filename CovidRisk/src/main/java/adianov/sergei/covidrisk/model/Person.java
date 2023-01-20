package adianov.sergei.covidrisk.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


import java.time.LocalDate;

public class Person {
    private final StringProperty fio;
    private final ObjectProperty<LocalDate> birthday;

    public Person() {
        this(null);
    }

    public Person(String fio){
        this.fio = new SimpleStringProperty(fio);

        //dummy inf
        this.birthday = new SimpleObjectProperty<LocalDate>(LocalDate.of(1999,12,11));

    }

    public String getFio() {
        return fio.get();
    }

    public void setFio(String fio){
        this.fio.set(String.valueOf(fio));
    }

    public StringProperty fioProperty() {
        return fio;
    }


    public LocalDate getBirthday() {
        return birthday.get();
    }

    public void setBirthday(LocalDate birthday){
        this.birthday.set(birthday);
    }

    public ObjectProperty<LocalDate> birthdayProperty() {
        return birthday;
    }

}
