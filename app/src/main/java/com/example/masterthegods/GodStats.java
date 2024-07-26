package com.example.masterthegods;

public class GodStats {
    double attempt_count;
    double success_count;
    double success_rate;
    String name;

    public GodStats(String name) {
        attempt_count = 0;
        success_count = 0;
        success_rate = 0;
        this.name = name;
    }

    public void updateSuccessRate() {
        if (attempt_count != 0) {
            success_rate = success_count / attempt_count;
        } else {
            success_rate = 0;
        }
    }

    public void reset() {
        attempt_count = 0;
        success_count = 0;
        success_rate = 0;
    }
}
