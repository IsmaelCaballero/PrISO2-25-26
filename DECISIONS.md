# DECISIONS.md — Decisiones y supuestos del proyecto

Registro de decisiones (estilo ADR) que usan los agentes como configuración persistente.
Responsable final: los 6 miembros del grupo. La IA propone; el humano decide y audita.

## D1. Mapeo 1RF : 1CDU y 1CDU : 1Iteración
- **Fecha:** 19/10/2026 · **Estado:** aceptada
- **Motivo:** simplificación sugerida en el enunciado. Se preserva la incrementalidad vía evolución semver de componentes.
- **Alternativa descartada:** agrupar CUs por módulo en una misma iteración (más PUD puro, pero peor trazabilidad 1:1 exigida en la evaluación).

## D2. Iteración 0 = 1.000 € · Iteración 21 (Transición) = 2.000 €
- Fijado por el enunciado. Equivalencias: 40 h y 80 h humanas a 25 €/h.

## D3. Tarifa humana = 25 €/h; semana = 40 h; equipo = 6 personas (3 parejas)
- **Motivo:** perfil junior; el paralelismo máximo del plan es 3 iteraciones simultáneas (6/semana en Construcción).

## D4. Esfuerzo por CDU con agentes: Baja 14 h · Media 22 h · Alta 36 h (sin agentes: 24/40/64 h)
- Incluye análisis, diseño de interfaces (humano), generación (agente) y revisión (humano).
- Contingencia del 10% sobre iteraciones de CU para contrarrestar el sesgo de optimismo de la IA.

## D5. Lenguaje Java + Maven multi-módulo + SemVer + Git Flow
- Preferencia explícita del claustro; un módulo Maven por componente; ramas `feature/cduXX` → PR → `develop`; nunca commits en `main`.

## D6. Herramientas IA: GitHub Copilot (licencia campus UCLM, 0 €) + OpenCode con modelos gratuitos
- Criterio de selección de modelo: mejor tasa de acierto en Java/Maven (menos rondas de revisión), no tamaño del modelo.
- Coste nominal de mercado de referencia: ~430 €/proyecto (no incurrido).

## D7. Orden de iteraciones por riesgos (Elaboración = CDU16, 17, 13, 06, 12, 19-spike)
- Se mitigan primero los riesgos arquitectónicos: catálogo (dominio), RBAC, agendas, citas, solicitudes e integraciones FHIR/SSO.

## D8. Reglas de arquitectura como guardarailes de los agentes
1. Comunicación entre módulos solo por interfaces de `iso2-common` (programar orientado a interfaces).
2. Capas MVC/servicio-puerto-adaptador dentro de cada módulo.
3. Los contratos publicados no se rompen: SemVer estricto.
4. Acceso a datos clínicos siempre a través de `iso2-seguridad` (auditoría RGPD).
5. DICOM/HL7/FHIR solo tras fachadas de `iso2-integracion`.

## D9. Calendario real
- Iter0: oct (hecha) · Elaboración: 09–20/11 · Construcción: 23/11–11/12 · Transición: 07–14/12 · Entrega: 15/12/2026.

--- Decisiones sesión 2 (07/11/2026, reglas de Ismael) ---

## D10. Dos aplicaciones: servidora + cliente con interfaz por rol
- **Estado:** aceptada. El cliente presenta vistas distintas según rol (paciente/médico/administrativo/adminSistema).
- **v1 simplificada:** una persona = un solo rol = una sola zona de la app (registro en usuarios).
- Cada CDU se desglosa en parte servidora y parte cliente (tabla en PLAN.md §4).

## D11. Login y credenciales locales en `iso2-seguridad`
- Tabla de usuarios con rol único + hash BCrypt + token de sesión (JWT). El SSO corporativo real se deja para CDU19 (integraciones). Justificación: requisito de gestión de usuarios del cliente sin infraestructura externa en el aula.

## D12. Servidor: Spring Boot (REST/JSON) + H2 embebido + JPA
- Justificación: despliegue cero-infraestructura en evaluación; enorme conocimiento del stack por los agentes (menos rondas de revisión = menos tokens y horas de revisión).

## D13. Cliente: JavaFX de escritorio (multiplataforma)
- Encaja con "distintos sistemas operativos de escritorio" del enunciado; web/móvil quedan habilitados en el futuro porque todo pasa por la API REST. Alternativa descartada para v1: cliente web (más infra, fuera de alcance del bloque 1).

## D14. Git Flow estricto + aprobación exclusiva de Ismael en PRs
- Ramas: `main` (solo releases/tags) · `develop` (solo merges de PR aprobados) · `feature/iterXX-CDUyy-…`.
- El agente nunca hace merge ni toca `main`/`develop` directamente.

## D15. Formato de commit: módulo exacto + resumen de tareas
- Título: `tipo(módulo · IterX/CDUyy): descripción`; cuerpo: una línea por tarea realizada (con métricas de tests). Ejemplo canónico en CONTEXT.md §3.

## D16. Pruebas por iteración: casos diseñados por humanos + JUnit 5; informes Surefire + JaCoCo
- Los casos de prueba (tabla TC) los diseña el alumno ANTES de implementar; el agente los traduce a JUnit. `mvn verify` genera Surefire + JaCoCo; umbrales de cobertura por release en PLAN.md.

## D17. SemVer 2.0 en todos los artefactos + Javadoc completo
- `<version>` del módulo en pom + tags Git `vX.Y.Z`. Javadoc obligatorio en todas las clases y paquetes (`package-info.java`); `mvn javadoc:javadoc` sin errores como criterio de aceptación.

## D18. Modelado: diagramas por iteración en StarUML — PENDIENTE DE CONFIRMAR
- Por iteración: casos de uso, secuencia/interacción, clases de análisis y clases de diseño relevantes.
- **Propuesta del agente:** fuente PlantUML en `docs/modelo/iterXX/` (diferenciable, barata en tokens) + exportación a `.staruml` hecha por el equipo. Generar `.staruml` directamente es factible pero frágil y caro (JSON). → Ismael debe confirmar.

## D19. Presupuesto de tokens por iteración y partida en 2 si Alta
- Baja 50–70k · Media 90–130k · Alta 150–220k tokens (6 CUs Altos se dividen en 2 sesiones: servidor | cliente+tests). Si el contexto amenaza con agotarse: avisar, cerrar lo en curso, guardar STATUS.md/PLAN.md y abrir sesión nueva (regla en CONTEXT.md §7).

## D20. Plan de despliegue con canales alfa/beta/final
- alfa 0.1.0→0.2.0-alpha (R1) · beta 0.4.0-beta (R3) y 0.9.0-RC (R4) · final 1.0.0 (R5, 15/12). Detalle y criterios de paso en PLAN.md §3.

## D21. Repo del proyecto: github.com/IsmaelCaballero/PrISO2-25-26
- Todo el trabajo (docs, código, modelo, informes) vive aquí; la wiki enlazará README + ficheros vivos.
