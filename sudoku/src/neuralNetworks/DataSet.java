package neuralNetworks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class DataSet {
	public int pattern;
	public int subPattern;
	public int dataDim;
	public int cropFactor;
	public float[][] data;
	public byte[][] bdata;
	public int[][] integ;
	public int[][] integ3;
	public String[] fileName;
	public float meanvet[];
	public int version;
	
	public DataSet() {}
	
	public DataSet(String _fileName) {
		byte[] bfile = Base.read(_fileName);
		ByteBuffer bb = ByteBuffer.wrap(bfile);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		if (bfile[0] != 0x76) version = 0;
		else {
			version = ((bfile[1]&0xFF - 48)<<8)+(bfile[2]&0xFF-48);
			int temp = bb.getInt();
		}

		pattern = bb.getInt();
		dataDim = bb.getInt();
		cropFactor = bb.getInt();
		subPattern = pattern/cropFactor;

		System.out.println("Version: "+version+" "+pattern + "x" + dataDim);
		
		if (cropFactor > 1) {
			pattern = pattern/cropFactor;
			data = new float[pattern][dataDim];	
			
			for (int p = 0; p < pattern; p++) {
				for (int i = 0; i < dataDim; i++) {
					data[p][i] = (float)bb.getDouble();
				}
			}
		}
		else {
			if (version == 0) {
				data = new float[pattern][dataDim];
				//System.out.println(dataDim);
				for (int p = 0; p < pattern; p++) {
					for (int i = 0; i < dataDim; i++) {
						data[p][i] = (float)bb.getDouble();
					}
				}
			}
			else if (version == 1) {
				bdata = new byte[pattern][1];
				//System.out.println(dataDim);
				for (int p = 0; p < pattern; p++) {
					bdata[p][0] = bb.get();
				}
			}
			else {
				bdata = new byte[pattern][dataDim];
				//System.out.println(dataDim);
				for (int p = 0; p < pattern; p++) {
					for (int i = 0; i < dataDim; i++) {
						bdata[p][i] = bb.get();
					}
				}
			}
		}
	}
	
	public void dataAugmentation() {
		Random rand = new Random();
		float[] temp = new float[dataDim];
		int dim = (int) Math.sqrt(dataDim/3);
		System.out.println(dim);
		int q = (int)(dim/3 - 1);
		for (int p = 0; p < pattern; p++) {
			float[] media = new float[3];
			for (int i = 0; i < dataDim; i++) {
				if (version == 0) media[(int)(i/(dataDim/3))] += data[p][i]*255;
				else media[(int)(i/(dataDim/3))] += bdata[p][i]&0xFF;
			}
			for (int i = 0; i < media.length; i++)
				media[i] /= dataDim/3;
			
			//flip e traslazione
	    	int offx = rand.nextInt(q)-q/2;
	    	int offy = rand.nextInt(q)-q/2;
	    	int flip = 0;
	    	if (rand.nextBoolean()) {
				flip = 0;
			}
			for (int i = 0; i < dataDim; i++)
				temp[i]=0.0f;//-meanvet[i];
			for (int j = 0; j < 3; j++) {
				for (int i = 0; i < dim; i++) {
					for (int k = 0; k < dim; k++) {
						float black=1.0f;
				    	int x2 = i+offx;
				    	int y = k+offy;
				    	if (x2 < 0) {
				    		x2 = 0;
				    		black = 0.0f;
				    	}
				    	if (x2 > dim-1) {
				    		x2 = dim-1;
				    		black = 0.0f;
				    	}
				    	if (y < 0) {
				    		y = 0;
				    		black = 0.0f;
				    	}
				    	if (y > dim-1) {
				    		y = dim-1;
				    		black = 0.0f;
				    	}
				    	if (version == 0)
				    		temp[(int)((i)*dim+flip*(dim-1-k) + (1-flip)*k ) + j*dim*dim] = black*data[p][(int)((x2)*dim+y) + j*dim*dim];//+ (1-black)*128;//meanvet[(int)((x2)*dim+y) + j*dim*dim]*255;
				    	else {
				    		temp[(int)((i)*dim+flip*(dim-1-k) + (1-flip)*k ) + j*dim*dim] = black*bdata[p][(int)((x2)*dim+y) + j*dim*dim]/255.0f;//+ (1-black)*0.5;//meanvet[(int)((x2)*dim+y) + j*dim*dim];
				    	}
					}
				}
			}
			for (int i = 0; i < dataDim; i++) {
				if (version == 0) {
					data[p][i] = (float)(temp[i]);
					if (temp[i] == 0.0) data[p][i] = (float)(media[(int)(i/(dataDim/3))]/255.0);
				}
				else {
					bdata[p][i] = (byte)(255*temp[i]);
					if (temp[i] == 0.0) bdata[p][i] = (byte)(media[(int)(i/(dataDim/3))]);
				}
			}
		}
	}
	
	public void check() {
		for (int p = 0; p < pattern; p++) {
			for (int i = 0; i < dataDim; i++) {
				if (data[p][i] < 0.0f || data[p][i] > 1.0f)
					System.out.println("ALARM IM DARM! " + data[p][i]);
			}
		}		
	}
	
	public void averageSubtraction() {
		for (int p = 0; p < pattern; p++) {
			float average = 0;
			for (int i = 0; i < dataDim; i++) {
				average += data[p][i];
			}
			average /= dataDim;
			for (int i = 0; i < dataDim; i++) {
				data[p][i] -= average;
			}
		}		
	}
	
	public void scale() {
		for (int p = 0; p < pattern; p++) {
			for (int i = 0; i < dataDim; i++) {
				data[p][i] = data[p][i]*2.0f - 1.0f;
			}
		}		
	}
	
	public void subtractMean(String _fileName) {
		meanvet = new float[dataDim];
		if (_fileName != null) {
			ByteBuffer b = ByteBuffer.wrap(Base.read(_fileName));
			b.order(ByteOrder.LITTLE_ENDIAN);
			for (int k = 0; k < dataDim; k++) {
				meanvet[k] = b.getFloat();
				//System.out.println(meanvet[k]);
			}
			
			if (cropFactor > 1) {
				
			}
			else {
				if (version == 0) {
					//sottrazione del vettore media
					for (int p = 0; p < pattern; p++) {
						for (int i = 0; i < dataDim; i++) {
							data[p][i] -= meanvet[i];
						}
					}
				} else {/*
					//sottrazione del vettore media
					for (int p = 0; p < pattern; p++) {
						for (int i = 0; i < dataDim; i++) {
							bdata[p][i] -= (byte)(meanvet[i]*255);
						}
					}*/
				}
			}		
		}
		else for (int k = 0; k < dataDim; k++) meanvet[k] = 0.0f;
	}
	
	public float[] zeroMean() {
		float[] meanvet = new float[dataDim];
		
		for (int i = 0; i < dataDim; i++) {
			meanvet[i] = 0.0f;
		}
		
		if (cropFactor > 1) {
			
		}
		else {
			if (version == 0) {
				//calcolo vettore media
				for (int p = 0; p < pattern; p++) {
					for (int i = 0; i < dataDim; i++) {
						meanvet[i] += data[p][i];
					}
				}
				for (int i = 0; i < dataDim; i++) {
					meanvet[i] /= pattern;
				}
			} else {
				//calcolo vettore media
				for (int p = 0; p < pattern; p++) {
					for (int i = 0; i < dataDim; i++) {
						meanvet[i] += (bdata[p][i]&0xFF)/255.0f;
					}
				}
				for (int i = 0; i < dataDim; i++) {
					meanvet[i] /= pattern;
				}				
			}
		}
		
		return meanvet;
	}
	
	public float[] getPattern(int _pattern) {
		float[] ret = null;
		if (version == 0)
			ret = data[_pattern % subPattern];
		else if (version == 1) { //label
			ret = new float[dataDim];
			ret[(bdata[_pattern][0]&0xFF)] = 1.0f;
		}
		else if (version == 2) {
			ret = new float[dataDim];
			for (int i = 0; i < dataDim; i++) {
				ret[i] = (float) ((bdata[_pattern][i]&0xFF)/255.0f);//-meanvet[i]);
			}
		}
		return ret;
	}
	
	public int getLabelRaw(int _pattern) {
		if (version == 1) { //label
			return (bdata[_pattern][0]&0xFF);
		}
		return -1;
	}
	
	public static float[] btof(byte[] bdata, String mean) {
		float[] ret = new float[bdata.length];
		float[] meanvet = new float[ret.length];
		
		ByteBuffer b = ByteBuffer.wrap(Base.read(mean));
		b.order(ByteOrder.LITTLE_ENDIAN);
		for (int k = 0; k < ret.length; k++) {
			meanvet[k] = b.getFloat();
		}
		
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (float) ((bdata[i]&0xFF)/255.0f-meanvet[i]);
		}
		return ret;
	}
	
	public float[] getPatternCrop(int _pattern, int channel, int dim) {
		float[] ret = null;
		if (version == 0) {
			int dimX = dim;
			int dimY = dim;
			int ch = channel;
			int dx = (int) Math.sqrt(dataDim/3);
			int dy = (int) Math.sqrt(dataDim/3);
			ret = new float[3*dimX*dimY];
			int istart = (int) (Math.random()*(dx-dimX));
			int kstart = (int) (Math.random()*(dy-dimY));
			//System.out.println(dx+" "+channel+" "+dimX+" "+kstart+" "+istart);
			for (int c = 0; c < ch; c++) {
				for (int i = istart; i < dimX+istart; i++) {
					for (int k = kstart; k < dimY+kstart; k++) {
						ret[c*dimX*dimY+(i-istart)*dimY+(k-kstart)] = data[_pattern][c*dx*dy+i*dy+k];
					}
				}
			}
		}
		else if (version == 1) { //label
			ret = new float[dataDim];
			ret[(bdata[_pattern][0]&0xFF)] = 1.0f;
		}
		else if (version == 2) {
			int dimX = dim;
			int dimY = dim;
			int ch = channel;
			int dx = (int) Math.sqrt(dataDim/3);
			int dy = (int) Math.sqrt(dataDim/3);
			ret = new float[3*dimX*dimY];
			int istart = (int) (Math.random()*(dx-dimX));
			int kstart = (int) (Math.random()*(dy-dimY));
			for (int c = 0; c < ch; c++) {
				for (int i = istart; i < dimX+istart; i++) {
					for (int k = kstart; k < dimY+kstart; k++) {
						ret[c*dimX*dimY+(i-istart)*dimY+(k-kstart)] = (float) ((bdata[_pattern][c*dx*dy+i*dy+k]&0xFF)/255.0f-meanvet[c*dx*dy+i*dy+k]);
					}
				}
			}
		}
		return ret;
	}
	
	public float[] getPatternFloat(int _pattern) {
		float[] ret = null;
		if (version == 0) {
			ret = new float[dataDim];
			for (int i = 0; i < dataDim; i++)
				ret[i] = data[_pattern % subPattern][i];
		}
		else if (version == 1) { //label
			ret = new float[dataDim];
			ret[(bdata[_pattern][0]&0xFF)] = 1.0f;
		}
		else if (version == 2) {
			ret = new float[dataDim];
			for (int i = 0; i < dataDim; i++) {
				ret[i] = (float) ((bdata[_pattern][i]&0xFF)/255.0f-meanvet[i]);
			}
		}
		return ret;
	}
	
	public void cropData(String newFile, int newDim) {
		byte[] _data = new byte[4];
	
		//pattern
		_data[0] = (byte)(pattern%256);
		_data[1] = (byte)(pattern/256 % 256);
		_data[2] = (byte)(pattern/(256*256) % 256);
		_data[3] = (byte)(pattern/(256*256*256) % 256);
		Base.write(newFile, _data, true);
		
		//dataDim
		dataDim = 400;
		_data[0] = (byte)(dataDim%256);
		_data[1] = (byte)(dataDim/256 % 256);
		_data[2] = (byte)(dataDim/(256*256) % 256);
		_data[3] = (byte)(dataDim/(256*256*256) % 256);
		Base.write(newFile, _data, true);
		
		//cropFactor
		_data[0] = 1;
		_data[1] = 0;
		_data[2] = 0;
		_data[3] = 0;
		Base.write(newFile, _data, true);
		
		_data = new byte[400*8];
		
		for (int p = 0; p < pattern; p++) {
			for (int i = 0; i < 20; i++) {
				for (int j = 0; j < 20; j++) {
					ByteBuffer.wrap(_data, (i*20 + j)*8, 8).order(ByteOrder.LITTLE_ENDIAN).putFloat(data[p][28*4+4+(i*28)+j]);
				}
			}
			Base.write(newFile, _data, true);
		}
	}
	
	public static void crop(String _dataFile, int cropFactor, int pattern, int dataDim) {
		int cropPattern = pattern/cropFactor;
		System.out.println(cropPattern);
		File file = new File(_dataFile);
		try {
			InputStream input = null;
			try {
				input = new BufferedInputStream(new FileInputStream(file));

				byte[] temp = new byte[12];
				input.read(temp, 0, 12);
				
				for (int i = 0; i < cropFactor; i++) {
					int count = 0;
					byte[] data = new byte[cropPattern*8*dataDim];
					System.out.println(data.length);
					while (count < data.length) {
						int rimasti = data.length - count;
						int letti = input.read(data, count, rimasti);
						if (letti > 0)
							count += letti;
					}					
					Base.write(_dataFile+i, data,true);
				}
			}
			finally {
				input.close();
			}
		}
		catch (FileNotFoundException e) {
			System.out.println(_dataFile + " - File non trovato!");
			System.exit(0);
		}
		catch (IOException e) {
			System.out.println("IOException");
			System.exit(0);
		}
	}
	
	public static void main(String args[]) {
		DataSet d = new DataSet(Base.mnist32Train);
		float[] mean = d.zeroMean();
		byte[] ba = new byte[mean.length*8];
		ByteBuffer b = ByteBuffer.wrap(ba);
		b.order(ByteOrder.LITTLE_ENDIAN);
		for (int k = 0; k < mean.length; k++) {
			b.putFloat(mean[k]);
		}
		Base.write("mnist32Mean.vet", ba);		
				
		/*float[] d = new float[3072];
		for (int k = 0; k < 3072; k++)
			d[k] = 0;
		for (int i = 0; i < 5; i++) {
			ByteBuffer b = ByteBuffer.wrap(Base.read("cifarExtMean"+(i+1)));
			b.order(ByteOrder.LITTLE_ENDIAN);
			for (int k = 0; k < 3072; k++)
				d[k] += b.getFloat();
		}
		byte[] mean = new byte[3072*8];
		ByteBuffer b = ByteBuffer.wrap(mean);
		for (int k = 0; k < 3072; k++)
			b.putFloat(d[k]/5.0f);
		Base.write("cifarMean.vet", mean);
		/*
		DataSet d = new DataSet("C:\\Users\\Redirectk\\Desktop\\NeuralNetwork\\mnist\\mnist.train");
		d.cropData("C:\\Users\\Redirectk\\Desktop\\NeuralNetwork\\mnist\\mnist20.train", 20);
		d = null;
		d = new DataSet("C:\\Users\\Redirectk\\Desktop\\NeuralNetwork\\mnist\\mnist.test");
		d.cropData("C:\\Users\\Redirectk\\Desktop\\NeuralNetwork\\mnist\\mnist20.test", 20);
		d = null;*/
	}
}
