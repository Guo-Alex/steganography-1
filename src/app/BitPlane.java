package app;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.BitSet;

public class BitPlane {
    private int height, width;
    private BitSet data;

    public BitPlane(int height, int width) {
        this.height = height;
        this.width = width;
        this.data = new BitSet(height * width);
    }

    public BitPlane(BitPlane plane) {
        this.height = plane.height;
        this.width = plane.width;
        this.data = new BitSet(height * width);
        this.data.or(plane.data);
    }

    public BitPlane(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_BYTE_BINARY)
            System.err.println("Warning: bit plane created of non-binary image");
        this.height = image.getHeight();
        this.width = image.getWidth();
        data = new BitSet(height * width);
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x) {
                boolean bit = image.getRGB(x, y) == Color.white.getRGB();
                this.data.set(width * y + x, bit);
            };
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setBit(int x, int y, boolean value) {
        data.set(width * y + x, value);
    }

    public boolean getBit(int x, int y) {
        return data.get(width * y + x);
    }

    public BufferedImage toBufferedImage() {
        BufferedImage result = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < result.getHeight(); ++y)
            for (int x = 0; x < result.getWidth(); ++x) {
                boolean bit = this.data.get(width * y + x);
                int rgb = (bit) ? Color.white.getRGB() : Color.black.getRGB();
                result.setRGB(x, y, rgb);
            }
        return result;
    }
}
