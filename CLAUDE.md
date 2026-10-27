# CLAUDE.md - Contexto del Proyecto: GreatNotes (Android Tablet App)

## 📌 Estado Actual del Proyecto (Milestone 1 Completado)
- **Namespace:** `com.greatnotes`
- **Min SDK:** 26 (Android 8.0)
- **Stack:** Kotlin, Jetpack Compose (Material 3), Room (persistencia de metadatos), Clean Architecture (MVVM, módulo único `app`).
- **Logros clave:** 
  - Capa `domain/` 100% pura (cero imports de Android/Room/Hilt).
  - Persistencia híbrida robusta: Room para metadatos, archivos binarios optimizados en `filesDir` para los trazos vectoriales por página (con protección contra archivos corruptos y prevención de OOM).
  - Estantería (Fase 1 y 2) terminada, probada y con **24/24 tests en verde** (`./gradlew check` pasa exitosamente en debug y release).

---

## 🎯 Siguiente Tarea Inmediata: Fase 3 (Motor de Tinta / Canvas)
El proyecto está listo para arrancar la **Fase 3: El Lienzo de Tinta Digital (Canvas, Stylus y Rechazo de Palma)**.

### Reglas de Arquitectura Obligatorias para la Fase 3:
1. **Latencia y Capas (¡Crítico!):** El motor de tinta debe separar estrictamente los trazos ya confirmados (*committed paths*) de la traza activa que se está dibujando en tiempo real (*live stroke*). Un Canvas de Compose que recomponga toda la lista de miles de trazos en cada evento del lápiz destruirá el rendimiento.
2. **Entrada de Lápiz:** Captura optimizada de `MotionEvent.TOOL_TYPE_STYLUS` (coordenadas, presión, inclinación/tilt).
3. **Rechazo de Palma (*Palm Rejection*):** Filtrar toques accidentales de la muñeca/mano en la pantalla de la tablet Lenovo.
4. **Herramientas base:** Pluma, resaltador (con transparencia) y borrador.

---

## 🛠️ Comandos de Verificación Útiles
- Verificar compilación y tests: `./gradlew check`
- Correr tests unitarios: `./gradlew test`
