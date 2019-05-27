package neuralNetworks;

import java.util.Random;

public class CNN {
	public LayerConvolution2D convLayer[];
	public Layer layer[];
	public int layerNum;
	public int convLayerNum;
	public boolean discriminative=true;
	private boolean ok=false;
	public boolean b = false;
	
	public CNN() {
		
	}
	
	public CNN(CNN cnn) {
		ok = true;
		convLayer = new LayerConvolution2D[cnn.convLayerNum];
		for (int i = 0; i < cnn.convLayerNum; i++) {
			if (cnn.convLayer[i].pool == null)
				convLayer[i] = new LayerConvolution2D(cnn.convLayer[i].inputNum, cnn.convLayer[i].inputDim, cnn.convLayer[i].mapNum, cnn.convLayer[i].kernelDim);
			else
				convLayer[i] = new LayerConvolution2D(cnn.convLayer[i].inputNum, cnn.convLayer[i].inputDim, cnn.convLayer[i].mapNum, cnn.convLayer[i].kernelDim,cnn.convLayer[i].pool.poolSize);
		}
		layer = new Layer[cnn.layerNum];
		
		int lastDim = 0;
		if (convLayer[cnn.convLayerNum-1].pool != null) {
			lastDim = convLayer[cnn.convLayerNum-1].pool.poolNum*convLayer[cnn.convLayerNum-1].pool.poolDim*convLayer[cnn.convLayerNum-1].pool.poolDim;
		}
		else {
			lastDim = convLayer[cnn.convLayerNum-1].mapNum*convLayer[cnn.convLayerNum-1].mapDim*convLayer[cnn.convLayerNum-1].mapDim;
		}
		if (layer.length > 1) {
			/*layer[0] = new LayerRELU(lastDim, cnn.layer[0].hiddenNum);
			for (int i = 1; i < layer.length-1; i++) {
				layer[i] = new LayerRELU(cnn.layer[i-1].hiddenNum,cnn.layer[i].hiddenNum);
			}
			layer[layer.length-1] = new LayerSoftmax(cnn.layer[layer.length-2].hiddenNum,cnn.layer[layer.length-1].hiddenNum);
		*/}
		else
			layer[0] = new LayerSoftmax(lastDim, cnn.layer[0].hiddenNum);
		
		layerNum = layer.length;
		convLayerNum = convLayer.length;
		
		for (int l = 0; l < convLayerNum; l++) {
			convLayer[l].kernel = cnn.convLayer[l].kernel;
			convLayer[l].bias = cnn.convLayer[l].bias;
		}
		for (int l = 0; l < layerNum; l++) {
			layer[l].weight = cnn.layer[l].weight;
			layer[l].bias = cnn.layer[l].bias;
		}
	}
	
	public CNN(int hiddenConv[][], int hiddenFull[]) {
		ok = true;
		convLayer = new LayerConvolution2D[hiddenConv.length];
		for (int i = 0; i < hiddenConv.length; i++) {
			if (hiddenConv[i].length == 4)
				convLayer[i] = new LayerConvolution2D(hiddenConv[i][0], hiddenConv[i][1], hiddenConv[i][2], hiddenConv[i][3]);
			else if (hiddenConv[i].length == 5)
				convLayer[i] = new LayerConvolution2D(hiddenConv[i][0], hiddenConv[i][1], hiddenConv[i][2], hiddenConv[i][3], hiddenConv[i][4]);
		}
		layer = new Layer[hiddenFull.length];
		
		int lastDim = 0;
		if (convLayer[hiddenConv.length-1].pool != null) {
			lastDim = convLayer[hiddenConv.length-1].pool.poolNum*convLayer[hiddenConv.length-1].pool.poolDim*convLayer[hiddenConv.length-1].pool.poolDim;
		}
		else {
			lastDim = convLayer[hiddenConv.length-1].mapNum*convLayer[hiddenConv.length-1].mapDim*convLayer[hiddenConv.length-1].mapDim;
		}
		//System.out.println("inputnum al layer: "+convLayer[hiddenConv.length-1].mapDim);
		//lastDim += convLayer[0].inputNum*convLayer[0].inputDim*convLayer[0].inputDim;
		if (layer.length > 1) {
			/*layer[0] = new LayerRELU(lastDim, hiddenFull[0]);
			for (int i = 1; i < layer.length-1; i++) {
				layer[i] = new LayerRELU(hiddenFull[i-1],hiddenFull[i]);
			}
			layer[layer.length-1] = new LayerSoftmax(hiddenFull[layer.length-2],hiddenFull[layer.length-1]);
		*/}
		else
			layer[0] = new LayerSoftmax(lastDim, hiddenFull[0]);
		
		layerNum = layer.length;
		convLayerNum = convLayer.length;
	}
	
	public static float[][][] zeroPad(float[][][] map, int pad, int mapNum, int mapDim) {
		float[][][] result = new float[mapNum][mapDim+2*pad][mapDim+2*pad];
		for (int m = 0; m < mapNum; m++)
			for (int i = 0; i < mapDim+2*pad; i++)
				for (int k = 0; k < mapDim+2*pad; k++)
					result[m][i][k] = 0.0f;
		
		for (int m = 0; m < mapNum; m++)
			for (int i = 0; i < mapDim; i++)
				for (int k = 0; k < mapDim; k++)
					result[m][pad+i][pad+k] = map[m][i][k];
					
		return result;
	}
	
	public void update(float[] _error, float[] _pattern, float _learningRate, float _momentum) {
		float[] input = new float[layer[0].inputNum];
		
		if (convLayer[convLayerNum-1].pool == null) {
			for (int i = 0; i < convLayer[convLayerNum-1].mapNum; i++) {
				for (int k = 0; k < convLayer[convLayerNum-1].mapDim; k++) {
					for (int j = 0; j < convLayer[convLayerNum-1].mapDim; j++) {
						input[i*convLayer[convLayerNum-1].mapDim*convLayer[convLayerNum-1].mapDim +k*convLayer[convLayerNum-1].mapDim + j] = convLayer[convLayerNum-1].map[i][k][j];
					}
				}
			}
			/*
			for (int i = 0; i < convLayer[0].inputNum*convLayer[0].inputDim*convLayer[0].inputDim; i++) {
				input[i+convLayer[convLayerNum-1].mapNum*convLayer[convLayerNum-1].mapDim*convLayer[convLayerNum-1].mapDim] = _pattern[i];
			}*/
		}
		else {
			for (int i = 0; i < convLayer[convLayerNum-1].pool.poolNum; i++) {
				for (int k = 0; k < convLayer[convLayerNum-1].pool.poolDim; k++) {
					for (int j = 0; j < convLayer[convLayerNum-1].pool.poolDim; j++) {
						input[i*convLayer[convLayerNum-1].pool.poolDim*convLayer[convLayerNum-1].pool.poolDim + k*convLayer[convLayerNum-1].pool.poolDim + j] = convLayer[convLayerNum-1].pool.pool[i][k][j];
					}
				}
			}	
			/*
			for (int i = 0; i < convLayer[0].inputNum*convLayer[0].inputDim*convLayer[0].inputDim; i++) {
				input[i+convLayer[convLayerNum-1].pool.poolNum*convLayer[convLayerNum-1].pool.poolDim*convLayer[convLayerNum-1].pool.poolDim] = _pattern[i];
			}*/
		}
		
		float[] delta_last;
		if (layerNum == 1) {
			delta_last = layer[0].update(_error, input, _learningRate, _momentum);
		}
		else {
			delta_last = layer[layerNum-1].update(_error, layer[layerNum-2].hidden, _learningRate, _momentum);
			float[] delta = new float[layer[layerNum-2].hiddenNum];
			
			for (int l = layerNum-2; l >= 1; l--) {
				delta = new float[layer[l].hiddenNum];
				for (int i = 0; i < layer[l].hiddenNum; i++) {
					for (int k = 0; k < layer[l+1].hiddenNum; k++)
						delta[i] += delta_last[k] * layer[l+1].weight[k][i];
				}
				delta_last = layer[l].update(delta, layer[l-1].hidden, _learningRate, _momentum);
			}
	
			delta = new float[layer[0].hiddenNum];
			for (int i = 0; i < layer[0].hiddenNum; i++) {
				for (int k = 0; k < layer[1].hiddenNum; k++)
					delta[i] += delta_last[k] * layer[1].weight[k][i];
			}
			delta_last = layer[0].update(delta, input, _learningRate, _momentum);
		}		

		//calcolo delta
		float[][][] delta =null;
		if (convLayer[convLayerNum-1].pool != null) {
			delta = new float[convLayer[convLayerNum-1].pool.poolNum][convLayer[convLayerNum-1].pool.poolDim][convLayer[convLayerNum-1].pool.poolDim];
			for (int m = 0; m < convLayer[convLayerNum-1].pool.poolNum; m++) {
				for (int j = 0; j < convLayer[convLayerNum-1].pool.poolDim; j++) {
					for (int k = 0; k < convLayer[convLayerNum-1].pool.poolDim; k++) {
						delta[m][j][k] = 0.0f;
						for (int i = 0; i < layer[0].hiddenNum; i++) {
							delta[m][j][k] += delta_last[i] * layer[0].weight[i][(m*convLayer[convLayerNum-1].pool.poolDim + j)*convLayer[convLayerNum-1].pool.poolDim + k];
						}
					}
				}
			}		
		}
		else {
			delta = new float[convLayer[convLayerNum-1].mapNum][convLayer[convLayerNum-1].mapDim][convLayer[convLayerNum-1].mapDim];
			for (int m = 0; m < convLayer[convLayerNum-1].mapNum; m++) {
				for (int j = 0; j < convLayer[convLayerNum-1].mapDim; j++) {
					for (int k = 0; k < convLayer[convLayerNum-1].mapDim; k++) {
						delta[m][j][k] = 0.0f;
						for (int i = 0; i < layer[0].hiddenNum; i++) {
							delta[m][j][k] += delta_last[i] * layer[0].weight[i][(m*convLayer[convLayerNum-1].mapDim + j)*convLayer[convLayerNum-1].mapDim + k];
						}
					}
				}
			}
		}
		
		delta = convLayer[convLayerNum-1].update(delta, _learningRate, _momentum);

		float[][][] deltaCurr=null;
		float[][][] deltaCurr2=null;
		float[][][] zmap=null;
		for (int l = convLayerNum-2; l >= 0; l--) {
			zmap = zeroPad(delta,convLayer[l+1].kernelDim-1,convLayer[l+1].mapNum,convLayer[l+1].mapDim);
			
			if (convLayer[l].pool == null) {
 				deltaCurr = new float[convLayer[l].mapNum][convLayer[l].mapDim][convLayer[l].mapDim];
 				for (int m = 0; m < convLayer[l].mapNum; m++) {
					for (int m2 = 0; m2 < convLayer[l+1].mapNum; m2++)
						deltaCurr[m] = msum(deltaCurr[m],fullConvolve(flip(convLayer[l+1].kernel[m2][m]),delta[m2]));
				}
			}
			else {
 				deltaCurr = new float[convLayer[l].pool.poolNum][convLayer[l].pool.poolDim][convLayer[l].pool.poolDim];
 				//deltaCurr2 = new float[convLayer[l].pool.poolNum][convLayer[l].pool.poolDim][convLayer[l].pool.poolDim];
 				for (int m = 0; m < convLayer[l].pool.poolNum; m++) {
					for (int m2 = 0; m2 < convLayer[l+1].mapNum; m2++) {
						float temp[][] = fullConvolve(flip(convLayer[l+1].kernel[m2][m]),delta[m2]);
						for (int j = 0; j < convLayer[l].pool.poolDim; j++) {
							for (int k = 0; k < convLayer[l].pool.poolDim; k++) {
								deltaCurr[m][j][k] += temp[j][k];
							}
						}
						//deltaCurr[m] = msum(deltaCurr[m],fullConvolve(flip(convLayer[l+1].kernel[m2][m]),delta[m2]));
					}
						/*
					for (int i = 0; i < convLayer[l].pool.poolDim; i++) {
						for (int k = 0; k < convLayer[l].pool.poolDim; k++) {
							deltaCurr[m][i][k] = 0.0f;

							for (int m2 = 0; m2 < convLayer[l+1].mapNum; m2++) { 
								for (int h = 0; h < convLayer[l+1].kernelDim; h++) {
									for (int s = 0; s < convLayer[l+1].kernelDim; s++) {
										deltaCurr[m][i][k] += zmap[m2][i+h][k+s] * convLayer[l+1].kernel[m2][m][convLayer[l+1].kernelDim-1-h][convLayer[l+1].kernelDim-1-s];
									}
								}
							}
							
							if ( Math.abs(deltaCurr[m][i][k]-deltaCurr2[m][i][k]) > 0.001) System.out.println("convolve wrong "+deltaCurr[m][i][k]+" "+deltaCurr2[m][i][k]);
						}
					}*/
				}
			}
			delta = convLayer[l].update(deltaCurr, _learningRate, _momentum);
		}
	}
	
	public void updateTrue(float _learningRate, float _momentum) {
		for (int l = convLayerNum-1; l >= 0; l--) {
			convLayer[l].updateTrue(_learningRate, _momentum);
		}
		for (int l = layerNum-1; l >= 0; l--) {
			layer[l].updateTrue(_learningRate, _momentum);
		}
	}
	
	public static float[][] msum(float[][] m1, float[][] m2) {
		float rsum[][] = new float[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int k = 0; k < m1[0].length; k++)
				rsum[i][k] = m1[i][k] + m2[i][k];
		return rsum;
	}
	
	public static float[][] flip(float[][] inp) {
		float[][] result = new float[inp.length][inp[0].length];
		for (int i = 0; i < inp.length; i++)
			for (int k = 0; k < inp[0].length; k++)
				result[i][k] = inp[inp.length-1-i][inp[0].length-1-k];
		return result;
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
	
	public static float[][] fullConvolve(float[][] filter, float[][] map) {
		int fx = filter.length;
		int fy = filter[0].length;
		int mx = map.length;
		int my = map[0].length;
		float[][] result = new float[fx-1+mx][fy-1+my];
		float[][] zmap = new float[mx+2*(fx-1)][my+2*(fy-1)];
		for (int i = 0; i < mx+2*(fx-1); i++)
			for (int k = 0; k < my+2*(fy-1); k++)
				zmap[i][k] = 0.0f;
		for (int i = 0; i < mx; i++)
			for (int k = 0; k < my; k++)
				zmap[fx-1+i][fy-1+k] = map[i][k];

		for (int i = 0; i < fx-1+mx; i++) {
			for (int k = 0; k < fy-1+my; k++) {
				result[i][k] = 0.0f;
				for (int h = 0; h < fx; h++)
					for (int s = 0; s < fy; s++)
						result[i][k] += filter[h][s] * zmap[i+h][k+s];
			}
		}
		
		return result;
	}
	
	public float[] feed(float[] _input) {
/*		int ke = 2;
		float[][][] temp = new float[convLayer[0].inputNum][convLayer[0].inputDim-ke][convLayer[0].inputDim-ke];
		for (int i = 0; i < convLayer[0].inputNum; i++)
			for (int k = 0; k < convLayer[0].inputDim-ke; k++)
				for (int j = 0; j < convLayer[0].inputDim-ke; j++)
					temp[i][k][j] = _input[i*(convLayer[0].inputDim-ke)*(convLayer[0].inputDim-ke) +k*(convLayer[0].inputDim-ke)+j];		

		float[][][] convInput = zeroPad(temp,1,convLayer[0].inputNum,convLayer[0].inputDim-ke);
*/
		float[][][] convInput = new float[convLayer[0].inputNum][convLayer[0].inputDim][convLayer[0].inputDim];
		for (int i = 0; i < convLayer[0].inputNum; i++)
			for (int k = 0; k < convLayer[0].inputDim; k++)
				for (int j = 0; j < convLayer[0].inputDim; j++)
					convInput[i][k][j] = _input[i*convLayer[0].inputDim*convLayer[0].inputDim +k*convLayer[0].inputDim+j];
		
		convLayer[0].feed(convInput);
		for (int c = 1; c < convLayerNum; c++) {
			if (convLayer[c-1].pool == null)
				convLayer[c].feed(convLayer[c-1].map);
			else
				convLayer[c].feed(convLayer[c-1].pool.pool);
		}

		float[] input = new float[layer[0].inputNum];

		if (convLayer[convLayerNum-1].pool == null) {
			for (int i = 0; i < convLayer[convLayerNum-1].mapNum; i++) {
				for (int k = 0; k < convLayer[convLayerNum-1].mapDim; k++) {
					for (int j = 0; j < convLayer[convLayerNum-1].mapDim; j++) {
						input[i*convLayer[convLayerNum-1].mapDim*convLayer[convLayerNum-1].mapDim +k*convLayer[convLayerNum-1].mapDim + j] = convLayer[convLayerNum-1].map[i][k][j];
					}
				}
			}
			/*
			for (int i = 0; i < convLayer[0].inputNum*convLayer[0].inputDim*convLayer[0].inputDim; i++) {
				input[i+convLayer[convLayerNum-1].mapNum*convLayer[convLayerNum-1].mapDim*convLayer[convLayerNum-1].mapDim] = _input[i];
			}*/
		}
		else {
			for (int i = 0; i < convLayer[convLayerNum-1].pool.poolNum; i++) {
				for (int k = 0; k < convLayer[convLayerNum-1].pool.poolDim; k++) {
					for (int j = 0; j < convLayer[convLayerNum-1].pool.poolDim; j++) {
						input[i*convLayer[convLayerNum-1].pool.poolDim*convLayer[convLayerNum-1].pool.poolDim + k*convLayer[convLayerNum-1].pool.poolDim + j] = convLayer[convLayerNum-1].pool.pool[i][k][j];
					}
				}
			}
			/*
			for (int i = 0; i < convLayer[0].inputNum*convLayer[0].inputDim*convLayer[0].inputDim; i++) {
				input[i+convLayer[convLayerNum-1].pool.poolNum*convLayer[convLayerNum-1].pool.poolDim*convLayer[convLayerNum-1].pool.poolDim] = _input[i];
			}*/
		}

		layer[0].feed(input);

		for (int l = 1; l < layerNum; l++) {
			layer[l].feed(layer[l-1].hidden);
		}

		return layer[layerNum-1].hidden;
	}
	
	public float wmax(int l) {
		float max=convLayer[l].kernel[0][0][0][0];
		for (int m = 0; m < convLayer[l].mapNum; m++) {
			for (int n = 0; n < convLayer[l].inputNum; n++) {
				for (int j = 0; j < convLayer[l].kernelDim; j++) {
					for (int k = 0; k < convLayer[l].kernelDim; k++) {
						if (convLayer[l].kernel[m][n][j][k] > max)
							max = convLayer[l].kernel[m][n][j][k];
					}
				}
			}
		}
		return max;
	}
	
	public float wmin(int l) {
		float min=convLayer[l].kernel[0][0][0][0];
		for (int m = 0; m < convLayer[l].mapNum; m++) {
			for (int n = 0; n < convLayer[l].inputNum; n++) {
				for (int j = 0; j < convLayer[l].kernelDim; j++) {
					for (int k = 0; k < convLayer[l].kernelDim; k++) {
						if (convLayer[l].kernel[m][n][j][k] < min)
							min = convLayer[l].kernel[m][n][j][k];
					}
				}
			}
		}
		return min;		
	}
	
	public void save(String _saveFile) {
		for (int l = 0; l < convLayerNum; l++) {
			convLayer[l].save(_saveFile+".convlayer"+l);
		}
		for (int l = 0; l < layerNum; l++) {
			layer[l].save(_saveFile+".layer"+l);
		}
	}
	
	public void load(String _loadFile) {
		for (int l = 0; l < convLayerNum; l++) {
			convLayer[l].load(_loadFile+".convlayer"+l);
		}
		for (int l = 0; l < layerNum; l++) {
			layer[l].load(_loadFile+".layer"+l);
		}
	}

	public void train(String _dataFile, String _labelFile, float _learningRate, float _momentum, float _iterations) {	
		trainSerial(_dataFile,_labelFile,_learningRate,_momentum,_iterations);
	}
	
	public void trainSerial(String _dataFile, String _labelFile, float _learningRate, float _momentum, float _iterations) {	
		DataSet data = new DataSet(_dataFile);
		DataSet label = null;
		//data.check();
		
		if (_dataFile == _labelFile) {
			label = data;
		}
		else {
			label = new DataSet(_labelFile);
		}
		//data.averageSubtraction();
		//data.zeroMean();
		//data.scale();
		//data.subtractMean("cifarMean.vet");
		//data.subtractMean("cifarMean.vet");
		
		
		float[] output=null;
		float[] desidered;
		float[] error = new float[layer[layerNum-1].hiddenNum];
		float[] input = new float[convLayer[0].inputNum*convLayer[0].inputDim*convLayer[0].inputDim];
		int errors;
		float reconerr;
		boolean wrong = false;
		long start = System.currentTimeMillis();
		long time = System.currentTimeMillis();
		int current;
		int Omax, Hmax;
		Random rand = new Random();
		long x;
		
		//for (int l = 0; l < convLayerNum; l++)
		//	convLayer[l].resetCache();

		
		for (int e = 0; e < 1; e++) {
			errors = 0;
			reconerr = 0;
			for (int p = 0; p < data.pattern*_iterations; p++) {
				x = System.currentTimeMillis();
				current = rand.nextInt(data.pattern);
				input = data.getPatternFloat(current);
				output = this.feed(input);
				
				desidered = label.getPatternFloat(current);
				
				for (int i = 0; i < label.dataDim; i++) {
					error[i] = desidered[i] - output[i];
				}
				//error = dcost(output,desidered);
				if (discriminative) {
					wrong = false;
					for (int i = 0; i < label.dataDim; i++) {
						if (Math.abs(error[i]) > 0.1) {
							wrong = true;
						}
					}
					
					Omax = 0;
					for (int i = 1; i < label.dataDim; i++) {
						if (desidered[i] > desidered[Omax])
							Omax = i;
					}
					
					Hmax = 0;
					for (int i = 1; i < label.dataDim; i++) {
						if (output[i] > output[Hmax])
							Hmax = i;
					}
					
					if (Omax != Hmax)
						errors++;
					
					if (p % 1000 == 0 ) {					
						System.out.printf("%d %.2f - %d %dms current: %d, %d - %d",(p+1),100.0f*(float)(p-errors+1)/(p+1),errors,(System.currentTimeMillis() - start),current, Omax, Hmax);
						start = System.currentTimeMillis();
						String s = "";
						for (int l = 0; l < convLayerNum; l++) {
							System.out.printf("\nmax %f min %f",wmax(l),wmin(l));
							convLayer[l].gradientWMaxMin();
							convLayer[l].outputMaxMin();
						}						
						float mx = layer[0].hidden[0];
						float mn = mx;
						for (int j = 0; j < layer[0].hiddenNum; j++) {
							mx = Math.max(layer[0].net[j], mx);
							mn = Math.min(layer[0].net[j], mn);
						}
						System.out.printf("\nsoftout max %f min %f ",mx,mn);
						
						float w_max=layer[0].weight[0][0], w_min=w_max;
						float g_max=layer[0].deltaWeight[0][0], g_min=g_max;
						for (int j = 0; j < layer[0].hiddenNum; j++) {
							for (int i = 0; i < layer[0].inputNum; i++) {
								w_max = Math.max(w_max,layer[0].weight[j][i]);
								w_min = Math.min(w_min,layer[0].weight[j][i]);
								g_max = Math.max(g_max,layer[0].deltaWeight[j][i]);
								g_min = Math.min(g_min,layer[0].deltaWeight[j][i]);
							}
						}
						System.out.printf("max %f min %f gmax %f min %f\n",w_max,w_min,g_max,g_min);
						System.out.println(s);
					}
					/*
					if ( (p+1) % 11000 == 0) {
						this.test(data, label);
					}*/
				}
				else {
					for (int i = 1; i < label.dataDim; i++) {
						reconerr += output[i];
					}
					
					if (p % 1000 == 0) {					
						System.out.printf("%d %.3f %dms\n",(p+1),reconerr/(p+1),(System.currentTimeMillis() - start));
						start = System.currentTimeMillis();
					}
				}

				this.update(error, data.getPatternFloat(current), _learningRate, _momentum);
				this.updateTrue(_learningRate, _momentum);
				/*try {
					Thread.sleep((System.currentTimeMillis()-x)/5);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
			}
		}
		System.out.println("Trained in: " + (System.currentTimeMillis() - time));
	}
	
	
	public float test(String _dataFile, String _labelFile) {	
		DataSet data = new DataSet(_dataFile);
		DataSet label = new DataSet(_labelFile);
		//data.scale();
		//data.averageSubtraction();
		//data.subtractMean("cifarMean.vet");
		float[] output;
		float[] desidered = new float[label.dataDim];
		float[] input = new float[convLayer[0].inputNum*convLayer[0].inputDim*convLayer[0].inputDim];
		int[] confusion = new int[label.dataDim];
		int[] totalconf = new int[label.dataDim];
		for (int i = 0; i < label.pattern; i++) {
			float[] temp = label.getPatternFloat(i);
			int max = 0;
			for (int k = 0; k < label.dataDim; k++)
				if (temp[k] > temp[max])
					max = k;
			totalconf[max]++;
		}
		float errors = 0;
		int errors2 = 0;
		int Omax, Hmax;
		long start = System.currentTimeMillis();
		Random rand = new Random();
		
		for (int p = 0; p < data.pattern; p++) {
			input = data.getPatternFloat(p);
			/*
			if (rand.nextFloat() < 0.5f) {
				for (int i = 0; i < input.length; i++)
					input[i] = 1-input[i];
			}*/
			
			
			output = this.feed(input);
				
			desidered = label.getPatternFloat(p);
			
			if (discriminative) {
				Omax = 0;
				for (int i = 1; i < label.dataDim; i++) {
					if (desidered[i] > desidered[Omax])
						Omax = i;
				}
				
				Hmax = 0;
				for (int i = 1; i < label.dataDim; i++) {
					if (output[i] > output[Hmax])
						Hmax = i;
				}
				
				if (Omax != Hmax) {
					confusion[Omax]++;
					int Hmax2 = 0;
					//vedo se sta nella seconda best answer
					for (int i = 1; i < label.dataDim; i++) {
						if (i != Hmax) {
							if (output[i] > output[Hmax2])
								Hmax2 = i;
						}
					}		
					
					if (Omax != Hmax2) {
						errors2++;
						errors++;
					}
					else {
						errors++;
					}
				}
				
				if (p % 1000 == 0) {
					System.out.printf("%d %.2f - %d, %d - %d\n",(p+1),100.0f*(float)(p-errors+1)/(p+1),(int)errors,Omax, Hmax);
				}
			} 
			else {
				for (int i = 1; i < label.dataDim; i++) {
					errors += Math.abs(desidered[i]-output[i]);
				}
				
				if (p % 1000 == 0) {
					System.out.printf("%d Reconstruction error: %.3f\n",(p+1),errors/(p+1));
				}
			}
		}
		
		System.out.print("Final: " + 100.0f*(float)(data.pattern-errors)/data.pattern + "\nSecond best: " + 100.0f*(float)(data.pattern-errors2)/data.pattern + "\nTested in: " + (System.currentTimeMillis() - start));
		for (int i = 0; i < label.dataDim; i++) {
			if (i%10 == 0)
				System.out.print("\n");
			System.out.printf("%d: %.2f|",i,100.0f-(((float)confusion[i])/totalconf[i]*100.0f));
		}
		System.out.printf("\n");
		return 100.0f*(float)(data.pattern-errors)/data.pattern;
	}

	public float test(DataSet data, DataSet label) {	
		float[] output;
		float[] desidered = new float[label.dataDim];
		
		float errors = 0;
		int errors2 = 0;
		int Omax, Hmax;
		long start = System.currentTimeMillis();
		
		for (int p = 0; p < data.pattern; p++) {
			
			output = this.feed(data.getPatternFloat(p));
				
			desidered = label.getPatternFloat(p);
			
			if (discriminative) {
				Omax = 0;
				for (int i = 1; i < label.dataDim; i++) {
					if (desidered[i] > desidered[Omax])
						Omax = i;
				}
				
				Hmax = 0;
				for (int i = 1; i < label.dataDim; i++) {
					if (output[i] > output[Hmax])
						Hmax = i;
				}
				
				if (Omax != Hmax) {
				
					int Hmax2 = 0;
					//vedo se sta nella seconda best answer
					for (int i = 1; i < label.dataDim; i++) {
						if (i != Hmax) {
							if (output[i] > output[Hmax2])
								Hmax2 = i;
						}
					}		
					
					if (Omax != Hmax2) {
						errors2++;
						errors++;
					}
					else {
						errors++;
					}
				}
				
				if (p % 1000 == 0) {
					System.out.printf("%d %.2f - %d, %d - %d\n",(p+1),100.0f*(float)(p-errors+1)/(p+1),(int)errors,Omax, Hmax);
				}
			} 
			else {
				for (int i = 1; i < label.dataDim; i++) {
					errors += Math.abs(desidered[i]-output[i]);
				}
				
				if (p % 1000 == 0) {
					System.out.printf("%d Reconstruction error: %.3f\n",(p+1),errors/(p+1));
				}
			}
		}
		
		System.out.println("Final: " + 100.0f*(float)(data.pattern-errors)/data.pattern + "\nSecond best: " + 100.0f*(float)(data.pattern-errors2)/data.pattern + "\nTested in: " + (System.currentTimeMillis() - start));
		return 100.0f*(float)(data.pattern-errors)/data.pattern;
	}

	public int predict(float[] input) {
		float[] output = this.feed(input);
		
		int max = 0;
		for (int i = 1; i < this.layer[layerNum-1].hiddenNum; i++) {
			if (output[i] > output[max])
				max = i;
		}
		
		return max;
	}

	public static void main(String args[]) {
		int hiddenConv[][] = {{1,28,8,5,2},{8,12,16,5,2},{16,4,64,3,2}};
		int hiddenFull[] = {9};
		CNN cnn = new CNN(hiddenConv,hiddenFull);
		//cnn.load("mnist_98.61");
		float acc = 0.0f;
		//acc =cnn.test(Base.mnistTest, Base.mnistTestLabel);
		System.out.println(acc);
		for (int i = 0; i < 100; i++) {
			cnn.train(Base.machineDigitsTrain, Base.machineDigitsTrainLabel, 0.001f, 0.9f, 1);
			//cnn.train(Base.mnistTrain, Base.mnistTrainLabel, 0.001f, 0.9f, 1);
			acc = cnn.test(Base.machineDigitsTrain, Base.machineDigitsTrainLabel);
			cnn.save("machine_"+acc);
		}
	}
}
