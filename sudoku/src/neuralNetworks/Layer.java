package neuralNetworks;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class Layer {
	public int hiddenNum;
	public int inputNum;

	public float[] hidden;
	public float[] net;
	public float[][] weight;
	public float[][] deltaWeight;
	public float[] bias;
	public float[] deltaBias;
	public float[] input;

	abstract void feed(float _input[]);
	abstract float[] update(float[] _error, float[] _input, float _learningRate, float _momentum);

	public abstract void load(String _loadFile);
	abstract void save(String _saveFile);
	
	abstract BufferedImage visualize(int dimX, int dimY, float zoom);

	abstract public void showFilters(String _file);
	
	public static Layer builder(String _type, int _inputNum, int _hiddenNum) {
		Layer layer = null;
		_type.toUpperCase();
		switch(_type) {
			case "SOFTMAX":
				layer = new LayerSoftmax(_inputNum, _hiddenNum);
				break;
			default:
				break;
		}
		
		return layer;
	}
	public void updateTrue(float _learningRate, float _momentum) {}
}
