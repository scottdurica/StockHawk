package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by administrator on 9/3/16.
 * Credit to abhinituk (github abhinituk/ StockHawk) for general layout and understanding on how to implement this class
 */
public class StockWidgetService extends RemoteViewsService {

    public static final String SYMBOL = "Symbol";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {

            private Cursor data = null;


            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null){
                    data.close();
                }
                final long token = Binder.clearCallingIdentity();
                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        QuoteColumns.STOCK_COLUMNS,
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(token);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)){
                    return null;
                }

                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_widget);

                String name = data.getString(QuoteColumns.INDEX_SYMBOL);
                String price = data.getString(QuoteColumns.INDEX_BID_PRICE);
                String change = data.getString(QuoteColumns.INDEX_CHANGE);
                String changePercent = data.getString(QuoteColumns.INDEX_PERCENT_CHANGE);

                if (data.getInt(data.getColumnIndex("is_up")) == 1) {
                    remoteViews.setInt(R.id.tv_widget_change, "setBackgroundResource", R.color.material_green_700);
                } else {
                    remoteViews.setInt(R.id.tv_widget_change, "setBackgroundResource", R.color.material_red_700);
                }

                remoteViews.setTextViewText(R.id.tv_widget_stock_symbol,name);
                remoteViews.setTextViewText(R.id.tv_widget_bid_price,price);
                if (Utils.showPercent){
                    remoteViews.setTextViewText(R.id.tv_widget_change,changePercent);
                }else{
                    remoteViews.setTextViewText(R.id.tv_widget_change,change);
                }
                setContentDescription(remoteViews,name);

                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(SYMBOL,name);
                fillInIntent.putExtra("bid_price",price);
                remoteViews.setOnClickFillInIntent(R.id.widget_item_container,fillInIntent);

                return remoteViews;


            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(),R.layout.list_item_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(QuoteColumns.INDEX_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setContentDescription(RemoteViews views, String desc){
                views.setContentDescription(R.id.widget_item_container,desc);
            }
        };

    }
}
