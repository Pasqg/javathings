package coin;

import java.security.NoSuchAlgorithmException;

import utils.Crypto;

public class Transaction {
	private double amount;
	private String sender;
	private String receiver;
	private long timestamp;
	
	private String signature;
	
	public Transaction() {
		sender = null;
		receiver = null;
		signature = null;
		amount = -1;
		timestamp = System.currentTimeMillis();
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	public double getAmount() {
		return amount;
	}

	public void setReceiver(String receiver) {
		this.receiver = new String(receiver);
	}
	
	public String getReceiver() {
		return new String(receiver);
	}
	
	public void setSender(String sender) {
		this.sender = new String(sender);
	}
	
	public String getSender() {
		return new String(sender);
	}
	
	public void setTimestamp(long t) {
		timestamp = t;
	}

	public boolean sign(String privateKey) throws Exception {
		String message = "";
		message += timestamp+"@";
		message += amount+"@";
		message += sender+"@";
		message += receiver;
		String messageHash = Crypto.sha256string(message.getBytes());
		
		//sign the transaction with the private key of the sender (owner of the coins)
		String signature = Crypto.signature(messageHash, privateKey);
		
		if (!Crypto.decode(signature, sender).equals(messageHash)) return false;
		this.signature = signature;
		return true;
	}
	
	public String getSignature() {
		if (signature != null) return new String(signature);
		else return null;
	}
	
	public void setSignature(String s) {
		if (s != null) this.signature = new String(signature);
		else this.signature = null;
	}
	
	public boolean verify(Blockchain blockchain) throws Exception {
		String message = "";
		message += timestamp+"@";
		message += amount+"@";
		message += sender+"@";
		message += receiver;
		String messageHash = Crypto.sha256string(message.getBytes());
		if (amount <= 0) return false;
		
		if (!sender.equals("coinbase")) {
			if (signature == null) return false;
			if (!Crypto.decode(signature, sender).equals(messageHash)) return false;
			if (amount > blockchain.getAddressBalance(sender)) return false;
		}
		return true;
	}
	
	public String hash() {
		String message = "";
		message += timestamp+"@";
		message += amount+"@";
		message += sender+"@";
		message += receiver+"@";
		if (signature != null) message += signature;
		return Crypto.sha256string(message.getBytes());
	}
}
