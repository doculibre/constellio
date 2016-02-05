package com.constellio.dev;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtractDocumentTypeCodes {
    public static void main(String[] args) {
        File file = new File("/Users/rodrigue/constellio-dev-2015-10-08/documentTypes.txt");
        assertThat(file).exists();


        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
            String line = "";
            while(StringUtils.isNotBlank(line = bufferedReader.readLine())){
                String codePart = StringUtils.substringAfter(line, "code:");
                if(StringUtils.isNotBlank(codePart)) {
                    if (StringUtils.split(codePart, ", ").length > 0){
                        String code = StringUtils.split(codePart, ", ")[0];
                        System.out.println(code);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
