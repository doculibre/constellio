package com.constellio.app.ui.pages.management.valueDomains;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.TabWithTable;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.app.ui.util.ViewErrorDisplay;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Language;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListValueDomainViewImpl extends BaseViewImpl implements ListValueDomainView, AdminViewGroup {
	private final ListValueDomainPresenter presenter;
	private VerticalLayout mainLayout;
	private TabSheet sheet;
	private List<TabWithTable> tabs;
	private Button addValueDomainButton;

	public ListValueDomainViewImpl() {
		presenter = new ListValueDomainPresenter(this);
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();

		addValueDomainButton = new DomainCreationWindowButton($("ListValueDomainViewImpl.addValueDomain"));
		addValueDomainButton.addStyleName(WindowButton.STYLE_NAME);

		actionMenuButtons.add(addValueDomainButton);
		return actionMenuButtons;
	}

	@Override
	protected String getTitle() {
		return $("ListValueDomainView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		initTabs();
		mainLayout = new VerticalLayout(sheet);
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");
		return mainLayout;
	}

	private void initTabs() {
		sheet = new TabSheet();
		tabs = new ArrayList<>();
		addTab(SYSTEM_TAB, $("ListValueDomainView.systemTabCaption"));
		addTab(CUSTOM_TAB, $("ListValueDomainView.customTabCaption"));
	}

	private void addTab(final String id, String caption) {
		TabWithTable tab = new TabWithTable(id) {
			@Override
			public Table buildTable() {
				return ListValueDomainViewImpl.this.buildTable(id);
			}
		};
		tabs.add(tab);
		sheet.addTab(tab.getTabLayout(), caption);
	}

	private void removeTab(String id) {
		TabWithTable tabToRemove = null;
		for (TabWithTable tab : tabs) {
			if (tab.getId().equals(id)) {
				tabToRemove = tab;
				sheet.removeComponent(tab.getTabLayout());
				break;
			}
		}
		tabs.remove(tabToRemove);
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	public void refreshTable() {
		for (TabWithTable tab : tabs) {
			tab.refreshTable();
		}
	}

	public class DomainCreationWindowButton extends WindowButton {
		private java.util.Map<Language, BaseTextField> baseTextFieldMap;
		private String originalStyleName;

		public DomainCreationWindowButton(String caption) {
			super(caption, caption);
		}

		@Override
		protected Component buildWindowContent() {
			baseTextFieldMap = new HashMap<>();

			if (presenter.getCollectionLanguage().size() == 1) {
				BaseTextField baseTextField = new BaseTextField($("title"));
				baseTextField.setRequired(true);
				baseTextFieldMap.put(Language.withCode(presenter.getCollectionLanguage().get(0)), baseTextField);
			} else {
				for (String language : presenter.getCollectionLanguage()) {
					BaseTextField baseTextField = new BaseTextField($("title") + " (" + language.toUpperCase() + ")");
					baseTextField.setRequired(true);
					baseTextFieldMap.put(Language.withCode(language), baseTextField);
				}
			}

			final AbstractField[] fieldArray = new AbstractField[baseTextFieldMap.size() + 1];

			Language currentLanguage = Language.withCode(getSessionContext().getCurrentLocale().getLanguage());

			final CheckBox isMultiLingualCheckBox = new CheckBox($("ListValueDomainViewImpl.multilingual"));
			isMultiLingualCheckBox.setValue(true);
			isMultiLingualCheckBox.setVisible(presenter.getCollectionLanguage().size() > 1 && Toggle.MULTI_LINGUAL.isEnabled());

			fieldArray[0] = isMultiLingualCheckBox;
			fieldArray[1] = baseTextFieldMap.get(currentLanguage);

			int i = 2;
			for (Language language : baseTextFieldMap.keySet()) {
				if (currentLanguage.getCode().equals(language.getCode())) {
					continue;
				} else {
					fieldArray[i] = baseTextFieldMap.get(language);
					i++;
				}

			}

			final BaseForm<Object> baseForm = new BaseForm(
					new Object(), this, fieldArray) {
				@Override
				protected void saveButtonClick(Object viewObject)
						throws ValidationException {
					if (!ViewErrorDisplay.validateFieldsContent(baseTextFieldMap, ListValueDomainViewImpl.this)) {
						return;
					}

					java.util.Map<Language, String> titleMap = new HashMap<>();

					for (Language language : baseTextFieldMap.keySet()) {
						BaseTextField baseTextField = baseTextFieldMap.get(language);
						String value = baseTextField.getValue();
						titleMap.put(language, value);
					}

					boolean isMultiLingual = false;

					if (isMultiLingualCheckBox.isVisible()) {
						isMultiLingual = isMultiLingualCheckBox.getValue();
					}

					List<Language> languagesInErrors = presenter.valueDomainCreationRequested(titleMap, isMultiLingual);
					ViewErrorDisplay.setFieldErrors(languagesInErrors, baseTextFieldMap, originalStyleName);

					if (languagesInErrors.size() == 0) {
						getWindow().close();
					}
				}

				@Override
				protected void cancelButtonClick(Object viewObject) {
					getWindow().close();
				}
			};

			originalStyleName = fieldArray[0].getStyleName();

			return baseForm;
		}

	}

	private Table buildTable(String id) {
		BeanItemContainer elements = new BeanItemContainer<>(
				MetadataSchemaTypeVO.class, presenter.getDomainValues(CUSTOM_TAB.equals(id)));

		ButtonsContainer container = new ButtonsContainer<>(elements, "buttons");
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.displayButtonClicked((MetadataSchemaTypeVO) itemId);
					}
				};
			}
		});

		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				WindowButton editButton = new WindowButton(
						$("edit"), $("ListValueDomainView.labelColumn")) {
					private java.util.Map<Language, BaseTextField> baseTextFieldMap;
					private String originalStyleName;

					@Override
					protected Component buildWindowContent() {
						final MetadataSchemaTypeVO typeVO = (MetadataSchemaTypeVO) itemId;

						baseTextFieldMap = new HashMap<>();

						for (String currentLanguage : presenter.getCollectionLanguage()) {
							BaseTextField baseTextField = new BaseTextField(
									$("title") + " (" + currentLanguage.toUpperCase() + ")");
							baseTextField.setRequired(true);
							Language language = Language.withCode(currentLanguage);
							baseTextFieldMap.put(language, baseTextField);
							String previousValue = typeVO.getLabels().get(language);
							baseTextField.setValue(previousValue);

						}

						final BaseTextField[] baseTextFieldArray = new BaseTextField[baseTextFieldMap.size()];

						Language currentLanguage = Language.withCode(getSessionContext().getCurrentLocale().getLanguage());

						baseTextFieldArray[0] = baseTextFieldMap.get(currentLanguage);
						int i = 1;
						for (Language language : baseTextFieldMap.keySet()) {
							if (currentLanguage.getCode().equals(language.getCode())) {
								continue;
							} else {
								baseTextFieldArray[i] = baseTextFieldMap.get(language);
								i++;
							}

						}

						final BaseForm<Object> baseForm = new BaseForm(
								new Object(), this, baseTextFieldArray) {
							@Override
							protected void saveButtonClick(Object viewObject)
									throws ValidationException {
								if (!ViewErrorDisplay.validateFieldsContent(baseTextFieldMap, ListValueDomainViewImpl.this)) {
									return;
								}

								java.util.Map<Language, String> titleMap = new HashMap<>();

								for (Language language : baseTextFieldMap.keySet()) {
									BaseTextField baseTextField = baseTextFieldMap.get(language);
									String value = baseTextField.getValue();
									titleMap.put(language, value);
								}

								List<Language> languagesInErrors = presenter
										.editButtonClicked(typeVO, titleMap, typeVO.getLabels());

								ViewErrorDisplay.setFieldErrors(languagesInErrors, baseTextFieldMap, originalStyleName);

								if (languagesInErrors.size() == 0) {
									getWindow().close();
								}
							}

							@Override
							protected void cancelButtonClick(Object viewObject) {
								getWindow().close();
							}
						};

						originalStyleName = baseTextFieldArray[0].getStyleName();

						return baseForm;
					}
				};
				editButton.setIcon(EditButton.ICON_RESOURCE);
				editButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
				editButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
				editButton.setIconAlternateText(editButton.getCaption());
				return editButton;
			}
		});

		container.addButton(new ContainerButton() {

			@Override
			protected Button newButtonInstance(Object itemId, ButtonsContainer<?> container) {
				final MetadataSchemaTypeVO typeVO = (MetadataSchemaTypeVO) itemId;
				final String schemaTypeCode = typeVO.getCode();
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {

						try {
							presenter.deleteButtonClicked(schemaTypeCode);
						} catch (ValidationException e) {
							showErrorMessage($(e));
						}
					}
				};

				deleteButton.setVisible(presenter.isValueListPossiblyDeletable(schemaTypeCode));

				return deleteButton;
			}
		});

		Table table = new BaseTable(getClass().getName(), $("ListValueDomainView.tableTitle", container.size()), container);
		table.setPageLength(Math.min(15, container.size()));
		table.setVisibleColumns("label", "buttons");
		table.setColumnHeader("label", $("ListValueDomainView.labelColumn"));
		table.setColumnHeader("buttons", "");
		table.setColumnWidth("buttons", 124);
		table.setWidth("100%");

		return table;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return null;
	}
}
