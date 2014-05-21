package me.guard.car;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

public class EncryptedJSONObject extends JSONObject {
  private final Cipher cipher;

  public EncryptedJSONObject(Map copyFrom) {
    super(copyFrom);

    try {
      cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    } catch (NoSuchAlgorithmException e) {
      throw fail(e);
    } catch (NoSuchPaddingException e) {
      throw fail(e);
    }
  }

  @Override
  public String toString() {
    final String salt = random(32);
    final String iv = random(16);
    final String unencryptedData = super.toString();
    HashMap<String, Object> encryptedData = new HashMap<String, Object>() {{
      put("salt", salt);
      put("iv", iv);
      put("data", encrypt(salt, iv, "fooobaar", unencryptedData));
    }};

    return new JSONObject(encryptedData).toString();
  }

  private String encrypt(String salt, String iv, String passPhrase, String plainText) {
    try {
      SecretKey key = generateKey(salt, passPhrase);
      byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, key, iv, plainText.getBytes("UTF-8"));
      return base64(encrypted);
    } catch (UnsupportedEncodingException e) {
      throw fail(e);
    }
  }

  private byte[] doFinal(int encryptMode, SecretKey key, String iv, byte[] bytes) {
    try {
      cipher.init(encryptMode, key, new IvParameterSpec(hex(iv)));
      return cipher.doFinal(bytes);
    } catch (InvalidKeyException e) {
      throw fail(e);
    } catch (InvalidAlgorithmParameterException e) {
      throw fail(e);
    } catch (IllegalBlockSizeException e) {
      throw fail(e);
    } catch (BadPaddingException e) {
      throw fail(e);
    }
  }

  private SecretKey generateKey(String salt, String passphrase) {
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), 10000, 128);
      return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    } catch (NoSuchAlgorithmException e) {
      throw fail(e);
    } catch (InvalidKeySpecException e) {
      throw fail(e);
    }
  }

  public static String random(int length) {
    byte[] salt = new byte[length];
    new SecureRandom().nextBytes(salt);
    return hex(salt);
  }

  public static String base64(byte[] bytes) {
    return new String(Base64.encodeBase64(bytes));
  }

  public static String hex(byte[] bytes) {
    return new String(Hex.encodeHex(bytes));
  }

  public static byte[] hex(String str) {
    try {
      return Hex.decodeHex(str.toCharArray());
    } catch (DecoderException e) {
      throw new IllegalStateException(e);
    }
  }

  private IllegalStateException fail(Exception e) {
    return new IllegalStateException(e);
  }
}
