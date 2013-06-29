using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading;
using NeatSim.Core;
using NeatSim.Thrift;
using SharpNeat.Core;
using SharpNeat.Phenomes.NeuralNets;
using SharpNeat.EvolutionAlgorithms;
using SharpNeat.Genomes.Neat;
using Thrift.Transport;

namespace NeatSim.Experiments.Sim
{
    class RemoteBatchSimEvaluator : IBatchPhenomeEvaluator<FastCyclicNetwork>
    {
        private NeatEvolutionAlgorithm<NeatGenome> _ea;
        public RemoteBatchSimEvaluator(NeatEvolutionAlgorithm<NeatGenome> ea)
        {
            Debug.WriteLine("Created new NeatsimPhenomeEvaluator");
            this._ea = ea;
        }

        public ulong EvaluationCount { get; private set; }

        public bool StopConditionSatisfied { get; private set; }

        private CPopulationFitness calculateSimPopulationFitness(CPopulationInfo populationInfo)
        {
            try
            {
                ProtocolManager.Open();
                Console.WriteLine("Evaluating generation " + populationInfo.Generation);
                return ProtocolManager.Client.calculateSimPopulationFitness(populationInfo);
            }
            catch (Exception exception)
            {
                Console.WriteLine("Lost connection to evaluator (" + exception.StackTrace + ")");
                Console.WriteLine("Sleeping for 2 seconds, creating new connection.");
                ProtocolManager.Close();
                Thread.Sleep(2000);
                return calculateSimPopulationFitness(populationInfo);
            }
        }

        public List<FitnessInfo> Evaluate(List<FastCyclicNetwork> phenomes)
        {
            var populationInfo = new CPopulationInfo
                                     {
                                         Phenomes = FastCyclicNetworkAdapter.Convert(phenomes),
                                         Generation = (int)_ea.CurrentGeneration
                                     };
            var fitnessInfo = calculateSimPopulationFitness(populationInfo);
            EvaluationCount += (uint)fitnessInfo.EvaluationCount;
            var result = new List<FitnessInfo>(fitnessInfo.FitnessInfos.Count);
            foreach (var fi in fitnessInfo.FitnessInfos)
            {
                StopConditionSatisfied |= fi.StopConditionSatisfied;
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
