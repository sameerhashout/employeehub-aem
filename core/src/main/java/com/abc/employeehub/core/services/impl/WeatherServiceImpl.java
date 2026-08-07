package com.abc.employeehub.core.services.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abc.employeehub.core.configurations.WeatherConfiguration;
import com.abc.employeehub.core.services.WeatherService;

@Component(service = WeatherService.class, immediate = true)
@Designate(ocd = WeatherConfiguration.class)
public class WeatherServiceImpl implements WeatherService {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherServiceImpl.class);

    private String geocodingApiUrl;
    private String forecastApiUrl;
    private String defaultCity;
    private int timeoutMillis;

    @Activate
    @Modified
    protected void activate(WeatherConfiguration config) {
        this.geocodingApiUrl = config.geocodingApiUrl();
        this.forecastApiUrl = config.forecastApiUrl();
        this.defaultCity = config.defaultCity();
        this.timeoutMillis = config.timeoutMillis();
    }

    @Override
    public Map<String, Object> getCurrentWeather(String city) {
        Map<String, Object> result = new HashMap<>();
        String targetCity = (city == null || city.trim().isEmpty()) ? defaultCity : city.trim();

        try {
            JSONObject geo = fetchJson(geocodingApiUrl
                    + "?name=" + URLEncoder.encode(targetCity, StandardCharsets.UTF_8.name())
                    + "&count=1&language=en&format=json");
            JSONArray results = geo.optJSONArray("results");
            if (results == null || results.length() == 0) {
                result.put("success", false);
                result.put("message", "City not found: " + targetCity);
                return result;
            }
            JSONObject place = results.getJSONObject(0);
            double lat = place.getDouble("latitude");
            double lon = place.getDouble("longitude");
            String resolvedName = place.optString("name", targetCity);
            String country = place.optString("country", "");

            JSONObject forecast = fetchJson(forecastApiUrl
                    + "?latitude=" + lat + "&longitude=" + lon + "&current_weather=true");
            JSONObject current = forecast.getJSONObject("current_weather");

            result.put("success", true);
            result.put("city", country.isEmpty() ? resolvedName : resolvedName + ", " + country);
            result.put("temperature", current.getDouble("temperature"));
            result.put("windspeed", current.getDouble("windspeed"));
            result.put("description", describe(current.optInt("weathercode", -1)));
        } catch (Exception e) {
            LOG.error("Failed to fetch weather for {}", targetCity, e);
            result.put("success", false);
            result.put("message", "Unable to fetch weather right now");
        }
        return result;
    }

    private JSONObject fetchJson(String urlString) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeoutMillis);
            conn.setReadTimeout(timeoutMillis);
            conn.setRequestProperty("Accept", "application/json");

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return new JSONObject(sb.toString());
        } finally {
            conn.disconnect();
        }
    }

    private String describe(int code) {
        switch (code) {
            case 0: return "Clear sky";
            case 1: case 2: case 3: return "Partly cloudy";
            case 45: case 48: return "Fog";
            case 51: case 53: case 55: return "Drizzle";
            case 61: case 63: case 65: return "Rain";
            case 71: case 73: case 75: return "Snow";
            case 80: case 81: case 82: return "Rain showers";
            case 95: case 96: case 99: return "Thunderstorm";
            default: return "Unknown";
        }
    }
}
