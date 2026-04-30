# AGENTS.md - EcoScanner Android App

## Build Commands
- **Build Debug APK**: `./gradlew assembleDebug`
- **Build Release APK**: `./gradlew assembleRelease`

## Key Architecture
- **App Entry**: `MainActivity.kt` - Uses ZXing embedded for barcode scanning
- **Navigation**: `NavigationState` object (in MainActivity.kt) - Persists page state across recompositions
- **Product Data**: `ProductRepository` - Uses StateFlow for reactive updates
- **Carbon Tracking**: `CarbonFootprintTracker` - Accumulates CO₂ and km saved
- **Carbon Calculator**: `CarbonCalculator` in `LocationHelper.kt` - Estimates CO₂ from origin country
- **Supabase**: Client managed by `SupabaseManager`, auth + Postgrest via BOM 3.0.0
- **OpenFoodFacts API**: Parsed in `OpenFoodFactsApi.kt` - Returns `ProductData`

## Important Patterns

### State Management
- Do NOT use `remember` with delegated StateFlow in Compose (causes smart cast errors)
- Use `.collectAsState()` for StateFlow or copy locally: `val current = stateFlow.value`

### Navigation Fix
- When calling `setContent()` in `onActivityResult`, always set `NavigationState.currentPage` FIRST to maintain session state
- NEVER call `setContent` after successful scan - only update `NavigationState.currentPage`

### Session Persistence (Critical)
- Supabase Auth config must include:
  ```kotlin
  install(Auth) {
      enableLifecycleCallbacks = false  // Prevents session clearing on background
      alwaysAutoRefresh = true           // Keeps tokens valid
  }
  ```
- Load session manually on app start:
  ```kotlin
  CoroutineScope(Dispatchers.Main).launch {
      supabase.auth.loadFromStorage()
      val hasSession = supabase.auth.currentSessionOrNull() != null
      NavigationState.currentPage = if (hasSession) "escaner" else "Registro"
  }
  ```
- Without `enableLifecycleCallbacks = false`, Auth clears session when app goes to background

### Carbon Footprint from API
- **Path**: `product.ecoscore_data.agribalyse.co2_total` (kg CO₂/kg)
- **Equivalent**: Generated as `kmCar = co2 * 2500` (1kg CO₂ ≈ 2500km car)
- **UI**: Card in `Datos.kt` - displays "Huella de carbono" with Impacto climático + Equivalencia
- **Fields**: `ProductData` has `carbonFootprint: Double?` and `carbonFootprintEquivalent: String?`

### Carbon Calculator (LocationHelper.kt)
- **Same-country logic**: Products from user's country return CO2=0, distance=0
- Use `calculateCarbonFootprintWithCoordinates(productOrigin, lat, lon, userCountry, weight)` with user's country from geocoder
- Both "espagne" and "espana" map to "España" in `normalizeCountry()` and `translateCountryToSpanish()`
- Compare normalized countries: if `originCountry.equals(userCountryNorm, ignoreCase = true)` → local product

### Supabase Save
- `StatsRepository.saveScan()` always returns failure (bug in decode), but data IS saved in DB
- Error toast is suppressed intentionally - do not re-enable
- User scans saved to `user_scans` table with user_id from auth session

## Compiler Quirks
- K2 compiler disabled: `-Xuse-k2=false` in kotlinOptions
- Serialization plugin (1.9.23) mismatches kotlinx-serialization-json runtime (1.6.3) in app/build.gradle.kts

## Dependencies
- ZXing Android Embedded 4.3.0 - Barcode scanning
- Supabase BOM 3.0.0 - Auth + Postgrest
- Ktor 3.0.0 - HTTP client for OpenFoodFacts API
- Coil 2.5.0 - Image loading
- Kotlin Serialization 1.6.3 - JSON parsing (mismatched with plugin 1.9.23)
- Play Services Location 21.3.0 - Location for carbon calculations

## Constraints
- `minSdk = 24`, `compileSdk = 36`, Java 17
- Material3 for Compose
- Package: `com.example.ecoscanner`