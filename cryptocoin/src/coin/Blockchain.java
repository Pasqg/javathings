package coin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Blockchain {
	public static double blockTime = 60000; //milliseconds
	public static double baseReward = 1000;
	public static int numBlockDifficultyAverage = 1440;
	public static int numBlockRewardHalving = 500000;
	public static double maxCoins = baseReward*numBlockRewardHalving*2;

	private ArrayList<Block> blocks;
	
	public static double reward(long id) {
		return baseReward / Math.pow(2, (int)(id/numBlockRewardHalving));
	}
	
	public double currentReward() {
		long id = 0;
		if (!blocks.isEmpty()) id = getLastBlock().getId();
		return reward(id);
	}
	
	public double getAverageBlockTime() {
		if (blocks.size() <= 1) return blockTime;
		return (blockTime+getLastBlock().getTimestamp()-blocks.get(0).getTimestamp())/blocks.size();
	}
	
	public Blockchain() {
		blocks = new ArrayList<Block>();
	}
	
	public Blockchain(String fileName) {
		//load from file
		blocks = new ArrayList<Block>();
		  try {
		      BufferedReader in = new BufferedReader(new FileReader(fileName));
		      String str = in.readLine();
		      if (str != null) {
		    	  String[] tokens = str.split(" ");
		    	  if (tokens.length == 2) {
		    		  if (tokens[0].equals("number_blocks:")) {
		    			  long numberBlocks = Long.parseLong(tokens[1]);
		    			  for (int i = 0; i < numberBlocks; i++) {
		    				  Block b = new Block("block"+i);
		    				  if (!addBlock(b)) {
		    					  System.err.println("[Blockchain] Invalid block"+i+" "+b.blockToHash());
					    		  System.exit(0);
		    				  }
		    			  }
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
		      else {
		    	  System.err.println("[Blockchain] Unable to read file "+fileName);
	    		  System.exit(0);
		      }
		      
		   } catch (IOException e) {
		    	  System.err.println("[Blockchain] Unable to open file "+fileName);
	    		  System.exit(0);
		   } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void shallowSave(String fileName) {
      try {
          BufferedWriter header = new BufferedWriter(new FileWriter(fileName));
          header.write("number_blocks: "+blocks.size()+"\n");
          header.close();
       }
       catch (IOException e) {
    	   System.err.println("Unable to save the blockchain");
       }
	}
	
	public void save(String fileName) {
		shallowSave(fileName);
		for (Block b : blocks) {
			b.save("block"+b.getId());
		}
	}
	
	public long getLastId() {
		if (blocks.isEmpty()) return -1;
		else return getLastBlock().getId();
	}
	
	public double getDifficulty() {
		if (blocks.size() <= 1) return 1;
		
		long lastId = getLastId();
		long windowSize = Math.min(numBlockDifficultyAverage,lastId);
		double timeMillis = blocks.get((int)lastId).getTimestamp() - blocks.get((int)(lastId-windowSize)).getTimestamp();
		
		double blockTime = timeMillis/windowSize;
		
		return this.blockTime/blockTime;
	}
	
	public BigInteger getTarget(long id) {
		 BigInteger target = new BigInteger("34000000000000000000000000000000000000000000000000000000000000000000000");
		 /*if (blocks.size() > 1) {
			 target = getLastBlock().getTarget();
		 }*/
		 
		 long c = 1000000;
		 target = target.divide(BigInteger.valueOf((long)(getDifficulty()*c)));
		 target = target.multiply(BigInteger.valueOf(c));
		 
		 return target;
	}
	
	public boolean addBlock(Block block) throws UnknownHostException, IOException, Exception {
		if (block.verify(this)) {
			blocks.add(block);
			
			//String[] ips = Network.getPeersIps();
			
			return true;
		}
		else return false;
	}
	
	public ArrayList<Block> getBlocks() {
		return blocks;
	}
	
	public Block getLastBlock() {
		return getBlocks().get(getBlocks().size()-1);
	}
	
	public double getAddressBalance(String address) {
		double amount = 0;
		for (Block b : blocks) {
			ArrayList<Transaction> transactions = b.getTransactions();
			for (Transaction t : transactions) {
				if (t.getSender().equals(address)) {
					amount -= t.getAmount();
				}
				else if (t.getReceiver().equals(address)) {
					amount += t.getAmount();
				}
			}
		}
		return amount;
	}
	
}
