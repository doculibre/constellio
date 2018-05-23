package com.constellio.model.services.schemas.builders;

import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator1;
import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator2;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_CustomParameters extends MetadataBuilderTest {

    java.util.Map<String, Object> customParameterMap1;
    java.util.Map<String, Object> customParameterMap2;

    List metadataPrefixDisplayConditionList1;
    List metadataPrefixDisplayConditionList2;

    @Before
    public void setUp(){


        metadataPrefixDisplayConditionList1 = new ArrayList();

        java.util.Map metadataMap1 = new HashMap();

        metadataMap1.put("prefix", "prefix1");
        metadataMap1.put("isalwaysshown", true);
        metadataMap1.put("metadata", "folder_default_code");

        metadataPrefixDisplayConditionList1.add(metadataMap1);

        java.util.Map metadataMap2 = new HashMap();
        metadataMap2.put("prefix", "prefix2");
        metadataMap2.put("isalwaysshown", false);
        metadataMap2.put("metadata", "folder_default_title");

        metadataPrefixDisplayConditionList1.add(metadataMap2);

        customParameterMap1 = new HashMap<>();
        customParameterMap1.put("customParameter", metadataPrefixDisplayConditionList1);

        metadataPrefixDisplayConditionList2 = new ArrayList();

        java.util.Map metadataMap3 = new HashMap();

        metadataMap3.put("prefix", "prefix3");
        metadataMap3.put("isalwaysshown", true);
        metadataMap3.put("metadata", "folder_default_adminsitrative_unit");


        java.util.Map metadataMap4 = new HashMap();
        metadataMap4.put("prefix", "prefix4");
        metadataMap4.put("isalwaysshown", false);
        metadataMap4.put("metadata", "folder_default_folder");

        metadataPrefixDisplayConditionList2.add(metadataMap3);
        metadataPrefixDisplayConditionList2.add(metadataMap4);

        customParameterMap2 = new HashMap<>();
        customParameterMap2.put("customParameter", metadataPrefixDisplayConditionList2);
    }

    @Test
    public void givenMetadatanWithCustomParameterAndInheritanceWhenBuildingThenMetadataWithInheritanceHasAll()
            throws Exception {

        inheritedMetadataBuilder.setType(STRING).setCustomParameter(customParameterMap1);
        metadataWithInheritanceBuilder.setCustomParameter(customParameterMap2);

        build();
        List list1 = (List) inheritedMetadata.getCustomParameter().get("customParameter");
        java.util.Map<String, Object> listMap1 = (Map<String, Object>) list1.get(0);

        assertThat(listMap1.get("prefix")).isEqualTo("prefix1");
        assertThat(listMap1.get("isalwaysshown")).isEqualTo(true);
        assertThat(listMap1.get("metadata")).isEqualTo("folder_default_code");

        java.util.Map<String, Object> listMap2 = (Map<String, Object>) list1.get(1);

        assertThat(listMap2.get("prefix")).isEqualTo("prefix2");
        assertThat(listMap2.get("isalwaysshown")).isEqualTo(false);
        assertThat(listMap2.get("metadata")).isEqualTo("folder_default_title");

        List list2 = (List) metadataWithInheritance.getCustomParameter().get("customParameter");

        java.util.Map<String, Object> listMap3 = (Map<String, Object>) list2.get(0);
        java.util.Map<String, Object> listMap4 = (Map<String, Object>) list2.get(1);

        assertThat(listMap3.get("prefix")).isEqualTo("prefix3");
        assertThat(listMap3.get("isalwaysshown")).isEqualTo(true);
        assertThat(listMap3.get("metadata")).isEqualTo("folder_default_adminsitrative_unit");

        assertThat(listMap4.get("prefix")).isEqualTo("prefix4");
        assertThat(listMap4.get("isalwaysshown")).isEqualTo(false);
        assertThat(listMap4.get("metadata")).isEqualTo("folder_default_folder");
    }

    @Test
    public void givenMetadataCustomParameterInMetadataAndInheritanceWhenModifyingThenMetadataWithInheritanceHasOnlyValueFromCustom()
            throws Exception {
        inheritedMetadataBuilder.setType(STRING).setCustomParameter(customParameterMap1);
        metadataWithInheritanceBuilder.setCustomParameter(customParameterMap2);

        buildAndModify();

        List list1 = (List) inheritedMetadataBuilder.getCustomParameter().get("customParameter");
        java.util.Map<String, Object> listMap1 = (Map<String, Object>) list1.get(0);

        assertThat(listMap1.get("prefix")).isEqualTo("prefix1");
        assertThat(listMap1.get("isalwaysshown")).isEqualTo(true);
        assertThat(listMap1.get("metadata")).isEqualTo("folder_default_code");

        java.util.Map<String, Object> listMap2 = (Map<String, Object>) list1.get(1);

        assertThat(listMap2.get("prefix")).isEqualTo("prefix2");
        assertThat(listMap2.get("isalwaysshown")).isEqualTo(false);
        assertThat(listMap2.get("metadata")).isEqualTo("folder_default_title");

        List list2 = (List) metadataWithInheritanceBuilder.getCustomParameter().get("customParameter");

        java.util.Map<String, Object> listMap3 = (Map<String, Object>) list2.get(0);
        java.util.Map<String, Object> listMap4 = (Map<String, Object>) list2.get(1);

        assertThat(listMap3.get("prefix")).isEqualTo("prefix3");
        assertThat(listMap3.get("isalwaysshown")).isEqualTo(true);
        assertThat(listMap3.get("metadata")).isEqualTo("folder_default_adminsitrative_unit");

        assertThat(listMap4.get("prefix")).isEqualTo("prefix4");
        assertThat(listMap4.get("isalwaysshown")).isEqualTo(false);
        assertThat(listMap4.get("metadata")).isEqualTo("folder_default_folder");
    }

    @Test
    public void givenRecordMetadataValidatorsDefinedDuplicatelyInMetadataAndInheritanceWhenBuildingThenNoDuplicate()
            throws Exception {
        inheritedMetadataBuilder.setType(STRING).setCustomParameter(customParameterMap1);


        build();

        List list1 = (List) inheritedMetadataBuilder.getCustomParameter().get("customParameter");
        java.util.Map<String, Object> listMap1 = (Map<String, Object>) list1.get(0);

        assertThat(listMap1.get("prefix")).isEqualTo("prefix1");
        assertThat(listMap1.get("isalwaysshown")).isEqualTo(true);
        assertThat(listMap1.get("metadata")).isEqualTo("folder_default_code");

        java.util.Map<String, Object> listMap2 = (Map<String, Object>) list1.get(1);

        assertThat(listMap2.get("prefix")).isEqualTo("prefix2");
        assertThat(listMap2.get("isalwaysshown")).isEqualTo(false);
        assertThat(listMap2.get("metadata")).isEqualTo("folder_default_title");

        List list2 = (List) metadataWithInheritance.getCustomParameter().get("customParameter");
        java.util.Map<String, Object> listMap3 = (Map<String, Object>) list2.get(0);

        assertThat(listMap3.get("prefix")).isEqualTo("prefix1");
        assertThat(listMap3.get("isalwaysshown")).isEqualTo(true);
        assertThat(listMap3.get("metadata")).isEqualTo("folder_default_code");

        java.util.Map<String, Object> listMap4 = (Map<String, Object>) list2.get(1);

        assertThat(listMap4.get("prefix")).isEqualTo("prefix2");
        assertThat(listMap4.get("isalwaysshown")).isEqualTo(false);
        assertThat(listMap4.get("metadata")).isEqualTo("folder_default_title");
    }

}
