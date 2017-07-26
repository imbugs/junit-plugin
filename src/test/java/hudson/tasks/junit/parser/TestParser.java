package hudson.tasks.junit.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.xerces.impl.io.MalformedByteSequenceException;
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

    public static void trimUTF8(File xmlReport) throws IOException {
        File tempFile = File.createTempFile(xmlReport.getName() + ".normal.", ".xml",
            xmlReport.getParentFile());
        FileWriter fw = new FileWriter(tempFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
            xmlReport), "UTF-8"));
        for (String line; (line = reader.readLine()) != null;) {
            String normalized = Normalizer.normalize(line, Normalizer.Form.NFD);
            fw.write(normalized);
            fw.write('\n');
        }
        fw.close();
        FileUtils.deleteQuietly(xmlReport);
        FileUtils.moveFile(tempFile, xmlReport);
    }

    public static void main(String[] args) throws DocumentException, InterruptedException,
                                          IOException {
        String xmlReportPath = "/home/tinghe/tinghe-source/aclinkelib/extern/surecool/testcase1/badxml/sub2/junitreports/TEST-com.alipay.vouchercore.servicetest.voucher.voucherqueryfacade.QueryWithSubAggregateVoucherInfoByIdsNormalTest.xml";
        //xmlReportPath = ""/tmp/TEST-com.alipay.zdal.test.common.AllTestSuit.xml"";
        File xmlReport = new File(xmlReportPath);
        retryParse(xmlReport, true);
    }

    static List<SuiteResult> retryParse(File xmlReport, boolean keepLongStdio)
                                                                              throws DocumentException,
                                                                              IOException,
                                                                              InterruptedException {
        try {
            return parse(xmlReport, true);
        } catch (DocumentException e) {
            System.out.println("========== TRIM UTF8 ");
            e.printStackTrace();
            trimUTF8(xmlReport);
            return parse(xmlReport, true);
        }
    }

    static List<SuiteResult> parse(File xmlReport, boolean keepLongStdio) throws DocumentException,
                                                                         IOException,
                                                                         InterruptedException {

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
        return null;
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
