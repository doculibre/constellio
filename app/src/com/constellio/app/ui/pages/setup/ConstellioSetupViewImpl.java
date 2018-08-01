package com.constellio.app.ui.pages.setup;

import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BasePasswordField;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.LogoUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ConstellioSetupViewImpl extends BaseViewImpl implements ConstellioSetupView {

	private ConstellioSetupBean bean = new ConstellioSetupBean();

	//	private String setupLocaleCode;

	private List<String> localeCodes = new ArrayList<>();

	private List<String> moduleIds = new ArrayList<>();

	//	private boolean loadSaveState;

	private VerticalLayout mainLayout;

	private CssLayout labelsLayout;

	private VerticalLayout preSetupButtonsLayout;

	private VerticalLayout formLayout;

	private Label welcomeLabel;

	@PropertyId("modules")
	private OptionGroup modulesField;

	@PropertyId("languages")
	private OptionGroup languagesField;

	@PropertyId("collectionTitle")
	private TextField collectionTitleField;

	@PropertyId("collectionCode")
	private TextField collectionCodeField;

	@PropertyId("adminPassword")
	private PasswordField adminPasswordField;

	@PropertyId("demoData")
	private CheckBox demoDataField;

	@PropertyId("saveState")
	private BaseUploadField saveStateField;

	private BaseForm<ConstellioSetupBean> form;

	private ConstellioSetupPresenter presenter;

	public ConstellioSetupViewImpl() {
		this.presenter = new ConstellioSetupPresenter(this);

		setSizeFull();

		mainLayout = new VerticalLayout();
		mainLayout.addStyleName("setup-panel");
		mainLayout.setSizeUndefined();
		mainLayout.setSpacing(true);

		buildLabels();
		buildPreSetupButtons();

		addComponent(mainLayout);
		mainLayout.addComponents(labelsLayout, preSetupButtonsLayout);

		setComponentAlignment(mainLayout, Alignment.MIDDLE_CENTER);
	}

	private void buildLabels() {
		labelsLayout = new CssLayout();
		labelsLayout.addStyleName("labels");

		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSpacing(true);
		hLayout.setSizeFull();

		welcomeLabel = new Label($("ConstellioSetupView.welcome"));
		welcomeLabel.setSizeUndefined();
		welcomeLabel.addStyleName(ValoTheme.LABEL_H2);
		welcomeLabel.addStyleName(ValoTheme.LABEL_COLORED);
		hLayout.addComponent(welcomeLabel);

		String linkTarget = presenter.getLogoTarget();
		Link logo = new Link(null, new ExternalResource(linkTarget));
		ModelLayerFactory modelLayerFactory = getConstellioFactories().getModelLayerFactory();
		logo.setIcon(LogoUtils.getAuthentificationImageResource(modelLayerFactory));
		logo.addStyleName("setup-logo");
		logo.setSizeUndefined();
		hLayout.addComponent(logo);
		labelsLayout.addComponent(hLayout);

		hLayout.setComponentAlignment(logo, Alignment.TOP_RIGHT);
	}

	@Override
	public void setLocaleCodes(List<String> localeCodes) {
		this.localeCodes = localeCodes;
	}

	@Override
	public void setModuleIds(List<String> moduleIds) {
		this.moduleIds = moduleIds;
	}

	private void buildPreSetupButtons() {
		preSetupButtonsLayout = new VerticalLayout();
		preSetupButtonsLayout.setSpacing(true);

		for (final String localeCode : localeCodes) {
			Button languageButton = new Button($("ConstellioSetupView.setup." + localeCode));
			languageButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			languageButton.setIcon(new ThemeResource("images/icons/language/" + localeCode + ".png"));
			languageButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.languageButtonClicked(localeCode);
				}
			});
			preSetupButtonsLayout.addComponent(languageButton);
		}

		Button loadSaveStateButton = new Button($("ConstellioSetupView.setup.loadSaveState"));
		loadSaveStateButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		loadSaveStateButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.loadSaveStateButtonClicked();
			}
		});
	}

	@Override
	public void reloadForm() {
		if (formLayout != null) {
			mainLayout.removeComponent(formLayout);
		}
		buildFields();
		mainLayout.addComponent(formLayout);
	}

	private void buildFields() {
		formLayout = new VerticalLayout();
		formLayout.setSpacing(true);

		Field<?>[] formFields;
		if (!presenter.isLoadSaveState()) {
			languagesField = new ListOptionGroup($("ConstellioSetupView.languages"));
			languagesField.setMultiSelect(true);
			languagesField.setRequired(true);

			String setupLocaleCode = presenter.getSetupLocaleCode();
			for (String languageCode : this.localeCodes) {
				languagesField.addItem(languageCode);
				languagesField.setItemEnabled(languageCode, !setupLocaleCode.equals(languageCode));
				languagesField.setItemCaption(languageCode, $("Language." + languageCode));
			}
			bean.setLanguages(asList(setupLocaleCode));

			modulesField = new ListOptionGroup($("ConstellioSetupView.modules"));
			modulesField.setMultiSelect(true);
			modulesField.setRequired(true);

			for (String moduleId : moduleIds) {
				String moduleName = $("ConstellioSetupView.module." + moduleId);
				modulesField.addItem(moduleId);
				modulesField.setItemCaption(moduleId, moduleName);
			}

			collectionTitleField = new BaseTextField($("ConstellioSetupView.collectionTitle"));

			collectionCodeField = new BaseTextField($("ConstellioSetupView.collectionCode"));
			collectionCodeField.setRequired(true);

			adminPasswordField = new BasePasswordField($("ConstellioSetupView.adminPassword"));

			demoDataField = new CheckBox($("ConstellioSetupView.demoData"));

			formFields = new Field[]{languagesField, modulesField, collectionCodeField, collectionTitleField,
									 adminPasswordField, demoDataField};
		} else {
			saveStateField = new BaseUploadField();
			saveStateField.setCaption($("ConstellioSetupView.saveState"));

			formFields = new Field[]{saveStateField};
		}

		form = new BaseForm<ConstellioSetupBean>(bean, this, formFields) {
			@Override
			protected void saveButtonClick(ConstellioSetupBean viewObject)
					throws ValidationException {
				new Thread() {
					@Override
					public void run() {
						if (!presenter.isLoadSaveState()) {
							List<String> modules = bean.getModules();
							List<String> languages = bean.getLanguages();
							String collectionTitle = bean.getCollectionTitle();
							String collectionCode = bean.getCollectionCode();
							String adminPassword = bean.getAdminPassword();
							boolean demoData = bean.isDemoData();

							try {
								presenter.saveRequested(languages, modules, collectionTitle, collectionCode,
										adminPassword, demoData);
							} catch (ConstellioSetupPresenterException constellioSetupPresenterException) {
								showMessage(constellioSetupPresenterException.getMessage());

							}
						} else {
							TempFileUpload saveState = bean.getSaveState();
							File saveStateFile = saveState.getTempFile();
							try {
								presenter.loadSaveStateRequested(saveStateFile);
							} catch (ConstellioSetupPresenterException constellioSetupPresenterException) {
								showErrorMessage(constellioSetupPresenterException.getMessage());
							} finally {
								saveState.delete();
							}
						}
					}
				}.start();
			}

			@Override
			protected void cancelButtonClick(ConstellioSetupBean viewObject) {
			}
		};

		formLayout.addComponents(form);
	}

	@Override
	public void showMessage(final String message) {
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				ConstellioSetupViewImpl.super.showMessage(message);
			}
		});
	}

	@Override
	public void showErrorMessage(final String errorMessage) {
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				ConstellioSetupViewImpl.super.showErrorMessage(errorMessage);
			}
		});
	}

	@Override
	public void updateUI() {
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				ConstellioSetupViewImpl.super.updateUI();
			}
		});
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return mainLayout;
	}

	public static class ConstellioSetupBean implements Serializable {

		private List<String> modules = new ArrayList<>();

		private List<String> languages = new ArrayList<>();

		private String collectionCode;

		private String collectionTitle;

		private String adminPassword;

		private boolean demoData = true;

		private TempFileUpload saveState;

		public final List<String> getModules() {
			return modules;
		}

		public final void setModules(List<String> modules) {
			this.modules = modules;
		}

		public final String getCollectionCode() {
			return collectionCode;
		}

		public final void setCollectionCode(String collectionCode) {
			this.collectionCode = collectionCode;
		}

		public final String getCollectionTitle() {
			return collectionTitle;
		}

		public final void setCollectionTitle(String collectionTitle) {
			this.collectionTitle = collectionTitle;
		}

		public final String getAdminPassword() {
			return adminPassword;
		}

		public final void setAdminPassword(String adminPassword) {
			this.adminPassword = adminPassword;
		}

		public final TempFileUpload getSaveState() {
			return saveState;
		}

		public final void setSaveState(TempFileUpload saveState) {
			this.saveState = saveState;
		}

		public List<String> getLanguages() {
			return languages;
		}

		public void setLanguages(List<String> languages) {
			this.languages = languages;
		}

		public boolean isDemoData() {
			return demoData;
		}

		public void setDemoData(boolean demoData) {
			this.demoData = demoData;
		}
	}

}
