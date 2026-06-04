Custom 3D Ray Tracer in JavaUn motor de renderizado por trazado de rayos (Ray Tracer) desarrollado desde cero en Java nativo, sin dependencias externas de gráficos. El proyecto soporta geometrías complejas, sombreado suave (Smooth Shading), múltiples tipos de iluminación con atenuación física, mapeo de texturas avanzadas (incluyendo canales alfa y mapas de relieve) y estructuras de aceleración espacial.Características PrincipalesGeometría Avanzada: Soporte para esferas y triángulos arbitrarios con interpolación de normales por vértice.Múltiples Fuentes de Luz:PointLight: Luces puntuales omnidireccionales con atenuación por distancia.DirectionalLight: Luces infinitas paralelas (ideales para simular el sol).SpotLight: Conos de luz con decaimiento suavizado (smoothstep).AreaLight: Luces de área con muestreo optimizado para simular sombras suaves (soft shadows).Pipeline de Materiales Completo (Material):Texturizado difuso linealizado (sRGB a Lineal).Mapas de rugosidad/brillo (map_Ns), mapas especulares (map_Ks) y mapas emisivos (map_Ke).Alpha Clipping: Procesamiento nativo de transparencias mediante canal alfa incrustado o mapas independientes (map_d).Carga de Escenas: Parsers optimizados para archivos estándar de la industria wavefront .obj y archivos de materiales .mtl asociados, con soporte para triangulación automática (Triangle Fan).Post-procesamiento de Imagen (ImageWriter):Tone Mapping de Reinhard para compresión de alto rango dinámico (HDR).Corrección Gamma lineal (gamma = 2.2) para una visualización natural en monitores modernos.Rendimiento: Estructuras de aceleración basadas en Cajas de Colisión Alineadas con los Ejes (AABB).Estructura del ProyectoEl código está organizado de forma modular bajo el paquete base edu.up.mateosanchez:src/edu/up/mateosanchez/
│
├── acceleration/
│   └── AABB.java           # Cajas de colisión para aceleración de intersecciones
│
├── camera/
│   └── Camera.java         # Configuración de perspectiva, posición y matriz de vista
│
├── core/
│   └── Scene.java          # Contenedor principal de objetos, luces y cámara
│
├── geometry/
│   ├── Intersectable.java  # Interfaz base para objetos de la escena
│   ├── HitRecord.java      # Estructura que almacena los datos del impacto del rayo
│   ├── Sphere.java         # Primitiva esférica con cálculo analítico
│   └── Triangle.java       # Primitiva triangular (Algoritmo Möller-Trumbore)
│
├── lights/
│   ├── Light.java          # Interfaz común para el comportamiento lumínico
│   ├── PointLight.java     # Luz puntual con atenuación cuadrática
│   ├── DirectionalLight.java
│   ├── SpotLight.java      # Luz focalizada con cortes interno/externo
│   └── AreaLight.java      # Luz de área para sombras suaves (ThreadSafe)
│
├── materials/
│   ├── Material.java       # Gestor de propiedades ópticas y mapas de texturas
│   └── Texture.java        # Cargador de imágenes (sRGB, lineal, canales alfa y luminancia)
│
├── math/
│   ├── Vector2d.java       # Vectores bidimensionales (coordenadas UV)
│   ├── Vector3d.java       # Álgebra lineal 3D (puntos, normales, colores)
│   └── Ray.java            # Estructura de rayo lineal matemático P(t) = O + tD
│
└── utils/
    ├── ObjParser.java      # Parser de mallas geométricas poligonales (.obj)
    ├── MtlParser.java      # Parser de librerías de materiales complejos (.mtl)
    └── ImageWriter.java    # Procesador de buffer sRGB, Tone Mapping y guardado en disco (PNG)
Cómo Funciona el Renderizado (Pipeline Matemático)Generación de Rayos: La Camera lanza un Ray primario por cada píxel de la pantalla hacia el espacio 3D de la Scene.Pruebas de Intersección:Se evalúa si el rayo impacta la caja protectora AABB de los objetos para descartar cálculos innecesarios.Si entra, se calcula la intersección exacta (analítica en Sphere y mediante coordenadas baricéntricas en Triangle).Mapeo y Texturas: En el punto de impacto, se extraen las coordenadas UV y se calculan las texturas lineales mediante el método de corrección de curvas de color:$$C_{\text{linear}} = \left(\frac{C_{\text{srgb}} + 0.055}{1.055}\right)^{2.4}$$Cálculo de Iluminación: Se acumulan los componentes Difusos, Especulares (Phong/Blinn-Phong) y Emisivos calculando los vectores de dirección y distancia hacia cada Light.Post-Procesamiento: El color final pasa por el ImageWriter, aplicando el decaimiento HDR Reinhard:$$\text{Color}_{\text{final}} = \frac{\text{Color}_{\text{hdr}}}{\text{Color}_{\text{hdr}} + 1.0}$$Y finalmente la codificación gamma de monitor de $1/2.2$ antes de escribir el archivo PNG de 8 bits por canal.Ejemplo de UsoA continuación se muestra cómo inicializar una escena, importar una malla con sus materiales y renderizar el resultado:import edu.up.mateosanchez.core.Scene;
import edu.up.mateosanchez.camera.Camera;
import edu.up.mateosanchez.lights.PointLight;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.utils.ImageWriter;

public class Main {
    public static void main(String[] args) {
        int width = 1920;
        int height = 1080;

        // 1. Configurar Cámara y Escena
        Camera camera = new Camera(new Vector3d(0, 5, 10), new Vector3d(0, 0, 0), 60);
        Scene scene = new Scene(camera, 0.02); // 0.02 de intensidad ambiental

        // 2. Importar modelo .obj (Carga automáticamente texturas y .mtl del mismo directorio)
        scene.importObj("assets/models/character.obj");

        // 3. Añadir Luces
        scene.addLight(new PointLight(
            new Vector3d(5, 10, 5),      // Posición
            new Vector3d(10.0, 10.0, 9.0) // Color / Intensidad HDR
        ));

        // 4. Inicializar el Lienzo de Dibujo
        ImageWriter writer = new ImageWriter(width, height);

        // 5. Bucle de Renderizado (Simplificado)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // [Aquí lanzarías tus rayos usando scene.camera y comprobarías colisiones]
                // Vector3d pixelColor = RayTracer.trace(scene, x, y);
                // writer.setPixel(x, y, pixelColor.x, pixelColor.y, pixelColor.z);
                
                // Ejemplo temporal de color de fondo:
                writer.setPixel(x, y, 0.1, 0.1, 0.15); 
            }
        }

        // 6. Guardar el resultado en disco
        writer.save("output/render.png");
        System.out.println("Renderizado guardado exitosamente");
    }
}
Requisitos del SistemaJava Development Kit (JDK): Versión 8 o superior. Se recomienda JDK 11 o 17 debido a optimizaciones de rendimiento en operaciones matemáticas y multihilo concurrentes si se expande con hilos concurrentes.Memoria: Dependiendo de la resolución de las texturas cargadas por el archivo .mtl, se recomienda aumentar el Heap Size de la JVM (-Xmx2g).Este software ha sido desarrollado con fines académicos y de optimización matemática para simulación de transporte de luz en entornos virtuales 3D de alta fidelidad.