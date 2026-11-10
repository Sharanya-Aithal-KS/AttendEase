# AttendEase – Smart Student Attendance Management System

<p align="center">
  <strong>Attendance, made effortless.</strong>
  <br/>
  A smarter way for faculty to manage classes and for students to stay on top of attendance.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Language-Java-orange?logo=java" />
  <img src="https://img.shields.io/badge/Database-Room%20%7C%20SQLite-blue" />
  <img src="https://img.shields.io/badge/IDE-Android%20Studio-green?logo=androidstudio" />
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" />
</p>

## Overview

**AttendEase** is an Android-based student attendance management application designed to simplify attendance tracking and academic monitoring.

The application provides separate interfaces for faculty and students. Faculty members can manage sections, students, subjects, and attendance records, while students can monitor their attendance percentage and subject-wise attendance performance.

Built using Java and Android's Room persistence library with SQLite, AttendEase provides a structured approach to managing attendance data locally.

## Features

### Faculty Module
- Faculty account creation and login.
- Create and manage academic sections.
- Add, edit, and delete student records.
- Create, edit, and delete subjects.
- Mark students as Present or Absent.
- Bulk attendance marking.
- Search students in attendance registers.
- View and edit previously recorded attendance.
- Generate section-wise attendance reports.
- Filter reports by date range.
- Export attendance reports as CSV files.
- Manage faculty profile and password.

### Student Module
- Secure student login.
- View overall attendance percentage.
- Monitor subject-wise attendance.
- View attendance history.
- Identify attendance shortages.
- Access personal profile information.

### Additional Features
- Separate faculty and student navigation.
- Light Mode, Dark Mode, and System Default themes.
- Persistent theme preferences.
- Local database storage using Room and SQLite.
- Organized attendance records.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Android application development |
| Android Studio | Development environment |
| Room Persistence Library | Local database management |
| SQLite | Database storage |
| XML / Android Views | User interface |
| Material Components | UI components |
| SharedPreferences | Saving user preferences |
| Git & GitHub | Version control |

## Application Screenshots

Screenshots of the application will be added here.

<!-- Add screenshots to a folder named screenshots and update the image paths below. -->

| Faculty Dashboard | Student Dashboard |
|---|---|
| Add screenshot here | Add screenshot here |

| Attendance Register | Attendance Reports |
|---|---|
| Add screenshot here | Add screenshot here |

## Project Structure

```text
AttendEase/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/attendease/
│   │       │       └── Application source files
│   │       │
│   │       ├── res/
│   │       │   ├── drawable/
│   │       │   ├── layout/
│   │       │   ├── mipmap/
│   │       │   └── values/
│   │       │
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle
│
├── gradle/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
├── .gitignore
├── LICENSE
└── README.md
```

## Getting Started

### Prerequisites

- Android Studio
- JDK 21 (for the current project setup)
- Git
- Android Emulator or a physical Android device

### Installation

**1. Clone the repository**

```bash
git clone https://github.com/Sharanya-Aithal-KS/AttendEase.git
```

**2. Open the project**

Open Android Studio and select:

`File → Open → AttendEase`

**3. Configure Gradle**

Allow Android Studio to complete the Gradle sync. Use JDK 21 as the Gradle JVM for this project.

**4. Run the application**

- Start an Android Emulator or connect an Android device.
- Select the application configuration.
- Click the Run button.

## Database

AttendEase uses **Room Persistence Library**, which provides an abstraction layer over SQLite for structured local data storage.

The database manages application information such as:

- Faculty accounts
- Student accounts
- Academic sections
- Subjects
- Attendance sessions
- Attendance records

## Future Enhancements

- Cloud-based database synchronization.
- Online attendance management.
- Attendance notifications and reminders.
- Advanced academic analytics.
- PDF report generation.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Author

**Sharanya Aithal KS**

- GitHub: [@Sharanya-Aithal-KS](https://github.com/Sharanya-Aithal-KS)

---

<p align="center">
  Made with dedication using Java and Android Studio.
</p>
