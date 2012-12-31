package org.iso3103.beercount;

/**
 * POJO for the Drink object.
 * 
 * @author victor@iso3103.net
 */

public class Drink {
	
	public enum Type {
		HALFPINT, PINT, BOTTLE, WINE, HARD, SWEETS
    }

    private int _id;
	private String _timestamp;
	private Type _type;
	
	public Drink() {
		
	}
	
	public Drink(int id, String timestamp, Type type){
		this._id = id;
		this._timestamp = timestamp;
		this._type = type;
	}
	
	public Drink(String timestamp, Type _type){
		this._timestamp = timestamp;
		this._type = _type;
	}
	
	public int getID(){
		return this._id;
	}
	
	public void setID(int id){
		this._id = id;
	}
	
	public String getTimestamp(){
		return this._timestamp;
	}
	
	public void setTimestamp(String timestamp){
		this._timestamp = timestamp;
	}
	
	public Type getType(){
		return this._type;
	}
	
	public void setType(Type type){
		this._type = type;
	}
}
