package neuralNetworks;


public class LayerMaxPooling {
	public int poolSize;
	public int poolStride;
	public int poolDim;
	public int poolNum;
	public float[][][] pool;
	
	public int mapDim;
	public float[][][] mask;
	public float[][][] _input;
	
	LayerMaxPooling(int _mapNum, int _mapDim, int _poolSize) {
		poolNum = _mapNum;
		poolSize = _poolSize;
		poolStride = _poolSize;
		poolDim = (_mapDim - (poolSize-poolStride))/poolStride;
		
		mapDim = _mapDim;
		
		pool = new float[poolNum][poolDim][poolDim];
		mask = new float[poolNum][mapDim][mapDim];
		for (int i = 0; i < poolNum; i++) {
			for (int j = 0; j < mapDim; j++) {
				for (int h = 0; h < mapDim; h++)
					mask[i][j][h] = 0.0f;
			}
		}
	}
	
	public void feed(final float[][][] _input) {
		float max;
		int mk=0, ml=0;
		for (int i = 0; i < poolNum; i++) {
			for (int j = 0; j < poolDim; j++) {
				for (int h = 0; h < poolDim; h++) {
					max = _input[i][j*poolStride][h*poolStride];
					pool[i][j][h] = _input[i][j*poolStride][h*poolStride];//max;
					mk = 0;
					ml = 0;
					for (int k = 0; k < poolSize; k++) {
						for (int l = 0; l < poolSize; l++) {
							//pool[i][j][h] = Math.max(pool[i][j][h], _input[i][j*poolStride+k][h*poolStride+l]);
							if (_input[i][j*poolStride+k][h*poolStride+l] > max) {
								max = _input[i][j*poolStride+k][h*poolStride+l];
								pool[i][j][h] = max;
								mk = k;
								ml = l;
							}
							mask[i][j*poolStride+k][h*poolStride+l] = 0.0f;
						}
					}
					mask[i][j*poolStride+mk][h*poolStride+ml] = 1.0f;
				}
			}
		}
	}
	
	public float[][][] update(float[][][] delta) {
		for (int i = 0; i < poolNum; i++) {
			for (int j = 0; j < mapDim; j++) {
				for (int h = 0; h < mapDim; h++)
					mask[i][j][h] *= delta[i][j][h];
			}
		}		
		return mask;
	}
	
	public float[][][] update(float[] delta) {
		for (int i = 0; i < poolNum; i++) {
			for (int j = 0; j < mapDim; j++) {
				for (int h = 0; h < mapDim; h++) {
					mask[i][j][h] *= delta[i*mapDim*mapDim+j*mapDim+h];
				}
			}
		}		
		return mask;
	}
	
	public void initCL() {
		
	}
}
