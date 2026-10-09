# Gestor de Gastos

<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/b1b08787-2c41-43cf-828d-d87daaece4c3" alt="Captura 1" width="180"></td>
    <td><img src="https://github.com/user-attachments/assets/ef1c2e90-f95c-4755-9cee-169ebddb020e" alt="Captura 2" width="180"></td>
    <td><img src="https://github.com/user-attachments/assets/6b893962-3df7-44db-a29e-9782388cd6bc" alt="Captura 3" width="180"></td>
    <td><img src="https://github.com/user-attachments/assets/58243095-6ec7-4d9b-822b-3378dc711419" alt="Captura 4" width="180"></td>
    <td><img src="https://github.com/user-attachments/assets/39c14bec-d628-47d4-a059-cf6907f6af76" alt="Captura 5" width="180"></td>
    <td><img src="https://github.com/user-attachments/assets/10f3b627-a99c-49db-97ba-fe7f19c871dd" alt="Captura 6" width="180"></td>
  </tr>
</table>

Aplicación Android para gestionar gastos compartidos en grupo (viajes, convivencias, casa, etc.). Permite crear un grupo, registrar gastos entre sus integrantes y calcular automáticamente quién le debe a quién.

Construida con Kotlin y Jetpack Compose, con sincronización en tiempo real mediante Firebase Firestore.

## Características

- **Grupos compartidos**: creá un grupo y compartí un código de 6 caracteres para que otros se unan.
- **Personas**: agregá, editá y eliminá integrantes del grupo.
- **Gastos por categoría**: registrá gastos con descripción, monto, categoría, pagante y forma de reparto. Categorías con ícono y color propios (supermercado, comida, transporte, servicios, etc.).
- **Cálculo de deudas**: reparto equitativo o por deudores específicos, con redondeo a centavos.
- **Sugerencia de transferencias**: algoritmo que minimiza las transferencias necesarias para saldar todas las deudas.
- **Historial de liquidaciones**: guardá el estado de una liquidación (gastos, personas y transferencias) y vaciá el grupo.
- **Notas del grupo**: bloc de notas con editor de texto enriquecido (negrita, itálica, subrayado, listas).
- **Sincronización en tiempo real** entre dispositivos vía Firestore, con *pull-to-refresh* para forzar lectura desde el servidor.
- **Persistencia de sesión**: el grupo activo se recuerda localmente con DataStore, así al reabrir la app seguís en el mismo grupo.
- **Pantalla de bienvenida** (splash screen) con Material 3.

## Tecnologías

| Área | Detalle |
| --- | --- |
| Lenguaje | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM (ViewModel + StateFlow) con repositorios |
| Backend | Firebase Firestore (tiempo real) + Firebase Analytics |
| Persistencia local | Jetpack DataStore (Preferences) |
| Editor de texto | [richeditor-compose](https://github.com/MohamedRejeb/compose-rich-editor) |
| Build | Gradle (Kotlin DSL) + AGP 8.10.0 + KSP |
| SDK | minSdk 26, target/compileSdk 35 |

## Estructura del proyecto

```text
app/src/main/java/com/example/gestordegastos/
├── MainActivity.kt      # Punto de entrada y navegación raíz
├── data/
│   ├── cache/           # Modelos de caché local
│   ├── datastore/       # Persistencia de preferencias (grupo activo)
│   └── repository/      # Repositorios Firestore (gastos, grupos, personas, notas, historial)
├── di/                  # ViewModelFactories (inyección manual)
├── domain/
│   ├── model/           # Modelos de dominio (Gasto, Persona, Grupo, Nota, Historial, ...)
│   └── usecase/         # Casos de uso
├── ui/
│   ├── components/      # Composables reutilizables
│   ├── screens/         # Pantallas (Start, Gastos, Notas, Historial)
│   └── theme/           # Tema, colores y tipografía
├── utils/               # Utilidades (moneda, fechas, conversores)
└── viewmodel/           # ViewModels de cada pantalla
```

## Modelo de datos (Firestore)

Los datos se organizan por grupo:

```text
grupos/{grupoId}
├── codigoGrupo: String
├── gastos/{gastoId}          # descripcion, montoCentavos, categoria, paganteId, porcentaje, deudoresIds, fecha
├── personas/{personaId}      # nombre
├── notas/{notaId}            # titulo, contenido, fechaCreacion
└── historial/{historialId}   # fechaLiquidacion, totalCentavos, gastos[], personas[], transferencias[]
```

> Los montos se almacenan como **centavos enteros (`Long`)** para evitar errores de redondeo con punto flotante.

## Requisitos

- Android Studio (versión reciente compatible con AGP 8.10).
- JDK 11 o superior.
- Un proyecto de Firebase con **Cloud Firestore** y **Google Analytics** habilitados.

## Configuración

1. Cloná el repositorio.
2. Creá un proyecto en la [consola de Firebase](https://console.firebase.google.com/).
3. Registrá una app Android con el package `com.example.gestordegastos`.
4. Descargá el archivo `google-services.json` y colocalo en `app/google-services.json`.
   > Este archivo está en `.gitignore` y no se versiona: cada entorno debe aportar el suyo.
5. Habilitá **Cloud Firestore** en modo producción o prueba según corresponda.

## Compilar y ejecutar

```bash
# Compilar la app en modo debug
./gradlew assembleDebug

# Instalar en un dispositivo/emulador conectado
./gradlew installDebug

# Ejecutar los tests unitarios
./gradlew test

# Ejecutar los tests instrumentados (requiere dispositivo/emulador)
./gradlew connectedAndroidTest
```

También podés abrir el proyecto en Android Studio y usar el botón **Run**.

## Cómo se calculan las deudas

El cálculo se hace en `GastoViewModel` (`calcularTransferencias`):

1. Se parte de un saldo en cero para cada persona.
2. Por cada gasto se determina qué deudores corresponden (los indicados explícitamente, o todos los integrantes según la configuración).
3. El monto se divide con **redondeo hacia arriba** (`divideHalfUp`) para no perder centavos.
4. Se ajustan los saldos del pagante (acreedor) y de los deudores.
5. Con los saldos finales se resuelve un algoritmo **greedy**: cada deudor paga al acreedor disponible hasta saldar su deuda, minimizando la cantidad de transferencias.

## Notas

- El archivo `app/google-services.json` es obligatorio para compilar; sin él, el plugin `google-services` fallará.
- La app arranca con una lectura forzada desde el servidor (`Source.SERVER`) antes de suscribirse a los listeners en tiempo real, evitando mostrar datos obsoletos de caché.
