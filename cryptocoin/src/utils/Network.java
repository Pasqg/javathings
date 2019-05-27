package utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Network {
	public static String repositoryIp = "192.168.1.43";
	public static int repositoryPort = 6666;

	public static byte[] changeEndian(int x) {
		byte array[] = new byte[4];

		array[0] = (byte) (x & 0x000000FF);
		array[1] = (byte) (x & 0x0000FF00);
		array[2] = (byte) (x & 0x00FF0000);
		array[3] = (byte) (x & 0xFF000000);

		array[1] = array[0];
		array[2] = array[0];
		array[3] = array[0];
		
		System.out.println(x & 0x000000FF);
		System.out.println(x & 0x0000FF00);
		System.out.println(x & 0x00FF0000);
		System.out.println(x & 0xFF000000);
		
		return array;
	}
	
	public static int changeEndianInt(int x) {
		int x0 =  (x & 0x000000FF);
		int x1 =  (x & 0x0000FF00);
		int x2 =  (x & 0x00FF0000);
		int x3 = (x & 0xFF000000);
		
		int i = (x3 >> 24) + (x2 >> 16) + (x1 >> 8) + x0;
		
		return i;
	}
	
	public static String[] getPeersIps() throws UnknownHostException, IOException {
		Socket s = new Socket(repositoryIp, repositoryPort);
		DataInputStream in = new DataInputStream(s.getInputStream());
		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		
		out.write(1);
		int numberIps = changeEndianInt(in.readInt());
		
		String[] ips = new String[numberIps];
		for (int i = 0; i < numberIps; i++) {
			//length
			byte l = in.readByte();
			byte[] buffer = new byte[l];
			for (byte k = 0; k < l; k++) {
				buffer[k] = in.readByte();
			}
			ips[i] = new String(buffer);
		}
		return ips;
	}
	
	public static String getCurrentIp() throws UnknownHostException {
		InetAddress currentIp = InetAddress.getLocalHost();
		String ipString = currentIp.getHostAddress();
		return ipString;
	}
	
	public static boolean register() throws IOException {
		Socket s = new Socket(repositoryIp, repositoryPort);
		DataInputStream in = new DataInputStream(s.getInputStream());
		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		
		String ipString = getCurrentIp();
		
		out.write(0);
		out.write((byte)ipString.length());
		
		for (int i = 0; i < ipString.length(); i++) {
			out.write((byte)(ipString.getBytes()[i]));	
		}
		
		int ack = in.read();
		return ack != 0;
	}
	
	public static void main(String[] args) throws IOException {
		//every time that connects
		//get peer list from repository
		//ask peers how many blocks they have
		//ask peers to send blocks that you don't have
		//once the blockchain is downloaded, register as peer
		//start doin:
		//MINING
		//	mine block
		//	if a transaction arrives, validate and verify it, then add to current block
		//  if a new block arrive, stop mining, take out the transactions already validate and start mining again
		//CREATE TRANSACTION
		//	create transaction and send it to peers
		//	if i am mining, add to the current mined block
		//
		
		//register
		
		System.out.println("Registering...");
		if (!register()) {
			System.err.println("Unable to register to the repository");
			return;
		}
		else System.out.println("Registered to the repository");
		
		String[] ips = getPeersIps();
		System.out.println(ips.length);
		for (int i = 0; i < ips.length; i++) 
			System.out.println(ips[i]);
	}
}
