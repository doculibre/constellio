package com.constellio.app.ui.pages.collection;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RoleAuthVO;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionGroupRolesViewImpl extends BaseViewImpl implements CollectionGroupRolesView {
	public static final String GROUP_ROLES = Group.DEFAULT_SCHEMA + "_" + Group.ROLES;
	public static final String ADD_ROLE = "add-role";
	public static final String ROLE_SELECTOR = "role-selector";
	public static final String ROLES = "roles";

	private final CollectionGroupRolesPresenter presenter;
	private RecordVO group;
	@PropertyId("roles") private OptionGroup availableRolesField;
	@PropertyId("target") private Field targetField;

	private Table inheritedRolesTable;
	private Table specificRolesTable;
	private VerticalLayout layout;

	public CollectionGroupRolesViewImpl() {
		presenter = new CollectionGroupRolesPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forRequestParams(event.getParameters());
		group = presenter.getGroup();
	}

	@Override
	protected String getTitle() {
		return $("CollectionGroupRolesView.viewTitle", group.getTitle());
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
	protected Component buildMainComponent(ViewChangeEvent event) {
		buildInheritedRolesTable();
		specificRolesTable = buildSpecificRolesTable();
		availableRolesField = new ListOptionGroup($("CollectionGroupRolesView.rolesField"));
		availableRolesField.setMultiSelect(true);
		availableRolesField.setId("roles");
		for (RoleVO role : presenter.getRoles()) {
			availableRolesField.addItem(role.getCode());
			availableRolesField.setItemCaption(role.getCode(), role.getTitle());
		}

		layout = new VerticalLayout(inheritedRolesTable, specificRolesTable);
		layout.setSpacing(true);

		return layout;
	}

	private void buildInheritedRolesTable() {
		inheritedRolesTable = new BaseTable(getClass().getName() + ".inherited", $("CollectionGroupRolesView.inheritedRolesTable"));
		BeanItemContainer<RoleAuthVO> container = new BeanItemContainer<>(RoleAuthVO.class);
		List<RoleAuthVO> userRoles = presenter.getInheritedRoles();
		for (RoleAuthVO roleAuth : userRoles) {
			container.addItem(roleAuth);
		}
		inheritedRolesTable.setContainerDataSource(container);
		inheritedRolesTable.addStyleName(ROLES);
		inheritedRolesTable.setWidth("100%");
		inheritedRolesTable.setPageLength(inheritedRolesTable.size());
		inheritedRolesTable.setVisibleColumns(RoleDisplay.ROLES, RoleDisplay.TARGET);
		new RoleDisplay().attachTo(inheritedRolesTable);
	}

	private Table buildSpecificRolesTable() {
		Table table = new BaseTable(getClass().getName() + ".specific", $("CollectionGroupRolesView.specificRolesTable"));
		BeanItemContainer<RoleAuthVO> container = new BeanItemContainer<>(RoleAuthVO.class);
		ButtonsContainer<BeanItemContainer> buttonsContainer = new ButtonsContainer<BeanItemContainer>(container, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteRoleButtonClicked((RoleAuthVO) itemId);
					}
				};
			}
		});
		List<RoleAuthVO> userRoles = presenter.getSpecificRoles();
		for (RoleAuthVO roleAuth : userRoles) {
			container.addItem(roleAuth);
		}
		table.setContainerDataSource(buttonsContainer);
		table.setColumnHeader("buttons", "");
		table.addStyleName(ROLES);
		table.setWidth("100%");
		table.setPageLength(table.size());
		table.setVisibleColumns(RoleDisplay.ROLES, RoleDisplay.TARGET, "buttons");
		new RoleDisplay().attachTo(table);
		return table;
	}

	public void refreshTable() {
		Table newSpecificRolesTable = buildSpecificRolesTable();
		layout.replaceComponent(specificRolesTable, newSpecificRolesTable);
		specificRolesTable = newSpecificRolesTable;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		Button windowButton = new WindowButton($("CollectionGroupRolesView.addRoleButton"),
				$("CollectionGroupRolesView.addRoleWindowTitle"), WindowConfiguration.modalDialog("900px", "450px")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout mainLayout = new VerticalLayout();

				if (presenter.isRMModuleEnabled()) {
					targetField = new LookupRecordField(presenter.getPrincipalTaxonomySchemaCode());
					targetField.setCaption($("CollectionGroupRolesView.targetField"));
				} else {
					targetField = new TextField();
					targetField.setVisible(false);
				}

				final Label warningLabel = new Label("<p style=\"color:red\">" + $("CollectionUserRolesView.onCollectionWarning") + "</p>", ContentMode.HTML);
				warningLabel.setReadOnly(true);
				warningLabel.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
				targetField.addValueChangeListener(new Property.ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						warningLabel.setVisible(presenter.isRMModuleEnabled() && event.getProperty().getValue() == null);
					}
				});

				BaseForm<RoleAuthVO> form = new BaseForm<RoleAuthVO>(presenter.newRoleAuthVO(), CollectionGroupRolesViewImpl.this, availableRolesField,
						targetField) {
					@Override
					protected void saveButtonClick(RoleAuthVO viewObject)
							throws ValidationException {
						presenter.addRoleButtonClicked(viewObject);
						getWindow().close();
					}

					@Override
					protected void cancelButtonClick(RoleAuthVO viewObject) {
						getWindow().close();
					}
				};
				mainLayout.addComponents(warningLabel, form);
				return mainLayout;
			}
		};
		return Arrays.asList(windowButton);
	}

	public class RoleDisplay implements ColumnGenerator {
		public static final String ROLES = "roles";
		public static final String TARGET = "target";

		public void attachTo(Table table) {
			table.addGeneratedColumn(ROLES, this);
			table.setColumnHeader(ROLES, $("CollectionGroupRolesView.rolesHeader"));
			table.setColumnExpandRatio(ROLES, 1);
			table.addGeneratedColumn(TARGET, this);
			table.setColumnHeader(TARGET, $("CollectionGroupRolesView.targetHeader"));
			table.setColumnExpandRatio(TARGET, 1);
		}

		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			if (columnId.equals(ROLES)) {
				List<Label> results = new ArrayList<>();
				for (String roleCode : ((RoleAuthVO) itemId).getRoles()) {
					results.add(new Label(presenter.getRoleTitle(roleCode)));
				}
				return new VerticalLayout(results.toArray(new Component[results.size()]));
			}
			if (columnId.equals(TARGET)) {
				String target = ((RoleAuthVO) itemId).getTarget();
				if (target != null) {
					return new ReferenceDisplay(target);
				} else {
					return $("CollectionGroupRolesView.global");
				}
			}
			return null;
		}
	}
}
