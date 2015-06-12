package org.acl.deepspark.nn.conf;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.acl.deepspark.nn.layers.FullyConnLayer;
import org.acl.deepspark.nn.layers.cnn.ConvolutionLayer;
import org.acl.deepspark.nn.layers.cnn.PoolingLayer;
import org.acl.deepspark.utils.MnistLoader;
import org.jblas.DoubleMatrix;


public class NeuralNetConfigurationTest {
	
	public static final int nTest = 1000;
	public static final int minibatch = 1000;
	
	public static void main(String[] args) {
		System.out.println("Data Loading...");
		MnistLoader loader = new MnistLoader();
		loader.load("C:/Users/Hanjoo Kim/Downloads/mnist_train.txt",true);
		DoubleMatrix[] train_data = loader.getData();
		DoubleMatrix[] train_label = loader.getLabel();
		
		loader.load("C:/Users/Hanjoo Kim/Downloads/mnist_test.txt",true);
		DoubleMatrix[] test_data = loader.getData();
		DoubleMatrix[] test_label = loader.getLabel();
		
		System.out.println("Shuffling...");
		Collections.shuffle(Arrays.asList(train_data));
		Collections.shuffle(Arrays.asList(test_data));
		
		// configure network
		NeuralNetConfiguration net = new NeuralNetConfiguration(0.1, 3, 1000,true);
		net.addLayer(new ConvolutionLayer(9, 9, 20)); // conv with 20 filters (9x9)
		net.addLayer(new PoolingLayer(2)); // max pool
		net.addLayer(new FullyConnLayer(200)); // hidden
		net.addLayer(new FullyConnLayer(10)); // output
		
		System.out.println("Start Learning...");
		Date startTime = new Date();
		net.training(train_data, train_label);
		DoubleMatrix[] matrix = new DoubleMatrix[1];
		
		System.out.println(String.format("Testing... with %d samples...", nTest));
		int count = 0;
		for(int j = 0 ; j < nTest; j++) {
			matrix[0] = test_data[j];
			if(test_label[j].argmax() == net.getOutput(matrix)[0].argmax())
				count++;
		}
		
		System.out.println(String.format("Accuracy: %f %%", (double) count / nTest * 100));
		Date endTime = new Date();
		
		long time = endTime.getTime() - startTime.getTime();
		
		System.out.println(String.format("Training time: %f secs", (double) time / 1000));
	} 
}
