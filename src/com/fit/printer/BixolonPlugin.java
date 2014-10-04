package com.fit.printer;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BixolonPlugin extends CordovaPlugin {
	
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }
    
	@Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("connect")) {
            String message = args.getString(0); 
            this.connect(message, callbackContext);
            return true;
        } else if (action.equals("print")) {
            String message = args.getString(0); 
            this.print(message, callbackContext);
            return true;
        }
        return false;
    }

    private void connect(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) { 
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void print(String text, CallbackContext callbackContext) {
        if (text != null && text.length() > 0) { 
            callbackContext.success(text);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}