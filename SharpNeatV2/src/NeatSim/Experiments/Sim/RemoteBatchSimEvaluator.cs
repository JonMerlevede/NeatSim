using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using NeatSim.Core;
using NeatSim.Thrift;
using SharpNeat.Core;
using SharpNeat.Phenomes.NeuralNets;

namespace NeatSim.Experiments.Sim
{
    class RemoteBatchSimEvaluator : IBatchPhenomeEvaluator<FastCyclicNetwork>
    {
        public RemoteBatchSimEvaluator()
        {
            Debug.WriteLine("Created new NeatsimPhenomeEvaluator");

        }

        public ulong EvaluationCount { get; private set; }

        public bool StopConditionSatisfied { get; private set; }

        public List<FitnessInfo> Evaluate(List<FastCyclicNetwork> phenomes)
        {
            var populationInfo = new CPopulationInfo
                                     {
                                         Phenomes = FastCyclicNetworkAdapter.Convert(phenomes)
                                     };
            ProtocolManager.Open();
            var fitnessInfo = ProtocolManager.Client.calculateSimPopulationFitness(populationInfo);
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
