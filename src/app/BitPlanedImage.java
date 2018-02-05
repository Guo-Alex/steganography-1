package app;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.BitSet;

public class BitPlanedImage {

    private int height, width;
    private BitPlane[] planes;
    private int type;

    public BitPlanedImage(BufferedImage image) {
        this.type = image.getType();
        this.height = image.getHeight();
        this.width = image.getWidth();
        int colorDepth = 8; //calculateColorDepth(image);
        planes = new BitPlane[colorDepth];
        for (int i = 0; i < planes.length; ++i)
            planes[i] = new BitPlane(height, width);
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x) {
                int gray = image.getRGB(x, y) & 0xFF;
                BitSet grayAsBits = new BitSet(planes.length);
                grayAsBits.or(BitSet.valueOf(new long[] {gray}));
                for (int i = 0; i < planes.length; ++i)
                    planes[i].setBit(x, y, grayAsBits.get(i));
            };
    }

    public BitPlanedImage(BitPlanedImage sourceImage) {
        this.type = sourceImage.type;
        this.height = sourceImage.height;
        this.width = sourceImage.width;
        this.planes = new BitPlane[sourceImage.planes.length];
        for(int i = 0; i < this.planes.length; ++i)
            this.planes[i] = new BitPlane(sourceImage.planes[i]);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return planes.length;
    }

    public BitPlane getPlane(int layer) {
        return new BitPlane(planes[layer]);
    }

    private static int calculateColorDepth(BufferedImage image) {
        int imageType = image.getType();
        switch (imageType) {
            case BufferedImage.TYPE_3BYTE_BGR:
                return 3 * 8;
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return 4 * 8;
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_BYTE_INDEXED:
                return 8;
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_INT_RGB:
                return 4 * 8;
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
            case BufferedImage.TYPE_USHORT_GRAY:
                return 2 * 8;
            default: return 0;
        }
    }

    public static BitPlanedImage addWatermark(BitPlanedImage sourceImage, BitPlane maskPlane, int planeLevel) {
        if (planeLevel < 0 || planeLevel > sourceImage.planes.length)
            throw new ArrayIndexOutOfBoundsException("Plane level out of range");
        BitPlane resultPlane = mergePlanes(sourceImage.planes[planeLevel], maskPlane);
        BitPlanedImage result = new BitPlanedImage(sourceImage);
        result.planes[planeLevel] = resultPlane;
        return result;
    }

    public static BitPlane mergePlanes(BitPlane first, BitPlane second) {
        int width = Math.min(first.getWidth(), second.getWidth());
        int height = Math.min(first.getHeight(), second.getHeight());
        BitPlane result = new BitPlane(height, width);
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x) {
                boolean firstBit = first.getBit(x, y);
                boolean secondBit = second.getBit(x, y);
                result.setBit(x, y, firstBit ^ secondBit);
            };
        return result;
    }

    public static BitPlane resolveWatermark(BitPlanedImage markedImage, BitPlanedImage sourceImage, int planeLevel) {
        if (planeLevel < 0 || planeLevel > sourceImage.planes.length
                || planeLevel > markedImage.planes.length)
            throw new ArrayIndexOutOfBoundsException("Plane level out of range");
        return mergePlanes(sourceImage.planes[planeLevel], markedImage.planes[planeLevel]);
    }

    public BufferedImage toBufferedImage() {
        BufferedImage result = new BufferedImage(this.width, this.height, this.type);
        for (int y = 0; y < result.getHeight(); ++y)
            for (int x = 0; x < result.getWidth(); ++x) {
                int gray = this.getGrayscale(x, y);
                result.setRGB(x, y, new Color(gray, gray, gray).getRGB());
            }
        return result;
    }

    private int getGrayscale(int x, int y) {
        BitSet grayAsBits = new BitSet(planes.length);
        for (int i = 0; i < planes.length; ++i) {
            grayAsBits.set(i, planes[i].getBit(x, y));
        }
        long[] arr = grayAsBits.toLongArray(); // returns empty array if 0x00
        return (arr.length > 0) ? (int)arr[0] : 0;
    }
}
