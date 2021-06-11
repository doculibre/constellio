package com.constellio.app.conf;

import com.constellio.model.entities.Language;
import org.joda.time.Duration;

import java.io.File;
import java.util.List;

public interface AppLayerConfiguration {

	void validate();

	File getTempFolder();

	File getPluginsFolder();

	File getPluginsManagementOnStartupFile();

	File getSetupProperties();

	File getSignatureKey();

	boolean isFastMigrationsEnabled();

	List<Language> getSupportedLanguages();

	List<String> getSupportedLanguageCodes();

	public Boolean getBoolean(String key, boolean defaultValue);

	public Duration getDuration(String key, Duration defaultDuration);

	public Object getEnum(String key, Enum<?> defaultValue);

	public File getFile(String key, File defaultValue);

	public int getInt(String key, int defaultValue);

	public long getLong(String key, long defaultValue);

	public String getString(String key, String defaultValue);

}
