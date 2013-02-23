using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using NeatSim.Core;
using NeatSim.Thrift;
using SharpNeat.Core;
using SharpNeat.Phenomes.NeuralNets;
using SharpNeat.EvolutionAlgorithms;
using SharpNeat.Genomes.Neat;

namespace NeatSim.Experiments.Xor
{
    class RemoteBatchXorEvaluator : IBatchPhenomeEvaluator<FastCyclicNetwork>
    {
        private NeatEvolutionAlgorithm<NeatGenome> _ea;
        public RemoteBatchXorEvaluator(NeatEvolutionAlgorithm<NeatGenome> ea)
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
            var fitnessInfo = ProtocolManager.Client.calculateXorPopulationFitness(populationInfo);
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
