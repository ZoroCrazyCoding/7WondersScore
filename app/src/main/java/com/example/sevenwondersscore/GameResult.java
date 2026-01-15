package com.example.sevenwondersscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameResult implements Serializable {
    public String gameId;
    public long timestamp;
    public String gameType;
    public String winnerName;
    public String victoryType; // "points", "military", "scientific"
    public int numPlayers;
    public List<PlayerResult> players;

    public GameResult() {
        this.players = new ArrayList<>();
    }

    public GameResult(String gameId, long timestamp, String gameType, String winnerName,
                      String victoryType, int numPlayers) {
        this.gameId = gameId;
        this.timestamp = timestamp;
        this.gameType = gameType;
        this.winnerName = winnerName;
        this.victoryType = victoryType;
        this.numPlayers = numPlayers;
        this.players = new ArrayList<>();
    }

    // Getters e Setters per Firebase
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }

    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }

    public String getVictoryType() { return victoryType; }
    public void setVictoryType(String victoryType) { this.victoryType = victoryType; }

    public int getNumPlayers() { return numPlayers; }
    public void setNumPlayers(int numPlayers) { this.numPlayers = numPlayers; }

    public List<PlayerResult> getPlayers() { return players; }
    public void setPlayers(List<PlayerResult> players) { this.players = players; }

    public static class PlayerResult implements Serializable {
        public String name;
        public int totalScore;
        public List<Integer> categoryScores; // punteggi per ogni categoria

        public PlayerResult() {
            this.categoryScores = new ArrayList<>();
        }

        public PlayerResult(String name, int totalScore, List<Integer> categoryScores) {
            this.name = name;
            this.totalScore = totalScore;
            this.categoryScores = categoryScores != null ? categoryScores : new ArrayList<>();
        }

        // Getters e Setters per Firebase
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getTotalScore() { return totalScore; }
        public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

        public List<Integer> getCategoryScores() { return categoryScores; }
        public void setCategoryScores(List<Integer> categoryScores) { this.categoryScores = categoryScores; }
    }
}