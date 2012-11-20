using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using NeatSim.Thrift;
using SharpNeat.Phenomes.NeuralNets;

namespace NeatSim.Core
{
    class FastCyclicNetworkAdapter
    {
        public static List<CFastCyclicNetwork> Convert(List<FastCyclicNetwork> fcns)
        {
            return fcns.Select(Convert).ToList();
        }

        public static CFastCyclicNetwork Convert(FastCyclicNetwork fcn) {
            var cfcn = new CFastCyclicNetwork();
            var numberOfNeurons = fcn._neuronActivationFnArray.Length;
            // Copy connections
            cfcn.Connections = new List<CConnection>(fcn._connectionArray.Length);
            foreach (var c in fcn._connectionArray)
            {
                var cc = new CConnection
                             {
                                 ToNeuronId = c._tgtNeuronIdx,
                                 FromNeuronId = c._srcNeuronIdx,
                                 Weight = c._weight
                             };
                cfcn.Connections.Add(cc);
            }

            // Copy activation functions
            cfcn.ActivationFunctions = new List<string>(numberOfNeurons);
            foreach (var s in fcn._neuronActivationFnArray)
            {
                if (s == null)
                    cfcn.ActivationFunctions.Add("");
                else
                    cfcn.ActivationFunctions.Add(s.FunctionId);
            }

            // Copy auxiliary arguments
            cfcn.NeuronAuxArgs = new List<List<double>>(numberOfNeurons);
            foreach (var aux in fcn._neuronAuxArgsArray)
            {
                if (aux == null)
                    cfcn.NeuronAuxArgs.Add(new List<double>());
                else
                    cfcn.NeuronAuxArgs.Add(new List<double>(aux));
            }
            cfcn.NeuronCount = fcn._neuronCount;
            cfcn.InputNeuronCount = fcn._inputNeuronCount;
            cfcn.OutputNeuronCount = fcn._outputNeuronCount;
            cfcn.TimestepsPerActivation = fcn._timestepsPerActivation;

            return cfcn;
        }
    }
}
