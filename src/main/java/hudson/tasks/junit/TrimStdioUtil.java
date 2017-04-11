package hudson.tasks.junit;

import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;

/**
 * Created by tinghe on 17-4-11.
 */
public class TrimStdioUtil {
    public static final int      HALF_BEGIN_SIZE = 2500;
    public static final int      HALF_END_SIZE   = 5000;
    public static final int      HALF_TOTAL_SIZE = HALF_BEGIN_SIZE + HALF_END_SIZE;
    public static ElementHandler elementHandler;

    public static String possiblyTrimStdio(String stdio) { // HUDSON-6516
        if (stdio == null) {
            return null;
        }
        int len = stdio.length();
        int middle = len - HALF_TOTAL_SIZE;
        if (middle <= 0) {
            return stdio;
        }
        StringBuffer sb = new StringBuffer(stdio.subSequence(0, HALF_BEGIN_SIZE));
        sb.append("\n...[truncated " + middle + " chars]...\n");
        sb.append(stdio.subSequence(len - HALF_END_SIZE, len));
        return sb.toString();
    }

    public synchronized static ElementHandler getElementHandler() {
        if (null == elementHandler) {
            elementHandler = new ElementHandler() {
                public void onStart(ElementPath path) {
                }

                public void onEnd(ElementPath path) {
                    if (path != null) {
                        Element row = path.getCurrent();
                        if (row != null && row.getText() != null
                            && row.getText().length() > HALF_TOTAL_SIZE) {
                            row.setText(possiblyTrimStdio(row.getText()));
                        }
                    }
                }
            };
        }
        return elementHandler;
    }
}
