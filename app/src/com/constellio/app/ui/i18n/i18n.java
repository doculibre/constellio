package com.constellio.app.ui.i18n;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;

public class i18n {

	private static Logger LOGGER = LoggerFactory.getLogger(i18n.class);

	// TODO Use a languageCode->Locale Map instead?
	private static Locale locale;

	private static Utf8ResourceBundles defaultBundle = null;

	private static List<Utf8ResourceBundles> registeredBundles = new ArrayList<>();

	public static Locale getLocale() {
		try {
			return ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		} catch (Throwable e) {
			//LOGGER.warn("error when trying to get session locale", e);
		}
		return locale;
	}

	public static void setLocale(Locale locale) {
		i18n.locale = locale;
		try {
			ConstellioUI.getCurrentSessionContext().setCurrentLocale(locale);
		} catch (Throwable e) {
			//LOGGER.warn("error when trying to set session locale", e);
		}
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

			ResourceBundle messages = bundle.getBundle(getLocale());

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

	public static String $(String key, Locale locale) {
		String message = null;

		if (key == null) {
			return "";
		}
		for (Utf8ResourceBundles bundle : getBundles()) {

			ResourceBundle messages = bundle.getBundle(locale);

			if (messages.containsKey(key)) {
				message = messages.getString(key);
			}
		}

		if (message == null) {
			message = key;
		}

		return message;
	}

	public static String $(String key, Map<String, Object> args) {
		String message = null;

		if (key == null) {
			return "";
		}
		for (Utf8ResourceBundles bundle : getBundles()) {
			ResourceBundle messages = bundle.getBundle(getLocale());
			if (messages.containsKey(key)) {
				message = messages.getString(key);
				if (args.get("prefix") != null) {
					message = args.get("prefix") + message;
				}
				if (args.get("suffix") != null) {
					message = message + args.get("suffix");
				}
				if (args != null) {
					for (String argName : args.keySet()) {
						Object argValue = args.get(argName);
						if (argValue instanceof String) {
							message = message.replace("{" + argName + "}", (String) argValue);
						} else if (argValue instanceof Number) {	
							message = message.replace("{" + argName + "}", "" + argValue);
						} else if (argValue instanceof Map) {
							/*	TODO Manage Map value here:
								- Must fetch the entry for the current language.
							 */
							Map<String, String> labelsMap = (Map<String, String>) argValue;
							String language = getLocale().getLanguage();
							message = message.replace("{" + argName + "}", labelsMap.get(language));
						} else if (argValue instanceof EnumWithSmallCode) {
							EnumWithSmallCode enumWithSmallCode = (EnumWithSmallCode) argValue;
							message = message.replace("{" + argName + "}",
									$(enumWithSmallCode.getClass().getSimpleName() + "." + enumWithSmallCode.getCode()));
						} else if (argValue instanceof Boolean) {
							message = message.replace("{" + argName + "}",
									$(argValue.toString()));
						} else if (argValue instanceof Enum) {
							Enum anEnum = (Enum) argValue;
							message = message.replace("{" + argName + "}",
									$(anEnum.getClass().getSimpleName() + "." + anEnum.name()));
						} else {
							message = message.replace("{" + argName + "}", "");
						}
					}
				}
			}
		}

		if (message != null && message.toLowerCase().startsWith("jexl:")) {
			try {
				message = callJexlScript(message.substring(5), args);
			} catch (Exception e) {
				LOGGER.warn("Script failure '" + message.substring(5) + "'", e);
				message = null;
			}
		}

		if (message == null) {
			if (args == null || args.isEmpty()) {
				message = key;
			} else {
				message = key + " " + args.toString();
			}
		}

		return message;

	}

	private static String callJexlScript(String expression, Map<String, Object> args)
			throws Exception {

		JexlEngine jexl = new JexlBuilder().create();
		JexlScript jexlScript = jexl.createScript(expression);

		JexlContext jc = prepareJexlContext(args);

		Object calculatedValue = jexlScript.execute(jc);
		return "null".equals(calculatedValue) ? null : (String) calculatedValue;

	}

	private static JexlContext prepareJexlContext(Map<String, Object> args) {
		JexlContext jc = new MapContext();

		for (Map.Entry<String, Object> entry : args.entrySet()) {
			jc.set(entry.getKey(), entry.getValue());
		}

		jc.set("i18n", new Map<String, String>() {
			@Override
			public int size() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isEmpty() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsKey(Object key) {
				return get(key) != null;
			}

			@Override
			public boolean containsValue(Object value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String get(Object key) {
				return $((String) key);
			}

			@Override
			public String put(String key, String value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String remove(Object key) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void putAll(Map m) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Set<String> keySet() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Collection<String> values() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Set<Entry<String, String>> entrySet() {
				throw new UnsupportedOperationException();
			}
		});

		return jc;
	}

	public static String $(ValidationErrors errors) {
		StringBuilder sb = new StringBuilder();
		for (ValidationError error : errors.getValidationErrors()) {
			sb.append(" - " + $(error) + "<br/>");
		}
		return sb.toString();
	}

	public static List<String> asListOfMessages(ValidationErrors errors) {
		List<String> messages = new ArrayList<>();
		for (ValidationError error : errors.getValidationErrors()) {
			messages.add($(error));
		}

		for (ValidationError error : errors.getValidationWarnings()) {
			messages.add($(error));
		}
		return messages;
	}

	public static List<String> asListOfMessages(List<ValidationError> errors) {
		List<String> messages = new ArrayList<>();

		for (ValidationError error : errors) {
			messages.add($(error));
		}
		return messages;
	}

	public static List<String> asListOfMessages(ValidationErrors errors, Object... params) {
		List<String> messages = new ArrayList<>();
		for (ValidationError error : errors.getValidationErrors()) {
			messages.add($(error.getCode(), params));
		}
		return messages;
	}

	public static String $(ValidationError error) {
		String key = error.getCode();
		Map<String, Object> args = error.getParameters();
		return $(key, args);
	}

	public static String $(Throwable throwable) {
		return $(throwable, (Object) null);
	}

	public static String $(Throwable throwable, Object... args) {
		return $(throwable.getMessage(), args);
	}

	private static List<Utf8ResourceBundles> getBundles() {
		List<Utf8ResourceBundles> bundles = new ArrayList<>();
		bundles.add(getDefaultBundle());
		bundles.addAll(registeredBundles);
		return bundles;
	}

	public static Utf8ResourceBundles getDefaultBundle() {
		if (defaultBundle == null) {
			defaultBundle = Utf8ResourceBundles.forPropertiesFile(new FoldersLocator().getI18nFolder(), "i18n");
		}
		return defaultBundle;
	}

	public static void registerBundle(File bundleFolder, String bundleName) {
		File bundleProperties = new File(bundleFolder, bundleName + ".properties");
		if (!bundleProperties.exists()) {
			throw new RuntimeException("No such file '" + bundleProperties.getAbsolutePath() + "'");
		}
		registeredBundles.add(Utf8ResourceBundles.forPropertiesFile(bundleFolder, bundleName));
	}

	public static void registerBundle(Utf8ResourceBundles bundle) {
		if (bundle != null) {
			registeredBundles.add(bundle);
		}
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
		registeredBundles.clear();
	}

	public static Language getLanguage() {
		Locale loc = getLocale();
		Language[] languages = Language.values();
		for (Language language : languages) {
			if (loc.getLanguage().equals(language.getCode())) {
				return language;
			}
		}
		throw new RuntimeException(
				"Current locale" + loc + " does not correspond to any language" + StringUtils.join(languages, ","));
	}

}
