package com.jhzhao.alibaba.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class OpenMeteoService {

    private static final String BASE_URL = "https://api.open-meteo.com/v1";

    private final RestClient restClient;

    public OpenMeteoService() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "OpenMeteoClient/1.0")
                .build();
    }

    // 获取未来7天天气预报（包含当前时刻 + 每日概要）
    @Tool(description = "获取指定经纬度的天气预报")
    public String getWeatherForecastByLocation(
            @ToolParam(description = "纬度") double latitude,
            @ToolParam(description = "经度") double longitude
    ) {
        // 常用参数组合 - 可根据需要增减
        String uri = "/forecast" +
                "?latitude={lat}" +
                "&longitude={lon}" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m,is_day" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,precipitation_sum,wind_speed_10m_max" +
                "&timezone=auto" +           // 强烈推荐使用 auto
                "&forecast_days=7" +
                "&temperature_unit=celsius" +
                "&wind_speed_unit=kmh" +
                "&precipitation_unit=mm";

        WeatherData data = restClient.get()
                .uri(uri, latitude, longitude)
                .retrieve()
                .body(WeatherData.class);

        return formatWeatherInfo(data);
    }

    private String formatWeatherInfo(WeatherData data) {
        StringBuilder sb = new StringBuilder();

        // 当前天气
        sb.append("当前位置: ").append(String.format("%.2f", data.latitude())).append(", ")
                .append(String.format("%.2f", data.longitude())).append("\n");
        sb.append("时区: ").append(data.timezone()).append("\n\n");

        CurrentWeather now = data.current();
        if (now != null) {
            sb.append("当前天气 (").append(now.time()).append("):\n");
            sb.append("  温度: ").append(now.temperature2m()).append(data.currentUnits().temperature2m()).append("\n");
            sb.append("  体感: ").append(now.apparentTemperature()).append(data.currentUnits().apparentTemperature()).append("\n");
            sb.append("  相对湿度: ").append(now.relativeHumidity2m()).append("%\n");
            sb.append("  降水: ").append(now.precipitation()).append(" mm\n");
            sb.append("  风速: ").append(now.windSpeed10m()).append(" km/h\n");
            sb.append("  天气代码: ").append(now.weatherCode()).append("\n\n");
        }

        // 未来7天预报
        DailyForecast daily = data.daily();
        if (daily != null && daily.time() != null) {
            sb.append("未来7天预报:\n");
            List<String> times = daily.time();
            List<Double> tMax = daily.temperature2mMax();
            List<Double> tMin = daily.temperature2mMin();
            List<Double> precip = daily.precipitationSum();
            List<Integer> wmoCode = daily.weatherCode();

            for (int i = 0; i < times.size(); i++) {
                String date = LocalDate.parse(times.get(i)).format(DateTimeFormatter.ofPattern("MM-dd (E)"));
                sb.append(date).append(": ")
                        .append(String.format("%.1f", tMin.get(i))).append(" ~ ")
                        .append(String.format("%.1f", tMax.get(i))).append(" °C  ")
                        .append("降水 ").append(precip.get(i)).append(" mm  ")
                        .append("WMO:").append(wmoCode.get(i)).append("\n");
            }
        }

        return sb.toString();
    }

    // 空气质量 - Open-Meteo 免费版空气质量数据非常有限（主要是欧洲），这里保留模拟版本
    @Tool(description = "获取指定位置的空气质量信息（模拟数据）")
    public String getAirQuality(
            @ToolParam(description = "纬度") double latitude,
            @ToolParam(description = "经度") double longitude
    ) {
        // 真实项目建议调用 https://air-quality.api.open-meteo.com/v1/air-quality （但需额外参数）
        // 这里仅演示模拟
        String[] levels = {"优", "良", "轻度污染", "中度污染", "重度污染", "严重污染"};
        int idx = (int) (Math.random() * levels.length);

        StringBuilder sb = new StringBuilder();
        sb.append("空气质量（模拟数据）\n");
        sb.append("AQI: ").append( (int)(Math.random()*150 + 10) ).append("\n");
        sb.append("等级: ").append(levels[idx]).append("\n");
        sb.append("位置: ").append(latitude).append(", ").append(longitude).append("\n");
        return sb.toString();
    }

    // 核心响应结构（根据2025-2026官方文档调整过）
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherData(
            Double latitude,
            Double longitude,
            String timezone,
            CurrentWeather current,
            CurrentUnits currentUnits,
            DailyForecast daily,
            DailyUnits dailyUnits
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentWeather(
            String time,
            Double temperature2m,
            Double relativeHumidity2m,
            Double apparentTemperature,
            Double precipitation,
            Integer weatherCode,
            Double windSpeed10m,
            Double windDirection10m,
            Integer isDay
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentUnits(
            @JsonProperty("temperature_2m") String temperature2m,
            @JsonProperty("relative_humidity_2m") String relativeHumidity2m,
            @JsonProperty("apparent_temperature") String apparentTemperature,
            String precipitation,
            @JsonProperty("weather_code") String weatherCode,
            @JsonProperty("wind_speed_10m") String windSpeed10m,
            @JsonProperty("wind_direction_10m") String windDirection10m,
            String isDay
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DailyForecast(
            List<String> time,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("temperature_2m_max") List<Double> temperature2mMax,
            @JsonProperty("temperature_2m_min") List<Double> temperature2mMin,
            @JsonProperty("apparent_temperature_max") List<Double> apparentTemperatureMax,
            @JsonProperty("apparent_temperature_min") List<Double> apparentTemperatureMin,
            @JsonProperty("precipitation_sum") List<Double> precipitationSum,
            @JsonProperty("wind_speed_10m_max") List<Double> windSpeed10mMax
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DailyUnits(
            @JsonProperty("weather_code") String weatherCode,
            @JsonProperty("temperature_2m_max") String temperature2mMax,
            @JsonProperty("temperature_2m_min") String temperature2mMin,
            @JsonProperty("apparent_temperature_max") String apparentTemperatureMax,
            @JsonProperty("apparent_temperature_min") String apparentTemperatureMin,
            @JsonProperty("precipitation_sum") String precipitationSum,
            @JsonProperty("wind_speed_10m_max") String windSpeed10mMax
    ) {}

}
