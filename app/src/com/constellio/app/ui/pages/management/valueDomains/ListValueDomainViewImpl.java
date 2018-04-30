package com.constellio.app.ui.pages.management.valueDomains;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.TabWithTable;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.Language;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListValueDomainViewImpl extends BaseViewImpl implements ListValueDomainView {
	private final ListValueDomainPresenter presenter;
	private VerticalLayout mainLayout;
	private TabSheet sheet;
	private List<TabWithTable> tabs;

	public ListValueDomainViewImpl() {
		presenter = new ListValueDomainPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListValueDomainView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		initTabs();
		mainLayout = new VerticalLayout(buildCreationComponent(), sheet);
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
		for(TabWithTable tab: tabs) {
			if(tab.getId().equals(id)) {
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
		for(TabWithTable tab: tabs) {
			tab.refreshTable();
		}
	}

	private Component buildCreationComponent() {
        DomainCreationWindowButton domainCreationWindowButton = new DomainCreationWindowButton($("add"));
		domainCreationWindowButton.addStyleName(WindowButton.STYLE_NAME);
        return domainCreationWindowButton;
	}

    public class DomainCreationWindowButton extends WindowButton {
        public static final String TITLE_FR = "title-fr";
        public static final String TITLE_EN = "title-en";

        @PropertyId("titleFr") private BaseTextField titleFr;
        @PropertyId("titleEn") private BaseTextField titleEn;

        public DomainCreationWindowButton(String caption) {
            super(caption, caption);
        }

        @Override
        protected Component buildWindowContent() {
            titleFr = new BaseTextField($("DomainCreationWindowButton.titleFr"));
            titleFr.setImmediate(true);
            titleEn = new BaseTextField($("DomainCreationWindowButton.titleEn"));

            BaseTextField[] baseTextFieldArray;
            baseTextFieldArray = new BaseTextField[2];
            titleEn.setImmediate(true);
            String lang = getSessionContext().getCurrentLocale().getLanguage();
            if(lang.equalsIgnoreCase(Language.English.getCode())){
                titleEn.setRequired(true);
                baseTextFieldArray[0] = titleEn;
                baseTextFieldArray[1] = titleFr;
            } else if(lang.equalsIgnoreCase(Language.French.getCode())) {
                baseTextFieldArray[0] = titleFr;
                baseTextFieldArray[1] = titleEn;
                titleFr.setRequired(true);
            }

            titleEn.setId(TITLE_EN);
            titleFr.setId(TITLE_FR);

			BaseForm<ListValueDomainParam> baseForm = new BaseForm<ListValueDomainParam>(
					new ListValueDomainParam(), this, baseTextFieldArray) {
				@Override
				protected void saveButtonClick(ListValueDomainParam viewObject) throws ValidationException {
					getWindow().close();
					java.util.Map<Language, String> titleMap = new HashMap<>();
					titleMap.put(Language.French, viewObject.getTitleFr());
					titleMap.put(Language.English, viewObject.getTitleEn());

					presenter.valueDomainCreationRequested(titleMap);
				}

				@Override
				protected void cancelButtonClick(ListValueDomainParam viewObject) {
					getWindow().close();
				}
			};


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
						$("edit"), $("ListValueDomainView.labelColumn"), WindowConfiguration.modalDialog("400px", "150px")) {
					@Override
					protected Component buildWindowContent() {
						final MetadataSchemaTypeVO typeVO = (MetadataSchemaTypeVO) itemId;

						Label caption = new Label($("ListValueDomainView.labelColumn"));
						caption.addStyleName(ValoTheme.LABEL_BOLD);

						final BaseTextField title = new BaseTextField();
						title.setValue(typeVO.getLabel());
						title.setWidth("250px");

						BaseButton save = new BaseButton("Save") {
							@Override
							protected void buttonClick(ClickEvent event) {
								java.util.Map<Language, String> languageMap = new HashMap<>();
								languageMap.put(Language.French,title.getValue());
								presenter.editButtonClicked(typeVO, languageMap);
								getWindow().close();
							}
						};



						save.addStyleName(ValoTheme.BUTTON_PRIMARY);

						HorizontalLayout line = new HorizontalLayout(caption, title);
						line.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
						line.setSizeUndefined();
						line.setSpacing(true);

						VerticalLayout window = new VerticalLayout(line, save);
						window.setComponentAlignment(save, Alignment.MIDDLE_CENTER);
						window.setSpacing(true);

						return window;
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
}
