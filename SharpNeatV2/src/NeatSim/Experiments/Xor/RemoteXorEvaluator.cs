using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using NeatSim.Core;
using SharpNeat.Core;
using SharpNeat.Domains;
using SharpNeat.Phenomes;
using SharpNeat.Phenomes.NeuralNets;

namespace NeatSim.Experiments.Xor
{
    class RemoteXorEvaluator : IPhenomeEvaluator<IBlackBox>
    {
        private readonly XorBlackBoxEvaluator _xorBlackBoxEvaluator = new XorBlackBoxEvaluator();

        static RemoteXorEvaluator()
        {
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
                throw new Exception("NeatSim exception: can only process FastCyclicNetworks!");
            }
            return Evaluate((FastCyclicNetwork) phenome);
        }

        public List<FitnessInfo> Evaluate(List<FastCyclicNetwork> phenomes)
        {
            return phenomes.Select(Evaluate).ToList();
        }

        public FitnessInfo Evaluate(FastCyclicNetwork phenome)
        {
            EvaluationCount++;

            var fcn = FastCyclicNetworkAdapter.Convert(phenome);
            ProtocolManager.Open();
            var cFitness = ProtocolManager.Client.calculateXorPhenotypeFitness(fcn);
            //ProtocolManager.Close(); adding this line implies a huge performance hit

            var auxFitness = new AuxFitnessInfo[cFitness.AuxFitness.Count];
            for (int i = 0 ; i < auxFitness.Length ; i++)
            {
                auxFitness[i] = new AuxFitnessInfo(cFitness.AuxFitness[i].Name,cFitness.AuxFitness[i].Value);
            }
            var fitness = new FitnessInfo(cFitness.Fitness, auxFitness);

            StopConditionSatisfied = cFitness.StopConditionSatisfied;
            if (StopConditionSatisfied)
            {
                FitnessInfo fi = _xorBlackBoxEvaluator.Evaluate(phenome);
                bool reallySatisfied = _xorBlackBoxEvaluator.StopConditionSatisfied;
                Debug.Assert((fi._fitness >= 10) == _xorBlackBoxEvaluator.StopConditionSatisfied);
                if (!reallySatisfied)
                {
                    Console.Out.WriteLine("ERROR: really fitness is not calculated (" + fi._fitness + " versus " +
                                          cFitness.Fitness + ")");
                    Console.Out.WriteLine(phenome.ToString());
                    //throw new Exception("Wheih :(");
                }
                else
                {
                 Console.Out.WriteLine("Networks is really good");   
                }
            }

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
