package com.socialbusiness.dev.orangebusiness.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created by liangyaotian on 2014-09-23.
 */
public class DateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        long second = json.getAsInt();
        long milliseconds = second * 1000;
        Date date = new Date(milliseconds);
        return date;
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        long seconds = (src.getTime() / 1000);
        return new JsonPrimitive(seconds);
    }
}
