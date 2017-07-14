package com.constellio.app.ui.framework.buttons;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportXMLGenerator;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class GetXMLButton extends WindowButton {
    public static final String BASE_FORM = "base-form";

    public static final String STYLE_FIELD = "base-form-field";

    public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";

    public static final String SAVE_BUTTON = "base-form-save";

    public static final String CANCEL_BUTTON = "base-form_cancel";

    public static final String FOREGROUNDS[] = new String[]{"\u001B[30m", "\u001B[30m", "\u001B[30m"};
    public static final String BACKGROUND = "\u001B[47m";

    private ModelLayerFactory model;
    private SearchServices ss;
    private RMSchemasRecordsServices rm;
    private String collection;
    private List<String> ids;
    private AppLayerFactory factory;
    private ContentManager contentManager;
    private ReportXMLGenerator reportXmlGenerator;
    private BaseView view;
    private String currentSchema;

    public GetXMLButton(String caption, String windowsCaption, AppLayerFactory factory, String collection, BaseView view) {
        super(caption, windowsCaption, WindowConfiguration.modalDialog("75%", "75%"));
        this.model = factory.getModelLayerFactory();
        this.collection = collection;
        this.factory = factory;
        this.ss = model.newSearchServices();
        this.rm = new RMSchemasRecordsServices(this.collection, factory);
        this.contentManager = model.getContentManager();
        this.reportXmlGenerator = new ReportXMLGenerator(collection, factory, view.getSessionContext().getCurrentUser().getUsername());
        this.view = view;
        this.currentSchema = Folder.SCHEMA_TYPE;
    }

    @Override
    protected Component buildWindowContent() {
        final TextField txtNbFolder = new TextField();
        txtNbFolder.addStyleName(STYLE_FIELD);
        txtNbFolder.setValue(1 + "");
        txtNbFolder.setConverter(Integer.class);
        txtNbFolder.setCaption(currentSchema.equals(Folder.SCHEMA_TYPE) ? $("GenerateXML.nbFolder") : $("GenerateXML.nbContainer"));

        return new BaseForm<LabelParametersVO>(
                new LabelParametersVO(new LabelTemplate()), this, txtNbFolder) {
            @Override
            protected void saveButtonClick(LabelParametersVO parameters) throws ValidationException {
                try {
                    VerticalLayout newMain = new VerticalLayout();
                    String filename = "Constellio-Test.xml";
                    int nbEntre = Integer.parseInt(txtNbFolder.getValue());
                    List<String> ids = new ArrayList<>();
                    LogicalSearchCondition log = from(rm.schemaType(currentSchema)).where(ALL);
                    LogicalSearchQuery query = new LogicalSearchQuery(log).setNumberOfRows(nbEntre);
                    if (currentSchema.equals(Folder.SCHEMA_TYPE)) {
                        List<Folder> folders = rm.wrapFolders(ss.search(query));
                        for (Folder f : folders) {
                            ids.add(f.getId());
                        }
                    } else {
                        List<ContainerRecord> containers = rm.wrapContainerRecords(ss.search(query));
                        for (ContainerRecord c : containers) {
                            ids.add(c.getId());
                        }
                    }

                    String xml = currentSchema.equals(Folder.SCHEMA_TYPE) ? reportXmlGenerator.convertFolderWithIdentifierToXML(ids, null) : reportXmlGenerator.convertContainerWithIdentifierToXML(ids, null);
                    //Embedded viewer = new Embedded();
                    StreamResource source = createResource(xml, filename);
//                    viewer.setSource(source);
//                    viewer.setType(Embedded.TYPE_BROWSER);
//
//                    viewer.setWidth("100%");
//                    viewer.setHeight("1024px");

                    Link download = new Link($("ReportViewer.download", filename),
                            new DownloadStreamResource(source.getStreamSource(), filename));
                    newMain.addComponents(download);
                    newMain.setWidth("100%");
                    getWindow().setContent(newMain);
                } catch (Exception e) {
                    view.showErrorMessage(e.getMessage());
                }
            }

            @Override
            protected void cancelButtonClick(LabelParametersVO parameters) {
                getWindow().close();
            }
        };

    }

    public List<PrintableLabel> getTemplates(String type) {
        LogicalSearchCondition condition = from(rm.newPrintableLabel().getSchema()).where(rm.newPrintableLabel().getSchema().getMetadata(PrintableLabel.TYPE_LABEL)).isEqualTo(type);
        return rm.wrapPrintableLabels(ss.search(new LogicalSearchQuery(condition)));
    }

    public static interface RecordSelector extends Serializable {
        List<String> getSelectedRecordIds();
    }

    public void setIds(List<String> ids) {
        this.ids.addAll(ids);
    }

    public void setIds(String id) {
        this.ids.add(id);
    }

    private StreamResource createResource(final String xml, final String filename) {
        return new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                try {
                    return new ByteArrayInputStream(xml.getBytes("UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }
        }, filename);
    }

    public void setCurrentSchema(String schema) {
        this.currentSchema = schema;
    }

    public String getCurrentSchema() {
        return this.currentSchema;
    }

    public static class DownloadStreamResource extends StreamResource {
        public static String PDF_MIMETYPE = "application/pdf";
        public static String ZIP_MIMETYPE = "application/zip";
        public static String EXCEL_MIMETYPE = "application/vnd.ms-excel";

        public DownloadStreamResource(StreamSource source, String filename) {
            this(source, filename, "application/xml");
        }

        public DownloadStreamResource(StreamSource source, String filename, String MIMEType) {
            super(source, filename);
            setMIMEType(MIMEType);
        }

        @Override
        public DownloadStream getStream() {
            DownloadStream stream = super.getStream();
            stream.setParameter("Content-Disposition", "attachment; filename=" + getFilename());
            return stream;
        }
    }
}
