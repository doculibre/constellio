package com.constellio.app.modules.tasks.model.calculators;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TaskFollowersCalculatorTest extends ConstellioTest {
    @Mock
    CalculatorParameters parameters;
    TaskFollowersCalculator calculator;
    List<TaskFollower> taskFollowers;

    @Before
    public void setUp()
            throws Exception {
        calculator = new TaskFollowersCalculator();
    }

    @Test
    public void givenNullOrEmptyFollowersWhenCalculatingThenReturnEmptyList()
            throws Exception {
        taskFollowers = null;
        assertThat(calculate()).isEmpty();

        taskFollowers = new ArrayList<>();
        assertThat(calculate()).isEmpty();
    }

    @Test
    public void givenFollower1AndFollower2WhenCalculatingThenReturnFollowers1And2()
            throws Exception {
        taskFollowers = new ArrayList<>();
        taskFollowers.add(new TaskFollower().setFollowerId("follower1"));
        taskFollowers.add(new TaskFollower().setFollowerId("follower2"));
        List<String> result = calculate();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsOnly("follower1", "follower2");
    }

    @Test
    public void givenFollower1AndFollower1WhenCalculatingThenReturnFollowers1()
            throws Exception {
        taskFollowers = new ArrayList<>();
        taskFollowers.add(new TaskFollower().setFollowerId("follower1"));
        taskFollowers.add(new TaskFollower().setFollowerId("follower1"));
        List<String> result = calculate();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result).containsOnly("follower1");
    }

    private List<String> calculate() {
        when(parameters.get(calculator.taskFollowers)).thenReturn(taskFollowers);

        return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
    }
}
