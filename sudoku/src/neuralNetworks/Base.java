package neuralNetworks;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class Base {
	private static Random rand = new Random();
	public static final String mnistTrain = "C:/Users/San Marzano/Documents/Roba/mnist/mnist.train";
	public static final String mnistTrainLabel = "C:/Users/San Marzano/Documents/Roba/mnist/mnist.train.label";
	public static final String mnistTest = "C:/Users/San Marzano/Documents/Roba/mnist/mnist.test";
	public static final String mnistTestLabel = "C:/Users/San Marzano/Documents/Roba/mnist/mnist.test.label";
	public static final String mnist20Train = "C:/Users/San Marzano/Documents/Roba/mnist/mnist20.train";
	public static final String mnist20Test = "C:/Users/San Marzano/Documents/Roba/mnist/mnist20.test";
	public static final String mnist32Train = "C:/Users/San Marzano/Documents/Roba/mnist/mnist32.train";
	public static final String mnist32Test = "C:/Users/San Marzano/Documents/Roba/mnist/mnist32.test";
	public static final String mnistExtTrain = "C:/Users/San Marzano/Documents/Roba/mnist/mnistExt.train";
	public static final String mnistExtTrainLabel = "C:/Users/San Marzano/Documents/Roba/mnist/mnistExt.train.label";
	public static final String mnistExtTest = "C:/Users/San Marzano/Documents/Roba/mnist/mnistExt.test";
	public static final String mnistExtTestLabel = "C:/Users/San Marzano/Documents/Roba/mnist/mnistExt.test.label";
	public static final String nFolder = "C:/Users/San Marzano/Documents/Roba/";
	public static final String machineDigitsTrain = nFolder+"MachinePrinted/digits1_9.train";
	public static final String machineDigitsTrainLabel = nFolder+"MachinePrinted/digits1_9.train.label";

	public static final String gtsrbTrain = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb.train";
	public static final String gtsrbTrainLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb.train.label";
	public static final String gtsrbTest = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb.test";
	public static final String gtsrbTestLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb.test.label";

	public static final String imgdTrain = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd.train";
	public static final String imgdTrainLabel = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd.train.label";
	public static final String imgdTest = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd.test";
	public static final String imgdTestLabel = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd.test.label";

	public static final String imgd9Train = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd9.train";
	public static final String imgd9TrainLabel = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd9.train.label";
	public static final String imgd9Test = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd9.test";
	public static final String imgd9TestLabel = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd9.test.label";
		
	public static final String imgd10Train = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd10.train";
	public static final String imgd10TrainLabel = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd10.train.label";
	public static final String imgd10Test = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd10.test";
	public static final String imgd10TestLabel = "C:\\Users\\San Marzano\\Documents\\Roba\\imgd\\imgd10.test.label";
		
	public static final String gtsrb32Train = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb32.train";
	public static final String gtsrb32TrainLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb32.train.label";
	public static final String gtsrb32Test = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb32.test";
	public static final String gtsrb32TestLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb32.test.label";
	
	public static final String roadTrain = "C:/Users/San Marzano/Documents/Roba/GTSRB/road.train";
	public static final String roadTrainLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/road.train.label";
	public static final String roadTest = "C:/Users/San Marzano/Documents/Roba/GTSRB/road.test";
	public static final String roadTestLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/road.test.label";
	
	public static final String gtsrb38Train = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb38.train";
	public static final String gtsrb38TrainLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb38.train.label";
	public static final String gtsrb38Test = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb38.test";
	public static final String gtsrb38TestLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb38.test.label";
	
	public static final String gtsrb64Train = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb64.train";
	public static final String gtsrb64TrainLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb64.train.label";
	public static final String gtsrb64Test = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb64.test";
	public static final String gtsrb64TestLabel = "C:/Users/San Marzano/Documents/Roba/GTSRB/gtsrb64.test.label";
	
	public static final String uspsTrain = "C:/Users/San Marzano/Documents/Roba/usps/usps.train";
	public static final String uspsTrainLabel = "C:/Users/San Marzano/Documents/Roba/usps/usps.train.label";
	public static final String uspsTest = "C:/Users/San Marzano/Documents/Roba/usps/usps.test";
	public static final String uspsTestLabel = "C:/Users/San Marzano/Documents/Roba/usps/usps.test.label";
	
	public static final String cifar10Train = "C:/Users/San Marzano/Documents/Roba\\CIFAR10\\CIFAR10.train";
	public static final String cifar10Test = "C:/Users/San Marzano/Documents/Roba\\CIFAR10\\CIFAR10.test";
	public static final String cifar10TestLabel = "C:/Users/San Marzano/Documents/Roba\\CIFAR10\\CIFAR10.test.label";
	
	public static final String svhnTrain = "C:/Users/San Marzano/Documents/Roba\\SVHN\\svhn.train";
	public static final String svhnTrainLabel = "C:/Users/San Marzano/Documents/Roba\\SVHN\\svhn.train.label";
	public static final String svhnTest = "C:/Users/San Marzano/Documents/Roba\\SVHN\\svhn.test";
	public static final String svhnTestLabel = "C:/Users/San Marzano/Documents/Roba\\SVHN\\svhn.test.label";
	
	public static final String cifar100fineTrain = "C:/Users/San Marzano/Documents/Roba\\CIFAR10\\CIFAR100fine.train";
	public static final String cifar100fineTest = "C:/Users/San Marzano/Documents/Roba\\CIFAR10\\CIFAR100fine.test0";

	public static final String cifar100fineTrainLabel = cifar100fineTrain+".label";
	public static final String cifar100fineTestLabel = "C:/Users/San Marzano/Documents/Roba\\CIFAR10\\CIFAR100fine.test.label0";

	public static float[][] Array1Dto2D(float[] input, int x, int y) {
		float[][] output = new float[y][x];
		
		if (x*y == input.length) {
			for (int i = 0; i < y; i++) {
				for (int j = 0; j < x; j++) {
					output[i][j] = input[i*x+j];
				}
			}
		}
		else {
			try {
				throw new Exception("Non combaciano cazzo!!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return output;
	}
	
	public static float[] Array2Dto1D(float[][] input, int x, int y) {
		float[] output = new float[x*y];
		
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				output[i*x+j] = input[i][j];
			}
		}
		
		return output;
	}
	
	public static float[] Concat(float[][] input, int num, int length) {
		float[] result = new float[num*length];
		for (int i = 0; i < num; i++) {
			for (int k = 0; k < length; k++) {
				result[i*length+k] = input[i][k];
			}
		}
		return result;
	}
	
	public static float[][] randShift(float[][] input, int offsetX, int offsetY) {	
		int xdim = input.length;
		int ydim = input[0].length;
		int offx = rand.nextInt(2*offsetX)-offsetX;
		int offy = rand.nextInt(2*offsetY)-offsetY;
		float[][] input2 = new float[xdim][ydim];
		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				input2[x][y] = input[(x+offx)][(y+offy)];
			}
		}
		return input;
	}
	
	public static float[][][] randShift(float[][][] input, int offsetX, int offsetY) {	
		int mdim = input.length;
		int xdim = input[0].length;
		int ydim = input[0][0].length;
		int offx = rand.nextInt(2*(offsetX+1))-offsetX;
		int offy = rand.nextInt(2*(offsetY+1))-offsetY;
		float[][][] input2 = new float[mdim][xdim][ydim];
		
		int x1=0;
		int x2=xdim;
		int y1=0;
		int y2=ydim;
		
		if (offx < 0) {
			x2 = xdim+offx;
			//offx *= -1;
		}
		if (offy < 0) {
			y2 = ydim+offy;
			//offy *= -1;
		}
		if (offx >= 0) {
			x1=offx;
		}
		if (offy >= 0) {
			y1=offy;
		}
		
		for (int m = 0; m < mdim; m++) {
			for (int x = x1; x < x2; x++) {
				for (int y = y1; y < y2; y++) {
					input2[m][x][y] = input[m][(x-offx)][(y-offy)];
				}
			}
		}
		return input;
	}
	
	public static float[] HSVtoRGB(float H, float S, float V) {
		float[] rgb = new float[3];
		float R=0,G=0,B=0;

    	float c = V*S;
    	float x = c*(1-Math.abs((H/60)%2 - 1));
    	float m = V - c;
    	
    	if (Math.abs(H) > 360) {
    		H = H%360;
    	}
    	if (H < 0) {
    		H = 360+H;
    	}
    	H = (int)(H);
    	//System.out.println(H);
    	if (H >= 0 && H < 60) {
    		R = c+m;
    		G = x+m;
    		B = m;
    	}
    	else if (H >= 60 && H < 120) {
    		R = x+m;
    		G = c+m;
    		B = m;
    	}
    	else if (H >= 120 && H < 180) {
    		R = m;
    		G = c+m;
    		B = x+m;
    	}
    	else if (H >= 180 && H < 240) {
    		R = m;
    		G = x+m;
    		B = c+m;
    	}
    	else if (H >= 240 && H < 300) {
    		R = x+m;
    		G = m;
    		B = c+m;
    	}
    	else if (H >= 300 && H <= 360) {
    		R = c+m;
    		G = m;
    		B = x+m;
    	}
    	
    	rgb[0] = G;
    	rgb[1] = B;
    	rgb[2] = R;
		
		return rgb;
	}
	
	public static byte[] read(String _fileName) {
		File file = new File(_fileName);

		byte[] data = new byte[(int)file.length()];
		try {
			InputStream input = null;
			try {
				int count = 0;
				input = new BufferedInputStream(new FileInputStream(file));
				while (count < data.length) {
					int rimasti = data.length - count;
					int letti = input.read(data, count, rimasti);
					if (letti > 0)
						count += letti;
				}
			}
			finally {
				input.close();
			}
		}
		catch (FileNotFoundException e) {
			System.out.println(_fileName + " - File non trovato!");
			System.exit(0);
		}
		catch (IOException e) {
			System.out.println("IOException");
			System.exit(0);
		}
		return data;	
	}
	
	public static void write(String _fileName, byte[] data) {
		try {
			FileOutputStream file = new FileOutputStream(_fileName);
			file.write(data);
			file.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void write(String _fileName, byte[] data, boolean append) {
		try {
			FileOutputStream file = new FileOutputStream(_fileName,append);
			file.write(data);
			file.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static float[] absnorm(float[] _x) {
		float max = _x[0];
		
		for (int i = 1; i < _x.length; i++) {
			if (Math.abs(_x[i]) > max)
				max = Math.abs(_x[i]);
		}
			
		for (int i = 0; i < _x.length; i++) {
			_x[i] = _x[i]/max;
		}
		
		return _x;
	}
	
	public static void stampa(float[][] array, int x, int y) {
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				System.out.printf("%f ",array[i][j]);
			}
			System.out.printf("\n");
		}
	}
	
	public static void stampa(float[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.printf("%f ",array[i]);
		}
	}
	
	public static float[][] zeroPad(float[][] map, int pad) {
		int mapDim = map.length;
		float[][] result = new float[mapDim+2*pad][mapDim+2*pad];
		for (int i = 0; i < mapDim; i++)
			for (int k = 0; k < mapDim; k++)
				result[pad+i][pad+k] = map[i][k];
		
		return result;
	}
	
	public static float[][][] zeroPad(float[][][] map, int pad, int mapNum, int mapDim) {
		float[][][] result = new float[mapNum][mapDim+2*pad][mapDim+2*pad];
		for (int m = 0; m < mapNum; m++)
			for (int i = 0; i < mapDim; i++)
				for (int k = 0; k < mapDim; k++)
					result[m][pad+i][pad+k] = map[m][i][k];
		
		return result;
	}
	
	public static void validConvolution(float[][] x, float[][] y) {
		
	}
	
	public static void fullConvolution(float[][] x, float[][] y) {
		
	}
	
	public static void main(String[] args) {
	    Scanner scan;
	    File file = new File("C:\\Users\\San Marzano\\Desktop\\cifar80n");
	    try {
	    	//int num = ((16*25*3 + 16)+(20*25*16 + 20)+(20*25*20 + 20)+(320*10 + 10));
	    	int num = 150;
	    	byte[] bfile = new byte[num*8];
	    	ByteBuffer bb = ByteBuffer.wrap(bfile);
	    	bb.order(ByteOrder.LITTLE_ENDIAN);
	    	
	        scan = new Scanner(file).useLocale(Locale.US);

        	float[] temp = new float[num];
	        for (int i = 0; i < 2; i++) {
	        	for (int k = 0; k < 3; k++) {
	        		for (int j = 0; j < 25; j++) {
	        			temp[75*i+j*3+k] = scan.nextFloat();
	        		}
	        	}
	        }
	        
	        for (int i = 0; i < num; i++) {
	        	bb.putFloat(temp[i]);
	        }
	        
	        /*
	        for (int i = 0; i < num; i++) {
	        	float temp = scan.nextFloat();
	        	bb.putFloat(temp);
	        	//System.out.println(temp);
	        }*/
	        Base.write("C:\\Users\\San Marzano\\Desktop\\cifar80.lconv2", bfile, true);
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }

	}
}
