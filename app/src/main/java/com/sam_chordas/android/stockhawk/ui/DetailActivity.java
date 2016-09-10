package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.LinearEase;
import com.db.chart.view.animation.style.DashAnimation;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.NetworkChangeEvent;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends Activity {

    private Context mContext;

    @BindView(R.id.linechart) LineChartView chartView;
    @BindView(R.id.tv_thirty_day_average) TextView averageValue;
    @BindView(R.id.tv_high_low) TextView hiLowValue;
    @BindView(R.id.tv_current_bid_price) TextView bidPrice;
    @BindView(R.id.tv_detail_no_connection_message) TextView mNoConnectionView;
    @BindView(R.id.label_30_day_hilo) TextView mLabel30HiLo;
    @BindView(R.id.label_30_day_average) TextView mLabel30Average;
    @BindView(R.id.label_current_price) TextView mLabelCurrentPrice;
    @BindView(R.id.tv_symbol_name) TextView mSymbolName;
    @BindView(R.id.progress_spinner_chartview) ProgressBar mProgressBar;
    float thirtyDayAverage;
    private String mSymbol;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIEW_STATE_CONNECTED,VIEW_STATE_NO_NETWORK})
    public @interface ViewState{}
    final int VIEW_STATE_CONNECTED = 1;
    final int VIEW_STATE_NO_NETWORK = 2;



    @Subscribe
    public void onNetworkChangeEvent(NetworkChangeEvent event){

        if (event.show){
            setViews(VIEW_STATE_CONNECTED);
            fetchData(mSymbol);
        }else{
            setViews(VIEW_STATE_NO_NETWORK);
        }

    }
    @ViewState
    private void setViews(@ViewState int state){
        if (state == VIEW_STATE_CONNECTED){
            mNoConnectionView.setVisibility(View.GONE);
            chartView.setVisibility(View.VISIBLE);
            averageValue.setVisibility(View.VISIBLE);
            hiLowValue.setVisibility(View.VISIBLE);
            bidPrice.setVisibility(View.VISIBLE);
            mLabel30Average.setVisibility(View.VISIBLE);
            mLabel30HiLo.setVisibility(View.VISIBLE);
            mLabelCurrentPrice.setVisibility(View.VISIBLE);
        }else{
            mNoConnectionView.setVisibility(View.VISIBLE);
            chartView.setVisibility(View.INVISIBLE);
            averageValue.setVisibility(View.INVISIBLE);
            hiLowValue.setVisibility(View.INVISIBLE);
            bidPrice.setVisibility(View.INVISIBLE);
            mLabel30Average.setVisibility(View.INVISIBLE);
            mLabel30HiLo.setVisibility(View.INVISIBLE);
            mLabelCurrentPrice.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mContext = this;
        ButterKnife.bind(this);
        mSymbol = getIntent().getStringExtra("Symbol");
        mSymbolName.setText(mSymbol);
        bidPrice.setText(getIntent().getStringExtra("bid_price"));
        if (Utils.connectedToNetwork(this)){
            fetchData(mSymbol);
        }else{
            Toast.makeText(this, R.string.detail_toast_connect_to_network,Toast.LENGTH_LONG).show();
            mNoConnectionView.setVisibility(View.VISIBLE);
        }

    }
    private void fetchData(String symbol){
        FetchHistoricalDataTask task = new FetchHistoricalDataTask();
        task.execute(symbol);
    }
    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }
    class FetchHistoricalDataTask extends AsyncTask<String, String, ArrayList<Point>> {

        private String LOG_TAG = FetchHistoricalDataTask.class.getSimpleName();
        private OkHttpClient client = new OkHttpClient();
        private float maxVal = 0;
        private float minVal = 100000;

//      https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22YHOO%22%20and%20startDate%20%3D%20%222009-09-11%22%20and%20endDate%20%3D%20%222010-03-10%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=

        @Override
        protected ArrayList<Point> doInBackground(String... params) {

            ArrayList<Point> dataList = new ArrayList<>();
            String stockName = params[0];
            StringBuilder urlStringBuilder = new StringBuilder();
            String[] dates = Utils.getOneAndThirtyDateStrings();
            String startDate = dates[0];
            String endDate = dates[1];
            String urlString;
            String getResponse;

            try {
                // Base URL for the Yahoo query
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = \"" + stockName + "\" and " +
                        "startDate = \"" + startDate + "\" and endDate = \"" + endDate + "\"", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // finalize the URL for the API query.
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                    + "org%2Falltableswithkeys&callback=");

            if (urlStringBuilder != null) {
                urlString = urlStringBuilder.toString();
                try {
                    getResponse = fetchData(urlString);
                    return parseJsonToArray(getResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return dataList;
        }

        String fetchData(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<Point> arrayList) {

            averageValue.setText(String.format("%.2f", thirtyDayAverage));
            hiLowValue.setText(String.format("%.2f", maxVal) + "/" + String.format("%.2f", minVal));
            int maxStep = (int) maxVal + 5;
            int minStep = (int) minVal - 5;
            //Adjust top and bottom range of line chart.  Must be divisible by the step value
            do {
                maxStep++;
            } while (maxStep % 5 != 0);

            do {
                minStep--;
            } while (minStep % 5 != 0);

            LineSet dataSet = new LineSet();

            for (Point point : arrayList) {
                dataSet.addPoint(point);
            }
            Animation animation = new Animation(2000);
            animation.setEasing(new LinearEase());
            dataSet.setDotsColor(getResources().getColor(R.color.material_blue_500));
            dataSet.setDotsStrokeColor(getResources().getColor(R.color.white));
            dataSet.setDotsStrokeThickness(5f);
            dataSet.setDotsRadius(10);
            dataSet.setThickness(6);
            dataSet.setDashed(new float[]{10.0f, 10.0f});
            dataSet.setColor(getResources().getColor(R.color.white));
            dataSet.setFill(getResources().getColor(R.color.material_blue_700));
            chartView.setLabelsColor(getResources().getColor(R.color.white));
            chartView.setAxisBorderValues(minStep, maxStep, 5);
            chartView.setAxisLabelsSpacing(50.0f);
            chartView.setXAxis(false);
            chartView.setYAxis(false);
            chartView.addData(dataSet);
            mProgressBar.setVisibility(View.INVISIBLE);
            chartView.show(animation);
            chartView.animateSet(0, new DashAnimation());
        }

        private ArrayList<Point> parseJsonToArray(String jsonString) {

            ArrayList<Point> pointsList = new ArrayList<>();
            int itemsCount;
            float bidsTotal = 0;
            JSONObject jsonObject;
            JSONObject query;
            JSONArray quote;
            JSONObject results;
            JSONObject dateObject;
            try {
                jsonObject = new JSONObject(jsonString);
                if (jsonObject != null && jsonObject.length() != 0) {
                    query = jsonObject.getJSONObject("query");
                    itemsCount = Integer.parseInt(query.getString("count"));
                    results = query.getJSONObject("results");
                    quote = results.getJSONArray("quote");
                    String date;
                    for (int i = itemsCount - 1; i >= 0; i--) {
                        dateObject = quote.getJSONObject(i);
                        if (i == 0 || i == itemsCount / 2 || i == itemsCount - 1) {
                            date = dateObject.getString("Date");
                            date = date.substring(5);
                        } else {
                            date = "";
                        }

                        String val = dateObject.getString("Close");
                        float value = Float.parseFloat(val);
                        bidsTotal += value;
                        pointsList.add(new Point(date, value));
                        if (value > maxVal) {
                            maxVal = value;
                        }
                        if (value < minVal) {
                            minVal = value;
                        }
                    }
                    float totalEntries = (float) itemsCount;
                    thirtyDayAverage = bidsTotal / totalEntries;
                    return pointsList;
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Parse failed");
            }
            return pointsList;
        }
    }


}
