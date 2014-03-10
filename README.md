# NeatSim

This is the README file accompanying the code used in the following paper.

>	Neuroevolution of a multi-agent system for the dynamic pickup and delivery problem
>	by Jonathan Merlevede, Rinde R.S. van Lon, and Tom Holvoet
>	from iMinds-Distrinet, KU Leuven, 3001 Leuven, Belgium

In the remainder of this README file we assume that the reader has read this paper.

Before reading on, you should know that this code was written in the context of this paper. It was never really meant to be used by anyone but the authors while coding: documentation is scarce, the code not always elegant. Nevertheless, we believe in open science and would often have preferred having ugly source code over none, so here it is.


## Overview
As described in the paper, our software consists of two parts.

* The first part is the C# part, based on [SharpNeatV2](http://sourceforge.net/projects/sharpneat/files/sharpneat2/), which implements the [NEAT](http://www.cs.ucf.edu/~kstanley/neat.html) algorithm.
* The second part is written in Java and is called upon by NEAT to determine the fitness of individuals during evolution. Determining fitness involves carrying out a simulation in [RinSim](https://github.com/rinde/RinSim)).

The Java part can also be used for evaluating and visualising heuristics that have already been evolved.

## Quickstart
Anyone who runs a 64-bit Windows system should be able to evaluate or evolve their own heuristic as follows. Running the SharpNeat part of the application on Windows is advised, but it should also be possible to run SharpNeat using Mono.

### Evolution
In order to evolve a new heuristic, the two parts of our software have to be up and running.

* Start the NeatSim server - the main class is called `Main`. Make sure that NeatSim is configured properly - you will also have to download the required dependencies using [Maven](http://maven.apache.org/) (see `neatsim.properties` and the description of NeatSim below).
* Start the SharpNEATv2 GUI (`SharpNeatV2\bin\SharpNeatGUI.exe`). Select the 'NeatSim - Simulation' experiment, the desired evolutionary properties and then press the start button.

In order to do any serious evolution, you will probably want to use distributed evaluation using JPPF. See the 'NeatSim' section below for (a little) more information.

### Evaluation
Existing genomes (as stored in XML files by SharpNEAT) can be read, evaluated and visualized by the NeatSim code (which uses RinSim). For an example of reading an XML file, take a look at the test class `TestNeuralNetworkReader`. For an example of how to start a simulation, take a look at the test class  `TestDeterminism`.

# Code
The code is organized in three folders: NeatSim, SharpNeatV2 and Thrift.

## NeatSim
This folder contains the Java portion of the code.

* 
* For some reason using local, multithreaded simulation is not deterministic. This is a known problem; the local multithreaded implementation currently fails determinism tests. Local multithreaded simulation should not be used for research purposes before addressing this issue (probably in `LocalMultithreadesSimulator`).

## SharpNeatV2
This folder contains the modified version of [SharpNEAT v2] that I used for evolving the vehicle heuristics. I basically simply added code for an evaluator that sends the genomes of an entire generation to Java for evaluation (using Thrift), but had to change some parts of SharpNeat that might make it hard to easily upgrade to newer versions of SharpNEAT (e.g. the possibility to evaluate an entire generation instead of just a single genome, but also changing the visibility of some properties of the neural networks in order to read them into the Thrift data transfer objects). I also added my experiment to the SharpNEATV2 configuration file `SharpNeatDomains.experiments.xml`. Most of my code is in the SharpNeat\NeatSim application.

To any researcher who considers building on my work, I would advise to start with a fresh pull of SharpNEAT and maybe look at the code here for reference. Everyone who simply wants to run my application should be able to do so. The hostname and the port of the Java Thrift 'evaluation server' are currently hardcoded to `localhost` and `7913` in the `ProtocolManager`.

## Thrift
This folder contains the [Thrift](http://thrift.apache.org/) service file that I used to generate the Java and C# interface and code for interprocess communication. It also includes the generated code and the (Windows) binary Thrift file that I used for generation. Using a newer version of Thrift or Thrift for Mac from the Thrift website should not be a problem.


-- Jonathan Merlevede




