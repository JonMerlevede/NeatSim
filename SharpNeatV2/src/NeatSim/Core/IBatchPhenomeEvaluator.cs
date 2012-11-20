using System.Collections.Generic;
using SharpNeat.Core;

namespace NeatSim.Core
{
    /// <summary>
    /// Generic interface for fcn evaluation classes.
    /// Evaluates and assigns a fitness to individual TPhenome's.
    /// </summary>
    public interface IBatchPhenomeEvaluator<TPhenome>
    {
        /// <summary>
        /// Gets the total number of individual genome evaluations that have been performed by this evaluator.
        /// </summary>
        ulong EvaluationCount { get; }

        /// <summary>
        /// Gets a value indicating whether some goal fitness has been achieved and that
        /// the the evolutionary algorithm search should stop. This property's value can remain false
        /// to allow the algorithm to run indefinitely.
        /// </summary>
        bool StopConditionSatisfied { get; }

        /// <summary>
        /// Evaluate the provided phenomes and returns their fitness score.
        /// </summary>
        List<FitnessInfo> Evaluate(List<TPhenome> phenome);

        /// <summary>
        /// Reset the internal state of the evaluation scheme if any exists.
        /// </summary>
        void Reset();
    }
}
