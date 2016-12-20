package com.constellio.app.modules.rm.model.calculators.container;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.spy;

/**
 * Created by Constellio on 2016-12-19.
 */

public class ContainerRecordLinearSizeCalculatorAcceptanceTest extends ConstellioTest {

    CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

    CopyRetentionRule principal, secondPrincipal, thirdPrincipal, secondary;
    List<String> mediumTypes;
    CopyType folderCopyType;

    List<String> PA_DM = asList("PA", "DM");

    ContainerRecordLinearSizeCalculator calculator;

    @Mock
    CalculatorParameters parameters;

    @Before
    public void setUp()
            throws Exception {
        calculator = spy(new ContainerRecordLinearSizeCalculator());
    }

    @Test
    public void givenFolderHasSecondaryCopyTypeThenReturnTheSecondary() {
        principal = copyBuilder.newPrincipal(PA_DM, "888-0-D");
        secondary = copyBuilder.newSecondary(PA_DM, "888-0-C");
        mediumTypes = PA_DM;
        folderCopyType = CopyType.SECONDARY;

//        assertThat(calculatedValue()).containsOnlyOnce(secondary);

    }
}
