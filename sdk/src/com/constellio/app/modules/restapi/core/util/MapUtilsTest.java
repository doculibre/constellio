package com.constellio.app.modules.restapi.core.util;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MapUtilsTest {

    @Test
    public void testSortByReverseValue() {
        Map<String, Integer> map = Maps.newHashMap();
        map.put("Alex", 11);
        map.put("Bob", 5);
        map.put("Cid", 19);

        Map<String, Integer> sortedMap = MapUtils.sortByReverseValue(map);

        List<String> keys = Lists.newArrayList(sortedMap.keySet());
        assertThat(keys).containsExactly("Cid", "Alex", "Bob");

        List<Integer> values = Lists.newArrayList(sortedMap.values());
        assertThat(values).containsExactly(19, 11, 5);

    }

}
