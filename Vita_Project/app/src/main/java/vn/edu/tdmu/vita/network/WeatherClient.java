package vn.edu.tdmu.vita.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.tdmu.vita.models.WeatherData;
import vn.edu.tdmu.vita.utils.DateTimeUtils;

/**
 * Gọi Open-Meteo trên luồng nền, cache JSON vào SharedPreferences để offline.
 */
public final class WeatherClient {

    private static final String FORECAST_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f"
            + "&current=temperature_2m,relative_humidity_2m&timezone=auto";
    private static final String AQI_URL =
            "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=%f&longitude=%f"
            + "&current=us_aqi,pm2_5&timezone=auto";
    private static final int TIMEOUT_MS = 5000;

    public static final String[] CITY_NAMES = {
            "TP. Hồ Chí Minh", "Bình Dương", "Hà Nội", "Đà Nẵng", "Cần Thơ"};
    public static final double[] CITY_LAT = {10.82, 11.16, 21.02, 16.05, 10.04};
    public static final double[] CITY_LON = {106.63, 106.65, 105.83, 108.20, 105.78};

    public static String selectedCity(Context context) {
        int position = context.getSharedPreferences("vita_home", 0).getInt("city_position", 0);
        return CITY_NAMES[position >= 0 && position < CITY_NAMES.length ? position : 0];
    }

    private static final String PREFS = "vita_weather_cache";
    private static final String KEY_FORECAST = "cache_forecast_json";
    private static final String KEY_AQI = "cache_aqi_json";
    private static final String KEY_CITY = "cache_city";
    private static final String KEY_TIME = "cache_time";

    private static ExecutorService executor;
    private static Handler mainHandler;

    public interface Callback {
        void onSuccess(WeatherData data);
        void onError();
    }

    private WeatherClient() {}

    private static synchronized ExecutorService executor() {
        if (executor == null) executor = Executors.newSingleThreadExecutor();
        return executor;
    }

    private static synchronized Handler mainHandler() {
        if (mainHandler == null) mainHandler = new Handler(Looper.getMainLooper());
        return mainHandler;
    }

    public static WeatherData parseForecastJson(String json, String cityName) {
        if (json == null) return null;
        try {
            JSONObject current = new JSONObject(json).getJSONObject("current");
            WeatherData data = new WeatherData();
            data.setTemperature((float) current.getDouble("temperature_2m"));
            data.setHumidity(current.getInt("relative_humidity_2m"));
            data.setAqi(-1);
            data.setCityName(cityName);
            data.setOffline(false);
            data.setUpdatedAt(DateTimeUtils.getCurrentTimestamp());
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    public static int parseAqiJson(String json) {
        if (json == null) return -1;
        try {
            return new JSONObject(json).getJSONObject("current").getInt("us_aqi");
        } catch (Exception e) {
            return -1;
        }
    }

    public static void fetchWeather(Context context, double lat, double lon,
                                    String cityName, Callback callback) {
        Context appContext = context.getApplicationContext();
        executor().execute(() -> {
            try {
                String forecastJson = httpGet(String.format(java.util.Locale.US, FORECAST_URL, lat, lon));
                String aqiJson = null;
                try {
                    aqiJson = httpGet(String.format(java.util.Locale.US, AQI_URL, lat, lon));
                } catch (Exception ignored) { /* Forecast remains usable without AQI. */ }
                WeatherData data = parseForecastJson(forecastJson, cityName);
                if (data == null) throw new IllegalStateException("bad forecast");
                data.setAqi(parseAqiJson(aqiJson));
                saveCache(appContext, forecastJson, aqiJson, cityName);
                postSuccess(callback, data);
            } catch (Exception e) {
                WeatherData cached = loadCachedWeather(appContext, cityName);
                if (cached != null) {
                    postSuccess(callback, cached);
                } else {
                    mainHandler().post(callback::onError);
                }
            }
        });
    }

    private static String httpGet(String urlString) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException("HTTP " + conn.getResponseCode());
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static void saveCache(Context context, String forecastJson, String aqiJson, String city) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_FORECAST + city, forecastJson)
                .putString(KEY_AQI + city, aqiJson == null ? "" : aqiJson)
                .putString(KEY_CITY + city, city)
                .putString(KEY_TIME + city, DateTimeUtils.getCurrentTimestamp())
                .apply();
    }

    public static WeatherData loadCachedWeather(Context context, String city) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String suffix = prefs.contains(KEY_FORECAST + city) ? city : "";
        return parseCachedWeather(prefs.getString(KEY_FORECAST + suffix, null),
                prefs.getString(KEY_AQI + suffix, null), prefs.getString(KEY_CITY + suffix, ""),
                city, prefs.getString(KEY_TIME + suffix, ""));
    }

    public static WeatherData parseCachedWeather(String forecastJson, String aqiJson,
                                                 String storedCity, String requestedCity, String timestamp) {
        if (requestedCity == null || !requestedCity.equals(storedCity)) return null;
        WeatherData data = parseForecastJson(forecastJson, storedCity);
        if (data == null) return null;
        data.setAqi(parseAqiJson(aqiJson));
        data.setOffline(true);
        data.setUpdatedAt(timestamp);
        return data;
    }

    private static void postSuccess(Callback callback, WeatherData data) {
        mainHandler().post(() -> callback.onSuccess(data));
    }
}
