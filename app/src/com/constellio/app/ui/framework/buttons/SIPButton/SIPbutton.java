package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.SIPForm;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.pages.SIP.BagInfoSIPForm;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.jdom2.JDOMException;
import org.joda.time.LocalDateTime;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class SIPbutton extends WindowButton {


    private List<RecordVO> objectList = new ArrayList<>();

    private ConstellioHeader view;
    private AppLayerFactory factory;
    private String collection;
    private SIPButtonPresenter presenter;

    public SIPbutton(String caption, String windowCaption, ConstellioHeader view) {
        super(caption, windowCaption, new WindowConfiguration(true, true, "75%", "75%"));
        this.view = view;
        this.presenter = new SIPButtonPresenter(this, objectList);
        if (this.view != null) {
            this.factory = this.view.getConstellioFactories().getAppLayerFactory();
            this.collection = this.view.getCollection();
            User user = this.view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices().getUserInCollection(this.view.getSessionContext().getCurrentUser().getUsername(), this.view.getCollection());
            if (!user.has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally()) {
                super.setVisible(false);
            }
        }
    }

    @Override
    protected Component buildWindowContent() {
        return new BagInfoSIPForm() {
            @Override
            protected void saveButtonClick(BagInfoVO viewObject) throws ValidationException {
                presenter.saveButtonClick(viewObject);
            }
        };
    }

    protected void showMessage(String value) {
        this.view.getCurrentView().showMessage(value);
    }

    protected void closeAllWindows(){
        this.view.getCurrentView().closeAllWindows();
    }

    public void showErrorMessage(String value) {
        this.view.getCurrentView().showErrorMessage(value);
    }

    public Navigation navigate(){
        return ConstellioUI.getCurrent().navigate();
    }

    public ConstellioHeader getView() {
        return view;
    }

    public void addAllObject(RecordVO... objects) {
        objectList.addAll(asList(objects));
    }

    public void setAllObject(RecordVO... objects) {
        objectList = new ArrayList<>();
        objectList.addAll(asList(objects));
    }
}
