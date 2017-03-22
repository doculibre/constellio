package com.constellio.app.ui.framework.buttons;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportFactory;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportUtils;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.model.entities.records.Content;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

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
    private double size;
    private String user;

    public LabelsButton(String caption, String windowsCaption, AppLayerFactory factory, String collection, String type, String id, String user) {
        this(caption, windowsCaption, factory, collection, type, Arrays.asList(id), user);
    }

    public LabelsButton(String caption, String windowsCaption, AppLayerFactory factory, String collection, String type, List<String> idObject, String user) {
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
        this.user = user;
    }

    @Override
    protected Component buildWindowContent() {
        startPosition = new ComboBox($("LabelsButton.startPosition"));

        startPosition.setNullSelectionAllowed(false);

        List<PrintableLabel> configurations = getTemplates(type);
        if (configurations.size() > 0) {
            this.size = (Double) configurations.get(0).get(PrintableLabel.LIGNE) * (Double) configurations.get(0).get(PrintableLabel.COLONNE);
            startPosition.clear();
            for (int i = 1; i <= size; i++) {
                startPosition.addItem(i);
            }
        }

        format = new ComboBox($("LabelsButton.labelFormat"));
        for (PrintableLabel configuration : configurations) {
            format.addItem(configuration);
            format.setItemCaption(configuration, configuration.getTitle());
        }

        for (LabelTemplate template : getTemplates()) {
            format.addItem(template);
            format.setItemCaption(template, $(template.getName()));
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
                Object ob = event.getProperty().getValue();
                if (ob instanceof PrintableLabel) {
                    PrintableLabel report = (PrintableLabel) event.getProperty().getValue();
                    size = (Double) report.get(PrintableLabel.COLONNE) * (Double) report.get(PrintableLabel.LIGNE);
                    startPosition.clear();
                    startPosition.removeAllItems();
                    for (int i = 1; i <= size; i++) {
                        startPosition.addItem(i);
                    }
                } else if (ob instanceof LabelTemplate) {
                    LabelTemplate labelTemplate = (LabelTemplate) event.getProperty().getValue();
                    int size = labelTemplate.getLabelsReportLayout().getNumberOfLabelsPerPage();
                    startPosition.clear();
                    startPosition.removeAllItems();
                    for (int i = 1; i <= size; i++) {
                        startPosition.addItem(i);
                    }
                } else throw new UnsupportedOperationException();
            }
        });

        copies = new TextField($("LabelsButton.numberOfCopies"));
        copies.setConverter(Integer.class);

        return new BaseForm<LabelParametersVO>(
                new LabelParametersVO(new LabelTemplate()), this, startPosition, format, copies) {
            @Override
            protected void saveButtonClick(LabelParametersVO parameters)
                    throws ValidationException {
                Object ob = format.getValue();
                if (ob instanceof PrintableLabel) {
                    PrintableLabel selected = (PrintableLabel) format.getValue();
                    ReportUtils ru = new ReportUtils(collection, factory, user);
                    try {
                        if ((Integer) startPosition.getValue() > size) {
                            throw new Exception($("ButtonLabel.error.posisbiggerthansize"));
                        }
                        ru.setStartingPosition((Integer) startPosition.getValue() - 1);
                        ru.setNumberOfCopies(Integer.parseInt(copies.getValue()));
                        String xml = type.equals(Folder.SCHEMA_TYPE) ? ru.convertFolderWithIdentifierToXML(ids, null) : ru.convertContainerWithIdentifierToXML(ids, null);
                        Content content = selected.get(PrintableLabel.JASPERFILE);
                        InputStream inputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), content.getId());
                        FileUtils.copyInputStreamToFile(inputStream, new File("jasper.jasper"));
                        File file = new File("jasper.jasper");
                        Content c = ru.createPDFFromXmlAndJasperFile(xml, file, ((PrintableLabel) format.getValue()).getTitle() + ".pdf");
                        getWindow().setContent(new LabelViewer(c, ReportUtils.escapeForXmlTag(((PrintableLabel) format.getValue()).getTitle()) + ".pdf"));
                        Page.getCurrent().getJavaScript().execute("$('iframe').find('#print').remove()");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (ob instanceof LabelTemplate) {
                    LabelTemplate labelTemplate = format.getValue() != null ? (LabelTemplate) format.getValue() : new LabelTemplate();
                    LabelsReportFactory factory = new LabelsReportFactory(ConstellioFactories.getInstance().getAppLayerFactory());
                    LabelsReportParameters params = new LabelsReportParameters(
                            ids, labelTemplate,
                            parameters.getStartPosition(), parameters.getNumberOfCopies());
                    ReportWriter writer = factory.getReportBuilder(params);
                    getWindow().setContent(new ReportViewer(writer, factory.getFilename(params)));
                } else throw new UnsupportedOperationException();



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

    public List<LabelTemplate> getTemplates() {
        return this.factory.getLabelTemplateManager().listTemplates(Folder.SCHEMA_TYPE);
    }

}
