package hudson.tasks.junit.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TrimStdioUtil;
import hudson.util.io.ParserConfigurator;
import org.dom4j.io.XMLWriter;

/**
 * Created by tinghe on 17-4-11.
 */
public class TestParser {
    public static class SuiteResultParserConfigurationContext {
        public final File xmlReport;

        SuiteResultParserConfigurationContext(File xmlReport) {
            this.xmlReport = xmlReport;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException,
                                          DocumentException {
        String xmlReportPath = "/opt/projects/github/junit-plugin/testXml/TEST-com.alipay.zdal.test.common.AllTestSuit.xml";
        //xmlReportPath = ""/tmp/TEST-com.alipay.zdal.test.common.AllTestSuit.xml"";
        File xmlReport = new File(xmlReportPath);
        SAXReader saxReader = new SAXReader();
        ParserConfigurator.applyConfiguration(saxReader, new SuiteResultParserConfigurationContext(
            xmlReport));
        saxReader.addHandler("/testsuite/testcase/error", TrimStdioUtil.getElementHandler());
        saxReader.addHandler("/testsuite/testcase/failure", TrimStdioUtil.getElementHandler());
        saxReader.addHandler("/testsuite/testcase/system-out", TrimStdioUtil.getElementHandler());
        saxReader.addHandler("/testsuite/testcase/system-err", TrimStdioUtil.getElementHandler());
        Document result = saxReader.read(xmlReport);
        Element root = result.getRootElement();
        FileOutputStream fos = new FileOutputStream(
            "/tmp/TEST-com.alipay.zdal.test.common.AllTestSuit.xml");
        XMLWriter xmlWriter = new XMLWriter(fos);
        xmlWriter.write(root);
        fos.close();
        parseSuite(xmlReport, false, null, root);
    }

    private static void parseSuite(File xmlReport, boolean keepLongStdio, List<SuiteResult> r,
                                   Element root) throws DocumentException, IOException {
        // nested test suites
        @SuppressWarnings("unchecked")
        List<Element> testSuites = (List<Element>) root.elements("testsuite");
        for (Element suite : testSuites) {
            parseSuite(xmlReport, keepLongStdio, r, suite);
        }

        // child test cases
        // FIXME: do this also if no testcases!
        if (root.element("testcase") != null || root.element("error") != null) {
            List<Element> testCases = (List<Element>) root.elements("testcase");
            for (Element testCase : testCases) {
                System.out.println("testcase: " + testCase);
                Element error = testCase.element("error");
                Element sout = testCase.element("system-out");
                Element serr = testCase.element("system-err");
                Element failure = testCase.element("failure");
                if (null != error) {
                    System.out.println(error.getText());
                }
                if (null != sout) {
                    System.out.println(sout.getText());
                }
                if (null != serr) {
                    System.out.println(serr.getText());
                }
                if (null != failure) {
                    System.out.println(failure.getText());
                }
            }
        }
    }

}
