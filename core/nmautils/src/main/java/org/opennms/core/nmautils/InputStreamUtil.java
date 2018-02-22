package org.opennms.core.nmautils;

import java.io.*;
import java.util.ArrayList;
/**
 * Created by IntelliJ IDEA. User: build2 Date: Mar 5, 2009 Time: 12:31:52 AM
 */
public class InputStreamUtil {
  private InputStreamReader in = null;
  private StringWriter writer = null;
 // private static Logger logger = Logger.getLogger(InputStreamUtil.class);

  public InputStreamUtil(InputStream in) {
    this.in = new InputStreamReader(in);
  }

  public InputStreamUtil(InputStream in, String charset) {
    try {
      this.in = new InputStreamReader(in, charset);
    } catch (UnsupportedEncodingException ex) {
      //logger.error("Invalid charset: " + charset, ex);
    }
  }

  public String toString() {
    if (writer == null) {
      writer = new StringWriter();
      try {
        for (int c = in.read(); c != -1; c = in.read()) {
          writer.write(c);
        }
      } catch (IOException ex) {
       // logger.error("Error reading the InputStream", ex);
        writer = null;
        return null;
      }
    }
    return writer.toString();
  }

  public static byte[] toBytes(InputStream is) {
    ArrayList<Byte> bytes = new ArrayList<Byte>();
    try {
      for (int c = is.read(); c != -1; c = is.read()) {
        bytes.add((byte) c);
      }
    } catch (IOException ex) {
     // logger.error("Error reading the InputStream", ex);
      return null;
    }
    byte[] retval = new byte[bytes.size()];
    for (int i = 0; i < retval.length; i++) {
      retval[i] = bytes.get(i);
    }
    return retval;
  }
}
