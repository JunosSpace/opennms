package org.opennms.core.cryptutil;
import java.security.Key;

public class JmpSecurityKey {

  private String keyId;

  private String keyStr;
  
  private Key key;

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }

  public Key getKey() {
    return key;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public String getKeyStr() {
    return keyStr;
  }

  public void setKeyStr(String keyStr) {
    this.keyStr = keyStr;
  }
}
