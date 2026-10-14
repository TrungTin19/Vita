package vn.edu.tdmu.vita.network;

import org.junit.Test;
import static org.junit.Assert.*;

import vn.edu.tdmu.vita.models.WeatherData;

public class WeatherClientTest {

    private static final String FORECAST_JSON =
            "{\"latitude\":10.82,\"longitude\":106.63,"
            + "\"current\":{\"time\":\"2026-10-14T10:00\","
            + "\"temperature_2m\":34.5,\"relative_humidity_2m\":70}}";

    private static final String AQI_JSON =
            "{\"latitude\":10.82,\"longitude\":106.63,"
            + "\"current\":{\"time\":\"2026-10-14T10:00\","
            + "\"us_aqi\":120,\"pm2_5\":45.0}}";

    @Test
    public void parseForecast_extractsTempAndHumidity() {
        WeatherData data = WeatherClient.parseForecastJson(FORECAST_JSON, "TP. Hồ Chí Minh");
        assertNotNull(data);
        assertEquals(34.5f, data.getTemperature(), 0.01f);
        assertEquals(70, data.getHumidity());
        assertEquals("TP. Hồ Chí Minh", data.getCityName());
    }

    @Test
    public void parseAqi_extractsUsAqi() {
        assertEquals(120, WeatherClient.parseAqiJson(AQI_JSON));
    }

    @Test
    public void parseInvalid_returnsNullAndMinusOne() {
        assertNull(WeatherClient.parseForecastJson("not-json", "X"));
        assertNull(WeatherClient.parseForecastJson(null, "X"));
        assertEquals(-1, WeatherClient.parseAqiJson("not-json"));
        assertEquals(-1, WeatherClient.parseAqiJson(null));
    }
}
