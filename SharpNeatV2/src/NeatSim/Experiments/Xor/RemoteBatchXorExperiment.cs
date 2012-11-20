using System.Collections.Generic;
using System.Xml;
using NeatSim.Core;
using SharpNeat.Core;
using SharpNeat.EvolutionAlgorithms;
using SharpNeat.Genomes.Neat;
using SharpNeat.Phenomes;
using SharpNeat.Phenomes.NeuralNets;

namespace NeatSim.Experiments.Xor
{
    class RemoteBatchXorExperiment : AbstractNeatExperiment
    {
        public override int InputCount
        {
            get { return 2; }
        }
        public override int OutputCount
        {
            get { return 1; }
        }

        private FastCyclicNeatGenomeDecoder _decoder;
        public override IGenomeDecoder<NeatGenome, IBlackBox> CreateGenomeDecoder()
        {
            return _decoder;
        }

        public override NeatEvolutionAlgorithm<NeatGenome> CreateEvolutionAlgorithm(IGenomeFactory<NeatGenome> genomeFactory, List<NeatGenome> genomeList)
        {
            // Create the evolution algorithm.
            var ea = DefaultNeatEvolutionAlgorithm;
            var evaluator = new RemoteBatchXorEvaluator();
            IGenomeDecoder<NeatGenome, FastCyclicNetwork> genomeDecoder = _decoder;
            // Evaluates list of phenotypes
            IGenomeListEvaluator<NeatGenome> innerEvaluator =
                new BatchGenomeListEvaluator<NeatGenome, FastCyclicNetwork>(
                    genomeDecoder,
                    evaluator);
            // Weeds down the list to be evaluated
            IGenomeListEvaluator<NeatGenome> selectiveEvaluator = 
                new SelectiveGenomeListEvaluator<NeatGenome>(
                    innerEvaluator,
                    SelectiveGenomeListEvaluator<NeatGenome>.CreatePredicate_OnceOnly());
            ea.Initialize(selectiveEvaluator, genomeFactory, genomeList);
            return ea;
        }

        public override void Initialize(string name, XmlElement xmlConfig) 
        {
            base.Initialize(name, xmlConfig);
            _decoder = new FastCyclicNeatGenomeDecoder(DefaultNetworkActivationScheme);
        }
    }
}
