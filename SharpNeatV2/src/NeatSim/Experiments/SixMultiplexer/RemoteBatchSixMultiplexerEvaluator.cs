using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using NeatSim.Core;
using NeatSim.Thrift;
using SharpNeat.Core;
using SharpNeat.Domains;
using SharpNeat.Domains.BinarySixMultiplexer;
using SharpNeat.Phenomes.NeuralNets;
using SharpNeat.EvolutionAlgorithms;
using SharpNeat.Genomes.Neat;

namespace NeatSim.Experiments.SixMultiplexer
{
    class RemoteBatchSixMultiplexerEvaluator : IBatchPhenomeEvaluator<FastCyclicNetwork>
    {
        private readonly BinarySixMultiplexerEvaluator _binarySixMultiplexerEvaluator = new BinarySixMultiplexerEvaluator();
        
        private NeatEvolutionAlgorithm<NeatGenome> _ea;
        public RemoteBatchSixMultiplexerEvaluator(NeatEvolutionAlgorithm<NeatGenome> ea)
        {
            Debug.WriteLine("Created new NeatsimPhenomeEvaluator");
            this._ea = ea;
        }

        public ulong EvaluationCount { get; private set; }

        public bool StopConditionSatisfied { get; private set; }

        public List<FitnessInfo> Evaluate(List<FastCyclicNetwork> phenomes)
        {
            var populationInfo = new CPopulationInfo
            {
                Phenomes = FastCyclicNetworkAdapter.Convert(phenomes),
                Generation = (int)_ea.CurrentGeneration
            };
            ProtocolManager.Open();
            var fitnessInfo = ProtocolManager.Client.calculateSixMultiplexerPopulationFitness(populationInfo);
            EvaluationCount += (uint)fitnessInfo.EvaluationCount;
            var result = new List<FitnessInfo>(fitnessInfo.FitnessInfos.Count);
            for (var i = 0; i < fitnessInfo.FitnessInfos.Count; i++)
            {
                var fi = fitnessInfo.FitnessInfos[i];
                StopConditionSatisfied |= fi.StopConditionSatisfied;
                // Verify that the stop condition is actually really satisfied; if not, there is an error in the Java evaluator and we throw an exception
                if (fi.StopConditionSatisfied)
                {
                    var sharpneatFitness = _binarySixMultiplexerEvaluator.Evaluate(phenomes[i]);
                    var reallySatisfied = sharpneatFitness._fitness >= 1000;
                    if (!reallySatisfied)
                    {
                        Console.Out.WriteLine("ERROR: " + sharpneatFitness._fitness + " versus " + fi.Fitness);
                        throw new Exception("Noes there is an error in my Java code :(");
                    }
                    else
                    {
                        Console.Out.WriteLine("Yeey, the result is really correct: " + sharpneatFitness._fitness + " versus " + fi.Fitness);
                    }
                }
                result.Add(new FitnessInfo(
                    fi.Fitness,
                    fi.AuxFitness.Select(aux => new AuxFitnessInfo(aux.Name, aux.Value)).ToArray())
                );
            }
            return result;
        }

        public void Reset()
        {
            // no state!
        }
    }
}
