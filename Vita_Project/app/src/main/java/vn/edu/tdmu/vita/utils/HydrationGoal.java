package vn.edu.tdmu.vita.utils;

import android.content.Context;
import vn.edu.tdmu.vita.models.WeatherData;
import vn.edu.tdmu.vita.network.WeatherClient;

public final class HydrationGoal {
    private HydrationGoal() {}

    public static int forDay(int baseGoal, WeatherData weather, String city, String date, boolean weatherEnabled) {
        int base = baseGoal > 0 ? baseGoal : 2000;
        boolean hotToday = weatherEnabled && weather != null && city.equals(weather.getCityName())
                && weather.getUpdatedAt() != null && weather.getUpdatedAt().startsWith(date + " ")
                && weather.getTemperature() >= 33f && Float.isFinite(weather.getTemperature());
        return base + (hotToday ? 300 : 0);
    }

    public static int current(Context context, vn.edu.tdmu.vita.models.User user) {
        String city = WeatherClient.selectedCity(context);
        return forDay(user != null ? user.getWaterGoalMl() : 2000, WeatherClient.loadCachedWeather(context, city), city, DateTimeUtils.getTodayDate(), user != null && user.isWeatherAdjustmentEnabled());
    }
}
