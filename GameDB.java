package com.louis.minesweeper;

import java.util.Collection;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDB extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "com.louis.minesweeper.database";

	private static final String TABLE_GAME = "game";

	private static final String KEY_ROW = "row";
	private static final String KEY_COL = "col";
	private static final String KEY_IS_BOMB = "is_bomb";
	private static final String KEY_BOMBS_AROUND = "bombs_around";
	private static final String KEY_IS_VISITED = "is_visited";
	private static final String KEY_IS_FLAGGED = "is_flagged";

	public GameDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_GAME_TABLE = "CREATE TABLE " + TABLE_GAME + " ("
				+ KEY_ROW + " INTEGER, " + KEY_COL + " INTEGER, " + KEY_IS_BOMB
				+ " INTEGER," + KEY_BOMBS_AROUND + " INTEGER," + KEY_IS_VISITED
				+ " INTEGER," + KEY_IS_FLAGGED + " INTEGER" + ")";
		db.execSQL(CREATE_GAME_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME);
		onCreate(db);
	}

	public void addTiles(Collection<MineFieldTile> tiles) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			for (MineFieldTile tile : tiles) {
				values.put(KEY_ROW, tile.getCoords().first);
				values.put(KEY_COL, tile.getCoords().second);
				values.put(KEY_IS_BOMB, tile.isBomb() ? 1 : 0);
				values.put(KEY_BOMBS_AROUND, tile.getBombsAround());
				values.put(KEY_IS_VISITED, tile.isVisited() ? 1 : 0);
				values.put(KEY_IS_FLAGGED, tile.isFlagged() ? 1 : 0);
				db.insert(TABLE_GAME, null, values);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		db.close();
	}

	public HashMap<Coords, MineFieldTile> getSavedGame(Context c) {
		HashMap<Coords, MineFieldTile> blocks = new HashMap<Coords, MineFieldTile>();
		String selectQuery = "SELECT  * FROM " + TABLE_GAME;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Coords coord = new Coords(cursor.getInt(0), cursor.getInt(1));
			MineFieldTile tile = new MineFieldTile(c.getApplicationContext(),
					cursor.getInt(0), cursor.getInt(1));
			tile.setIsBomb(cursor.getInt(2) == 1);
			tile.setBombsAround(cursor.getInt(3));
			tile.setVisited(cursor.getInt(4) == 1);
			tile.setFlagged(cursor.getInt(5) == 1);
			blocks.put(coord, tile);
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return blocks;
	}

	public int getRowCount() {
		String countQuery = "SELECT * FROM " + TABLE_GAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();
		return rowCount;
	}

	public void resetTables() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_GAME, null, null);
		db.close();
	}
}