package com.constellio.app.services.icons;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.server.FontIcon;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DefaultIconService implements IconService {
	private final IconService impl;

	public DefaultIconService(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		impl = new FontAwesomeIconService(appLayerFactory, sessionContext);
	}

	@Override
	public FontIcon getIconByName(String name) {
		return impl.getIconByName(name);
	}

	@Override
	public FontIcon getIconByCode(int code) {
		return impl.getIconByCode(code);
	}

	@Override
	public Stream<FontIcon> stream() {
		return impl.stream();
	}

	@Override
	public List<FontIcon> getAllIcons() {
		return impl.getAllIcons();
	}

	@Override
	public List<FontIcon> getAllIcons(Predicate<FontIcon> filter) {
		return impl.getAllIcons(filter);
	}

	@Override
	public String getIconLabel(FontIcon icon) {
		return impl.getIconLabel(icon);
	}

	@Override
	public String getIconLabel(FontIcon icon, Locale locale) {
		return impl.getIconLabel(icon, locale);
	}

	@Override
	public String getIconName(FontIcon icon) {
		return impl.getIconName(icon);
	}
}
