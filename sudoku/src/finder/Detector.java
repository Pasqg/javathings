package finder;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import neuralNetworks.CNN;

public class Detector {
	public static byte[] grayscaleInt(int[] grayScale) {
		byte[] result = new byte[3*grayScale.length];
		for (int i = 0; i < grayScale.length; i++) {
			result[3*i] = (byte)grayScale[i];
			result[3*i+1] = (byte)grayScale[i];
			result[3*i+2] = (byte)grayScale[i];
		}
		return result;
	}
	/*
	public static byte[][] extract (net.CNNCL c) {
		
	}*/
	
	public static byte[][] preprocess(byte[] original, int width, int height) {
		//tograyscale
		int[] grayScale = new int[width*height];
		for (int i = 0; i < grayScale.length; i++) {
			grayScale[i] = (original[3*i]&0xFF)+(original[3*i+1]&0xFF)+(original[3*i+2]&0xFF);
			grayScale[i] /= 3;
		}
		saveImage(grayscaleInt(grayScale),width,height,"00_grayscale.png");
		
		//blur
		int blurRange = Math.min(width, height)/100;
		int[] blur = boxBlur(grayScale,width,height,1);
		for (int i = 0; i < blurRange; i++) {
			blur = boxBlur(blur,width,height,3);
		}
		
		//int[] blur = gaussianBlur(grayScale,width,height,blurRange);
		/*width -= 4*(blurRange+1);
		height -= 4*(blurRange+1);*/
		saveImage(grayscaleInt(blur),width,height,"01_blur.png");
		
		//adaptative threshold
		int thresholdSize = 7;
		int thresholdGain = -2;	
		width = (width-thresholdSize+1);
		height = (height-thresholdSize+1);
		byte[] threshold = adaptiveThreshold(blur,width,height,thresholdSize,thresholdGain);

		int[] thresholdInt = new int[width*height];
		for(int i = 0; i < thresholdInt.length; i++) thresholdInt[i] = threshold[i]&0xFF;
		int[] thresholdInt2 = new int[width*height];
		for(int i = 0; i < thresholdInt2.length; i++) thresholdInt2[i] = threshold[i]&0xFF;
		saveImage(grayscaleInt(thresholdInt),width,height,"02_threshold.png");
		
		//IN CASO DI PROBLEMI: SFOCARE ULTERIORMENTE L'IMMAGINE E THRESHOLDARLA A VALORE COSTANTE (ci sta il metodo apposta)
		
		//grid finding
		BlobFinder b = new BlobFinder(width, height);
		ArrayList<BlobFinder.Blob> blobList = new ArrayList<BlobFinder.Blob>();
		int[] blobMax = findBlob(thresholdInt2,width,height,blobList);
		//trova il blob di area massima (NON massa), quindi la griglia
		int max = 0;
		int areaMax = blobList.get(0).mass;
		for (int i = 1; i < blobList.size(); i++) {
			if (blobList.get(i).mass > areaMax) {
				areaMax = blobList.get(i).mass;
				max = i;
			}
		}
		saveImage(grayscaleInt(thresholdInt2),width,height,"03_tblobmax.png");
		saveImage(grayscaleInt(blobMax),width,height,"03_blobmax.png");
		
		//ritaglio max blob
		int w2 = blobList.get(max).xMax-blobList.get(max).xMin;
		int h2 = blobList.get(max).yMax-blobList.get(max).yMin;
		int cut[] = new int[w2*h2];
		int imgcut[] = new int[w2*h2];
		for (int i = 0; i < h2; i++) {
			for (int k = 0; k < w2; k++) {
				cut[i*w2+k] = blobMax[(i+blobList.get(max).yMin)*(width)+k+blobList.get(max).xMin];
				imgcut[i*w2+k] = thresholdInt[(i+blobList.get(max).yMin)*(width)+k+blobList.get(max).xMin];
			}
		}
		width = w2;
		height = h2;
		saveImage(grayscaleInt(cut),width,height,"04_cut.png");
		saveImage(grayscaleInt(imgcut),width,height,"04_imgcut.png");
		
		//flood fill
		//i 9 punti di partenza sono i centri delle sottogriglie
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 3; k++) {		
				//i centri sono 1/6 3/6 e 5/6
				//floodFill(cut,width,height,(2*i+1)*(height/6),(2*k+1)*(width/6));
			}
		}
		saveImage(grayscaleInt(cut),width,height,"05_fill.png");
		
		//IN CASO DI PROBLEMI: SFOCARE ULTERIORMENTE L'IMMAGINE E THRESHOLDARLA A VALORE COSTANTE (ci sta il metodo apposta)
		//i problemi sarebbero che la griglia è bucata da un lato e con la ciorta che tengo
		//sampla le linee proprio dentro al cazzo di buco
		
		//corner detection
		//adesso ho un quadrato bianco che maschera il sudoku
		//calcolo le equazioni dei quattro lati e le le loro intersezioni che saranno la posizione degli angoli
		//SI PUO FARE ANCHE SENZA FLOODFILL!!
		//se c'è un intersezione con coordinate negative e dovesse dare problemi, cropparla a 0 don't worry
		//stessa cosa se è > w o > h
		int[] corners = extractCorners(cut,width,height,4);
		for (int c = 0; c < corners.length; c+=2)
			System.out.println(corners[c]+" "+corners[c+1]);
		
		//mapping
		int dim = 32*9; //ogni cella è 32 pixel; -> il numero è al massimo 32 pixel
		byte[] processed = mapping(imgcut,width,height,corners,dim);
		width = dim;
		height = dim;
		byte[] saveprocessed = new byte[3*dim*dim];
		for (int i = 0; i < 3*dim*dim; i++) saveprocessed[i] = processed[i/3];
		saveImage(saveprocessed,width,height,"99_processed.png");
		
		BlobFinder cb = new BlobFinder(width, height);
		blobList = new ArrayList<BlobFinder.Blob>();
		cb.detectBlobs2(processed,null, 0, -1, (byte)0, blobList);

		ArrayList<BlobFinder.Blob> digitList = new ArrayList<BlobFinder.Blob>();
		for (int i = 0; i < blobList.size(); i++) {
			if ( (blobList.get(i).xMax-blobList.get(i).xMin) < 24 
					&& (blobList.get(i).yMax-blobList.get(i).yMin) < 24
					&& (blobList.get(i).yMax-blobList.get(i).yMin) > 12
					//centro non sul bordo
					&& ((blobList.get(i).xMax+blobList.get(i).xMin)/2)%32 > 8
					&& ((blobList.get(i).yMax+blobList.get(i).yMin)/2)%32 > 8
					&& ((blobList.get(i).xMax+blobList.get(i).xMin)/2)%32 < 24
					&& ((blobList.get(i).yMax+blobList.get(i).yMin)/2)%32 < 24) {
				digitList.add(blobList.get(i));
			 }
		}
		
		int digitDim = 28;		
		int hiddenConv[][] = {{1,28,8,5,2},{8,12,16,5,2},{16,4,64,3,2}};
		int hiddenFull[] = {9};
		CNN cnnllol = new CNN(hiddenConv,hiddenFull);
		CNN cnn = new CNN(cnnllol);
		cnn.load("machine_99.548294");
		byte[][] sudoku = new byte[9][9];
		for (int i = 0; i < digitList.size(); i++) {
			System.out.println(digitList.get(i).toString());
			
			int cx = (digitList.get(i).xMax+digitList.get(i).xMin)/2;
			int cy = (digitList.get(i).yMax+digitList.get(i).yMin)/2;

			byte[] img = new byte[3*digitDim*digitDim];
			float[] cnnInput = new float[digitDim*digitDim];
			int sx = Math.max(0,Math.min(cx-digitDim/2,width-digitDim));
			int sy = Math.max(0,Math.min(cy-digitDim/2,height-digitDim));
			for (int k = 0; k < digitDim; k++) {
				for (int j = 0; j < digitDim; j++) {
					cnnInput[(k*digitDim+j)] = 1.0f-(processed[(sy+k)*width+(sx+j)]&0xFF)/255.0f;
					img[3*(k*digitDim+j)] = (byte)(255-processed[(sy+k)*width+(sx+j)]&0xFF);
					img[3*(k*digitDim+j)+1] = (byte)(255-processed[(sy+k)*width+(sx+j)]&0xFF);
					img[3*(k*digitDim+j)+2] = (byte)(255-processed[(sy+k)*width+(sx+j)]&0xFF);
				}
			}
			sudoku[(cy-1)/32][(cx-1)/32] = (byte)(cnn.predict(cnnInput)+1);
			saveImage(img,digitDim,digitDim,"digits\\digit"+i+"_"+(cx-1)/32+"x"+(cy-1)/32+".png");
		}
		
		return sudoku;
	}
	
	private static int[] boxBlur(int[] image, int w, int h, int blurRange) {
		int[] blur = new int[w*h];
		int[] integ = integralImage(image,w,h);
		for (int i = 0; i < h; i++) {
			int ny = Math.min(blurRange,h-i);
			for (int k = 0; k < w; k++) {
				int nx = Math.min(blurRange,w-k);
				int A = integ[i*(w+1)+k];
				int B = integ[i*(w+1)+k+nx];
				int C = integ[(i+ny)*(w+1)+k];
				int D = integ[(i+ny)*(w+1)+k+nx];
				blur[i*w+k] = (D+A-B-C)/(nx*ny);
			}
		}
		
		return blur;
	}
	 
	private static int[] integralImage(int[] image, int w, int h) {
		int[] integ = new int[(w+1)*(h+1)];
		for (int i = 0; i < h; i++) {
			for (int k = 0; k < w; k++) {
				int val = image[i*w+k];

				int A = integ[i*(w+1)+k];
				int B = integ[i*(w+1)+k+1];
				int C = integ[(i+1)*(w+1)+k];
				int D = val;
				
				integ[(i+1)*(w+1)+k+1] = B+C-A+D;
			}
		}
		return integ;
	}
	
	private static int[] gaussianBlur(int[] grayScale, int width, int height, int blurRange) {
		int[][] blurKernel = new int[][]{
			{1,4,7,4,1},
			{4,16,26,16,4},
			{7,26,41,26,7},
			{4,16,26,16,4},
			{1,4,7,4,1}
		};
		int[] blur = convolve(grayScale,blurKernel,width,height,5);
		width -= 4;
		height -= 4;
		for (int i = 0; i < blurRange; i++) {
			blur = convolve(blur,blurKernel,width,height,5);
			width -= 4;
			height -= 4;
		}
		return blur;
	}
	
	private static byte[] adaptiveThreshold(int[] blur, int width, int height, int thresholdSize, int thresholdGain) {
		byte[] threshold = new byte[width*height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int sum = 0;
				for (int ik = 0; ik < thresholdSize; ik++) {
					for (int ih = 0; ih < thresholdSize; ih++) {
						sum += blur[(i+ik)*(width+thresholdSize-1)+ih+j];
					}
				}
				sum /= thresholdSize*thresholdSize;
				if (blur[(i+thresholdSize/2)*(width+thresholdSize-1)+(j+thresholdSize/2)] > sum+thresholdGain)
					threshold[i*width+j] = (byte)255;
				else threshold[i*width+j] = 0;
			}
		}
		return threshold;
	}

	private static int[] imageThreshold(int[] blur, int width, int height, int thresholdValue) {
		int[] threshold = new int[width*height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (blur[i*width+j] > thresholdValue) threshold[i*width+j] = 255;
				else threshold[i*width+j] = 0;
			}
		}
		return threshold;
	}
	
	public static byte[] mapping(int[] original, int width, int height, int[] corners, int dim) {
		//implementa il seguente sistema
		/*
		xx1=(a*x1+b*y1+c)/(g*x1+h*y1+1)
		yy1=(d*x1+e*y1+f)/(g*x1+h*y1+1)
		xx2=(a*x2+b*y2+c)/(g*x2+h*y2+1)
		yy2=(d*x2+e*y2+f)/(g*x2+h*y2+1)
		xx3=(a*x3+b*y3+c)/(g*x3+h*y3+1)
		yy3=(d*x3+e*y3+f)/(g*x3+h*y3+1)
		xx4=(a*x4+b*y4+c)/(g*x4+h*y4+1)
		yy4=(d*x4+e*y4+f)/(g*x4+h*y4+1)
		
		in matlab:
		syms x1 y1 x2 y2 x3 y3 x4 y4 xx1 xx2 xx3 xx4 yy1 yy2 yy3 yy4
		A= [ x1, y1, 1,  0,  0, 0, -x1*xx1, -xx1*y1;
		  0,  0, 0, x1, y1, 1, -x1*yy1, -y1*yy1;
		 x2, y2, 1,  0,  0, 0, -x2*xx2, -xx2*y2;
		  0,  0, 0, x2, y2, 1, -x2*yy2, -y2*yy2;
		 x3, y3, 1,  0,  0, 0, -x3*xx3, -xx3*y3;
		  0,  0, 0, x3, y3, 1, -x3*yy3, -y3*yy3;
		 x4, y4, 1,  0,  0, 0, -x4*xx4, -xx4*y4;
		  0,  0, 0, x4, y4, 1, -x4*yy4, -y4*yy4]
		B = [xx1; yy1; xx2; yy2; xx3; yy3; xx4; yy4]
		C = linsolve(A,B);
		in genere assegno i punti del quadrato raddrizzato a x,y 
		gli altri punti li lascio come parametri in modo che mi calcolo per ogni punto del quadrato riaddrizzato
		a quale punto corrisponde su quello originale
		
		x1 = 0; y1 = 0; x2 = 0; y2 = 1; x3 = 1; y3 = 1; x4 = 1; y4 = 0;
		
		ottengo:*/
		
		int xx1 = corners[6];
		int yy1 = corners[7];
		int xx2 = corners[2];
		int yy2 = corners[3];
		int xx3 = corners[0];
		int yy3 = corners[1];
		int xx4 = corners[4];
		int yy4 = corners[5];
		
		float a = -(xx1*xx2*yy3 - xx1*xx3*yy2 - xx1*xx2*yy4 + xx2*xx4*yy1 + xx1*xx3*yy4 - xx3*xx4*yy1 - xx2*xx4*yy3 + xx3*xx4*yy2)/(float)(xx2*yy3 - xx3*yy2 - xx2*yy4 + xx4*yy2 + xx3*yy4 - xx4*yy3);
  		float b = (xx1*xx3*yy2 - xx2*xx3*yy1 - xx1*xx4*yy2 + xx2*xx4*yy1 - xx1*xx3*yy4 + xx1*xx4*yy3 + xx2*xx3*yy4 - xx2*xx4*yy3)/(float)(xx2*yy3 - xx3*yy2 - xx2*yy4 + xx4*yy2 + xx3*yy4 - xx4*yy3);
        float c = xx1;
		float d = -(xx2*yy1*yy3 - xx3*yy1*yy2 - xx1*yy2*yy4 + xx4*yy1*yy2 + xx1*yy3*yy4 - xx4*yy1*yy3 - xx2*yy3*yy4 + xx3*yy2*yy4)/(float)(xx2*yy3 - xx3*yy2 - xx2*yy4 + xx4*yy2 + xx3*yy4 - xx4*yy3);
		float e = (xx1*yy2*yy3 - xx2*yy1*yy3 - xx1*yy2*yy4 + xx2*yy1*yy4 - xx3*yy1*yy4 + xx4*yy1*yy3 + xx3*yy2*yy4 - xx4*yy2*yy3)/(float)(xx2*yy3 - xx3*yy2 - xx2*yy4 + xx4*yy2 + xx3*yy4 - xx4*yy3);
        float f = yy1;
        float g = (xx1*yy2 - xx2*yy1 - xx1*yy3 + xx3*yy1 + xx2*yy4 - xx4*yy2 - xx3*yy4 + xx4*yy3)/(float)(xx2*yy3 - xx3*yy2 - xx2*yy4 + xx4*yy2 + xx3*yy4 - xx4*yy3);
        float h = (xx1*yy3 - xx3*yy1 - xx1*yy4 - xx2*yy3 + xx3*yy2 + xx4*yy1 + xx2*yy4 - xx4*yy2)/(float)(xx2*yy3 - xx3*yy2 - xx2*yy4 + xx4*yy2 + xx3*yy4 - xx4*yy3);

        //SONO UN GRANDISSIMA TESTA DI CAZZO
        //TENGO TUTTI I PARAMETRI, MI BASTA FARE UNA CAZZO DI MOLTIPLICAZIONE MATRICIALE PER OTTENERE
        //QUEL CAZZO DI SISTEMA MERDA
        
        byte[] mapped = new byte[dim*dim];
        for (int i = 0; i < dim; i++) {
        	for (int k = 0; k < dim; k++) {
        		float u = ((float)k)/(dim-1);
        		float v = ((float)(dim-i-1))/(dim-1);
                int x = (int)((a*u+b*v+c)/(g*u+h*v+1));
                int y = (int)((d*u+e*v+f)/(g*u+h*v+1));
                //System.out.println(width+"x"+height+" "+x+","+y);
        		mapped[i*dim+k] = (byte)original[(height-1-y)*width+x];
        	}
        }
        return mapped;
	}
	
	//più è alta la distanza più ci si avvicina al bordo dell'immagine
	public static int[] extractCorners(int[] square, int w, int h, int distance) {
		//TOP EDGE
		int x1 = w/distance;
		int x2 = (distance-1)*w/distance;
		int y1 = 0;
		int y2 = 0;
		while(square[y1*w+x1] == 0) y1++;
		while(square[y2*w+x2] == 0) y2++;
		y1 = h-1-y1;
		y2 = h-1-y2;
		float topM = ((float)(y2-y1))/(x2-x1);
		float topQ = -x1*topM+y1;
		//BOTTOM EDGE
		y1 = h-1;
		y2 = h-1;
		while(square[y1*w+x1] == 0) y1--;
		while(square[y2*w+x2] == 0) y2--;
		y1 = h-1-y1;
		y2 = h-1-y2;
		float bottomM = ((float)(y2-y1))/(x2-x1);
		float bottomQ = -x1*bottomM+y1;
		//LEFT EDGE
		y1 = h/distance;
		y2 = (distance-1)*h/distance;
		x1 = 0;
		x2 = 0;
		while(square[y1*w+x2] == 0) x2++;
		while(square[y2*w+x1] == 0) x1++;
		float leftM = ((float)(y2-y1))/(x2-x1+1);
		float leftQ = -x1*leftM+y1;
		//RIGHT EDGE
		x1 = w-1;
		x2 = w-1;
		while(square[y1*w+x2] == 0) x2--;
		while(square[y2*w+x1] == 0) x1--;
		float rightM = ((float)(y2-y1))/(x2-x1+1);
		float rightQ = -x1*rightM+y1;

		//intersezioni
		int xTopRight = (int)((topQ-rightQ)/(rightM-topM)); 
		int yTopRight = (int)(xTopRight*topM+topQ);

		int xTopLeft = (int)((topQ-leftQ)/(leftM-topM)); 
		int yTopLeft = (int)(xTopLeft*topM+topQ);

		int xBottomRight = (int)((bottomQ-rightQ)/(rightM-bottomM)); 
		int yBottomRight = (int)(xBottomRight*bottomM+bottomQ);

		int xBottomLeft = (int)((bottomQ-leftQ)/(leftM-bottomM)); 
		int yBottomLeft = (int)(xBottomLeft*bottomM+bottomQ);
		
		int[] result = new int[]{xTopRight,yTopRight,xTopLeft,yTopLeft,xBottomRight,yBottomRight,xBottomLeft,yBottomLeft};
		for (int i = 0; i < result.length; i+=2) {
			if (result[i] < 0) result[i] = 0;
			if (result[i+1] < 0) result[i+1] = 0;
			if (result[i] >= w-1) result[i] = w-1;
			if (result[i+1] >= h-1) result[i+1] = h-1;
		}
		return result;
	}
	
	public static int[] findBlob(int[] start, int w, int h, ArrayList<BlobFinder.Blob> blobList) {
		for (int i = 0; i < h; i++) {
			for (int k = 0; k < w; k++) {
				int xMin = k;
				int xMax = k;
				int yMin = i;
				int yMax = i;
				int mass = 1;
				int xp = 0;
				int yp = 0;
				boolean add = false;
				ArrayList<int[]> toCheck = new ArrayList<int[]>();
				if (start[i*w+k] == 0) {
					toCheck.add(new int[]{k,i});
					xp = k;
					yp = i;
					start[i*w+k] = 128;
					add = true;
					//System.out.println(i+","+k);
				}
				while (!toCheck.isEmpty()) {
					int[] point = toCheck.get(0);
					toCheck.remove(0);
					int x = point[0];
					int y = point[1];
					if (y < 0 || y >= h) continue;
					if (x < 0 || x >= w) continue;
					//left
					if (x-1 >= 0) {
						if (start[y*w+x-1] == 0) {
							start[y*w+x-1] = 128;
							toCheck.add(new int[]{x-1,y});
							xMin = Math.min(x-1,xMin);
							mass++;
						}
					}			
					//right
					if (x+1 < w) {
						//System.out.println(w+"x"+h+" "+x+","+y);
						if (start[y*w+x+1] == 0) {
							start[y*w+x+1] = 128;
							toCheck.add(new int[]{x+1,y});
							xMax = Math.max(x+1,xMax);
							mass++;
						}
					}		
					//top
					if (y-1 >= 0) {
						if (start[(y-1)*w+x] == 0) {
							start[(y-1)*w+x] = 128;
							toCheck.add(new int[]{x,y-1});
							yMin = Math.min(y-1,yMin);
							mass++;
						}
					}	
					//bottom
					if (y+1 < h) {
						if (start[(y+1)*w+x] == 0) {
							start[(y+1)*w+x] = 128;
							toCheck.add(new int[]{x,y+1});
							yMax =  Math.max(y+1,yMax);
							mass++;
						}
					}
				}
				if (add && mass > 32) blobList.add(new BlobFinder.Blob(xMin,xMax,yMin,yMax,mass,xp,yp));
			}
		}
		
		int[] blobMax = new int[w*h];
		int max = 0;
		for (int i = 0; i < blobList.size(); i++) {
			System.out.println(blobList.get(i).toString());
			if (blobList.get(i).mass > blobList.get(max).mass) max = i;
		}
		
		if (blobList.size() > 0) {
			System.out.println("extracting blob max");
			ArrayList<int[]> toCheck = new ArrayList<int[]>();
			int i = blobList.get(max).yp;
			int k = blobList.get(max).xp;
			if (start[i*w+k] == 128) {
				toCheck.add(new int[]{k,i});
				start[i*w+k] = 37;
				blobMax[i*w+k] = 255;
			}
			while (!toCheck.isEmpty()) {
				int[] point = toCheck.get(0);
				toCheck.remove(0);
				int x = point[0];
				int y = point[1];
				if (y < 0 || y >= h) continue;
				if (x < 0 || x >= w) continue;
				//left
				if (x-1 >= 0) {
					if (start[y*w+x-1] == 128) {
						start[y*w+x-1] = 37;
						blobMax[y*w+x-1] = 255;
						toCheck.add(new int[]{x-1,y});
					}
				}			
				//right
				if (x+1 < w) {
					//System.out.println(w+"x"+h+" "+x+","+y);
					if (start[y*w+x+1] == 128) {
						start[y*w+x+1] = 37;
						blobMax[y*w+x+1] = 255;
						toCheck.add(new int[]{x+1,y});
					}
				}		
				//top
				if (y-1 >= 0) {
					if (start[(y-1)*w+x] == 128) {
						start[(y-1)*w+x] = 37;
						blobMax[(y-1)*w+x] = 255;
						toCheck.add(new int[]{x,y-1});
					}
				}	
				//bottom
				if (y+1 < h) {
					if (start[(y+1)*w+x] == 128) {
						start[(y+1)*w+x] = 37;
						blobMax[(y+1)*w+x] = 255;
						toCheck.add(new int[]{x,y+1});
					}
				}
			}
		}
		return blobMax;
	}
	
	public static void floodFill(int[] start, int w, int h, int x, int y) {
		ArrayList<int[]> toCheck = new ArrayList<int[]>();
		toCheck.add(new int[]{x,y});
		start[y*w+x] = 255;
		while (!toCheck.isEmpty()) {
			int[] point = toCheck.get(0);
			toCheck.remove(0);
			x = point[0];
			y = point[1];
			//left
			if (x-1 >= 0) {
				if (start[y*w+x-1] == 0) {
					start[y*w+x-1] = 255;
					toCheck.add(new int[]{x-1,y});
				}
			}			
			//right
			if (x+1 < w) {
				if (start[y*w+x+1] == 0) {
					start[y*w+x+1] = 255;
					toCheck.add(new int[]{x+1,y});
				}
			}		
			//top
			if (y-1 >= 0) {
				if (start[(y-1)*w+x] == 0) {
					start[(y-1)*w+x] = 255;
					toCheck.add(new int[]{x,y-1});
				}
			}	
			//bottom
			if (y+1 < h) {
				if (start[(y+1)*w+x] == 0) {
					start[(y+1)*w+x] = 255;
					toCheck.add(new int[]{x,y+1});
				}
			}
		}
	}
	
	public static void saveImage(byte[] image, int w, int h, String fileName) {
		BufferedImage patch = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
	    byte[] patchB = ((DataBufferByte) patch.getRaster().getDataBuffer()).getData();
	    for (int i = 0; i < image.length; i++) patchB[i] = image[i];
	    
		try {
			ImageIO.write(patch, "png", new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static int[] convolve(int[] image, int[][] kernel, int w, int h, int k) {
		int[] convolution = new int[(w-k+1)*(h-k+1)];
		
		for (int i = 0; i < h-k+1; i++) {
			for (int j = 0; j < w-k+1; j++) {
				int sum = 0;
				for (int ik = 0; ik < k; ik++) {
					for (int ih = 0; ih < k; ih++) {
						sum += image[(i+ik)*w+j+ih]*kernel[ik][ih];
					}
				}
				sum /= 273;
				convolution[i*(w-k+1)+j] = sum;
			}
		}
		
		return convolution;
	}
	
	public static void main (String args[]) {
		try {
			BufferedImage image = ImageIO.read(new File("photo.jpg"));
		    byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		    long start = System.currentTimeMillis();
		    byte sudoku[][] = preprocess(data,image.getWidth(),image.getHeight());
		    for (int i = 0; i < 9; i++) {
		    	for (int k = 0; k < 9; k++) {
		    		System.out.print(sudoku[i][k]+" ");
		    	}
		    	System.out.println("");
		    }
		    
		    System.out.println(System.currentTimeMillis()-start);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Digit {
	byte[] img;
	int i;
	int k;
	int digit;
	int digitDim;
	
	public Digit(byte[] im, int ii, int kk) {
		img = im;
		i = ii;
		kk = k;
		digit = -1;
		digitDim = (int) Math.sqrt(im.length);
	}
}
