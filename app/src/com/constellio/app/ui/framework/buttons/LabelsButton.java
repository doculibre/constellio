package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportField;
import com.constellio.app.modules.rm.services.reports.ReportUtils;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.data.utils.Factory;
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
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * FIXME Use a presenter
 */
public class LabelsButton extends WindowButton {
	
    @PropertyId("startPosition")
    private ComboBox startPositionField;
    @PropertyId("labelConfigurations")
    private ComboBox formatField;
    @PropertyId("numberOfCopies")
    private TextField copiesField;
    private ModelLayerFactory model;
    private String type;
    private SearchServices ss;
    private RMSchemasRecordsServices rm;
    private String collection;
    private List<String> ids;
    private AppLayerFactory appLayerFactory;
    private ContentManager contentManager;
    private double size;
    private String user;
    private Factory<List<LabelTemplate>> customLabelTemplatesFactory;
    private Factory<List<LabelTemplate>> defaultLabelTemplatesFactory;

    public LabelsButton(String caption, String windowsCaption, Factory<List<LabelTemplate>> customLabelTemplatesFactory, Factory<List<LabelTemplate>> defaultLabelTemplatesFactory, AppLayerFactory factory, String collection, String type, String id, String user) {
        this(caption, windowsCaption, customLabelTemplatesFactory, defaultLabelTemplatesFactory, factory, collection, type, Arrays.asList(id), user);
    }

    public LabelsButton(String caption, String windowsCaption, Factory<List<LabelTemplate>> customLabelTemplatesFactory, Factory<List<LabelTemplate>> defaultLabelTemplatesFactory, AppLayerFactory factory, String collection, String type, List<String> idObject, String user) {
        super(caption, windowsCaption, new WindowConfiguration(true, true, "75%", "250px"));
        this.customLabelTemplatesFactory = customLabelTemplatesFactory;
        this.defaultLabelTemplatesFactory = defaultLabelTemplatesFactory;
        this.model = factory.getModelLayerFactory();
        this.collection = collection;
        this.appLayerFactory = factory;
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
        startPositionField = new ComboBox($("LabelsButton.startPosition"));
        startPositionField.setNullSelectionAllowed(false);
        startPositionField.setRequired(true);
        
    	List<LabelTemplate> customTemplates = getCustomTemplates();
        if (customTemplates.size() > 0) {
        	LabelTemplate firstLabelTemplate = customTemplates.get(0);
            this.size = firstLabelTemplate.getLines() * firstLabelTemplate.getColumns();
            startPositionField.clear();
            for (int i = 1; i <= size; i++) {
                startPositionField.addItem(i);
            }
        }

        formatField = new ComboBox($("LabelsButton.labelFormat"));
        formatField.setRequired(true);
        this.getWindow().setResizable(true);
        List<Object> formatOptions = new ArrayList<Object>(customTemplates);
    	List<PrintableLabel> printableLabels = getTemplates(type);
        if (!printableLabels.isEmpty()) {
        	PrintableLabel firstPrintableLabel = printableLabels.get(0);
            this.size = (Double) firstPrintableLabel.get(PrintableLabel.LIGNE) * (Double) firstPrintableLabel.get(PrintableLabel.COLONNE);
            startPositionField.clear();
            for (int i = 1; i <= size; i++) {
                startPositionField.addItem(i);
            }
            formatOptions.addAll(printableLabels);
        } else {
        	List<LabelTemplate> defaultTemplates = getDefaultTemplates();
            if (defaultTemplates.size() > 0) {
            	LabelTemplate firstLabelTemplate = defaultTemplates.get(0);
                this.size = firstLabelTemplate.getLines() * firstLabelTemplate.getColumns();
                startPositionField.clear();
                for (int i = 1; i <= size; i++) {
                    startPositionField.addItem(i);
                }
            }
            formatOptions.addAll(defaultTemplates);
        }
        for (Object formatOption : formatOptions) {
            formatField.addItem(formatOption);
            String itemCaption;
            if (formatOption instanceof PrintableLabel) {
                itemCaption = ((PrintableLabel) formatOption).getTitle();
            } else {
                String templateName = ((LabelTemplate) formatOption).getName();
                itemCaption = $(templateName);
            }
            formatField.setItemCaption(formatOption, itemCaption);
        }

        if (formatOptions.size() > 0) {
            formatField.select(formatOptions.get(0));
        }
        formatField.setPageLength(formatOptions.size());
        formatField.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
        formatField.setNullSelectionAllowed(false);
        formatField.setValue(formatOptions.get(0));
        formatField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                Object ob = event.getProperty().getValue();
                Integer previousStartPosition = (Integer) startPositionField.getValue();
                if (ob instanceof PrintableLabel) {
                    PrintableLabel report = (PrintableLabel) event.getProperty().getValue();
                    size = (Double) report.get(PrintableLabel.COLONNE) * (Double) report.get(PrintableLabel.LIGNE);
                    startPositionField.clear();
                    startPositionField.removeAllItems();
                    for (int i = 1; i <= size; i++) {
                        startPositionField.addItem(i);
                    }
                    if (previousStartPosition != null && previousStartPosition <= size) {
                    	startPositionField.setValue(previousStartPosition);
                    } else if (size > 0) {
                    	startPositionField.setValue(1);
                    }
                } else if (ob instanceof LabelTemplate) {
                    LabelTemplate labelTemplate = (LabelTemplate) event.getProperty().getValue();
                    int size = labelTemplate.getLabelsReportLayout().getNumberOfLabelsPerPage();
                    startPositionField.clear();
                    startPositionField.removeAllItems();
                    for (int i = 1; i <= size; i++) {
                        startPositionField.addItem(i);
                    }
                    if (previousStartPosition != null && previousStartPosition <= size) {
                    	startPositionField.setValue(previousStartPosition);
                    } else if (size > 0) {
                    	startPositionField.setValue(1);
                    }
                } else throw new UnsupportedOperationException();
            }
        });

        copiesField = new TextField($("LabelsButton.numberOfCopies"));
        copiesField.setRequired(true);
        copiesField.setConverter(Integer.class);
        
        final HorizontalLayout startAndCopiesLayout = new HorizontalLayout(startPositionField, copiesField);
//        startAndCopiesLayout.setWidth("100%");
        startAndCopiesLayout.setSpacing(true);

        return new BaseForm<LabelParametersVO>(
                new LabelParametersVO(new LabelTemplate()), this, startPositionField, formatField, copiesField) {
            @Override
			protected void addFieldToLayout(Field<?> field, VerticalLayout fieldLayout) {
            	if (field == startPositionField) {
            		fieldLayout.addComponent(startAndCopiesLayout);
            	} else if (field != copiesField) {
    				super.addFieldToLayout(field, fieldLayout);
            	}
			}

			@Override
            protected void saveButtonClick(LabelParametersVO parameters)
                    throws ValidationException {
                Object ob = formatField.getValue();
                if (ob instanceof PrintableLabel) {
                    PrintableLabel selected = (PrintableLabel) formatField.getValue();
                    ReportUtils ru = new ReportUtils(collection, appLayerFactory, user);
                    try {
                        if ((Integer) startPositionField.getValue() > size) {
                            throw new Exception($("ButtonLabel.error.posisbiggerthansize"));
                        }
                        ru.setStartingPosition((Integer) startPositionField.getValue() - 1);
                        String number = copiesField.getValue();
                        number = number.replace(" ", "");
                        ru.setNumberOfCopies(Integer.parseInt(number));
                        String xml = type.equals(Folder.SCHEMA_TYPE) ? ru.convertFolderWithIdentifierToXML(ids, (ReportField[]) null) : ru.convertContainerWithIdentifierToXML(ids, null);
                        Content content = selected.get(PrintableLabel.JASPERFILE);
                        InputStream inputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), content.getId());
                        FileUtils.copyInputStreamToFile(inputStream, new File("jasper.jasper"));
                        File file = new File("jasper.jasper");
                        Content c = ru.createPDFFromXmlAndJasperFile(xml, file, ((PrintableLabel) formatField.getValue()).getTitle() + ".pdf");
                        getWindow().setContent(new LabelViewer(c, ReportUtils.escapeForXmlTag(((PrintableLabel) formatField.getValue()).getTitle()) + ".pdf"));
                        Page.getCurrent().getJavaScript().execute("$('iframe').find('#print').remove()");
                        getWindow().setHeight("90%");
                        getWindow().center();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (ob instanceof LabelTemplate) {
                    LabelTemplate labelTemplate = formatField.getValue() != null ? (LabelTemplate) formatField.getValue() : new LabelTemplate();
                    LabelsReportParameters params = new LabelsReportParameters(
                            ids, labelTemplate,
                            parameters.getStartPosition(), parameters.getNumberOfCopies());
                    ReportWriter writer = getLabelsReportFactory().getReportBuilder(params);
                    getWindow().setContent(new ReportViewer(writer, getLabelsReportFactory().getFilename(params)));
                    getWindow().setHeight("90%");
                    getWindow().center();
                } else throw new UnsupportedOperationException();
            }
            
            @Override
            protected void cancelButtonClick(LabelParametersVO parameters) {
                getWindow().close();
            }
            
			@Override
			protected String getSaveButtonCaption() {
				return $("LabelsButton.generate");
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

    public List<LabelTemplate> getCustomTemplates() {
        return customLabelTemplatesFactory.get();
    }

    public List<LabelTemplate> getDefaultTemplates() {
        return defaultLabelTemplatesFactory.get();
    }

    public NewReportWriterFactory<LabelsReportParameters> getLabelsReportFactory() {
        final AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
        final RMModuleExtensions rmModuleExtensions = extensions.forModule(ConstellioRMModule.ID);
        return rmModuleExtensions.getReportBuilderFactories().labelsBuilderFactory.getValue();
    }

}
