package adianov.sergei.covidrisk;



import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

public class CreateWorkbook {
    public static void Create() throws Exception{
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFSheet sheet1 = workbook.createSheet("Данные");

        CellStyle cs = workbook.createCellStyle();
        cs.setWrapText(true);

        XSSFRow row = sheet1.createRow(0);
        row.createCell(3).setCellValue("Акушерский анамнез");
        row.createCell(4).setCellValue("Соматический анамнез");
        row.createCell(5).setCellValue("Течение беременности");
        row.createCell(6).setCellValue("Течение SARS-C0V-2");

        XSSFRow row1 = sheet1.createRow(1);
        row1.createCell(0).setCellValue("Пациент");
        row1.createCell(1).setCellValue("Наименование группы");
        row1.createCell(2).setCellValue("Возраст");
        row1.createCell(3).setCellValue("Не отягощён");
        row1.createCell(4).setCellValue("Отягощён 1 осложнением");

        FileOutputStream out = new FileOutputStream(new File("workbook.xlsx"));

        workbook.write(out);
        out.close();
        System.out.println("Sucess!");
    }
}
