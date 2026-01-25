<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" alt="Naivety Logo" width="120" height="120">
</p>

<h1 align="center">Naivety</h1>

<p align="center">
  <strong>A Modern E-Book Reader for Android</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#screenshots">Screenshots</a> •
  <a href="#installation">Installation</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#building">Building</a> •
  <a href="#contributing">Contributing</a> •
  <a href="#license">License</a>
</p>

---

## 📖 About

**Naivety** is a feature-rich, modern e-book reader application for Android that supports both **PDF** and **EPUB** formats. Built with Jetpack Compose and following Material Design 3 guidelines, Naivety provides a seamless reading experience with comprehensive reading statistics, customizable themes, and an intuitive user interface.

Whether you're a casual reader or a book enthusiast, Naivety helps you organize your digital library, track your reading progress, and achieve your reading goals.

---

## ✨ Features

### 📚 Multi-Format Support
- **PDF Reader** - Full-featured PDF viewing with smooth scrolling and page navigation
- **EPUB Reader** - Native EPUB support with reflowable text and customizable typography
- **16KB Page Size Compliant** - Optimized for Android 15+ requirements

### 📊 Reading Statistics & Tracking
- **Reading Heatmap** - Visualize your reading activity over time (GitHub-style contribution graph)
- **Reading Streaks** - Track consecutive days of reading to build habits
- **Pages Read Counter** - Monitor your total pages read across all books
- **Time Tracking** - See how much time you've spent reading
- **Achievement System** - Unlock achievements as you reach reading milestones

### 🎨 Customization
- **Multiple Reading Modes**
  - Page-by-Page (Horizontal/Vertical)
  - Continuous Scroll
  - Chapter Scroll
- **Typography Settings** (EPUB)
  - Font family selection
  - Font size adjustment
  - Line spacing control
  - Text alignment options
  - Font weight customization
- **Display Settings**
  - Multiple color themes (Light, Dark, Sepia, and more)
  - Brightness control
  - Night mode
- **Screen Orientation** - Portrait, Landscape, or Free rotation

### 📑 Organization
- **Custom Lists** - Create and organize books into personalized collections
- **Bookmarks** - Save and quickly navigate to important pages
- **Reading Progress** - Automatically saves your last read position
- **Search** - Find books in your library quickly

### 🌐 Browse & Discover
- **Open Library Integration** - Browse millions of books from Open Library
- **Book Details** - View book information, ratings, and descriptions
- **Save to Lists** - Add discovered books to your reading lists

### 🔐 Account & Sync
- **Google Sign-In** - Secure authentication with your Google account
- **Guest Mode** - Use the app without creating an account
- **Firebase Integration** - Cloud-based user data management

### 🎯 Additional Features
- **Splash Screen** - Beautiful animated app launch experience
- **Offline Support** - Read your downloaded books without internet
- **AdMob Integration** - Sustainable free app with non-intrusive ads

---

## 📱 Screenshots

| Home Screen | PDF Reader | EPUB Reader |
|:-----------:|:----------:|:-----------:|
| Library view with your books | Feature-rich PDF viewing | Customizable EPUB reading |

| Reading Stats | Achievements | Browse |
|:-------------:|:------------:|:------:|
| Track your reading habits | Unlock reading milestones | Discover new books |

---

## 📥 Installation

### From Google Play Store
<a href="https://play.google.com/store/apps/details?id=com.abundance.naivety">
  <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="200">
</a>

### From Source
See the [Building](#building) section below.

---

## 🛠 Tech Stack

### Core Technologies
| Technology | Purpose |
|------------|---------|
| **Kotlin** | Primary programming language |
| **Jetpack Compose** | Modern declarative UI toolkit |
| **Material Design 3** | UI design system |

### Android Jetpack
| Component | Purpose |
|-----------|---------|
| **Room** | Local database for books, bookmarks, and reading stats |
| **Navigation Compose** | In-app navigation |
| **ViewModel** | UI state management |
| **Lifecycle** | Lifecycle-aware components |
| **Paging 3** | Efficient data loading for large lists |
| **DataStore** | Preferences storage |
| **Splash Screen API** | Modern splash screen implementation |

### Dependency Injection
| Library | Purpose |
|---------|---------|
| **Dagger Hilt** | Dependency injection framework |

### Networking
| Library | Purpose |
|---------|---------|
| **Retrofit** | REST API client |
| **OkHttp** | HTTP client with logging |
| **Gson** | JSON serialization/deserialization |

### Document Reading
| Library | Purpose |
|---------|---------|
| **AndroidPdfViewer** | PDF rendering (16KB compliant fork) |
| **Readium Kotlin Toolkit** | EPUB parsing and rendering |
| **AndroidX WebKit** | WebView for EPUB display |

### Firebase
| Service | Purpose |
|---------|---------|
| **Firebase Auth** | User authentication |
| **Firebase Firestore** | Cloud database |
| **Firebase Crashlytics** | Crash reporting |
| **Firebase Analytics** | Usage analytics |

### UI & Media
| Library | Purpose |
|---------|---------|
| **Coil** | Image loading |
| **Lottie** | Animated illustrations |
| **Accompanist** | Compose UI utilities |

### Monetization
| Service | Purpose |
|---------|---------|
| **Google AdMob** | Advertisement integration |

---

## 🏗 Architecture

Naivety follows **Clean Architecture** principles with **MVVM** (Model-View-ViewModel) pattern:

```
app/
├── ads/                    # AdMob integration
├── auth/                   # Authentication states
├── data/                   # Room database, DAOs, entities
├── di/                     # Hilt dependency injection modules
├── epub/                   # EPUB reader components
│   ├── EpubHtmlWrapper     # HTML/CSS styling for EPUB
│   ├── EpubPreferencesManager
│   ├── EpubResourceLoader  # Resource loading utilities
│   ├── EpubViewerState     # UI state for EPUB reader
│   └── ReadiumManager      # Readium toolkit wrapper
├── models/                 # Data models
├── navigation/             # Navigation graph and destinations
├── network/                # API interfaces and models
├── repository/             # Data repositories
├── ui/
│   ├── components/         # Reusable UI components
│   │   ├── epub/          # EPUB-specific components
│   │   └── pdf/           # PDF-specific components
│   ├── pdf/               # PDF reader states and enums
│   ├── screens/           # App screens
│   └── theme/             # Material theme configuration
├── utils/                  # Utility classes
├── viewmodels/             # ViewModels for each feature
├── AuthActivity.kt         # Authentication flow
├── AuthMainScreen.kt       # Login/Register UI
├── EpubReaderActivity.kt   # EPUB reader activity
├── MainScreenActivity.kt   # Main app container
├── NaivetyApplication.kt   # Application class
├── PdfViewerActivity.kt    # PDF reader activity
└── WalkthroughScreen.kt    # Onboarding screens
```

### Data Flow
```
UI (Compose) → ViewModel → Repository → Data Source (Room/Network)
```

---

## 🔨 Building

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17** or higher
- **Android SDK** with API level 35
- **Gradle 9.1.0**

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/naivety.git
   cd naivety
   ```

2. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add an Android app with package name `com.abundance.naivety`
   - Download `google-services.json` and place it in `app/`
   - Enable Authentication (Google Sign-In) and Firestore

3. **Configure AdMob** (Optional)
   - Create an AdMob account at [AdMob Console](https://admob.google.com)
   - Update ad unit IDs in `AdManager.kt`

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

### Build Variants

| Variant | Description |
|---------|-------------|
| `debug` | Development build with debugging enabled |
| `release` | Production build with ProGuard minification |

### Generating Signed APK/Bundle

1. Create a keystore file for signing
2. Update `app/build.gradle.kts` with your keystore details
3. Run:
   ```bash
   ./gradlew bundleRelease
   ```

---

## 📋 Requirements

| Requirement | Minimum | Recommended |
|-------------|---------|-------------|
| Android Version | 8.0 (API 26) | 14.0+ (API 34+) |
| RAM | 2 GB | 4 GB+ |
| Storage | 100 MB | 200 MB+ |

---

## 🔒 Privacy & Permissions

Naivety requests the following permissions:

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Browse books, sync data, load images |
| `READ_EXTERNAL_STORAGE` | Access PDF/EPUB files |
| `WAKE_LOCK` | Keep screen on while reading |

**Data Collection**: We collect anonymous usage analytics through Firebase Analytics to improve the app. No personal reading data is shared with third parties.

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful commit messages
- Write documentation for public APIs
- Add tests for new features

---

## 📄 License

```
Copyright 2024-2026 Abundance

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 📞 Contact & Support

- **Email**: support@abundance.com
- **Issues**: [GitHub Issues](https://github.com/yourusername/naivety/issues)
- **Play Store**: [Leave a Review](https://play.google.com/store/apps/details?id=com.abundance.naivety)

---

## 🙏 Acknowledgments

- [Readium Foundation](https://readium.org/) - EPUB toolkit
- [AndroidPdfViewer](https://github.com/barteksc/AndroidPdfViewer) - PDF rendering
- [Open Library](https://openlibrary.org/) - Book metadata API
- [Material Design](https://m3.material.io/) - Design system
- All open-source contributors

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/yourusername">Abundance</a>
</p>

<p align="center">
  <a href="#naivety">Back to Top ↑</a>
</p>
