package adianov.sergei.covidrisk;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;

public class OpenWorkbook {
    public static void Open() throws Exception{
        File file = new File("workbook.xlsx");

        FileInputStream fIP = new FileInputStream(file);

        XSSFWorkbook workbook = new XSSFWorkbook(file);

        if(file.isFile()&&file.exists()){
            System.out.println("Sucess!");
        }
        else {
            System.out.println("Error!");
        }
    }
}
