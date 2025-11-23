# Ordnung - Smart Ticket Management App

## Overview

Ordnung is an Android application that intelligently manages your tickets and events with offline-first architecture, automatic cloud synchronization, and smart lockscreen integration.

## Features

### 🏗️ Build & Deployment
- **GitHub Actions CI/CD**: Automated APK building on every push to main branch
- **Gradle Build System**: Simplified build configuration with minimal dependencies
- **No Complex Frameworks**: Eliminated Room, Hilt, and other unnecessary complexity for reliable builds

### 📱 Core Functionality

#### 🎫 Ticket Management
- **PDF Ticket Processing**: Automatically scans and processes PDF tickets
- **QR Code Display**: Shows QR codes at full width over PDF content for easy scanning
- **Local Storage**: Offline-first architecture stores all tickets locally on device
- **Smart Time Display**: Automatically shows current/active tickets based on time and date

#### 🔒 Lockscreen Integration
- **Active Ticket Display**: Shows current ticket directly on lockscreen
- **QR Code Overlay**: Displays QR codes prominently over PDF content
- **Full Width Display**: QR codes shown at maximum readable width
- **Time-Based Activation**: Only displays tickets that are currently valid

#### ☁️ Google Drive Integration
- **Automatic Upload**: Auto-shares all processed tickets to Google Drive
- **Calendar-Based Sharing**: Shares with all people present in relevant calendar events
- **Smart Organization**: Files organized by event and date in Drive

#### 📅 Google Calendar Integration
- **Event Creation**: Automatically creates calendar events based on ticket information
- **Special Format Support**: Properly handles destination format (e.g., `>> destination`)
- **Multi-Calendar Support**: Option to select one or multiple calendars for event sharing
- **Attendee Detection**: Automatically identifies and includes all relevant people

### 🏪 Offline-First Architecture
- **Local Priority**: All core functionality works without internet connection
- **Background Sync**: Synchronizes with Google services when connection is available
- **Reliable Storage**: No database dependency - uses efficient local file storage

## Technical Implementation

### Build Configuration
- **Android Gradle Plugin**: Latest stable version
- **Kotlin**: Primary development language
- **Jetpack Compose**: Modern UI toolkit
- **Target SDK**: Optimized for recent Android versions
- **Minimal Dependencies**: Only essential Android components included

### Key Integrations
- **Google Drive API**: Automatic file synchronization
- **Google Calendar API**: Event creation and management
- **PDF Processing**: Native PDF rendering and QR code extraction
- **Lockscreen API**: System integration for always-on display

## Getting Started

### Prerequisites
- Android Studio or command-line Android SDK
- Java 17 JDK
- Google APIs configured (Drive, Calendar)

### Build Instructions
```bash
# Clone the repository
git clone https://github.com/KUKARAF/ordning.git
cd ordning

# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug
```

### Installation
The APK is automatically built and available as an artifact in GitHub Actions for every successful build.

## Project Structure

```
app/
├── src/main/
│   ├── java/          # Kotlin source code
│   ├── res/           # Android resources
│   └── assets/        # PDF and ticket assets
├── build.gradle       # Simplified build configuration
└── proguard-rules.pro # Code obfuscation rules
```

## Development Philosophy

This project follows a **minimal complexity** approach:
- Remove unnecessary frameworks and dependencies
- Focus on core functionality
- Prioritize reliability and offline capability
- Use native Android APIs where possible

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make minimal, focused changes
4. Test thoroughly with the simplified build system
5. Submit a pull request

## License

[Add your license information here]

---

**Note**: This app is designed with privacy and reliability in mind. All ticket processing happens locally on your device, with cloud synchronization as an optional convenience feature.