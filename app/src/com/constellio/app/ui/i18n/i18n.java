package com.constellio.app.ui.i18n;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;

public class i18n {

	private static Locale locale;

	private static List<Utf8ResourceBundles> bundles = new ArrayList<>();

	public static Locale getLocale() {
		return locale;
	}

	public static void setLocale(Locale locale) {
		i18n.locale = locale;
	}

	public static String $(String key) {
		return $(key, (Object) null);
	}

	public static String $(String key, Object... args) {
		String message = null;

		if (key == null) {
			return "";
		}

		for (Utf8ResourceBundles bundle : getBundles()) {
			ResourceBundle messages = bundle.getBundle(locale);

			if (messages.containsKey(key)) {
				message = messages.getString(key);
				if (args != null) {
					try {
						message = MessageFormat.format(message, args);
					} catch (Exception e) {
						// Ignore, return the original message before the formatting attempt
					}
				}
			}
		}

		if (message == null) {
			message = key;
		}

		return message;
	}

	public static String $(String key, Map<String, String> args) {
		String message = null;

		if (key == null) {
			return "";
		}

		for (Utf8ResourceBundles bundle : getBundles()) {
			ResourceBundle messages = bundle.getBundle(locale);

			if (messages.containsKey(key)) {
				message = messages.getString(key);
				if (args != null) {
					for (String argName : args.keySet()) {
						String argValue = args.get(argName);
						message = message.replace("{" + argName + "}", argValue);
					}
				}
			}
		}

		if (message == null) {
			message = key;
		}

		return message;

	}

	public static String $(ValidationErrors errors) {
		StringBuilder sb = new StringBuilder();
		for (ValidationError error : errors.getValidationErrors()) {
			sb.append(" - " + $(error) + "<br/>");
		}
		return sb.toString();
	}

	public static String $(ValidationError error) {
		String key = error.getCode();
		Map<String, String> args = error.getParameters();
		return $(key, args);
	}

	public static String $(Throwable throwable) {
		return $(throwable, (Object) null);
	}

	public static String $(Throwable throwable, Object... args) {
		return $(throwable.getMessage(), args);
	}

	private static List<Utf8ResourceBundles> getBundles() {

		if (bundles.isEmpty()) {
			registerBundle(new FoldersLocator().getI18nFolder(), "i18n");
		}

		return bundles;
	}

	public static void registerBundle(File bundleFolder, String bundleName) {
		File bundleProperties = new File(bundleFolder, bundleName + ".properties");
		if (!bundleProperties.exists()) {
			throw new RuntimeException("No such file '" + bundleProperties.getAbsolutePath() + "'");
		}
		bundles.add(Utf8ResourceBundles.forPropertiesFile(bundleFolder, bundleName));
	}

	public static List<String> getSupportedLanguages() {
		List<String> localeCodes = new ArrayList<>();
		Language[] languages = Language.values();
		for (Language language : languages) {
			if (language != Language.UNKNOWN) {
				String localeCode = language.getCode();
				localeCodes.add(localeCode);
			}
		}
		return localeCodes;
	}

	public static void clearBundles() {
		bundles.clear();
	}
}
