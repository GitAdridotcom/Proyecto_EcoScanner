# AGENTS.md - EcoScanner Android App

## Build Commands
- **Build Debug APK**: `./gradlew assembleDebug`
- **Build Release APK**: `./gradlew assembleRelease`

## Key Architecture

- **App Entry**: `MainActivity.kt` - Uses ZXing embedded for barcode scanning
- **Navigation**: `NavigationState` object (in MainActivity) - Persists page state across recompositions
- **Product Data**: `ProductRepository` - Uses StateFlow for reactive updates
- **Carbon Tracking**: `CarbonFootprintTracker` - Accumulates CO₂ and km saved
- **Carbon Calculator**: `CarbonCalculator` in `LocationHelper.kt` - Estimates CO₂ from origin country
- **Supabase**: Client managed by `SupabaseManager`, auth + Postgrest via BOM 3.0.0

## Important Patterns

### State Management
- Do NOT use `remember` with delegated StateFlow in compose (causes smart cast errors)
- Use `.collectAsState()` for StateFlow or copy locally: `val current = stateFlow.value`

### Navigation Fix
- When calling `setContent()` in `onActivityResult`, always set `NavigationState.currentPage` FIRST to maintain session state

## Compiler Quirks
- K2 compiler disabled: `-Xuse-k2=false` in kotlinOptions (Kotlin 2.0+ default is K2)
- Serialization plugin version (1.9.23) mismatches Kotlin version (2.0.21) in app/build.gradle.kts

## Dependencies
- ZXing Android Embedded 4.3.0 - Barcode scanning
- Supabase BOM 3.0.0 - Auth + Postgrest
- Ktor 3.0.0 - HTTP client for OpenFoodFacts API
- Coil 2.5.0 - Image loading
- Kotlin Serialization 1.9.23 - JSON parsing
- Play Services Location 21.3.0 - Location for carbon calculations

## Constraints
- `minSdk = 24`, `compileSdk = 36`, Java 17
- Material3 for Compose
- Package: `com.example.ecoscanner`
