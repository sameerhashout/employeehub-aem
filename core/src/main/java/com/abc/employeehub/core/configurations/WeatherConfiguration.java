package com.abc.employeehub.core.configurations;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Employee Hub - Weather Configuration",
        description = "External weather API settings for the Weather component"
)
public @interface WeatherConfiguration {

    @AttributeDefinition(name = "Geocoding API URL", description = "Endpoint used to resolve a city name to coordinates")
    String geocodingApiUrl() default "https://geocoding-api.open-meteo.com/v1/search";

    @AttributeDefinition(name = "Forecast API URL", description = "Endpoint used to fetch the current weather")
    String forecastApiUrl() default "https://api.open-meteo.com/v1/forecast";

    @AttributeDefinition(name = "Default City", description = "City used when no city is supplied by the request")
    String defaultCity() default "Bengaluru";

    @AttributeDefinition(name = "Connection Timeout (ms)", description = "HTTP connect/read timeout in milliseconds")
    int timeoutMillis() default 5000;
}
