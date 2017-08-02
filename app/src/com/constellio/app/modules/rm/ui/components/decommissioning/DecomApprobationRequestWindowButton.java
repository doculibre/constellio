package com.constellio.app.modules.rm.ui.components.decommissioning;

import com.constellio.app.modules.rm.services.decommissioning.DecommissioningEmailServiceException;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListPresenter;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListView;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListViewImpl;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField;
import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import javax.ws.rs.NotSupportedException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.i18n.i18n.$;

public class DecomApprobationRequestWindowButton extends WindowButton {
    private DecommissioningListPresenter presenter;

    private ListAddRemoveField users;
    private TextArea comments;
    private CheckBox checkBox;
    private ConstellioFactories constellioFactories;
    DecommissioningListView decommissioningListView;

    public DecomApprobationRequestWindowButton(DecommissioningListPresenter presenter, DecommissioningListViewImpl decommissioningListView, ConstellioFactories constellioFactories) {
        super(i18n.$("DecommissioningListView.approvalRequest"),
                i18n.$("DecommissioningListView.approvalRequest.windowCaption"));
        this.presenter = presenter;
        this.constellioFactories = constellioFactories;
        this.decommissioningListView = decommissioningListView;
    }

    @Override
    protected Component buildWindowContent() {
        final VerticalLayout layout = new VerticalLayout();
        List<User> managers = new ArrayList<>();
        try {
            managers = presenter.getAdministrativeUnitAdmin();
        } catch (DecommissioningEmailServiceException e) {
            decommissioningListView.showErrorMessage($("DecommissioningListView.noManagerAvalible"));
        }

        final List<User> managersFinal = managers;

        this.users = new ListAddRemoveField() {
            @Override
            protected AbstractField<?> newAddEditField() {
                return new LookupField<User>(new UserTextInputdataProvider(managersFinal), new UserLookupTreeDataProvider(managersFinal)) {
                    @Override
                    public Class<? extends User> getType() {
                        return User.class;
                    }
                };
            }
        };
        this.users.setItemConverter(new RecordIdToCaptionConverter());

        layout.setSpacing(true);
        users.setCaption($("DecomAskForValidationWindowButton.usersCaption"));
        users.setRequired(true);
        //users.setRequiredError($("DecomAskForValidationWindowButton.error.users"));
        layout.addComponent(users);

        BaseButton sendButton = new BaseButton($("DecomAskForValidationWindowButton.okButton")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                if (users.getValue().size() > 0) {
                    presenter.approvalRequestButtonClicked(users.getValue());
                    getWindow().close();
                }
            }
        };
        sendButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.addComponent(sendButton);

        BaseButton cancelButton = new BaseButton($("DecomAskForValidationWindowButton.cancelButton")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                getWindow().close();
            }
        };
        buttonsLayout.addComponent(cancelButton);
        buttonsLayout.setSpacing(true);
        layout.addComponent(buttonsLayout);
        layout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_RIGHT);
        getWindow().setHeight("220px");
        return layout;
    }

    class UserLookupTreeDataProvider implements LookupField.LookupTreeDataProvider {
        private List<User> userList;

        public UserLookupTreeDataProvider(List<User> userList) {
            this.userList = userList;
        }

        @Override
        public LookupField.TextInputDataProvider<User> search() {
            return new UserTextInputdataProvider(userList);
        }

        @Override
        public LookupField.TextInputDataProvider<User> searchWithoutDisabled() {
            throw new NotSupportedException();
        }

        @Override
        public boolean isSelectable(Serializable selection) {
            return true;
        }

        @Override
        public ObjectsResponse<User> getRootObjects(int start, int maxSize) {
            return null;
        }

        @Override
        public Serializable getParent(Serializable child) {
            return null;
        }

        @Override
        public ObjectsResponse getChildren(Serializable parent, int start, int maxSize) {
            return null;
        }

        @Override
        public boolean hasChildren(Serializable parent) {
            return false;
        }

        @Override
        public boolean isLeaf(Serializable object) {
            return false;
        }

        @Override
        public String getTaxonomyCode() {
            return null;
        }

        @Override
        public String getCaption(Serializable id) {
            return null;
        }

        @Override
        public String getDescription(Serializable id) {
            return null;
        }

        @Override
        public Resource getIcon(Serializable id, boolean expanded) {
            return null;
        }

        @Override
        public int getEstimatedRootNodesCount() {
            return 0;
        }

        @Override
        public int getEstimatedChildrenNodesCount(Serializable parent) {
            return 0;
        }

        @Override
        public void addDataRefreshListener(DataRefreshListener dataRefreshListener) {

        }

        @Override
        public List<DataRefreshListener> getDataRefreshListeners() {
            return null;
        }

        @Override
        public void removeDataRefreshListener(DataRefreshListener dataRefreshListener) {

        }

        @Override
        public void fireDataRefreshEvent() {

        }
    }

    class UserTextInputdataProvider extends LookupField.TextInputDataProvider<User> {
        List<User> listUser;

        public UserTextInputdataProvider(List<User> users) {
            super();
            listUser = users;
        }

        @Override
        public List<User> getData(String text, int startIndex, int count) {
            return search(text).subList(startIndex, count);
        }

        public List<User> search(String text) {
            List<User> managersToReturn = new ArrayList<>();

            if (text.isEmpty()) {
                return listUser;
            } else {
                for (User user : listUser) {
                    if ((user.getFirstName() + " " + user.getLastName()).contains(text)) {
                        managersToReturn.add(user);
                    }
                }

                return managersToReturn;
            }
        }

        @Override
        public ModelLayerFactory getModelLayerFactory() {
            return getModelLayerFactory();
        }

        @Override
        public int size(String text) {
            return search(text).size();
        }

        @Override
        public User getCurrentUser() {
            return getCurrentUser();
        }

        @Override
        public void setOnlyLinkables(boolean onlyLinkables) {
            // Ignore
        }
    }
}

