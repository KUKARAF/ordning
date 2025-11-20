# Project Context

## Purpose
Offline-first Android ticket management app that reads PDF tickets, creates calendar events with travel details, and syncs with Google services. The app displays QR codes prominently before/after travel times and enables multi-user ticket sharing via Google Drive.

## Tech Stack
- **Primary**: Kotlin (Android)
- **UI**: Android Jetpack Compose
- **Offline Storage**: Room Database
- **PDF Processing**: PdfiumAndroid or similar
- **Authentication**: Google OAuth
- **Calendar Integration**: Google Calendar API
- **File Sync**: Google Drive API
- **Build System**: Gradle with GitHub Actions
- **Testing**: JUnit, Espresso

## Project Conventions

### Code Style
- Follow Kotlin coding conventions
- Use Android Architecture Components (ViewModel, LiveData/StateFlow)
- Repository pattern for data access
- Dependency injection with Hilt
- MVVM architecture

### Architecture Patterns
- Clean Architecture with separation of concerns
- Repository pattern for data sources
- UseCase pattern for business logic
- Offline-first design with sync capabilities

### Testing Strategy
- Unit tests for business logic (JUnit)
- Integration tests for repositories
- UI tests for critical flows (Espresso)
- Test coverage minimum 80%

### Git Workflow
- Feature branches from main
- Conventional commits (feat:, fix:, refactor:)
- PR reviews required
- Semantic versioning

## Domain Context
- **Tickets**: PDF files containing travel information (train/bus tickets)
- **Events**: Calendar entries with travel details extracted from tickets
- **Sync**: Multi-user sharing via Google Drive folder "ordnung_ticketoza"
- **Display**: Lockscreen integration for QR codes 1 hour before/after travel
- **Timezones**: Automatic timezone detection based on departure location

## Important Constraints
- **Offline-first**: Core functionality must work without internet
- **Battery efficiency**: Minimal background processing
- **Privacy**: Local storage first, optional cloud sync
- **Performance**: Fast PDF processing and QR code extraction
- **Security**: Proper OAuth token handling

## External Dependencies
- **Google OAuth**: User authentication
- **Google Calendar API**: Event creation and management
- **Google Drive API**: Ticket file synchronization
- **Android System**: Lockscreen integration, notifications
- **GitHub Actions**: Automated APK building and releases
