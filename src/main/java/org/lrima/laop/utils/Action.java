package org.lrima.laop.utils;

/**
 * Une acton. Peut être nimporte quoi et elle est utilisée dans les listeners de notre application
 * @param <T> le type que l'action va prendre en paramètre
 */
public interface Action<T> {
    void handle(T value);
}
