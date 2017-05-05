package project.unitato.encrypchat;


import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;


public class Encrypter {
	
	
	private String AESkey;
	
	private KeyPair keyPair;
	
	
	
	
	
	
	/*
	
	
	
	SharedPreferences prefs = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    String key = prefs.getString(PRIVATE_KEY_KEY, "");

    KeyPair kp;
    
    if (key.equals("")) {
        // generate KeyPair
        kp = Encriptor.GenerateRSAKeyPair();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o;
        try {
            o = new ObjectOutputStream(b);
            o.writeObject(kp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] res = b.toByteArray();
        String encodedKey = Base64.encodeToString(res, Base64.DEFAULT);

        prefs.edit().putString(PRIVATE_KEY_KEY, encodedKey).commit();

    } else {
        // read the KeyPair from internal storage
        byte[] res = Base64.decode(key, Base64.DEFAULT);
        ByteArrayInputStream bi = new ByteArrayInputStream(res);
        ObjectInputStream oi;
        try {
            oi = new ObjectInputStream(bi);
            Object obj = oi.readObject();
            kp = (KeyPair) obj;
            Log.w("Cipher", ((KeyPair) obj).toString());
        } catch (Exception e)
        {
        
        }
    }
	
	*/
	
	
	
	
	public Encrypter()
	{
		AESkey = GenerateAESKeyString();
		keyPair = GenerateRSAKeyPair();
	}
	
	
	public Encrypter(String AESKey)
	{
		AESkey = AESKey;
		//keyPair = GenerateRSAKeyPair();
	}

    public void GenerateKeyPair()
    {
        keyPair = GenerateRSAKeyPair();
    }

	
	public Encrypter(String aesKey, KeyPair kp)
	{
		keyPair = kp;
		AESkey = aesKey;
	}
	
	
	public void setAESKey(String newKey)
	{
		AESkey = newKey;
	}
	
	public String getAesKey()
	{
		return AESkey;
	}
	
	
	
	public String getPrivateKey()
	{
		return private2string(keyPair.getPrivate());
	}
	
	public String getPublicKey()
	{
		return public2string(keyPair.getPublic());
	}
	
	
	
	public void setKeyPair(String publicKey, String privateKey)
	{
		keyPair = new KeyPair(string2public(publicKey), string2private(privateKey));
	}
	
	
	
	public String Encrypt(String message) throws Exception
	{
		return AESEncrypt(AESkey, message);
	}
	
	
	public String Decrypt(String message) throws Exception
	{
		return AESDecrypt(AESkey, message);
	}
	
	
	
	
	public static void startSampleTransmission()
	{
		//=======ALICE======
		KeyPair kp = GenerateRSAKeyPair();
		PublicKey publicKey = kp.getPublic();
		String pkStr = public2string(publicKey);
		String prStr = private2string(kp.getPrivate());
		//SEND publicKey
		
		//========BOB=======
		String aesKey = GenerateAESKeyString();
		String encryptedAesKey = RSAEncrypt(aesKey, pkStr);
		//SEND encryptedAesKey
		
		
		//=======ALICE======
		String decryptedAesKey = RSADecrypt(encryptedAesKey, prStr);
		String message = "HELLO WORLD";
		String toSend = AESEncrypt(decryptedAesKey, message);
		//SEND toSend
		
		
		//========BOB=======		
		String decryptedMsg = "";
		try {
			decryptedMsg = AESDecrypt(aesKey, toSend);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.i("strings", decryptedMsg);
	}
	
	
	
	
	
	static public String GenerateAESKeyString()
	{
		Random r = new Random();
		String keyString = "";
		for(int i = 0; i < 16; i++){
			if(r.nextBoolean())
				keyString += (char) r.nextInt(128);
			else
				keyString += (char) r.nextInt(128);
		}
		return keyString;
	}
	
	//testing function
	public static void TestRun()
	{
		String str = "Hello World";
		String tmpKey = GenerateAESKeyString();//"123qweasdzxc!@#$";
	    String msgString = AESEncrypt(tmpKey, str);
	    String end = null;
	    try {
	    	/* 	//OLD SYNTAX:
			decryptedMsg = AESDecrypt(tmpKey, hex2byte(msgString.getBytes()));
			end = new String(decryptedMsg);
			*/
	    	
	    	end = AESDecrypt(tmpKey, msgString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	}
	
	
	
	/*
	 * 
	 * Encriptor enc = new Encriptor(sharedprefs.getPref("EASkey" + chatId);
	 * 
	 * MessageReceivedListener()
	 * {
	 * 		OnReceive(String Message)
	 * 		{
	 * 			enc.Decrypt(message);
	 * 			textview.settext(this^);
	 * 		}
	 * }
	 * 
	 * 
	 * NewMessage(String number)
	 * {
	 * 		KeyPair kp = Encriptor.GenerateKeyPair();
	 * }
	 */
	
	
	
	
	
	
	
	
	
	
	
	
	
	 // utility function
	 public static String byte2hex(byte[] b) 
	 {
		 String hs = "";
		 String stmp = "";
		 for (int n = 0; n < b.length; n++) 
		 {
			 stmp = Integer.toHexString(b[n] & 0xFF);
			 if (stmp.length() == 1)
				 hs += ("0" + stmp);
			 else
				 hs += stmp;
		 }
		 return hs.toUpperCase();
	 }
	 
	 
	 // utility function: convert hex array to byte array
	 public static byte[] hex2byte(String hex) 
	 {
		 byte[] b = hex.getBytes();
		 if ((b.length % 2) != 0)
			 throw new IllegalArgumentException("hello");
	
		 byte[] b2 = new byte[b.length / 2];
	
		 for (int n = 0; n < b.length; n += 2) 
		 {
			 String item = new String(b, n, 2);
			 b2[n / 2] = (byte) Integer.parseInt(item, 16);
		 }
		 return b2;
	 }
	 
	 
	 
	 

	 // encryption function
	 public static String AESEncrypt(String keyString, String contentString) 
	 {
	
		 try 
		 {
			 byte[] returnArray;
		
			 
			 
			 // generate AES secret key from user input
			 Key key = AESKeyFromString(keyString);
		
			 // specify the cipher algorithm using AES
			 Cipher c = Cipher.getInstance("AES");
		
			 // specify the encryption mode
			 c.init(Cipher.ENCRYPT_MODE, key);
		
			 // encrypt
			 returnArray = c.doFinal(contentString.getBytes());
		
			 return byte2hex(returnArray);
	
		 } catch (Exception e) {
			 e.printStackTrace();
			 byte[] returnArray = null;
			 return byte2hex(returnArray);
		 }
	
	 }
	 
	 
	 
	 
	 
	 
	 public String AESEncrypt(String contentString) 
	 {
	
		 try 
		 {
			 byte[] returnArray;
		
			 
			 
			 // generate AES secret key from user input
			 Key key = AESKeyFromString(AESkey);
		
			 // specify the cipher algorithm using AES
			 Cipher c = Cipher.getInstance("AES");
		
			 // specify the encryption mode
			 c.init(Cipher.ENCRYPT_MODE, key);
		
			 // encrypt
			 returnArray = c.doFinal(contentString.getBytes());
		
			 return byte2hex(returnArray);
	
		 } catch (Exception e) {
			 e.printStackTrace();
			 byte[] returnArray = null;
			 return byte2hex(returnArray);
		 }
	
	 }
	
	
	
	 // decryption function
	 public static String AESDecrypt(String secretKeyString, String encryptedMsgString) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	 {
		 byte[] encryptedMsg = hex2byte(encryptedMsgString);
		 
		 // generate AES key from the user input secret key
		 Key key = AESKeyFromString(secretKeyString);
	
		 // get the cipher algorithm for AES
         try {
             Cipher c = Cipher.getInstance("AES");


             // specify the decryption mode
             c.init(Cipher.DECRYPT_MODE, key);

             // decrypt the message
             byte[] decValue = c.doFinal(encryptedMsg);

             String decStr = new String(decValue);
             return decStr;
         }catch (Exception e){
             return null;
         }
	 }
	 
	 
	 
	 
	 
	 public  String AESDecrypt(String encryptedMsgString) throws Exception 
	 {
		 return AESDecrypt(AESkey, encryptedMsgString);
	 }
	 
	
	
	
	 private static Key AESKeyFromString(String secretKeyString) {
		 // generate AES key from string
		 Key key = new SecretKeySpec(secretKeyString.getBytes(), "AES");
		 return key;
	 }

	
	
	
	
	
	
	
	
	
	public static String RSAEncrypt(String message, String publicKeyString)
	{
		String text = null;
		PublicKey publicKey = string2public(publicKeyString);
		try{
	        text = message;
	        Cipher cipher = Cipher.getInstance("RSA");
	        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
	        byte[] x = cipher.doFinal(text.getBytes());
	        text = getStr(x);
		}catch(Exception e){
			e.printStackTrace();
		}
		return text;
	}
	
	
	
	
	public String RSAEncrypt(String message)
	{
		String text = null;
		PublicKey publicKey = keyPair.getPublic();
		try{
	        text = message;
	        Cipher cipher = Cipher.getInstance("RSA");
	        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
	        byte[] x = cipher.doFinal(text.getBytes());
	        text = getStr(x);
		}catch(Exception e){
			e.printStackTrace();
		}
		return text;
	}
	
	
	
	
	
	
	public static KeyPair GenerateRSAKeyPair()
	{
		
		try{
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
	        KeyPair kp = keyGen.genKeyPair();
	        return kp;
		}catch(Exception e){
			e.printStackTrace();
		}
			return null;
	}
	
	
	
	public static String public2string(PublicKey key)
	{
	    byte[] keyBytes = key.getEncoded();
	    return byte2hex(keyBytes);
	}
	
	
	
	
	public static String private2string(PrivateKey key)
	{
		byte[] keyBytes = key.getEncoded();
	    return byte2hex(keyBytes);
	}
	
	
	
	
	public static PublicKey string2public(String keyString)
	{
		byte[] keyBytes = hex2byte(keyString);
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			PublicKey key = keyFactory.generatePublic(keySpec);
			return key;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	
	public static PrivateKey string2private(String keyString)
	{
		byte[] keyBytes = hex2byte(keyString);
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			PrivateKey key = keyFactory.generatePrivate(keySpec);
			return key;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	public static String private2string(KeyPair keypair)
	{
		PublicKey pk= GenerateRSAKeyPair().getPublic();
	    byte[] b = pk.getEncoded();
		String s = byte2hex(b);
		byte[] c = hex2byte(s);
		
		
		String algorithm = "RSA";

	    PrivateKey privateKey = keypair.getPrivate();
	    PublicKey publicKey = keypair.getPublic();
	    
	    Key k = publicKey;
	    PublicKey pk1 = (PublicKey) k;
	    boolean SUCCESS = pk1.equals(publicKey);
	    
	    byte[] privateKeyBytes = privateKey.getEncoded();
	    byte[] publicKeyBytes = publicKey.getEncoded();
	    try{
	    KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
	    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
	    PrivateKey privateKey2 = keyFactory.generatePrivate(privateKeySpec);

	    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
	    PublicKey publicKey2 = keyFactory.generatePublic(publicKeySpec);

	    // The orginal and new keys are the same
	    boolean same1 = privateKey.equals(privateKey2); 
	    boolean same2 = publicKey.equals(publicKey2);
	    Log.i("Lol", "llol");
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    return null;
	}
	
	
	
	
	
	
	public static KeyPair string2kp(String str)
	{
        return null;
	}
	
	
	
	
	public static String RSADecrypt(String message, String privateKeyString)
	{
		PrivateKey privateKey = string2private(privateKeyString);
		Log.d("Cipher", "Decrypt in: " + message);
		String text = null;
		try{
	 
	        text = message;
	        Cipher cipher = Cipher.getInstance("RSA");
	        
	        cipher.init(Cipher.DECRYPT_MODE, privateKey);
	        byte[] y = cipher.doFinal(getByte(message));
	        
	        text = new String(y);
		}catch(Exception e)
		{
		    e.printStackTrace();
		}
		return text;
	}
	
	
	
	
	
	
	public String RSADecrypt(String message)
	{
		PrivateKey privateKey = keyPair.getPrivate();
		Log.d("Cipher", "Decrypt in: " + message);
		String text = null;
		try{
	 
	        text = message;
	        Cipher cipher = Cipher.getInstance("RSA");
	        
	        cipher.init(Cipher.DECRYPT_MODE, privateKey);
	        byte[] y = cipher.doFinal(getByte(message));
	        
	        text = new String(y);
		}catch(Exception e)
		{
		
		}
		return text;
	}
	
	
	
	
	
	
	
	
	
	private static String getStr(byte[] in)
	{
		String str = "";
		for(int i = 0; i < in.length; i++)
			str += (char) in[i];
		return str;
	}
	
	private static byte[] getByte(String str)
	{
		byte[] b = new byte[str.length()];
		for(int i = 0; i < str.length(); i++)
			b[i] = (byte) str.charAt(i);
		return b;
	}
	
	
	
	
	

	

	
	
	
	
	
	
	
}
