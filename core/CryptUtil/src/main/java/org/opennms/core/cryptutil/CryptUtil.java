package org.opennms.core.cryptutil;
import org.jboss.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class CryptUtil {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(CryptUtil.class);
	
	private CryptUtil() {

	  }
	
	public static String encodePwd(String text) {
	    String cipherText = "";
	    try {
	      String keyStr = AESKeyManager.getInst().getKey().getKeyStr();
	      cipherText = CryptService.getInst().encodePassword(text, keyStr);
	    } catch (Exception e) {
	      LOG.debug("Encode device password error, please check the AESKey!");
	      LOG.debug("Encode device password error details:", e);
	    }
	    return cipherText;
	  }

	  public static String decodePwd(String cipherText) {
	    String text = cipherText;
	    try {
	    String keyStr = AESKeyManager.getInst().getKey().getKeyStr();
	    	if(text.contains("IV:")){
	      text = CryptService.getInst().decodePassword(cipherText, keyStr);
	    	}	
	    } catch (Exception e) {
	      LOG.debug("Decode device password error, please check the AESKey!");
	      LOG.debug("Decode device password error details:", e);
	    }
	    return text;
	  }
	
}
