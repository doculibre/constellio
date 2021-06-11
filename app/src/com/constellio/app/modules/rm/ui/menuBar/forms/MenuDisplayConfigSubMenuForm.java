package com.constellio.app.modules.rm.ui.menuBar.forms;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu.MenuDisplayConfigSubMenu;
import com.constellio.app.modules.rm.ui.menuBar.forms.MenuDisplayConfigSubMenuForm.EditableMenuDisplayConfigSubMenu;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.vaadin.data.Item;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.FontIcon;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import org.h2.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class MenuDisplayConfigSubMenuForm extends BaseForm<EditableMenuDisplayConfigSubMenu> {

	public MenuDisplayConfigSubMenuForm(MenuDisplayConfigSubMenu subMenu, AppLayerFactory appLayerFactory) {
		super(new EditableMenuDisplayConfigSubMenu(subMenu), buildSubMenuFormFields(subMenu, appLayerFactory));


		addStyleName("menu-display-config-sub-menu-form");
	}

	@Override
	public void attach() {
		super.attach();

		buildMainComponent();
	}

	private void buildMainComponent() {
		formLayout.addComponent(buildTitleField(), 0);

		setSizeFull();
	}

	public Component buildTitleField() {
		Label title = new Label($("MenuDisplayConfigViewImpl.subMenuEditor.title"));
		title.addStyleName(ValoTheme.LABEL_BOLD);

		return title;
	}

	private static List<FieldAndPropertyId> buildSubMenuFormFields(MenuDisplayConfigSubMenu subMenu,
																   AppLayerFactory appLayerFactory) {
		List<FieldAndPropertyId> fieldAndPropertyIds = new ArrayList<>(buildCaptionsField(subMenu, appLayerFactory));
		fieldAndPropertyIds.add(buildIconField(subMenu));

		return fieldAndPropertyIds;
	}

	private static List<FieldAndPropertyId> buildCaptionsField(MenuDisplayConfigSubMenu subMenu,
															   AppLayerFactory appLayerFactory) {
		SessionContext sessionContext = subMenu.getSessionContext();
		Locale currentLocale = sessionContext.getCurrentLocale();

		List<String> installedLanguages = getInstalledLanguages(subMenu, appLayerFactory);

		Map<Locale, String> captions = subMenu.getCaptions();
		List<FieldAndPropertyId> fieldAndPropertyIds = new ArrayList<>();

		installedLanguages.forEach(installedLanguage -> {
			Locale locale = new Locale(installedLanguage);
			locale.getDisplayName(sessionContext.getCurrentLocale());
			String currentCaptionForLocale = captions.get(locale);
			if (StringUtils.isNullOrEmpty(currentCaptionForLocale)) {
				currentCaptionForLocale = "";
			}

			BaseTextField captionField = new BaseTextField(
					$("MenuDisplayConfigViewImpl.subMenuEditor.caption.title", locale.getDisplayName(currentLocale)),
					currentCaptionForLocale);
			captionField.setRequired(true);

			String languageTag = locale.toLanguageTag();
			languageTag = languageTag.substring(0, 1).toUpperCase() + languageTag.substring(1);
			fieldAndPropertyIds.add(new FieldAndPropertyId(captionField, "title" + languageTag));
		});

		return fieldAndPropertyIds;
	}

	private static List<String> getInstalledLanguages(MenuDisplayConfigSubMenu subMenu,
													  AppLayerFactory appLayerFactory) {
		return appLayerFactory.getCollectionsManager().getCollectionLanguages(subMenu.getSessionContext().getCurrentCollection());
	}

	private static FieldAndPropertyId buildIconField(MenuDisplayConfigSubMenu subMenu) {
		IconService iconService = subMenu.getIconService();
		BaseComboBox icon = new BaseComboBox($("MenuDisplayConfigViewImpl.subMenuEditor.icon.title"));
		icon.setConverter(new FontIconStringConverter(iconService));

		iconService.stream()
				.map(iconService::getIconName)
				.filter(iconName -> iconName.contains("_O_") || iconName.endsWith("_O"))
				.distinct()
				.map(iconService::getIconByName)
				.forEach(validIcon -> {
					Item item = icon.addItem(validIcon);
					icon.setItemIcon(validIcon, validIcon);
					icon.setItemCaption(validIcon, iconService.getIconLabel(validIcon));
				});

		String currentIcon = subMenu.getIconName();
		icon.setRequired(true);
		if (!StringUtils.isNullOrEmpty(currentIcon)) {
			icon.setValue(iconService.getIconByName(subMenu.getIconName()));
		}

		return new FieldAndPropertyId(icon, "iconName");
	}

	public static class EditableMenuDisplayConfigSubMenu {
		private MenuDisplayConfigSubMenu subMenu;

		public EditableMenuDisplayConfigSubMenu(MenuDisplayConfigSubMenu subMenu) {
			this.subMenu = subMenu;
		}

		public Map<Locale, String> getCaptions() {
			return subMenu.getCaptions();
		}

		public void setCaptions(Map<Locale, String> captions) {
			subMenu = new MenuDisplayConfigSubMenu(subMenu) {
				@Override
				public Map<Locale, String> getCaptions() {
					return captions;
				}
			};
		}

		public String getTitleFr() {
			return subMenu.getCaptions().get(Language.French.getLocale());
		}

		public void setTitleFr(String title) {
			subMenu.getCaptions().put(Language.French.getLocale(), title);
		}

		public String getTitleEn() {
			return subMenu.getCaptions().get(Language.English.getLocale());
		}

		public void setTitleEn(String title) {
			subMenu.getCaptions().put(Language.English.getLocale(), title);
		}

		public String getTitleAr() {
			return subMenu.getCaptions().get(Language.Arabic.getLocale());
		}

		public void setTitleAr(String title) {
			subMenu.getCaptions().put(Language.Arabic.getLocale(), title);
		}


		public String getIconName() {
			return subMenu.getIconName();
		}

		public void setIconName(String iconName) {
			subMenu = new MenuDisplayConfigSubMenu(subMenu) {
				@Override
				public String getIconName() {
					return iconName;
				}
			};
		}

		public MenuDisplayConfigSubMenu getSubMenu() {
			return subMenu;
		}
	}

	private static class FontIconStringConverter implements Converter<Object, String> {
		private final IconService iconService;

		private FontIconStringConverter(IconService iconService) {
			this.iconService = iconService;
		}


		@Override
		public String convertToModel(Object value, Class<? extends String> targetType, Locale locale)
				throws ConversionException {
			return iconService.getIconName((FontIcon) value);
		}

		@Override
		public Object convertToPresentation(String value, Class<?> targetType, Locale locale)
				throws ConversionException {
			return iconService.getIconByName(value);
		}

		@Override
		public Class<String> getModelType() {
			return String.class;
		}

		@Override
		public Class<Object> getPresentationType() {
			return Object.class;
		}
	}
}
