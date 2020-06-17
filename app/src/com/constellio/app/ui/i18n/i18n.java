package com.constellio.app.ui.i18n;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.data.services.tenant.TenantLocal;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class i18n {

	private static Logger LOGGER = LoggerFactory.getLogger(i18n.class);

	private static TenantLocal<Locale> locale = new TenantLocal<Locale>();

	private static List<Utf8ResourceBundles> defaultBundles = null;

	private static List<Utf8ResourceBundles> registeredBundles = new ArrayList<>();


	public static Locale getLocale() {
		try {
			ConstellioUI constellioUI = ConstellioUI.getCurrent();
			SessionContext context = constellioUI == null ? null : constellioUI.getSessionContext();
			if (context != null) {
				return context.getCurrentLocale();
			}
		} catch (Throwable e) {
			LOGGER.warn("error when trying to get session locale", e);
		}
		return locale.get();
	}

	public static void setLocale(Locale locale) {
		i18n.locale.set(locale);
		try {
			ConstellioUI constellioUI = ConstellioUI.getCurrent();
			SessionContext context = constellioUI == null ? null : constellioUI.getSessionContext();
			if (context != null) {
				context.setCurrentLocale(locale);
			}
		} catch (Throwable e) {
			LOGGER.warn("error when trying to set session locale", e);
		}
	}

	public static String $(String key) {
		return $(key, null, (Object) null);
	}

	public static String $(String key, Object... args) {
		return $(key, null, args);
	}

	public static String $(String key, Map<String, Object> args) {
		return $(key, null, args);
	}

	public static String $(String key, Locale locale) {
		return $(key, locale, (Object) null);
	}

	public static String $(String key, Locale locale, Object... args) {
		String message = null;

		if (key == null) {
			return "";
		}
		if (locale == null) {
			locale = getLocale();
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
			if (Toggle.RETURN_EMPTY_VALUE_WHEN_KEY_IS_MISSING.isEnabled()) {
				message = "";
			} else {
				message = key;
			}
		}

		return message;
	}

	public static String $(String key, Locale locale, Map<String, Object> args) {
		String message = null;

		if (key == null) {
			return "";
		}

		if (locale == null) {
			locale = getLocale();
		}

		String language = locale.getLanguage();
		for (Utf8ResourceBundles bundle : getBundles()) {
			ResourceBundle messages = bundle.getBundle(locale);
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
							Map<Object, String> labelsMap = (Map<Object, String>) argValue;

							String label = null;
							for (Map.Entry<Object, String> entry : labelsMap.entrySet()) {
								if (entry.getKey().equals(language) || entry.getKey().equals(Language.withCode(language))) {
									label = entry.getValue();
								}
							}

							message = message.replace("{" + argName + "}", label);
						} else if (argValue instanceof EnumWithSmallCode) {
							EnumWithSmallCode enumWithSmallCode = (EnumWithSmallCode) argValue;
							message = message.replace("{" + argName + "}",
									$(enumWithSmallCode.getClass().getSimpleName() + "." + enumWithSmallCode.getCode(), locale));
						} else if (argValue instanceof Boolean) {
							message = message.replace("{" + argName + "}",
									$(argValue.toString(), locale));
						} else if (argValue instanceof Enum) {
							Enum anEnum = (Enum) argValue;
							message = message.replace("{" + argName + "}",
									$(anEnum.getClass().getSimpleName() + "." + anEnum.name(), locale));
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

		return message.replace(" ''", " '''").replace("''", "'");

	}

	public static String $(EnumWithSmallCode enumWithSmallCode) {
		return $(enumWithSmallCode.getClass().getSimpleName() + "." + enumWithSmallCode.getCode());
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
			if (error != null) {
				sb.append(" - " + $(error) + "<br/>" + "\n");
			}
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
		return $(error, null);
	}

	public static String $(ValidationError error, Locale locale) {
		String key = error.getCode();
		Map<String, Object> args = error.getParameters();
		return $(key, locale, args);
	}

	public static String $(Throwable throwable) {
		if (throwable instanceof ValidationException) {
			return $(((ValidationException) throwable).getValidationErrors());
		} else {
			return $(throwable, (Object) null);
		}
	}

	public static String $(Throwable throwable, Object... args) {
		return $(throwable.getMessage(), args);
	}

	private static List<Utf8ResourceBundles> getBundles() {
		List<Utf8ResourceBundles> bundles = new ArrayList<>();
		for (Utf8ResourceBundles bundle : getDefaultBundle()) {
			bundles.add(bundle);
			bundles.addAll(registeredBundles);
		}
		return bundles;
	}

	public static List<Utf8ResourceBundles> getDefaultBundle() {
		List<Utf8ResourceBundles> bundles = new ArrayList<>();
		if (defaultBundles == null) {
			FoldersLocator foldersLocator = new FoldersLocator();
			File i18nFolder = new File(foldersLocator.getI18nFolder().getAbsolutePath());

			for (File propertyFile : FileUtils.listFiles(i18nFolder, new String[]{"properties"}, true)) {

				if (!propertyFile.getName().contains("_")) {
					//String RessourceName = StringUtils.substringBefore(propertyFile.getAbsolutePath().replace(file.getAbsolutePath(), ""), ".");
					Utf8ResourceBundles defaultBundle = Utf8ResourceBundles.forPropertiesFile(propertyFile.getParentFile(), propertyFile.getName().replace(".properties", ""));
					bundles.add(defaultBundle);
				}
			}
			defaultBundles = bundles;
			//            File arr[] = file.listFiles();
			//            for (File name : arr) {
			//                if (name.isDirectory()) {
			//
			//                    Collection<File> files = FileUtils.listFiles(name, new WildcardFileFilter("*.properties"), null);
			//
			//                    for (File filename : files) {
			//                        if (!filename.getName().contains("_")) {
			//                            String RessourceName = StringUtils.substringBefore(filename.getName(), ".");
			//                            defaultBundle = Utf8ResourceBundles.forPropertiesFile(name, RessourceName);
			//                            bundles.add(defaultBundle);
			//
			//
			//                        }
			//                    }
			//                } else {
			//                    String RessourceName = StringUtils.substringBefore(name.getName(), ".");
			//                    if (!name.getName().contains("_")) {
			//                        defaultBundle = Utf8ResourceBundles.forPropertiesFile(new FoldersLocator().getI18nFolder(), RessourceName);
			//                        bundles.add(defaultBundle);
			//                    }
			//                }
			//            }
		}


		return defaultBundles;
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

	public static boolean isRightToLeft() {
		Language language = getLanguage();
		return Language.Arabic.equals(language);// || Language.French.equals(language);
	}

}
