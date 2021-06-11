package com.constellio.app.services.icons;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.FontIcon;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.app.ui.i18n.i18n.$;

public class FontAwesomeIconService implements IconService {
	private static FontAwesome[] values;
	private static Map<Integer, FontIcon> codeToIconMap;
	private static Map<String, Integer> nameToCodeMap;
	private static Map<FontIcon, String> iconToNameMap;

	static {
		values = FontAwesome.values();
		codeToIconMap = new HashMap<>();
		nameToCodeMap = new HashMap<>();
		iconToNameMap = new HashMap<>();

		for (FontAwesome icon : values) {
			int code = icon.getCodepoint();
			String name = icon.name();

			codeToIconMap.put(code, icon);
			nameToCodeMap.put(name, code);
			iconToNameMap.put(icon, name);
		}

	}

	private final AppLayerFactory appLayerFactory;
	private final SessionContext sessionContext;

	public FontAwesomeIconService(AppLayerFactory appLayerFactory,
								  SessionContext sessionContext) {
		this.appLayerFactory = appLayerFactory;
		this.sessionContext = sessionContext;
	}

	@Override
	public FontIcon getIconByName(String name) {
		return codeToIconMap.get(nameToCodeMap.get(name));
	}

	@Override
	public FontIcon getIconByCode(int code) {
		return codeToIconMap.get(code);
	}

	@Override
	public Stream<FontIcon> stream() {
		return Arrays.stream(values);
	}

	@Override
	public List<FontIcon> getAllIcons() {
		return stream().collect(Collectors.toList());
	}

	@Override
	public List<FontIcon> getAllIcons(Predicate<FontIcon> filter) {
		return stream().filter(filter).collect(Collectors.toList());
	}

	@Override
	public String getIconLabel(FontIcon icon) {
		return getIconLabel(icon, sessionContext.getCurrentLocale());
	}

	@Override
	public String getIconLabel(FontIcon icon, Locale locale) {
		return $("FontAwesomeIconService.label." + getIconName(icon), locale);
	}

	@Override
	public String getIconName(FontIcon icon) {
		return iconToNameMap.get(icon);
	}
}
