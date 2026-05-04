# 🛡️ AndroidVulnScanner

An Android security analysis tool that scans all installed apps for **exported components without permission protection** — a common Android security vulnerability.

---

## 📁 Project Structure

```
AndroidVulnScanner/
├── componentanalyzer/          ← AAR Library (reusable)
│   └── src/main/
│       ├── AndroidManifest.xml         (queries block for API 30+)
│       └── java/com/vulnscanner/analyzer/
│           ├── data/
│           │   ├── model/
│           │   │   └── Models.kt       (VulnerableApp, ScanResult, ScanState, …)
│           │   └── repository/
│           │       ├── ComponentAnalyzer.kt    (core scan engine)
│           │       ├── RiskScoreCalculator.kt  (weighted scoring)
│           │       └── ScanRepository.kt       (Flow-based pipeline)
│           ├── domain/
│           │   └── usecase/
│           │       └── StartScanUseCase.kt     (clean arch use case)
│           └── presentation/
│               └── RiskGaugeView.kt            (custom Canvas gauge)
│
└── samplehost/                 ← Demo App
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/vulnscanner/sample/
            ├── VulnScannerApp.kt       (Hilt Application)
            ├── MainActivity.kt
            ├── di/
            │   └── AppModule.kt        (Hilt DI wiring)
            └── ui/
                ├── viewmodel/
                │   └── ScanViewModel.kt
                ├── screens/
                │   └── ScanScreen.kt   (full Compose UI)
                ├── components/
                │   ├── VulnerableAppCard.kt    (expandable list item)
                │   └── StatsRow.kt
                └── theme/
                    └── Theme.kt
```

---

## 🚀 How to Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK API 35 installed
- A device or emulator running Android 7.0+ (API 24+)

### Steps
1. **Clone / open** the project in Android Studio.
2. Let Gradle sync finish (first sync downloads dependencies).
3. Select the **`samplehost`** run configuration.
4. Click ▶️ Run on your device/emulator.
5. Tap **"Start Scan"** to begin analysis.

---

## 📦 Using the AAR Library

### Option 1 — Module dependency (same project)
```kotlin
// samplehost/build.gradle.kts
dependencies {
    implementation(project(":componentanalyzer"))
}
```

### Option 2 — Standalone AAR
1. Build the AAR:
   ```
   Android Studio → Build → Make Module 'componentanalyzer'
   ```
   Output: `componentanalyzer/build/outputs/aar/componentanalyzer-debug.aar`

2. Copy the `.aar` into your target project's `libs/` folder.

3. Add to `build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(files("libs/componentanalyzer-debug.aar"))
       // Required transitive dependency:
       implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
   }
   ```

4. **Critical** — add the `<queries>` block to your host app's `AndroidManifest.xml`:
   ```xml
   <queries>
       <intent>
           <action android:name="android.intent.action.MAIN" />
       </intent>
   </queries>
   ```

### Minimal Usage
```kotlin
// 1. Create repository
val repository = ScanRepository(context.packageManager)
val useCase = StartScanUseCase(repository)

// 2. Collect the Flow in a ViewModel
viewModelScope.launch {
    useCase().collect { state ->
        when (state) {
            is ScanState.Scanning -> { /* update progress */ }
            is ScanState.Success  -> { /* use state.result */ }
            is ScanState.Error    -> { /* handle error */ }
            else -> {}
        }
    }
}

// 3. Show the gauge in Compose
RiskGaugeView(score = result.riskScore)
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     samplehost (App)                     │
│                                                          │
│  ScanScreen (Compose UI)                                 │
│       │  collectAsStateWithLifecycle()                   │
│       ▼                                                  │
│  ScanViewModel  ←── Hilt @HiltViewModel                  │
│       │  viewModelScope.launch { useCase().collect{} }   │
│       ▼                                                  │
├───────────────────────────────────────────────────────── │
│                  componentanalyzer (AAR)                 │
│                                                          │
│  StartScanUseCase   ← domain layer                       │
│       │  operator fun invoke(): Flow<ScanState>          │
│       ▼                                                  │
│  ScanRepository     ← data layer                         │
│       │  flow { emit(Scanning…); emit(Success…) }        │
│       │  .flowOn(Dispatchers.IO)   ← off main thread     │
│       ▼                                                  │
│  ComponentAnalyzer  ← PackageManager queries             │
│  RiskScoreCalculator← pure scoring logic                 │
└─────────────────────────────────────────────────────────┘
```

### Key patterns
| Pattern | Where used | Why |
|---|---|---|
| MVVM | ScanViewModel ↔ ScanScreen | Separates UI from logic |
| Clean Architecture | UseCase layer | ViewModel never touches repository directly |
| Repository pattern | ScanRepository | Single source of truth for scan data |
| Kotlin Flow | `flow { }.flowOn(IO)` | Reactive, non-blocking, lifecycle-safe |
| StateFlow | `_scanState: MutableStateFlow` | Compose-friendly state holder |
| Hilt DI | AppModule | Testable, swappable dependencies |

---

## ⚙️ Android 11+ Package Visibility

**The Problem:** Android 11 (API 30) introduced restrictions on `PackageManager.getInstalledApplications()`. Without a `<queries>` block, only system apps are returned.

**The Fix:** Both the library and host app manifest declare:
```xml
<queries>
    <intent>
        <action android:name="android.intent.action.MAIN" />
    </intent>
</queries>
```
This tells the OS: "I need to see apps that have a MAIN launcher intent" — which covers all user-installed apps.

---

## 🔍 Vulnerability Detection Logic

For each installed app, the analyzer fetches:
```kotlin
val packageInfo = packageManager.getPackageInfo(
    packageName,
    PackageManager.GET_ACTIVITIES or
    PackageManager.GET_SERVICES or
    PackageManager.GET_RECEIVERS
)
```

A component is flagged **vulnerable** when:
```kotlin
component.exported == true && component.permission == null
```

| Component | Risk | Why |
|---|---|---|
| Exported Activity (no permission) | HIGH | Any app can launch it, bypass auth screens |
| Exported Service (no permission) | CRITICAL | Any app can bind/start it, execute background code |
| Exported Receiver (no permission) | MEDIUM | Any app can send it broadcasts, trigger behaviors |

---

## 🎯 Risk Score Calculation

Weighted scoring (capped at 100):

| Component Type | Points per vuln |
|---|---|
| Activity | +6 |
| Service | +8 (highest — background exec) |
| BroadcastReceiver | +4 |

```kotlin
score = min(100, sum of all weighted vulnerabilities)
```

| Score Range | Risk Level |
|---|---|
| 0–25 | 🟢 Low |
| 26–50 | 🟡 Medium |
| 51–75 | 🟠 High |
| 76–100 | 🔴 Critical |

---

## 🎨 RiskGaugeView — Custom Canvas Implementation

The gauge is a pure Compose `Canvas`-based component that mirrors Android View's `onDraw(canvas: Canvas)`:

- **Semi-circle arc** (180°) drawn with `drawArc()`
- **Gradient track**: Green (0) → Yellow (50) → Red (100) using `Brush.sweepGradient()`
- **Animated needle**: `Animatable(0f).animateTo(score)` with `tween(1200ms)`
- **Tick marks**: calculated at fixed degree intervals with trigonometry
- **Center hub**: layered circles for depth effect
- **Score label + risk level**: Compose `Text` below the canvas

```kotlin
// Animation equivalent to ValueAnimator.ofInt(0, score)
val animatedScore = remember { Animatable(0f) }
LaunchedEffect(score) {
    animatedScore.animateTo(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1200)
    )
}
```

---

## 🔄 Flow Lifecycle Safety

```kotlin
// ViewModel:
viewModelScope.launch {         // cancelled when ViewModel is cleared
    useCase()                   // cold Flow starts here
        .catch { e -> ... }     // error handling in pipeline
        .collect { state ->     // each emission updates StateFlow
            _scanState.value = state
        }
}

// UI:
val state by viewModel.scanState.collectAsStateWithLifecycle()
// collectAsStateWithLifecycle() pauses collection when app is backgrounded
```

✅ **No memory leaks** — `viewModelScope` tied to ViewModel lifecycle  
✅ **Rotation safe** — ViewModel survives config changes  
✅ **Background safe** — `collectAsStateWithLifecycle()` respects lifecycle  
✅ **No UI blocking** — `flowOn(Dispatchers.IO)` pushes work off main thread

---

## 📱 UI Flow

```
Launch App
    │
    ▼
[Idle Screen]
 • Shield icon
 • "Start Scan" button
    │ tap
    ▼
[Scanning Screen]
 • Circular progress indicator
 • "Scanning… XX%" text
 • Current package name
 • Linear progress bar
    │ complete
    ▼
[Results Screen]
 • RiskGaugeView (animated 0→score)
 • Stats row (scanned / vulnerable / issues)
 • LazyColumn of VulnerableAppCards
   └── Tap card → expand component list
       ├── Activity1 · no permission
       ├── Service1  · no permission
       └── Receiver1 · no permission
 • Top-right refresh → reset to Idle
```
