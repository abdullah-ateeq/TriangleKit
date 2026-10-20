# TriangleKit 🔺

**TriangleKit** is an offline, high-precision Android application designed to solve, classify, and visualize triangles. Built with **100% Kotlin** and modern **Jetpack Compose (Material 3)**, TriangleKit offers step-by-step mathematical solutions, unit conversion, vector triangle visualization, and calculation history.

---

## ✨ Features

- **8 Solving Modes**:
  - **SSS**: Three Sides
  - **SAS**: Two Sides + Included Angle
  - **ASA**: Two Angles + Included Side
  - **AAS**: Two Angles + One Side
  - **SSA**: Two Sides + Non-Included Angle *(Handles the ambiguous case / two solutions)*
  - **Right Triangle**: Specialized Right-angle solver
  - **Base + Height**: Fast area and geometry solver
  - **Auto Solve**: Intelligent flexible solver that automatically determines the appropriate method based on given inputs
- **Comprehensive Outputs**:
  - Complete side lengths ($a, b, c$) and angles ($A^\circ, B^\circ, C^\circ$)
  - Area calculation (Heron's formula & Trigonometric area)
  - Perimeter & Semi-perimeter ($s$)
  - Altitudes / Heights ($h_a, h_b, h_c$)
  - Full Triangle Classification (by Sides: *Equilateral, Isosceles, Scalene*; by Angles: *Acute, Right, Obtuse*)
- **Unit Conversion & Mixed Units**:
  - **Metric Units**: Millimeters (mm), Centimeters (cm), Meters (m), Kilometers (km)
  - **Imperial Units**: Inches (in), Feet (ft), Yards (yd)
  - Supports entering mixed units per side (e.g. $a$ in meters, $b$ in inches)
  - Area conversion support ($m^2, cm^2, mm^2, km^2, in^2, ft^2, yd^2, \text{acres}, \text{hectares}$)
- **High-Precision Math Engine**:
  - Built on top of Kotlin `BigDecimal` and custom series expansions (`BigDecimalMath`) to prevent floating-point rounding errors.
  - Step-by-Step calculation breakdown showing exact formulas used.
- **Dynamic Vector Triangle Diagram**:
  - Real-time scaled triangle visualization drawn using Compose Canvas.
- **History & Persistence**:
  - Automatically saves the last calculations for quick review and restoration.
- **Privacy & Offline First**:
  - Requires **zero network permissions** — 100% offline, privacy-friendly, with no tracking or ads.
- **Modern Android Design**:
  - Jetpack Compose with Material 3 design system.
  - Dark, Light, and Dynamic Color (Material You) theme support.
  - Edge-to-Edge display and haptic feedback.

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose, Material 3
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles
- **State Management**: Kotlin StateFlow & Coroutines
- **Navigation**: Jetpack Navigation Compose
- **Design Pattern**: Domain-Driven Design (Domain solvers, formatters, classifiers, repositories)
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 37 (Android 15 / Latest)

---

## 📂 Project Structure

```text
TriangleKit/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/trianglekit/
│   │   │   │   ├── domain/         # Core solver algorithms, classifiers, unit converters
│   │   │   │   │   ├── TriangleSolver.kt
│   │   │   │   │   ├── TriangleClassifier.kt
│   │   │   │   │   ├── TriangleValidator.kt
│   │   │   │   │   ├── LengthUnitConverter.kt
│   │   │   │   │   └── AreaUnitConverter.kt
│   │   │   │   ├── math/           # High-precision BigDecimal trigonometry engine
│   │   │   │   │   ├── BigDecimalMath.kt
│   │   │   │   │   └── MathConstants.kt
│   │   │   │   ├── model/          # Input/output data models & enum types
│   │   │   │   ├── repository/     # Data persistence (Settings & History)
│   │   │   │   ├── ui/             # Jetpack Compose Screens & UI Components
│   │   │   │   │   ├── home/       # Home Screen with Solver & Diagram
│   │   │   │   │   ├── history/    # Calculation History Screen
│   │   │   │   │   ├── settings/   # App Settings & Preferences Screen
│   │   │   │   │   ├── about/      # App Features & Information Screen
│   │   │   │   │   └── components/ # Reusable UI components (Inputs, Diagrams)
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/                # Vector assets, themes, and resources
│   │   └── test/                   # Unit test suite covering domain logic & math engine
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish or newer
- **JDK**: Java 11 or higher
- **Android SDK**: API Level 35+

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/TriangleKit.git
   cd TriangleKit
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run on an emulator or physical Android device running Android 8.0 (API 26) or higher.

Alternatively, build from terminal:
```bash
./gradlew assembleDebug
```

---

## 🧪 Testing

The project includes an extensive unit test suite covering `BigDecimalMath`, `TriangleSolver`, `TriangleClassifier`, `TriangleValidator`, and unit conversions.

Run unit tests via terminal:
```bash
./gradlew test
```

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.
