package com.abc.employeehub.core.services;

import java.util.Map;

public interface WeatherService {

    /**
     * Fetches current weather for the given city from an external API.
     *
     * @param city city name; when blank the configured default city is used
     * @return map with keys: success, city, temperature, windspeed, description
     */
    Map<String, Object> getCurrentWeather(String city);
}
