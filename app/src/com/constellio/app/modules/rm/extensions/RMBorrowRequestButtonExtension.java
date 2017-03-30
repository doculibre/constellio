package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.decorators.base.ActionMenuButtonsDecorator;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.*;
import org.joda.time.LocalDate;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Constellio on 2017-03-16.
 */
public class RMBorrowRequestButtonExtension extends PagesComponentsExtension {

    enum RequestType {
        EXTENSION, BORROW
    }

    String collection;
    AppLayerFactory appLayerFactory;
    ModelLayerFactory modelLayerFactory;
    TasksSchemasRecordsServices taskSchemas;

    public RMBorrowRequestButtonExtension(String collection, AppLayerFactory appLayerFactory) {
        this.collection = collection;
        this.appLayerFactory = appLayerFactory;
        this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
        this.taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
    }

    @Override
    public void decorateMainComponentBeforeViewAssembledOnViewEntered(DecorateMainComponentAfterInitExtensionParams params) {
        super.decorateMainComponentAfterViewAssembledOnViewEntered(params);
        Component mainComponent = params.getMainComponent();
        if (mainComponent instanceof DisplayFolderViewImpl) {
            DisplayFolderViewImpl view = (DisplayFolderViewImpl) mainComponent;
            view.addActionMenuButtonsDecorator(new ActionMenuButtonsDecorator() {
                @Override
                public void decorate(final BaseViewImpl view, List<Button> actionMenuButtons) {
                    actionMenuButtons.add(buildRequestBorrowButton(view));
                    actionMenuButtons.add(buildRequestReturnButton(view));
                    actionMenuButtons.add(buildRequestReactivationButton(view));
                    actionMenuButtons.add(buildRequestBorrowExtensionButton(view));
                }
            });
        } else if (mainComponent instanceof DisplayContainerViewImpl) {
            DisplayContainerViewImpl view = (DisplayContainerViewImpl) mainComponent;
            view.addActionMenuButtonsDecorator(new ActionMenuButtonsDecorator() {
                @Override
                public void decorate(final BaseViewImpl view, List<Button> actionMenuButtons) {
                    actionMenuButtons.add(buildRequestBorrowButton(view));
                    actionMenuButtons.add(buildRequestReturnButton(view));
                    actionMenuButtons.add(buildRequestReactivationButton(view));
                    actionMenuButtons.add(buildRequestBorrowExtensionButton(view));
                }
            });
        }
    }

    private User getCurrentUser(BaseView view) {
        BasePresenterUtils basePresenterUtils = new BasePresenterUtils(view.getConstellioFactories(), view.getSessionContext());
        return basePresenterUtils.getCurrentUser();
    }

    private Button buildRequestBorrowButton(final BaseViewImpl view) {
        if (view instanceof DisplayFolderViewImpl) {
            if(((DisplayFolderViewImpl) view).getRecord().get(Folder.CONTAINER) != null) {
                return new BaseButton($("DisplayFolderView.borrow")) {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        ConfirmDialog.show(
                                UI.getCurrent(),
                                $("DisplayFolderViewImpl.requestBorrowButtonTitle"),
                                $("DisplayFolderViewImpl.requestBorrowButtonMessage"),
                                $("cancel"),
                                $("DisplayFolderView.borrowContainerInstead"),
                                $("DisplayFolderView.confirmBorrowFolder"),
                                new ConfirmDialog.Listener() {
                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            borrowRequest(view, true);
                                        } else if (dialog.isCanceled()) {
                                            return;
                                        } else {
                                            borrowRequest(view, false);
                                        }
                                    }
                                }).setWidth("55%");

                    }

                    @Override
                    public boolean isVisible() {
                        return !isRecordBorrowed(view);
                    }
                };
            } else {
                return new BaseButton($("DisplayFolderView.borrow")) {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        ConfirmDialog.show(
                                UI.getCurrent(),
                                $("DisplayFolderViewImpl.requestBorrowButtonTitle"),
                                $("DisplayFolderViewImpl.requestBorrowButtonMessage"),
                                $("DisplayFolderView.confirmBorrowFolder"),
                                $("cancel"),
                                new ConfirmDialog.Listener() {
                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            borrowRequest(view, true);
                                        } else if (dialog.isCanceled()) {
                                            return;
                                        } else {
                                            borrowRequest(view, false);
                                        }
                                    }
                                }).setWidth("55%");

                    }

                    @Override
                    public boolean isVisible() {
                        return !isRecordBorrowed(view);
                    }
                };
            }
        } else if (view instanceof DisplayContainerViewImpl) {
            return new ConfirmDialogButton($("DisplayFolderView.borrow")) {

                @Override
                protected String getConfirmDialogMessage() {
                    return $("DisplayContainerViewImpl.confirmBorrowContainer");
                }

                @Override
                protected void confirmButtonClick(ConfirmDialog dialog) {
                    borrowRequest(view, true);
                }

                @Override
                public boolean isVisible() {
                    return !isRecordBorrowed(view);
                }
            };
        } else throw new UnsupportedOperationException();
    }

    private Button buildRequestReturnButton(final BaseViewImpl view) {
        return new ConfirmDialogButton($("DocumentActionsComponent.checkIn")) {

            @Override
            protected String getConfirmDialogMessage() {
                return $("DisplayFolderView.confirmReturnMessage");
            }

            @Override
            protected void confirmButtonClick(ConfirmDialog dialog) {
                returnRequest(view);
            }

            @Override
            public boolean isVisible() {
                return isRecordBorrowed(view);
            }
        };
    }

    private Button buildRequestReactivationButton(final BaseViewImpl view) {
        return new ConfirmDialogButton($("DisplayFolderView.reactivation")) {

            @Override
            protected String getConfirmDialogMessage() {
                return $("DisplayFolderView.confirmReactivationMessage");
            }

            @Override
            protected void confirmButtonClick(ConfirmDialog dialog) {
                reactivationRequested(view);
            }

            @Override
            public boolean isVisible() {
                return isRecordReactivable(view);
            }
        };
    }

    private Button buildRequestBorrowExtensionButton(final BaseViewImpl view) {
        return new WindowButton($("DisplayFolderView.extension"), $("Extension")) {
            @PropertyId("value")
            private InlineDateField datefield;

            @Override
            protected Component buildWindowContent() {
                Label dateLabel = new Label($("DisplayFolderView.chooseDateToExtends"));

                datefield = new InlineDateField();
                List<BaseForm.FieldAndPropertyId> fields = Collections.singletonList(new BaseForm.FieldAndPropertyId(datefield, "value"));
                Request req = new Request(new Date(), RequestType.EXTENSION);
                BaseForm form = new BaseForm<Request>(req, this, datefield) {

                    @Override
                    protected void saveButtonClick(Request viewObject) throws ValidationException {
                        viewObject.setValue(datefield.getValue());
                        borrowExtensionRequested(view, viewObject);
                        getWindow().close();
                    }

                    @Override
                    protected void cancelButtonClick(Request viewObject) {
                        getWindow().close();
                    }
                };
                return form;
            }

            public void setDatefield(InlineDateField datefield) {
                this.datefield = datefield;
            }

            public InlineDateField getDatefield() {
                return this.datefield;
            }

            @Override
            public boolean isVisible() {
                return isRecordBorrowed(view);
            }
        };
    }

    public void borrowRequest(BaseViewImpl view, boolean isContainer) {
        try {
            if (view instanceof DisplayFolderViewImpl) {
                DisplayFolderViewImpl displayFolderView = (DisplayFolderViewImpl) view;
                Task borrowRequest;
                if(isContainer) {
                    borrowRequest = taskSchemas.newBorrowContainerRequestTask(getCurrentUser(view).getId(), (String) displayFolderView.getRecord().get(Folder.CONTAINER));
                } else {
                    borrowRequest = taskSchemas.newBorrowFolderRequestTask(getCurrentUser(view).getId(), displayFolderView.getRecord().getId());
                }
                modelLayerFactory.newRecordServices().add(borrowRequest);
            } else if (view instanceof DisplayContainerViewImpl) {
                DisplayContainerViewImpl displayContainerView = (DisplayContainerViewImpl) view;
                Task borrowRequest = taskSchemas.newBorrowContainerRequestTask(getCurrentUser(view).getId(), displayContainerView.getPresenter().getContainerId());
                modelLayerFactory.newRecordServices().add(borrowRequest);
            } else throw new UnsupportedOperationException("invalid view : " + view.getClass().getCanonicalName());
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    public void returnRequest(BaseViewImpl view) {
        try {
            if (view instanceof DisplayFolderViewImpl) {
                DisplayFolderViewImpl displayFolderView = (DisplayFolderViewImpl) view;
                Task returnRequest = taskSchemas.newReturnFolderRequestTask(getCurrentUser(view).getId(), displayFolderView.getRecord().getId());
                modelLayerFactory.newRecordServices().add(returnRequest);
            } else if (view instanceof DisplayContainerViewImpl) {
                DisplayContainerViewImpl displayFolderView = (DisplayContainerViewImpl) view;
                Task returnRequest = taskSchemas.newReturnContainerRequestTask(getCurrentUser(view).getId(), displayFolderView.getPresenter().getContainerId());
                modelLayerFactory.newRecordServices().add(returnRequest);
            } else throw new UnsupportedOperationException("invalid view : " + view.getClass().getCanonicalName());
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    public void reactivationRequested(BaseViewImpl view) {
        try {
            if (view instanceof DisplayFolderViewImpl) {
                DisplayFolderViewImpl displayFolderView = (DisplayFolderViewImpl) view;
                Task reactivationRequest = taskSchemas.newReactivateFolderRequestTask(getCurrentUser(view).getId(), displayFolderView.getRecord().getId());
                modelLayerFactory.newRecordServices().add(reactivationRequest);
            } else if (view instanceof DisplayContainerViewImpl) {
                DisplayContainerViewImpl displayContainerView = (DisplayContainerViewImpl) view;
                Task reactivationRequest = taskSchemas.newReactivationContainerRequestTask(getCurrentUser(view).getId(), displayContainerView.getPresenter().getContainerId());
                modelLayerFactory.newRecordServices().add(reactivationRequest);
            } else throw new UnsupportedOperationException("invalid view : " + view.getClass().getCanonicalName());
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    public void borrowExtensionRequested(BaseViewImpl view, Request req) {
        try {
            if (view instanceof DisplayFolderViewImpl) {
                DisplayFolderViewImpl displayFolderView = (DisplayFolderViewImpl) view;
                Task borrowExtensionRequest = taskSchemas.newBorrowFolderExtensionRequestTask(getCurrentUser(view).getId(), displayFolderView.getRecord().getId(), new LocalDate(req.getValue()));
                modelLayerFactory.newRecordServices().add(borrowExtensionRequest);
            } else if (view instanceof DisplayContainerViewImpl) {
                DisplayContainerViewImpl displayContainerView = (DisplayContainerViewImpl) view;
                Task borrowExtensionRequest = taskSchemas.newBorrowContainerExtensionRequestTask(getCurrentUser(view).getId(), displayContainerView.getPresenter().getContainerId(), new LocalDate(req.getValue()));
                modelLayerFactory.newRecordServices().add(borrowExtensionRequest);
            } else throw new UnsupportedOperationException("invalid view : " + view.getClass().getCanonicalName());
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    private boolean isRecordBorrowed(BaseView view) {
        if (view instanceof DisplayFolderViewImpl) {
            DisplayFolderViewImpl displayView = (DisplayFolderViewImpl) view;
            Folder f = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory).getFolder(displayView.getRecord().getId());
            return Boolean.TRUE.equals(f.getBorrowed()) && f.getBorrowUser().equals(getCurrentUser(view).getId()) && getCurrentUser(view).has(RMPermissionsTo.BORROW_FOLDER).on(f);
        } else if (view instanceof DisplayContainerViewImpl) {
            DisplayContainerViewImpl displayView = (DisplayContainerViewImpl) view;
            ContainerRecord c = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory).getContainerRecord(displayView.getPresenter().getContainerId());
            return Boolean.TRUE.equals(c.getBorrowed()) && getCurrentUser(view).has(RMPermissionsTo.BORROW_FOLDER).on(c);
        }
        return false;
    }

    private boolean isRecordReactivable(BaseView view) {
        if (view instanceof DisplayFolderViewImpl) {
            DisplayFolderViewImpl displayView = (DisplayFolderViewImpl) view;
            Folder f = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory).getFolder(displayView.getRecord().getId());
            return f.getArchivisticStatus().isActiveOrSemiActive() && f.getMediaType().equals(FolderMediaType.ANALOG) && getCurrentUser(view).has(RMPermissionsTo.BORROW_FOLDER).on(f);
        } else if (view instanceof DisplayContainerViewImpl) {
            //TODO
            DisplayContainerViewImpl displayView = (DisplayContainerViewImpl) view;
            ContainerRecord c = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory).getContainerRecord(displayView.getPresenter().getContainerId());
            return Boolean.TRUE.equals(c.getBorrowed()) && getCurrentUser(view).has(RMPermissionsTo.BORROW_FOLDER).on(c);
        }
        return false;
    }

    public class Request {

        @PropertyId("value")
        public Object value;

        @PropertyId("type")
        public RequestType type;

        public Request() {

        }

        public Request(Object value, RequestType requestType) {
            this.type = requestType;
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public Request setValue(Object value) {
            this.value = value;
            return this;
        }

        public RequestType getType() {
            return type;
        }

        public Request setType(RequestType type) {
            this.type = type;
            return this;
        }
    }
}
