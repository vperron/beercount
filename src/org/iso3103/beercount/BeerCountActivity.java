package org.iso3103.beercount;

import org.iso3103.beercount.Drink.Type;
import org.iso3103.beercount.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class BeerCountActivity extends Activity {

	private static final String TAG = BeerCountActivity.class.getName();
	
	private static final long VIBRATION_DURATION = 50; // millisecs

	int pintCount, halfCount, bottleCount;
	
	private Vibrator hapticHandle;
	private Button pintBtn, bottleBtn, halfBtn, wineBtn, cocktailBtn;
    private TextView countView;

	private DrinkInterface drinkInterface;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		drinkInterface = new DrinkInterface(this);
		hapticHandle = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		
		// startService(new Intent(getApplicationContext(), BeerCountService.class));

		initActivityControls();
		
		updateCountView();

		pintBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(VIBRATION_DURATION);
				drinkInterface.addDrink(Type.PINT);
				updateCountView();
			}
		});

		bottleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(VIBRATION_DURATION);
				drinkInterface.addDrink(Type.BOTTLE);
				updateCountView();
			}
		});

		halfBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(VIBRATION_DURATION);
				drinkInterface.addDrink(Type.HALFPINT);
				updateCountView();
			}
		});
		
		cocktailBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(VIBRATION_DURATION);
				drinkInterface.addDrink(Type.COCKTAIL);
				updateCountView();
			}
		});
		
		wineBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(VIBRATION_DURATION);
				drinkInterface.addDrink(Type.WINE);
				updateCountView();
			}
		});

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
		AlertDialog helpDialog;
		switch (item.getItemId()) {
		case R.id.undo:
			drinkInterface.undoLastDrink();
			updateCountView();
			return true;
		case R.id.about:
            helpBuilder.setTitle(getString(R.string.beercount_2013));
            helpBuilder.setMessage(getString(R.string.license));
            helpBuilder.setPositiveButton(getString(android.R.string.ok),
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
            helpBuilder.setTitle(getString(R.string.confirmation));
            helpBuilder.setMessage(getString(R.string.confirm_erase_local));
            helpBuilder.setPositiveButton(getString(R.string.yes),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        	drinkInterface.deleteAllDrinks();
                        	updateCountView();
                        }
                    });
            helpBuilder.setNegativeButton(getString(R.string.no),
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

	private void initActivityControls() {
		pintBtn = (Button) findViewById(R.id.pint_btn);
		bottleBtn = (Button) findViewById(R.id.bottle_btn);
		halfBtn = (Button) findViewById(R.id.half_btn);
		wineBtn = (Button) findViewById(R.id.wine_btn);
		cocktailBtn = (Button) findViewById(R.id.cocktail_btn);
	
		countView = (TextView) findViewById(R.id.counts);
	}

	private void updateCountView() {
		countView.setText(getString(R.string.count_text, 
				drinkInterface.getDrinkCount(Type.PINT),
				drinkInterface.getDrinkCount(Type.HALFPINT),
				drinkInterface.getDrinkCount(Type.BOTTLE),
				drinkInterface.getDrinkCount(Type.WINE),
				drinkInterface.getDrinkCount(Type.COCKTAIL)));
	}

}
