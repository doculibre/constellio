package com.constellio.model.services.search;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchConfigurationsManagerAcceptanceTest extends ConstellioTest {

    public SearchConfigurationsManager searchConfigurationsManager;
    public static final String TWO_SYNONYMS = "tv ppppppppppppppppp";
    public static final String THREE_SYNONYMS = "auto cccccccccccccc dada";

    @Before
    public void setUp() {
        searchConfigurationsManager = getModelLayerFactory().getSearchConfigurationsManager();
    }

    @Test
    public void getSetGetSynonymsOnServerThenOk() {
        searchConfigurationsManager.setSynonyms(new ArrayList<String>());
        searchConfigurationsManager.initialize();
        assertThat(searchConfigurationsManager.getSynonyms().size()).isEqualTo(1);
        assertThat(searchConfigurationsManager.getSynonyms().get(0)).isEqualTo("");
        searchConfigurationsManager.setSynonyms(Arrays.asList(TWO_SYNONYMS, THREE_SYNONYMS));
        searchConfigurationsManager.initialize();
        assertThat(searchConfigurationsManager.getSynonyms().get(0)).isEqualTo(TWO_SYNONYMS);
        assertThat(searchConfigurationsManager.getSynonyms().get(1)).isEqualTo(THREE_SYNONYMS);

    }
}
