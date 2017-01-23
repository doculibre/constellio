package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportUtils;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.sun.org.apache.xml.internal.security.algorithms.implementations.IntegrityHmac;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import org.apache.commons.el.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.beanutils.converters.StringConverter;
import org.eclipse.jetty.deploy.App;

public class LabelsButton extends WindowButton {
    @PropertyId("startPosition")
    private ComboBox startPosition;
    @PropertyId("labelConfigurations")
    private ComboBox format;
    @PropertyId("numberOfCopies")
    private TextField copies;
    private ModelLayerFactory model;
    private String type;
    private SearchServices ss;
    private RMSchemasRecordsServices rm;
    private String collection;
    private List<String> ids;
    private AppLayerFactory factory;
    private ContentManager contentManager;
    private int size;

    public LabelsButton(String caption, String windowsCaption, AppLayerFactory factory, String collection, String type, String id) {
        this(caption, windowsCaption, factory, collection, type, Arrays.asList(id));
    }

    public LabelsButton(String caption, String windowsCaption, AppLayerFactory factory, String collection, String type, List<String> idObject) {
        super(caption, windowsCaption, WindowConfiguration.modalDialog("75%", "75%"));
        this.model = factory.getModelLayerFactory();
        this.collection = collection;
        this.factory = factory;
        this.ss = model.newSearchServices();
        this.type = type;
        this.ids = idObject;
        this.rm = new RMSchemasRecordsServices(this.collection, factory);
        this.contentManager = model.getContentManager();
        this.size = 0;
    }

    @Override
    protected Component buildWindowContent() {
        startPosition = new ComboBox($("LabelsButton.startPosition"));

        startPosition.setNullSelectionAllowed(false);

        List<RMReport> configurations = getTemplates(type);
        if (configurations.size() > 0) {
            this.size = Integer.parseInt(configurations.get(0).get(RMReport.LIGNE) + "") * Integer.parseInt(configurations.get(0).get(RMReport.COLONNE) + "");
            startPosition.clear();
            for (int i = 1; i <= size; i++) {
                startPosition.addItem(i);
            }
        }

        format = new ComboBox($("LabelsButton.labelFormat"));
        for (RMReport configuration : configurations) {
            format.addItem(configuration);
            format.setItemCaption(configuration, configuration.getTitle());
        }
        if (configurations.size() > 0) {
            format.select(configurations.get(0));
        }
        format.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
        format.setNullSelectionAllowed(false);
        format.setValue(configurations.get(0));
        format.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                RMReport report = (RMReport) event.getProperty().getValue();
                size = Integer.parseInt(report.get(RMReport.COLONNE) + "") * Integer.parseInt(report.get(RMReport.LIGNE) + "");
                startPosition.clear();
                startPosition.removeAllItems();
                for (int i = 1; i <= size; i++) {
                    startPosition.addItem(i);
                }
            }
        });

        copies = new TextField($("LabelsButton.numberOfCopies"));
        copies.setConverter(Integer.class);

        return new BaseForm<LabelParametersVO>(
                new LabelParametersVO(new LabelTemplate()), this, startPosition, format, copies) {
            @Override
            protected void saveButtonClick(LabelParametersVO parameters)
                    throws ValidationException {
                RMReport selected = (RMReport) format.getValue();
                ReportUtils ru = new ReportUtils(collection, factory);
                try {
                    if ((Integer) startPosition.getValue() > size) {
                        throw new Exception($("ButtonLabel.error.posisbiggerthansize"));
                    }
                    ru.setStartingPosition((Integer) startPosition.getValue() - 1);
                    String xml = type.equals(Folder.SCHEMA_TYPE) ? ru.convertFolderWithIdentifierToXML(ids, null) : ru.convertContainerWithIdentifierToXML(ids, null);
                    Content content = selected.get(RMReport.JASPERFILE);
                    InputStream inputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), content.getId());
                    FileUtils.copyInputStreamToFile(inputStream, new File("jasper.jasper"));
                    File file = new File("jasper.jasper");
                    Content c = ru.createPDFFromXmlAndJasperFile(xml, file, ((RMReport) format.getValue()).getTitle() + ".pdf");
                    getWindow().setContent(new LabelViewer(c, ReportUtils.escapeForXmlTag(((RMReport) format.getValue()).getTitle()) + ".pdf"));
                    Page.getCurrent().getJavaScript().execute("$('iframe').find('#print').remove()");
                } catch (Exception e) {
                    e.printStackTrace();
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
}
