package com.ed;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.io.File;

/**
 * Created by shicongyu01_91 on 2015/3/2.
 */
public class JsoupTest {
    @Test
    public void testJsoup() throws Exception {
        File file = new File("C:\\Users\\shicongyu01_91\\Desktop\\index.html");
        String content = FileUtils.readFileToString(file,"gb2312");
        Document doc = Jsoup.parse("");
        Element ele = doc.select("#email_reg > p:nth-child(7) > span.info_tips").first();
        System.out.println(ele.html());
    }
}
