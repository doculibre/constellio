package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

public class ShareContentListViewImpl extends ListAuthorizationsViewImpl implements
		ShareContentListView {

	private BreadcrumbTrail breadcrumbTrail;

	public ShareContentListViewImpl() {
		presenter = new ShareContentListPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListContentShareView.viewTitle", record.getTitle());
	}

	@Override
	protected Button buildAddButton() {
		addButton = new ShareContentListViewImpl.AddDocumentShareButton();
		return addButton;
	}

	@Override
	protected Button buildSecondaryAddButton() {
		addSecondButton = new ShareContentListViewImpl.AddFolderShareButton();
		return addSecondButton;
	}

	@Override
	protected DisplayMode getDisplayMode() {
		return DisplayMode.BOTH;
	}

	public class AddDocumentShareButton extends AddAuthorizationButton {
		@PropertyId("users") protected ListAddRemoveRecordLookupField users;
		@PropertyId("groups") protected ListAddRemoveRecordLookupField groups;
		@PropertyId("record") protected LookupRecordField content;

		@Override
		protected Component buildWindowContent() {
			buildRecordField();
			buildUsersAndGroupsField();
			buildAccessField();
			buildRolesField();
			buildDateFields();
			BaseForm authorizationComponent = new BaseForm<AuthorizationVO>(
					 AuthorizationVO.forShare(presenter.getCurrentUserId())
					, this, users, groups, content, accessRoles, userRoles, startDate, endDate) {
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
			((Component) authorizationComponent).setHeight("95%");
			return authorizationComponent;
		}

		private void buildUsersAndGroupsField() {
			users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
			users.setCaption($("AuthorizationsView.users"));
			users.setId("users");

			groups = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
			groups.setCaption($("AuthorizationsView.groups"));
			groups.setId("groups");
		}

		protected void buildRecordField() {
			content = new LookupRecordField(Document.SCHEMA_TYPE);
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

	public class AddFolderShareButton  extends AddAuthorizationButton {
		@PropertyId("users") protected ListAddRemoveRecordLookupField users;
		@PropertyId("groups") protected ListAddRemoveRecordLookupField groups;
		@PropertyId("record") protected LookupRecordField content;

		@Override
		protected Component buildWindowContent() {
			buildRecordField();
			buildUsersAndGroupsField();
			buildAccessField();
			buildRolesField();
			buildDateFields();
			BaseForm authorizationComponent = new BaseForm<AuthorizationVO>(
					AuthorizationVO.forShare(presenter.getCurrentUserId())
					, this, users, groups, content, accessRoles, userRoles, startDate, endDate) {
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
			((Component) authorizationComponent).setHeight("95%");
			return authorizationComponent;
		}

		private void buildUsersAndGroupsField() {
			users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
			users.setCaption($("AuthorizationsView.users"));
			users.setId("users");

			groups = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
			groups.setCaption($("AuthorizationsView.groups"));
			groups.setId("groups");
		}

		protected void buildRecordField() {
			content = new LookupRecordField(Folder.SCHEMA_TYPE);
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

	@Override
	public void setBreadcrumbTrail(BreadcrumbTrail breadcrumbTrail) {
		this.breadcrumbTrail = breadcrumbTrail;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return breadcrumbTrail != null ? (BaseBreadcrumbTrail) breadcrumbTrail : super.buildBreadcrumbTrail();
	}

	@Override
	protected boolean canEditAuthorizations() {
		return false;
	}
}
