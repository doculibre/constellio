package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.entities.FolderUnicityVO;

import java.util.Collections;
import java.util.List;

public class FolderUniqueKeyDataProvider extends AbstractDataProvider {

    private List<FolderUnicityVO> folderUnicityVOs;

    public FolderUniqueKeyDataProvider(List<FolderUnicityVO> folderUnicityVOs) {
        this.folderUnicityVOs = folderUnicityVOs;
    }

    public FolderUnicityVO get(int index) {
        return folderUnicityVOs.get(index);
    }

    public FolderUnicityVO get(String metadataCode) {
        FolderUnicityVO match = null;
        for (FolderUnicityVO summaryColumnVO : folderUnicityVOs) {
            if (summaryColumnVO.getMetadataVO().getCode().equals(metadataCode)) {
                match = summaryColumnVO;
                break;
            }
        }
        return match;
    }

    public List<FolderUnicityVO> getFolderUnicityVOs() {
        return Collections.unmodifiableList(folderUnicityVOs);
    }

    public void setFolderUnicityVOs(List<FolderUnicityVO> folderUnicityVOs) {
        this.folderUnicityVOs = folderUnicityVOs;
    }

    public void addFolderUnicityVO(int index, FolderUnicityVO summaryColumnVO) {
        folderUnicityVOs.add(index, summaryColumnVO);
    }

    public void addFolderUnicityVO(FolderUnicityVO summaryColumnVO) {
        folderUnicityVOs.add(summaryColumnVO);
    }

    public void removeFolderUnicityVO(FolderUnicityVO summaryColumnVO) {
        folderUnicityVOs.remove(summaryColumnVO);
    }

    public FolderUnicityVO removeFolderUnicityVO(int index) {
        return folderUnicityVOs.remove(index);
    }

    public void clear() {
        folderUnicityVOs.clear();
    }

}
