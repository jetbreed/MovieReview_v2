package com.example.moviereview_v2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccessModifier extends SQLiteOpenHelper {

    public static final String JAVAMOVIE = "JAVAMOVIE";
    public static final String DURATION = "DURATION";
    public static final String MOVIENAME = "MOVIENAME";
    public static final String RATINGBAR = "RATINGBAR";
    public static final String YEAR = "YEAR";
    public static final String PYTHONMOVIE = "PYTHONMOVIE";
    public static final String REACTMOVIE = "REACTMOVIE";
    public static final String GENDER = "GENDER";
    public static final String MSSQLMOVIE = "MSSQLMOVIE";
    public static final String MOVIEWREVIEW = "MOVIEWREVIEW";
    public static final String ID = "ID";

    public DatabaseAccessModifier(@Nullable Context context) {
        super(context, "MoviewReview.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + MOVIEWREVIEW + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MOVIENAME + " TEXT, " +
                YEAR + " INTEGER, " +
                DURATION + " INTEGER, " +
                RATINGBAR + " FLOAT, " +
                PYTHONMOVIE + " TEXT, " +
                REACTMOVIE + " TEXT, " +
                GENDER + " TEXT, " +
                MSSQLMOVIE + " TEXT, " +
                JAVAMOVIE + " TEXT" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MOVIEWREVIEW);
        onCreate(db);
    }

    public boolean addOne(MoviewReview moviewReview) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = toContentValues(moviewReview);
        long insert = db.insert(MOVIEWREVIEW, null, cv);
        db.close();
        return insert != -1;
    }

    public boolean updateReview(MoviewReview moviewReview) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = toContentValues(moviewReview);
        int result = db.update(MOVIEWREVIEW, cv, ID + " = ?", new String[]{String.valueOf(moviewReview.getId())});
        db.close();
        return result > 0;
    }

    private ContentValues toContentValues(MoviewReview moviewReview) {
        ContentValues cv = new ContentValues();
        cv.put(JAVAMOVIE, moviewReview.getJavaMovie());
        cv.put(DURATION, moviewReview.getDuration());
        cv.put(MSSQLMOVIE, moviewReview.getMssqlMovie());
        cv.put(REACTMOVIE, moviewReview.getReactMovie());
        cv.put(PYTHONMOVIE, moviewReview.getPythonMovie());
        cv.put(RATINGBAR, moviewReview.getRatingBar());
        cv.put(YEAR, moviewReview.getYear());
        cv.put(MOVIENAME, moviewReview.getmovie_name());
        cv.put(GENDER, moviewReview.isFemale().equals("Yes") ? "Female" : "Male");
        return cv;
    }

    public boolean deleteOne(MoviewReview moviewReview) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(MOVIEWREVIEW, ID + " = ?", new String[]{String.valueOf(moviewReview.getId())});
        db.close();
        return result > 0;
    }

    public List<MoviewReview> getResultList() {
        List<MoviewReview> resultSet = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            String query = "SELECT * FROM " + MOVIEWREVIEW + " ORDER BY id DESC";
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
                    String movieName = cursor.getString(cursor.getColumnIndexOrThrow(MOVIENAME));
                    int year = cursor.getInt(cursor.getColumnIndexOrThrow(YEAR));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(DURATION));
                    float ratingBar = cursor.getFloat(cursor.getColumnIndexOrThrow(RATINGBAR));
                    String pythonMovie = cursor.getString(cursor.getColumnIndexOrThrow(PYTHONMOVIE));
                    String reactMovie = cursor.getString(cursor.getColumnIndexOrThrow(REACTMOVIE));
                    String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));
                    String mssqlMovie = cursor.getString(cursor.getColumnIndexOrThrow(MSSQLMOVIE));
                    String javaMovie = cursor.getString(cursor.getColumnIndexOrThrow(JAVAMOVIE));

                    MoviewReview moviewReview = new MoviewReview(
                            id, movieName, pythonMovie, reactMovie,
                            mssqlMovie, javaMovie, year, duration,
                            ratingBar,
                            gender.equals("Female") ? "Yes" : "No",
                            gender.equals("Male") ? "Yes" : "No"
                    );
                    resultSet.add(moviewReview);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return resultSet;
    }

    public boolean exportToCSV(String fileName) {
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);
            writer.append("ID,MOVIENAME,YEAR,DURATION,RATINGBAR,PYTHONMOVIE,REACTMOVIE,GENDER,MSSQLMOVIE,JAVAMOVIE\n");

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + MOVIEWREVIEW + " ORDER BY id DESC", null);

            while (cursor.moveToNext()) {
                writer.append(cursor.getInt(cursor.getColumnIndexOrThrow(ID)) + ",");
                writer.append(escape(cursor.getString(cursor.getColumnIndexOrThrow(MOVIENAME))) + ",");
                writer.append(cursor.getInt(cursor.getColumnIndexOrThrow(YEAR)) + ",");
                writer.append(cursor.getInt(cursor.getColumnIndexOrThrow(DURATION)) + ",");
                writer.append(String.valueOf(cursor.getFloat(cursor.getColumnIndexOrThrow(RATINGBAR)))).append(",");
                writer.append(cursor.getString(cursor.getColumnIndexOrThrow(PYTHONMOVIE)) + ",");
                writer.append(cursor.getString(cursor.getColumnIndexOrThrow(REACTMOVIE)) + ",");
                writer.append(cursor.getString(cursor.getColumnIndexOrThrow(GENDER)) + ",");
                writer.append(cursor.getString(cursor.getColumnIndexOrThrow(MSSQLMOVIE)) + ",");
                writer.append(cursor.getString(cursor.getColumnIndexOrThrow(JAVAMOVIE)) + "\n");
            }

            writer.flush();
            writer.close();
            cursor.close();
            db.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace(",", " ");
    }
}
