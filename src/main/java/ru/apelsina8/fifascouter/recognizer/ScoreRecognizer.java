package ru.apelsina8.fifascouter.recognizer;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Component
public class ScoreRecognizer {
    private final Tesseract tesseract;

    public ScoreRecognizer() {
        tesseract = new Tesseract();
        tesseract.setDatapath("/opt/homebrew/Cellar/tesseract/5.4.1_1/share/tessdata");
        tesseract.setLanguage("eng");
    }

    public String recognizeTime(BufferedImage image) {
        try {
            BufferedImage resizedImage = image.getSubimage(75, 40, 60, 20);
            return tesseract.doOCR(resizedImage).trim();
        } catch (TesseractException e) {
            e.printStackTrace();
            return null;
        }
    }
    public String recognizeScore(BufferedImage image) {
        try {
            BufferedImage resizedImage = image.getSubimage(225, 40, 50, 20);
            return tesseract.doOCR(resizedImage).trim();
        } catch (TesseractException e) {
            e.printStackTrace();
            return null;
        }
    }
}
