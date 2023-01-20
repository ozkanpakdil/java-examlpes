module COVID.Risk {
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.apache.commons.codec;
    requires org.apache.commons.io;
    requires org.apache.poi.ooxml;
    opens adianov.sergei.covidrisk to javafx.fxml;

    exports adianov.sergei.covidrisk;
}