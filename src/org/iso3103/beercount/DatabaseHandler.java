package org.iso3103.beercount;

import org.iso3103.beercount.Drink.Type;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Following code is courtesy from AndroidHive and freely adapted.
 * Original source on:
 * http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 * 
 * Victor Perron
 */

class DatabaseHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "beerCountDb";
	private static final String TABLE_DRINKS = "drinks";

	private static final String KEY_ID = "id";
	private static final String KEY_TIMESTAMP = "ts";
	private static final String KEY_DRINKTYPE = "type";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createDrinksTable(db);
	}

	private void createDrinksTable(SQLiteDatabase db) {
		String CREATE_DRINKS_TABLE = "CREATE TABLE " + TABLE_DRINKS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIMESTAMP + " TEXT,"
				+ KEY_DRINKTYPE + " INTEGER" + ")";
		db.execSQL(CREATE_DRINKS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRINKS);
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	void addDrink(Drink drink) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TIMESTAMP, drink.getTimestamp());
		values.put(KEY_DRINKTYPE, drink.getType().ordinal());

		db.insert(TABLE_DRINKS, null, values);
		db.close();
	}

	Drink getDrink(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_DRINKS, new String[] { KEY_ID, KEY_TIMESTAMP, KEY_DRINKTYPE }, 
				KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor.moveToFirst()) {
            return new Drink(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), Type.values()[cursor.getInt(2)]);
        } else {
            // TODO: what to do here? Probably not that.
            return null;
        }
	}
	
	public List<Drink> getAllDrinks() {
		List<Drink> drinkList = new ArrayList<Drink>();

		String selectQuery = "SELECT  * FROM " + TABLE_DRINKS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			do {
				Drink drink = new Drink();
				drink.setID(Integer.parseInt(cursor.getString(0)));
				drink.setTimestamp(cursor.getString(1));
				drink.setType(Drink.Type.values()[cursor.getInt(2)]);
				// Adding drink to list
				drinkList.add(drink);
			} while (cursor.moveToNext());
		}

		return drinkList;
	}

	public int updateDrink(Drink drink) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TIMESTAMP, drink.getTimestamp());
		values.put(KEY_DRINKTYPE, drink.getType().ordinal());

		return db.update(TABLE_DRINKS, values, KEY_ID + " = ?",
				new String[] { String.valueOf(drink.getID()) });
	}

	public void deleteDrink(Drink drink) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_DRINKS, KEY_ID + " = ?",
				new String[] { String.valueOf(drink.getID()) });
		db.close();
	}
	
	public void deleteAllDrinks() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_DRINKS, null, null);
		db.close();
	}

	public int getDrinksCount() {
		String countQuery = "SELECT  * FROM " + TABLE_DRINKS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}
	
	public int getTypedDrinksCount(Type type) {
		String countQuery = "SELECT  * FROM " + TABLE_DRINKS + " WHERE " +
							KEY_DRINKTYPE + " = " + type.ordinal();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

}
