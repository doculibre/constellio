package com.constellio.model.services.thesaurus;

import com.constellio.data.dao.managers.StatefulService;

import java.io.FileInputStream;
import java.io.InputStream;

public class ThesaurusManager implements StatefulService {

    private ThesaurusService thesaurus;
    InputStream skosFileStream;

    public ThesaurusManager(FileInputStream fileInputStream) {
        this.skosFileStream = fileInputStream;
        initialize();
    }

    /**
     * Gets thesaurus in non persistent memory.
     *
     * @return
     */
    public ThesaurusService get() {
        return thesaurus;
    }

    public void set(FileInputStream fileInputStream) {
        skosFileStream = fileInputStream;
        initialize();
    }

    /**
     * Saves Thesaurus to persistent memory.
     * @return true if success
     */
    public boolean save(){
        // TODO continue
        return false;
    }

    @Override
    public void initialize() {
        if(skosFileStream != null) {
            this.thesaurus = ThesaurusBuilder.getThesaurus(skosFileStream);
        }
    }

    @Override
    public void close() {
        // nothing to close (no threads to kill)
    }
}
