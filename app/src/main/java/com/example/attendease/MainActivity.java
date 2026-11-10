
package com.example.attendease;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.attendease.data.*;
import com.example.attendease.data.Entities.*;
import com.google.android.material.button.MaterialButton;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends AppCompatActivity {

 private AppDatabase db;
 private AppDao dao;

 private ExecutorService io = Executors.newSingleThreadExecutor();

 private LinearLayout body;

 private User current;
 private Section section;
 private Subject subject;

 private final SimpleDateFormat fmt =
         new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

 private int blue, accent, ink, muted, green, red, background, white, card, border, amber;

 private void applyPalette() {
  boolean dark = (getResources().getConfiguration().uiMode &
          Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
  blue = dark ? Color.rgb(35, 25, 20) : Color.rgb(56, 37, 29);       // chocolate
  accent = dark ? Color.rgb(212, 147, 104) : Color.rgb(184, 115, 78); // copper
  ink = dark ? Color.rgb(247, 235, 224) : Color.rgb(56, 37, 29);
  muted = dark ? Color.rgb(190, 172, 157) : Color.rgb(123, 103, 88);
  green = dark ? Color.rgb(110, 231, 168) : Color.rgb(22, 132, 91);
  red = dark ? Color.rgb(255, 133, 133) : Color.rgb(208, 68, 68);
  background = dark ? Color.rgb(23, 19, 16) : Color.rgb(244, 233, 222);
  white = dark ? Color.rgb(41, 32, 26) : Color.WHITE;
  card = dark ? Color.rgb(41, 32, 26) : Color.WHITE;
  border = dark ? Color.rgb(83, 62, 48) : Color.rgb(225, 207, 190);
  amber = dark ? Color.rgb(255, 196, 107) : Color.rgb(168, 103, 22);
 }

 private GradientDrawable rounded(int color, int stroke, int radius) {
  GradientDrawable d = new GradientDrawable();
  d.setColor(color);
  d.setCornerRadius(radius * getResources().getDisplayMetrics().density);
  if (stroke != 0) d.setStroke((int)(1 * getResources().getDisplayMetrics().density), stroke);
  return d;
 }

 @Override
 public void onCreate(Bundle b) {
  super.onCreate(b);

  String themeChoice = getSharedPreferences("attendease_settings", MODE_PRIVATE).getString("theme", "system");
  AppCompatDelegate.setDefaultNightMode("light".equals(themeChoice) ? AppCompatDelegate.MODE_NIGHT_NO :
          "dark".equals(themeChoice) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
  applyPalette();
  getWindow().setStatusBarColor(blue);
  getWindow().setNavigationBarColor(background);
  db = AppDatabase.get(this);
  dao = db.dao();

  login();
 }

 // ------------------------------------------------
 // COMMON UI METHODS
 // ------------------------------------------------


 private void shell(String title) {
  applyPalette();

  LinearLayout page = new LinearLayout(this);
  page.setOrientation(1);
  page.setBackgroundColor(background);

  // Top toolbar
  LinearLayout toolbar = new LinearLayout(this);
  toolbar.setGravity(Gravity.CENTER);
  toolbar.setOrientation(1);
  toolbar.setPadding(dp(20), dp(18), dp(20), dp(18));
  toolbar.setBackgroundColor(blue);

  // Centered app name
  TextView brand = txt("AttendEase", 23, accent, true);
  brand.setGravity(Gravity.CENTER);
  toolbar.addView(
          brand,
          new LinearLayout.LayoutParams(-1, -2)
  );

  // App tagline
  TextView subtitle = txt(
          "SMART STUDENT ATTENDANCE MANAGER",
          10,
          Color.WHITE,
          true
  );
  subtitle.setGravity(Gravity.CENTER);
  subtitle.setPadding(0, dp(5), 0, 0);

  toolbar.addView(
          subtitle,
          new LinearLayout.LayoutParams(-1, -2)
  );

  page.addView(
          toolbar,
          new LinearLayout.LayoutParams(-1, -2)
  );

  // Main content
  ScrollView sc = new ScrollView(this);
  sc.setFillViewport(true);

  body = new LinearLayout(this);
  body.setOrientation(1);
  body.setPadding(dp(18), dp(18), dp(18), dp(30));

  sc.addView(body);
  page.addView(
          sc,
          new LinearLayout.LayoutParams(-1, 0, 1)
  );

  // Bottom navigation
  if (current != null) {
   LinearLayout nav = new LinearLayout(this);
   nav.setOrientation(0);
   nav.setPadding(dp(8), dp(7), dp(8), dp(7));
   nav.setBackgroundColor(card);

   String[] labels = "FACULTY".equals(current.role)
           ? new String[]{"Home", "Sections", "Reports", "Profile"}
           : new String[]{"Home", "History", "Profile"};

   Runnable[] actions = "FACULTY".equals(current.role)
           ? new Runnable[]{
           this::facultyHome,
           this::sectionsScreen,
           this::reportsScreen,
           this::profileScreen
   }
           : new Runnable[]{
           this::studentHome,
           this::studentHistory,
           this::profileScreen
   };

   for (int i = 0; i < labels.length; i++) {
    final Runnable action = actions[i];

    MaterialButton item = new MaterialButton(this);
    item.setText(labels[i]);
    item.setAllCaps(false);
    item.setTextSize(11);
    item.setInsetTop(0);
    item.setInsetBottom(0);
    item.setMinHeight(dp(42));

    item.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(blue)
    );

    item.setTextColor(Color.WHITE);

    LinearLayout.LayoutParams navLp =
            new LinearLayout.LayoutParams(0, dp(44), 1);

    navLp.setMargins(dp(2), 0, dp(2), 0);

    nav.addView(item, navLp);
    item.setOnClickListener(v -> action.run());
   }

   page.addView(nav);
  }

  setContentView(page);
 }


 private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

 private TextView txt(String s, int z, int c, boolean b) {
  TextView t = new TextView(this);
  t.setText(s); t.setTextSize(z); t.setTextColor(c);
  if (b) t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
  return t;
 }

 private void head(String s) {
  TextView t = txt(s, 19, ink, true);
  t.setGravity(Gravity.CENTER);
  t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
  t.setPadding(dp(2), dp(15), dp(2), dp(9));

  body.addView(t, new LinearLayout.LayoutParams(-1, -2));
 }

 private void note(String s) {
  TextView t = txt(s, 14, muted, false);
  t.setGravity(Gravity.CENTER);
  t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
  t.setPadding(dp(2), dp(3), dp(2), dp(8));
  t.setLineSpacing(dp(2), 1f);

  body.addView(t, new LinearLayout.LayoutParams(-1, -2));
 }

 private LinearLayout panel() {
  LinearLayout box = new LinearLayout(this);
  box.setOrientation(1);
  box.setPadding(dp(16), dp(15), dp(16), dp(15));
  box.setBackground(rounded(card, border, 18));
  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
  lp.bottomMargin = dp(12);
  body.addView(box, lp);
  return box;
 }

 private void statTile(LinearLayout parent, String value, String label, int valueColor) {
  LinearLayout tile = new LinearLayout(this);
  tile.setOrientation(1);
  tile.setPadding(dp(14), dp(13), dp(10), dp(13));
  tile.setBackground(rounded(background, border, 15));
  TextView number = txt(value, 23, valueColor, true);
  tile.addView(number);
  TextView caption = txt(label, 12, muted, false);
  caption.setPadding(0, dp(3), 0, 0);
  tile.addView(caption);
  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
  lp.setMargins(dp(3), dp(3), dp(3), dp(3));
  parent.addView(tile, lp);
 }

 private void progressLine(LinearLayout parent, int progress, int color) {
  ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
  bar.setMax(100);
  bar.setProgress(Math.max(0, Math.min(100, progress)));
  bar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
  bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(border));
  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(7));
  lp.topMargin = dp(9);
  parent.addView(bar, lp);
 }

 private EditText field(String s) {
  EditText e = new EditText(this);
  e.setSingleLine(true);
  e.setTextSize(15);
  e.setTextColor(ink);
  e.setHintTextColor(muted);
  e.setHint(s);
  e.setPadding(dp(15), dp(11), dp(15), dp(11));
  e.setBackground(rounded(card, border, 14));
  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
  lp.bottomMargin = dp(12);
  body.addView(e, lp);
  return e;
 }

 private void btn(String s, Runnable r) {
  MaterialButton b = new MaterialButton(this);
  b.setText(s);
  b.setTextSize(14);
  b.setAllCaps(false);
  b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
  b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent));
  b.setTextColor(Color.WHITE);
  b.setCornerRadius(dp(14));
  b.setElevation(dp(1));
  b.setInsetTop(0); b.setInsetBottom(0);
  b.setMinHeight(dp(50));
  b.setOnClickListener(v -> r.run());
  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
  lp.bottomMargin = dp(10);
  body.addView(b, lp);
 }

 private void toast(String s) {

  Toast.makeText(
          this,
          s,
          Toast.LENGTH_SHORT
  ).show();
 }

 private void fail(Exception e) {

  new AlertDialog.Builder(this)
          .setTitle("Error")
          .setMessage(e.getMessage())
          .setPositiveButton("OK", null)
          .show();
 }

 private String hash(String p) {

  try {

   byte[] d = MessageDigest.getInstance("SHA-256")
           .digest(p.getBytes("UTF-8"));

   StringBuilder s = new StringBuilder();

   for (byte x : d) {
    s.append(String.format("%02x", x));
   }

   return s.toString();

  } catch (Exception e) {
   throw new RuntimeException(e);
  }
 }

 // ------------------------------------------------
 // LOGIN SCREEN
 // ------------------------------------------------

 private void login() {

  current = null;

  shell("AttendEase");
  head("Attendance, made effortless.");
  note("A smarter way for faculty to manage classes and for students to stay on top of attendance.");
  note("Choose how you would like to continue.");

  btn("Faculty Login", () -> showLoginForm("FACULTY"));

  btn("Student Login", () -> showLoginForm("STUDENT"));

  io.execute(() -> {

   boolean noFaculty = dao.facultyCount() == 0;

   runOnUiThread(() -> {

    if (noFaculty) {

     btn("First-time Faculty Setup", this::createFaculty);
    }
   });
  });
 }

 // ------------------------------------------------
 // SEPARATE FACULTY AND STUDENT LOGIN
 // ------------------------------------------------

 private void showLoginForm(String role) {

  String title = role.equals("FACULTY")
          ? "Faculty Login"
          : "Student Login";

  shell(title);

  head(title);

  EditText u = field("Username");

  EditText p = field("Password");
  p.setInputType(129);

  btn("Login", () -> {

   String username = u.getText().toString().trim();
   String password = p.getText().toString();

   if (username.isEmpty() || password.isEmpty()) {
    toast("Enter username and password");
    return;
   }

   io.execute(() -> {

    User user = dao.user(username);

    runOnUiThread(() -> {

     if (user == null
             || !role.equals(user.role)
             || !user.passwordHash.equals(hash(password))) {

      toast("Invalid " + title + " credentials");
      return;
     }

     current = user;

     if ("FACULTY".equals(current.role)) {
      facultyHome();
     } else {
      studentHome();
     }
    });
   });
  });

  btn("Back", this::login);
 }

 // ------------------------------------------------
 // FACULTY ACCOUNT CREATION
 // ------------------------------------------------

 private void createFaculty() {

  shell("Faculty Registration");

  head("Create Faculty Account");

  note("Register a faculty member with a unique username and password.");

  EditText name = field("Faculty name");

  EditText username = field("Username");

  EditText password = field("Password (minimum 6 characters)");

  btn("Create Faculty Account", () -> {

   String n = name.getText().toString().trim();
   String u = username.getText().toString().trim();
   String p = password.getText().toString();

   if (n.isEmpty() || u.isEmpty() || p.length() < 6) {

    toast("Fill all fields. Password must have at least 6 characters.");
    return;
   }

   io.execute(() -> {

    if (dao.usernameExists(u) > 0) {

     runOnUiThread(() ->
             toast("Username already exists. Choose another.")
     );

     return;
    }

    try {

     long facultyId = dao.addUser(
             new User(
                     u,
                     hash(p),
                     n,
                     "FACULTY",
                     "",
                     0
             )
     );

     runOnUiThread(() -> {

      toast("Faculty account created. Faculty ID: " + facultyId);

      if (current != null
              && "FACULTY".equals(current.role)) {

       facultyHome();

      } else {

       login();
      }
     });

    } catch (Exception e) {

     runOnUiThread(() -> fail(e));
    }
   });
  });

  btn("Back", () -> {

   if (current != null
           && "FACULTY".equals(current.role)) {

    facultyHome();

   } else {

    login();
   }
  });
 }

 // ------------------------------------------------
 // FACULTY DASHBOARD
 // ------------------------------------------------

 private void facultyHome() {

  shell("Faculty Workspace");
  LinearLayout welcome = panel();
  welcome.addView(txt("Good day, " + current.name, 22, ink, true));
  TextView sub = txt("Your classes, attendance and student records — all in one place.", 13, muted, false);
  sub.setPadding(0, dp(5), 0, 0);
  welcome.addView(sub);

  head("Workspace overview");
  LinearLayout stats = new LinearLayout(this);
  stats.setOrientation(0);
  body.addView(stats);
  LinearLayout stats2 = new LinearLayout(this);
  stats2.setOrientation(0);
  body.addView(stats2);

  io.execute(() -> {
   List<Section> owned = dao.sectionsForFaculty(current.id);
   int studentCount = 0, subjectCount = 0, classCount = 0;
   for (Section item : owned) {
    studentCount += dao.students(item.id).size();
    subjectCount += dao.subjects(item.id).size();
    classCount += dao.sessions(item.id).size();
   }
   final int students = studentCount, subjects = subjectCount, classes = classCount;
   runOnUiThread(() -> {
    statTile(stats, String.valueOf(owned.size()), "Sections", accent);
    statTile(stats, String.valueOf(students), "Students", green);
    statTile(stats2, String.valueOf(subjects), "Subjects", accent);
    statTile(stats2, String.valueOf(classes), "Classes held", muted);
   });
  });

  head("Quick actions");
  btn("＋  Create / Manage Sections", this::sectionsScreen);
  btn("♙  Manage Students & Accounts", this::chooseSectionStudents);
  btn("▤  Manage Subjects", this::chooseSectionSubjects);
  btn("✓  Take / Edit Attendance", this::chooseSectionAttendance);
  btn("▤  Attendance Reports & Export", this::reportsScreen);
  btn("＋  Create Faculty Account", this::createFaculty);
  btn("⚙  Profile & Settings", this::profileScreen);
  btn("Sign Out", this::login);
 }

 // ------------------------------------------------
 // SECTION MANAGEMENT
 // ------------------------------------------------

 private void sectionsScreen() {

  shell("Manage Sections");

  head("Create Class Section");

  EditText name = field("Section name (e.g. CSE - Section C)");
  EditText dept = field("Department");
  EditText sem = field("Semester");
  EditText year = field("Academic year");

  btn("Create Section", () -> {

   String sectionName = name.getText().toString().trim();

   if (sectionName.isEmpty()) {
    toast("Section name required");
    return;
   }

   Section s = new Section(
           sectionName,
           dept.getText().toString().trim(),
           sem.getText().toString().trim(),
           year.getText().toString().trim(),
           current.id
   );

   io.execute(() -> {

    dao.addSection(s);

    runOnUiThread(() -> {

     toast("Section created successfully");

     sectionsScreen();
    });
   });
  });

  io.execute(() -> {

   List<Section> list =
           dao.sectionsForFaculty(current.id);

   runOnUiThread(() -> {

    head("My Sections (" + list.size() + ")");

    for (Section item : list) {
     LinearLayout itemCard = panel();
     itemCard.addView(txt(item.name, 16, ink, true));
     itemCard.addView(txt(item.department + " • " + item.semester + " • " + item.academicYear, 12, muted, false));
     btn("Edit section", () -> editSection(item));
     btn("Delete section", () -> confirmDelete("Delete section?", "This permanently deletes its students, subjects, classes and attendance records.", () -> io.execute(() -> { dao.deleteSection(item.id, current.id); runOnUiThread(this::sectionsScreen); })));
    }
   });
  });

  btn("Back", this::facultyHome);
 }


 // ------------------------------------------------
 // FACULTY REPORTS AND CSV EXPORT
 // ------------------------------------------------

 private void reportsScreen() {
  shell("Attendance Reports");
  head("Section reports");
  note("Review attendance activity for a section and export a spreadsheet-compatible CSV report.");
  io.execute(() -> {
   List<Section> owned = dao.sectionsForFaculty(current.id);
   runOnUiThread(() -> {
    if (owned.isEmpty()) {
     LinearLayout empty = panel();
     empty.addView(txt("No sections available", 17, ink, true));
     empty.addView(txt("Create a section and add students and subjects before generating reports.", 13, muted, false));
    } else {
     for (Section item : owned) {
      LinearLayout reportCard = panel();
      reportCard.addView(txt(item.name, 17, ink, true));
      reportCard.addView(txt(item.department + " • Semester " + item.semester + " • " + item.academicYear, 12, muted, false));
      btn("View report and export", () -> showSectionReport(item));
     }
    }
    btn("Back to Dashboard", this::facultyHome);
   });
  });
 }

 private void showSectionReport(Section selected) {
  io.execute(() -> {
   List<Student> students = dao.students(selected.id);
   List<Subject> subjects = dao.subjects(selected.id);
   List<ClassSession> sessions = dao.sessions(selected.id);
   int present = 0, absent = 0;
   for (ClassSession cs : sessions) {
    for (Attendance a : dao.attendanceForSession(cs.id)) {
     if ("PRESENT".equals(a.status)) present++;
     else if ("ABSENT".equals(a.status)) absent++;
    }
   }
   final int p = present, a = absent;
   runOnUiThread(() -> {
    shell("Section Report");
    head(selected.name);
    note(selected.department + " • Semester " + selected.semester + " • " + selected.academicYear);
    LinearLayout stats = new LinearLayout(this);
    stats.setOrientation(0);
    body.addView(stats);
    statTile(stats, String.valueOf(students.size()), "Students", accent);
    statTile(stats, String.valueOf(subjects.size()), "Subjects", muted);
    LinearLayout stats2 = new LinearLayout(this);
    stats2.setOrientation(0);
    body.addView(stats2);
    statTile(stats2, String.valueOf(sessions.size()), "Classes", accent);
    statTile(stats2, String.valueOf(p), "Present marks", green);
    LinearLayout stats3 = new LinearLayout(this);
    stats3.setOrientation(0);
    body.addView(stats3);
    statTile(stats3, String.valueOf(a), "Absent marks", red);
    int marked = p + a;
    double rate = marked == 0 ? 0 : p * 100.0 / marked;
    head("Attendance summary");
    LinearLayout summary = panel();
    summary.addView(txt(String.format(Locale.getDefault(), "%.1f%% present rate", rate), 25, rate >= 75 ? green : red, true));
    progressLine(summary, (int)Math.round(rate), rate >= 75 ? green : red);
    note("The report includes one row per recorded student attendance entry, with subject and class date.");
    btn("Filter report by date range", () -> chooseReportRange(selected));
    btn("Export CSV Report", () -> exportSectionReport(selected));
    btn("Back to Reports", this::reportsScreen);
   });
  });
 }


 private void chooseReportRange(Section selected) {
  Calendar now=Calendar.getInstance();
  Calendar start=Calendar.getInstance(); start.add(Calendar.DAY_OF_YEAR,-30);
  final long[] from={dateKey(start.get(Calendar.YEAR),start.get(Calendar.MONTH),start.get(Calendar.DAY_OF_MONTH))};
  final long[] to={dateKey(now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH))};
  new DatePickerDialog(this,(v,y,m,d)->{
   from[0]=dateKey(y,m,d);
   new DatePickerDialog(this,(v2,y2,m2,d2)->{
    to[0]=dateKey(y2,m2,d2)+86400000L-1;
    if(to[0]<from[0]){toast("End date must be after start date");return;}
    showFilteredReport(selected,from[0],to[0]);
   },now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH)).show();
  },start.get(Calendar.YEAR),start.get(Calendar.MONTH),start.get(Calendar.DAY_OF_MONTH)).show();
 }

 private void showFilteredReport(Section selected,long from,long to) {
  io.execute(()->{
   List<ClassSession> sessions=dao.sessionsBetween(selected.id,from,to);
   int p=0,a=0;
   for(ClassSession cs:sessions)for(Attendance mark:dao.attendanceForSession(cs.id)){
    if("PRESENT".equals(mark.status))p++;else if("ABSENT".equals(mark.status))a++;
   }
   int present=p,absent=a,marked=p+a;
   runOnUiThread(()->{
    shell("Filtered Report");head(selected.name);
    note(fmt.format(new Date(from))+" – "+fmt.format(new Date(to)));
    LinearLayout tiles=new LinearLayout(this);tiles.setOrientation(0);body.addView(tiles);
    statTile(tiles,String.valueOf(present),"Present",green);statTile(tiles,String.valueOf(absent),"Absent",red);
    double rate=marked==0?0:present*100.0/marked;
    LinearLayout cardView=panel();cardView.addView(txt(String.format(Locale.getDefault(),"%.1f%% present rate",rate),24,rate>=75?green:red,true));
    progressLine(cardView,(int)Math.round(rate),rate>=75?green:red);
    btn("Export selected date range",()->exportSectionReportRange(selected,from,to));
    btn("Back to section report",()->showSectionReport(selected));
   });
  });
 }

 private void exportSectionReportRange(Section selected,long from,long to) {
  io.execute(()->{
   try{
    List<Student> students=dao.students(selected.id);
    Map<Long,Student> studentMap=new HashMap<>();for(Student st:students)studentMap.put(st.id,st);
    List<ClassSession> sessions=dao.sessionsBetween(selected.id,from,to);
    StringBuilder out=new StringBuilder("Section,Subject,Subject Code,Date,Student Name,USN,Status\n");
    for(ClassSession cs:sessions){Subject sub=dao.subject(cs.subjectId);for(Attendance mark:dao.attendanceForSession(cs.id)){
     Student st=studentMap.get(mark.studentId);if(st==null)continue;
     out.append(csv(selected.name)).append(',').append(csv(sub==null?"":sub.name)).append(',').append(csv(sub==null?"":sub.code)).append(',')
      .append(csv(fmt.format(new Date(cs.dateKey)))).append(',').append(csv(st.name)).append(',').append(csv(st.usn)).append(',').append(csv(mark.status)).append('\n');
    }}
    File dir=new File(getCacheDir(),"reports");if(!dir.exists())dir.mkdirs();
    File file=new File(dir,"AttendEase_Filtered_"+System.currentTimeMillis()+".csv");
    try(FileOutputStream stream=new FileOutputStream(file)){stream.write(out.toString().getBytes(StandardCharsets.UTF_8));}
    runOnUiThread(()->{Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/csv");send.putExtra(Intent.EXTRA_STREAM,uri);send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(send,"Share filtered report"));});
   }catch(Exception e){runOnUiThread(()->fail(e));}
  });
 }

 private String csv(String value) {
  String safe = value == null ? "" : value;
  return "\"" + safe.replace("\"", "\"\"") + "\"";
 }

 private void exportSectionReport(Section selected) {
  io.execute(() -> {
   try {
    List<Student> students = dao.students(selected.id);
    List<ClassSession> sessions = dao.sessions(selected.id);
    Map<Long, Student> studentMap = new HashMap<>();
    for (Student st : students) studentMap.put(st.id, st);
    StringBuilder csv = new StringBuilder();
    csv.append("Section,Department,Semester,Academic Year,Subject,Subject Code,Class Date,Student Name,USN,Status\n");
    int rows = 0;
    for (ClassSession cs : sessions) {
     Subject sub = dao.subject(cs.subjectId);
     List<Attendance> marks = dao.attendanceForSession(cs.id);
     for (Attendance mark : marks) {
      Student st = studentMap.get(mark.studentId);
      if (st == null) continue;
      csv.append(csv(selected.name)).append(',')
       .append(csv(selected.department)).append(',')
       .append(csv(selected.semester)).append(',')
       .append(csv(selected.academicYear)).append(',')
       .append(csv(sub == null ? "" : sub.name)).append(',')
       .append(csv(sub == null ? "" : sub.code)).append(',')
       .append(csv(fmt.format(new Date(cs.dateKey)))).append(',')
       .append(csv(st.name)).append(',')
       .append(csv(st.usn)).append(',')
       .append(csv(mark.status)).append('\n');
      rows++;
     }
    }
    File folder = new File(getCacheDir(), "reports");
    if (!folder.exists() && !folder.mkdirs()) throw new IllegalStateException("Could not create report folder");
    String safeName = selected.name.replaceAll("[^A-Za-z0-9_-]", "_");
    File file = new File(folder, "AttendEase_" + safeName + "_" + System.currentTimeMillis() + ".csv");
    try (FileOutputStream out = new FileOutputStream(file)) {
     out.write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
    final int exportedRows = rows;
    runOnUiThread(() -> {
     try {
      Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
      Intent share = new Intent(Intent.ACTION_SEND);
      share.setType("text/csv");
      share.putExtra(Intent.EXTRA_SUBJECT, "AttendEase Attendance Report - " + selected.name);
      share.putExtra(Intent.EXTRA_STREAM, uri);
      share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      startActivity(Intent.createChooser(share, "Share attendance report"));
      toast(exportedRows + " attendance records exported");
     } catch (Exception e) {
      fail(e);
     }
    });
   } catch (Exception e) {
    runOnUiThread(() -> fail(e));
   }
  });
 }


 // ------------------------------------------------
 // SAFE EDIT / DELETE MANAGEMENT
 // ------------------------------------------------
 private void confirmDelete(String title, String message, Runnable action) {
  new AlertDialog.Builder(this).setTitle(title).setMessage(message)
   .setNegativeButton("Cancel", null)
   .setPositiveButton("Delete", (dialog, which) -> action.run()).show();
 }

 private void editSection(Section item) {
  LinearLayout form = new LinearLayout(this); form.setPadding(dp(20),dp(8),dp(20),0); form.setOrientation(1);
  EditText name = new EditText(this); name.setHint("Section name"); name.setText(item.name); form.addView(name);
  EditText dept = new EditText(this); dept.setHint("Department"); dept.setText(item.department); form.addView(dept);
  EditText sem = new EditText(this); sem.setHint("Semester"); sem.setText(item.semester); form.addView(sem);
  EditText year = new EditText(this); year.setHint("Academic year"); year.setText(item.academicYear); form.addView(year);
  new AlertDialog.Builder(this).setTitle("Edit section").setView(form)
   .setNegativeButton("Cancel",null).setPositiveButton("Save",(di,w)->{
    item.name=name.getText().toString().trim(); item.department=dept.getText().toString().trim();
    item.semester=sem.getText().toString().trim(); item.academicYear=year.getText().toString().trim();
    if(item.name.isEmpty()){toast("Section name is required");return;}
    io.execute(()->{dao.updateSection(item);runOnUiThread(this::sectionsScreen);});
   }).show();
 }

 private void editStudent(Student item, Section owner) {
  LinearLayout form = new LinearLayout(this); form.setPadding(dp(20),dp(8),dp(20),0); form.setOrientation(1);
  EditText name=new EditText(this); name.setHint("Name"); name.setText(item.name); form.addView(name);
  EditText usn=new EditText(this); usn.setHint("USN"); usn.setText(item.usn); form.addView(usn);
  EditText email=new EditText(this); email.setHint("Email"); email.setText(item.email); form.addView(email);
  new AlertDialog.Builder(this).setTitle("Edit student").setView(form).setNegativeButton("Cancel",null)
   .setPositiveButton("Save",(di,w)->{item.name=name.getText().toString().trim();item.usn=usn.getText().toString().trim();item.email=email.getText().toString().trim();
    if(item.name.isEmpty()||item.usn.isEmpty()){toast("Name and USN are required");return;}
    io.execute(()->{try{dao.updateStudent(item);runOnUiThread(()->studentsScreen(owner));}catch(Exception e){runOnUiThread(()->fail(e));}});
   }).show();
 }

 private void editSubject(Subject item, Section owner) {
  LinearLayout form=new LinearLayout(this);form.setPadding(dp(20),dp(8),dp(20),0);form.setOrientation(1);
  EditText name=new EditText(this);name.setHint("Subject name");name.setText(item.name);form.addView(name);
  EditText code=new EditText(this);code.setHint("Subject code");code.setText(item.code);form.addView(code);
  new AlertDialog.Builder(this).setTitle("Edit subject").setView(form).setNegativeButton("Cancel",null)
   .setPositiveButton("Save",(di,w)->{item.name=name.getText().toString().trim();item.code=code.getText().toString().trim();
    if(item.name.isEmpty()){toast("Subject name is required");return;}
    io.execute(()->{dao.updateSubject(item);runOnUiThread(()->subjectsScreen(owner));});
   }).show();
 }

 private void profileScreen() {
  shell("Profile & Settings"); head("Your profile");
  LinearLayout profile=panel();
  profile.addView(txt(current.name,21,ink,true));
  profile.addView(txt(("FACULTY".equals(current.role)?"Faculty":"Student")+" • "+current.username,13,muted,false));
  if("STUDENT".equals(current.role)) profile.addView(txt("USN: "+current.studentUsn,13,muted,false));
  head("Account settings");
  EditText displayName=field("Display name"); displayName.setText(current.name);
  EditText newPassword=field("New password (leave blank to keep current)");
  btn("Save profile",()->{
   String n=displayName.getText().toString().trim(), p=newPassword.getText().toString();
   if(n.isEmpty()||(!p.isEmpty()&&p.length()<6)){toast("Enter a name and a password of at least 6 characters");return;}
   io.execute(()->{
    int changed=dao.updateUserProfile(current.id,n,p.isEmpty()?current.passwordHash:hash(p));
    runOnUiThread(()->{current.name=n;if(!p.isEmpty())current.passwordHash=hash(p);toast(changed>0?"Profile updated":"No changes saved");profileScreen();});
   });
  });
  head("Appearance");
  btn("Choose appearance: Light / Dark / System", () -> {
   String[] modes={"System default","Light","Dark"};
   String saved=getSharedPreferences("attendease_settings",MODE_PRIVATE).getString("theme","system");
   int checked="light".equals(saved)?1:("dark".equals(saved)?2:0);
   new AlertDialog.Builder(this).setTitle("Choose appearance").setSingleChoiceItems(modes,checked,(dialog,which)->{
    String value=which==1?"light":which==2?"dark":"system";
    getSharedPreferences("attendease_settings",MODE_PRIVATE).edit().putString("theme",value).apply();
    AppCompatDelegate.setDefaultNightMode(which==1?AppCompatDelegate.MODE_NIGHT_NO:which==2?AppCompatDelegate.MODE_NIGHT_YES:AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    dialog.dismiss();
   }).setNegativeButton("Close",null).show();
  });
  btn("Sign Out",this::login);
 }

 // ------------------------------------------------
 // SECTION SELECTION
 // ------------------------------------------------

 private interface SectionAction {
  void run(Section s);
 }

 private void pickSection(SectionAction action) {

  io.execute(() -> {

   List<Section> sections =
           dao.sectionsForFaculty(current.id);

   runOnUiThread(() -> {

    if (sections.isEmpty()) {

     toast("You have not created any sections yet");
     return;
    }

    String[] names = new String[sections.size()];

    for (int i = 0; i < names.length; i++) {
     names[i] = sections.get(i).name;
    }

    new AlertDialog.Builder(this)
            .setTitle("Select Your Section")
            .setItems(names, (dialog, which) ->
                    action.run(sections.get(which))
            )
            .show();
   });
  });
 }

 private void chooseSectionStudents() {
  pickSection(this::studentsScreen);
 }

 private void chooseSectionSubjects() {
  pickSection(this::subjectsScreen);
 }

 private void chooseSectionAttendance() {
  pickSection(this::attendanceSubjectPicker);
 }

 // ------------------------------------------------
 // STUDENT MANAGEMENT + STUDENT LOGIN CREATION
 // ------------------------------------------------

 private void studentsScreen(Section s) {

  section = s;

  shell("Manage Students");

  head(s.name);

  note("Add a student and create their login account.");

  EditText n = field("Student full name");

  EditText usn = field("USN / Student ID");

  EditText email = field("Email (optional)");

  EditText username = field("Student login username");

  EditText password = field("Student login password (minimum 6 characters)");

  btn("Add Student and Create Login", () -> {

   String studentName = n.getText().toString().trim();
   String studentUsn = usn.getText().toString().trim();
   String studentEmail = email.getText().toString().trim();
   String studentUsername = username.getText().toString().trim();
   String studentPassword = password.getText().toString();

   if (studentName.isEmpty()
           || studentUsn.isEmpty()
           || studentUsername.isEmpty()
           || studentPassword.length() < 6) {

    toast("Enter name, USN, username and a password of at least 6 characters");
    return;
   }

   io.execute(() -> {

    if (dao.usernameExists(studentUsername) > 0) {

     runOnUiThread(() ->
             toast("Username already exists")
     );

     return;
    }

    try {

     final long[] newStudentId = {0};

     // Save student and login together
     db.runInTransaction(() -> {

      Student st = new Student(
              studentName,
              studentUsn,
              studentEmail,
              s.id
      );

      long id = dao.addStudent(st);

      newStudentId[0] = id;

      User studentUser = new User(
              studentUsername,
              hash(studentPassword),
              studentName,
              "STUDENT",
              studentUsn,
              id
      );

      dao.addUser(studentUser);
     });

     runOnUiThread(() -> {

      toast("Student and login account created successfully");

      studentsScreen(s);
     });

    } catch (Exception e) {

     runOnUiThread(() -> fail(e));
    }
   });
  });

  io.execute(() -> {

   List<Student> list = dao.students(s.id);

   runOnUiThread(() -> {

    head("Student Roster (" + list.size() + ")");

    for (Student x : list) {

     note(x.name + " • " + x.usn);
    }
   });
  });

  btn("Back", this::facultyHome);
 }

 // ------------------------------------------------
 // SUBJECT MANAGEMENT
 // ------------------------------------------------

 private void subjectsScreen(Section s) {

  section = s;

  shell("Manage Subjects");

  head(s.name);

  EditText n = field("Subject name");

  EditText code = field("Subject code");

  btn("Add Subject", () -> {

   String subjectName = n.getText().toString().trim();

   if (subjectName.isEmpty()) {

    toast("Subject name required");
    return;
   }

   Subject sub = new Subject(
           subjectName,
           code.getText().toString().trim(),
           s.id
   );

   io.execute(() -> {

    dao.addSubject(sub);

    runOnUiThread(() -> {

     toast("Subject added");

     subjectsScreen(s);
    });
   });
  });

  io.execute(() -> {

   List<Subject> list = dao.subjects(s.id);

   runOnUiThread(() -> {

    head("Subjects");

    for (Subject x : list) {

     note(x.name + " • " + x.code);
    }
   });
  });

  btn("Back", this::facultyHome);
 }

 // ------------------------------------------------
 // ATTENDANCE MANAGEMENT
 // ------------------------------------------------

 private void attendanceSubjectPicker(Section s) {

  io.execute(() -> {

   List<Subject> list = dao.subjects(s.id);

   runOnUiThread(() -> {

    if (list.isEmpty()) {

     toast("Add subjects first");
     return;
    }

    String[] names = new String[list.size()];

    for (int i = 0; i < names.length; i++) {
     names[i] = list.get(i).name;
    }

    new AlertDialog.Builder(this)
            .setTitle("Select Subject")
            .setItems(names, (dialog, which) ->
                    pickDateAndTakeAttendance(
                            s,
                            list.get(which)
                    )
            )
            .show();
   });
  });
 }

 private long dateKey(int y, int m, int d) {

  Calendar c = Calendar.getInstance();

  c.set(y, m, d, 0, 0, 0);
  c.set(Calendar.MILLISECOND, 0);

  return c.getTimeInMillis();
 }

 private void pickDateAndTakeAttendance(
         Section s,
         Subject sub
 ) {

  Calendar c = Calendar.getInstance();

  new DatePickerDialog(
          this,
          (v, y, m, d) -> {

           long key = dateKey(y, m, d);

           io.execute(() -> {

            ClassSession session = dao.session(sub.id, key);

            if (session == null) {

             session = new ClassSession(
                     s.id,
                     sub.id,
                     key,
                     System.currentTimeMillis()
             );

             session.id = dao.addSession(session);
            }

            ClassSession finalSession = session;

            List<Student> roster = dao.students(s.id);

            runOnUiThread(() ->
                    attendanceRegister(
                            s,
                            sub,
                            finalSession,
                            roster
                    )
            );
           });

          },
          c.get(Calendar.YEAR),
          c.get(Calendar.MONTH),
          c.get(Calendar.DAY_OF_MONTH)
  ).show();
 }

 private void attendanceRegister(
         Section s,
         Subject sub,
         ClassSession session,
         List<Student> roster
 ) {

  section = s;
  subject = sub;

  shell("Attendance Register");

  head(sub.name);

  note(
          s.name + " • " +
                  fmt.format(new Date(session.dateKey)) +
                  " • " + roster.size() + " students"
  );

  if (roster.isEmpty()) {
   LinearLayout empty = panel();
   empty.addView(txt("No students in this section yet", 17, ink, true));
   empty.addView(txt("Add students before taking attendance for this class.", 13, muted, false));
   btn("Back", () -> attendanceSubjectPicker(s));
   return;
  }

  Map<Long, RadioGroup> groups = new LinkedHashMap<>();
  TextView attendanceSummary = txt("Present: 0   •   Absent: 0   •   Unmarked: " + roster.size(), 14, muted, true);
  attendanceSummary.setPadding(dp(2), dp(4), dp(2), dp(10));
  body.addView(attendanceSummary);
  Runnable refreshCounts = () -> {
   int pCount = 0, aCount = 0, uCount = 0;
   for (RadioGroup group : groups.values()) {
    if (group.getCheckedRadioButtonId() == -1) uCount++;
    else {
     RadioButton selected = group.findViewById(group.getCheckedRadioButtonId());
     if (selected != null && "PRESENT".equals(selected.getTag())) pCount++; else aCount++;
    }
   }
   attendanceSummary.setText("Present: " + pCount + "   •   Absent: " + aCount + "   •   Unmarked: " + uCount);
  };

  LinearLayout quick = new LinearLayout(this);
  quick.setOrientation(0);
  body.addView(quick);
  MaterialButton allPresent = new MaterialButton(this);
  allPresent.setText("All Present"); allPresent.setAllCaps(false);
  allPresent.setTextColor(Color.WHITE); allPresent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));
  MaterialButton allAbsent = new MaterialButton(this);
  allAbsent.setText("All Absent"); allAbsent.setAllCaps(false);
  allAbsent.setTextColor(Color.WHITE); allAbsent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(red));
  quick.addView(allPresent, new LinearLayout.LayoutParams(0, dp(46), 1));
  LinearLayout.LayoutParams absentLp = new LinearLayout.LayoutParams(0, dp(46), 1); absentLp.leftMargin = dp(8);
  quick.addView(allAbsent, absentLp);

  EditText searchStudent = field("Search student by name or USN");
  List<View> attendanceRows = new ArrayList<>();

  for (Student st : roster) {

   LinearLayout row = new LinearLayout(this);

   row.setOrientation(1);
   row.setPadding(dp(14), dp(12), dp(14), dp(12));
   row.setBackground(rounded(card, border, 16));

   LinearLayout.LayoutParams rp =
           new LinearLayout.LayoutParams(-1, -2);

   rp.setMargins(0, 4, 0, 7);

   body.addView(row, rp);
   attendanceRows.add(row);

   row.addView(
           txt(st.name + " • " + st.usn, 15, ink, true)
   );

   RadioGroup rg = new RadioGroup(this);
   rg.setOrientation(0);
   rg.setPadding(0, dp(5), 0, 0);

   RadioButton present = new RadioButton(this);

   present.setId(View.generateViewId());
   present.setText("Present");
   present.setTextColor(green);
   present.setButtonTintList(android.content.res.ColorStateList.valueOf(green));
   present.setTag("PRESENT");

   rg.addView(present);

   RadioButton absent = new RadioButton(this);

   absent.setId(View.generateViewId());
   absent.setText("Absent");
   absent.setTextColor(red);
   absent.setButtonTintList(android.content.res.ColorStateList.valueOf(red));
   absent.setTag("ABSENT");

   rg.addView(absent);
   rg.setOnCheckedChangeListener((g, checkedId) -> refreshCounts.run());

   // Leave new records unmarked so attendance is never saved by assumption.
   row.addView(rg);

   groups.put(st.id, rg);
  }

  searchStudent.addTextChangedListener(new android.text.TextWatcher() {
   @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
   @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
    String query = value.toString().trim().toLowerCase(Locale.ROOT);
    for (int i = 0; i < roster.size(); i++) {
     Student student = roster.get(i);
     boolean visible = student.name.toLowerCase(Locale.ROOT).contains(query) || student.usn.toLowerCase(Locale.ROOT).contains(query);
     attendanceRows.get(i).setVisibility(visible ? View.VISIBLE : View.GONE);
    }
   }
   @Override public void afterTextChanged(android.text.Editable value) { }
  });

  allPresent.setOnClickListener(v -> { for (RadioGroup g : groups.values()) g.check(((RadioButton) g.getChildAt(0)).getId()); refreshCounts.run(); });
  allAbsent.setOnClickListener(v -> { for (RadioGroup g : groups.values()) g.check(((RadioButton) g.getChildAt(1)).getId()); refreshCounts.run(); });
  refreshCounts.run();

  // Load previously saved attendance
  io.execute(() -> {

   for (Student st : roster) {

    Attendance a = dao.attendance(st.id, session.id);

    if (a != null) {

     RadioGroup rg = groups.get(st.id);

     runOnUiThread(() -> {

      for (int i = 0; i < rg.getChildCount(); i++) {

       RadioButton rb =
               (RadioButton) rg.getChildAt(i);

       if (a.status.equals(rb.getTag())) {
        rg.check(rb.getId());
       }
      }
     });
    }
   }
  });

  btn("Save Attendance", () -> {
   Map<Long, String> captured = new LinkedHashMap<>();
   for (Student st : roster) {
    RadioGroup rg = groups.get(st.id);
    int checked = rg.getCheckedRadioButtonId();
    if (checked == -1) { toast("Mark attendance for every student before saving"); return; }
    RadioButton selected = rg.findViewById(checked);
    captured.put(st.id, String.valueOf(selected.getTag()));
   }
   io.execute(() -> {
    db.runInTransaction(() -> {
     for (Student st : roster) {
      dao.saveAttendance(new Attendance(st.id, session.id, captured.get(st.id), System.currentTimeMillis()));
     }
    });
    runOnUiThread(() -> { toast("Attendance saved successfully"); facultyHome(); });
   });
  });

  btn("Back", () -> attendanceSubjectPicker(s));
 }

 // ------------------------------------------------
 // STUDENT DASHBOARD
 // ------------------------------------------------

 private void studentHome() {

  shell("AttendEase • Student");

  head("Welcome, " + current.name);

  note("USN: " + current.studentUsn);

  io.execute(() -> {

   Student st = dao.student(current.studentId);

   List<Subject> subs = st == null
           ? new ArrayList<>()
           : dao.subjects(st.sectionId);

   int total = dao.totalForStudent(current.studentId);

   int present = dao.presentForStudent(current.studentId);

   Map<Long, int[]> perSubject = new LinkedHashMap<>();

   for (Subject sub : subs) {

    perSubject.put(
            sub.id,
            new int[]{
                    dao.totalForSubject(current.studentId, sub.id),
                    dao.presentForSubject(current.studentId, sub.id)
            }
    );
   }

   runOnUiThread(() -> {

    if (st == null) {

     note("Student record not found.");

     btn("Sign Out", this::login);

     return;
    }

    double pct = total == 0
            ? 0
            : present * 100.0 / total;

    LinearLayout overall = panel();
    overall.addView(txt("Overall attendance", 14, muted, true));
    overall.addView(txt(String.format(Locale.getDefault(), "%.1f%%", pct), 34, pct >= 75 ? green : red, true));
    overall.addView(txt(present + " present  •  " + (total - present) + " absent  •  " + total + " classes", 13, muted, false));
    progressLine(overall, (int) Math.round(pct), pct >= 75 ? green : red);
    if (total > 0 && pct < 75) {
     TextView warning = txt("Attendance is below the 75% requirement. Review subject-wise attendance and plan your classes.", 13, amber, true);
     warning.setPadding(0, dp(10), 0, 0); overall.addView(warning);
    }

    head("Subject-wise Attendance");

    for (Subject sub : subs) {

     int[] v = perSubject.get(sub.id);

     int t = v[0];
     int p = v[1];

     double pc = t == 0
             ? 0
             : p * 100.0 / t;

     LinearLayout subjectCard = panel();
     subjectCard.addView(txt(sub.name, 16, ink, true));
     subjectCard.addView(txt(String.format(Locale.getDefault(), "%.1f%%  •  %d of %d classes", pc, p, t), 13, pc >= 75 ? green : red, true));
     progressLine(subjectCard, (int) Math.round(pc), pc >= 75 ? green : red);

     if (t == 0) {

      note("Remark: No attendance records available.");

     } else if (pc < 75) {

      int required = (int) Math.ceil(
              (0.75 * t - p) / 0.25
      );

      note(
              "Remark: Attendance shortage! " +
                      "Attend the next " + required +
                      " consecutive classes to reach 75%."
      );

     } else {

      note(
              "Remark: Good attendance. " +
                      "You meet the 75% requirement."
      );
     }
    }

    btn("My Attendance History", this::studentHistory);

    btn("Sign Out", this::login);
   });
  });
 }

 // ------------------------------------------------
 // STUDENT ATTENDANCE HISTORY
 // ------------------------------------------------

 private void studentHistory() {

  shell("My Attendance History");

  head("Attendance Records");

  io.execute(() -> {

   List<Attendance> list =
           dao.attendanceForStudent(current.studentId);

   list.sort((a, b) -> {

    ClassSession sa = dao.sessionById(a.sessionId);
    ClassSession sb = dao.sessionById(b.sessionId);

    long da = sa == null ? 0 : sa.dateKey;
    long db = sb == null ? 0 : sb.dateKey;

    return Long.compare(db, da);
   });

   List<String> rows = new ArrayList<>();

   for (Attendance a : list) {

    ClassSession cs = dao.sessionById(a.sessionId);

    Subject sub = cs == null
            ? null
            : dao.subject(cs.subjectId);

    String status = a.status.equals("PRESENT")
            ? "Present"
            : "Absent";

    rows.add(
            (sub == null ? "Subject" : sub.name) +
                    " • " +
                    (cs == null
                            ? "Unknown date"
                            : fmt.format(new Date(cs.dateKey))) +
                    " • " + status
    );
   }

   runOnUiThread(() -> {

    if (rows.isEmpty()) {
     note("No attendance records yet.");
    }

    for (String row : rows) {
     note(row);
    }

    btn("Back", this::studentHome);
   });
  });
 }

 // ------------------------------------------------
 // CLEANUP
 // ------------------------------------------------

 @Override
 protected void onDestroy() {

  super.onDestroy();

  io.shutdown();
 }
}
