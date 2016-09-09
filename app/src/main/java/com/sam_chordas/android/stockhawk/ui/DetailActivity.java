package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.net.URLEncoder;
import java.util.ArrayList;

public class DetailActivity extends Activity {

    private Context mContext;

    private LineChartView chartView;
    float thirtyDayAverage;
    private TextView averageValue;
    private TextView hiLowValue;
    private TextView mNoConnectionView;

    @Subscribe
    public void onNetworkChangeEvent(NetworkChangeEvent event){

        if (event.show){
            mNoConnectionView.setVisibility(View.GONE);
        }else{
            mNoConnectionView.setVisibility(View.VISIBLE);
        }

//        if (event.show){
//
//            if (mIsConnected == false){
//                mNoConnectionView.setVisibility(View.GONE);
//                Intent serviceIntent = new Intent(this, StockIntentService.class);
//                serviceIntent.putExtra("tag","init");
//                this.startService(serviceIntent);
//                mIsConnected = true;
//            }
//        }else{
//            if(mIsConnected){
//                mNoConnectionView.setVisibility(View.VISIBLE);
//                mIsConnected = false;
//            }
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String symbol = getIntent().getStringExtra("Symbol");
//        Log.e("Sent symbol is: ", symbol);
        chartView = (LineChartView) findViewById(R.id.linechart);
        mContext = this;
        TextView symbolName = (TextView) findViewById(R.id.tv_symbol_name);
        symbolName.setText(symbol);
        TextView bidPrice = (TextView) findViewById(R.id.tv_current_bid_price);
        averageValue = (TextView) findViewById(R.id.tv_thirty_day_average);
        hiLowValue = (TextView) findViewById(R.id.tv_high_low);
        mNoConnectionView = (TextView)findViewById(R.id.tv_detail_no_connection_message);
        bidPrice.setText(getIntent().getStringExtra("bid_price"));
        FetchHistoricalDataTask task = new FetchHistoricalDataTask();
        if (Utils.connectedToNetwork(this)){
            task.execute(symbol);
        }else{
            Toast.makeText(this,"Connect to network for stock details",Toast.LENGTH_LONG).show();
        }


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

        private StringBuilder mStoredSymbols = new StringBuilder();
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
//                Log.e("MY STRING", urlString);
                try {
                    getResponse = fetchData(urlString);
//                    Log.e("Response", getResponse);
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
        protected void onPostExecute(ArrayList<Point> arrayList) {

            averageValue.setText(String.format("%.2f", thirtyDayAverage));
            hiLowValue.setText(String.format("%.2f", maxVal) + "/" + String.format("%.2f", minVal));
            int maxStep = (int) maxVal + 5;
            int minStep = (int) minVal - 5;

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

//            chartView.setStep(20);
            chartView.setAxisBorderValues(minStep, maxStep, 5);
            chartView.setAxisLabelsSpacing(50.0f);

            chartView.setXAxis(false);
            chartView.setYAxis(false);

            chartView.addData(dataSet);

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
