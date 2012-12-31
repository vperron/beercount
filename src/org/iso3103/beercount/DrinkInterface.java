package org.iso3103.beercount;

import android.content.Context;

class DrinkInterface {
	
	private final DatabaseHandler db;

	public DrinkInterface(Context c) {
		db = new DatabaseHandler(c);
	}
	
	public void addDrink(Drink.Type type) {
		db.addDrink((new Drink(ts(), type)));
	}
	
	public  int getDrinkCount(Drink.Type type) {
		return db.getTypedDrinksCount(type);
	}

    private String ts() {
		return String.valueOf(System.currentTimeMillis() / 1000);
	}

	public void deleteAllDrinks() {
		db.deleteAllDrinks();
	}

}
