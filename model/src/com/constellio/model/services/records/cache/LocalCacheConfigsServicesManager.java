package com.constellio.model.services.records.cache;

import com.constellio.model.services.records.cache.LocalCacheConfigs.LocalCacheConfigsBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

public class LocalCacheConfigsServicesManager {

	File file;

	LocalCacheConfigs cached;

	public LocalCacheConfigsServicesManager(File file) {
		this.file = file;
	}


	private LocalCacheConfigs read() {

		String jsonContent = null;
		if (file.exists()) {
			try {
				jsonContent = FileUtils.readFileToString(file, "UTF-8");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return parseFromJSON(jsonContent);
	}

	public LocalCacheConfigs get() {
		if (cached == null) {
			cached = read();
		}
		return cached;
	}

	public synchronized void clear() {
		try {
			FileUtils.forceDelete(file);
			cached = null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void alter(Consumer<LocalCacheConfigsBuilder> consumer) {
		LocalCacheConfigs configs = get();
		LocalCacheConfigsBuilder builder = new LocalCacheConfigsBuilder(configs);

		consumer.accept(builder);

		LocalCacheConfigs newVersion = new LocalCacheConfigs(builder.typesConfigs);

		write(newVersion);
		cached = newVersion;
	}


	private void write(LocalCacheConfigs cacheConfigs) {

		String jsonContent = writeToJSON(cacheConfigs);
		try {
			FileUtils.write(file, jsonContent, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private LocalCacheConfigs parseFromJSON(String json) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		if (StringUtils.isNotBlank(json)) {
			TypeToken<LocalCacheConfigs> listTypeToken = new TypeToken<LocalCacheConfigs>() {
			};

			return gson.fromJson(json, listTypeToken.getType());
		} else {
			return new LocalCacheConfigs(new HashMap<>());
		}
	}

	private LocalCacheConfigs buildFull(String json) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		if (StringUtils.isNotBlank(json)) {
			TypeToken<LocalCacheConfigs> listTypeToken = new TypeToken<LocalCacheConfigs>() {
			};

			return gson.fromJson(json, listTypeToken.getType());
		} else {
			return null;
		}
	}

	private String writeToJSON(LocalCacheConfigs cacheConfigs) {

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		return gson.toJson(cacheConfigs);

	}

}
