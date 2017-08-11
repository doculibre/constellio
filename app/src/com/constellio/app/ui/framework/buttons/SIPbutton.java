package com.constellio.app.ui.framework.buttons;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.filter.SIPFilter;
import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.ConstellioSIP;
import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.data.intelligid.IntelliGIDSIPObjectsProvider;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class SIPbutton extends WindowButton {


    private List<RecordVO> objectList = new ArrayList<>();
    private CheckBox deleteCheckBox;
    private BaseView view;

    public SIPbutton(String caption, String windowCaption, BaseView view) {
        super(caption, windowCaption);
        this.view = view;
    }

//    @Override
//    public void setVisible(boolean visible) {
//        User user = view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices().getUserInCollection(view.getCollection(), view.getSessionContext().getCurrentUser().getUsername());
//        super.setVisible(user.has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally());
//    }

    @Override
    protected Component buildWindowContent() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(buildDeleteItemCheckbox());
        mainLayout.addComponent(buildButtonComponent());
        return mainLayout;
    }

    public void addAllObject(RecordVO... objects) {
        objectList.addAll(asList(objects));
    }

    private HorizontalLayout buildDeleteItemCheckbox(){
        HorizontalLayout layout = new HorizontalLayout();
        deleteCheckBox = new CheckBox($("SIPButton.deleteFilesLabel"));
        layout.addComponents(deleteCheckBox);
        return layout;
    }

    private HorizontalLayout buildButtonComponent() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button cancelButton = new BaseButton($("cancel")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                getWindow().close();
            }
        };
        Button continueButton = new BaseButton($("ok")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                try{
                    continueButtonClicked();
                }catch (IOException  e) {
                    view.showErrorMessage(e.getMessage());
                }
            }
        };
        buttonLayout.addComponents(cancelButton, continueButton);
        return buttonLayout;
    }

    private List<String> getDocumentIDListFromObjectList() {
        List<String> documents = new ArrayList<>();
        for(RecordVO recordVO : this.objectList) {
            if(recordVO.getSchema().getTypeCode().equals(Document.SCHEMA_TYPE)){
                documents.add(recordVO.getId());
            }
        }
        return documents;
    }

    private List<String> getFolderIDListFromObjectList() {
        List<String> folders = new ArrayList<>();
        for(RecordVO recordVO : this.objectList) {
            if(recordVO.getSchema().getTypeCode().equals(Folder.SCHEMA_TYPE)) {
                folders.add(recordVO.getId());
            }
        }
        return folders;
    }

    public void continueButtonClicked() throws IOException {
        //File bagInfoFile = new File();
        //InputStream bagInfoIn = new FileInputStream(bagInfoFile);
       // List<String> packageInfoLines = IOUtils.readLines(bagInfoIn);
        //bagInfoIn.close();
        List<String> documentList = getDocumentIDListFromObjectList();
        List<String> folderList = getFolderIDListFromObjectList();
        SIPFilter filter = new SIPFilter(view.getCollection(), view.getConstellioFactories().getAppLayerFactory())
                .withIncludeDocumentIds(documentList)
                .withIncludeFolderIds(folderList);
        IntelliGIDSIPObjectsProvider metsObjectsProvider = new IntelliGIDSIPObjectsProvider(view.getCollection(), view.getConstellioFactories().getAppLayerFactory(), filter);
        if (!metsObjectsProvider.list().isEmpty()) {
            //ConstellioSIP constellioSIP = new ConstellioSIP(metsObjectsProvider, packageInfoLines, limiterTaille);
            //constellioSIP.build(outFile);
        }
    }
}
