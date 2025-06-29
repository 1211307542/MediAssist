# MediAssist - Android Medical Assistant App

A comprehensive Android application for managing medical appointments, medication reminders, and doctor-patient interactions.

## Features

- User authentication (Login/Register)
- Doctor and Patient role management
- Appointment scheduling and management
- Medication reminders with notifications
- Doctor availability management
- Medical history tracking
- Profile management

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK (API level 21 or higher)
- Google Firebase account

### Installation

1. **Clone the repository**
   ```bash
   git clone <your-repository-url>
   cd MediAssist
   ```

2. **Set up Firebase Configuration**
   
   **Important**: The `google-services.json` file contains sensitive API keys and is not included in this repository for security reasons.
   
   To set up Firebase:
   
   a. Go to [Firebase Console](https://console.firebase.google.com/)
   b. Create a new project or select an existing one
   c. Add an Android app to your Firebase project:
      - Package name: `com.example.mediassist`
      - Download the `google-services.json` file
   d. Place the downloaded `google-services.json` file in the `app/` directory
   
   **Note**: Use the `google-services.json.template` file as a reference for the required structure.

3. **Build and Run**
   ```bash
   # Open the project in Android Studio
   # Or use command line:
   ./gradlew build
   ./gradlew installDebug
   ```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/mediassist/
│   │   ├── data/           # Data models and database
│   │   ├── ui/             # UI components and activities
│   │   ├── broadcast/      # Broadcast receivers
│   │   └── util/           # Utility classes
│   ├── res/                # Resources (layouts, drawables, etc.)
│   └── AndroidManifest.xml
├── google-services.json    # Firebase configuration (not in repo)
└── build.gradle.kts        # App-level build configuration
```

## Security Notes

- **API Keys**: The `google-services.json` file contains sensitive Firebase API keys and should never be committed to version control.
- **Environment Variables**: For production apps, consider using environment variables or encrypted configuration files.
- **ProGuard**: Enable ProGuard/R8 for release builds to obfuscate code and protect sensitive information.

## Dependencies

- Firebase Authentication
- Firebase Firestore
- Firebase Cloud Messaging
- AndroidX libraries
- Material Design components

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## Support

For support and questions, please contact 1211307542@student.mmu.edu.my
