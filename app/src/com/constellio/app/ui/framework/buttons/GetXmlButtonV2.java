package com.constellio.app.ui.framework.buttons;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportUtils;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleView;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.ui.Component;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Marco on 2017-07-10.
 */
public class GetXmlButtonV2 extends WindowButton{
    private ModelLayerFactory model;
    private SearchServices ss;
    private RMSchemasRecordsServices rm;
    private String collection;
    private List<String> ids;
    private AppLayerFactory factory;
    private ContentManager contentManager;
    private ReportUtils ru;
    private BaseView view;
    private PrintableReportListPossibleView currentSchema;
    private MetadataSchemaToVOBuilder schemaVOBuilder;


    public GetXmlButtonV2(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection, BaseView, PrintableReportListPossibleView currentSchema) {
        super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
        this.model = factory.getModelLayerFactory();
        this.collection = collection;
        this.factory = factory;
        this.ss = model.newSearchServices();
        this.rm = new RMSchemasRecordsServices(this.collection, factory);
        this.contentManager = model.getContentManager();
        this.ru = new ReportUtils(collection, factory, view.getSessionContext().getCurrentUser().getUsername());
        this.view = view;
        this.currentSchema = currentSchema;
        schemaVOBuilder = new MetadataSchemaToVOBuilder();
    }

    @Override
    protected Component buildWindowContent() {
        SearchResultVODataProvider dataProvider = getDataProvider();
        SearchResultVOLazyContainer lazyContainer = new SearchResultVOLazyContainer(dataProvider);
        return new BaseForm<LabelParametersVO>(new LabelParametersVO(new LabelTemplate()), this, lazyContainer.get) {

            @Override
            protected void saveButtonClick(LabelParametersVO viewObject) throws ValidationException {

            }

            @Override
            protected void cancelButtonClick(LabelParametersVO viewObject) {

            }
        };
    }

    private SearchResultVODataProvider getDataProvider() {
        return new SearchResultVODataProvider(new RecordToVOBuilder(), factory, view.getSessionContext()) {
            @Override
            protected LogicalSearchQuery getQuery() {
                MetadataSchema metadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(currentSchema.toString());
                return new LogicalSearchQuery(from(metadataSchema).where(ALL));
            }
        };
    }
}
