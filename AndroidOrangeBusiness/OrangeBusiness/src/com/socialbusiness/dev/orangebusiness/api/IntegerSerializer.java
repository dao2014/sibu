package com.socialbusiness.dev.orangebusiness.api;

import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * 服务端那边有可能在JSON整型属性那里传个空串""过来(坑爹不?),
 * 因为gson默认配置不能吧空串""解释成0, 所以要写这个序列化适配器
 * Created by liangyaotian on 2014-07-03.
 */
public class IntegerSerializer implements JsonSerializer<Integer>, JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String str = json.getAsString();
        if(TextUtils.isEmpty(str)){
            return 0;
        }else{
            return Integer.parseInt(str);
        }
    }

    @Override
    public JsonElement serialize(Integer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }
}
