package iia.dsl.framework.tasks;

/**
 * Enumeración que define las categorías principales de tareas en el framework.
 * - ROUTER: Tareas que dirigen mensajes a diferentes destinos (Splitter,
 * Merger, Filter, etc.).
 * - MODIFIER: Tareas que alteran el contenido o metadatos del mensaje
 * (Enricher, Slimmer).
 * - TRANSFORMER: Tareas que transforman la estructura del mensaje (Translator,
 * Aggregator).
 */
public enum TaskType {
    ROUTER,
    MODIFIER,
    TRANSFORMER
}
