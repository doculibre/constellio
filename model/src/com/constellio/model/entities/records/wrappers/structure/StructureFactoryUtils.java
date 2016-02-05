package com.constellio.model.entities.records.wrappers.structure;

import com.google.gson.*;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;

public class StructureFactoryUtils {

    public static LocalDateJsonSerializerDeserializer newLocalDateJsonSerializerDeserializer(){
        return new LocalDateJsonSerializerDeserializer();
    }


    static class LocalDateJsonSerializerDeserializer implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate>
    {

        private static final String DATE_PATTERN = "yyyy-MM-dd";
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_PATTERN);


        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(formatter.print(src));
        }


        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            return formatter.parseLocalDate(json.getAsString());
        }
    }
}
