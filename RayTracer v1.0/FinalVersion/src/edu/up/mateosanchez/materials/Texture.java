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
            // ImageIO lee perfectamente tanto JPG como PNG
            image = ImageIO.read(new File(filePath));
            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
                System.out.println("  Textura cargada: " + filePath + " (" + width + "x" + height + ")");
            } else {
                System.err.println("Error al cargar la textura (formato no soportado): " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar la textura: " + filePath);
        }
    }

    // Verifica si la imagen se cargó correctamente
    public boolean isLoaded() {
        return image != null;
    }

    // Calcula las coordenadas del píxel a partir de UV con envoltura (Tiling)
    private int getPixelX(double u) {
        // Envoltura: asegura que u esté entre 0.0 y 1.0 (ej: 2.5 -> 0.5)
        u = u - Math.floor(u);
        return (int) (u * (width - 1));
    }

    private int getPixelY(double v) {
        // Envoltura: asegura que v esté entre 0.0 y 1.0
        v = v - Math.floor(v);
        
        // Invertir V: Los archivos OBJ suelen tener la V invertida respecto a las imágenes 2D
        v = 1.0 - v; 
        return (int) (v * (height - 1));
    }

    // Devuelve el color de la textura en sRGB crudo (0.0 a 1.0)
    public void getColor(double u, double v, Vector3d colorBuffer) {
        if (image == null) {
            colorBuffer.set(1.0, 0.0, 1.0); // Magenta chillón si hay error
            return;
        }

        int x = getPixelX(u);
        int y = getPixelY(v);

        //getRGB(x,y) extrae el entero de 32 bits independientemente de si es JPG o PNG
        int rgb = image.getRGB(x, y);
        double r = ((rgb >> 16) & 0xFF) / 255.0;
        double g = ((rgb >> 8) & 0xFF) / 255.0;
        double b = (rgb & 0xFF) / 255.0;

        colorBuffer.set(r, g, b);
    }

    // Convierte un canal de sRGB a espacio Lineal
    public static double sRgbToLinear(double c) {
        return (c <= 0.04045) ? (c / 12.92) : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    // Devuelve el color linealizado de la textura (sRGB -> Lineal) para color difuso/emisión
    public void getColorLinear(double u, double v, Vector3d colorBuffer) {
        getColor(u, v, colorBuffer);
        colorBuffer.set(
            sRgbToLinear(colorBuffer.x),
            sRgbToLinear(colorBuffer.y),
            sRgbToLinear(colorBuffer.z)
        );
    }

    // Devuelve un valor escalar (luminancia) para mapas de bump, shininess
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

        // Luminancia percibida (fórmula estándar ITU-R BT.601)
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    /**
     * Devuelve el valor de transparencia real de la imagen.
     * CORRECCIÓN: Se eliminó el parche que convertía el negro en transparente.
     * Ahora lee directamente el canal Alfa del archivo (PNG o JPG).
     */
    public double getAlpha(double u, double v) {
        if (image == null) {
            return 1.0; 
        }

        int x = getPixelX(u);
        int y = getPixelY(v);

        // getRGB devuelve un entero de 32 bits: [ALPHA (8 bits) | RED (8) | GREEN (8) | BLUE (8)]
        int rgb = image.getRGB(x, y);
        
        // Extraemos los 8 bits superiores (del 24 al 31) y aplicamos máscara
        int alphaRaw = (rgb >> 24) & 0xFF;
        
        // Convertimos a rango 0.0 (transparente) - 1.0 (opaco).
        // Los JPGs siempre devolverán 255 (1.0) aquí porque no tienen canal Alfa.
        return alphaRaw / 255.0;
    }

    // Devuelve el valor escalar en coordenadas de píxel (usado internamente para bump mapping)
    public double getValueAtPixel(int px, int py) {
        if (image == null) return 0.0;
        
        // Clamping para asegurar que el píxel esté en rango
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