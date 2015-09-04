/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
