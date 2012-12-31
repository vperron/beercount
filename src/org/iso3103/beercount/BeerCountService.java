package org.iso3103.beercount;



import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This service will be used in future async or network operations.
 * 
 * @author victor
 *
 */
public class BeerCountService extends Service{

    @Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
        final String userName = getUserName();
		if(userName != null) {
			// initZeroMq();
		}
	}


	@Override
	public void onDestroy() {
		// deinitZeroMq();
	}

	private String getUserName() throws ArrayIndexOutOfBoundsException {
		AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		Account[] list = manager.getAccounts();
		if(list.length > 0)
			return list[0].name;
		
		return null;
	}


}
