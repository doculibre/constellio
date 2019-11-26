package com.constellio.app.ui.pages.setup;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BasePasswordField;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.LogoUtils;
import com.constellio.app.ui.pages.management.updates.UploadWaitWindow;
import com.constellio.app.utils.ManualUpdateHandler;
import com.constellio.app.utils.ManualUpdateHandlerView;
import com.constellio.model.entities.Language;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ConstellioSetupViewImpl extends BaseViewImpl implements ConstellioSetupView, ManualUpdateHandlerView {

	public static final String UPDATE_WAR = "/updatewar";

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

	private UploadWaitWindow uploadWaitWindow;

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


	@PropertyId("adminPasswordConfirmation")
	private PasswordField adminPasswordConfirmationField;

	@PropertyId("demoData")
	private CheckBox demoDataField;

	@PropertyId("saveState")
	private BaseUploadField saveStateField;

	private BaseForm<ConstellioSetupBean> form;

	private ConstellioSetupPresenter presenter;

	private boolean threadIsRunning = false;

	private boolean isUpdateWar;

	public ConstellioSetupViewImpl(String parameter) {
		this.presenter = new ConstellioSetupPresenter(this);

		setSizeFull();

		isUpdateWar = parameter.equals(UPDATE_WAR);

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

		if (isUpdateWar) {
			Button updateButton = new BaseButton($("ConstellioSetupView.setup.update." + Language.English.getCode())) {
				@Override
				protected void buttonClick(ClickEvent event) {
					preSetupButtonsLayout.removeAllComponents();
					if (formLayout != null) {
						mainLayout.removeComponent(formLayout);
					}
					ManualUpdateHandler manualUpdateHandler = new ManualUpdateHandler(
							getConstellioFactories().getAppLayerFactory(),
							ConstellioSetupViewImpl.this);
					preSetupButtonsLayout.addComponent(manualUpdateHandler.buildUpdatePanel());
				}
			};
			updateButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			preSetupButtonsLayout.addComponent(updateButton);
		}

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

	@Override
	public void navigateToMonitoring() {
		Page.getCurrent().setLocation("/constellio/serviceMonitoring");
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

			adminPasswordConfirmationField = new BasePasswordField($("ConstellioSetupView.adminPasswordConfirmation"));

			demoDataField = new CheckBox($("ConstellioSetupView.demoData"));

			formFields = new Field[]{languagesField, modulesField, collectionCodeField, collectionTitleField,
					adminPasswordField, adminPasswordConfirmationField, demoDataField};
		} else {
			saveStateField = new BaseUploadField();
			saveStateField.setCaption($("ConstellioSetupView.saveState"));

			formFields = new Field[]{saveStateField};
		}

		form = new BaseForm<ConstellioSetupBean>(bean, this, formFields) {
			@Override
			protected void saveButtonClick(ConstellioSetupBean viewObject) {

				final List<String> modules = bean.getModules();
				final List<String> languages = bean.getLanguages();
				final String collectionTitle = bean.getCollectionTitle();
				final String collectionCode = bean.getCollectionCode();

				String nonFinaladminPassword = "";
				String nonFinalAdminConfirmationPassword = "";

				if(nonFinaladminPassword != null) {
					nonFinaladminPassword = bean.getAdminPassword();
				}

				if(nonFinalAdminConfirmationPassword != null) {
					nonFinalAdminConfirmationPassword = bean.getAdminPasswordConfirmation();
				}

				final String adminPassword = nonFinaladminPassword;
				final String adminPasswordConfirmation = nonFinalAdminConfirmationPassword;

				final boolean demoData = bean.isDemoData();
				if (!presenter.isLoadSaveState()) {

					try {
						presenter.saveRequested(languages, modules, collectionTitle, collectionCode,
								adminPassword, adminPasswordConfirmation, demoData);
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

			@Override
			protected void cancelButtonClick(ConstellioSetupBean viewObject) {

			}

			@Override
			protected boolean isAddButtonsToStaticFooter() {
				return false;
			}
		};

		formLayout.addComponents(form);
	}

	public void setSubmitButtonEnabled(boolean enabled) {
		form.getSaveButton().setEnabled(enabled);
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
	public void showRestartRequiredPanel() {
		presenter.restart();
	}

	@Override
	public ProgressInfo openProgressPopup() {
		uploadWaitWindow = new UploadWaitWindow();
		final ProgressInfo progressInfo = new ProgressInfo() {
			@Override
			public void setTask(String task) {
				uploadWaitWindow.setTask(task);
			}

			@Override
			public void setProgressMessage(String progressMessage) {
				uploadWaitWindow.setProgressMessage(progressMessage);
			}
		};
		UI.getCurrent().addWindow(uploadWaitWindow);
		return progressInfo;
	}

	@Override
	public void closeProgressPopup() {
		uploadWaitWindow.close();
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

		private String adminPasswordConfirmation;

		private boolean demoData = true;

		private TempFileUpload saveState;

		public final String getAdminPasswordConfirmation() {
			return adminPasswordConfirmation;
		}

		public final void setAdminPasswordConfirmation(String adminPasswordConfirmation) {
			this.adminPasswordConfirmation = adminPasswordConfirmation;
		}

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
