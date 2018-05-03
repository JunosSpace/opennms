package org.opennms.core.cryptutil;

public class JMPRuntimeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public JMPRuntimeException() {
    super();
  }

  public JMPRuntimeException(String errmsg) {
    super(errmsg);
  }

  public JMPRuntimeException(String errmsg, Throwable e) {
    super(errmsg, e);
  }

  public JMPRuntimeException(Throwable e) {
    super(e);
  }
}
