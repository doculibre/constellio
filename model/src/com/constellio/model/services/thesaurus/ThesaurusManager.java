package com.constellio.model.services.thesaurus;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.services.thesaurus.exception.ThesaurusInvalidFileFormat;

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

    public void set(InputStream fileInputStream) {
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
            try {
                this.thesaurus = ThesaurusBuilder.getThesaurus(skosFileStream);
            } catch (ThesaurusInvalidFileFormat thesaurusInvalidFileFormat) {
                thesaurusInvalidFileFormat.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        // nothing to close (no threads to kill)
    }
}
