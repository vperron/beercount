package org.iso3103.beercount;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.jeromq.ZMQ;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;


public class BeerCountActivity extends Activity {

	private static final String TAG = "BeerCount";
	private static final String FILENAME = "beercount.csv";
	private static final String TEMPORARY_LOG = "temp_saved.csv";
	private static final String DEFAULT_ENDPOINT = "tcp://iso3103.net:11337";
	private static final String ACKOK = "ACK_OK";

	private static final byte PINT = 'P';
	private static final byte BOTTLE = 'B';
	private static final byte HALF = 'H';

	ZMQ.Context zmqCtx = null;
	ZMQ.Socket reqSocket = null;
	boolean is_sending = false;
	String lineSep = System.getProperty("line.separator");

	FileOutputStream file = null, tempfile = null;
	int pintCount, halfCount, bottleCount;
	
	TextView pintView = null;
	TextView bottleView = null;
	TextView halfView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final Vibrator hapticHandle = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

		final ImageButton pintBtn = (ImageButton) findViewById(R.id.pint_btn);
		final ImageButton bottleBtn = (ImageButton) findViewById(R.id.bottle_btn);
		final ImageButton halfBtn = (ImageButton) findViewById(R.id.half_btn);

		pintView = (TextView) findViewById(R.id.pintcount);
		bottleView = (TextView) findViewById(R.id.bottlecount);
		halfView = (TextView) findViewById(R.id.halfcount);


		new Thread() {

			@Override
			public void run() {
				zmqCtx = ZMQ.context();
				reqSocket = zmqCtx.socket(ZMQ.REQ);
				reqSocket.connect(DEFAULT_ENDPOINT);
				reqSocket.setLinger(0);
				reqSocket.setSendTimeOut(10 * 1000); // 10s
				reqSocket.setReceiveTimeOut(10 * 1000); // 10s
			}
		}.start();

		/* Get main account name */
		AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		Account[] list = manager.getAccounts();
		final String user = list[0].name;

		/* Update beer counts */
		updateBeerCounts();
		pintView.setText("Pints: " + pintCount);
		bottleView.setText("Bottles: " + bottleCount);
		halfView.setText("Halfs: " + halfCount);

		/* Persistency binary file */
		try {
			file = openFileOutput(FILENAME, Context.MODE_APPEND);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		pintBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(50);
				appendRecord(file, byteRecord(PINT));
				pintCount++;
				pintView.setText("Pints: " + pintCount);
				new Thread(new ZmqSendThread(stringRecord(user, PINT))).start();
			}
		});

		bottleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(50);
				appendRecord(file, byteRecord(BOTTLE));
				bottleCount++;
				bottleView.setText("Bottles: " + bottleCount);
				new Thread(new ZmqSendThread(stringRecord(user, BOTTLE))).start();
			}
		});

		halfBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticHandle.vibrate(50);
				appendRecord(file, byteRecord(HALF));
				halfCount++;
				halfView.setText("Halfs: " + halfCount);
				new Thread(new ZmqSendThread(stringRecord(user, HALF))).start();
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			if (file != null)
				file.close();
			if (tempfile != null)
				tempfile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			helpBuilder.setTitle("BeerCount 2013 :)");
			helpBuilder.setMessage("You can't cheat no more.\nVictor Perron\nLicense: GPLv3.\n<victor@iso3103.net>");
			helpBuilder.setPositiveButton("Ok",
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
			helpBuilder.setTitle("Confirmation");
			helpBuilder.setMessage("Are you sure you want to erase the local stats ?");
			helpBuilder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						file.close();
						tempfile = openFileOutput(FILENAME, Context.MODE_PRIVATE);
						/* Update beer counts */
						updateBeerCounts();
						pintView.setText("Pints: " + pintCount);
						bottleView.setText("Bottles: " + bottleCount);
						halfView.setText("Halfs: " + halfCount);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			helpBuilder.setNegativeButton("No",
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
	/*
	 * Read the persistency file.
	 */
	private void updateBeerCounts() {
		pintCount = 0;
		bottleCount = 0;
		halfCount = 0;

		byte[] buffer = new byte[9];
		FileInputStream fis = null;
		try {
			int length;
			fis = openFileInput(FILENAME);
			while ((length = fis.read(buffer, 0, 9)) != -1) {
				switch (buffer[8]) {
				case PINT:
					pintCount++;
					break;
				case BOTTLE:
					bottleCount++;
					break;
				case HALF:
					halfCount++;
					break;
				}
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Send the saved file upstream.
	 */
	private void sendUnsentRecords() {

		/* Try a PING first */

		byte[] buffer = new byte[9];
		FileInputStream fis = null;
		try {
			int length;
			fis = openFileInput(FILENAME);
			while ((length = fis.read(buffer, 0, 9)) != -1) {
				switch (buffer[8]) {
				case PINT:
					pintCount++;
					break;
				case BOTTLE:
					bottleCount++;
					break;
				case HALF:
					halfCount++;
					break;
				}
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private long now() {
		return System.currentTimeMillis() / 1000;
	}

	private String stringRecord(String user, byte type) {
		String buf = Long.toString(now());
		buf += "," + user + ",";
		buf += Byte.toString(type);
		return buf;
	}

	private byte[] byteRecord(byte type) {
		ByteBuffer buf = ByteBuffer.allocate(9);
		buf.putLong(now());
		buf.put(type);
		return buf.array();
	}

	private void appendRecord(FileOutputStream file, byte[] byteRecord) {
		try {
			file.write(byteRecord);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class ZmqSendThread implements Runnable {

		String orig_string = null;
		String total_string = null;

		public ZmqSendThread(String s) {
			try {
				orig_string = s + lineSep;
				total_string = getCachedStrings() + orig_string;
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d(TAG, "TOTAL : " + total_string + " len = " + total_string.length());
		}

		private String getCachedStrings() throws IOException {
			FileInputStream fis = null;
			String cached = "";
			try {
				fis = openFileInput(TEMPORARY_LOG);
				Scanner sc = new Scanner(fis);
				String record = null;
				while (true) {
					record = sc.nextLine();
					Log.d(TAG, "READ LINE: " + record);
					cached += record + lineSep;
				}
			} catch (NoSuchElementException e) {
				fis.close();
				return cached;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return cached;

		}

		private void dumpLine(String str) {
			try {
				FileOutputStream tempfile = openFileOutput(TEMPORARY_LOG, Context.MODE_APPEND);
				tempfile.write(str.getBytes());
				Log.d(TAG, "DUMPING LINE: " + str);
				tempfile.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			if (!is_sending && reqSocket != null && reqSocket.send(total_string)) {
				is_sending = true;
				String answer = reqSocket.recvStr();
				is_sending = false;
				if (answer == null || !answer.contentEquals(ACKOK)) {
					/* If server not reachable, save the string. */
					reqSocket.close();
					reqSocket = null;
					dumpLine(orig_string);
				} else {
					/* If server answered correctly, reset the file */
					try {
						tempfile = openFileOutput(TEMPORARY_LOG, Context.MODE_PRIVATE);
						tempfile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			} else {
				dumpLine(orig_string);
			}

		}

	}

}
