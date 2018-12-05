package com.constellio.app.ui.pages.search.savedSearch;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class SavedSearchViewImpl extends BaseViewImpl implements SavedSearchView {
	private final SavedSearchPresenter presenter;

	@PropertyId(SavedSearch.TITLE) private TextField titleField;
	@PropertyId(SavedSearch.SHARED_USERS) private ListAddRemoveRecordLookupField users;
	@PropertyId(SavedSearch.SHARED_GROUPS) private ListAddRemoveRecordLookupField groups;
	@PropertyId("shareOptions") private OptionGroup shareOptions;

	private enum ShareType {
		NONE, ALL, RESTRICTED
	}

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

		container.addButton(buildEditionContainerButton(dataProvider, false));
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
		final RecordVODataProvider dataProvider = presenter.getPublicSearchesDataProvider();
		ButtonsContainer container = buildButtonsContainer(dataProvider);

		container.addButton(buildEditionContainerButton(dataProvider, true));

		if (presenter.hasUserAcessToDeletePublicSearches()) {
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
		}

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

	private ContainerButton buildEditionContainerButton(final RecordVODataProvider dataProvider,
														final boolean publicSearch) {
		return new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new WindowButton(EditButton.ICON_RESOURCE, "", true,
						WindowConfiguration.modalDialog("50%", "70%")) {

					@Override
					protected Component buildWindowContent() {
						RecordVO recordVO = dataProvider.getRecordVO((int) itemId);
						buildShareOptionsField(recordVO);
						buildUsersAndGroupsField(recordVO);

						return new BaseForm<RecordVO>(recordVO, SavedSearchViewImpl.this,
								titleField, shareOptions, groups, users) {

							@Override
							protected void saveButtonClick(RecordVO viewObject) {
								boolean publicAccess = !shareOptions.getValue().equals(ShareType.NONE);
								presenter.searchModificationRequested(viewObject.getId(), titleField.getValue(),
										publicAccess, groups.getValue(), users.getValue());
								getWindow().close();
							}

							@Override
							protected void cancelButtonClick(RecordVO viewObject) {
								getWindow().close();
							}
						};
					}

					@Override
					public boolean isVisible() {
						return presenter.isSavedSearchEditable(publicSearch);
					}
				};
			}
		};
	}

	private void buildUsersAndGroupsField(RecordVO recordVO) {
		users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
		users.setValue(recordVO.<List<String>>get(SavedSearch.SHARED_USERS));
		users.setCaption($("SavedSearchView.savedSearch.users"));
		users.setId("users");
		users.setVisible(getShareType(recordVO).equals(ShareType.RESTRICTED));

		groups = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
		groups.setValue(recordVO.<List<String>>get(SavedSearch.SHARED_GROUPS));
		groups.setCaption($("SavedSearchView.savedSearch.groups"));
		groups.setId("groups");
		groups.setVisible(getShareType(recordVO).equals(ShareType.RESTRICTED));
	}

	private void buildShareOptionsField(RecordVO recordVO) {
		shareOptions = new OptionGroup();
		shareOptions.addItems(ShareType.NONE, ShareType.ALL, ShareType.RESTRICTED);
		shareOptions.setItemCaption(ShareType.NONE, $("SavedSearchView.savedSearch.share.none"));
		shareOptions.setItemCaption(ShareType.ALL, $("SavedSearchView.savedSearch.share.all"));
		shareOptions.setItemCaption(ShareType.RESTRICTED, $("SavedSearchView.savedSearch.share.restrict"));
		shareOptions.setValue(getShareType(recordVO));
		shareOptions.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				boolean visible = event.getProperty().getValue().equals(ShareType.RESTRICTED);
				groups.setVisible(visible);
				users.setVisible(visible);
				if (!visible) {
					groups.clear();
					users.clear();
				}
			}
		});
		shareOptions.setId("shareOptions");
	}

	private ShareType getShareType(RecordVO recordVO) {
		boolean publicSearch = recordVO.get(SavedSearch.PUBLIC);
		if (!publicSearch) {
			return ShareType.NONE;
		}
		boolean restricted = recordVO.get(SavedSearch.RESTRICTED);
		return restricted ? ShareType.RESTRICTED : ShareType.ALL;
	}
}
