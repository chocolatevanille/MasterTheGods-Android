package game.hollowknight.masterthegods;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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

        //Initialize GodStats and history lists (and their maps) for all pantheons
        initializeData();

        //Load any saved data
        loadData();

        //Create spinners and their interactions
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
                        //Toast.makeText(MainActivity.this, "Selected: " + new_pantheon, Toast.LENGTH_SHORT).show();
                        displayStatus("Selected: " + new_pantheon);
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
                            //Toast.makeText(MainActivity.this, "Slain by: " + slayer, Toast.LENGTH_SHORT).show();
                            displayStatus("Slain by: " + slayer, slayer);
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
                Spinner my_slayer_spinner = findViewById(R.id.slayers);
                String selectedSlayer = slayer_spinner.getSelectedItem().toString();
                updateSlayerImage(selectedSlayer);
                //Toast.makeText(MainActivity.this, "Slain by: " + selectedSlayer, Toast.LENGTH_SHORT).show();
                displayStatus("Slain by: " + selectedSlayer, selectedSlayer);
                if (history != null) {
                    history.add(selectedSlayer);
                }
                updateSlayCount(selectedSlayer);
            }
        });

        Button undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> history = pantheonToHistoryMap.get(active_pantheon);
                if ((history != null) && (history.isEmpty())) {
                    //Toast.makeText(MainActivity.this, "No death history", Toast.LENGTH_SHORT).show();
                    displayStatus("No death history");
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
                                //Toast.makeText(MainActivity.this, "Undid death to: " + god.name, Toast.LENGTH_SHORT).show();
                                displayStatus("Undid death to: " + god.name, god.name);
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

    @Override
    protected void onPause() {
        super.onPause();
        saveData(); //Always save data when user navigates away from app
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData(); //Always save data when user navigates away from app
    }

    //initializeData(): void -> void
    //Initializes lists of GodStats and history for each Pantheon
    private void initializeData() {
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
    }

    //saveData(): void -> void
    //Saves user data using SharedPreferences, called when user navigates away from app
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("GodStatsPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        editor.putString("potm_gods", gson.toJson(potm_gods));
        editor.putString("pota_gods", gson.toJson(pota_gods));
        editor.putString("pots_gods", gson.toJson(pots_gods));
        editor.putString("potk_gods", gson.toJson(potk_gods));
        editor.putString("poh_gods", gson.toJson(poh_gods));
        editor.putString("potm_history", gson.toJson(potm_history));
        editor.putString("pota_history", gson.toJson(pota_history));
        editor.putString("pots_history", gson.toJson(pots_history));
        editor.putString("potk_history", gson.toJson(potk_history));
        editor.putString("poh_history", gson.toJson(poh_history));

        editor.apply(); // Use apply() for asynchronous operation
        Log.d("SaveData", "Data saved successfully.");
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("GodStatsPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        Type godStatsType = new TypeToken<ArrayList<GodStats>>() {}.getType();
        Type stringListType = new TypeToken<ArrayList<String>>() {}.getType();

        String potm_gods_json = sharedPreferences.getString("potm_gods", null);
        if (potm_gods_json != null) {
            potm_gods = gson.fromJson(potm_gods_json, godStatsType);
        }

        String pota_gods_json = sharedPreferences.getString("pota_gods", null);
        if (pota_gods_json != null) {
            pota_gods = gson.fromJson(pota_gods_json, godStatsType);
        }

        String pots_gods_json = sharedPreferences.getString("pots_gods", null);
        if (pots_gods_json != null) {
            pots_gods = gson.fromJson(pots_gods_json, godStatsType);
        }

        String potk_gods_json = sharedPreferences.getString("potk_gods", null);
        if (potk_gods_json != null) {
            potk_gods = gson.fromJson(potk_gods_json, godStatsType);
        }

        String poh_gods_json = sharedPreferences.getString("poh_gods", null);
        if (poh_gods_json != null) {
            poh_gods = gson.fromJson(poh_gods_json, godStatsType);
        }

        String potm_history_json = sharedPreferences.getString("potm_history", null);
        if (potm_history_json != null) {
            potm_history = gson.fromJson(potm_history_json, stringListType);
        }

        String pota_history_json = sharedPreferences.getString("pota_history", null);
        if (pota_history_json != null) {
            pota_history = gson.fromJson(pota_history_json, stringListType);
        }

        String pots_history_json = sharedPreferences.getString("pots_history", null);
        if (pots_history_json != null) {
            pots_history = gson.fromJson(pots_history_json, stringListType);
        }

        String potk_history_json = sharedPreferences.getString("potk_history", null);
        if (potk_history_json != null) {
            potk_history = gson.fromJson(potk_history_json, stringListType);
        }

        String poh_history_json = sharedPreferences.getString("poh_history", null);
        if (poh_history_json != null) {
            poh_history = gson.fromJson(poh_history_json, stringListType);
        }

        // Reinitialize maps after loading data
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

        Log.d("LoadData", "Data loaded successfully.");
    }

    private void displayStatus(String messageText, String imageName) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        ImageView image = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_message);

        String slayer_image_name = imageName.replace(" ", "_").toLowerCase();
        int imageResourceId = getResources().getIdentifier(slayer_image_name, "drawable", getPackageName());
        if (imageResourceId != 0) {
            image.setImageResource(imageResourceId);
        } else {
            Log.e("MainActivity", "Drawable resource not found: " + slayer_image_name);
        }

        text.setText(messageText);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void displayStatus(String messageText) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        ImageView image = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_message);

        image.setImageResource(R.drawable.hk_icon);
        text.setText(messageText);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    //showStatsDialog: void -> void
    //displays the table of stats
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

    //populateStatsTable: TableLayout -> void
    //Taking in a TableLayout, it puts in the header and then all of the GodStats for the
    // current pantheon
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
                god.updateSuccessRate();
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
            textView.setBackgroundColor(0xFFC8A333); // Header color
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
        positiveButton.setTextColor(0xFFC8A333); // Use your header color

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(0xFFFFFFFF); // Use your header color
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
        //Toast.makeText(MainActivity.this, "Data cleared for " + active_pantheon, Toast.LENGTH_SHORT).show();
        displayStatus("Data cleared for " + active_pantheon);
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