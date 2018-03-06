package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.reports.AbstractXmlGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.modules.rm.services.reports.printableReport.PrintableReportXmlGenerator;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

public class GetXmlButtonV2 extends WindowButton{
    private ModelLayerFactory model;
    private String collection;
    private AppLayerFactory factory;
    private ContentManager contentManager;
    private PrintableReportListPossibleType currentSchema;

    private Field elementLookUpField;
    private BaseView view;
    private boolean isXmlForTest;

    public GetXmlButtonV2(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection, BaseView view, PrintableReportListPossibleType currentSchema) {
        this(caption, windowCaption, appLayerFactory, collection, view, currentSchema, false);
    }

    public GetXmlButtonV2(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection, BaseView view, PrintableReportListPossibleType currentSchema, boolean isXmlForTest ) {
        super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
        this.factory = appLayerFactory;
        this.model = factory.getModelLayerFactory();
        this.collection = collection;
        this.contentManager = model.getContentManager();
        this.currentSchema = currentSchema;
        this.view = view;
        this.isXmlForTest = isXmlForTest;
    }

    public void setCurrentSchema(PrintableReportListPossibleType schema) {
        this.currentSchema = schema;
    }

    @Override
    protected Component buildWindowContent() {
        if(currentSchema.equals(PrintableReportListPossibleType.TASK)) {
            this.elementLookUpField = getLookupFieldForTaskSchema();
        } else {
            this.elementLookUpField = getLookupFieldForCurrentSchema();
        }
        return new GetXmlFrom(new LabelParametersVO(new LabelTemplate()), this, this.elementLookUpField);
    }

    private ListAddRemoveRecordLookupField getLookupFieldForCurrentSchema() {
        //TODO add permission support.
        ListAddRemoveRecordLookupField listAddRemoveRecordLookupField = new ListAddRemoveRecordLookupField(currentSchema.getSchemaType());
        return listAddRemoveRecordLookupField;
    }

    private ComboBox getLookupFieldForTaskSchema() {
        ComboBox taskFieldcomboBox = new BaseComboBox();
        MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
        List<Record> records = factory.getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery(from(metadataSchemasManager.getSchemaTypes(collection).getSchemaType(Task.SCHEMA_TYPE)).where(ALL)));
        TaskToVOBuilder taskToVOBuilder = new TaskToVOBuilder();
        for(Record record : records) {
            TaskVO taskVO = taskToVOBuilder.build(record, RecordVO.VIEW_MODE.FORM, view.getSessionContext());
            taskFieldcomboBox.addItem(taskVO);
            taskFieldcomboBox.setItemCaption(taskVO, taskVO.getTitle());
        }
        return taskFieldcomboBox;
    }

    private class GetXmlFrom extends BaseForm<LabelParametersVO> {
        private GetXmlButtonV2 parent = GetXmlButtonV2.this;
        public GetXmlFrom(LabelParametersVO viewObject, Serializable objectWithMemberFields, Field<?>... fields) {
            super(viewObject, objectWithMemberFields, fields);
        }

        @Override
        protected void saveButtonClick(LabelParametersVO viewObject) throws ValidationException {
            try{
                Field lookupField = fields.get(0);
                List<String> ids = currentSchema.equals(PrintableReportListPossibleType.TASK) ? asList(((TaskVO) lookupField.getValue()).getId()):((ListAddRemoveRecordLookupField) lookupField).getValue();
                if(ids.size() > 0) {
                    XmlReportGeneratorParameters xmlGeneratorParameters =  new XmlReportGeneratorParameters(1);
                    xmlGeneratorParameters.setElementWithIds(currentSchema.getSchemaType(), ids);
                    if(parent.isXmlForTest) {
                        xmlGeneratorParameters.markAsTestXml();
                    }
                    AbstractXmlGenerator xmlGenerator = new PrintableReportXmlGenerator(parent.factory, parent.collection, xmlGeneratorParameters);
                    String xml = xmlGenerator.generateXML();
                    String filename = "Constellio-Test.xml";
                    StreamResource source = createResource(xml, filename);
                    Link download = new Link($("ReportViewer.download", filename),
                            new GetXMLButton.DownloadStreamResource(source.getStreamSource(), filename));
                    VerticalLayout newLayout = new VerticalLayout();
                    newLayout.addComponents(download);
                    newLayout.setWidth("100%");
                    getWindow().setContent(newLayout);
                } else {
                    view.showErrorMessage($("DisplayLabelViewImpl.menu.getXMLButton.selectEntity"));
                }
            } catch (Exception e) {
              e.printStackTrace();
            }
        }

        @Override
        protected void cancelButtonClick(LabelParametersVO viewObject) {
            getWindow().close();
        }

        private StreamResource createResource(final String xml, final String filename) {
            return new StreamResource(new StreamResource.StreamSource() {
                @Override
                public InputStream getStream() {
                    System.out.println();
                    try {
                        return new ByteArrayInputStream(xml.getBytes("UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }, filename);
        }
    }

    public static class DownloadStreamResource extends StreamResource {

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
