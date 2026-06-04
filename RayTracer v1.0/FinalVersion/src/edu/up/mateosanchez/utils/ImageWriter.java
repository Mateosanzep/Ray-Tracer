package edu.up.mateosanchez.utils;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ImageWriter {
    public int width, height;
    public BufferedImage image;

    public ImageWriter(int width, int height) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void setPixel(int x, int y, double r, double g, double b) {
        // 1. Reinhard Tone Mapping
        // Maps infinite high-dynamic-range (HDR) light values into a soft [0.0, 1.0] display range
        r = r / (r + 1.0);
        g = g / (g + 1.0);
        b = b / (b + 1.0);

        // 2. Gamma Correction (gamma = 2.2)
        // Adjusts linear color space to match human visual perception and standard monitor hardware curves
        r = Math.pow(r, 1.0 / 2.2);
        g = Math.pow(g, 1.0 / 2.2);
        b = Math.pow(b, 1.0 / 2.2);

        // 3. Final Clamping and 8-bit Integer Conversion
        int red = (int) Math.max(0, Math.min(255, r * 255.99));
        int green = (int) Math.max(0, Math.min(255, g * 255.99));
        int blue = (int) Math.max(0, Math.min(255, b * 255.99));

        int rgb = (red << 16) | (green << 8) | blue;

        image.setRGB(x, y, rgb);
    }
    
    public void save(String filename){
        try {
            ImageIO.write(this.image, "png", new java.io.File(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}