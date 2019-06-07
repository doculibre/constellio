package com.constellio.app.ui.pages.collection;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AuthorizationsButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.RolesButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionUserViewImpl extends BaseViewImpl implements CollectionUserView {
	public static final String COMPUTED_USER_ROLES = User.DEFAULT_SCHEMA + "_" + User.ALL_ROLES;
	public static final String USER_ROLES = User.DEFAULT_SCHEMA + "_" + User.ROLES;

	private final CollectionUserPresenter presenter;

	public CollectionUserViewImpl() {
		presenter = new CollectionUserPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forRequestParams(event.getParameters());
	}

	@Override
	protected String getTitle() {
		return $("CollectionUserView.viewTitle");
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
				presenter.deleteButtonClicked();
			}
		};
		delete.setEnabled(presenter.isDeletionEnabled());
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
		RecordVO user = presenter.getUser();
		return new RecordDisplay(user, new UserMetadataDisplayFactory());
	}

	public class UserMetadataDisplayFactory extends MetadataDisplayFactory {
		@Override
		public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
			switch (metadata.getCode()) {
				case COMPUTED_USER_ROLES:
					return new Label(presenter.getRoleTitle((String) displayValue));
				case USER_ROLES:
					return null;
				default:
					return super.buildSingleValue(recordVO, metadata, displayValue);
			}
		}
	}
}
