package org.opennms.core.cryptutil;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import org.jboss.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class AESKeyManager implements CryptographKeyManager {

	private static final transient Logger LOG = LoggerFactory.getLogger(AESKeyManager.class);

	private static final String AESKEY_PREFIX = "AESKEY:";

	private static final String KEY_ALGORITHM = "AES";
	
	private JmpSecurityKey jmpAESKey;

	private static final CryptographKeyManager inst = new AESKeyManager();

	private CryptographKeyPersister keyPersister;

	private AESKeyManager() {
		CryptographKeyPersister p = null;
		LOG.debug("AESKEY use file!");
		p = new FilePersister();
		setKeyPersister(p);
	}

	private CryptographKeyPersister getKeyPersister() {
		return keyPersister;
	}

	private void setKeyPersister(CryptographKeyPersister keyPersister) {
		this.keyPersister = keyPersister;

	}

	public static CryptographKeyManager getInst() {
		return inst;
	}
	
	 public JmpSecurityKey getKey() {
		    if (jmpAESKey == null) {
		      String keyStr = getKeyStr();
		      LOG.debug("before decode AESKEY:" + keyStr);
		      jmpAESKey = decodeKey(keyStr);
		    }
		    LOG.debug("AESKEY get jmpAESKey:"+ jmpAESKey.getKeyStr());
		    return jmpAESKey;
		  }

	private String getKeyStr() {
		LOG.debug("before Read_AESKEY_DEBUG:");
		String keyStr = getKeyPersister().readKey();
		LOG.debug("Read_AESKEY_DEBUG:" + keyStr);
		if (keyStr == null || keyStr.trim().length() == 0) {
				throw new JMPRuntimeException(
						"Not find the AESKey at Slave node! Please check does the NMA syncFile work right.");
			
		}
		return keyStr;
	}

	 private JmpSecurityKey decodeKey(String keyStr) {
		 LOG.debug("JMPKEY_DEBUG start:"+keyStr);
		    String keyId = keyStr.substring(0, AESKEY_PREFIX.length());
		    String base64Key = keyStr.substring(AESKEY_PREFIX.length());
		    Key skey = new SecretKeySpec(Base64.decode(base64Key), KEY_ALGORITHM);
		    JmpSecurityKey jmpKey = new JmpSecurityKey();
		    jmpKey.setKeyStr(keyStr);
		    LOG.debug("JMPKEY_DEBUG:"+keyStr);
		    jmpKey.setKeyId(keyId);
		    jmpKey.setKey(skey);
		    return jmpKey;
		  }

}
