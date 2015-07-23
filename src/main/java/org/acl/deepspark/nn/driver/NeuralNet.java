package org.acl.deepspark.nn.driver;

import org.acl.deepspark.data.Sample;
import org.acl.deepspark.data.Weight;
import org.acl.deepspark.nn.conf.LayerConf;
import org.acl.deepspark.nn.conf.NeuralNetConf;
import org.acl.deepspark.nn.functions.ActivatorType;
import org.acl.deepspark.nn.layers.ConvolutionLayer;
import org.acl.deepspark.nn.layers.FullyConnectedLayer;
import org.acl.deepspark.nn.layers.Layer;
import org.acl.deepspark.nn.layers.PoolingLayer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.ArrayList;

/**
 * Created by Jaehong on 2015-07-16.
 */
public class NeuralNet {
    private Layer[]     layers;
    private Weight[]    weights;
    private Weight[]    weightUpdates;

    double learningRate;
    double decayLambda;
    double momentum;
    double dropOutRate;

    public NeuralNet(final NeuralNetConf conf) {
        learningRate = conf.getLearningRate();
        decayLambda = conf.getDecayLambda();
        momentum = conf.getMomentum();
        dropOutRate = conf.getDropOutRate();
        initNetwork(conf);
    }

    public void initNetwork(final NeuralNetConf conf) {
        int size = conf.getLayerList().size();
        layers = new Layer[size];
        weights = new Weight[size];
        weightUpdates = new Weight[size];
        buildNetwork(conf.getLayerList(), conf.getDimIn());
    }

    private void buildNetwork(ArrayList<LayerConf> arr, int[] dimIn) {
        for (int i = 0 ; i< arr.size(); i++) {
            LayerConf layerConf = arr.get(i);
            ActivatorType activator = (ActivatorType) layerConf.get("activator");

            switch (layerConf.getType()) {
                case CONVOLUTION:
                    layers[i] = new ConvolutionLayer(dimIn, activator);
                    break;
                case POOLING:
                    layers[i] = new PoolingLayer(dimIn, activator);
                    break;
                case FULLYCONN:
                    layers[i] = new FullyConnectedLayer(dimIn, activator);
                    break;
            }
            weights[i] = layers[i].createWeight(layerConf, dimIn);
            dimIn = layers[i].calculateOutputDimension(layerConf, dimIn);
            weightUpdates[i] = new Weight(weights[i].getShape(), dimIn);
        }
    }

    public void setWeights(Weight[] weights) {
        this.weights = weights;
    }

    public Weight[] getWeights() {
        return weights;
    }

    public int getNumLayers() {
        return layers.length;
    }

    public Weight[] train(Sample in) {
        Weight[] gradient = new Weight[layers.length];
        INDArray[] output = new INDArray[layers.length];
        INDArray[] input = new INDArray[layers.length + 1];
        input[0] = in.data;

        for (int i = 0; i < layers.length; i++) {
            output[i] = layers[i].generateOutput(weights[i], input[i]);
            input[i+1] = layers[i].activate(output[i]);
        }

        INDArray delta = input[layers.length].sub(in.label);
        for (int i = layers.length-1; i >= 0; i--) {
            delta = layers[i].deriveDelta(delta, output[i]);
            gradient[i] = layers[i].gradient(input[i], delta);
            if (i > 0)
                delta = layers[i].calculateBackprop(weights[i], delta);
        }
        return gradient;
    }

    public INDArray predict(Sample in) {
        INDArray activatedOut = in.data;
        for (int i = 0; i < layers.length; i++) {
            INDArray output = layers[i].generateOutput(weights[i], activatedOut);
            activatedOut = layers[i].activate(output);
        }
        return activatedOut;
    }

    public void updateWeight(Weight[] deltaWeight) throws Exception {
        if (weights.length != deltaWeight.length)
            throw new Exception("Weight dimension mismatch");
        for (int i = 0 ; i < weights.length; i++) {
            // TODO: PoolingLayer weight is null
            weightUpdates[i].muli(momentum)
                            .subi(weights[i].mul(learningRate*decayLambda))
                            .subi(deltaWeight[i].mul(learningRate));
            weights[i].addi(weightUpdates[i]);
        }
    }
}