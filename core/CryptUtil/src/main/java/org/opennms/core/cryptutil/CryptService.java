package org.opennms.core.cryptutil;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import org.jboss.util.Base64;

public class CryptService {
	 private static CryptService inst;

	  public CryptService() {
	    super();
	  }
	  
	  public static CryptService getInst(){
		    if (inst == null){
		      inst = new CryptService();
		    }
		    return inst;
		  }
	  
	  private Key decodeKey(String keyStr) {
		    String base64Key = keyStr.substring("AESKEY:".length());
		    Key skey = new SecretKeySpec(Base64.decode(base64Key), "AES");
		    return skey;
		  }
	  
	  public String encodePassword(String passwordText, String keyStr) {
	      String pwdBase64 = new String(Base64.encodeBytes(passwordText.getBytes()));
	      return encrypt(pwdBase64, keyStr);
	  } 
	  
	  public String encrypt(String passwordText, String keyStr) {
	      AESCBCCryptography cbc = new AESCBCCryptography(decodeKey(keyStr));
	      return cbc.encrypt(passwordText);
	  }
	  public String decodePassword(String cipherText, String keyStr) {
		    String text = decrypt(cipherText, keyStr);
		    text = new String(Base64.decode(text));
		    
		    return text;
		  }
		  
		  public String decrypt(String cipherText, String keyStr){
		      String text = cipherText;
		     if (cipherText.contains("IV:")){
		        AESCBCCryptography cbc = new AESCBCCryptography(decodeKey(keyStr));
		        text = cbc.decrypt(cipherText);
		      }
		      return text;
		  }
}
