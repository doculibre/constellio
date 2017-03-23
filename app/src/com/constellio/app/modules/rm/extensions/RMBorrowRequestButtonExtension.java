package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.decorators.base.ActionMenuButtonsDecorator;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Constellio on 2017-03-16.
 */
public class RMBorrowRequestButtonExtension extends PagesComponentsExtension {

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
        if(mainComponent instanceof DisplayFolderViewImpl) {
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
        }
    }

    private User getCurrentUser(BaseView view) {
        BasePresenterUtils basePresenterUtils = new BasePresenterUtils(view.getConstellioFactories(), view.getSessionContext());
        return basePresenterUtils.getCurrentUser();
    }

    private Button buildRequestBorrowButton(final BaseViewImpl view) {
        return new BaseButton($("Borrow")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                ConfirmDialog.show(
                        UI.getCurrent(),
                        $("DisplayFolderViewImpl.requestBorrowButtonTitle"),
                        $("DisplayFolderViewImpl.requestBorrowButtonMessage"),
                        $("container"),
                        $("cancel"),
                        $("folder"),
                        new ConfirmDialog.Listener() {
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    borrowContainerRequested(view);
                                } else if (dialog.isCanceled()) {
                                    return;
                                } else {
                                    borrowFolderRequested(view);
                                }
                            }
                        });
            }
        };
    }

    private Button buildRequestReturnButton(final BaseViewImpl view) {
        return new BaseButton($("Return")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                returnFolderRequested(view);
            }
        };
    }

    private Button buildRequestReactivationButton(final BaseViewImpl view) {
        return new BaseButton($("Reactivation")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                reactivationRequested(view);
            }
        };
    }

    private Button buildRequestBorrowExtensionButton(final BaseViewImpl view) {
        return new BaseButton($("Extension")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                borrowExtensionRequested(view);
            }
        };
    }

    public void borrowContainerRequested(BaseViewImpl view) {

    }

    public void borrowFolderRequested(BaseViewImpl view){
        try {
            if(view instanceof DisplayFolderViewImpl) {
                DisplayFolderViewImpl displayFolderView = (DisplayFolderViewImpl) view;
                Task borrowRequest = taskSchemas.newBorrowFolderRequestTask(getCurrentUser(view).getId(), displayFolderView.getRecord().getId());
                modelLayerFactory.newRecordServices().add(borrowRequest);
            }
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    public void returnFolderRequested(BaseViewImpl view){
        try {
            if(view instanceof DisplayFolderViewImpl) {
                DisplayFolderViewImpl displayFolderView = (DisplayFolderViewImpl) view;
                Task returnRequest = taskSchemas.newReturnFolderRequestTask(getCurrentUser(view).getId(), displayFolderView.getRecord().getId());
                modelLayerFactory.newRecordServices().add(returnRequest);
            }
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    public void reactivationRequested(BaseViewImpl view){
        try {
            if(view instanceof DisplayFolderViewImpl) {
                DisplayFolderViewImpl displayFolderView = (DisplayFolderViewImpl) view;
                Task reactivationRequest = taskSchemas.newReactivateFolderRequestTask(getCurrentUser(view).getId(), displayFolderView.getRecord().getId());
                modelLayerFactory.newRecordServices().add(reactivationRequest);
            }
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    public void borrowExtensionRequested(BaseViewImpl view){
        try {
            if(view instanceof DisplayFolderViewImpl) {
                DisplayFolderViewImpl displayFolderView = (DisplayFolderViewImpl) view;
                Task borrowExtensionRequest = taskSchemas.newBorrowFolderExtensionRequestTask(getCurrentUser(view).getId(), displayFolderView.getRecord().getId());
                modelLayerFactory.newRecordServices().add(borrowExtensionRequest);
            }
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }
}
