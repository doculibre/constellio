package com.constellio.app.modules.es.connectors.smb.cache;

import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;

import java.io.*;

/**
 * Created by admin on 2017-06-05.
 */
public class ContextReader {
    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        System.out.println("File : " + file.getAbsolutePath());
        String url = args[1];
        System.out.println("URL : " + url);

        try (FileInputStream fis = new FileInputStream(file)) {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fis));
            SmbConnectorContext context = (SmbConnectorContext) ois.readObject();
            SmbModificationIndicator modification = context.recordUrls.get(url);
            if (modification == null) {
                System.out.println("URL not found !");
            } else {
                System.out.println("LastModified " + modification.getLastModified());
                System.out.println("ParentId " + modification.getParentId());
                System.out.println("PermissionsHash " + modification.getPermissionsHash());
                System.out.println("TraversalCode " + modification.getTraversalCode());
                System.out.println("Size " + modification.getSize());
            }
        }
    }
}
