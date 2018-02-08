package com.constellio.app.modules.rm.pdfgenerator;

import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusMessageProvider;

public class FileStatus {
    private final String fileName;
    private String message;

    public FileStatus(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return getFileName() + ": " +getMessage();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileStatus that = (FileStatus) o;

        return fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return fileName.hashCode();
    }
}
