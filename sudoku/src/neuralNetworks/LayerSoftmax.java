package neuralNetworks;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Random;

public class LayerSoftmax extends Layer {
	private float[] lastBias;
	public float[] lastBiasCache;
	public float[] biasCache;
	
	private float[][] lastWeight;
	public float[][] lastWeightCache;
	public float[][] weightCache;
	
	public boolean train = true;
	
	//variabili per l'ottimizzazione
	private float sum;
	float[] delta;
	Random rand = new Random();
	
	public LayerSoftmax(int _inputNum, int _hiddenNum) {
		inputNum = _inputNum;
		hiddenNum = _hiddenNum;
		
		bias = new float[hiddenNum];
		deltaBias = new float[hiddenNum];
		lastBias = new float[hiddenNum];
		lastBiasCache = new float[hiddenNum];
		biasCache = new float[hiddenNum];
		weight = new float[hiddenNum][inputNum];
		deltaWeight = new float[hiddenNum][inputNum];
		lastWeight = new float[hiddenNum][inputNum];
		lastWeightCache = new float[hiddenNum][inputNum];
		weightCache = new float[hiddenNum][inputNum];
		hidden = new float[hiddenNum];
		net = new float[hiddenNum];
		
		float range = (float)(1.0f/Math.sqrt(inputNum));
		Random rand = new Random();
		for (int j = 0; j < hiddenNum; j++) {
			//bias[j] = (2.0f * rand.nextFloat() - 1.0f)*range;
			bias[j] = 0.0f;//rand.nextGaussian()*range;
			lastBias[j] = 0.0f;
			lastBiasCache[j] = 0.0f;
			biasCache[j] = 0.0f;
			for (int i = 0; i < inputNum; i++) {
				//weight[j][i] = (2.0f * rand.nextFloat() - 1.0f)*range;
				weight[j][i] = (float) (rand.nextGaussian()*range);
				lastWeight[j][i] = 0.0f;
				lastWeightCache[j][i] = 0.0f;
				weightCache[j][i] = 0.0f;
			}
		}
		
		delta = new float[hiddenNum];
	}
	
	public LayerSoftmax(int _hidden[]) {
		inputNum = _hidden[0];
		hiddenNum = _hidden[1];
		
		bias = new float[hiddenNum];
		deltaBias = new float[hiddenNum];
		lastBias = new float[hiddenNum];
		lastBiasCache = new float[hiddenNum];
		biasCache = new float[hiddenNum];
		weight = new float[hiddenNum][inputNum];
		deltaWeight = new float[hiddenNum][inputNum];
		lastWeight = new float[hiddenNum][inputNum];
		lastWeightCache = new float[hiddenNum][inputNum];
		weightCache = new float[hiddenNum][inputNum];
		hidden = new float[hiddenNum];
		net = new float[hiddenNum];
		
		float range = 1.0f/(inputNum);
		Random rand = new Random();
		for (int j = 0; j < hiddenNum; j++) {
			//bias[j] = (2.0f * rand.nextFloat() - 1.0f)*range;
			bias[j] = 0.0f;//rand.nextGaussian()*range;
			lastBias[j] = 0.0f;
			lastBiasCache[j] = 0.0f;
			biasCache[j] = 0.0f;
			for (int i = 0; i < inputNum; i++) {
				//weight[j][i] = (2.0f * rand.nextFloat() - 1.0f)*range;
				weight[j][i] = (float) (rand.nextGaussian()*range);
				lastWeight[j][i] = 0.0f;
				lastWeightCache[j][i] = 0.0f;
				weightCache[j][i] = 0.0f;
			}
		}
		
		delta = new float[hiddenNum];
	}
	
	@Override
	public void save(String _saveFile) {
		byte[] data = new byte[13 + hiddenNum*8 + hiddenNum*inputNum*8];
		data[0] = 76;
		data[1] = 65;
		data[2] = 89;
		data[3] = 69;
		data[4] = 82;
		
		ByteBuffer.wrap(data, 5, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(inputNum);
		ByteBuffer.wrap(data, 9, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(hiddenNum);
		
	    for(int j = 0; j < hiddenNum; j++){
	        ByteBuffer.wrap(data, 13+j*4, 4).order(ByteOrder.LITTLE_ENDIAN).putFloat(bias[j]);
	    }
	    
	    for(int j = 0; j < hiddenNum; j++){
	    	for (int i = 0; i < inputNum; i++) {
	    		ByteBuffer.wrap(data, 13+hiddenNum*4 + j*inputNum*4 + i*4, 4).order(ByteOrder.LITTLE_ENDIAN).putFloat(weight[j][i]);
	    	}
	    }
				
		Base.write(_saveFile+".lsoft", data);
	}
	
	public void savestr() {
		String str="";
		for (int j = 0; j < hiddenNum; j++) {
			str += "float[] weight"+j+" = {";
			for (int i = 0; i < inputNum-1; i++) {
				str += String.format(Locale.US,"%.8f",weight[j][i])+",";
			}
			str += String.format(Locale.US,"%.8f",weight[j][inputNum-1])+"};\n";
			System.out.println(str);
			if (j % 3 == 0) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			str = "";
		}
		str= "{";
		for (int j = 0; j < hiddenNum-1; j++)
			str += String.format(Locale.US,"%.8f",bias[j])+",";
		str+=String.format(Locale.US,"%.8f",bias[hiddenNum-1])+"};";
		System.out.println(str);
	}
	
	public void savestr2() {
		String str="";
		for (int j = 0; j < hiddenNum; j++) {
			str += "float[] weight"+j+" = {";
			for (int i = 0; i < inputNum-1; i++) {
				str += weight[j][i]+",";
			}
			str += weight[j][inputNum-1]+"};\n";
			System.out.println(str);
			if (j % 3 == 0) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			str = "";
		}
		str= "{";
		for (int j = 0; j < hiddenNum-1; j++)
			str +=bias[j]+",";
		str+=bias[hiddenNum-1]+"};";
		System.out.println(str);
	}
	
	@Override
	public void load(String _loadFile) {
		ByteBuffer bb = ByteBuffer.wrap(Base.read(_loadFile+".lsoft"));
		bb.order(ByteOrder.LITTLE_ENDIAN);
		byte[] b = new byte[5];
		bb.get(b,0,5);
		
		if (b[0] != 76 || b[1] != 65 || b[2] != 89 || b[3] != 69 || b[4] != 82) {
			System.out.println("LayerFastSigmoid: wrong Magic Word!");
			System.exit(0);
		}
		
		int temp = bb.getInt();
		
		if (temp != inputNum) {
			System.out.println("LayerFastSigmoid: wrong inputNum!" + temp);
			System.exit(0);
		}
		
		temp = bb.getInt();
		
		if (temp != hiddenNum) {
			System.out.println("LayerFastSigmoid: wrong hiddenNum!");
			System.exit(0);
		}

		for (int j = 0; j < hiddenNum; j++)
			bias[j] = bb.getFloat();
		
		for (int j = 0; j < hiddenNum; j++) {
			for (int i = 0; i < inputNum; i++) {
				weight[j][i] = bb.getFloat();
			}
		}
	}

	@Override
	public void feed(float[] _input) {
		float t=-1.0f;
		/*
		if (!train) {
			for (int j = 0; j < hiddenNum; j++)
				for (int i = 0; i < inputNum; i++)
					weight[j][i] *= dropout;			
		}*/
		
		for (int j = 0; j < hiddenNum; j++) {
			hidden[j] = bias[j];
			for (int i = 0; i < inputNum; i++)
				hidden[j] += _input[i]*weight[j][i];
			
			//questa ricerca del massimo potrebbe essere tolta... (in c++ non creava problemi)
			if (hidden[j] > t)
				t = hidden[j];
			
			net[j] = hidden[j];
		}

		sum = 0;
		for (int j = 0; j < hiddenNum; j++) {
			hidden[j] = (float) Math.exp(hidden[j]-t);
			sum += hidden[j];
		}
		for (int j = 0; j < hiddenNum; j++) {
			hidden[j] /= sum;
		}
		/*
		if (!train) {
			for (int j = 0; j < hiddenNum; j++)
				for (int i = 0; i < inputNum; i++)
					weight[j][i] /= dropout;			
		}*/
	}

	@Override
	public float[] update(float[] _error, float[] _input, float _learningRate, float _momentum) {
		for (int j = 0; j < hiddenNum; j++) {
			//delta[j] =  _error[j]* hidden[j] * (1 - hidden[j]);
			delta[j] = _error[j];
			/*if (Float.isNaN(delta[j])) {
				System.out.println("Nan in softmax update!");
				System.exit(0);
			}*/
			//if (Math.abs(delta[j]) > 0.5) delta[j] = 0.5;
			
			/*for (int k = 0; k < hiddenNum; k++) {
	    		if (k != j) {
					delta[j] -= _error[k] * hidden[k] * hidden[j];
	    		}
	    	}*/
		}
		for (int j = 0; j < hiddenNum; j++) {
			deltaBias[j] =delta[j]; //inutile
			for (int i = 0; i < inputNum; i++) {
				deltaWeight[j][i] = delta[j]*_input[i];
			}
		}
		return delta;
	}
	
	@Override
	public void updateTrue(float _learningRate, float _momentum) {
		float ro = 0.95f;
		float eps = 1e-8f;
		float dec2 = 0.001f;
		for (int j = 0; j < hiddenNum; j++) {
			/*sum = deltaBias[j];
			biasCache[j] = ro*biasCache[j] + (1-ro)*sum*sum; //adadelta
			lastBias[j] = Math.sqrt(lastBiasCache[j]+eps)*sum/Math.sqrt(biasCache[j]+eps);// + _momentum*convLayer[convLayerNum-1].lastKernel[m][n][j][k];
			lastBiasCache[j] = ro*lastBiasCache[j] + (1-ro)*lastBias[j]*lastBias[j];				
			*/lastBias[j] = 2.0f*_learningRate * (deltaBias[j]-dec2*bias[j]) + _momentum*lastBias[j];
			bias[j] += lastBias[j];
			for (int i = 0; i < inputNum; i++) {
				/*sum = deltaWeight[j][i];
				weightCache[j][i] = ro*weightCache[j][i] + (1-ro)*sum*sum; //adadelta
				lastWeight[j][i] = Math.sqrt(lastWeightCache[j][i]+eps)*sum/Math.sqrt(weightCache[j][i]+eps);// + _momentum*convLayer[convLayerNum-1].lastKernel[m][n][j][k];
				lastWeightCache[j][i] = ro*lastWeightCache[j][i] + (1-ro)*lastWeight[j][i]*lastWeight[j][i];				
				*/lastWeight[j][i] = _learningRate * (deltaWeight[j][i]-dec2*weight[j][i]) + _momentum*lastWeight[j][i];  //sgd
				weight[j][i] += lastWeight[j][i];
			}
		}
	}

	@Override
	BufferedImage visualize(int dimX, int dimY, float zoom) {
		BufferedImage base = new BufferedImage(dimX, dimY, BufferedImage.TYPE_4BYTE_ABGR);

	    byte[] pixel = ((DataBufferByte) base.getRaster().getDataBuffer()).getData(); //crea una copia
	    for (int i = 0; i < pixel.length; i+=4) {
	    	byte value = (byte)(255*hidden[(i/4)]);
	    	pixel[i] = (byte)255;
	    	pixel[i+1] = value;
	    	pixel[i+2] = value;
	    	pixel[i+3] = value;
	    }	
	
		BufferedImage result =  new BufferedImage((int)(dimX*zoom), (int)(dimY*zoom), BufferedImage.TYPE_3BYTE_BGR);
	    Graphics2D bGr = result.createGraphics();
	    bGr.drawImage(base, 0, 0,(int)(dimX*zoom), (int)(dimY*zoom), null);
	    bGr.dispose();
	    
		return result;
	}
	
	@Override
	public void showFilters(String _file) {
		int hiddenX = 25;
		int hiddenY = 20;
		int visibleX = 28;
		int visibleY = 28;
		int width = hiddenX*(visibleX+1);
		int height = hiddenY*(visibleY+1);
		
		float max = weight[0][0];
		float min = weight[0][0];
		for (int j = 0; j < hiddenNum; j++) {
			for (int i = 0; i < inputNum; i++) {
				if (weight[j][i] > max)
					max = weight[j][i];
				else if (weight[j][i] < min)
					min = weight[j][i];
			}
		}
		
		FileWriter file;
		try {
			file = new FileWriter(_file);
			PrintWriter print = new PrintWriter(file);

			print.printf("P2 " + width + " " + height + " 255\n");
			for (int j = 0; j < hiddenY; j++) {
				for (int i = 0; i < visibleY; i++) {
					for (int n = 0 + hiddenX*j; n < hiddenX + hiddenX*j; n++) {
						for (int m = i*visibleX; m < visibleX+i*visibleX; m++) {
							print.printf("%d ",(int)(255*(weight[n][m] - min)/(max-min)));
						}
						print.printf("0 ");
					}
				}
				for (int i = 0; i < width; i++)
					print.printf("0 ");
			}
			
			print.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		LayerSoftmax layer = new LayerSoftmax(256,3072);
		
		float[] input = new float[256];
		Random rand = new Random();
		for (int i = 0; i < 256; i++)
			input[i] = rand.nextFloat();
		
		long avg=0;
		for (int i = 0; i < 1000; i++) {
			for (int i1 = 0; i1 < 256; i1++)
				input[i1] = rand.nextFloat();
			long start = System.nanoTime();
			layer.feed(input);
			avg += System.nanoTime() - start;
		}
		System.out.println(avg/1000000.0);
	}
}
