package com.constellio.app.services.corrector;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CorrectorExcluderManagerAcceptanceTest extends ConstellioTest {

    private String COLLECTION = "zeCollection";
    private String EXCLUSION = "Exclusion";
    private String COLLECTION_2 = "zeCollection2";
    private String EXCLUSION_2 = "Exclusion2";
    private String EXCLUSION_3 = "Exclusion3";

    private CorrectorExcluderManager correctorExcluderManager;

    @Before
    public void setUp() {
        correctorExcluderManager = new CorrectorExcluderManager(getModelLayerFactory());
        correctorExcluderManager.initialize();
    }

    @Test
    public void addCorrectorExcluder() {
        CorrectorExclusion correctorExclusion = new CorrectorExclusion();
        correctorExclusion.setCollection(COLLECTION).setExclusion(EXCLUSION);

        correctorExcluderManager.createCollectionExcluder(COLLECTION);
        correctorExcluderManager.addExclusion(correctorExclusion);
        List<CorrectorExclusion> correctorExclusions = correctorExcluderManager.getAllExclusion(COLLECTION);

        assertThat(correctorExclusions.get(0).getCollection()).isEqualTo(COLLECTION);
        assertThat(correctorExclusions.get(0).getExclusion()).isEqualTo(EXCLUSION);
    }

    @Test
    public void readCorrectorExcluder() {
        CorrectorExclusion correctorExclusion = new CorrectorExclusion();
        correctorExclusion.setCollection(COLLECTION).setExclusion(EXCLUSION);

        CorrectorExclusion correctorExclusion2 = new CorrectorExclusion();
        correctorExclusion2.setCollection(COLLECTION).setExclusion(EXCLUSION_2);

        CorrectorExclusion correctorExclusion3 = new CorrectorExclusion();
        correctorExclusion3.setCollection(COLLECTION_2).setExclusion(EXCLUSION_3);

        correctorExcluderManager.createCollectionExcluder(COLLECTION);
        correctorExcluderManager.addExclusion(correctorExclusion);
        correctorExcluderManager.addExclusion(correctorExclusion2);
        correctorExcluderManager.createCollectionExcluder(COLLECTION_2);
        correctorExcluderManager.addExclusion(correctorExclusion3);
        List<CorrectorExclusion> correctorExclusions1 = correctorExcluderManager.getAllExclusion(COLLECTION);

        List<CorrectorExclusion> correctorExclusions2 = correctorExcluderManager.getAllExclusion(COLLECTION_2);


        assertThat(correctorExclusions1.get(0).getCollection()).isEqualTo(COLLECTION);
        assertThat(correctorExclusions1.get(0).getExclusion()).isEqualTo(EXCLUSION);

        assertThat(correctorExclusions1.get(1).getCollection()).isEqualTo(COLLECTION);
        assertThat(correctorExclusions1.get(1).getExclusion()).isEqualTo(EXCLUSION_2);

        assertThat(correctorExclusions2.get(0).getCollection()).isEqualTo(COLLECTION_2);
        assertThat(correctorExclusions2.get(0).getExclusion()).isEqualTo(EXCLUSION_3);


    }

    @Test
    public void updateCorrectorExclusion() {
        CorrectorExclusion correctorExclusion = new CorrectorExclusion();
        correctorExclusion.setCollection(COLLECTION).setExclusion(EXCLUSION);
        CorrectorExclusion correctorExclusion2 = new CorrectorExclusion();
        correctorExclusion2.setCollection(COLLECTION).setExclusion(EXCLUSION_2);

        correctorExcluderManager.createCollectionExcluder(COLLECTION);
        correctorExcluderManager.addExclusion(correctorExclusion);

        List<CorrectorExclusion> correctorExclusions1 = correctorExcluderManager.getAllExclusion(COLLECTION);

        assertThat(correctorExclusions1.size()).isEqualTo(1);

        assertThat(correctorExclusions1.get(0).getCollection()).isEqualTo(COLLECTION);
        assertThat(correctorExclusions1.get(0).getExclusion()).isEqualTo(EXCLUSION);

        correctorExcluderManager.updateException(correctorExclusion, correctorExclusion2);

        List<CorrectorExclusion> correctorExclusions2 = correctorExcluderManager.getAllExclusion(COLLECTION);

        assertThat(correctorExclusions2.get(0).getCollection()).isEqualTo(COLLECTION);
        assertThat(correctorExclusions2.get(0).getExclusion()).isEqualTo(EXCLUSION_2);
    }

    @Test
    public void deleteCorrectorExclusion() {
        CorrectorExclusion correctorExclusion = new CorrectorExclusion();
        correctorExclusion.setCollection(COLLECTION).setExclusion(EXCLUSION);

        correctorExcluderManager.createCollectionExcluder(COLLECTION);
        correctorExcluderManager.addExclusion(correctorExclusion);
        List<CorrectorExclusion> correctorExclusions = correctorExcluderManager.getAllExclusion(COLLECTION);

        assertThat(correctorExclusions.get(0).getCollection()).isEqualTo(COLLECTION);
        assertThat(correctorExclusions.get(0).getExclusion()).isEqualTo(EXCLUSION);

        correctorExcluderManager.deleteException(correctorExclusion);

        List<CorrectorExclusion> correctorExclusions2 = correctorExcluderManager.getAllExclusion(COLLECTION);

        assertThat(correctorExclusions2.size()).isEqualTo(0);
    }
}
