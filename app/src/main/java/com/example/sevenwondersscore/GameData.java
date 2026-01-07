package com.example.sevenwondersscore;

import java.util.HashMap;
import java.util.Map;

public class GameData {

    // Singleton
    private static GameData instance;

    // Mappa delle statistiche
    private Map<String, NewGameActivity.PlayerStats> statistics = new HashMap<>();

    // Costruttore privato
    private GameData() {}

    // Metodo per ottenere l'istanza unica
    public static GameData getInstance() {
        if (instance == null) instance = new GameData();
        return instance;
    }

    // Getter e setter per le statistiche
    public Map<String, NewGameActivity.PlayerStats> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, NewGameActivity.PlayerStats> stats) {
        statistics = stats;
    }
}
