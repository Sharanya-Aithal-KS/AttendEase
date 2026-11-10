
package com.example.attendease.data;

import androidx.room.*;

public class Entities {

    @Entity(
            tableName = "users",
            indices = {@Index(value = {"username"}, unique = true)}
    )
    public static class User {
        @PrimaryKey(autoGenerate = true)
        public long id;

        public String username;
        public String passwordHash;
        public String name;
        public String role;
        public String studentUsn;
        public long studentId;

        public User(String username, String passwordHash, String name,
                    String role, String studentUsn, long studentId) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.name = name;
            this.role = role;
            this.studentUsn = studentUsn;
            this.studentId = studentId;
        }
    }

    @Entity(
            tableName = "sections",
            indices = {@Index("facultyId")}
    )
    public static class Section {
        @PrimaryKey(autoGenerate = true)
        public long id;

        public String name;
        public String department;
        public String semester;
        public String academicYear;

        // Faculty who owns/manages this section
        public long facultyId;

        public Section(String name, String department, String semester,
                       String academicYear, long facultyId) {
            this.name = name;
            this.department = department;
            this.semester = semester;
            this.academicYear = academicYear;
            this.facultyId = facultyId;
        }
    }

    @Entity(
            tableName = "students",
            indices = {@Index(value = {"usn"}, unique = true)},
            foreignKeys = @ForeignKey(
                    entity = Section.class,
                    parentColumns = "id",
                    childColumns = "sectionId",
                    onDelete = ForeignKey.CASCADE
            )
    )
    public static class Student {
        @PrimaryKey(autoGenerate = true)
        public long id;

        public String name;
        public String usn;
        public String email;
        public long sectionId;

        public Student(String name, String usn, String email, long sectionId) {
            this.name = name;
            this.usn = usn;
            this.email = email;
            this.sectionId = sectionId;
        }
    }

    @Entity(
            tableName = "subjects",
            foreignKeys = @ForeignKey(
                    entity = Section.class,
                    parentColumns = "id",
                    childColumns = "sectionId",
                    onDelete = ForeignKey.CASCADE
            ),
            indices = {@Index("sectionId")}
    )
    public static class Subject {
        @PrimaryKey(autoGenerate = true)
        public long id;

        public String name;
        public String code;
        public long sectionId;

        public Subject(String name, String code, long sectionId) {
            this.name = name;
            this.code = code;
            this.sectionId = sectionId;
        }
    }

    @Entity(
            tableName = "class_sessions",
            foreignKeys = {
                    @ForeignKey(
                            entity = Section.class,
                            parentColumns = "id",
                            childColumns = "sectionId",
                            onDelete = ForeignKey.CASCADE
                    ),
                    @ForeignKey(
                            entity = Subject.class,
                            parentColumns = "id",
                            childColumns = "subjectId",
                            onDelete = ForeignKey.CASCADE
                    )
            },
            indices = {
                    @Index("sectionId"),
                    @Index("subjectId"),
                    @Index(value = {"subjectId", "dateKey"}, unique = true)
            }
    )
    public static class ClassSession {
        @PrimaryKey(autoGenerate = true)
        public long id;

        public long sectionId;
        public long subjectId;
        public long dateKey;
        public long createdAt;

        public ClassSession(long sectionId, long subjectId,
                            long dateKey, long createdAt) {
            this.sectionId = sectionId;
            this.subjectId = subjectId;
            this.dateKey = dateKey;
            this.createdAt = createdAt;
        }
    }

    @Entity(
            tableName = "attendance",
            foreignKeys = {
                    @ForeignKey(
                            entity = Student.class,
                            parentColumns = "id",
                            childColumns = "studentId",
                            onDelete = ForeignKey.CASCADE
                    ),
                    @ForeignKey(
                            entity = ClassSession.class,
                            parentColumns = "id",
                            childColumns = "sessionId",
                            onDelete = ForeignKey.CASCADE
                    )
            },
            indices = {
                    @Index(value = {"studentId", "sessionId"}, unique = true),
                    @Index("sessionId")
            }
    )
    public static class Attendance {
        @PrimaryKey(autoGenerate = true)
        public long id;

        public long studentId;
        public long sessionId;
        public String status; // PRESENT or ABSENT
        public long updatedAt;

        public Attendance(long studentId, long sessionId,
                          String status, long updatedAt) {
            this.studentId = studentId;
            this.sessionId = sessionId;
            this.status = status;
            this.updatedAt = updatedAt;
        }
    }
}
