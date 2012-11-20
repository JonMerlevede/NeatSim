using System.Collections.Generic;
using SharpNeat.Core;

namespace NeatSim.Core
{
    /// <summary>
    /// Like SerialGenomeListEvaluator, but operates in batchmode.
    /// Look at SerialGenomeListEvaluator for decent comments.
    /// </summary>
    /// <typeparam name="TGenome">The genome type that is decoded.</typeparam>
    /// <typeparam name="TPhenome">The phenome type that is decoded to and then evaluated.</typeparam>
    public class BatchGenomeListEvaluator<TGenome,TPhenome> : IGenomeListEvaluator<TGenome>
        where TGenome : class, IGenome<TGenome>
        where TPhenome: class
    {
        private readonly EvaluationMethod _evaluationMethod;
        private readonly IGenomeDecoder<TGenome, TPhenome> _genomeDecoder;
        private readonly IBatchPhenomeEvaluator<TPhenome> _phenomeEvaluator;
        private readonly bool _enablePhenomeCaching;

        delegate void EvaluationMethod(IList<TGenome> genomeList);

        #region Constructor

        /// <summary>
        /// Construct with the provided IGenomeDecoder and IPhenomeEvaluator.
        /// Phenome caching is enabled by default.
        /// </summary>
        public BatchGenomeListEvaluator(IGenomeDecoder<TGenome,TPhenome> genomeDecoder,
                                         IBatchPhenomeEvaluator<TPhenome> phenomeEvaluator)
        {
            _genomeDecoder = genomeDecoder;
            _phenomeEvaluator = phenomeEvaluator;
            _enablePhenomeCaching = true;
            _evaluationMethod = EvaluateCaching;
        }

        /// <summary>
        /// Construct with the provided IGenomeDecoder, IPhenomeEvaluator and enablePhenomeCaching flag.
        /// </summary>
        public BatchGenomeListEvaluator(IGenomeDecoder<TGenome,TPhenome> genomeDecoder,
                                         IBatchPhenomeEvaluator<TPhenome> phenomeEvaluator,
                                         bool enablePhenomeCaching)
        {
            _genomeDecoder = genomeDecoder;
            _phenomeEvaluator = phenomeEvaluator;
            _enablePhenomeCaching = enablePhenomeCaching;

            if(_enablePhenomeCaching) {
                _evaluationMethod = EvaluateCaching;
            } else {
                _evaluationMethod = EvaluateNonCaching;
            }
        }

        #endregion

        #region IGenomeListEvaluator<TGenome> Members

        /// <summary>
        /// Gets the total number of individual genome evaluations that have been performed by this evaluator.
        /// </summary>
        public ulong EvaluationCount
        {
            get { return _phenomeEvaluator.EvaluationCount; }
        }

        /// <summary>
        /// Gets a value indicating whether some goal fitness has been achieved and that
        /// the the evolutionary algorithm/search should stop. This property's value can remain false
        /// to allow the algorithm to run indefinitely.
        /// </summary>
        public bool StopConditionSatisfied
        {
            get { return _phenomeEvaluator.StopConditionSatisfied; }
        }

        /// <summary>
        /// Evaluates a list of genomes. Here we decode each genome in series using the contained
        /// IGenomeDecoder and evaluate the resulting TPhenome using the contained IPhenomeEvaluator.
        /// </summary>
        public void Evaluate(IList<TGenome> genomeList)
        {
            _evaluationMethod(genomeList);
        }

        /// <summary>
        /// Reset the internal state of the evaluation scheme if any exists.
        /// </summary>
        public void Reset()
        {
            _phenomeEvaluator.Reset();
        }

        #endregion

        #region Private Methods

        //private void Evaluate_NonCaching(IList<TGenome> genomeList)
        //{
        //    // Decode and evaluate each genome in turn.
        //    foreach(TGenome genome in genomeList)
        //    {
        //        TPhenome phenome = _genomeDecoder.Decode(genome);
        //        if(null == phenome)
        //        {   // Non-viable genome.
        //            genome.EvaluationInfo.SetFitness(0.0);
        //            genome.EvaluationInfo.AuxFitnessArr = null;
        //        }
        //        else
        //        {
                    
        //            FitnessInfo fitnessInfo = _phenomeEvaluator.Evaluate(phenome);
        //            genome.EvaluationInfo.SetFitness(fitnessInfo._fitness);
        //            genome.EvaluationInfo.AuxFitnessArr = fitnessInfo._auxFitnessArr;
        //        }
        //    }
        //}
        private void EvaluateNonCaching(IList<TGenome> genomeList)
        {
            var phenomes = new List<TPhenome>(genomeList.Count);
            var genomes = new List<TGenome>(genomeList.Count);
            // Decode and evaluate each genome in turn.
            foreach (TGenome genome in genomeList)
            {
                var phenome = _genomeDecoder.Decode(genome);
                if (null == phenome)
                {   // Non-viable genome.
                    genome.EvaluationInfo.SetFitness(0.0);
                    genome.EvaluationInfo.AuxFitnessArr = null;
                }
                else
                {
                    phenomes.Add(phenome);
                    genomes.Add(genome);
                }
            }
            var fitnesses = _phenomeEvaluator.Evaluate(phenomes);
            for (int i = 0; i < fitnesses.Count; i++)
            {
                genomes[i].EvaluationInfo.SetFitness(fitnesses[i]._fitness);
                genomes[i].EvaluationInfo.AuxFitnessArr = fitnesses[i]._auxFitnessArr;
            }
        }


        private void EvaluateCaching(IList<TGenome> genomeList)
        {
            var phenomes = new List<TPhenome>();
            var genomes = new List<TGenome>();
            // Decode and evaluate each genome in turn.
            foreach(TGenome genome in genomeList)
            {
                var phenome = (TPhenome)genome.CachedPhenome;
                if(null == phenome) 
                {   // Decode the phenome and store a ref against the genome.
                    phenome = _genomeDecoder.Decode(genome);
                    genome.CachedPhenome = phenome;
                }

                if(null == phenome)
                {   // Non-viable genome.
                    genome.EvaluationInfo.SetFitness(0.0);
                    genome.EvaluationInfo.AuxFitnessArr = null;
                }
                else
                {
                    phenomes.Add(phenome);
                    genomes.Add(genome);
                }
            }
            var fitnesses = _phenomeEvaluator.Evaluate(phenomes);
            for (int i = 0; i < fitnesses.Count; i++)
            {
                genomes[i].EvaluationInfo.SetFitness(fitnesses[i]._fitness);
                genomes[i].EvaluationInfo.AuxFitnessArr = fitnesses[i]._auxFitnessArr;
            }
        }

        #endregion
    }
}
