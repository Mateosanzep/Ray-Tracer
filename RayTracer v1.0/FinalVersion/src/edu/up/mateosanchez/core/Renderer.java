package edu.up.mateosanchez.core;

import edu.up.mateosanchez.acceleration.BVHNode; 
import edu.up.mateosanchez.camera.Camera;
import edu.up.mateosanchez.math.Ray;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.utils.ImageWriter;
import edu.up.mateosanchez.geometry.HitRecord;
import edu.up.mateosanchez.geometry.Intersectable;
import edu.up.mateosanchez.lights.Light;

import java.util.ArrayList;
import java.util.stream.IntStream; 

public class Renderer {
    public Camera camera;
    public ImageWriter imagewriter;
    public ArrayList<Intersectable> objects;
    public ArrayList<Light> lights;
    public double ambientIntensity;
    public BVHNode worldRoot; 
    public int samplesPerPixel = 1;
    
    // --- CONFIGURACIÓN DE LINEAR WORKFLOW & TONE MAPPING ---
    public enum ToneMapping {
        NONE,
        ACES
    }
    public double maxClampIntensity = 20.0; 
    // He quitado REINHARD de la enumeración porque no se usaba y generaba confusión.
    public ToneMapping toneMapping = ToneMapping.ACES; 

    // Función ACES Film tone mapping corregida para su uso.
    private double acesFilm(double x) {
        double a = 2.51;
        double b = 0.03;
        double c = 2.43;
        double d = 0.59;
        double e = 0.14;
        return (x * (a * x + b)) / (x * (c * x + d) + e);
    }

    public Renderer(Camera camara, ImageWriter imagewriter, Scene scene) {
        this(camara, imagewriter, scene.objects, scene.lights, scene.ambientIntensity);
    }

    public Renderer(Camera camara, ImageWriter imagewriter, ArrayList<Intersectable> objects, ArrayList<Light> lights, double ambientIntensity) {
        this.camera = camara;
        this.imagewriter = imagewriter;
        this.objects = objects;
        this.lights = lights;
        this.ambientIntensity = ambientIntensity;
        
        System.out.println("Construyendo BVH espacial...");
        long startTime = System.currentTimeMillis();
        this.worldRoot = new BVHNode(this.objects, 0, this.objects.size());
        long endTime = System.currentTimeMillis();
        System.out.println("BVH construido en " + (endTime - startTime) + " ms.");
    }

    public void render(double tMin, double tMax){
        int bounces = 6;

        System.out.println("Renderizando en paralelo con " + samplesPerPixel + " muestras por píxel...");
        
        IntStream.range(0, imagewriter.height).parallel().forEach(y -> {
            Vector3d directionBuffer = new Vector3d();
            Ray ray = new Ray(camera.origin, directionBuffer);
            Vector3d pixelColor = new Vector3d();

            for (int x = 0; x < imagewriter.width; x++){
                double rSum = 0.0;
                double gSum = 0.0;
                double bSum = 0.0;

                for (int s = 0; s < samplesPerPixel; s++) {
                    double u, v;
                    if (samplesPerPixel == 1) {
                        u = (double) x / (imagewriter.width - 1);
                        v = (double) (imagewriter.height - 1 - y) / (imagewriter.height - 1);
                    } else {
                        double px = x + Math.random() - 0.5;
                        double py = y + Math.random() - 0.5;
                        u = px / (imagewriter.width - 1);
                        v = (imagewriter.height - 1 - py) / (imagewriter.height - 1);
                    }

                    camera.getRayDirection(u, v, directionBuffer);
                    ray.setDirection(directionBuffer);

                    boolean debugPixel = (x == 640 && y == 360);
                    rayColor(ray, pixelColor, tMin, tMax, bounces, debugPixel);

                    if (Double.isNaN(pixelColor.x) || Double.isInfinite(pixelColor.x) || pixelColor.x < 0.0) pixelColor.x = 0.0;
                    if (Double.isNaN(pixelColor.y) || Double.isInfinite(pixelColor.y) || pixelColor.y < 0.0) pixelColor.y = 0.0;
                    if (Double.isNaN(pixelColor.z) || Double.isInfinite(pixelColor.z) || pixelColor.z < 0.0) pixelColor.z = 0.0;

                    double luma = 0.2126 * pixelColor.x + 0.7152 * pixelColor.y + 0.0722 * pixelColor.z;
                    if (luma > maxClampIntensity) {
                        double scale = maxClampIntensity / luma;
                        pixelColor.x *= scale;
                        pixelColor.y *= scale;
                        pixelColor.z *= scale;
                    }

                    rSum += pixelColor.x;
                    gSum += pixelColor.y;
                    bSum += pixelColor.z;
                } 

                double finalR = rSum / samplesPerPixel;
                double finalG = gSum / samplesPerPixel;
                double finalB = bSum / samplesPerPixel;

                // --- EXPOSICIÓN ---
                // Se ha reducido la exposición global, 2.0 es demasiado alto para esta escena.
                double exposure = 0.5; 
                finalR *= exposure;
                finalG *= exposure;
                finalB *= exposure;

                // --- TONE MAPPING UNIFICADO CON ACES ---
                // He eliminado el código de Hable/Uncharted 2 para usar la función ACES
                // que habías definido previamente, respetando la configuración inicial.
                if (toneMapping == ToneMapping.ACES) {
                    finalR = acesFilm(finalR);
                    finalG = acesFilm(finalG);
                    finalB = acesFilm(finalB);
                }

                finalR = Math.max(0.0, Math.min(1.0, finalR));
                finalG = Math.max(0.0, Math.min(1.0, finalG));
                finalB = Math.max(0.0, Math.min(1.0, finalB));

                // --- CORRECCIÓN GAMMA ---
                double gamma = 1.0 / 2.2;
                finalR = Math.pow(finalR, gamma);
                finalG = Math.pow(finalG, gamma);
                finalB = Math.pow(finalB, gamma);

                imagewriter.setPixel(x, y, finalR, finalG, finalB);
            }
            
            if (y % 100 == 0) {
                System.out.println("Renderizadas " + y + " de " + imagewriter.height + " líneas...");
            }
        });
    }

    private void rayColor(Ray ray, Vector3d resultColor, double tMin, double tMax, int bounces, boolean debug) {
        if(bounces <= 0){
            // --- CIELO CORREGIDO: DEGRADADO VIVO SIN LÍNEA BLANCA ---
        Vector3d unitDir = new Vector3d(ray.direction.x, ray.direction.y, ray.direction.z);
        unitDir.normalize();
        
        double t = Math.max(0.0, unitDir.y); 
        
        // Colores base normalizados en un rango similar para evitar la desaturación intermedia
        Vector3d fuerteNaranja = new Vector3d(3.5, 0.7, 0.01);    // Naranja encendido y puro
        Vector3d fuerteAzulMar = new Vector3d(0.01, 0.15, 2.2);   // Azul profundo tipo océano
        
        // Usamos una curva sigmoide (Smoothstep) para que la mezcla física sea drástica pero suave
        double mezcla = t * t * (3.0 - 2.0 * t);
        
        // Interpolación directa de los componentes
        double skyR = (1.0 - mezcla) * fuerteNaranja.x + mezcla * fuerteAzulMar.x;
        double skyG = (1.0 - mezcla) * fuerteNaranja.y + mezcla * fuerteAzulMar.y;
        double skyB = (1.0 - mezcla) * fuerteNaranja.z + mezcla * fuerteAzulMar.z;
        
        // Empuje extra en la base para simular la intensidad del sol poniente
        if (t < 0.25) {
            double factorIntensidad = Math.pow(1.0 - (t / 0.25), 2.0);
            skyR += factorIntensidad * 2.0;
            skyG += factorIntensidad * 0.3;
        }

        if (unitDir.y < 0.0) {
            skyR = 0.001; skyG = 0.005; skyB = 0.02; // El vacío debajo del horizonte
        }

        resultColor.set(skyR, skyG, skyB);
        return;
        }

        HitRecord tempRecord = new HitRecord();
        boolean hittedSome = false;
        double tNear = tMax;

        if (worldRoot.intersect(ray, tMin, tNear, tempRecord)) {
            hittedSome = true;
            tNear = tempRecord.t;
        }

        if (hittedSome) {
            Vector3d n = new Vector3d(tempRecord.normal.x, tempRecord.normal.y, tempRecord.normal.z);
            double secondaryTMin = Math.max(tMin, 0.001);
            
            // --- DETECTOR DE AGUA ---
            boolean isWater = (tempRecord.material.diffuseColor.z > tempRecord.material.diffuseColor.x && n.y > 0.8);
            
            if (isWater) {
                double x = tempRecord.point.x;
                double z = tempRecord.point.z;
                
                // Fractales de ondas (fBm)
                double distX = Math.cos(x * 0.3 + z * 0.1) * 0.25
                             + Math.cos(x * 0.8 - z * 0.4) * 0.15
                             + Math.cos(z * 2.2 + x * 1.5) * 0.08
                             + Math.cos(x * 5.5 - z * 3.7) * 0.04
                             + Math.cos(z * 12.0 + x * 9.0) * 0.02
                             + Math.cos(x * 28.0 - z * 18.0) * 0.01;
                             
                double distZ = Math.cos(x * 0.2 + z * 0.4) * 0.25
                             + Math.cos(z * 0.9 - x * 0.5) * 0.15
                             + Math.cos(x * 1.8 + z * 1.4) * 0.08
                             + Math.cos(z * 4.8 - x * 3.2) * 0.04
                             + Math.cos(x * 11.0 + z * 8.5) * 0.02
                             + Math.cos(z * 26.0 - x * 15.0) * 0.01;
                
                n.x += distX * 0.22;
                n.z += distZ * 0.22;
                n.normalize();
            }

            if (debug) {
                System.out.println("\n--- DEBUG PIXEL ---");
                System.out.println("Hit Point: " + tempRecord.point.x + ", " + tempRecord.point.y + ", " + tempRecord.point.z);
            }

            // --- 1. BUMP MAPPING ---
            if (tempRecord.material.bumpMap != null && tempRecord.material.bumpMap.isLoaded()) {
                Vector3d bumpColor = new Vector3d();
                tempRecord.material.bumpMap.getColor(tempRecord.u, tempRecord.v, bumpColor);
                
                double bx = (bumpColor.x * 2.0) - 1.0;
                double by = (bumpColor.y * 2.0) - 1.0;
                
                Vector3d T = new Vector3d();
                if (Math.abs(n.y) < 0.99) {
                    T.set(0.0, 1.0, 0.0);
                } else {
                    T.set(1.0, 0.0, 0.0);
                }
                double ndotT = n.dot(T);
                T.set(T.x - n.x * ndotT, T.y - n.y * ndotT, T.z - n.z * ndotT);
                T.normalize();
                
                Vector3d B = new Vector3d(
                    n.y * T.z - n.z * T.y,
                    n.z * T.x - n.x * T.z,
                    n.x * T.y - n.y * T.x
                );
                
                double strength = tempRecord.material.bumpStrength;
                double tx = bx * strength;
                double ty = by * strength;
                double tz = Math.sqrt(Math.max(0.0, 1.0 - tx * tx - ty * ty));
                
                n.set(
                    T.x * tx + B.x * ty + n.x * tz,
                    T.y * tx + B.y * ty + n.y * tz,
                    T.z * tx + B.z * ty + n.z * tz
                );
                n.normalize();
            }

            // --- 2. COLOR DIFUSO ---
            double baseDiffuseR, baseDiffuseG, baseDiffuseB;
            if (isWater) {
                baseDiffuseR = 0.0;
                baseDiffuseG = 0.04;
                baseDiffuseB = 0.35; 
            } else if (tempRecord.material.texture != null) {
                Vector3d tempColor = new Vector3d();
                tempRecord.material.texture.getColorLinear(tempRecord.u, tempRecord.v, tempColor);
                baseDiffuseR = tempColor.x; 
                baseDiffuseG = tempColor.y; 
                baseDiffuseB = tempColor.z;
            } else {
                baseDiffuseR = edu.up.mateosanchez.materials.Texture.sRgbToLinear(tempRecord.material.diffuseColor.x);
                baseDiffuseG = edu.up.mateosanchez.materials.Texture.sRgbToLinear(tempRecord.material.diffuseColor.y);
                baseDiffuseB = edu.up.mateosanchez.materials.Texture.sRgbToLinear(tempRecord.material.diffuseColor.z);
            }

            // CORRECCIÓN: Se ha eliminado el bloque "Checkerboard Floor" para evitar
            // sobreescribir la textura de madera del barco.
            
            // --- 3. PROPIEDADES MATERIAL ---
            Vector3d currentEmissive = new Vector3d();
            tempRecord.material.getEffectiveEmissive(tempRecord.u, tempRecord.v, currentEmissive);
            
            Vector3d currentSpecular = new Vector3d();
            tempRecord.material.getEffectiveSpecular(tempRecord.u, tempRecord.v, currentSpecular);
            
            double currentShininess = Math.max(80.0, tempRecord.material.getEffectiveShininess(tempRecord.u, tempRecord.v));
            if (isWater) {
                currentShininess = 2500.0; 
            }
            
            double currentTransparency = 1.0 - tempRecord.material.getEffectiveAlpha(tempRecord.u, tempRecord.v);

            double localR = baseDiffuseR * this.ambientIntensity + currentEmissive.x;
            double localG = baseDiffuseG * this.ambientIntensity + currentEmissive.y;
            double localB = baseDiffuseB * this.ambientIntensity + currentEmissive.z;

            Vector3d viewDirBuffer = new Vector3d(-ray.direction.x, -ray.direction.y, -ray.direction.z);
            viewDirBuffer.normalize();

            double geomNdotI = ray.direction.dot(tempRecord.normal);
            boolean geomFrontFace = geomNdotI < 0.0;
            double faceGeomNX = geomFrontFace ? tempRecord.normal.x : -tempRecord.normal.x;
            double faceGeomNY = geomFrontFace ? tempRecord.normal.y : -tempRecord.normal.y;
            double faceGeomNZ = geomFrontFace ? tempRecord.normal.z : -tempRecord.normal.z;
            
            Vector3d lightDirBuffer = new Vector3d();
            Vector3d lightColorBuffer = new Vector3d();
            Ray shadowRay = new Ray(new Vector3d(), new Vector3d());
            HitRecord shadowRecord = new HitRecord();
            Vector3d halfwayBuffer = new Vector3d();

            for (Light light : lights) {
                light.getDirection(tempRecord.point, lightDirBuffer);
                light.getColor(tempRecord.point, lightColorBuffer);
                double distanceToLight = light.getDistance(tempRecord.point);

                double nDotL = n.dot(lightDirBuffer);
                if (nDotL <= 0.0) continue; 

                shadowRay.origin.set(
                    tempRecord.point.x + n.x * 0.001, 
                    tempRecord.point.y + n.y * 0.001, 
                    tempRecord.point.z + n.z * 0.001
                );
                shadowRay.setDirection(lightDirBuffer);

                double shadowFactor = 1.0;
                if (worldRoot.intersect(shadowRay, tMin, distanceToLight - 0.005, shadowRecord)) {
                    double opacity =
    shadowRecord.material.getEffectiveAlpha(
        shadowRecord.u,
        shadowRecord.v
    );

shadowFactor *= opacity;
                }

                double diffuseFactor = nDotL * shadowFactor;
                localR += diffuseFactor * lightColorBuffer.x * baseDiffuseR;
                localG += diffuseFactor * lightColorBuffer.y * baseDiffuseG;
                localB += diffuseFactor * lightColorBuffer.z * baseDiffuseB;

                halfwayBuffer.set(lightDirBuffer.x + viewDirBuffer.x, lightDirBuffer.y + viewDirBuffer.y, lightDirBuffer.z + viewDirBuffer.z);
                halfwayBuffer.normalize();

                double specularFactor = Math.max(0.0, n.dot(halfwayBuffer));
                double specularSpecular = Math.pow(specularFactor, currentShininess) * shadowFactor;
                
                // CORRECCIÓN: Se ha eliminado specularBoost = 5.0; era demasiado alto.
                
                localR += specularSpecular * lightColorBuffer.x * currentSpecular.x;
                localG += specularSpecular * lightColorBuffer.y * currentSpecular.y;
                localB += specularSpecular * lightColorBuffer.z * currentSpecular.z;
            }

            double ndotI = ray.direction.dot(n);
            boolean frontFace = ndotI < 0.0;
            double faceNX = frontFace ? n.x : -n.x;
            double faceNY = frontFace ? n.y : -n.y; 
            double faceNZ = frontFace ? n.z : -n.z;
            double cosI = -(ray.direction.x * faceNX + ray.direction.y * faceNY + faceNZ * ray.direction.z);

            double reflectR = 0.0, reflectG = 0.0, reflectB = 0.0;
            double refractR = 0.0, refractG = 0.0, refractB = 0.0;
            double fresnel = 0.0;
            boolean tir = false;

            if (currentTransparency > 0.0) {
                double eta1 = frontFace ? 1.0 : tempRecord.material.ior;
                double eta2 = frontFace ? tempRecord.material.ior : 1.0;
                double eta = eta1 / eta2;
                double k = 1.0 - eta * eta * (1.0 - cosI * cosI);
                double r0 = (eta1 - eta2) / (eta1 + eta2);
                r0 = r0 * r0;
                
                if (k < 0.0) {
                    tir = true;
                    fresnel = 1.0;
                } else {
                    fresnel = r0 + (1.0 - r0) * Math.pow(1.0 - cosI, 5.0);
                    fresnel = Math.max(0.0, Math.min(1.0, fresnel));
                    if (!frontFace) fresnel = 0.0;
                }
            }

            double matReflect = tempRecord.material.reflectivity;
            
            if (isWater) {
                double r0 = 0.04; 
                double fresnelWater = r0 + (1.0 - r0) * Math.pow(1.0 - cosI, 5.0);
                matReflect = 0.15 + 0.80 * fresnelWater; 
            }

            if (matReflect > 0.0 || tir || (currentTransparency > 0.0 && frontFace && fresnel > 0.02)) {
                double dotFace = ray.direction.x * faceNX + ray.direction.y * faceNY + ray.direction.z * faceNZ;
                Vector3d reflectDirBuffer = new Vector3d(
                    ray.direction.x - 2.0 * dotFace * faceNX,
                    ray.direction.y - 2.0 * dotFace * faceNY,
                    ray.direction.z - 2.0 * dotFace * faceNZ
                );
                reflectDirBuffer.normalize();
                
                Ray reflectionRay = new Ray(
                    new Vector3d(tempRecord.point.x + faceGeomNX * 0.001, tempRecord.point.y + faceGeomNY * 0.001, tempRecord.point.z + faceGeomNZ * 0.001),
                    reflectDirBuffer
                );
                
                Vector3d reflectedColor = new Vector3d();
                rayColor(reflectionRay, reflectedColor, secondaryTMin, tMax, bounces - 1, debug);
                reflectR = reflectedColor.x; reflectG = reflectedColor.y; reflectB = reflectedColor.z;
            }

            if (currentTransparency > 0.0 && !tir) {
                double eta1 = frontFace ? 1.0 : tempRecord.material.ior;
                double eta2 = frontFace ? tempRecord.material.ior : 1.0;
                double eta = eta1 / eta2;
                double k = 1.0 - eta * eta * (1.0 - cosI * cosI);
                double tFactor = eta * cosI - Math.sqrt(Math.max(0.0, k));
                
                Vector3d refractDirBuffer = new Vector3d(
                    eta * ray.direction.x + tFactor * faceNX,
                    eta * ray.direction.y + tFactor * faceNY,
                    eta * ray.direction.z + tFactor * faceNZ
                );
                refractDirBuffer.normalize();
                
                Ray refractionRay = new Ray(
                    new Vector3d(tempRecord.point.x - faceGeomNX * 0.001, tempRecord.point.y - faceGeomNY * 0.001, tempRecord.point.z - faceGeomNZ * 0.001),
                    refractDirBuffer
                );
                
                Vector3d refractedColor = new Vector3d();
                rayColor(refractionRay, refractedColor, secondaryTMin, tMax, bounces - 1, debug);
                refractR = refractedColor.x; refractG = refractedColor.y; refractB = refractedColor.z;
            }
            
            double matTrans = currentTransparency;
            double matOpacity = Math.max(0.0, 1.0 - matReflect - matTrans);

            if (matTrans > 0.0) {
                double fresnelReflect = fresnel * matTrans;
                double fresnelTransmit = matTrans * (1.0 - fresnel);
                matReflect += fresnelReflect;
                matTrans = fresnelTransmit;
                double glassSum = matReflect + matTrans + matOpacity;
                if (glassSum > 1.0) {
                    double scale = 1.0 / glassSum;
                    matReflect *= scale; matTrans *= scale; matOpacity *= scale;
                }
            }

            resultColor.set(
                (localR * matOpacity) + (reflectR * matReflect) + (refractR * matTrans),
                (localG * matOpacity) + (reflectG * matReflect) + (refractG * matTrans),
                (localB * matOpacity) + (reflectB * matReflect) + (refractB * matTrans)
            );
            return;
        }

       Vector3d unitDir = new Vector3d(ray.direction.x, ray.direction.y, ray.direction.z);
unitDir.normalize();

double t = Math.max(0.0, unitDir.y);

// ---------- SKY : SUNSET GRADIENT ----------

// Colores calibrados
Vector3d amarillo = new Vector3d(1.8, 1.5, 0.3);
Vector3d naranja  = new Vector3d(1.6, 0.7, 0.2);
Vector3d magenta  = new Vector3d(1.0, 0.4, 0.9);
Vector3d morado   = new Vector3d(0.5, 0.3, 1.3);
Vector3d azul     = new Vector3d(0.2, 0.6, 2.0);
Vector3d azulDeep = new Vector3d(0.03, 0.10, 0.6);

double h1 = 0.04;
double h2 = 0.10;
double h3 = 0.20;
double h4 = 0.40;
double h5 = 0.80;

double skyR;
double skyG;
double skyB;

if (t < h1)
{
    double m = t / h1;
    m = m * m * (3.0 - 2.0 * m);

    skyR = amarillo.x * (1.0 - m) + naranja.x * m;
    skyG = amarillo.y * (1.0 - m) + naranja.y * m;
    skyB = amarillo.z * (1.0 - m) + naranja.z * m;
}
else if (t < h2)
{
    double m = (t - h1) / (h2 - h1);
    m = m * m * (3.0 - 2.0 * m);

    skyR = naranja.x * (1.0 - m) + magenta.x * m;
    skyG = naranja.y * (1.0 - m) + magenta.y * m;
    skyB = naranja.z * (1.0 - m) + magenta.z * m;
}
else if (t < h3)
{
    double m = (t - h2) / (h3 - h2);
    m = m * m * (3.0 - 2.0 * m);

    skyR = magenta.x * (1.0 - m) + morado.x * m;
    skyG = magenta.y * (1.0 - m) + morado.y * m;
    skyB = magenta.z * (1.0 - m) + morado.z * m;
}
else if (t < h4)
{
    double m = (t - h3) / (h4 - h3);
    m = m * m * (3.0 - 2.0 * m);

    skyR = morado.x * (1.0 - m) + azul.x * m;
    skyG = morado.y * (1.0 - m) + azul.y * m;
    skyB = morado.z * (1.0 - m) + azul.z * m;
}
else if (t < h5)
{
    double m = (t - h4) / (h5 - h4);
    m = m * m * (3.0 - 2.0 * m);

    skyR = azul.x * (1.0 - m) + azulDeep.x * m;
    skyG = azul.y * (1.0 - m) + azulDeep.y * m;
    skyB = azul.z * (1.0 - m) + azulDeep.z * m;
}
else
{
    skyR = azulDeep.x;
    skyG = azulDeep.y;
    skyB = azulDeep.z;
}

// Curva para dar más presencia al horizonte
double horizonBoost = Math.exp(-t * 8.0);

skyR += 0.25 * horizonBoost;
skyG += 0.18 * horizonBoost;
skyB += 0.05 * horizonBoost;

// Exposición global - Esto está bien aquí porque solo afecta al cielo base
double skyExposure = 0.35;

skyR *= skyExposure;
skyG *= skyExposure;
skyB *= skyExposure;

// Debajo del horizonte
if (unitDir.y < 0.0)
{
    skyR = 0.001;
    skyG = 0.005;
    skyB = 0.02;
}

resultColor.set(skyR, skyG, skyB);
        
    }
}