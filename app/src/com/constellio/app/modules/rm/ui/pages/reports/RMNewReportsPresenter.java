package com.constellio.app.modules.rm.ui.pages.reports;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.administration.plan.*;
import com.constellio.app.modules.rm.reports.factories.ExampleReportWithoutRecordsParameters;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

import java.util.Arrays;
import java.util.List;

public class RMNewReportsPresenter extends BasePresenter<RMReportsView> implements NewReportPresenter {

    private static final boolean BY_ADMINISTRATIVE_UNIT = true;
    private String schemaTypeValue;

    public RMNewReportsPresenter(RMReportsView view) {
        super(view);
    }

    @Override
    public List<String> getSupportedReports() {
        return Arrays.asList("Reports.ClassificationPlan",
                "Reports.DetailedClassificationPlan",
                "Reports.ClassificationPlanByAdministrativeUnit",
                "Reports.ConservationRulesList",
                "Reports.ConservationRulesListByAdministrativeUnit",
                "Reports.AdministrativeUnits",
                "Reports.AdministrativeUnitsAndUsers",
                "Reports.Users",
                "Reports.AvailableSpaceReport",
                "Reports.AvailableSpaceReportAll");
    }

    public NewReportWriterFactory getReport(String report) {
        AppLayerCollectionExtensions appCollectionExtentions = appLayerFactory.getExtensions().forCollection(collection);
        RMModuleExtensions rmModuleExtensions = appCollectionExtentions.forModule(ConstellioRMModule.ID);

        switch (report) {
            case "Reports.fakeReport2":
                return rmModuleExtensions.getReportBuilderFactories().exampleWithoutRecordsBuilderFactory.getValue();
            case "Reports.ClassificationPlan":
                return rmModuleExtensions.getReportBuilderFactories().classifcationPlanRecordBuilderFactory.getValue();
            case "Reports.DetailedClassificationPlan":
                return rmModuleExtensions.getReportBuilderFactories().classifcationPlanRecordBuilderFactory.getValue();
            case "Reports.ConservationRulesList":
                return rmModuleExtensions.getReportBuilderFactories().conservationRulesRecordBuilderFactory.getValue();
            case "Reports.ConservationRulesListByAdministrativeUnit":
                return rmModuleExtensions.getReportBuilderFactories().conservationRulesRecordBuilderFactory.getValue();
            case "Reports.AdministrativeUnits":
                return rmModuleExtensions.getReportBuilderFactories().administrativeUnitRecordBuilderFactory.getValue();
            case "Reports.AdministrativeUnitsAndUsers":
                return rmModuleExtensions.getReportBuilderFactories().administrativeUnitRecordBuilderFactory.getValue();
            case "Reports.Users":
                return rmModuleExtensions.getReportBuilderFactories().userRecordBuilderFactory.getValue();
            case "Reports.ClassificationPlanByAdministrativeUnit":
                return rmModuleExtensions.getReportBuilderFactories().classifcationPlanRecordBuilderFactory.getValue();
            case "Reports.AvailableSpaceReport":
                return rmModuleExtensions.getReportBuilderFactories().availableSpaceBuilderFactory.getValue();
            case "Reports.AvailableSpaceReportAll":
                return rmModuleExtensions.getReportBuilderFactories().availableSpaceBuilderFactory.getValue();

        }

        throw new RuntimeException("BUG: Unknown report: " + report);

    }

    @Override
    public Object getReportParameters(String report) {
        switch (report) {
            case "Reports.fakeReport2":
                return new ExampleReportWithoutRecordsParameters();
            case "Reports.ClassificationPlan":
                return new ClassificationReportPlanParameters(false, null);
            case "Reports.DetailedClassificationPlan":
                return new ClassificationReportPlanParameters(true, null);
            case "Reports.ConservationRulesList":
                return new ConservationRulesReportParameters(false, null);
            case "Reports.ConservationRulesListByAdministrativeUnit":
                return new ConservationRulesReportParameters(true, null);
            case "Reports.AdministrativeUnits":
                return new AdministrativeUnitReportParameters(false);
            case "Reports.AdministrativeUnitsAndUsers":
                return new AdministrativeUnitReportParameters(true);
            case "Reports.Users":
                return new UserReportParameters();
            case "Reports.ClassificationPlanByAdministrativeUnit":
                return new ClassificationReportPlanParameters(false, schemaTypeValue);
            case "Reports.AvailableSpaceReport":
                return new AvailableSpaceReportParameters(false);
            case "Reports.AvailableSpaceReportAll":
                return new AvailableSpaceReportParameters(true);
        }

        throw new RuntimeException("BUG: Unknown report: " + report);
    }

    public boolean isWithSchemaType(String report) {
        switch (report) {
            case "Reports.ConservationRulesListByAdministrativeUnit":
            case "Reports.ClassificationPlanByAdministrativeUnit":
                return true;
            default:
                return false;
        }
    }

    public String getSchemaTypeValue(String report) {
        switch (report) {
            case "Reports.ConservationRulesListByAdministrativeUnit":
            case "Reports.ClassificationPlanByAdministrativeUnit":
                return AdministrativeUnit.SCHEMA_TYPE;
            default:
                return null;
        }
    }

    public String getSchemaTypeValue()
    {
        return schemaTypeValue;
    }

    public void setSchemaTypeValue(String schemaTypeValue) {
        this.schemaTypeValue = schemaTypeValue;
    }

    public void backButtonClicked() {
        view.navigate().to(RMViews.class).archiveManagement();
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return user.has(RMPermissionsTo.MANAGE_REPORTS).globally();
    }
}
