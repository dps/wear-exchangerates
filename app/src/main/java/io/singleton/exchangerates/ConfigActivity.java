package io.singleton.exchangerates;

import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConfigActivity extends WearableActivity implements WearableListView.ClickListener {

    private String[] mElements;
    private String[] mSymbols;
    private String[] mTickers;
    private int[] mInvert;
    private int mComplicationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Get the list component from the layout of the activity
        WearableListView listView =
                (WearableListView) findViewById(R.id.wearable_list);

        mElements = getResources().getStringArray(R.array.currency_names);
        mSymbols = getResources().getStringArray(R.array.currency_symbols);
        mTickers = getResources().getStringArray(R.array.currency_tickers);
        mInvert = getResources().getIntArray(R.array.currency_invert);


        // Assign an adapter to the list
        listView.setAdapter(new Adapter(this, mElements));

        // Set a click listener
        listView.setClickListener(this);

        mComplicationId = getIntent().getIntExtra(
                android.support.wearable.complications.ComplicationProviderService.EXTRA_CONFIG_COMPLICATION_ID, -1);

        int selected = getSharedPreferences("config", 0).getInt(mComplicationId + "_selected_position", 0);
        listView.scrollToPosition(selected);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int pos = viewHolder.getAdapterPosition();
        getSharedPreferences("config", 0).edit()
                .putInt(mComplicationId + "_selected_position", pos)
                .putString(mComplicationId + "_selected_currency_name", mElements[pos])
                .putString(mComplicationId + "_selected_currency_symbol", mSymbols[pos])
                .putString(mComplicationId + "_selected_currency_ticker", mTickers[pos])
                .putInt(mComplicationId + "_selected_currency_invert", mInvert[pos])
                .apply();

        setResult(RESULT_OK);
        ComplicationService.requestUpdateComplication(this, mComplicationId);
        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    private static final class Adapter extends WearableListView.Adapter {
        private String[] mDataset;
        private final LayoutInflater mInflater;

        public Adapter(Context context, String[] dataset) {
            mInflater = LayoutInflater.from(context);
            mDataset = dataset;
        }

        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView textView;
            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                textView = (TextView) itemView.findViewById(R.id.name);
            }
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
            return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder,
                                     int position) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView view = itemHolder.textView;
            view.setText(mDataset[position]);
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }

}
