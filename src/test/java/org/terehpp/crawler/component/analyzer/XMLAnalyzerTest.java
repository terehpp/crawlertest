package org.terehpp.crawler.component.analyzer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terehpp.crawler.model.Entry;

import java.io.File;

public class XMLAnalyzerTest {
    private static XMLAnalyzerImpl<Entry> analyzer;

    @Before
    public void setUp() {
        analyzer = new XMLAnalyzerImpl<>(Entry.class, getPathToFile("entry.xsd"));
    }

    @Test
    public void successTest() {
        AnalyzerResult<Entry> result = analyzer.analyze(getPathToFile("test.xml"), 1L);
        Assert.assertEquals(false, result.isError());
    }

    @Test
    public void failTest() {
        try {
            AnalyzerResult<Entry> result = analyzer.analyze("fileNotFound.xml", 1L);
            Assert.assertEquals(true, result.isError());

            result = analyzer.analyze(getPathToFile("error.xml"), 1L);
            Assert.assertEquals(true, result.isError());

            result = analyzer.analyze(getPathToFile("invalidContent.xml"), 1L);
            Assert.assertEquals(true, result.isError());

            result = analyzer.analyze(getPathToFile("invalidDate.xml"), 1L);
            Assert.assertEquals(true, result.isError());
        } catch (Exception e) {
            Assert.fail("Analyzer should not throw exceptions: " + e.getMessage());
        }
    }

    private String getPathToFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file.getAbsolutePath();
    }
}
