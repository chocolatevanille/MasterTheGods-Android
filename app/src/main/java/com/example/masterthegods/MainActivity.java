package com.example.masterthegods;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private String active_pantheon = "Pantheon of the Master";
    private ImageView slayerImageView;
    private ArrayAdapter<CharSequence> pantheon_adapter;
    private ArrayAdapter<CharSequence> slayer_adapter;
    private List<GodStats> potm_gods = new ArrayList();
    private List<GodStats> pota_gods = new ArrayList();
    private List<GodStats> pots_gods = new ArrayList();
    private List<GodStats> potk_gods = new ArrayList();
    private List<GodStats> poh_gods = new ArrayList();
    private Map<String, List<GodStats>> pantheonToGodsMap;
    private Boolean isPantheonUpdate = false;
    private List<String> potm_history = new ArrayList();
    private List<String> pota_history = new ArrayList();
    private List<String> pots_history = new ArrayList();
    private List<String> potk_history = new ArrayList();
    private List<String> poh_history = new ArrayList();
    private Map<String, List<String>> pantheonToHistoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Refactor repetitive code later...
        String[] potm_gods_str = getResources().getStringArray(R.array.potm_gods);
        for (String god : potm_gods_str) {
            GodStats newGod = new GodStats(god);
            potm_gods.add(newGod);
        }

        String[] pota_gods_str = getResources().getStringArray(R.array.pota_gods);
        for (String god : pota_gods_str) {
            GodStats newGod = new GodStats(god);
            pota_gods.add(newGod);
        }

        String[] pots_gods_str = getResources().getStringArray(R.array.pots_gods);
        for (String god : pots_gods_str) {
            GodStats newGod = new GodStats(god);
            pots_gods.add(newGod);
        }

        String[] potk_gods_str = getResources().getStringArray(R.array.potk_gods);
        for (String god : potk_gods_str) {
            GodStats newGod = new GodStats(god);
            potk_gods.add(newGod);
        }

        String[] poh_gods_str = getResources().getStringArray(R.array.poh_gods);
        for (String god : poh_gods_str) {
            GodStats newGod = new GodStats(god);
            poh_gods.add(newGod);
        }

        pantheonToGodsMap = new HashMap<>();
        pantheonToGodsMap.put("Pantheon of the Master", potm_gods);
        pantheonToGodsMap.put("Pantheon of the Artist", pota_gods);
        pantheonToGodsMap.put("Pantheon of the Sage", pots_gods);
        pantheonToGodsMap.put("Pantheon of the Knight", potk_gods);
        pantheonToGodsMap.put("Pantheon of Hallownest", poh_gods);

        pantheonToHistoryMap = new HashMap<>();
        pantheonToHistoryMap.put("Pantheon of the Master", potm_history);
        pantheonToHistoryMap.put("Pantheon of the Artist", pota_history);
        pantheonToHistoryMap.put("Pantheon of the Sage", pots_history);
        pantheonToHistoryMap.put("Pantheon of the Knight", potk_history);
        pantheonToHistoryMap.put("Pantheon of Hallownest", poh_history);

        Spinner pantheon_spinner = findViewById(R.id.pantheons);
        Spinner slayer_spinner = findViewById(R.id.slayers);
        slayerImageView = findViewById(R.id.slayer);

        pantheon_adapter = ArrayAdapter.createFromResource(this,
                R.array.pantheons_items, android.R.layout.simple_spinner_item);

        slayer_adapter = ArrayAdapter.createFromResource(this,
                R.array.potm_gods, android.R.layout.simple_spinner_item);

        pantheon_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        slayer_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        pantheon_spinner.setAdapter(pantheon_adapter);
        slayer_spinner.setAdapter(slayer_adapter);

        pantheon_spinner.post(new Runnable() {
            public void run() {
                pantheon_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        isPantheonUpdate = true;
                        String new_pantheon = parent.getItemAtPosition(position).toString();
                        Toast.makeText(MainActivity.this, "Selected: " + new_pantheon, Toast.LENGTH_SHORT).show();
                        // Switch active pantheons
                        if (!new_pantheon.equals(active_pantheon)) {
                            active_pantheon = new_pantheon;
                            updateSlayersSpinner(new_pantheon);
                            slayerImageView.setImageResource(R.drawable.the_knight);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

        slayer_spinner.post(new Runnable() {
            public void run() {
                slayer_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (isPantheonUpdate){
                            isPantheonUpdate = false;
                        }else {
                            String slayer = parent.getItemAtPosition(position).toString();
                            // Add another death
                            updateSlayCount(slayer);
                            List<String> history = pantheonToHistoryMap.get(active_pantheon);
                            if (history != null) {
                                history.add(slayer);
                            }
                            Toast.makeText(MainActivity.this, "Slayed by: " + slayer, Toast.LENGTH_SHORT).show();
                            updateSlayerImage(slayer);
                        }
                    }


                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

        Button statsButton = findViewById(R.id.statsButton);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStatsDialog();
            }
        });

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> history = pantheonToHistoryMap.get(active_pantheon);
                if ((history != null) && (history.isEmpty())) {
                    List<GodStats> godsList = pantheonToGodsMap.get(active_pantheon);
                    if (godsList != null) {
                        String first_slayer = godsList.get(0).name;
                        updateSlayCount(first_slayer);
                        updateSlayerImage(first_slayer);
                        Toast.makeText(MainActivity.this, "Slayed by: " + first_slayer, Toast.LENGTH_SHORT).show();
                        history.add(first_slayer);
                    }
                } else if (history != null) {
                    String most_recent_slayer = history.get(history.size()-1);
                    updateSlayCount(most_recent_slayer);
                    Toast.makeText(MainActivity.this, "Slayed by: " + most_recent_slayer, Toast.LENGTH_SHORT).show();
                    updateSlayerImage(most_recent_slayer);
                    history.add(most_recent_slayer);
                }
            }
        });

        Button undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> history = pantheonToHistoryMap.get(active_pantheon);
                if ((history != null) && (history.isEmpty())) {
                    Toast.makeText(MainActivity.this, "No death history", Toast.LENGTH_SHORT).show();
                } else if (history != null) {
                    String most_recent_slayer = history.remove(history.size()-1);
                    List<GodStats> godsList = pantheonToGodsMap.get(active_pantheon);
                    if (godsList != null) {
                        for (GodStats god : godsList) {
                            god.attempt_count -= 1;
                            if (god.name.equals(most_recent_slayer)) {
                                god.updateSuccessRate();
                                if (!history.isEmpty()) {
                                    updateSlayerImage(history.get(history.size()-1));
                                } else {
                                    updateSlayerImage("The Knight");
                                }
                                Toast.makeText(MainActivity.this, "Undid death to: " + god.name, Toast.LENGTH_SHORT).show();
                                break;
                            }
                            god.success_count -= 1;
                            god.updateSuccessRate();
                        }
                    }
                }
            }
        });

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetConfirmationDialog();
            }
        });
    }

    private void showStatsDialog() {
        View statsView = getLayoutInflater().inflate(R.layout.stats_table, null);

        TableLayout statsTable = statsView.findViewById(R.id.statsTable);
        Button closeButton = statsView.findViewById(R.id.closeButton);

        // Populate the table
        populateStatsTable(statsTable);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(statsView);
        AlertDialog dialog = builder.create();

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void populateStatsTable(TableLayout statsTable) {
        // Clear existing rows
        statsTable.removeAllViews();

        // Create header row
        TableRow headerRow = new TableRow(this);
        addTextViewToRow(headerRow, "God", true);
        addTextViewToRow(headerRow, "Tries", true);
        addTextViewToRow(headerRow, "Wins", true);
        addTextViewToRow(headerRow, "Success Rate", true);
        statsTable.addView(headerRow);

        // Add data rows
        List<GodStats> godsList = pantheonToGodsMap.get(active_pantheon);
        if (godsList != null) {
            for (GodStats god : godsList) {
                TableRow row = new TableRow(this);
                addTextViewToRow(row, god.name, false);
                addTextViewToRow(row, String.valueOf((int) god.attempt_count), false);
                addTextViewToRow(row, String.valueOf((int) god.success_count), false);
                addTextViewToRow(row, String.format("%.2f", god.success_rate), false);
                statsTable.addView(row);
            }
        }
    }

    private void addTextViewToRow(TableRow row, String text, boolean isHeader) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        if (isHeader) {
            textView.setBackgroundColor(0xFF6200EE); // Header color
            textView.setTextColor(0xFFFFFFFF); // Header text color
            textView.setTextSize(16);
        } else {
            textView.setBackgroundColor(0xFFFFFFFF); // Row color
            textView.setTextColor(0xFF000000); // Row text color
            textView.setTextSize(14);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1);
        textView.setLayoutParams(params);
        row.addView(textView);
    }

    private void showResetConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirmation, null);

        // Create and customize the dialog
        builder.setView(dialogView);
        builder.setTitle("Confirm Reset");
        builder.setMessage("Are you sure you want to clear all data for the current pantheon?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Handle the reset action here
            clearPantheonData();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Customize dialog buttons
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(0xFF6200EE); // Use your header color

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(0xFF6200EE); // Use your header color
    }

    private void clearPantheonData() {
        List<GodStats> godsList = pantheonToGodsMap.get(active_pantheon);
        if (godsList != null) {
            for (GodStats god : godsList) {
                god.reset();
            }
        }
        List<String> history = pantheonToHistoryMap.get(active_pantheon);
        if (history != null) {
            history.clear();
        }

        updateSlayerImage("The Knight");
        Toast.makeText(MainActivity.this, "Data cleared for " + active_pantheon, Toast.LENGTH_SHORT).show();
    }

    private void updateSlayersSpinner(String new_pantheon) {

        Map<String, Integer> pantheonToArrayMap = new HashMap<>();
        pantheonToArrayMap.put("Pantheon of the Artist", R.array.pota_gods);
        pantheonToArrayMap.put("Pantheon of the Master", R.array.potm_gods);
        pantheonToArrayMap.put("Pantheon of the Sage", R.array.pots_gods);
        pantheonToArrayMap.put("Pantheon of the Knight", R.array.potk_gods);
        pantheonToArrayMap.put("Pantheon of Hallownest", R.array.poh_gods);

        if (pantheonToArrayMap.containsKey(new_pantheon)) {
            int arrayResourceId = pantheonToArrayMap.get(new_pantheon);

            ArrayAdapter<CharSequence> new_adapter = ArrayAdapter.createFromResource(this,
                    arrayResourceId, android.R.layout.simple_spinner_item);
            new_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            Spinner slayer_spinner = findViewById(R.id.slayers);
            slayer_spinner.setAdapter(new_adapter);
        }
    }

    private void updateSlayCount(String slayer) {
        List<GodStats> godsList = pantheonToGodsMap.get(active_pantheon);

        if (godsList != null) {
            for (GodStats god : godsList) {
                god.attempt_count += 1;
                if (god.name.equals(slayer)) {
                    break;
                }
                god.success_count += 1;
                god.updateSuccessRate();
            }
        }
    }
    private void updateSlayerImage(String slayer) {
        String slayer_image_name = slayer.replace(" ", "_").toLowerCase();
        int imageResourceId = getResources().getIdentifier(slayer_image_name, "drawable", getPackageName());
        if (imageResourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(this, imageResourceId);
            slayerImageView.setImageDrawable(drawable);
        } else {
            Log.e("MainActivity", "Drawable resource not found: " + slayer_image_name);
        }
    }
    private Map<String, List<GodStats>> initializePantheonToGodsMap() {
        Map<String, List<GodStats>> map = new HashMap<>();

        String[] pantheonNames = getResources().getStringArray(R.array.pantheons_items);
        int[] pantheonArrays = {R.array.potm_gods, R.array.pota_gods, R.array.pots_gods, R.array.potk_gods, R.array.poh_gods};

        for (int i = 0; i < pantheonNames.length; i++) {
            String[] godNames = getResources().getStringArray(pantheonArrays[i]);
            List<GodStats> godStatsList = new ArrayList<>();
            for (String godName : godNames) {
                godStatsList.add(new GodStats(godName));
            }
            map.put(pantheonNames[i], godStatsList);
        }

        return map;
    }
}