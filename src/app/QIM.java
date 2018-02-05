package app;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class QIM {

    public static BufferedImage addWatermark(BufferedImage sourceImage, BufferedImage maskImage, int q, boolean toRGB) {
        if (q <= 0)
            throw new IllegalArgumentException("Quantification parameter should be greater than zero");
        if (sourceImage.getType() != BufferedImage.TYPE_BYTE_GRAY)
            System.err.println("Warning: trying to apply a watermark to non-grayscale image");
        if (maskImage.getType() != BufferedImage.TYPE_BYTE_BINARY)
            System.err.println("Warning: trying to apply a non-binary watermark");
        int width = Math.min(sourceImage.getWidth(), maskImage.getWidth());
        int height = Math.min(sourceImage.getHeight(), maskImage.getHeight());
        BufferedImage result = new BufferedImage(width, height, (toRGB) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; ++y)
            for (int x  = 0; x < width; ++x) {
                int sourceGray = sourceImage.getRGB(x, y) & 0xFF; // from 0 to 255
                int maskGray = maskImage.getRGB(x, y) & 0xFF; // 0 or 255
                maskGray = (maskGray == 0) ? 0 : 1; // change 0, 255 to 0, 1
                int resultGray = sourceGray - sourceGray % (2 * q) + sourceGray % q + maskGray * q;
                if (resultGray > 255)
                    // set to the brightest color C so that C mod 2q = q
                    resultGray = 255 - 255 % q;
                int rgb = resultGray << 16 | resultGray << 8 | resultGray;
                result.setRGB(x, y, rgb);
            };
        return result;
    }

    public static BufferedImage resolveWatermark(BufferedImage maskedImage, int q) {
        if (q <= 0)
            throw new IllegalArgumentException("Quantification parameter should be greater than zero");
        if (maskedImage.getType() != BufferedImage.TYPE_BYTE_GRAY)
            System.err.println("Warning: trying to resolve watermark from a non-grayscale image");
        int width = maskedImage.getWidth();
        int height = maskedImage.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; ++y)
            for (int x  = 0; x < width; ++x) {
                int maskedGray = maskedImage.getRGB(x, y) & 0xFF; // from 0 to 255
                int noise = maskedGray % (2 * q);
                int maskGray = (noise < q) ? Color.black.getRGB() : Color.white.getRGB();
                result.setRGB(x, y, maskGray);
            };
        return result;
    }

}
