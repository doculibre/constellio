package com.constellio.app.ui.framework.buttons;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.reports.XmlGenerator;
import com.constellio.app.modules.rm.services.reports.XmlReportGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class GetXmlButtonV2 extends WindowButton{
    private ModelLayerFactory model;
    private String collection;
    private AppLayerFactory factory;
    private ContentManager contentManager;
    private PrintableReportListPossibleType currentSchema;

    private ListAddRemoveRecordLookupField elementLookUpField;


    public GetXmlButtonV2(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection, BaseView view, PrintableReportListPossibleType currentSchema) {
        super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
        this.factory = appLayerFactory;
        this.model = factory.getModelLayerFactory();
        this.collection = collection;
        this.contentManager = model.getContentManager();
        this.currentSchema = currentSchema;
    }

    public void setCurrentSchema(PrintableReportListPossibleType schema) {
        this.currentSchema = schema;
    }

    @Override
    protected Component buildWindowContent() {
        this.elementLookUpField = getLookupFieldForCurrentSchema();
        return new GetXmlFrom(new LabelParametersVO(new LabelTemplate()), this, this.elementLookUpField);
    }

    private ListAddRemoveRecordLookupField getLookupFieldForCurrentSchema() {
        return new ListAddRemoveRecordLookupField(currentSchema.getSchemaType());
    }

    private class GetXmlFrom extends BaseForm<LabelParametersVO> {
        private GetXmlButtonV2 parent = GetXmlButtonV2.this;
        public GetXmlFrom(LabelParametersVO viewObject, Serializable objectWithMemberFields, Field<?>... fields) {
            super(viewObject, objectWithMemberFields, fields);
        }

        @Override
        protected void saveButtonClick(LabelParametersVO viewObject) throws ValidationException {
            try{
                ListAddRemoveRecordLookupField lookupField = (ListAddRemoveRecordLookupField) fields.get(0);
                XmlReportGeneratorParameters xmlGeneratorParameters =  new XmlReportGeneratorParameters(1);
                xmlGeneratorParameters.setElementWithIds(currentSchema.getSchemaType(), lookupField.getValue());
                XmlGenerator xmlGenerator = new XmlReportGenerator(parent.factory, parent.collection, xmlGeneratorParameters);
                String xml = xmlGenerator.generateXML();
                String filename = "Constellio-Test.xml";
                StreamResource source = createResource(xml, filename);
                Link download = new Link($("ReportViewer.download", filename),
                        new GetXMLButton.DownloadStreamResource(source.getStreamSource(), filename));
                VerticalLayout newLayout = new VerticalLayout();
                newLayout.addComponents(download);
                newLayout.setWidth("100%");
                getWindow().setContent(newLayout);
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
