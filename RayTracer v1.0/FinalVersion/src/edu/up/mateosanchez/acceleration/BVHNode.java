package edu.up.mateosanchez.acceleration;

import edu.up.mateosanchez.geometry.Intersectable;
import edu.up.mateosanchez.geometry.HitRecord;
import edu.up.mateosanchez.math.Ray;
import java.util.Comparator;
import java.util.List;

public class BVHNode implements Intersectable {
    private final Intersectable left;
    private final Intersectable right;
    private final AABB boundingBox;

    // Constructor que recibe tu lista de objetos y construye el árbol recursivamente
    public BVHNode(List<Intersectable> objects, int start, int end) {
        this.boundingBox = new AABB();

        // 1. Calculamos la caja gigante que envuelve a los objetos actuales
        AABB span = new AABB();
        for (int i = start; i < end; i++) {
            span.setFromMerge(span, objects.get(i).getBoundingBox());
        }

        // 2. Decidimos sobre qué eje vamos a cortar (El más largo de la caja)
        double dx = span.max.x - span.min.x;
        double dy = span.max.y - span.min.y;
        double dz = span.max.z - span.min.z;
        
        int axis = 0; // Por defecto cortamos en X
        if (dy > dx && dy > dz) axis = 1; // Cortamos en Y
        else if (dz > dx && dz > dy) axis = 2; // Cortamos en Z

        // 3. Creamos un comparador para ordenar los objetos sobre ese eje
        final int finalAxis = axis;
        Comparator<Intersectable> comparator = (a, b) -> {
            double minA = finalAxis == 0 ? a.getBoundingBox().min.x : (finalAxis == 1 ? a.getBoundingBox().min.y : a.getBoundingBox().min.z);
            double minB = finalAxis == 0 ? b.getBoundingBox().min.x : (finalAxis == 1 ? b.getBoundingBox().min.y : b.getBoundingBox().min.z);
            return Double.compare(minA, minB);
        };

        int objectSpan = end - start;

        // 4. Casos base y recursión para dividir la lista
        if (objectSpan == 1) {
            // Si solo queda un objeto, se asigna a ambas ramas para terminar la hoja
            left = right = objects.get(start);
        } else if (objectSpan == 2) {
            // Si quedan dos, los ordenamos y asignamos uno a cada rama
            if (comparator.compare(objects.get(start), objects.get(start + 1)) < 0) {
                left = objects.get(start);
                right = objects.get(start + 1);
            } else {
                left = objects.get(start + 1);
                right = objects.get(start);
            }
        } else {
            // Si hay más de dos, ordenamos la sublista, la partimos a la mitad y aplicamos recursividad
            objects.subList(start, end).sort(comparator);
            int mid = start + objectSpan / 2;
            left = new BVHNode(objects, start, mid);
            right = new BVHNode(objects, mid, end);
        }

        // 5. Finalmente, la caja de este nodo se fusiona abarcando a sus dos hijos
        this.boundingBox.setFromMerge(left.getBoundingBox(), right.getBoundingBox());
    }

    @Override
    public AABB getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public boolean intersect(Ray ray, double tMin, double tMax, HitRecord hitRecord) {
        // Si el rayo no toca la caja del nodo, ignoramos toda esta rama al instante
        if (!this.boundingBox.intersect(ray, tMin, tMax)) {
            return false;
        }

        // Revisamos si golpeamos algo en la mitad izquierda
        boolean hitLeft = left.intersect(ray, tMin, tMax, hitRecord);
        
        // TRUCO DE RENDIMIENTO: Si golpeamos la izquierda, limitamos el tMax de la derecha
        // al punto donde chocamos. Así evitamos calcular cosas que están "detrás".
        boolean hitRight = right.intersect(ray, tMin, hitLeft ? hitRecord.t : tMax, hitRecord);

        return hitLeft || hitRight;
    }
}