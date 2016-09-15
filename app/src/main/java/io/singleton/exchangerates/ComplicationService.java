package io.singleton.exchangerates;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.support.wearable.complications.ProviderUpdateRequester;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class ComplicationService extends ComplicationProviderService {
    private static final String TAG = "FXCS";

    @Override
    public void onComplicationUpdate(int complicationId, int type, ComplicationManager manager) {
        FxNetworkRequester.makeUpdateRequest(this, complicationId, type, manager);
    }

    static void requestUpdateComplication(Context ctx, int id) {
        ProviderUpdateRequester requester = new ProviderUpdateRequester(
                ctx, ComponentName.createRelative(
                    "io.singleton.exchangerates",
                    ".ComplicationService"));

        requester.requestUpdate(id);
    }

    static void updateComplication(float quote, boolean invert, String symbol, int complicationId, int type,
                                   ComplicationManager manager, PendingIntent pi, Context ctx) {
        if (invert) {
            quote = 1.0f / quote;
        }
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);
        String displayQuote = quote > 0.0f ? df.format(quote) : "-";
        String displayLabel = invert ? "$:" + symbol : symbol + ":$";
        ComplicationData.Builder builder = new ComplicationData.Builder(type);
        builder.setTapAction(pi);

        if (type == ComplicationData.TYPE_SHORT_TEXT) {
            builder.setShortText(ComplicationText.plainText(displayQuote));
            builder.setShortTitle(ComplicationText.plainText(displayLabel));
        } else if (type == ComplicationData.TYPE_LONG_TEXT) {
            builder.setLongText(ComplicationText.plainText(displayQuote));
            builder.setLongTitle(ComplicationText.plainText(displayLabel));
            builder.setIcon(Icon.createWithResource(ctx, R.mipmap.ic_provider_icon));
        }
        manager.updateComplicationData(complicationId, builder.build());

    }
}
