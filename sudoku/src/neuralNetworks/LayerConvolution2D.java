package neuralNetworks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class LayerConvolution2D /*extends Layer*/ {
	public int inputNum; //numero di mappe in input
	public int inputDim; //dimensione x (=y) delle mappe di input
	public float[][][] input;
	
	public int mapNum;
	public int mapDim;
	public float[][][] net;
	public float[][][] map;
	
	public int kernelDim; //dimensione x (=y) dei kernel;
	public float[][][][] kernel;
	public float[][][][] deltaKernel;
	public float[][][][] lastKernel;
	public float[][][][] lastKernelCache;
	public float[][][][] kernelCache;
	public float[] bias;
	public float[] deltaBias;
	public float[] lastBias;
	public float[] cache; //for adagrad
	public float[] lastCache; //for adadelta
	
	float[][][] delta;
	
	public LayerMaxPooling pool;
	public boolean nextFull=false;

	Random rand = new Random();
	
	public void init(float range) {
		Random rand = new Random();
		//1.0f/Math.sqrt(inputNum*mapNum*kernelDim*kernelDim);// 0.1f;//0.18f;
		for (int m = 0; m < mapNum; m++) {
			for (int n = 0; n < inputNum; n++) {
				for (int j = 0; j < kernelDim; j++) {
					for (int k = 0; k < kernelDim; k++) {
						kernel[m][n][j][k] = (float)(rand.nextGaussian()*range);//(2.0f*rand.nextFloat() - 1.0f)*range;
						lastKernel[m][n][j][k] = 0.0f;
						lastKernelCache[m][n][j][k] = 0.0f;
						kernelCache[m][n][j][k] = 0.0f;
					}
				}
			}
			bias[m] = 0.0f;//(2.0f*rand.nextFloat() - 1.0f)*range;
			lastBias[m] = 0.0f;
			lastCache[m] = 0.0f;
			cache[m] = 0.0f;
		}
	}
	
	public void resetCache() {
		for (int m = 0; m < mapNum; m++) {
			for (int n = 0; n < inputNum; n++) {
				for (int j = 0; j < kernelDim; j++) {
					for (int k = 0; k < kernelDim; k++) {
						lastKernel[m][n][j][k] = 0.0f;
						lastKernelCache[m][n][j][k] = 0.0f;
						kernelCache[m][n][j][k] = 0.0f;
					}
				}
			}
			lastBias[m] = 0.0f;
			lastCache[m] = 0.0f;
			cache[m] = 0.0f;
		}
	}
	
	LayerConvolution2D(int _inputNum, int _inputDim, int _mapNum, int _kernelDim) {
		inputNum = _inputNum;
		inputDim = _inputDim;
		mapNum = _mapNum;
		kernelDim = _kernelDim;
		mapDim = inputDim-kernelDim+1;
		
		input = new float[inputNum][inputDim][inputDim];
		net = new float[mapNum][mapDim][mapDim];
		map = new float[mapNum][mapDim][mapDim];
		delta = new float[mapNum][mapDim][mapDim];
		kernel = new float[mapNum][inputNum][kernelDim][kernelDim];
		deltaKernel = new float[mapNum][inputNum][kernelDim][kernelDim];
		bias = new float[mapNum];
		deltaBias = new float[mapNum];
		lastKernel = new float[mapNum][inputNum][kernelDim][kernelDim];
		lastBias = new float[mapNum];
		cache = new float[mapNum];
		lastCache = new float[mapNum];
		kernelCache = new float[mapNum][inputNum][kernelDim][kernelDim];
		lastKernelCache = new float[mapNum][inputNum][kernelDim][kernelDim];
		pool = null;
		
		//Math.sqrt
		init(1.0f/(kernelDim*kernelDim*inputNum));
		
		//System.out.println(_inputNum+" "+inputNum+"\n"+_inputDim+" "+inputDim+"\n"+_mapNum+" "+mapNum+"\n"+mapDim+"\n"+_kernelDim+" "+kernelDim+"\n");
	}
	
	LayerConvolution2D(int _inputNum, int _inputDim, int _mapNum, int _kernelDim, int _poolSize) {
		inputNum = _inputNum;
		inputDim = _inputDim;
		mapNum = _mapNum;
		kernelDim = _kernelDim;
		mapDim = inputDim-kernelDim+1;
		
		input = new float[inputNum][inputDim][inputDim];
		net = new float[mapNum][mapDim][mapDim];
		map = new float[mapNum][mapDim][mapDim];
		delta = new float[mapNum][mapDim][mapDim];
		kernel = new float[mapNum][inputNum][kernelDim][kernelDim];
		deltaKernel = new float[mapNum][inputNum][kernelDim][kernelDim];
		bias = new float[mapNum];
		deltaBias = new float[mapNum];
		lastKernel = new float[mapNum][inputNum][kernelDim][kernelDim];
		lastBias = new float[mapNum];
		cache = new float[mapNum];
		lastCache = new float[mapNum];
		kernelCache = new float[mapNum][inputNum][kernelDim][kernelDim];
		lastKernelCache = new float[mapNum][inputNum][kernelDim][kernelDim];
		
		pool = new LayerMaxPooling(mapNum, mapDim, _poolSize);

		//Math.sqrt
		init(1.0f/(kernelDim*kernelDim*inputNum));
		//System.out.println(_inputNum+" "+inputNum+"\n"+_inputDim+" "+inputDim+"\n"+_mapNum+" "+mapNum+"\n"+mapDim+"\n"+_kernelDim+" "+kernelDim+"\n"+_poolSize+" "+pool.poolSize+"\n"+pool.poolDim);
	}
	
	public String savestr() {
		String str = "{";
		for (int m = 0; m < mapNum; m++) {
			str += "{";
			for (int i = 0; i < inputNum; i++) {
				str += "{";
				for (int j = 0; j < kernelDim; j++) {
					str += "{";
					for (int k = 0; k < kernelDim-1; k++) {
						str += kernel[m][i][j][k]+",";
					}
					str += kernel[m][i][j][kernelDim-1]+"}";
					if (j < kernelDim-1) str += ",";
				}
				str += "}";
				if (i < inputNum-1) str += ",";
			}
			str +="}";
			if (m < mapNum-1) str += ",";
		}
		str+="};\n{";
		for (int m = 0; m < mapNum-1; m++)
			str += bias[m]+",";
		str += bias[mapNum-1]+"};";
		return str;
	}
	
	public void save(String _saveFile) {
		byte[] data = new byte[25 + mapNum*8 + inputNum*mapNum*kernelDim*kernelDim*8];
		data[0] = 67;
		data[1] = 79;
		data[2] = 78;
		data[3] = 86;
		data[4] = 50;
		
		ByteBuffer.wrap(data, 5, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(inputNum);
		ByteBuffer.wrap(data, 9, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(inputDim);
		ByteBuffer.wrap(data, 13, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(mapNum);
		ByteBuffer.wrap(data, 17, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(mapDim);
		ByteBuffer.wrap(data, 21, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(kernelDim);
			
		for (int j = 0; j < mapNum; j++){
			ByteBuffer.wrap(data, 25+j*4, 4).order(ByteOrder.LITTLE_ENDIAN).putFloat(bias[j]);
		}
		
		for (int m = 0; m < mapNum; m++) {
			for (int i = 0; i < inputNum; i++) {
				for (int j = 0; j < kernelDim; j++) {
					for (int k = 0; k < kernelDim; k++) {
						ByteBuffer.wrap(data, 25+mapNum*4+m*inputNum*kernelDim*kernelDim*4 +i*kernelDim*kernelDim*4+j*kernelDim*4+ k*4, 4).order(ByteOrder.LITTLE_ENDIAN).putFloat(kernel[m][i][j][k]);	
					}
				}
			}
		}
		Base.write(_saveFile+".lconv2", data);
	}
	
	public void gradientWMaxMin() {
		float max = kernel[0][0][0][0];
		float min = max;
		for (int m = 0; m < mapNum; m++) {
			for (int n = 0; n < inputNum; n++) {
				for (int i = 0; i < kernelDim; i++) {
					for (int i1 = 0; i1 < kernelDim; i1++) {
						max = Math.max(deltaKernel[m][n][i][i1], max);
						min = Math.min(deltaKernel[m][n][i][i1], min);
					}
				}
			}
		}
		System.out.print("gradW Max: "+max+" Min: "+min+"  ");
	}	
	
	public void outputMaxMin() {
		float max = map[0][0][0];
		float min = max;
		for (int m = 0; m < mapNum; m++) {
			for (int i = 0; i < mapDim; i++) {
				for (int i1 = 0; i1 < mapDim; i1++) {
					max = Math.max(map[m][i][i1], max);
					min = Math.min(map[m][i][i1], min);
				}
			}
		}
		System.out.print("out Max: "+max+" Min: "+min+"  ");
	}

	public void load(String _loadFile) {
		ByteBuffer bb = ByteBuffer.wrap(Base.read(_loadFile+".lconv2"));
		bb.order(ByteOrder.LITTLE_ENDIAN);
		byte[] b = new byte[5];
		bb.get(b,0,5);
		
		if (b[0] != 67 || b[1] != 79 || b[2] != 78 || b[3] != 86 || b[4] != 50) {
			System.out.println("LayerConvolution2D: wrong Magic Word!");
			//System.exit(0);
		}

		int temp = bb.getInt();
		
		if (temp != inputNum) {
			System.out.println("LayerConvolution2D: wrong inputNum! " + temp + " ("+inputNum+")" );
			//System.exit(0);
		}
		
		temp = bb.getInt();
		
		if (temp != inputDim) {
			System.out.println("LayerConvolution2D: wrong inputDim! " + temp + " ("+inputDim+")" );
			//System.exit(0);
		}
		
		temp = bb.getInt();
		
		if (temp != mapNum) {
			System.out.println("LayerConvolution2D: wrong mapNum! " + temp + " ("+mapNum+")" );
			//System.exit(0);
		}
		
		temp = bb.getInt();
		
		if (temp != mapDim) {
			System.out.println("LayerConvolution2D: wrong mapDim! " + temp + " ("+mapDim+")" );
			//System.exit(0);
		}
		
		temp = bb.getInt();
		
		if (temp != kernelDim) {
			System.out.println("LayerConvolution2D: wrong kernelDim! " + temp + " ("+kernelDim+")" );
			//System.exit(0);
		}
		
		for (int j = 0; j < mapNum; j++){
			bias[j] = bb.getFloat();
		}
		
		for (int m = 0; m < mapNum; m++) {
			for (int i = 0; i < inputNum; i++) {
				for (int j = 0; j < kernelDim; j++) {
					for (int k = 0; k < kernelDim; k++) {
						kernel[m][i][j][k] = bb.getFloat();
					}
				}
			}
		}
	}
	
	public static float[][] msum(float[][] m1, float[][] m2) {
		for (int i = 0; i < m1.length; i++)
			for (int k = 0; k < m1[0].length; k++)
				m1[i][k] += m2[i][k];
		return m1;
	}
	
	public void feed(float[][][] _input) {
		for (int i = 0; i < inputNum; i++)
			for (int k = 0; k < inputDim; k++)
				for (int j = 0; j < inputDim; j++)
					input[i][k][j] = _input[i][k][j];
		for (int m = 0; m < mapNum; m++) {
			for (int i = 0; i < mapDim; i++) {
				for (int j = 0; j < mapDim; j++) {
					net[m][i][j] = bias[m];
					for (int n = 0; n < inputNum; n++) {
						for (int k = 0; k < kernelDim; k++) {
							for (int h = 0; h < kernelDim; h++) {
								net[m][i][j] += kernel[m][n][k][h] * input[n][i+k][j+h];
							}
						}
					}
					/*if (net[m][i][j] > 5.0f)
						map[m][i][j] = 5.0f;
					else*/ 
					map[m][i][j] = 0.333f*net[m][i][j];
					if (net[m][i][j] > 0.0f)
						map[m][i][j] = net[m][i][j];
					//else
					//	map[m][i][j] = 0.1818f*net[m][i][j];
				}
			}
		}
		
		//POOLING
		if (pool != null) {
			pool.feed(map);
		}
	}
	
	public static float[][] convolve(float[][] filter, float[][] map) {
		int fx = filter.length;
		int fy = filter[0].length;
		int mx = map.length;
		int my = map[0].length;
		float[][] result = new float[-fx+1+mx][-fy+1+my];
		for (int i = 0; i < -fx+1+mx; i++) {
			for (int k = 0; k < -fy+1+my; k++) {
				result[i][k] = 0.0f;
				for (int h = 0; h < fx; h++)
					for (int s = 0; s < fy; s++)
						result[i][k] += filter[h][s] * map[i+h][k+s];
			}
		}
		
		return result;
	}
	
	public float[][][] update(float[][][] error, float _learningRate, float _momentum) {
		if (pool != null) {
			for (int m = 0; m < pool.poolNum; m++) {
				for (int j = 0; j < pool.poolDim; j++) {
					for (int k = 0; k < pool.poolDim; k++) {
						for (int h = 0; h < pool.poolSize; h++) {
							for (int s = 0; s < pool.poolSize; s++) {
								delta[m][j*pool.poolSize+h][k*pool.poolSize+s] =0.333f*error[m][j][k] * pool.mask[m][j*pool.poolSize+h][k*pool.poolSize+s];
								if (net[m][j*pool.poolSize+h][k*pool.poolSize+s] > 0.0f)
									delta[m][j*pool.poolSize+h][k*pool.poolSize+s] = error[m][j][k] * pool.mask[m][j*pool.poolSize+h][k*pool.poolSize+s];
								//else
								//	delta[m][j*pool.poolSize+h][k*pool.poolSize+s] = 0.1818f*error[m][j][k] * pool.mask[m][j*pool.poolSize+h][k*pool.poolSize+s];
							}
						}
					}
				}
			}				 
		}
		else {
			for (int m = 0; m < mapNum; m++) {
				for (int j = 0; j < mapDim; j++) {
					for (int k = 0; k < mapDim; k++) {
						delta[m][j][k] = 0.333f*error[m][j][k];
						if (net[m][j][k] > 0.0f)
							delta[m][j][k] = error[m][j][k];
					}
				}
			}	
		}
		float sum = 0.0f;
		float eps = 1e-8f;
		float dec1 = 0.0f;//1e-5;
		float dec2 = 0.0001f;//0.0001f;//1e-5;
		float ro = 0.95f;
		float th = 0.07f; //0.1 BUONO per mnist
		for (int m = 0; m < mapNum; m++) {
			for (int n = 0; n < inputNum; n++) {
				//float[][] ww
				deltaKernel[m][n] = CNN.convolve(delta[m],input[n]);
				//System.out.println(ww.length);
				/*for (int j = 0; j < kernelDim; j++) {
					for (int k = 0; k < kernelDim; k++) {
						sum = 0.0f;
						for (int h = j; h < mapDim+j; h++) {
							for (int s = k; s < mapDim+k; s++) {
								sum += input[n][h][s] * delta[m][h-j][s-k];
							}
						}
						//if ( Math.abs(sum-ww[j][k]) > 0.0001) System.out.println("convolve wrong "+sum+" "+ww[j][k]);
						deltaKernel[m][n][j][k] = ww[j][k];
					}
				}*/
			}
			sum = 0.0f;
			for (int i = 0; i < mapDim; i++)
				for (int k = 0; k < mapDim; k++)
					sum += delta[m][i][k];
			deltaBias[m] = sum;
		}
		//System.out.println(Math.sqrt(lastKernelCache[0][0][0][0]+eps)/Math.sqrt(kernelCache[0][0][0][0]+eps));
		return delta;
	}
	
	public void updateTrue(float _learningRate, float _momentum) {
		float eps = 1e-8f;
		float ro = 0.95f;
		float dec2 = 0.000f;
		for (int m = 0; m < mapNum; m++) {
			for (int n = 0; n < inputNum; n++) {
				for (int j = 0; j < kernelDim; j++) {
					for (int k = 0; k < kernelDim; k++) {
						/*float sum = deltaKernel[m][n][j][k];
						kernelCache[m][n][j][k] = ro*kernelCache[m][n][j][k] + (1-ro)*sum*sum; //adadelta
						lastKernel[m][n][j][k] = Math.sqrt(lastKernelCache[m][n][j][k]+eps)*sum/Math.sqrt(kernelCache[m][n][j][k]+eps);//+ _momentum*lastKernel[m][n][j][k];
						lastKernelCache[m][n][j][k] = ro*lastKernelCache[m][n][j][k] + (1-ro)*lastKernel[m][n][j][k]*lastKernel[m][n][j][k];
						*/lastKernel[m][n][j][k] = _learningRate*(deltaKernel[m][n][j][k]-dec2*kernel[m][n][j][k]) + _momentum*lastKernel[m][n][j][k];
						kernel[m][n][j][k] += lastKernel[m][n][j][k];
					}
				}
			}
			/*float sum = deltaBias[m];
			cache[m] = ro*cache[m] + (1-ro)*sum*sum;
			lastBias[m] = Math.sqrt(lastCache[m]+eps)*sum/Math.sqrt(cache[m]+eps);// + _momentum*lastBias[m];
			lastCache[m] = ro*lastCache[m] + (1-ro)*lastBias[m]*lastBias[m];
			*/lastBias[m] = _learningRate*(deltaBias[m]-dec2*bias[m]) + _momentum*lastBias[m];	
			bias[m] += lastBias[m];
		}
	}
}
