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
        BufferedImage image = readImage("D:/dump/flowerRGB.png");
        BufferedImage mask = readImage("D:/dump/mask.bmp");
        /* BIT PLANES */
        BitPlanedImage imageBitPlanes = new BitPlanedImage(image);
        BitPlane maskBitPlane = new BitPlane(mask);
        BitPlanedImage watermarkedBitPlanes = BitPlanedImage.addWatermark(imageBitPlanes, maskBitPlane, 3);
        BufferedImage watermarked = watermarkedBitPlanes.toBufferedImage();
        for (int i = 0; i < imageBitPlanes.getDepth(); ++i)
            writeImage(imageBitPlanes.getPlane(i).toBufferedImage(), "D:/dump/" + i + ".bmp");
        writeImage(watermarkedBitPlanes.getPlane(1).toBufferedImage(), "D:/dump/altered.bmp");
        writeImage(watermarked, "D:/dump/watermarked.png");

        BitPlane resolvedMark = BitPlanedImage.resolveWatermark(watermarkedBitPlanes, imageBitPlanes, 3);
        writeImage(resolvedMark.toBufferedImage(), "D:/dump/resolved.bmp");

        /* QUANTUM */
        BufferedImage q_watermarked = QIM.addWatermark(image, mask, 10, true);
        writeImage(q_watermarked, "D:/dump/qim_watermarked.png");

        BufferedImage q_resolvedMark = QIM.resolveWatermark(q_watermarked, 10);
        writeImage(q_resolvedMark, "D:/dump/qim_resolved.bmp");
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
            prepareImage(image);
            return image;
        } catch (IOException e) {
            System.out.println("Failed to open file " + path);
            e.printStackTrace();
        }
        return null;
    }

    private static void prepareImage(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY)
            return;
        for (int y = 0; y < image.getHeight(); ++y)
            for (int x  = 0; x < image.getWidth(); ++x) {
                int rgb = image.getRGB(x, y) & 0xFF; // from 0 to 255
                image.setRGB(x, y, new Color(rgb, rgb, rgb).getRGB());
            };
    }
}
