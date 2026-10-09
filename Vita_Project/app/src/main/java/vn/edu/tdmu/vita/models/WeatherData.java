package vn.edu.tdmu.vita.models;

public class WeatherData {
    private float temperature;
    private int humidity;
    private int aqi;
    private String cityName;
    private boolean isOffline;
    private String updatedAt;

    public WeatherData() {}

    public WeatherData(float temperature, int humidity, int aqi, String cityName, boolean isOffline, String updatedAt) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.aqi = aqi;
        this.cityName = cityName;
        this.isOffline = isOffline;
        this.updatedAt = updatedAt;
    }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public int getAqi() { return aqi; }
    public void setAqi(int aqi) { this.aqi = aqi; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public boolean isOffline() { return isOffline; }
    public void setOffline(boolean offline) { isOffline = offline; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
