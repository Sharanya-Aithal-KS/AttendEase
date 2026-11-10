
package com.example.attendease.data;

import androidx.room.*;
import java.util.*;
import static com.example.attendease.data.Entities.*;

@Dao
public interface AppDao {

 // USER MANAGEMENT

 @Insert
 long addUser(User user);

 @Update int updateUser(User user);
 @Query("UPDATE users SET name = :name, passwordHash = :passwordHash WHERE id = :id")
 int updateUserProfile(long id, String name, String passwordHash);

 @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
 User user(String username);

 @Query("SELECT COUNT(*) FROM users WHERE role = 'FACULTY'")
 int facultyCount();

 @Query("SELECT COUNT(*) FROM users WHERE username = :username")
 int usernameExists(String username);

 @Query("SELECT * FROM users WHERE studentId = :studentId LIMIT 1")
 User userForStudent(long studentId);

 // SECTION MANAGEMENT

 @Insert
 long addSection(Section section);
 @Update int updateSection(Section section);
 @Query("DELETE FROM sections WHERE id = :id AND facultyId = :facultyId")
 int deleteSection(long id, long facultyId);

 @Query("SELECT * FROM sections WHERE facultyId = :facultyId ORDER BY department, semester, name")
 List<Section> sectionsForFaculty(long facultyId);

 @Query("SELECT * FROM sections WHERE id = :id AND facultyId = :facultyId LIMIT 1")
 Section sectionForFaculty(long id, long facultyId);

 // STUDENT MANAGEMENT

 @Insert
 long addStudent(Student student);
 @Update int updateStudent(Student student);
 @Query("DELETE FROM users WHERE studentId = :studentId AND role = 'STUDENT'")
 int deleteStudentLogin(long studentId);
 @Query("DELETE FROM students WHERE id = :id")
 int deleteStudent(long id);

 @Query("SELECT * FROM students WHERE sectionId = :sectionId ORDER BY name")
 List<Student> students(long sectionId);

 @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
 Student student(long id);

 @Query("SELECT * FROM students WHERE usn = :usn LIMIT 1")
 Student studentByUsn(String usn);

 // SUBJECT MANAGEMENT

 @Insert
 long addSubject(Subject subject);
 @Update int updateSubject(Subject subject);
 @Query("DELETE FROM subjects WHERE id = :id")
 int deleteSubject(long id);

 @Query("SELECT * FROM subjects WHERE sectionId = :sectionId ORDER BY name")
 List<Subject> subjects(long sectionId);

 @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
 Subject subject(long id);

 // CLASS SESSION MANAGEMENT

 @Insert
 long addSession(ClassSession session);

 @Query("SELECT * FROM class_sessions WHERE subjectId = :subjectId AND dateKey = :dateKey LIMIT 1")
 ClassSession session(long subjectId, long dateKey);

 @Query("SELECT * FROM class_sessions WHERE sectionId = :sectionId AND dateKey BETWEEN :fromDate AND :toDate ORDER BY dateKey DESC")
 List<ClassSession> sessionsBetween(long sectionId, long fromDate, long toDate);

 @Query("SELECT * FROM class_sessions WHERE sectionId = :sectionId ORDER BY dateKey DESC")
 List<ClassSession> sessions(long sectionId);

 @Query("SELECT * FROM class_sessions WHERE id = :id LIMIT 1")
 ClassSession sessionById(long id);

 // ATTENDANCE MANAGEMENT

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 void saveAttendance(Attendance attendance);

 @Query("SELECT * FROM attendance WHERE studentId = :studentId AND sessionId = :sessionId LIMIT 1")
 Attendance attendance(long studentId, long sessionId);

 @Query("SELECT * FROM attendance WHERE sessionId = :sessionId")
 List<Attendance> attendanceForSession(long sessionId);

 @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY updatedAt DESC")
 List<Attendance> attendanceForStudent(long studentId);

 // OVERALL ATTENDANCE

 @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND status IN ('PRESENT','ABSENT')")
 int totalForStudent(long studentId);

 @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND status = 'PRESENT'")
 int presentForStudent(long studentId);

 // SUBJECT-WISE ATTENDANCE

 @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND sessionId IN (SELECT id FROM class_sessions WHERE subjectId = :subjectId) AND status IN ('PRESENT','ABSENT')")
 int totalForSubject(long studentId, long subjectId);

 @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND sessionId IN (SELECT id FROM class_sessions WHERE subjectId = :subjectId) AND status = 'PRESENT'")
 int presentForSubject(long studentId, long subjectId);
}
