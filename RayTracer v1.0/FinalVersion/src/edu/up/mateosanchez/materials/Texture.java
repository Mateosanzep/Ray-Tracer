package edu.up.mateosanchez.materials;

import edu.up.mateosanchez.math.Vector3d;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Texture {
    private BufferedImage image;
    private int width, height;

    public Texture(String filePath) {
        try {
            // ImageIO handles both JPG and PNG formats automatically
            image = ImageIO.read(new File(filePath));
            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
                System.out.println("  Texture loaded: " + filePath + " (" + width + "x" + height + ")");
            } else {
                System.err.println("Error loading texture (unsupported format): " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error loading texture: " + filePath);
        }
    }

    public boolean isLoaded() {
        return image != null;
    }

    // Map U coordinate to image horizontal pixel space with repeating wrapping (Tiling)
    private int getPixelX(double u) {
        u = u - Math.floor(u);
        return (int) (u * (width - 1));
    }

    // Map V coordinate to image vertical pixel space with repeating wrapping and axis inversion
    private int getPixelY(double v) {
        v = v - Math.floor(v);
        
        // Invert V axis since OBJ specifications are inverted relative to standard 2D image coordinates
        v = 1.0 - v; 
        return (int) (v * (height - 1));
    }

    // Retrieve raw non-linearized sRGB color from texture coordinates
    public void getColor(double u, double v, Vector3d colorBuffer) {
        if (image == null) {
            colorBuffer.set(1.0, 0.0, 1.0); // Hot magenta fallback color on error
            return;
        }

        int x = getPixelX(u);
        int y = getPixelY(v);

        // Extract 32-bit integer packed ARGB color channel components
        int rgb = image.getRGB(x, y);
        double r = ((rgb >> 16) & 0xFF) / 255.0;
        double g = ((rgb >> 8) & 0xFF) / 255.0;
        double b = (rgb & 0xFF) / 255.0;

        colorBuffer.set(r, g, b);
    }

    // Convert a single color channel from sRGB non-linear curve to Linear space
    public static double sRgbToLinear(double c) {
        return (c <= 0.04045) ? (c / 12.92) : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    // Get color converted into linear color space for diffuse and emissive shading math
    public void getColorLinear(double u, double v, Vector3d colorBuffer) {
        getColor(u, v, colorBuffer);
        colorBuffer.set(
            sRgbToLinear(colorBuffer.x),
            sRgbToLinear(colorBuffer.y),
            sRgbToLinear(colorBuffer.z)
        );
    }

    // Convert color values into a scalar luminance value for scalar maps (shininess, bump)
    public double getValue(double u, double v) {
        if (image == null) {
            return 0.0;
        }

        int x = getPixelX(u);
        int y = getPixelY(v);

        int rgb = image.getRGB(x, y);
        double r = ((rgb >> 16) & 0xFF) / 255.0;
        double g = ((rgb >> 8) & 0xFF) / 255.0;
        double b = (rgb & 0xFF) / 255.0;

        // Standard ITU-R BT.601 perceived luminance weighting formula
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    // Read alpha transparency channel directly from image data bytes
    public double getAlpha(double u, double v) {
        if (image == null) {
            return 1.0; 
        }

        int x = getPixelX(u);
        int y = getPixelY(v);

        int rgb = image.getRGB(x, y);
        
        // Isolate the highest 8 bits containing the alpha data channel
        int alphaRaw = (rgb >> 24) & 0xFF;
        
        // Standard JPG images lack an alpha layer and will consistently return 1.0 (fully opaque)
        return alphaRaw / 255.0;
    }

    // Direct pixel coordinate evaluation for internal usage such as bump mapping calculations
    public double getValueAtPixel(int px, int py) {
        if (image == null) return 0.0;
        
        px = Math.max(0, Math.min(width - 1, px));
        py = Math.max(0, Math.min(height - 1, py));
        
        int rgb = image.getRGB(px, py);
        double r = ((rgb >> 16) & 0xFF) / 255.0;
        double g = ((rgb >> 8) & 0xFF) / 255.0;
        double b = (rgb & 0xFF) / 255.0;
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}