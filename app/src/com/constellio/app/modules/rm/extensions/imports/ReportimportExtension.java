package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by constellios on 2017-05-12.
 */
public class ReportImportExtension extends RecordImportExtension {

    RMSchemasRecordsServices rm;

    public final static String METADATA_CODE = "metadataCode";
    public final static String X_POSITION = "xPosition";
    public final static String Y_POSITION = "yPosition";

    public ReportImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
        this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
    }


    @Override
    public void build(BuildParams event) {
        Report report = rm.wrapReport(event.getRecord());

        List<ReportedMetadata> reportedMetadataList = new ArrayList<>();

        List<Map<String, String>> mapReportedMetadata = event.getImportRecord().getList(Report.REPORTED_METADATA);

        for(Map<String,String> map : mapReportedMetadata)
        {
            ReportedMetadata reportedMetadata = new ReportedMetadata(map.get(METADATA_CODE),
                    Integer.parseInt(map.get(X_POSITION)));

            if(map.get(Y_POSITION) != null)
            {
                reportedMetadata.setYPosition(Integer.parseInt(map.get(Y_POSITION)));
            }
        }

        report.setReportedMetadata(reportedMetadataList);
    }

    @Override
    public String getDecoratedSchemaType() {
        return Report.SCHEMA_TYPE;
    }
}
