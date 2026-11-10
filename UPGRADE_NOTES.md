# AttendEase – Professional Upgrade (Pass 2)

## Included in this pass
- Faculty dashboard now displays live counts for sections, students, subjects and classes held.
- Student dashboard now uses a prominent overall attendance card, progress indicator and subject-wise attendance cards.
- Attendance register includes student name/USN search, Present/Absent bulk actions and live Present/Absent/Unmarked counts.
- New attendance entries start unmarked. Saving is blocked until every student has been marked, preventing accidental default-Present submissions.
- Attendance values are captured on the UI thread and saved together in a Room transaction, avoiding background-thread access to Android views and partial attendance saves.
- Attendance register Back action returns to the subject/date workflow rather than unexpectedly leaving the section.
- Empty roster state added for attendance.
- Room migration uses a safe fallback faculty ID if no faculty account exists.
- Gradle wrapper aligned to Gradle 8.9, compatible with the project's Android Gradle Plugin 8.7.3.

## Important
Open this folder in Android Studio and let Gradle sync. Run the app and verify the existing database migration before replacing your working copy. This environment did not have the Android SDK / Gradle distribution available to complete an emulator build, so a successful local build still needs to be confirmed.
