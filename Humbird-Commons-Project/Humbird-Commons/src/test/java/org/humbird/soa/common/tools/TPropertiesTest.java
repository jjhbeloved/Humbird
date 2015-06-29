package org.humbird.soa.common.tools;

import org.humbird.soa.common.model.common.PropsModel;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class TPropertiesTest {

    @Test
    public void testSaveProperties() throws Exception {
        InputStream stream = TPropertiesTest.class.getResourceAsStream("/receiveBillZip.properties");

        InputStreamReader inputStreamReader = new InputStreamReader(stream);
        Reader reader = inputStreamReader;
        BufferedReader bufferedReader = new BufferedReader(reader);
        String lines = null;
        PropsModel propsModel = new PropsModel();

        while((lines = bufferedReader.readLine()) != null) {
            propsModel.add(lines);
        }
        int siz = propsModel.getProps().size();
        int c = 1;
        for(int i=0; i<siz; i++) {
            PropsModel.Prop prop = propsModel.getProps().get(i);
            if(prop == null) {
                System.out.println(c++);
            } else {
                PropsModel.Comments comments = propsModel.getComms().get(i);
                if (comments != null) {
                    int j = 0;
                    for (String comm : comments.getComments()) {
                        System.out.println(c++ + " # " + comm);
                    }
                }
                System.out.println(c++ + " " + prop.getKey() + " = " + prop.getVal());
            }
        }
    }

    private static boolean isComment(String lines) {
        return lines.charAt(0) == '#';
    }
}