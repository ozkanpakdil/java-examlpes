package io.github.oazkanpakdil.pdfedit;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

public class PasswordProtectedPDF {
    private static final Logger logger = Logger.getLogger(PasswordProtectedPDF.class.getName());
    static final String USER_PASSWORD = "111";
    static final String OWNER_PASSWORD = "111";

    public static void main(String[] args) {
        try (
                FileOutputStream out = new FileOutputStream(new File("1_protected.pdf"));
                PdfReader reader = new PdfReader(new File("1.pdf").getPath())) {
            PdfStamper stamper = new PdfStamper(reader, out);

            HashMap<String, String> info = new HashMap<>();
            info.put("Producer", "");
            reader.getInfo().forEach((key, value) -> {
                logger.info("Key: " + key + ", Value: " + value);
            });
            stamper.setInfoDictionary(info);
            stamper.setEncryption(USER_PASSWORD.getBytes(), OWNER_PASSWORD.getBytes(), PdfWriter.ALLOW_PRINTING,
                    PdfWriter.ENCRYPTION_AES_128);

            stamper.close();
            logger.info("Password protected PDF created successfully.");
        } catch (IOException e) {
            logger.severe("Error creating password protected PDF: " + e.getMessage());
        }
    }
}