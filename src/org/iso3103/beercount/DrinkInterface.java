package org.iso3103.beercount;

import java.util.List;

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

	public void undoLastDrink() {
		List<Drink> drinks = db.getAllDrinks();
		if(drinks.size() > 0) {
			Drink latest = drinks.get(0);
			long latestsTimestamp = Long.parseLong(latest.getTimestamp()); 
			for(Drink d: drinks) {
				long currentTimestamp = Long.parseLong(d.getTimestamp());   
				if(currentTimestamp > latestsTimestamp)
					latest = d;
			}
			db.deleteDrink(latest);
		}
	}

}
