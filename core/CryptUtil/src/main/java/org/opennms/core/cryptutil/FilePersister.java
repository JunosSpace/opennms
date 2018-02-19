package org.opennms.core.cryptutil;

import java.io.IOException;

import org.opennms.core.nmautils.NMACommand;
import org.opennms.core.nmautils.NMAResponse;


public class FilePersister implements CryptographKeyPersister {
	
	private static final int LOCK_BUSY_ERROR = 2;
	
	   public String readKey() {
		   NMACommand cmd = new NMACommand("localhost", "readAESKey.pl");
		    cmd.changeCommand("/local-cgi/readAESKey.pl");
		    cmd.setUseDefaultPassword(true);
		    String keyStr = null;
		    try {
		      NMAResponse response;
		      response = cmd.execute();
		      if (NMAResponse.FAILURE.equals(response.getStatus())) {
		        if (response.getErrorcode() == LOCK_BUSY_ERROR) {
		          throw new JMPRuntimeException("Another user is accessing AESKey file!");
		        } else {
		          throw new JMPRuntimeException("NMA read AESKey error!");
		        }
		      }
		      keyStr = response.getDetail();
		    } catch (IOException e) {
		      throw new JMPRuntimeException(e);
		    }
		    return keyStr;

}
}
