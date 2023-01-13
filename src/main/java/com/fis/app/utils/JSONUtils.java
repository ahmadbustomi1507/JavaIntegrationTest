package com.fis.app.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public final class JSONUtils {
    private static final String ERROR_MESSAGE = "Fail to convert";

    private JSONUtils() {
    }

    public static <T> T convertToObject(String content, Class<T> valueType) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(content, valueType);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return null;
        }
    }

    public static <T> List<T> convertToList(String content, Class<T[]> valueType) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return Arrays.asList(objectMapper.readValue(content, valueType));
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return List.of();
        }
    }

    public static String convertToJson(Object object) {
        String value = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            value = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(ERROR_MESSAGE, e);
        }
        return value;
    }

    public static String convertToPrettyJson(Object object) {
        String value = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            value = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(ERROR_MESSAGE, e);
        }
        return value;
    }

    public static boolean isJSONValid(String jsonInString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
