using System;
using System.Collections.Generic;
using System.Diagnostics;
using NeatSim.Thrift;
using SharpNeat.Core;
using SharpNeat.Phenomes;
using SharpNeat.Phenomes.NeuralNets;
using Thrift.Protocol;
using Thrift.Transport;

namespace NeatSim
{
    class RemoteXorEvaluator : IPhenomeEvaluator<IBlackBox>
    {
        private static readonly CFitnessCalculatorService.Client Client;

        static RemoteXorEvaluator()
        {
            Client = new CFitnessCalculatorService.Client(ProtocolManager.Protocol);
        }

        public RemoteXorEvaluator()
        {
            Debug.WriteLine("Created new NeatsimPhenomeEvaluator");

        }

        public ulong EvaluationCount
        {
            get;
            private set;
        }

        public bool StopConditionSatisfied
        {
            get;
            private set;
        }
        public FitnessInfo Evaluate(IBlackBox phenome)
        {
            if (!(phenome is FastCyclicNetwork))
            {
                throw new Exception("moo");
            }
            return Evaluate((FastCyclicNetwork) phenome);
        }

        public FitnessInfo Evaluate(FastCyclicNetwork phenome)
        {
            EvaluationCount++;

            
            var n = new CFastCyclicNetwork();
            var numberOfNeurons = phenome._neuronActivationFnArray.Length;

            // Copy connections
            n.Connections = new List<CConnection>(phenome._connectionArray.Length);
            foreach (var c in phenome._connectionArray)
            {
                var cc = new CConnection {
                    ToNeuronId = c._tgtNeuronIdx,
                    FromNeuronId = c._srcNeuronIdx,
                    Weight = c._weight
                };
                n.Connections.Add(cc);
            }

            // Copy activation functions
            n.ActivationFunctions = new List<string>(numberOfNeurons);
            foreach (var s in phenome._neuronActivationFnArray)
            {
                if (s == null)
                    n.ActivationFunctions.Add("");
                else
                    n.ActivationFunctions.Add(s.FunctionId);
            }

            // Copy auxiliary arguments
            n.NeuronAuxArgs = new List<List<double>>(numberOfNeurons);
            foreach (var aux in phenome._neuronAuxArgsArray)
            {
                if (aux == null)
                    n.NeuronAuxArgs.Add(new List<double>());
                else
                    n.NeuronAuxArgs.Add(new List<double>(aux));
            }

            n.NeuronCount = phenome._neuronCount;
            n.InputNeuronCount = phenome._inputNeuronCount;
            n.OutputNeuronCount = phenome._outputNeuronCount;
            n.TimestepsPerActivation = phenome._timestepsPerActivation;

            if (!ProtocolManager.Transport.IsOpen)
                ProtocolManager.Transport.Open();
            var cFitness = Client.calculateFitness(n);
            //ProtocolManager.Transport.Close();

            var auxFitness = new AuxFitnessInfo[cFitness.AuxFitness.Count];
            for (int i = 0 ; i < auxFitness.Length ; i++)
            {
                auxFitness[i] = new AuxFitnessInfo(cFitness.AuxFitness[i].Name,cFitness.AuxFitness[i].Value);
            }
            var fitness = new FitnessInfo(cFitness.Fitness, auxFitness);

            StopConditionSatisfied = cFitness.StopConditionSatisfied;
            //nspec.evaluate()

            //NeatSimPhenomeEvaluatorClient nspec = new NeatSimPhenomeEvaluatorClient();
            //nspec.evaluate()

            // We do not use the NEAT genome decoder. The NEAT genome decoder would
            // do the same as the following code, but provide us with an IBlackBox object.
            // Because we pass on the object to our Java code, we need to be aware of its
            // underlying structure. The additional layer of abstraction gets in the way.


            return fitness;
        }

        public void Reset()
        {
            // no state!
        }
    }

}
