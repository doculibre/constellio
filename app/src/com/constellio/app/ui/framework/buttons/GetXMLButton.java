package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportUtils;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentTypeFieldLookupImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.constellio.app.ui.framework.components.viewers.image.ImageViewer;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.sun.org.apache.xml.internal.security.algorithms.implementations.IntegrityHmac;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.themes.ValoTheme;
import net.didion.jwnl.data.Exc;
import org.apache.commons.el.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.beanutils.converters.StringConverter;
import org.apache.commons.jexl3.JxltEngine;
import org.eclipse.jetty.deploy.App;

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
    private ReportUtils ru;
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
        this.ru = new ReportUtils(collection, factory, view.getSessionContext().getCurrentUser().getUsername());
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

                    String xml = currentSchema.equals(Folder.SCHEMA_TYPE) ? ru.convertFolderWithIdentifierToXML(ids, null) : ru.convertContainerWithIdentifierToXML(ids, null);
                    Embedded viewer = new Embedded();
                    StreamResource source = createResource(xml, filename);
                    viewer.setSource(source);
                    viewer.setType(Embedded.TYPE_BROWSER);

                    viewer.setWidth("100%");
                    viewer.setHeight("1024px");

                    Link download = new Link($("ReportViewer.download", filename),
                            new DownloadStreamResource(source.getStreamSource(), filename));
                    newMain.addComponents(download, viewer);
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

    public List<RMReport> getTemplates(String type) {
        LogicalSearchCondition condition = from(rm.newRMReport().getSchema()).where(rm.newRMReport().getSchema().getMetadata(RMReport.TYPE_LABEL)).isEqualTo(type);
        return rm.wrapRMReports(ss.search(new LogicalSearchQuery(condition)));
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
