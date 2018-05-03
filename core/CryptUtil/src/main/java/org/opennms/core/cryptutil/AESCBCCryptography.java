
package org.opennms.core.cryptutil;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.jboss.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AESCBCCryptography implements CryptographManager {

	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";
	private static final String KEY_ALGORITHM = "AES";
	private static final String CBC_PREFIX = "CBC:";
	private static final String IV_PREFIX = "IV:";

	private Key aesKey;

	private Cipher cipher;

	public AESCBCCryptography(Key aesKey) {
		this.aesKey = aesKey;
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private Cipher getCipher() {
		if (cipher == null) {
			try {
				cipher = Cipher.getInstance(CIPHER_ALGORITHM, "BC");
			} catch (NoSuchAlgorithmException e) {
				handleException(e);
			} catch (NoSuchProviderException e) {
				handleException(e);
			} catch (NoSuchPaddingException e) {
				handleException(e);
			}
		}
		return cipher;
	}

	private void handleException(Exception e) {
	    throw new RuntimeException("Get cipher instance error!", e);
	  }
	
	private byte[] getRandomIV() {
		byte[] iv = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);
		return iv;
	}

	private static String[] parseCipherText(String cipherText) {
		// index 0: iv; index 1: cipher text;
		// 24IV:hFBrZWBjXfSDeKy15XhHdA==CBC:qL7kwfWPDukrOyZMIQiTLbHMYpsKl2IIwnc01Smar0Q=
		String[] str = new String[2];
		int iv_idx = cipherText.indexOf(IV_PREFIX);
		String str_len = cipherText.substring(0, iv_idx);
		int iv_len = Integer.valueOf(str_len).intValue();

		str[0] = cipherText.substring(iv_idx + IV_PREFIX.length(), iv_idx
				+ IV_PREFIX.length() + iv_len); // iv string
		str[1] = cipherText.substring(iv_idx + IV_PREFIX.length() + iv_len
				+ CBC_PREFIX.length()); // cipher text string
		return str;
	}

	private static String assembleCipherText(String iv, String cipher) {
		String str = iv.length() + IV_PREFIX + iv + CBC_PREFIX + cipher;
		return str;
	}

	public String encrypt(String text) {
		// cipherText form: keyID+Base64(aesCipherText)
		String cipherText = text;
		Cipher aesCipher = getCipher();
		try {
			byte[] iv = getRandomIV();
			AlgorithmParameters params = AlgorithmParameters
					.getInstance(KEY_ALGORITHM);
			params.init(new IvParameterSpec(iv));
			aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, params);
			byte[] b = aesCipher.doFinal(cipherText.getBytes());
			cipherText = assembleCipherText(encodeBase64(iv), encodeBase64(b));
		} catch (InvalidKeyException e) {
			handleEncryptException(e);
		} catch (IllegalBlockSizeException e) {
			handleEncryptException(e);
		} catch (BadPaddingException e) {
			handleEncryptException(e);
		} catch (InvalidAlgorithmParameterException e) {
			handleEncryptException(e);
		} catch (NoSuchAlgorithmException e) {
			handleEncryptException(e);
		} catch (InvalidParameterSpecException e) {
			handleEncryptException(e);
		}
		return cipherText;
	}

	private void handleEncryptException(Exception e) {
		throw new RuntimeException("do encrypt error!", e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.juniper.jmp.crypt.CryptographManager#decrypt(java.lang.String)
	 */
	public String decrypt(String cipherText) {
		String text = null;

		if (isEncryptByAES(cipherText, IV_PREFIX)) {
			Cipher aesCipher = getCipher();
			try {
				String[] str = parseCipherText(cipherText);
				byte[] iv = decodeBase64(str[0]);
				AlgorithmParameters params = AlgorithmParameters
						.getInstance(KEY_ALGORITHM);
				params.init(new IvParameterSpec(iv));
				String cipherBase64 = str[1];
				aesCipher.init(Cipher.DECRYPT_MODE, aesKey, params);
				byte[] b = aesCipher.doFinal(decodeBase64(cipherBase64));
				text = new String(b);

			} catch (InvalidKeyException e) {
				handleDecryptException(e);
			} catch (IllegalBlockSizeException e) {
				handleDecryptException(e);
			} catch (BadPaddingException e) {
				handleDecryptException(e);
			} catch (InvalidAlgorithmParameterException e) {
				handleDecryptException(e);
			} catch (NoSuchAlgorithmException e) {
				handleDecryptException(e);
			} catch (InvalidParameterSpecException e) {
				handleDecryptException(e);
			}
		} else {
			text = cipherText;
		}
		return text;
	}
	
	 private void handleDecryptException(Exception e) {
		    throw new RuntimeException("do decrypt error!", e);
		  }
	 
	private boolean isEncryptByAES(String cipherText, String keyId) {
		boolean b = false;
		if (cipherText.contains(keyId)) {
			b = true;
		}
		return b;
	}

	private String encodeBase64(byte[] b) {
		String str = Base64.encodeBytes(b);
		return str;
	}

	private byte[] decodeBase64(String str) {
		return Base64.decode(str);
	}
}
