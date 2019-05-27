package coin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import utils.Base64;

public class Address {
	private String privateKey;
	private String publicKey;

	public Address(String pub, String priv) {
		privateKey = new String(priv);
		publicKey = new String(pub);
	}
	
	public Address(String filename) {
		
	}
	
	public void save(String filename) {
	     try {
	          BufferedWriter header = new BufferedWriter(new FileWriter(filename));
	          header.write("publicKey: "+publicKey+"\n");
	          header.write("privateKey: "+privateKey+"\n");
	          header.close();
	       }
	       catch (IOException e) {
	       }
	}
	
	public String getPrivateKey() {
		return new String(privateKey);
	}
	public String getPublicKey() {
		return new String(publicKey);
	}
	
	static public Address generate() {
		SecureRandom random = new SecureRandom();
		KeyPairGenerator generator = null;
		try {
			generator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		generator.initialize(2048, random);
		
		KeyPair pair = generator.generateKeyPair();
		Key pubKey = pair.getPublic();
		Key privKey = pair.getPrivate();
		
		return new Address(Base64.encode(pubKey.getEncoded()), Base64.encode(privKey.getEncoded()));
	}
}
