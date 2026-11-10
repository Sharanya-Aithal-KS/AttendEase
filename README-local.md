# AttendEase — Local multi-student Android app

This edition uses Java + Room (SQLite) only. No Firebase account, internet, or cloud setup is needed.

## Open and run
1. Extract the ZIP.
2. Open the `AttendEase_Local` folder in Android Studio.
3. Wait for Gradle Sync. Use JDK 17 and Android SDK 35.
4. Run on an emulator or Android phone.

## First use
1. Open the app and choose **First-time faculty setup**.
2. Create the faculty username and password. This option disappears after a faculty account is created.
3. Faculty creates sections, adds students and subjects.
4. Faculty chooses a section, subject and any date to take attendance.
5. Mark each student's status and save. Reopening the same subject/date loads existing marks and updates them.

## Student accounts
On the login screen, choose **Student account setup (faculty-managed)**. Select the student's roster record, then create a username and password. The login is linked to that student's local database ID. Students can then sign in and see only their own attendance.

## Local storage limitations
- Data is stored in the app's private SQLite database on this device.
- There is no cloud sync or automatic sharing between different phones.
- If the app is uninstalled or its app data is cleared, the database may be deleted. Backups/export are not included in this starter release.
- Student accounts are local profiles, not secure institutional identity verification. Protect the device and don't share faculty credentials.
- This project is a complete functional foundation; test all workflows on your target Android version before real institutional use.
