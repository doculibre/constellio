package com.constellio.app.api.graphql.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GraphqlGson {

	static final Gson GSON = new GsonBuilder().serializeNulls().create();

	public static Map<String, Object> toMap(String value) {
		return GSON.fromJson(value, new TypeToken<HashMap<String, Object>>() {
		}.getType());
	}

	public static void writeJson(HttpServletResponse response, Object result) throws IOException {
		GSON.toJson(result, response.getWriter());
	}

}
