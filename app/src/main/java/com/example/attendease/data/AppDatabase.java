
package com.example.attendease.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;

import androidx.sqlite.db.SupportSQLiteDatabase;

import static com.example.attendease.data.Entities.*;

@Database(
        entities = {
                User.class,
                Section.class,
                Student.class,
                Subject.class,
                ClassSession.class,
                Attendance.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

 public abstract AppDao dao();

 private static volatile AppDatabase INSTANCE;

 // Migration from database version 1 to version 2
 private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
  @Override
  public void migrate(SupportSQLiteDatabase database) {

   database.execSQL(
           "ALTER TABLE sections ADD COLUMN facultyId INTEGER NOT NULL DEFAULT 0"
   );
   database.execSQL(
           "CREATE INDEX IF NOT EXISTS index_sections_facultyId ON sections(facultyId)"
   );

   // Assign existing sections to the first faculty account
   database.execSQL(
           "UPDATE sections SET facultyId = " +
                   "COALESCE((SELECT id FROM users WHERE role = 'FACULTY' ORDER BY id LIMIT 1), 0)"
   );
  }
 };

 public static AppDatabase get(Context context) {

  if (INSTANCE == null) {
   synchronized (AppDatabase.class) {

    if (INSTANCE == null) {

     INSTANCE = Room.databaseBuilder(
                     context.getApplicationContext(),
                     AppDatabase.class,
                     "attendease_local.db"
             )
             .addMigrations(MIGRATION_1_2)
             .build();
    }
   }
  }

  return INSTANCE;
 }
}
