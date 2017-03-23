package com.constellio.app.ui.pages.search.savedSearch;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class SavedSearchViewImpl extends BaseViewImpl implements SavedSearchView {
	private final SavedSearchPresenter presenter;

	@PropertyId(SavedSearch.TITLE) private TextField titleField;
	@PropertyId(SavedSearch.PUBLIC) private CheckBox publicField;

	public SavedSearchViewImpl() {
		presenter = new SavedSearchPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("SavedSearchView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		layout.addComponents(buildUserSearchesTable(), buildPublicSearchesTable());

		return layout;
	}

	private Table buildUserSearchesTable() {
		final RecordVODataProvider dataProvider = presenter.getUserSearchesDataProvider();
		final ButtonsContainer container = buildButtonsContainer(dataProvider);

		titleField = new BaseTextField($(SavedSearch.TITLE));
		publicField = new CheckBox($(SavedSearch.PUBLIC));

		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new WindowButton(EditButton.ICON_RESOURCE, "", true,
						WindowConfiguration.modalDialog("45%", "45%")) {

					@Override
					protected Component buildWindowContent() {
						RecordVO recordVO = dataProvider.getRecordVO((int) itemId);
						return new BaseForm<RecordVO>(recordVO, SavedSearchViewImpl.this, titleField, publicField) {

							@Override
							protected void saveButtonClick(RecordVO viewObject)
									throws ValidationException {
								presenter.searchModificationRequested(viewObject);
								getWindow().close();
							}

							@Override
							protected void cancelButtonClick(RecordVO viewObject) {
								getWindow().close();
							}
						};
					}
				};
			}
		});
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						presenter.deleteButtonClicked(dataProvider.getRecordVO(index));
					}
				};
			}
		});
		RecordVOTable table = new RecordVOTable($("SavedSearchView.userSearches", container.size()), container);
		table.setColumnHeader("buttons", "");
		table.setWidth("100%");
		table.setPageLength(container.size());
		return table;
	}

	private Table buildPublicSearchesTable() {
		ButtonsContainer container = buildButtonsContainer(presenter.getPublicSearchesDataProvider());
		RecordVOTable table = new RecordVOTable($("SavedSearchView.publicSearches", container.size()), container);
		table.setColumnHeader("buttons", "");
		table.setWidth("100%");
		table.setPageLength(container.size());
		return table;
	}

	public ButtonsContainer buildButtonsContainer(final RecordVODataProvider dataProvider) {
		RecordVOLazyContainer recordVOLazyContainer = new RecordVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordVOLazyContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						presenter.searchButtonClicked(dataProvider.getRecordVO(index));
					}
				};
			}
		});
		return buttonsContainer;
	}
}
