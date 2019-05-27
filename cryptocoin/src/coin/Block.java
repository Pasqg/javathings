package coin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import utils.Crypto;

public class Block {
	long id;
	long timestamp;
	ArrayList<Transaction> transactions;
	String previousBlockHash=null;
	long nonce;
	BigInteger target;
	
	public Block(long id, BigInteger t) {
		transactions = new ArrayList<Transaction>();
		nonce = 0;
		target = t;
		this.id = id;
	}
	
	public Block(String fileName) {
		//load from file
		transactions = new ArrayList<Transaction>();
		try {
	      BufferedReader in = new BufferedReader(new FileReader(fileName));
	      String str;
	      while ( (str = in.readLine()) != null) {
	    	  String[] tokens = str.split(" ");
	    	  if (tokens.length == 2) {
	    		  if (tokens[0].equals("id:")) id = Long.parseLong(tokens[1]);
	    		  else if (tokens[0].equals("timestamp:")) timestamp = Long.parseLong(tokens[1]);
	    		  else if (tokens[0].equals("previousHash:")) {
	    			  if (tokens[1].equals("null")) previousBlockHash = null;
	    			  else previousBlockHash = tokens[1];
	    		  }
	    		  else if (tokens[0].equals("nonce:")) nonce = Long.parseLong(tokens[1]);
	    		  else if (tokens[0].equals("target:")) target = new BigInteger(tokens[1]);
	    		  else if (tokens[0].equals("hash:")) {}
	    		  else {
    	    		  System.err.println("[Blockchain] Invalid token in file "+fileName+": "+tokens[0]);
    	    		  System.exit(0);
	    		  }
	    	  }
	    	  else if (tokens.length == 1) {
	    		  if (tokens[0].equals("transaction:{")) {
	    			  Transaction t = new Transaction();
	    			  String txStr;
	    			  while (!(txStr = in.readLine()).equals("}")) {
	    		    	  String[] tokensTx = txStr.split(" ");
	    		    	  if (tokensTx.length == 2) {
		    				  if (tokensTx[0].equals("\ttimestamp:")) t.setTimestamp(Long.parseLong(tokensTx[1]));
		    				  else if (tokensTx[0].equals("\tsender:")) t.setSender(tokensTx[1]);
		    				  else if (tokensTx[0].equals("\treceiver:")) t.setReceiver(tokensTx[1]);
		    				  else if (tokensTx[0].equals("\tamount:")) t.setAmount(Double.parseDouble(tokensTx[1]));
		    				  else if (tokensTx[0].equals("\tsignature:")) {
		    	    			  if (tokensTx[1].equals("null")) t.setSignature(null);
		    	    			  else t.setSignature(tokensTx[1]);
		    	    		  }
		    	    		  else {
		        	    		  System.err.println("[Blockchain] Invalid token in file "+fileName+": "+tokensTx[0]);
		        	    		  System.exit(0);
		    	    		  }
	    		    	  }
	    		    	  else {
	    		    		  System.err.println("[Blockchain] Invalid number of tokens "+tokensTx.length+" in file "+fileName+": "+txStr);
	    		    		  System.exit(0);
	    		    	  }
	    			  }
	    			  addTransaction(t);
	    		  }
	    		  else {
    	    		  System.err.println("[Blockchain] Invalid token in file "+fileName+": "+tokens[0]);
    	    		  System.exit(0);
	    		  }
	    	  }
	    	  else {
	    		  System.err.println("[Blockchain] Invalid number of tokens "+tokens.length+" in file "+fileName+": "+str);
	    		  System.exit(0);
	    	  }
	      }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save(String filename) {
      try {
          BufferedWriter header = new BufferedWriter(new FileWriter(filename));
          header.write("id: "+id+"\n");
          header.write("timestamp: "+timestamp+"\n");
          header.write("previousHash: "+previousBlockHash+"\n");
          header.write("nonce: "+nonce+"\n");
          header.write("target: "+target.toString()+"\n");
          header.write("hash: "+hash()+"\n");
          
          
          for (Transaction t : transactions) {
        	  header.write("transaction:{\n");
        	  header.write("\ttimestamp: "+t.getTimestamp()+"\n");
        	  header.write("\tsender: "+t.getSender()+"\n");
        	  header.write("\treceiver: "+t.getReceiver()+"\n");
        	  header.write("\tamount: "+t.getAmount()+"\n");
        	  header.write("\tsignature: "+t.getSignature()+"\n");
        	  header.write("}\n");
          }
          
          header.close();
       }
       catch (IOException e) {
       }
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}

	public long getNonce() {
		return nonce;
	}

	public BigInteger getTarget() {
		return new BigInteger(target.toByteArray());
	}
	
	public void addTransaction(Transaction t) {
		transactions.add(t);
	}
	
	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}

	public String transactionHash() {
		String str = "";
		for (Transaction t : transactions) {
			str += t.hash()+"@";
		}
		return Crypto.sha256string(str.getBytes());
	}
	
	public String blockToHash() {
		String str = "";
		str += id+"@";
		str += timestamp+"@";
		str += previousBlockHash+"@";
		str += transactionHash()+"@";
		str += nonce+"@";
		str += target.toString()+"@";
		return str;
	}
	
	public byte[] byteHash() {
		return Crypto.sha256(blockToHash().getBytes());
	}
	
	public String hash() {
		return Crypto.sha256string(blockToHash().getBytes());
	}
	
	public void setTarget(BigInteger target) {
		this.target = (target);
	}
	
	public void setPreviousBlockHash(String hash) {
		previousBlockHash = hash;
	}
	

	public void mine() {
		while (!mine(10000));
	}
	
	public boolean mine(int n) {
		timestamp = System.currentTimeMillis();
		BigInteger bigHash = new BigInteger(byteHash());
		for (int i = 0; i < n; i++) {
			if (bigHash.abs().compareTo(target) > 0) { //big < target
				nonce++;
				bigHash = new BigInteger(byteHash());
			}
			else {
				return true;
			}
		}
		return false;
	}
	
	public boolean verify(Blockchain blockchain) throws Exception {
		if (this.timestamp > System.currentTimeMillis()) return false;
		if (blockchain.getBlocks().isEmpty() && this.id != 0) return false;
		if (!blockchain.getBlocks().isEmpty() && this.id != blockchain.getLastBlock().getId()+1) return false;

		int coinbaseNum = 0;
		for (Transaction t : transactions) {
			if (t.getSender().equals("coinbase")) {
				coinbaseNum++;
				if (coinbaseNum > 1)
					return false;
				if (t.getAmount() > Blockchain.reward(this.id)) return false;
			}
		}
		
		//if target is too high
		if (!blockchain.getBlocks().isEmpty() && target.abs().compareTo(blockchain.getTarget(this.id)) > 0)
			return false;

		BigInteger bigHash = new BigInteger(byteHash());
		if (bigHash.abs().compareTo(target) > 0) {
			return false;
		}
		if (!blockchain.getBlocks().isEmpty() && !blockchain.getLastBlock().hash().equals(previousBlockHash)) {
			return false;
		}
		for (Transaction t : transactions) {
			if (!t.verify(blockchain)) return false;
		}
		return true;
	}
}
