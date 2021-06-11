package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionConverter;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AuthorizationsButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.RolesButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.Group;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionGroupViewImpl extends BaseViewImpl implements CollectionGroupView {
	public static final String GROUP_ROLES = Group.DEFAULT_SCHEMA + "_" + Group.ROLES;
	public static final String GROUP_CODE = Group.DEFAULT_SCHEMA + "_" + Group.CODE;

	private final CollectionGroupPresenter presenter;
	private RecordVO group;

	Button roles, authorizations, delete;


	public CollectionGroupViewImpl() {
		presenter = new CollectionGroupPresenter(this);
	}

	public CollectionGroupViewImpl(ViewChangeEvent event) {
		presenter = new CollectionGroupPresenter(this);
		this.buildMainComponent(event);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forRequestParams(event.getParameters());
		buildActionMenuButtons();
	}

	@Override
	protected String getTitle() {
		return $("CollectionGroupView.viewTitle");
	}

	private void buildActionMenuButtons() {
		authorizations = new AuthorizationsButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.authorizationsButtonClicked();
			}
		};
		authorizations.setEnabled(presenter.isRMModuleEnabled());

		roles = new RolesButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.rolesButtonClicked();
			}
		};

		delete = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				String code = group.get(GROUP_CODE);
				presenter.deleteButtonClicked(code);
			}
		};
	}

	@Override
	protected List<MenuItemAction> buildMenuItemActions(ViewChangeEvent event) {
		return ListUtils.flatMapFilteringNull(
				super.buildMenuItemActions(event),
				Stream.of(roles, authorizations, delete).map(MenuItemActionConverter::toMenuItemAction).collect(Collectors.toList())
		);
	}

	@Override
	protected ActionMenuDisplay buildActionMenuDisplay(ActionMenuDisplay defaultActionMenuDisplay) {
		return new ActionMenuDisplay(defaultActionMenuDisplay) {
			@Override
			public Supplier<String> getSchemaTypeCodeSupplier() {
				return presenter.getGroup().getSchema()::getTypeCode;
			}
		};
	}

	@Override
	protected String getActionMenuBarCaption() {
		return super.getActionMenuBarCaption();
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				navigateTo().collectionSecurity();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		group = presenter.getGroup();
		return new RecordDisplay(group, new GroupMetadataDisplayFactory());
	}

	public class GroupMetadataDisplayFactory extends MetadataDisplayFactory {
		@Override
		public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
			switch (metadata.getCode()) {
				case GROUP_ROLES:
					return new Label(presenter.getRoleTitle((String) displayValue));
				default:
					return super.buildSingleValue(recordVO, metadata, displayValue);
			}
		}
	}
}
