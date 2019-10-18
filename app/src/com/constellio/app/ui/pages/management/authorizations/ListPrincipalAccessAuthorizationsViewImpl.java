package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.framework.components.BaseForm.CANCEL_BUTTON;
import static com.constellio.app.ui.framework.components.BaseForm.SAVE_BUTTON;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

public class ListPrincipalAccessAuthorizationsViewImpl extends ListAuthorizationsViewImpl implements
		ListPrincipalAccessAuthorizationsView {

	public ListPrincipalAccessAuthorizationsViewImpl() {
		presenter = new ListPrincipalAccessAuthorizationsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListPrincipalAccessAuthorizationsView.viewTitle", record.getTitle());
	}

	@Override
	protected Button buildAddButton() {
		return new AddPrincipalAuthorizationButton();
	}

	@Override
	protected Button buildAddAccessButton() {
		return new AddPrincipalAccessButton();
	}

	@Override
	protected DisplayMode getDisplayMode() {
		return DisplayMode.CONTENT;
	}

	@Override
	protected void buildGlobalAccess(VerticalLayout layout) {
		ListPrincipalAccessAuthorizationsPresenter accessPresenter = (ListPrincipalAccessAuthorizationsPresenter) presenter;
		List<String> globalAccesses = accessPresenter.getUserGlobalAccess();
		if (!globalAccesses.isEmpty()) {
			Label label = new Label();
			if (presenter.seeAccessField()) {
				label.setValue($("ListAccessAuthorizationsView.globalAccesses"));
			}

			label.addStyleName(ValoTheme.LABEL_H2);
			layout.addComponent(label);
			StringBuilder accesses = new StringBuilder();
			for (String access : globalAccesses) {
				if (accesses.length() > 0) {
					accesses.append(", ");
				}
				accesses.append($("AuthorizationsView." + access));
			}
			if (accesses.length() > 0) {
				layout.addComponent(new Label(accesses.toString()));
			}
		}

	}

	public class AddPrincipalAuthorizationButton extends AddAuthorizationButton {
		@PropertyId("record") protected LookupRecordField content;

		@Override
		protected Component buildWindowContent() {
			buildRecordField();
			buildAccessField();
			buildRolesField();
			buildDateFields();
			return new BaseForm<AuthorizationVO>(
					AuthorizationVO.forUsers(record.getId()), this, content, accessRoles, userRoles, startDate, endDate) {
				@Override
				protected void saveButtonClick(AuthorizationVO authorization)
						throws ValidationException {
					getWindow().close();
					presenter.authorizationCreationRequested(authorization);
				}

				@Override
				protected void cancelButtonClick(AuthorizationVO authorization) {
					getWindow().close();
				}
			};

		}

		protected void buildRecordField() {
			content = new LookupRecordField(AdministrativeUnit.SCHEMA_TYPE);
			content.setCaption($("AuthorizationsView.content"));
			content.setRequired(true);
			content.setId("content");
		}

		@Override
		public boolean isVisible() {
			return super.isVisible() && !isViewReadOnly();
		}

		@Override
		public boolean isEnabled() {
			return super.isVisible() && !isViewReadOnly();
		}
	}

	public class AddPrincipalAccessButton extends WindowButton {
		@PropertyId("accessRoles") protected ListOptionGroup accessRoles;

		public AddPrincipalAccessButton() {
			super("", "", WindowConfiguration.modalDialog("550px", "350px"));
			String caption = $("ListPrincipalAccessAuthorizationsView.addAccess");
			super.setCaption(caption);
			super.setWindowCaption(caption);
			ListPrincipalAccessAuthorizationsPresenter accessPresenter = (ListPrincipalAccessAuthorizationsPresenter) presenter;
			setVisible(accessPresenter.seeCollectionAccessField());
		}

		protected void buildAccessField() {
			accessRoles = new ListOptionGroup($("AuthorizationsView.access"));
			ListPrincipalAccessAuthorizationsPresenter accessPresenter = (ListPrincipalAccessAuthorizationsPresenter) presenter;
			for (String accessCode : accessPresenter.getCollectionAccessChoicesModifiableByCurrentUser()) {
				accessRoles.addItem(accessCode);
				accessRoles.setValue(presenter.hasUserAccess(accessCode));
				accessRoles.setItemCaption(accessCode, $("AuthorizationsView." + accessCode));
			}
			accessRoles.setMultiSelect(true);
			accessRoles.setValue(accessPresenter.getUserGlobalAccess());

			accessRoles.setId("accessRoles");
		}

		@Override
		protected Component buildWindowContent() {
			VerticalLayout vLayout = new VerticalLayout();
			vLayout.setSizeFull();
			Label warningLabel = new Label("<p style=\"color:red\">" + $("CollectionUserRolesView.onCollectionWarning") + "</p>", ContentMode.HTML);
			vLayout.addComponent(warningLabel);
			buildAccessField();
			vLayout.addComponent(accessRoles);
			Button saveButton = new Button($("save"));
			saveButton.addStyleName(SAVE_BUTTON);
			saveButton.addStyleName(BUTTON_PRIMARY);
			saveButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					ListPrincipalAccessAuthorizationsPresenter accessPresenter = (ListPrincipalAccessAuthorizationsPresenter) presenter;
					Object accessModified = accessRoles.getValue();
					getWindow().close();
					accessPresenter.accessCreationRequested(new ArrayList<>((Set<String>) accessModified));
				}
			});

			Button cancelButton = new Button($("cancel"));
			cancelButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					getWindow().close();
				}
			});
			cancelButton.addStyleName(CANCEL_BUTTON);
			HorizontalLayout buttonsLayout = new HorizontalLayout();
			buttonsLayout.addStyleName(BUTTONS_LAYOUT);
			buttonsLayout.setSpacing(true);

			buttonsLayout.addComponents(saveButton, cancelButton);
			vLayout.addComponent(buttonsLayout);
			vLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);
			return vLayout;
		}
	}

	@Override
	protected boolean canEditAuthorizations() {
		return false;
	}
}
