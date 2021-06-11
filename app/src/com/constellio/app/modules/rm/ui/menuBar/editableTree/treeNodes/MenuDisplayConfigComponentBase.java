package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes;

import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MenuDisplayConfigComponentBase implements MenuDisplayConfigComponent {
	private final MenuDisplayConfigComponent parent;
	private final String code;
	private final Map<Locale, String> captions;
	private final String i18nKey;
	private final String iconName;
	private final List<MenuDisplayConfigComponent> children;
	private final Boolean enabled;
	private final Boolean alwaysEnabled;
	private final Class<? extends MenuDisplayConfigComponent> mainClass;
	private final SessionContext sessionContext;
	private final IconService iconService;

	public MenuDisplayConfigComponentBase(MenuDisplayConfigComponent copy) {
		this.code = copy.getCode();
		this.parent = copy.getParent();
		this.captions = copy.getCaptions();
		this.i18nKey = copy.getI18NKey();
		this.iconName = copy.getIconName();
		this.children = copy.getChildren();
		this.enabled = copy.isEnabled();
		this.alwaysEnabled = copy.isAlwaysEnabled();
		this.mainClass = copy.getMainClass();
		this.sessionContext = copy.getSessionContext();
		this.iconService = copy.getIconService();
	}

	public MenuDisplayConfigComponentBase(Class<? extends MenuDisplayConfigComponent> clazz, String code,
										  SessionContext sessionContext, IconService iconService) {
		this(new MenuDisplayConfigComponent() {
			@Override
			public String getCode() {
				return code;
			}

			@Override
			public MenuDisplayConfigComponent getParent() {
				return null;
			}

			@Override
			public MenuDisplayConfigComponent applyModification(MenuDisplayConfigComponent copy) {
				return new MenuDisplayConfigComponentBase(copy);
			}

			@Override
			public Map<Locale, String> getCaptions() {
				return MenuDisplayConfigComponentBase.defaultCaptions();
			}

			@Override
			public String getI18NKey() {
				return MenuDisplayConfigComponentBase.defaultCaptions().get(sessionContext.getCurrentLocale());
			}

			@Override
			public String getIconName() {
				return null;
			}

			@Override
			public List<MenuDisplayConfigComponent> getChildren() {
				return new ArrayList<>();
			}

			@Override
			public List<MenuDisplayConfigComponent> getEnabledChildren() {
				return getChildren().stream().filter(MenuDisplayConfigComponent::isEnabled).collect(Collectors.toList());
			}

			@Override
			public List<MenuDisplayConfigComponent> getDisabledChildren() {
				return getChildren().stream().filter(MenuDisplayConfigComponent::isEnabled).collect(Collectors.toList());
			}

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean isAlwaysEnabled() {
				return false;
			}

			@Override
			public Class<? extends MenuDisplayConfigComponent> getMainClass() {
				return clazz;
			}

			@Override
			public SessionContext getSessionContext() {
				return sessionContext;
			}

			@Override
			public IconService getIconService() {
				return iconService;
			}
		});
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public MenuDisplayConfigComponent getParent() {
		return parent;
	}

	@Override
	public MenuDisplayConfigComponent applyModification(MenuDisplayConfigComponent copy) {
		return new MenuDisplayConfigComponentBase(copy);
	}

	@Override
	public Map<Locale, String> getCaptions() {
		return captions;
	}

	@Override
	public String getI18NKey() {
		return i18nKey;
	}

	@Override
	public String getIconName() {
		return iconName;
	}

	@Override
	public List<MenuDisplayConfigComponent> getChildren() {
		return children;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isAlwaysEnabled() {
		return alwaysEnabled;
	}

	@Override
	public Class<? extends MenuDisplayConfigComponent> getMainClass() {
		return mainClass;
	}

	@Override
	public SessionContext getSessionContext() {
		return sessionContext;
	}

	@Override
	public IconService getIconService() {
		return iconService;
	}

	@Override
	public List<MenuDisplayConfigComponent> getEnabledChildren() {
		return getChildren().stream().filter(MenuDisplayConfigComponent::isEnabled).collect(Collectors.toList());
	}

	@Override
	public List<MenuDisplayConfigComponent> getDisabledChildren() {
		return getChildren().stream().filter(children -> !children.isEnabled()).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return "Title: " + getCaptions().get(getSessionContext().getCurrentLocale());
	}

	protected static Map<Locale, String> defaultCaptions() {
		Map<Locale, String> captions = new HashMap<>();

		captions.put(Language.French.getLocale(), "");
		captions.put(Language.English.getLocale(), "");
		captions.put(Language.Arabic.getLocale(), "");

		return captions;
	}
}
