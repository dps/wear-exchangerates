package io.singleton.exchangerates;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class FxNetworkRequester {

    private static final String TAG = "FXNR";
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000;
    private static FxNetworkRequester sInstance;

    private final RequestQueue mRequestQueue;

    public FxNetworkRequester(Context ctx) {
        mRequestQueue = Volley.newRequestQueue(ctx);
    }

    public static synchronized FxNetworkRequester getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new FxNetworkRequester(ctx);
        }
        return sInstance;
    }

    private final static String BASE_URL = "http://fx.us.davidsingleton.org/";

    private void makeRequest(StringRequest request) {
        mRequestQueue.add(request);
    }

    public static void makeUpdateRequest(final Context ctx,
                                         final int complicationId, final int type, final ComplicationManager manager) {
        FxNetworkRequester requester = getInstance(ctx);

        final SharedPreferences sharedPreferences = ctx.getSharedPreferences("config", 0);
        final String ticker = sharedPreferences.getString(complicationId + "_selected_currency_ticker", "EUR");
        final String symbol = sharedPreferences.getString(complicationId + "_selected_currency_symbol", "€");
        final boolean invert = sharedPreferences.getInt(complicationId + "_selected_currency_invert", 0) == 0 ? false : true;
        final Intent intent = new Intent(ctx, ConfigActivity.class);
        intent.putExtra(ComplicationProviderService.EXTRA_CONFIG_COMPLICATION_ID, complicationId);
        final PendingIntent pi = PendingIntent.getActivity(ctx, complicationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final float cachedQuote = sharedPreferences.getFloat(ticker + "_quote", 0.0f);
        final long cachedAgeMillis = System.currentTimeMillis() - sharedPreferences.getLong(ticker + "_when", 0);
        if (cachedAgeMillis < ONE_HOUR_MILLIS) {
            ComplicationService.updateComplication(cachedQuote, invert, symbol, complicationId, type, manager, pi, ctx);
            return;
        }
        // Show '-' while fetching...
        ComplicationService.updateComplication(0.0f, invert, symbol, complicationId, type, manager, pi, ctx);

        StringRequest request = new StringRequest(Request.Method.GET, BASE_URL + ticker, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Network Response: " + response);
                float quote = Float.parseFloat(response);
                sharedPreferences.edit()
                        .putFloat(ticker + "_quote", quote)
                        .putLong(ticker + "_when", System.currentTimeMillis())
                        .apply();
                ComplicationService.updateComplication(quote, invert, symbol, complicationId, type, manager, pi, ctx);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error Response " + error);

                float quote = cachedAgeMillis < ONE_DAY_MILLIS ? cachedQuote : 0.0f;

                ComplicationService.updateComplication(quote, invert, symbol, complicationId, type, manager, pi, ctx);
            }
        });
        requester.makeRequest(request);

    }

}
