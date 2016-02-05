package com.constellio.app.ui.i18n;

import java.net.MalformedURLException;
import java.net.URL;
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

	private static Utf8ResourceBundles bundles;

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
		String message;
		//		try {
		//			locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		//		} catch (Exception e) {
		//			locale = getLocale();
		//		}

		ResourceBundle messages = getBundle(locale);

		if (messages.containsKey(key)) {
			message = messages.getString(key);
			if (args != null) {
				try {
					message = MessageFormat.format(message, args);
				} catch (Exception e) {
					// Ignore, return the original message before the formatting attempt
				}
			}
		} else {
			message = key;
		}
		return message;
	}

	public static String $(String key, Map<String, String> args) {
		String message;
		//		try {
		//			locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		//		} catch (Exception e) {
		//			locale = getLocale();
		//		}

		ResourceBundle messages = getBundle(locale);

		if (messages.containsKey(key)) {
			message = messages.getString(key);
			if (args != null) {
				for (String argName : args.keySet()) {
					String argValue = args.get(argName);
					message = message.replace("{" + argName + "}", argValue);
				}
			}
		} else {
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

	private static ResourceBundle getBundle(Locale locale) {

		if (bundles == null) {
			URL[] urls;
			try {
				urls = new URL[] { new FoldersLocator().getI18nFolder().toURI().toURL() };
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			bundles = new Utf8ResourceBundles("i18n", urls);
		}

		return bundles.getBundle(locale);
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

}
