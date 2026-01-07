package com.example.sevenwondersscore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "scoreDB";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE players(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
        db.execSQL("CREATE TABLE games(id INTEGER PRIMARY KEY AUTOINCREMENT, gameType TEXT, winnerId INTEGER)");
        db.execSQL("CREATE TABLE scores(id INTEGER PRIMARY KEY AUTOINCREMENT, gameId INTEGER, playerId INTEGER, military INTEGER, civil INTEGER, total INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS players");
        db.execSQL("DROP TABLE IF EXISTS games");
        db.execSQL("DROP TABLE IF EXISTS scores");
        onCreate(db);
    }

    // Aggiungi un nuovo giocatore se non esiste
    public long addPlayer(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        long id = db.insertWithOnConflict("players", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        if(id == -1){ // già esiste
            Cursor c = db.rawQuery("SELECT id FROM players WHERE name=?", new String[]{name});
            if(c.moveToFirst()){
                id = c.getLong(0);
            }
            c.close();
        }
        return id;
    }

    public long addGame(String gameType, long winnerId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("gameType", gameType);
        cv.put("winnerId", winnerId);
        return db.insert("games", null, cv);
    }

    public long addScore(long gameId, long playerId, int military, int civil){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("gameId", gameId);
        cv.put("playerId", playerId);
        cv.put("military", military);
        cv.put("civil", civil);
        cv.put("total", military + civil);
        return db.insert("scores", null, cv);
    }

    // Recupera statistiche giocatori
    public Cursor getPlayerStats(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p.name, COUNT(g.id) AS played, " +
                "SUM(CASE WHEN g.winnerId=p.id THEN 1 ELSE 0 END) AS won, " +
                "SUM(CASE WHEN g.winnerId!=p.id THEN 1 ELSE 0 END) AS lost " +
                "FROM players p LEFT JOIN games g ON g.winnerId=p.id " +
                "GROUP BY p.id";
        return db.rawQuery(query, null);
    }
}