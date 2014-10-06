package com.fit.printer;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bixolon.printer.BixolonPrinter;

public class BixolonPlugin extends CordovaPlugin {
	protected static final String TAG = "BixolonPlugin";
	static BixolonPrinter mBixolonPrinter;
	private boolean mIsConnected;
	private String docToPrint;

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		Log.i(TAG, "Bixolon Printer bluetooth");
		mBixolonPrinter = new BixolonPrinter(this.cordova.getActivity()
				.getApplicationContext(), mHandler, null);
	}

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		if (action.equals("print")) {
			String message = args.getString(0);
			this.print(message, callbackContext);
			return true;
		}
		return false;
	}

	private void connect(String message, CallbackContext callbackContext) {
		if (message != null && message.length() > 0) {
			mBixolonPrinter.connect((String) null);
			callbackContext.success(message);
		} else {
			callbackContext.error("Expected one non-empty string argument.");
		}
	}

	private void print(String xml, CallbackContext callbackContext) {
		if (xml != null && xml.length() > 0) {
			docToPrint = xml;
			mBixolonPrinter.connect((String) null);
			callbackContext.success("OK");
		} else {
			callbackContext.error("Expected one non-empty string argument.");
		}
	}

	private final Handler mHandler = new Handler(new Handler.Callback() {

		@SuppressWarnings("unchecked")
		@Override
		public boolean handleMessage(Message msg) {
			Log.d(TAG, "mHandler.handleMessage(" + msg + ")");

			switch (msg.what) {
			case BixolonPrinter.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BixolonPrinter.STATE_CONNECTED:
					mIsConnected = true;
					Log.i(TAG, "Connected to bluetooth");
					/*
					 * mBixolonPrinter.formFeed(true);
					 * mBixolonPrinter.printText("AM Test\n",
					 * BixolonPrinter.ALIGNMENT_CENTER,
					 * BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
					 * BixolonPrinter.TEXT_SIZE_HORIZONTAL1 |
					 * BixolonPrinter.TEXT_SIZE_VERTICAL1, true);
					 */

					DocumentBuilderFactory factory = DocumentBuilderFactory
							.newInstance();
					try {
						Log.i(TAG, docToPrint);
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document dom = builder.parse(new ByteArrayInputStream(
								docToPrint.getBytes(StandardCharsets.UTF_8)));
						Element root = dom.getDocumentElement();
						NodeList items = root.getElementsByTagName("font");
						for (int i = 0; i < items.getLength(); i++) {
							Element item = (Element) items.item(i);

							int size = (item.getAttribute("size") == null || ""
									.equals(item.getAttribute("size"))) ? 0
									: Integer.parseInt(item
											.getAttribute("size"));
							int align = (item.getAttribute("align") == null || ""
									.equals(item.getAttribute("align"))) ? 0
									: Integer.parseInt(item
											.getAttribute("align"));
							int attribute = (item.getAttribute("attribute") == null || ""
									.equals(item.getAttribute("attribute"))) ? 0
									: Integer.parseInt(item
											.getAttribute("attribute"));
							String text = item.getTextContent();

							Log.i(TAG, "size:" + size + " align:" + align
									+ " attribute:" + attribute + " text:"
									+ text);
							mBixolonPrinter.printText(text+"\n", align, attribute,
									size, false);
						}
						mBixolonPrinter.cutPaper(true);
					} catch (Exception e) {
						Log.i(TAG, e.getMessage());
						throw new RuntimeException(e);
					}
					break;

				case BixolonPrinter.STATE_CONNECTING:
					Log.i(TAG, "Connecting to bluetooth");
					break;

				case BixolonPrinter.STATE_NONE:
					Log.i(TAG, "Disconnected to bluetooth");
					mIsConnected = false;
					break;
				}
				return true;

			case BixolonPrinter.MESSAGE_WRITE:
				switch (msg.arg1) {
				case BixolonPrinter.PROCESS_SET_SINGLE_BYTE_FONT:
					// Bundle data = msg.getData();
					// Toast.makeText(getApplicationContext(),
					// data.getString(BixolonPrinter.KEY_STRING_CODE_PAGE),
					// Toast.LENGTH_SHORT).show();
					break;

				case BixolonPrinter.PROCESS_SET_DOUBLE_BYTE_FONT:
					// mHandler.obtainMessage(MESSAGE_END_WORK).sendToTarget();

					// Toast.makeText(getApplicationContext(),
					// "Complete to set double byte font.",
					// Toast.LENGTH_SHORT).show();
					break;

				case BixolonPrinter.PROCESS_DEFINE_NV_IMAGE:
					mBixolonPrinter.getDefinedNvImageKeyCodes();
					// Toast.makeText(getApplicationContext(),
					// "Complete to define NV image", Toast.LENGTH_LONG).show();
					break;

				case BixolonPrinter.PROCESS_REMOVE_NV_IMAGE:
					mBixolonPrinter.getDefinedNvImageKeyCodes();
					// Toast.makeText(getApplicationContext(),
					// "Complete to remove NV image", Toast.LENGTH_LONG).show();
					break;

				case BixolonPrinter.PROCESS_UPDATE_FIRMWARE:
					mBixolonPrinter.disconnect();
					// Toast.makeText(getApplicationContext(),
					// "Complete to download firmware.\nPlease reboot the printer.",
					// Toast.LENGTH_SHORT).show();
					break;
				}
				return true;

			case BixolonPrinter.MESSAGE_READ:
				// MainActivity.this.dispatchMessage(msg);
				return true;

			case BixolonPrinter.MESSAGE_DEVICE_NAME:
				// mConnectedDeviceName =
				// msg.getData().getString(BixolonPrinter.KEY_STRING_DEVICE_NAME);
				// Toast.makeText(getApplicationContext(), mConnectedDeviceName,
				// Toast.LENGTH_LONG).show();
				return true;

			case BixolonPrinter.MESSAGE_TOAST:
				// mListView.setEnabled(false);
				// Toast.makeText(getApplicationContext(),
				// msg.getData().getString(BixolonPrinter.KEY_STRING_TOAST),
				// Toast.LENGTH_SHORT).show();
				return true;

			case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
				/*
				 * if (msg.obj == null) {
				 * Toast.makeText(BixolonPrint.appContext, "No paired device",
				 * Toast.LENGTH_SHORT).show(); } else {
				 * DialogManager.showBluetoothDialog(this.cordova.getActivity(),
				 * (Set<BluetoothDevice>) msg.obj); }
				 */
				// LOG.i("am", ((Set<BluetoothDevice>) msg.obj).toString());
				Set<BluetoothDevice> bluetoothDevicesSet = (Set<BluetoothDevice>) msg.obj;
				for (BluetoothDevice device : bluetoothDevicesSet) {
					Log.i(TAG, device.getName());
					// if(device.getName().equals("SPP-R300")) {
					// mBixolonPrinter.connect(device.getAddress());
					//
					// break;
					// }
				}
				mBixolonPrinter.connect((String) null);
				return true;

			case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
				// Toast.makeText(getApplicationContext(), "Complete to print",
				// Toast.LENGTH_SHORT).show();
				mBixolonPrinter.disconnect();
				return true;

			case BixolonPrinter.MESSAGE_ERROR_INVALID_ARGUMENT:
				// Toast.makeText(getApplicationContext(), "Invalid argument",
				// Toast.LENGTH_SHORT).show();
				return true;

			case BixolonPrinter.MESSAGE_ERROR_NV_MEMORY_CAPACITY:
				// Toast.makeText(getApplicationContext(),
				// "NV memory capacity error", Toast.LENGTH_SHORT).show();
				return true;

			case BixolonPrinter.MESSAGE_ERROR_OUT_OF_MEMORY:
				// Toast.makeText(getApplicationContext(), "Out of memory",
				// Toast.LENGTH_SHORT).show();
				return true;

			case BixolonPrinter.MESSAGE_COMPLETE_PROCESS_BITMAP:
				String text = "Complete to process bitmap.";
				Bundle data = msg.getData();
				byte[] value = data
						.getByteArray(BixolonPrinter.KEY_STRING_MONO_PIXELS);
				/*
				 * if (value != null) { Intent intent = new Intent();
				 * intent.setAction(ACTION_COMPLETE_PROCESS_BITMAP);
				 * intent.putExtra(EXTRA_NAME_BITMAP_WIDTH, msg.arg1);
				 * intent.putExtra(EXTRA_NAME_BITMAP_HEIGHT, msg.arg2);
				 * intent.putExtra(EXTRA_NAME_BITMAP_PIXELS, value);
				 * sendBroadcast(intent); }
				 * 
				 * Toast.makeText(getApplicationContext(), text,
				 * Toast.LENGTH_SHORT).show();
				 */
				return true;

				// case MESSAGE_START_WORK:
				// mListView.setEnabled(false);
				// mProgressBar.setVisibility(View.VISIBLE);
				// return true;

				// case MESSAGE_END_WORK:
				// mListView.setEnabled(true);
				// mProgressBar.setVisibility(View.INVISIBLE);
				// return true;

			case BixolonPrinter.MESSAGE_USB_DEVICE_SET:
				/*
				 * if (msg.obj == null) {
				 * Toast.makeText(getApplicationContext(),
				 * "No connected device", Toast.LENGTH_SHORT).show(); } else {
				 * DialogManager.showUsbDialog(MainActivity.this,
				 * (Set<UsbDevice>) msg.obj, mUsbReceiver); }
				 */
				return true;

			case BixolonPrinter.MESSAGE_NETWORK_DEVICE_SET:
				/*
				 * if (msg.obj == null) {
				 * Toast.makeText(getApplicationContext(),
				 * "No connectable device", Toast.LENGTH_SHORT).show(); }
				 * DialogManager.showNetworkDialog(MainActivity.this,
				 * (Set<String>) msg.obj);
				 */
				return true;
			}
			return false;
		}
	});
}