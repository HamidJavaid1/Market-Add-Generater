<div align="center">

# Market Add Generator

**An intelligent Android application for generating professional market advertisements powered by AI**

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

</div>

---

## 📱 Overview

Market Add Generator is a sophisticated Android application designed to streamline the creation of marketing advertisements. Leveraging modern Android development practices and AI integration, this app provides users with an intuitive interface to generate compelling marketing content for their products and services.

## ✨ Features

- **AI-Powered Content Generation**: Utilize advanced AI models to create engaging marketing copy
- **Product Management**: Easily add, edit, and organize product information
- **Local Database**: Securely store your marketing materials with Room database
- **Modern UI/UX**: Built with Jetpack Compose for a smooth, responsive experience
- **Material Design**: Follows Google's Material Design 3 guidelines
- **Offline Support**: Core functionality available without internet connection

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Async Processing**: Coroutines + Flow
- **Build System**: Gradle (Kotlin DSL)

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: [Koala | 2024.1.1](https://developer.android.com/studio) or later
- **Android SDK**: API Level 24+ (Android 7.0)
- **JDK**: Version 17 or higher
- **Git**: For version control

## 🚀 Getting Started

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/HamidJavaid1/Market-Add-Generater.git
   cd Market-Add-Generater
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **File → Open**
   - Navigate to the cloned project directory
   - Allow Android Studio to sync and build the project

3. **Configure Environment Variables**
   - Copy the example environment file:
     ```bash
     cp .env.example .env
     ```
   - Edit `.env` and add your API keys:
     ```
     GEMINI_API_KEY=your_api_key_here
     ```

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click the **Run** button in Android Studio or use:
     ```bash
     ./gradlew installDebug
   ```

## 📁 Project Structure

```
Market-Add-Generater/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── api/           # API services and networking
│   │   │   │   ├── data/          # Database models and DAOs
│   │   │   │   ├── ui/            # Compose UI screens and ViewModels
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/               # Resources (layouts, strings, etc.)
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/           # Instrumentation tests
│   │   └── test/                  # Unit tests
│   ├── build.gradle.kts           # App-level build configuration
│   └── proguard-rules.pro         # ProGuard configuration
├── gradle/
│   └── libs.versions.toml         # Dependency versions
├── build.gradle.kts               # Project-level build configuration
├── settings.gradle.kts            # Project settings
├── .env.example                   # Environment variables template
├── .gitignore
└── README.md
```

## 🔧 Configuration

### API Keys

The application requires API keys for AI-powered features. Configure these in your `.env` file:

- `GEMINI_API_KEY`: Your Google Gemini API key for content generation

### Build Variants

The project supports multiple build variants:
- **debug**: For development and testing
- **release**: For production builds

## 🧪 Testing

Run the test suite using Gradle:

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## 📸 Screenshots

*(Add screenshots of your application here)*

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Hamid Javaid**

- GitHub: [@HamidJavaid1](https://github.com/HamidJavaid1)

## 🙏 Acknowledgments

- Google for the Gemini API
- The Android Open Source Project
- Jetpack Compose community

## 📞 Support

If you encounter any issues or have questions, please:
- Open an issue on GitHub
- Contact: [hamidjavaid1@example.com]

---

<div align="center">

**⭐ Star this repository if you find it helpful!**

Made with ❤️ by Hamid Javaid

</div>
