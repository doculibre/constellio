package com.constellio.app.services.icons;

import com.vaadin.server.FontIcon;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface IconService {
	FontIcon getIconByName(String name);

	FontIcon getIconByCode(int code);

	Stream<FontIcon> stream();

	List<FontIcon> getAllIcons();

	List<FontIcon> getAllIcons(Predicate<FontIcon> filter);

	String getIconLabel(FontIcon icon);

	String getIconLabel(FontIcon icon, Locale locale);

	String getIconName(FontIcon icon);
}
