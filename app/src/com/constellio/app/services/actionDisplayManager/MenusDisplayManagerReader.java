package com.constellio.app.services.actionDisplayManager;

import com.constellio.app.services.actionDisplayManager.MenuDisplayItem.Type;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenusDisplayManagerReader {

	private Document document;

	public MenusDisplayManagerReader(Document document) {
		this.document = document;
	}

	public MenuDisplayListBySchemaType getActionDisplayListBySchemaType() {

		MenuDisplayListBySchemaType menuDisplayListBySchemaType = new MenuDisplayListBySchemaType();

		Element rootElement = document.getRootElement();
		for (Element currentChildren : rootElement.getChildren()) {
			String schemaType = currentChildren.getAttributeValue(IMenuDisplayIO.SCHEMA_TYPE);
			menuDisplayListBySchemaType.setListForSchemaType(schemaType, getActionDisplayList(currentChildren));
		}

		return menuDisplayListBySchemaType;
	}

	public MenuDisplayList getActionDisplayList(Element schemaTypeElement) {
		List<MenuDisplayItem> actionDisplays = new ArrayList<>();

		List<Element> schemaTypeActionDisplay = schemaTypeElement.getChildren();
		for (Element actionDisplayElement : schemaTypeActionDisplay) {
			actionDisplays.add(getActionDisplayFromElement(actionDisplayElement));
		}

		return new MenuDisplayList(actionDisplays);
	}

	private MenuDisplayItem getActionDisplayFromElement(Element element) {

		Map<Locale, String> displayName = new HashMap<>();

		for (Attribute attribute : element.getAttributes()) {
			if (attribute.getName().startsWith(IMenuDisplayIO.LABELS + "_")) {
				Locale language = Locale.forLanguageTag(attribute.getName().replace(IMenuDisplayIO.LABELS + "_", ""));
				displayName.put(language, attribute.getValue());
			}
		}

		String type = element.getAttributeValue(IMenuDisplayIO.TYPE);
		String parentCode = element.getAttributeValue(IMenuDisplayIO.PARENT_CODE);
		String code = element.getAttributeValue(IMenuDisplayIO.CODE);
		String activeStr = element.getAttributeValue(IMenuDisplayIO.ACTIVE);
		String i18nKey = element.getAttributeValue(IMenuDisplayIO.I18N_KEY);
		String icon = element.getAttributeValue(IMenuDisplayIO.ICON);
		String alwaysActiveStr = element.getAttributeValue(IMenuDisplayIO.ALWAYS_ACTIVE);

		boolean alwaysActive = "true".equalsIgnoreCase(alwaysActiveStr);
		boolean active = "true".equalsIgnoreCase(activeStr);

		Type typeAsEnum = (Type) EnumWithSmallCodeUtils.toEnumWithSmallCode(Type.class, type);

		MenuDisplayItem actionDisplay;

		if (typeAsEnum == Type.MENU) {
			actionDisplay = new MenuDisplayItem(code, icon, i18nKey, active, parentCode, alwaysActive);
		} else if (typeAsEnum == Type.CONTAINER) {
			actionDisplay = new MenuDisplayContainer(code, displayName, icon, active, alwaysActive);
		} else {
			throw new IllegalStateException("Type not supported" + typeAsEnum);
		}

		return actionDisplay;
	}


}
