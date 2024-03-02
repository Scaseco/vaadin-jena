package org.aksw.vaadin.common.provider.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;


public class TestDataProviderBatch {
    @Test
    public void test() {
        DataProvider<String, SerializablePredicate<String>> core = new ListDataProvider<>(Arrays.asList("a", "b", "c", "d", "e"));
        DataProvider<List<String>, SerializablePredicate<String>> dp = new DataProviderBatch<>(core, 2);
        Query<List<String>, SerializablePredicate<String>> query = new Query<>(0, 50, null, null, null);

        List<List<String>> expectedItems = List.of(List.of("a", "b"), List.of("c", "d"), List.of("e"));

        int actualSize = dp.size(query);
        List<List<String>> actualItems = dp.fetch(query).collect(Collectors.toList());

        Assert.assertEquals(3, actualSize);
        Assert.assertEquals(expectedItems, actualItems);
    }
}
