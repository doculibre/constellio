package com.constellio.app.ui.pages.collection;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AuthorizationsButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.RolesButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
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

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionGroupViewImpl extends BaseViewImpl implements CollectionGroupView {
	public static final String GROUP_ROLES = Group.DEFAULT_SCHEMA + "_" + Group.ROLES;
	public static final String GROUP_CODE = Group.DEFAULT_SCHEMA + "_" + Group.CODE;

	private final CollectionGroupPresenter presenter;
	private RecordVO group;

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
	}

	@Override
	protected String getTitle() {
		return $("CollectionGroupView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);

		if (presenter.isRMModuleEnabled()) {
			Button authorizations = new AuthorizationsButton(false) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.authorizationsButtonClicked();
				}
			};
			buttons.add(authorizations);
		}

		Button roles = new RolesButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.rolesButtonClicked();
			}
		};
		buttons.add(roles);

		Button delete = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				String code = group.get(GROUP_CODE);
				presenter.deleteButtonClicked(code);
			}
		};
		buttons.add(delete);

		return buttons;
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
