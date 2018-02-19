package org.opennms.core.cryptutil;

public interface CryptographManager {
	String encrypt(String text);

	String decrypt(String cipherText);
}
