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

    public void setPixel(int x, int y, double r, double g, double b){
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
            // TODO: handle exception
        }
    }
}
