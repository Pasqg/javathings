package utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import coin.Address;
import coin.Block;
import coin.Blockchain;
import coin.Transaction;
import javafx.scene.effect.InnerShadow;

public class Crypto {
	static void init() {
		File file = new File("coin.chain");
		if (!file.exists()) {
			try {
				file.createNewFile();
				try {
					FileOutputStream out = new FileOutputStream(file);
					//byte[] b = new byte[];
					//out.write(b);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public static byte[] sha256(byte[] input) {
        MessageDigest mDigest = null;
		try {
			mDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return mDigest.digest(input);
    }
	
	public static String sha256string(byte[] input) {
		byte[] result = sha256(input);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
		return sb.toString();
	}
	
	public static String hex(byte[] input) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < input.length; i++) {
            sb.append(Integer.toString((input[i] & 0xff) + 0x100, 16).substring(1));
        }
		return sb.toString();
	}
	
	public static String signature(String message, String privKey) throws Exception {
		byte[] privateKey = Base64.decode(privKey);
	
		KeyFactory kf = KeyFactory.getInstance("RSA");
		Key key = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKey));

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		
		byte[] cipherText = cipher.doFinal(message.getBytes());
		return Base64.encode(cipherText);
	}
	
	public static String decode(String signature, String pubKey) throws Exception {
		byte[] publicKey = Base64.decode(pubKey);
		
		KeyFactory kf = KeyFactory.getInstance("RSA");
		Key key = kf.generatePublic(new X509EncodedKeySpec(publicKey));

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		
		String cipherText = new String(cipher.doFinal(Base64.decode(signature)));
		return cipherText;
	}
	
	public static boolean verifySignature(String message, String pubKey, String signature) throws Exception {
		String cipherText = decode(signature, pubKey);
		return (message.equals(cipherText));
	}
		
	public static void hashcash() throws NoSuchAlgorithmException {
		byte[] data = new byte[32];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte)(Math.random()*255);
		int nonce = 0;
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(0,nonce);
		byte[] hash = sha256(data);
		System.out.println(hash.length);
		BigInteger target = new BigInteger("1000000000000000000000000000000000000000000000000000000000000000000000000");
		BigInteger big = new BigInteger(hash);
		long start = System.currentTimeMillis();
		while (big.abs().compareTo(target) > 0) { //big < target
			nonce++;
			bb.putInt(0,nonce);
			hash = sha256(data);
			big = new BigInteger(hash);
		}
		System.out.println((System.currentTimeMillis()-start)+"ms "+big.toString());
		System.out.println(nonce);
	}
	
	/*

    AES/CBC/NoPadding (128)
    AES/CBC/PKCS5Padding (128)
    AES/ECB/NoPadding (128)
    AES/ECB/PKCS5Padding (128)
    DES/CBC/NoPadding (56)
    DES/CBC/PKCS5Padding (56)
    DES/ECB/NoPadding (56)
    DES/ECB/PKCS5Padding (56)
    DESede/CBC/NoPadding (168)
    DESede/CBC/PKCS5Padding (168)
    DESede/ECB/NoPadding (168)
    DESede/ECB/PKCS5Padding (168)
    RSA/ECB/PKCS1Padding (1024, 2048)
    RSA/ECB/OAEPWithSHA-1AndMGF1Padding (1024, 2048)
    RSA/ECB/OAEPWithSHA-256AndMGF1Padding (1024, 2048)

il client che riceve le transazioni le sparge ai propri peer (tranne a chi l'ha inviata)

il miner che riceve le transazioni le sparge ai propri peer e
in ogni caso le aggiunge alla lista di transazioni da includere in un blocco.
All'arrivo di un nuovo blocco mentre un altro blocco è in corso di validazione, vengono controllate le transazioni del nuovo blocco


quando un nuovo blocco è stato hashcashato da un miner, 
esso può aggiungere al suo blocco una transazione destinata a se stesso 
(o anche ad un altro) che crea i coin (la transazione è inclusa nell'hash 
su cui poi si calcola l'hashcash) nei limiti dettati da 1/2?n ecc
i blocchi devono avere un minimo numero di transazioni per essere validi!

	 */
	
	public static void main(String[] args) throws Exception {
		Address addr1 = Address.generate();
		addr1.save("myAddress");
		
		Blockchain blockchain = new Blockchain("blockchain");	
		long averageTime = 0;
		boolean x = true;
		while(x) {
			long id = blockchain.getLastId()+1;
			BigInteger currentTarget = blockchain.getTarget(id);
			System.out.println("Difficulty: "+blockchain.getDifficulty());
			System.out.println("Target: "+currentTarget.toString());
			System.out.println("Average block time: "+blockchain.getAverageBlockTime()+"ms");
			System.out.println("Mining block "+id);
			
			Block genesis = new Block(id,currentTarget);
			
			Transaction coinbase = new Transaction();
			coinbase.setAmount(blockchain.currentReward());
			coinbase.setReceiver(addr1.getPublicKey());
			coinbase.setSender("coinbase");
			if (id > 0) {
				genesis.setPreviousBlockHash(blockchain.getLastBlock().hash());
			}
			
			genesis.addTransaction(coinbase);
			long start = System.currentTimeMillis();
			while (!genesis.mine(10000));
			long end = System.currentTimeMillis();
			averageTime += end-start;
			System.out.println("Block time: "+(end-start)+"ms");
			System.out.println("Block hash: "+genesis.hash());
			System.out.println("Block nonce: "+genesis.getNonce());
			System.out.println("verified: "+genesis.verify(blockchain));
			System.out.println("added: "+blockchain.addBlock(genesis));
			blockchain.save("blockchain");
		}
		
		System.out.println("Mining time: "+averageTime+"ms");
		
		
		
		Address addr2 = Address.generate();
		
		Transaction t = new Transaction();
		t.setAmount(1);
		t.setSender(addr1.getPublicKey());
		t.setReceiver(addr2.getPublicKey());

		try {
			if (!t.sign(addr1.getPrivateKey()))
				System.err.println("Unable to sign the transaction. Wrong key pair?");
			//if (!t.verify())
				//System.err.println("Unable to verify the transaction. Wrong key pair?");
		} catch (Exception e) {
			System.err.println("Unable to sign/verify the transaction. Wrong key pair?");
		}	
	}
}
