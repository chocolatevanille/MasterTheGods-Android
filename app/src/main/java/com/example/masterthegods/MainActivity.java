package com.example.masterthegods;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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


        String[] potm_gods_str = getResources().getStringArray(R.array.potm_gods);
        for (String god : potm_gods_str) {
            GodStats newGod = new GodStats(god);
            potm_gods.add(newGod);
        }

        String[] pota_gods_str = getResources().getStringArray(R.array.potm_gods);
        for (String god : pota_gods_str) {
            GodStats newGod = new GodStats(god);
            pota_gods.add(newGod);
        }

        String[] pots_gods_str = getResources().getStringArray(R.array.potm_gods);
        for (String god : pots_gods_str) {
            GodStats newGod = new GodStats(god);
            pots_gods.add(newGod);
        }

        String[] potk_gods_str = getResources().getStringArray(R.array.potm_gods);
        for (String god : potk_gods_str) {
            GodStats newGod = new GodStats(god);
            potk_gods.add(newGod);
        }

        String[] poh_gods_str = getResources().getStringArray(R.array.potm_gods);
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
                            return;
                        }
                        String slayer = parent.getItemAtPosition(position).toString();
                        Toast.makeText(MainActivity.this, "Slayed by: " + slayer, Toast.LENGTH_SHORT).show();
                        // Add another death
                        updateSlayCount(slayer);
                        updateSlayerImage(slayer);
                    }


                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
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
}