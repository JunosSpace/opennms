package org.opennms.core.nmautils;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;



/**
 * Created by IntelliJ IDEA. User: build2 Date: Mar 4, 2009 Time: 11:29:23 PM To change this
 * template use File | Settings | File Templates.
 */
public class NMAResponse {
	private static final Logger log = LoggerFactory.getLogger(NMAResponse.class);
  private String msg = null;
  private String detail;
  private String status;
  private int errorcode = 0;
  public static final String SUCCESS = "SUCCESS";
  public static final String FAILURE = "FAILURE";

  public NMAResponse(String msg) {
    this.msg = msg;
    try {
      ByteArrayInputStream input = new ByteArrayInputStream(msg.getBytes());
      JAXBContext jc = JAXBContext.newInstance(Response.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();

      Response resp = (Response) unmarshaller.unmarshal(input);

      if (resp.getMessage() != null)
        detail = (String) resp.getMessage();

      if (resp.getErrorcode() != null && resp.getErrorcode() > 0) errorcode = resp.getErrorcode();

      if (resp.getStatus() != null) status = resp.getStatus();

    } catch (Exception e) {
      status = FAILURE;
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  public String toString() {
    return msg;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getErrorcode() {
    return errorcode;
  }

  public void setErrorcode(int errorcode) {
    this.errorcode = errorcode;
  }


}