package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.sdk.tests.ConstellioTest;
import com.google.gwt.dev.util.collect.HashMap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RetentionRuleImportExtensionRealTest extends ConstellioTest {

    public static final String SEMI_ACTIVE_RETENTION_PERIOD_COMMENT = "semiActiveRetentionPeriodComment";
    public static final String ACTIVE_RETENTION_PERIOD_COMMENT = "activeRetentionPeriodComment";
    public static final String SEMI_ACTIVE_RETENTION_PERIOD = "semiActiveRetentionPeriod";
    public static final String INACTIVE_DISPOSAL_COMMENT = "inactiveDisposalComment";
    public static final String COPY_RETENTION_RULE_INDEX = "copyRetentionRuleIndex";
    public static final String ACTIVE_RETENTION_PERIOD = "activeRetentionPeriod";
    public static final String INACTIVE_DISPOSAL_TYPE = "inactiveDisposalType";
    public static final String CONTENT_TYPES_COMMENT = "contentTypesComment";
    public static final String DOCUMENT_TYPE_INDEX = "documentTypeIndex";
    public static final String ARCHIVISTIC_STATUS = "archivisticStatus";
    public static final String MEDIUM_TYPES = "mediumTypes";
    public static final String COPY_TYPE = "copyType";
    public static final String CODE = "code";
    public static final String COPY_RET_RULE_ID = "id";
    public static final String OPEN_ACTIVE_RETENTION_PERIOD = "openActiveRetentionPeriod";
    public static final String ACTIVE_DATE_METADATA = "activeDateMetadata";
    public static final String SEMI_ACTIVE_DATE_METADATA = "semiActiveDateMetadata";
    public static final String DOCUMENT_TYPE_ID = "documentTypeId";

    private RetentionRuleImportExtension importExtension;
    private RMSchemasRecordsServices rm;

    @Before
    public void setUp() throws Exception {
        prepareSystem(
                withZeCollection().withConstellioRMModule()
        );

        importExtension = new RetentionRuleImportExtension(zeCollection,getModelLayerFactory());
        rm = new RMSchemasRecordsServices(zeCollection,getAppLayerFactory());
    }

    @Test
    public void givenRulesWithDifferentValuesAndFieldsThenAllImportedProperly() throws Exception {
        RetentionRule retentionRule = rm.newRetentionRule();
        retentionRule.setScope(RetentionRuleScope.DOCUMENTS);

        Map<String,Object> importDataMap = new HashMap<String,Object>();

        // Two CopyRetentionRules for DOCUMENT_COPY_RETENTION_RULES
        List<Map<String,String>> docCopyRetRules = new ArrayList<>();

        Map<String,String> docCopyRetRule1 = new HashMap<>();
        docCopyRetRule1.put(COPY_RET_RULE_ID,"DCR001");
        docCopyRetRule1.put(COPY_TYPE, CopyType.PRINCIPAL.getCode());
        docCopyRetRule1.put(MEDIUM_TYPES, "P");
        docCopyRetRule1.put(ACTIVE_RETENTION_PERIOD, "2");
        docCopyRetRule1.put(SEMI_ACTIVE_RETENTION_PERIOD, "3");
        docCopyRetRule1.put(INACTIVE_DISPOSAL_TYPE, "D");
        //docCopyRetRule1.put(OPEN_ACTIVE_RETENTION_PERIOD,"2"); This is optional, testing case where it's absent.
        //docCopyRetRule1.put(ACTIVE_DATE_METADATA,""); This is optional, testing case where it's absent.
        //docCopyRetRule1.put(SEMI_ACTIVE_DATE_METADATA,""); This is optional, testing case where it's absent.
        docCopyRetRule1.put(DOCUMENT_TYPE_ID,"DocType1");

        Map<String,String> docCopyRetRule2 = new HashMap<>();
        docCopyRetRule2.put(COPY_RET_RULE_ID,"DCR002");
        docCopyRetRule2.put(COPY_TYPE, CopyType.PRINCIPAL.getCode());
        docCopyRetRule2.put(MEDIUM_TYPES, "P");
        docCopyRetRule2.put(ACTIVE_RETENTION_PERIOD, "2");
        docCopyRetRule2.put(SEMI_ACTIVE_RETENTION_PERIOD, "3");
        docCopyRetRule2.put(INACTIVE_DISPOSAL_TYPE, "D");
        //docCopyRetRule2.put(OPEN_ACTIVE_RETENTION_PERIOD,"2"); This is optional, testing case where it's absent.
        //docCopyRetRule2.put(ACTIVE_DATE_METADATA,"dateMeta"); This is optional, testing case where it's absent.
        //docCopyRetRule2.put(SEMI_ACTIVE_DATE_METADATA,"dateMeta"); This is optional, testing case where it's absent.
        docCopyRetRule2.put(DOCUMENT_TYPE_ID,"DocType2");

        docCopyRetRules.add(docCopyRetRule1);
        docCopyRetRules.add(docCopyRetRule2);

        // One CopyRetRule for PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE
        Map<String,String> princDefaultDocCopyRetRule = new HashMap<>();
        princDefaultDocCopyRetRule.put(COPY_RET_RULE_ID,"DCR003");
        princDefaultDocCopyRetRule.put(COPY_TYPE, CopyType.PRINCIPAL.getCode());
        princDefaultDocCopyRetRule.put(MEDIUM_TYPES, "P");
        princDefaultDocCopyRetRule.put(ACTIVE_RETENTION_PERIOD, "2");
        princDefaultDocCopyRetRule.put(SEMI_ACTIVE_RETENTION_PERIOD, "3");
        princDefaultDocCopyRetRule.put(INACTIVE_DISPOSAL_TYPE, "D");
        princDefaultDocCopyRetRule.put(OPEN_ACTIVE_RETENTION_PERIOD,"2");
        princDefaultDocCopyRetRule.put(ACTIVE_DATE_METADATA,"dateMeta1");
        princDefaultDocCopyRetRule.put(SEMI_ACTIVE_DATE_METADATA,"dateMeta2");
        princDefaultDocCopyRetRule.put(DOCUMENT_TYPE_ID,"DocType3");

        // One CopyRetRule for SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE
        Map<String,String> secDefaultDocCopyRetRule = new HashMap<>();
        //secDefaultDocCopyRetRule.put(COPY_RET_RULE_ID,"DCR004"); Testing case where ID is generated
        secDefaultDocCopyRetRule.put(COPY_TYPE, CopyType.PRINCIPAL.getCode());
        secDefaultDocCopyRetRule.put(MEDIUM_TYPES, "P");
        secDefaultDocCopyRetRule.put(ACTIVE_RETENTION_PERIOD, "2");
        secDefaultDocCopyRetRule.put(SEMI_ACTIVE_RETENTION_PERIOD, "3");
        secDefaultDocCopyRetRule.put(INACTIVE_DISPOSAL_TYPE, "D");
        secDefaultDocCopyRetRule.put(DOCUMENT_TYPE_ID,"DocType4");

        importDataMap.put(RetentionRule.DOCUMENT_COPY_RETENTION_RULES,docCopyRetRules);
        importDataMap.put(RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE,princDefaultDocCopyRetRule);
        importDataMap.put(RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE,secDefaultDocCopyRetRule);

        ImportData importData = new ImportData(1, retentionRule.getSchemaCode(), "zeLegacyId", importDataMap);
        BuildParams buildParams = new BuildParams(retentionRule.getWrappedRecord(),rm.getTypes(), importData);

        importExtension.build(buildParams);

        RetentionRule rule = rm.wrapRetentionRule(buildParams.getRecord());

        List<CopyRetentionRule> docCopyRetRulesBuilt = rule.getDocumentCopyRetentionRules();
        assertThat(docCopyRetRulesBuilt).hasSize(2);

        CopyRetentionRule princCopyRetRuleBuilt = rule.getPrincipalDefaultDocumentCopyRetentionRule();
        assertThat(princCopyRetRuleBuilt.getId()).isEqualTo("DCR003");
        assertThat(princCopyRetRuleBuilt.getOpenActiveRetentionPeriod()).isEqualTo(2);
        assertThat(princCopyRetRuleBuilt.getActiveDateMetadata()).isEqualTo("dateMeta1");
        assertThat(princCopyRetRuleBuilt.getSemiActiveDateMetadata()).isEqualTo("dateMeta2");
        assertThat(princCopyRetRuleBuilt.getDocumentTypeId()).isEqualTo("DocType3");

        CopyRetentionRule secCopyRetRuleBuilt = rule.getSecondaryDefaultDocumentCopyRetentionRule();
        assertThat(secCopyRetRuleBuilt.getId()).isNotNull();
        assertThat(secCopyRetRuleBuilt.getOpenActiveRetentionPeriod()).isNull();
        assertThat(secCopyRetRuleBuilt.getActiveDateMetadata()).isNull();
        assertThat(secCopyRetRuleBuilt.getSemiActiveDateMetadata()).isNull();
        assertThat(secCopyRetRuleBuilt.getDocumentTypeId()).isEqualTo("DocType4");
    }
}
