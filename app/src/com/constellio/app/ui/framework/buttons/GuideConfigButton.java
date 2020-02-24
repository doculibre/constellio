package com.constellio.app.ui.framework.buttons;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.guide.GuideManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.vaadin.navigator.View;
import com.vaadin.ui.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.framework.components.BaseForm.SAVE_BUTTON;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

public class GuideConfigButton extends WindowButton {
	private static String KEY_PREFIX = "guide.";
	private static String BOTTOM_MARGIN_HEIGHT = "5px";
	private AppLayerFactory appLayerFactory;
	private String collection;
	private VerticalLayout windowLayout;
	private GuideManager guideManager;
	private List<String> languages;

	public GuideConfigButton(String caption, String windowCaption, WindowConfiguration configuration,
							 AppLayerFactory appLayerFactory) {
		super(caption, windowCaption, configuration);
		this.appLayerFactory = appLayerFactory;
		this.collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		this.guideManager = new GuideManager(appLayerFactory.getModelLayerFactory().getDataLayerFactory());
	}

	private String generateGuideKey(View view) {
		if (view == null) {
			return null;
		}
		return KEY_PREFIX + view.getClass().getSimpleName();
	}

	@Override
	protected Component buildWindowContent() {
		windowLayout = new VerticalLayout();
		windowLayout.setSpacing(true);
		this.languages = appLayerFactory.getCollectionsManager().getCollectionLanguages(collection);
		for (String languageCode : languages) {
			String labelText = $("MainLayout.guideConfigButton.languageLabel", $("Language." + languageCode));
			windowLayout.addComponent(new Label(labelText));

			HorizontalLayout cellLayout = new HorizontalLayout();
			TextField inputField = new TextField();
			inputField.setId(languageCode);
			inputField.setInputPrompt($("MainLayout.guideConfigButton.toolTip"));
			inputField.setWidth("400px");
			Locale locale = new Locale(languageCode);

			String currentValue = getCurrentUrl(locale);
			if (currentValue != null) {
				inputField.setValue(currentValue);
				inputField.setStyleName("");
			}
			cellLayout.setSpacing(true);
			cellLayout.addComponent(inputField);
			cellLayout.addComponent(buildResetButton(inputField, locale));

			windowLayout.addComponent(cellLayout);
		}

		windowLayout.addComponent(buildButtonsLayout());
		VerticalLayout bottomMargin = new VerticalLayout();
		bottomMargin.setHeight(BOTTOM_MARGIN_HEIGHT);
		windowLayout.addComponent(bottomMargin);
		return windowLayout;
	}

	private Component buildResetButton(TextField inputField, Locale language) {
		Button resetButton = new BaseButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				inputField.setValue(getDefaultUrl(language));
			}
		};
		resetButton.setCaption($("MainLayout.guideConfigButton.reset"));
		return resetButton;
	}

	private String getDefaultUrl(Locale locale) {
		String fieldKey = generateGuideKey(ConstellioUI.getCurrent().getCurrentView());
		return $(fieldKey, locale);
	}

	private String getCurrentUrl(Locale locale) {
		String fieldKey = generateGuideKey(ConstellioUI.getCurrent().getCurrentView());
		String customUrl = guideManager.getPropertyValue(locale.getLanguage(), fieldKey);
		if (customUrl == null || customUrl.isEmpty()) {
			return $(fieldKey, locale);
		}
		return customUrl;

	}

	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.addComponent(buildSaveButton());
		buttonsLayout.addComponent(buildCancelButton());
		buttonsLayout.setSpacing(true);
		return buttonsLayout;
	}

	private Button buildSaveButton() {
		Button saveButton = new Button($("save"));
		saveButton.addStyleName(SAVE_BUTTON);
		saveButton.addStyleName(BUTTON_PRIMARY);
		saveButton.addClickListener((ClickListener) event -> {
			String guideKey = generateGuideKey(ConstellioUI.getCurrent().getCurrentView());
			Map<String, String> newValues = getNewUrlValues();
			for (String language : languages) {
				String newValue = newValues.get(language);
				guideManager.alterProperty(language, guideKey, newValue);
			}
			getWindow().close();
			refreshPage();
		});
		saveButton.focus();
		return saveButton;
	}

	private void refreshPage() {
		getUI().getPage().reload();
	}

	private Map<String, String> getNewUrlValues() {
		Map<String, String> newValues = new HashMap<>();
		for (String language : languages) {
			Component inputField = findComponentById(windowLayout.getParent(), language);
			if (inputField instanceof TextField) {
				String newUrl = ((TextField) inputField).getValue();
				newValues.put(language, newUrl);
			}
		}
		return newValues;
	}


	private Button buildCancelButton() {
		Button cancelButton = new Button($("cancel"));
		cancelButton.addClickListener((ClickListener) event -> getWindow().close());
		return cancelButton;
	}

	public static Component findComponentById(HasComponents root, String id) {
		for (Component child : root) {
			if (id.equals(child.getId())) {
				return child;
			} else if (child instanceof HasComponents) {
				Component result = findComponentById((HasComponents) child, id);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
}
