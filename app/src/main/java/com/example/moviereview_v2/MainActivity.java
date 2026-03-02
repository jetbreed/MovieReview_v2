package com.example.moviereview_v2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText movieNameInput, yearInput, durationInput;
    RatingBar ratingBar;
    RadioButton pythonRadio, reactRadio;
    CheckBox mssqlCheck, javaCheck;
    RadioGroup genderGroup;
    Button btnSave, btnAdd, btnExport;
    RecyclerView recyclerView;

    MovieReviewAdapter adapter;
    List<MoviewReview> list;
    DatabaseAccessModifier db;
    MoviewReview currentEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Dark Mode (optional toggle)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views
        movieNameInput = findViewById(R.id.movie_name);
        yearInput = findViewById(R.id.year);
        durationInput = findViewById(R.id.duration);
        ratingBar = findViewById(R.id.ratingBar);
        pythonRadio = findViewById(R.id.pythonMovie);
        reactRadio = findViewById(R.id.reactMovie);
        mssqlCheck = findViewById(R.id.mssqlMovie);
        javaCheck = findViewById(R.id.javaMovie);
        genderGroup = findViewById(R.id.gender);
        btnSave = findViewById(R.id.btnSave);
        btnAdd = findViewById(R.id.btnAdd);
        btnExport = findViewById(R.id.btnExport); // Add in layout
        recyclerView = findViewById(R.id.recyclerView); // Change ListView to RecyclerView

        db = new DatabaseAccessModifier(this);
        list = db.getResultList();

        // Recycler setup
        adapter = new MovieReviewAdapter(this, list, new MovieReviewAdapter.OnReviewActionListener() {
            @Override
            public void onEdit(MoviewReview r) {
                currentEdit = r;
                movieNameInput.setText(r.getmovie_name());
                yearInput.setText(String.valueOf(r.getYear()));
                durationInput.setText((r.getDuration() / 60) + " hr " + (r.getDuration() % 60) + " min");
                ratingBar.setRating(r.getRatingBar());
                pythonRadio.setChecked("Yes".equals(r.getPythonMovie()));
                reactRadio.setChecked("Yes".equals(r.getReactMovie()));
                mssqlCheck.setChecked("Yes".equals(r.getMssqlMovie()));
                javaCheck.setChecked("Yes".equals(r.getJavaMovie()));
                genderGroup.check("Yes".equals(r.isFemale()) ? R.id.female : R.id.male);
            }

            @Override
            public void onDelete(MoviewReview r) {
                db.deleteOne(r);
                list.remove(r);
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Year Picker
        yearInput.setFocusable(false);
        yearInput.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR);
            new DatePickerDialog(this, (view, year, m, d) -> yearInput.setText(String.valueOf(year)),
                    y, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Duration Picker
        durationInput.setFocusable(false);
        durationInput.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hour, minute) -> {
                durationInput.setText(hour + " hr " + minute + " min");
            }, 1, 0, true).show();
        });

        // Save
        btnSave.setOnClickListener(v -> {
            String name = movieNameInput.getText().toString().trim();
            String yearStr = yearInput.getText().toString().trim();
            String durationStr = durationInput.getText().toString().trim();

            if (name.isEmpty()) {
                movieNameInput.setError("Required"); return;
            }
            if (yearStr.isEmpty()) {
                yearInput.setError("Required"); return;
            }
            if (durationStr.isEmpty()) {
                durationInput.setError("Required"); return;
            }

            int year = Integer.parseInt(yearStr);
            int duration = extractMinutes(durationStr);
            float rating = ratingBar.getRating();

            String python = pythonRadio.isChecked() ? "Yes" : "No";
            String react = reactRadio.isChecked() ? "Yes" : "No";
            String mssql = mssqlCheck.isChecked() ? "Yes" : "No";
            String java = javaCheck.isChecked() ? "Yes" : "No";

            int genderId = genderGroup.getCheckedRadioButtonId();
            String gender = (genderId == R.id.female) ? "Female" : "Male";

            MoviewReview r = new MoviewReview(
                    currentEdit != null ? currentEdit.getId() : 0,
                    name, python, react, mssql, java, year, duration, rating,
                    gender.equals("Female") ? "Yes" : "No",
                    gender.equals("Male") ? "Yes" : "No"
            );

            boolean ok;
            if (currentEdit != null) {
                ok = db.updateReview(r);
                int index = list.indexOf(currentEdit);
                list.set(index, r);
                adapter.notifyItemChanged(index);
                currentEdit = null;
            } else {
                ok = db.addOne(r);
                if (ok) {
                    list.add(r);
                    adapter.notifyItemInserted(list.size() - 1);
                }
            }

            if (ok) Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            clearFields();
        });

        // Add/Reset
        btnAdd.setOnClickListener(v -> clearFields());

        // Export CSV
        btnExport.setOnClickListener(v -> {
            String path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/MovieReviews.csv";
            boolean ok = db.exportToCSV(path);
            String msg = ok ? "Exported to:\n" + path : "Export failed!";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }

    private void clearFields() {
        movieNameInput.setText("");
        yearInput.setText("");
        durationInput.setText("");
        ratingBar.setRating(0);
        pythonRadio.setChecked(false);
        reactRadio.setChecked(false);
        mssqlCheck.setChecked(false);
        javaCheck.setChecked(false);
        genderGroup.clearCheck();
        currentEdit = null;
    }

    private int extractMinutes(String s) {
        try {
            String[] parts = s.split(" ");
            int hrs = Integer.parseInt(parts[0]);
            int mins = Integer.parseInt(parts[2]);
            return hrs * 60 + mins;
        } catch (Exception e) {
            return 0;
        }
    }
}
