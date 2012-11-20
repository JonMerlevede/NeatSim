using System.Collections.Generic;
using NeatSim.Core;
using SharpNeat.Core;
using SharpNeat.EvolutionAlgorithms;
using SharpNeat.Genomes.Neat;
using SharpNeat.Phenomes;

namespace NeatSim.Experiments.Xor
{
    class LocalXorExperiment : AbstractNeatExperiment
    {
        public override int InputCount
        {
            get { return 2; }
        }

        public override int OutputCount
        {
            get { return 1; }
        }

        public override NeatEvolutionAlgorithm<NeatGenome> CreateEvolutionAlgorithm(IGenomeFactory<NeatGenome> genomeFactory, List<NeatGenome> genomeList)
        {
            var ea = DefaultNeatEvolutionAlgorithm;
            var evaluator = new LocalXorEvaluator();
            // Create genome decoder.
            IGenomeDecoder<NeatGenome, IBlackBox> genomeDecoder = CreateGenomeDecoder();
            // Create a genome list evaluator. This packages up the genome decoder with the genome evaluator.
            IGenomeListEvaluator<NeatGenome> innerEvaluator = new SerialGenomeListEvaluator<NeatGenome, IBlackBox>(genomeDecoder, evaluator);
            // Wrap the list evaluator in a 'selective' evaulator that will only evaluate new genomes. That is, we skip re-evaluating any genomes
            // that were in the population in previous generations (elite genomes). This is determined by examining each genome's evaluation info object.
            IGenomeListEvaluator<NeatGenome> selectiveEvaluator = new SelectiveGenomeListEvaluator<NeatGenome>(
                                                                                    innerEvaluator,
                                                                                    SelectiveGenomeListEvaluator<NeatGenome>.CreatePredicate_OnceOnly());
            // Initialize the evolution algorithm.
            ea.Initialize(selectiveEvaluator, genomeFactory, genomeList);
            // Finished. Return the evolution algorithm
            return ea;
        }
    }
}
