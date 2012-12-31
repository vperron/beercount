package org.iso3103.beercount;

import org.iso3103.beercount.Drink.Type;
import org.iso3103.beercount.R.string;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;


public class BeerCountActivity extends Activity {

	private static final String TAG = BeerCountActivity.class.getName();
	
	protected static final long VIBRATION_DURATION = 50;

	int pintCount, halfCount, bottleCount;
	
	Vibrator hapticHandle;
	ImageButton pintBtn, bottleBtn, halfBtn; 
	TextView pintView, bottleView, halfView;

	private DrinkInterface drinkInterface;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		drinkInterface = new DrinkInterface(this);
		hapticHandle = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		
		startService(new Intent(getApplicationContext(), BeerCountService.class));

		initActivityControls();
		
		updateDrinkView(pintView, R.string.pints, Type.PINT);
		updateDrinkView(bottleView, R.string.bottles, Type.BOTTLE);
		updateDrinkView(halfView, R.string.halfs, Type.HALFPINT);

		pintBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(VIBRATION_DURATION);
				drinkInterface.addDrink(Type.PINT);
				updateDrinkView(pintView, R.string.pints, Type.PINT);
			}
		});

		bottleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(VIBRATION_DURATION);
				drinkInterface.addDrink(Type.BOTTLE);
				updateDrinkView(bottleView, R.string.bottles, Type.BOTTLE);
			}
		});

		halfBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(VIBRATION_DURATION);
				drinkInterface.addDrink(Type.HALFPINT);
				updateDrinkView(halfView, R.string.halfs, Type.HALFPINT);
			}
		});

	}

	private void updateDrinkView(TextView view, int resid, Type type) {
		/* TODO: More clever function which gets the type only and
		 * updates the correct view with the right text from resources.
		 */
		view.setText(getString(resid, drinkInterface.getDrinkCount(type)));
	}

	private void initActivityControls() {
		pintBtn = (ImageButton) findViewById(R.id.pint_btn);
		bottleBtn = (ImageButton) findViewById(R.id.bottle_btn);
		halfBtn = (ImageButton) findViewById(R.id.half_btn);

		pintView = (TextView) findViewById(R.id.pintcount);
		bottleView = (TextView) findViewById(R.id.bottlecount);
		halfView = (TextView) findViewById(R.id.halfcount);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.xml.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
		AlertDialog helpDialog = null;
		switch (item.getItemId()) {
		case R.id.about:
            helpBuilder.setTitle(getString(string.beercount_2013));
            helpBuilder.setMessage(getString(string.license));
            helpBuilder.setPositiveButton(getString(string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog
                        }
                    });
            helpDialog = helpBuilder.create();
            helpDialog.show();
			return true;
		case R.id.reset:
            helpBuilder.setTitle(getString(string.confirmation));
            helpBuilder.setMessage(getString(string.confirm_erase_local));
            helpBuilder.setPositiveButton(getString(string.yes),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        	drinkInterface.deleteAllDrinks();
                    		updateDrinkView(pintView, R.string.pints, Type.PINT);
                    		updateDrinkView(bottleView, R.string.bottles, Type.BOTTLE);
                    		updateDrinkView(halfView, R.string.halfs, Type.HALFPINT);
                        }
                    });
            helpBuilder.setNegativeButton(getString(string.no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog
                        }
                    });
            helpDialog = helpBuilder.create();
            helpDialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
