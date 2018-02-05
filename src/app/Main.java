package app;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        BufferedImage image = readImage(currentDir + "/img/flowerRGB.png");
        BufferedImage mask = readImage(currentDir + "/img/mask.bmp");
        /* BIT PLANES */
        BitPlanedImage imageBitPlanes = new BitPlanedImage(image);
        BitPlane maskBitPlane = new BitPlane(mask);
        BitPlanedImage watermarkedBitPlanes = BitPlanedImage.addWatermark(imageBitPlanes, maskBitPlane, 1);
        BufferedImage watermarked = watermarkedBitPlanes.toBufferedImage();
        for (int i = 0; i < imageBitPlanes.getDepth(); ++i)
            writeImage(imageBitPlanes.getPlane(i).toBufferedImage(), currentDir + "/img/" + i + ".bmp");
        writeImage(watermarkedBitPlanes.getPlane(1).toBufferedImage(), currentDir + "/img/altered.bmp");
        writeImage(watermarked, currentDir + "/img/watermarked.png");

        BitPlane resolvedMark = BitPlanedImage.resolveWatermark(watermarkedBitPlanes, imageBitPlanes, 1);
        writeImage(resolvedMark.toBufferedImage(), currentDir + "/img/resolved.bmp");

        /* QUANTUM */
        BufferedImage q_watermarked = QIM.addWatermark(image, mask, 10, true);
        writeImage(q_watermarked, currentDir + "/img/qim_watermarked.png");

        BufferedImage q_resolvedMark = QIM.resolveWatermark(q_watermarked, 10);
        writeImage(q_resolvedMark, currentDir + "/img/qim_resolved.bmp");
    }

    private static void writeImage(BufferedImage image, String path) {
        try {
            OutputStream imgOutStream = Files.newOutputStream(Paths.get(path), StandardOpenOption.CREATE);
            ImageIO.write(image, "png", imgOutStream);
        } catch (IOException e) {
            System.out.println("Failed to save file " + path);
            e.printStackTrace();
        }
    }

    private static BufferedImage readImage(String path) {
        try {
            InputStream imgInpStream = Files.newInputStream(Paths.get(path));
            BufferedImage image = ImageIO.read(imgInpStream);
            return image;
        } catch (IOException e) {
            System.out.println("Failed to open file " + path);
            e.printStackTrace();
        }
        return null;
    }

}
