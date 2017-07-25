package com.constellio.data.utils;

import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;

public class MultipleFieldsComparator implements Comparator{
    List<Comparator> comparatorList;

    public MultipleFieldsComparator() {
        this.comparatorList = new ArrayList<>();
    }

    public MultipleFieldsComparator(List<Comparator> comparatorList) {
        this.comparatorList = comparatorList;
    }

    public MultipleFieldsComparator(Comparator... comparatorList) {
        this.comparatorList = asList(comparatorList);
    }

    public MultipleFieldsComparator addComparator(Comparator comparator) {
        comparatorList.add(comparator);
        return this;
    }

    @Override
    public int compare(Object o1, Object o2) {
        int compare = 0;
        for(Comparator comparator: comparatorList) {
            compare = comparator.compare(o1, o2);
            if(compare != 0) {
                return compare;
            }
        }
        return compare;
    }
}
