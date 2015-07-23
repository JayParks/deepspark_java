package org.acl.deepspark.nn.driver;

import org.acl.deepspark.data.Accumulator;
import org.acl.deepspark.data.Sample;
import org.jblas.util.Random;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by Jaehong on 2015-07-16.
 */
public class NeuralNetRunner {
    private NeuralNet net;
    private Accumulator weightAccum;

    private int iteration;
    private int batchSize;

    public NeuralNetRunner(NeuralNet net) {
        this.net = net;
        this.weightAccum = new Accumulator(net.getNumLayers());

        /* default configuration */
        this.iteration = 10000;
        this.batchSize = 1;
    }

    public NeuralNetRunner setIterations(int iteration) {
        this.iteration = iteration;
        return this;
    }

    public NeuralNetRunner setMiniBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public void train(Sample[] data) throws Exception {
        int dataSize = data.length;
        for (int i = 0 ; i < iteration; i++) {
            for (int j = 0; j < batchSize; j++)
                weightAccum.accumulate(net.train(data[Random.nextInt(dataSize)]));
            net.updateWeight(weightAccum.getAverage());
            weightAccum.clear();
        }
    }

    public INDArray[] predict(Sample[] data) {
        INDArray[] output = new INDArray[data.length];
        for (int i = 0 ; i < data.length ; i++)
            output[i] = predict(data[i]);
        return output;
    }

    public INDArray predict(Sample data) {
        return net.predict(data);
    }
}