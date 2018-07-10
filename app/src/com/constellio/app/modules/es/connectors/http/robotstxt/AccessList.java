package com.constellio.app.modules.es.connectors.http.robotstxt;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AccessList {
    private final List<Access> accessList = new ArrayList<Access>();

    /**
     * Adds access to the list.
     * @param access access
     */
    public void addAccess(Access access) {
        accessList.add(access);
    }

    /**
     * Imports entire access list from another instance.
     * @param ref another instance
     */
    public void importAccess(AccessList ref) {
        accessList.addAll(ref.accessList);
    }

    @Override
    public String toString() {
        List<String> toString = new ArrayList<>();

        for (Access a:accessList) {
            toString.add(a.toString());
        }

        return StringUtils.join(toString, "\n");
    }

    /**
     * Select any access matching input path.
     * @param relativePath path to test
     * @param matchingStrategy matcher
     * @return list of matching elements
     */
    public List<Access> select(String relativePath, MatchingStrategy matchingStrategy) {
        ArrayList<Access> allMatching = new ArrayList<Access>();

        if (relativePath!=null) {
            for (Access acc: accessList) {
                if (acc.matches(relativePath, matchingStrategy)) {
                    allMatching.add(acc);
                }
            }
        }

        return allMatching;
    }

    /**
     * Lists all accesses.
     * @return list of all accesses
     */
    public List<Access> listAll() {
        return accessList;
    }
}
