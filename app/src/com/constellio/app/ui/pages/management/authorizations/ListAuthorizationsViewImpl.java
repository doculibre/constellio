package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.converters.GroupIdToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.constellio.app.ui.framework.components.converters.TaxonomyRecordIdToContextCaptionConverter;
import com.constellio.app.ui.framework.components.converters.UserIdToCaptionConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.publish.PublishDocumentTable;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Container;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public abstract class ListAuthorizationsViewImpl extends BaseViewImpl implements ListAuthorizationsView {
	public static final String INHERITED_AUTHORIZATIONS = "authorizations-inherited";
	public static final String AUTHORIZATIONS = "authorizations";
	private static final String ENABLE = "AuthorizationsView.enable";
	private static final String DISABLE = "AuthorizationsView.disable";
	private static final String SHARED = "AuthorizationsView.shared";

	public enum AuthorizationSource {
		INHERITED, OWN, INHERITED_FROM_METADATA, SHARED
	}

	public enum DisplayMode {
		CONTENT, PRINCIPALS, BOTH
	}

	protected ListAuthorizationsPresenter presenter;
	protected RecordVO record;
	protected VerticalLayout layout;
	private Table authorizations;
	private Table authorizationsReceivedFromMetadatas;
	private Table globalLinks;
	protected Button detach;
	protected Button addSecondButton, addButton;
	private boolean isViewReadOnly;

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		record = presenter.forRequestParams(event.getParameters()).getRecordVO();
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> result = super.buildActionMenuButtons(event);
		if (!(this instanceof ShareContentListView)) {
			result.add(buildDetachButton());
		}
		result.add(buildAddButton());
		if (this instanceof ListPrincipalAccessAuthorizationsView ||
			this instanceof ShareContentListView) {
			result.add(buildSecondaryAddButton());
		}
		return result;
	}

	@Override
	protected boolean isActionMenuBar() {
		return true;
	}

	@Override
	protected List<Button> getQuickActionMenuButtons() {
		List<Button> quickActionMenuButtons = new ArrayList<>();
		List<Button> actionMenuButtons = getActionMenuButtons();
		if (actionMenuButtons != null) {
			List<Button> visibleButtons = actionMenuButtons.stream().filter(button -> button.isVisible() && button.isEnabled()).collect(Collectors.toList());
			int remainingSpaceForQuickActions = 2;
			for (Button button : visibleButtons) {
				if (remainingSpaceForQuickActions > 0) {
					remainingSpaceForQuickActions--;
					quickActionMenuButtons.add(button);
				} else {
					break;
				}
			}
			actionMenuButtons.stream().forEach(quickActionMenuButtons::add);
		}

		return quickActionMenuButtons;
	}

	protected abstract Button buildAddButton();

	protected Button buildSecondaryAddButton() {
		return null;
	}

	private Button buildDetachButton() {
		if (presenter.seeAccessField()) {
			detach = new ConfirmDialogButton($("ListContentAccessAuthorizationsView.detach")) {
				@Override
				protected String getConfirmDialogMessage() {
					return $("ListContentAccessAuthorizationsView.comfirmDetach");
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.detachRequested();
				}

				@Override
				public boolean isVisible() {
					return super.isVisible() && !isViewReadOnly();
				}
			};
		}
		if (presenter.seeRolesField()) {
			detach = new ConfirmDialogButton($("ListContentRoleAuthorizationsView.detach")) {
				@Override
				protected String getConfirmDialogMessage() {
					return $("ListContentRoleAuthorizationsView.comfirmDetach");
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.detachRequested();
				}

				@Override
				public boolean isVisible() {
					return super.isVisible() && !isViewReadOnly();
				}
			};
		}
		detach.setVisible(presenter.isDetacheable());
		detach.setEnabled(presenter.isAttached());
		return detach;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked(record.getSchema().getCode());
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setWidth("100%");
		if (!presenter.seeSharedBy()) {
			buildGlobalAccess(layout);
			buildInheritedAuthorizationsFromMetadatas(layout);
			buildInheritedAuthorizations(layout);
			buildOwnAuthorizations(layout);
		} else {
			buildSharedAuthorizations(layout);
			buildGlobalLinks(layout);
		}
		return layout;
	}

	protected void buildGlobalAccess(VerticalLayout layout) {

	}

	@Override
	public void refresh() {
		layout.removeAllComponents();
		if (!presenter.seeSharedBy()) {
			buildGlobalAccess(layout);
			buildInheritedAuthorizationsFromMetadatas(layout);
			buildInheritedAuthorizations(layout);
			buildOwnAuthorizations(layout);
			detach.setEnabled(presenter.isAttached());
		} else {
			buildSharedAuthorizations(layout);
			buildGlobalLinks(layout);
		}
	}

	@Override
	public void addAuthorization(AuthorizationVO authorizationVO) {
		authorizations.addItem(authorizationVO);
		updateAuthorizationsTable();
	}

	@Override
	public void removeAuthorization(AuthorizationVO authorization) {
		refresh();
	}

	protected abstract DisplayMode getDisplayMode();

	private void buildOwnAuthorizations(VerticalLayout layout) {
		Label label = new Label();
		if (presenter.seeAccessField()) {
			label.setValue($("ListAccessAuthorizationsView.ownAuthorizations"));
		} else if (presenter.seeRolesField()) {
			label.setValue($("ListRoleAuthorizationsView.ownAuthorizations"));
		} else if (presenter.seeSharedBy()) {
			label.setValue($("ListContentShareView.ownAuthorizations"));
		}
		label.addStyleName(ValoTheme.LABEL_H2);
		authorizations = buildAuthorizationTable(presenter.getOwnAuthorizations(), AuthorizationSource.OWN);
		layout.addComponents(label, authorizations);
	}

	private void buildSharedAuthorizations(VerticalLayout layout) {
		if (presenter.seeSharedBy()) {
			Label label = new Label();
			label.setValue($("ListContentShareView.ownAuthorizations", record.getTitle()));

			label.addStyleName(ValoTheme.LABEL_H2);
			authorizations = buildAuthorizationTable(presenter.getSharedAuthorizations(), AuthorizationSource.SHARED);
			layout.addComponents(label, authorizations);
		}
	}

	private void buildGlobalLinks(VerticalLayout layout) {
		if (presenter.seeSharedBy()) {
			Label label = new Label();
			label.setValue($("ListContentShareView.globalLinks", record.getTitle()));

			label.addStyleName(ValoTheme.LABEL_H2);
			globalLinks = buildDocumentTable(presenter.getPublishedDocuments());
			layout.addComponents(label, globalLinks);
		}
	}

	private void buildInheritedAuthorizationsFromMetadatas(VerticalLayout layout) {
		List<AuthorizationVO> authorizationVOs = presenter.getInheritedAuthorizationsFromMetadatas();

		Label label = new Label();
		if (presenter.seeAccessField()) {
			label.setValue($("ListAccessAuthorizationsView.inheritedMetadataAuthorizations"));
		} else if (presenter.seeRolesField()) {
			label.setValue($("ListRoleAuthorizationsView.inheritedMetadataAuthorizations"));
		} else if (presenter.seeSharedBy()) {
			label.setValue($("ListContentShareView.inheritedMetadataAuthorizations"));
		}
		label.addStyleName(ValoTheme.LABEL_H2);
		authorizationsReceivedFromMetadatas = buildAuthorizationTable(authorizationVOs,
				AuthorizationSource.INHERITED_FROM_METADATA);

		if (!authorizationVOs.isEmpty()) {

			layout.addComponent(label);
			if (presenter.hasOverridenSecurityFromMetadatas()) {
				Label warningLabel = new Label();
				if (presenter.seeAccessField()) {
					warningLabel.setValue($("ListAccessAuthorizationsView.inheritedAuthorizationsOverriden"));
				} else if (presenter.seeRolesField()) {
					warningLabel.setValue($("ListRoleAuthorizationsView.inheritedAuthorizationsOverriden"));
				} else if (presenter.seeSharedBy()) {
					warningLabel.setValue($("ListContentShareView.inheritedAuthorizationsOverriden"));
				}
				warningLabel.addStyleName(ValoTheme.LABEL_COLORED);
				layout.addComponent(warningLabel);
			}

			layout.addComponent(authorizationsReceivedFromMetadatas);
		}
	}

	private void buildInheritedAuthorizations(VerticalLayout layout) {
		List<AuthorizationVO> authorizationVOs = presenter.getInheritedAuthorizations();

		Label label = new Label();
		if (presenter.seeAccessField()) {
			label.setValue($("ListAccessAuthorizationsView.inheritedAuthorizations"));
		} else if (presenter.seeRolesField()) {
			label.setValue($("ListRoleAuthorizationsView.inheritedAuthorizations"));
		}
		label.addStyleName(ValoTheme.LABEL_H2);

		if (!presenter.hasOverridenSecurityFromMetadatas() || !authorizationVOs.isEmpty()) {
			authorizations = buildAuthorizationTable(authorizationVOs, AuthorizationSource.INHERITED);
			layout.addComponents(label, authorizations);
		}
	}

	private Table buildAuthorizationTable(List<AuthorizationVO> authorizationVOs, AuthorizationSource source) {
		Container container = buildAuthorizationContainer(authorizationVOs, source);
		String tableCaption = "";
		if (presenter.seeAccessField()) {
			tableCaption = $("ListAccessAuthorizationsView.authorizations", container.size());
		} else if (presenter.seeRolesField()) {
			tableCaption = $("ListRoleAuthorizationsView.authorizations", container.size());
		} else if (presenter.seeSharedBy()) {
			tableCaption = $("ListContentShareView.authorizations", container.size());
		}
		Table table = new BaseTable(getClass().getName(), tableCaption, container);
		table.setPageLength(container.size());
		table.addStyleName((source == AuthorizationSource.OWN || source == AuthorizationSource.SHARED) ? AUTHORIZATIONS : INHERITED_AUTHORIZATIONS);
		new Authorizations(source, getDisplayMode(), presenter.seeRolesField(), presenter.seeSharedBy(), presenter.seeAccessField(), isViewReadOnly(), getSessionContext().getCurrentLocale()).attachTo(table, presenter.isRecordNotATaxonomyConcept());
		return table;
	}

	private Table buildDocumentTable(List<DocumentVO> documentVOS) {
		Container container = buildDocumentContainer(documentVOS);
		String tableCaption = $("ListContentShareView.nombreGlobalLinks", container.size());

		Table table = new BaseTable(getClass().getName(), tableCaption, container);
		table.setPageLength(container.size());
		table.addStyleName(AUTHORIZATIONS);
		new PublishDocumentTable(getSessionContext().getCurrentLocale()).attachTo(table, presenter.isRecordNotATaxonomyConcept());
		return table;
	}

	private Container buildAuthorizationContainer(List<AuthorizationVO> authorizationVOs, AuthorizationSource source) {
		BeanItemContainer<AuthorizationVO> authorizations = new BeanItemContainer<>(AuthorizationVO.class, authorizationVOs);
		return source == AuthorizationSource.OWN || source == AuthorizationSource.SHARED ?
			   addButtons(authorizations, source == AuthorizationSource.INHERITED) :
			   authorizations;
	}

	private Container buildDocumentContainer(List<DocumentVO> documentVOS) {
		BeanItemContainer<DocumentVO> documents = new BeanItemContainer<>(DocumentVO.class, documentVOS);
		return addButtonDocument(documents, true);
	}

	private Container addButtons(BeanItemContainer<AuthorizationVO> authorizations, final boolean inherited) {
		ButtonsContainer container = new ButtonsContainer<>(authorizations, Authorizations.BUTTONS);
		if (canEditAuthorizations() && !isViewReadOnly()) {
			container.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(Object itemId, ButtonsContainer<?> container) {
					final AuthorizationVO authorization = (AuthorizationVO) itemId;
					EditAuthorizationButton button = new EditAuthorizationButton(authorization) {
						@Override
						protected void onSaveButtonClicked(AuthorizationVO authorizationVO) {
							presenter.authorizationModificationRequested(authorization);
						}

						@Override
						public boolean isVisible() {
							return super.isVisible() && !isViewReadOnly();
						}
					};
					button.setVisible(inherited || !authorization.isSynched());
					return button;
				}
			});
		}
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				final AuthorizationVO authorization = (AuthorizationVO) itemId;
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked(authorization);
					}

					@Override
					public boolean isVisible() {
						return super.isVisible() && !isViewReadOnly();
					}
				};
				deleteButton.setVisible(inherited || !authorization.isSynched());
				return deleteButton;
			}
		});
		return container;
	}

	private Container addButtonDocument(BeanItemContainer<DocumentVO> documents, final boolean inherited) {
		ButtonsContainer container = new ButtonsContainer<>(documents, Authorizations.BUTTONS);

		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				final DocumentVO document = (DocumentVO) itemId;
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.unpublish(document);
					}

					@Override
					public boolean isVisible() {
						return super.isVisible() && !isViewReadOnly();
					}
				};
				deleteButton.setVisible(inherited);
				return deleteButton;
			}
		});
		return container;
	}

	private void updateAuthorizationsTable() {
		String tableCaption = "";
		if (presenter.seeAccessField()) {
			tableCaption = $("ListAccessAuthorizationsView.authorizations", authorizations.size());
		} else if (presenter.seeRolesField()) {
			tableCaption = $("ListRoleAuthorizationsView.authorizations", authorizations.size());
		} else if (presenter.seeSharedBy()) {
			tableCaption = $("ListContentShareView.authorizations", authorizations.size());
		}
		authorizations.setCaption(tableCaption);
		authorizations.setPageLength(authorizations.size());
	}

	public abstract class AddAuthorizationButton extends WindowButton {
		@PropertyId("accessRoles") protected ListOptionGroup accessRoles;
		@PropertyId("userRoles") protected ListOptionGroup userRoles;
		@PropertyId("negative") protected ComboBox negative;
		@PropertyId("startDate") protected JodaDateField startDate;
		@PropertyId("endDate") protected JodaDateField endDate;

		public AddAuthorizationButton() {
			super("", "", WindowConfiguration.modalDialog("40%", "65%"));
			String caption = "";
			if (presenter.seeAccessField()) {
				caption = $("ListAccessAuthorizationsView.add");
			} else if (presenter.seeRolesField()) {
				caption = $("ListRoleAuthorizationsView.add");
			} else if (presenter.seeSharedBy()) {
				caption = $("ListContentShareView.addDocument");
			}
			super.setCaption(caption);
			super.setWindowCaption(caption);
			super.setVisible(presenter.isRMModuleActive());
			super.setEnabled(presenter.isRMModuleActive());
		}

		protected void buildAccessField() {
			accessRoles = new ListOptionGroup($("AuthorizationsView.access"));
			for (String accessCode : presenter.getAllowedAccesses()) {
				accessRoles.addItem(accessCode);
				accessRoles.setItemCaption(accessCode, $("AuthorizationsView." + accessCode));
			}
			accessRoles.setEnabled(presenter.seeAccessField());
			accessRoles.setVisible(presenter.seeAccessField());
			accessRoles.setRequired(presenter.seeAccessField());
			accessRoles.setMultiSelect(true);
			accessRoles.setId("accessRoles");
		}

		protected void buildRolesField() {
			userRoles = new ListOptionGroup($("AuthorizationsView.userRoles"));
			for (String roleCode : presenter.getAllowedRoles()) {
				userRoles.addItem(roleCode);
				userRoles.setItemCaption(roleCode, presenter.getRoleTitle(roleCode));
			}
			userRoles.setEnabled(presenter.seeRolesField());
			userRoles.setVisible(presenter.seeRolesField());
			userRoles.setRequired(presenter.seeRolesField());
			userRoles.setMultiSelect(true);
			userRoles.setId("userRoles");
		}

		protected void buildNegativeAuthorizationField() {
			negative = new ComboBox();
			negative.setCaption($("AuthorizationsView.negativeAuthotization"));
			negative.setEnabled(presenter.hasManageSecurityPermission());
			negative.setVisible(presenter.isRecordNotATaxonomyConcept());
			negative.setRequired(presenter.hasManageSecurityPermission());
			negative.setId("negative");
			negative.setNullSelectionAllowed(false);
			negative.addItems(asList($(ENABLE), $(DISABLE), $(SHARED)));
		}

		protected void buildDateFields() {
			startDate = new JodaDateField();
			startDate.setCaption($("AuthorizationsView.startDate"));
			startDate.setId("startDate");

			endDate = new JodaDateField();
			endDate.setCaption($("AuthorizationsView.endDate"));
			endDate.setId("endDate");
		}
	}

	public static abstract class EditAuthorizationButton extends WindowButton {
		private final AuthorizationVO authorization;

		@PropertyId("users") private ListAddRemoveRecordLookupField users;
		@PropertyId("groups") private ListAddRemoveRecordLookupField groups;

		public EditAuthorizationButton(AuthorizationVO authorization) {
			super(EditButton.ICON_RESOURCE, $("ListAuthorizationsView.edit"), true,
					WindowConfiguration.modalDialog("50%", "50%"));
			this.authorization = authorization;
			addStyleName(ValoTheme.BUTTON_BORDERLESS);
		}

		@Override
		protected Component buildWindowContent() {
			buildUsersAndGroupsField();
			return new BaseForm<AuthorizationVO>(authorization, this, users, groups) {
				@Override
				protected void saveButtonClick(AuthorizationVO viewObject)
						throws ValidationException {
					getWindow().close();
					onSaveButtonClicked(viewObject);
				}

				@Override
				protected void cancelButtonClick(AuthorizationVO viewObject) {
					getWindow().close();
				}
			};
		}

		abstract protected void onSaveButtonClicked(AuthorizationVO authorizationVO);

		private void buildUsersAndGroupsField() {
			users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
			users.setItemConverter(new UserIdToCaptionConverter());
			users.setValue(authorization.getUsers());
			users.setVisible(!authorization.getUsers().isEmpty());
			users.setCaption($("AuthorizationsView.users"));
			users.setId("users");

			groups = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
			groups.setItemConverter(new GroupIdToCaptionConverter());
			groups.setValue(authorization.getGroups());
			groups.setVisible(!authorization.getGroups().isEmpty());
			groups.setCaption($("AuthorizationsView.groups"));
			groups.setId("groups");
		}
	}

	public static class Authorizations implements ColumnGenerator {
		public static final String PRINCIPALS = "principal";
		public static final String CONTENT = "content";
		public static final String BOTH = "both";
		public static final String SHARED_BY = "sharedBy";
		public static final String ACCESS = "access";
		public static final String USER_ROLES = "userRoles";
		public static final String START_DATE = "startDate";
		public static final String POSITIVE_OR_NEGATIVE = "positiveOrNegative";
		public static final String END_DATE = "endDate";
		public static final String RECEIVED_FROM_METADATA_LABEL = "receivedFromMetadataLabel";
		public static final String RECEIVED_FROM_RECORD_CAPTION = "receivedFromRecordCaption";
		public static final String BUTTONS = "buttons";
		public static final String SHARE_ACCESS = "shareAccess";

		private final AuthorizationSource source;
		private final DisplayMode mode;
		private boolean seeRolesField;
		private boolean seeAccessField;
		private boolean seeMetadataField;
		private boolean seeSharedBy;
		private Locale currentLocale;
		private final JodaDateToStringConverter converter;
		private boolean isViewOnly;

		public Authorizations(AuthorizationSource source, DisplayMode mode, boolean seeRolesField, boolean seeSharedBy,
							  boolean seeAccessField, boolean isViewOnly, Locale currentLocale) {
			this.source = source;
			this.mode = mode;
			this.seeRolesField = seeRolesField;
			this.seeAccessField = seeAccessField;
			this.seeSharedBy = seeSharedBy;
			this.seeMetadataField = source == AuthorizationSource.INHERITED_FROM_METADATA;
			this.currentLocale = currentLocale;
			this.isViewOnly = isViewOnly;
			converter = new JodaDateToStringConverter();
		}

		public void attachTo(Table table, boolean negativeAuthorizationConfigEnabled) {
			String primary;
			List<String> columnIds = new ArrayList<>();

			if (mode == DisplayMode.CONTENT) {
				table.addGeneratedColumn(CONTENT, this);
				table.setColumnHeader(CONTENT, $("AuthorizationsView.content"));
				primary = CONTENT;
			} else if (mode == DisplayMode.PRINCIPALS) {
				table.addGeneratedColumn(PRINCIPALS, this);
				table.setColumnHeader(PRINCIPALS, $("AuthorizationsView.principals"));
				primary = PRINCIPALS;
			} else {
				table.addGeneratedColumn(PRINCIPALS, this);
				table.setColumnHeader(PRINCIPALS, $("AuthorizationsView.principals"));
				primary = PRINCIPALS;
				table.addGeneratedColumn(CONTENT, this);
				table.setColumnHeader(CONTENT, $("AuthorizationsView.content"));
				columnIds.add(CONTENT);
			}
			table.setColumnExpandRatio(primary, 1);
			if (negativeAuthorizationConfigEnabled && seeAccessField) {
				columnIds.add(POSITIVE_OR_NEGATIVE);
			}
			columnIds.add(primary);

			if (seeAccessField) {
				table.addGeneratedColumn(ACCESS, this);
				table.setColumnHeader(ACCESS, $("AuthorizationsView.access"));
				columnIds.add(ACCESS);
			}

			if (seeSharedBy) {
				table.addGeneratedColumn(SHARED_BY, this);
				table.setColumnHeader(SHARED_BY, $("AuthorizationsView.sharedBy"));
				columnIds.add(SHARED_BY);
			}

			columnIds.addAll(asList(START_DATE, END_DATE, SHARE_ACCESS));

			if (seeRolesField) {
				table.addGeneratedColumn(USER_ROLES, this);
				table.setColumnHeader(USER_ROLES, $("AuthorizationsView.userRoles"));
				columnIds.add(USER_ROLES);
			}

			if (seeMetadataField) {
				table.addGeneratedColumn(RECEIVED_FROM_METADATA_LABEL, this);
				table.setColumnHeader(RECEIVED_FROM_METADATA_LABEL, $("AuthorizationsView.receivedFromMetadata"));

				table.addGeneratedColumn(RECEIVED_FROM_RECORD_CAPTION, this);
				table.setColumnHeader(RECEIVED_FROM_RECORD_CAPTION, $("AuthorizationsView.receivedFromRecord"));
			}

			table.addGeneratedColumn(POSITIVE_OR_NEGATIVE, this);
			table.setColumnHeader(POSITIVE_OR_NEGATIVE, $("AuthorizationsView.type"));

			table.addGeneratedColumn(SHARE_ACCESS, this);
			table.setColumnHeader(SHARE_ACCESS, $("AuthorizationsView.method"));

			table.addGeneratedColumn(START_DATE, this);
			table.setColumnHeader(START_DATE, $("AuthorizationsView.startDate"));

			table.addGeneratedColumn(END_DATE, this);
			table.setColumnHeader(END_DATE, $("AuthorizationsView.endDate"));

			if (source == AuthorizationSource.OWN && !isViewOnly) {
				table.setColumnHeader(BUTTONS, "");
				table.setColumnWidth(BUTTONS, 80);
				columnIds.add(BUTTONS);
			} else if (source == AuthorizationSource.INHERITED_FROM_METADATA) {
				columnIds.add(RECEIVED_FROM_RECORD_CAPTION);
			} else if (source == AuthorizationSource.SHARED && !isViewOnly) {
				table.setColumnHeader(BUTTONS, "");
				table.setColumnWidth(BUTTONS, 80);
				columnIds.add(BUTTONS);
			}

			table.setVisibleColumns(columnIds.toArray());
			table.setWidth("100%");
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			AuthorizationVO authorization = (AuthorizationVO) itemId;
			TaxonomyRecordIdToContextCaptionConverter taxonomyCaptionConverter = new TaxonomyRecordIdToContextCaptionConverter();
			switch ((String) columnId) {
				case CONTENT:
					ReferenceDisplay referenceDisplay = new ReferenceDisplay(authorization.getRecord());
					referenceDisplay.setCaption(taxonomyCaptionConverter.convertToPresentation(authorization.getRecord(), String.class,
							currentLocale));
					return referenceDisplay;
				case PRINCIPALS:
					return buildPrincipalColumn(authorization.getGroups(), authorization.getUsers());
				case ACCESS:
					return buildAccessColumn(authorization.getAccessRoles());
				case USER_ROLES:
					return buildUserRolesColumn(authorization.getUserRoles(), authorization.getUserRolesTitles());
				case RECEIVED_FROM_METADATA_LABEL:
					return authorization.getReceivedFromMetadataLabel();
				case RECEIVED_FROM_RECORD_CAPTION:
					return authorization.getReceivedFromRecordCaption();
				case POSITIVE_OR_NEGATIVE:
					return buildNegativeAuthorizationsColumn(authorization);
				case SHARED_BY:
					return buildSharedByColumn(authorization.getSharedBy());
				case SHARE_ACCESS:
					return buildShareAccessColumn(authorization);
				default:
					LocalDate date = (LocalDate) source.getItem(itemId).getItemProperty(columnId).getValue();
					return converter.convertToPresentation(
							date, String.class, ConstellioUI.getCurrentSessionContext().getCurrentLocale());
			}
		}

		private Object buildNegativeAuthorizationsColumn(AuthorizationVO authorization) {
			if ($(DISABLE).equals(authorization.getNegative())) {
				return new Label($("AuthorizationsView.disable"));
			} else {
				return new Label($("AuthorizationsView.enable"));
			}
		}

		private Object buildShareAccessColumn(AuthorizationVO authorization) {
			if (authorization.getSharedBy() != null) {
				return new Label($("AuthorizationsView.shared"));
			} else {
				return new Label($("AuthorizationsView.accessGiven"));
			}
		}

		private Object buildPrincipalColumn(List<String> groups, List<String> users) {
			final List<ReferenceDisplay> results = new ArrayList<>();
			for (String groupId : groups) {
				results.add(new ReferenceDisplay(groupId, true, new GroupIdToCaptionConverter()));
			}
			for (String userId : users) {
				results.add(new ReferenceDisplay(userId, true, new UserIdToCaptionConverter()));
			}
			return new VerticalLayout(results.toArray(new Component[results.size()]));
		}

		private Object buildSharedByColumn(String users) {
			ReferenceDisplay sharedBy = new ReferenceDisplay(users, true, new UserIdToCaptionConverter());
			return sharedBy;
		}

		private Component buildAccessColumn(List<String> roles) {
			List<String> accesses = new ArrayList<>(3);
			List<String> shortened = new ArrayList<>(3);
			if (roles.contains(Role.READ)) {
				accesses.add($("AuthorizationsView.READ"));
				shortened.add($("AuthorizationsView.short.READ"));
			}
			if (roles.contains(Role.WRITE)) {
				accesses.add($("AuthorizationsView.WRITE"));
				shortened.add($("AuthorizationsView.short.WRITE"));
			}
			if (roles.contains(Role.DELETE)) {
				accesses.add($("AuthorizationsView.DELETE"));
				shortened.add($("AuthorizationsView.short.DELETE"));
			}
			Label label = new Label(StringUtils.join(shortened, "/"));
			label.setDescription(StringUtils.join(accesses, ", "));
			return label;
		}

		private Component buildUserRolesColumn(List<String> roleCodes, List<String> roleTitles) {
			Label label = new Label(StringUtils.join(roleCodes, "/"));
			label.setDescription(StringUtils.join(roleTitles, ", "));
			return label;
		}
	}

	protected boolean canEditAuthorizations() {
		return true;
	}

	@Override
	public void setViewReadOnly(boolean isViewReadOnly) {
		this.isViewReadOnly = isViewReadOnly;
	}

	@Override
	public boolean isViewReadOnly() {
		return isViewReadOnly;
	}

	@Override
	public Record getAutorizationTarget() {
		return record == null ? null : record.getRecord();
	}
}
