package com.cambiolabs.citewrite.util.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CrypterDataUtil{

	
    private static final String AESCBCPKCS5PADDING = "AES/CBC/PKCS5Padding";
    private static final String UTF8 = "UTF-8";
    public static String AES = "AES";
    public static Integer AESKEYBITS128 = 128;
    public static Integer AESKEYBITS256 = 256;
    

    public CrypterDataUtil() {
    }

    private static String toHexadecimal(byte[] digest) {
        
        String hash = "";
        for (byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) {
                hash += "0";
            }
            hash += Integer.toHexString(b);
        }
        return hash;
    }

    public static byte[] toByteArray(String text) {
        
        int len = text.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(text.charAt(i), 16) << 4)
                    + Character.digit(text.charAt(i + 1), 16));
        }
        return data;
    }

    public String getStringMessageCryptAES(String message, String key){

        SecretKeySpec skeySpec = null;
        IvParameterSpec ivSpec = null;
        Cipher cipher = null;
        String encryptMessage = null;

        try {

            skeySpec = new SecretKeySpec(toByteArray(key), AES);
            ivSpec = new IvParameterSpec(getIVAES());
            cipher = Cipher.getInstance(AESCBCPKCS5PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
            encryptMessage = toHexadecimal(cipher.doFinal(message.getBytes(UTF8)));

        } catch (Exception e) {
        	System.out.println(e.getMessage());
        }

        return encryptMessage;
    }

    public String getStringMessageDecryptAES(String message, String key){

        SecretKeySpec skeySpec = null;
        IvParameterSpec ivSpec = null;
        Cipher cipher = null;
        String decryptMessage = null;
        

        try {

            skeySpec = new SecretKeySpec(toByteArray(key), AES);
            ivSpec = new IvParameterSpec(getIVAES());
            cipher = Cipher.getInstance(AESCBCPKCS5PADDING);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            decryptMessage = new String(cipher.doFinal(toByteArray(message)), UTF8);

        } catch (Exception e) {
        }

        return decryptMessage;
    }

    public String getSecureRandomKeyGenerator(String algorithm, Integer bits){

        KeyGenerator keygen = null;
        String secureRandomKey = null;
        byte[] key = null;

        try {
            keygen = KeyGenerator.getInstance(algorithm);
            keygen.init(bits);
            key = keygen.generateKey().getEncoded();
            secureRandomKey = toHexadecimal(key);
        } catch (Exception e) {
        }
        return secureRandomKey;
    }
    
    private static byte[] getIVAES(){
    	return new byte[] {8, 6, 9, 4, 5, 5, 1, 3, 8, 8, 7, 3, 2, 5, 9, 7};
    }
    
}
