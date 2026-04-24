# AGENTS.md - EcoScanner Android App

## Build Commands
- **Build Debug APK**: `./gradlew assembleDebug`
- **Build Release APK**: `./gradlew assembleRelease`

## Key Architecture

- **App Entry**: `MainActivity.kt` - Uses ZXing embedded for barcode scanning
- **Navigation**: `NavigationState` object (in MainActivity) - Persists page state across recompositions
- **Product Data**: `ProductRepository` - Uses StateFlow for reactive updates
- **Carbon Tracking**: `CarbonFootprintTracker` - Accumulates CO₂ and km saved

## Color Palette (defined in ui/theme/Color.kt)
| Name | Hex | Usage |
|------|-----|-------|
| SpringWood | #F8F6F1 | Background |
| GrayNurse | #E1EAE5 | Secondary background |
| MossGreen | #A7D7B8 | Accent |
| Tradewind | #66B2A0 | Primary buttons |
| Como | #4E796B | Text, dark elements |

## Important Patterns

### State Management
- Do NOT use `remember` with delegated StateFlow in compose (causes smart cast errors)
- Use `.collectAsState()` for StateFlow or copy locally: `val current = stateFlow.value`

### Navigation Fix
- When calling `setContent()` in `onActivityResult`, always set `NavigationState.currentPage` FIRST to maintain session state

### Dependencies
- ZXing Android Embedded 4.3.0 - For barcode scanning
- Supabase - For auth and database
- Kotlin Coroutines + StateFlow - For reactive state

## Important Constraints
- `minSdk = 24` (not all CameraX APIs work below 26)
- Kotlin 2.0.21, compileSdk = 36, Java 17 required
- Material3 for Compose

## Files in Use
- `MainActivity.kt`, `Escaner.kt`, `Datos.kt`, `Estadisticas.kt` - Main UI
- `Registro.kt`, `InicioSesion.kt` - Auth screens
- `ProductData.kt` - Data models and repository
- `OpenFoodFactsApi.kt` - API client
- `CarbonFootprintTracker.kt` - Environmental impact tracking