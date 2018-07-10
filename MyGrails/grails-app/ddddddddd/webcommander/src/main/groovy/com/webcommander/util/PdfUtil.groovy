package com.webcommander.util

import com.sun.pdfview.PDFFile
import com.sun.pdfview.PDFPage

import javax.imageio.ImageIO
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths

class PdfUtil {

    public static void pdfToImage(String pdfFilePath, String imageFilePath) {
        ByteBuffer buf = ByteBuffer.wrap(Files.readAllBytes(Paths.get(pdfFilePath)))
        PDFFile pdf = new PDFFile(buf)
        for(int i = 0; i < pdf.getNumPages(); i++) {
            createImage(pdf.getPage(i), imageFilePath.replace("{#}", (i + 1).toString()))
        }
    }

    public static void createImage(PDFPage page, String destination) throws IOException {
        Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight())
        BufferedImage bufferedImage = new BufferedImage((int)rect.width, (int)rect.height, BufferedImage.TYPE_INT_RGB)
        Image image = page.getImage((int)rect.width, (int)rect.height, rect, null, true, true);
        Graphics2D bufImageGraphics = bufferedImage.createGraphics();
        bufImageGraphics.drawImage(image, 0, 0, null);
        ImageIO.write(bufferedImage, "PNG", new File( destination ));
    }
}
