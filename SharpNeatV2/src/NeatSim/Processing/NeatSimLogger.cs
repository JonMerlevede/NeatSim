using SharpNeat.Core;
using SharpNeat.EvolutionAlgorithms;
using SharpNeat.Genomes.Neat;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace NeatSim.Processing
{
    public class NeatSimLogger
    {
        private readonly StreamWriter _neatsim_logger;
        private readonly StreamWriter _fitness_logger;
        // Necessary for calculating evaluations per second
        private ulong _oldTotalEvaluationCount;
        private DateTime _oldCurrentTime;
        private double _oldEvaluationCountPerSecond;

        /// <summary>
        /// Constructor
        /// </summary>
        public NeatSimLogger(String path)
        {
            const bool append = true;
            _neatsim_logger = new StreamWriter(path + "NeatSim.csv", append, Encoding.ASCII);
            _neatsim_logger.WriteLine(
                "ClockTime," + //1
                "Gen," + //2
                "LowestCost," + //3
                "MeanCost," + //4
                "HighestCost," + //5
                "MeanSpecieChampCost," + //6
                "ChampComplexity," + //7
                "MeanComplexity," + //8
                "MaxComplexity," + //9
                "TotalEvaluationCount," + //10
                "EvaluationsPerSec," + //11
                "SearchMode," + // 12
                "SpecieSizes" // 13
                );
            _oldTotalEvaluationCount = 0;
            _oldCurrentTime = DateTime.Now;
            _fitness_logger = new StreamWriter(path + "FitnessValues.csv", append, Encoding.ASCII);
        }


        /// <summary>
        /// Log
        /// </summary>
        /// <param name="ea"></param>
        /// <param name="igle"></param>
        public void Log(NeatEvolutionAlgorithm<NeatGenome> ea)
        {
            Func<NeatGenome, double> costOfNeatGenome = neatGenome => neatGenome.EvaluationInfo.AuxFitnessArr[0]._value;
            // I made the evaluator public myself...
            IGenomeListEvaluator<NeatGenome> igle = ea._genomeListEvaluator;

            var champGenome = ea.GenomeList.Aggregate(
                (currentBest, candidate) =>
                costOfNeatGenome(currentBest) <= costOfNeatGenome(candidate)
                    ? currentBest
                    : candidate);

            var currentTime = DateTime.Now;
            var currentGeneration = ea.CurrentGeneration;
            var minimumCost = costOfNeatGenome(champGenome);
            var meanCost = ea.GenomeList.Average(costOfNeatGenome);
            var highestCost = ea.GenomeList.Max(costOfNeatGenome);
            var meanSpecieChampCost = ea.SpecieList.Average(
                specieList => specieList.GenomeList.Min(costOfNeatGenome));
            var champComplexity = champGenome.Complexity;
            var meanComplexity = ea.GenomeList.Average(neatGenome => neatGenome.Complexity);
            var maxComplexity = ea.GenomeList.Max(neatGenome => neatGenome.Complexity);
            //double totalEvaluationCount = ea.GenomeList.Sum(neatGenome => neatGenome.EvaluationInfo.EvaluationCount);
            var totalEvaluationCount = igle.EvaluationCount;

            var timeSinceLastCallToLog = currentTime - _oldCurrentTime;

            // To smooth out the evaluations per second statistic, we update only if at least one second has elapsed since it was last updated.
            // This was taken from NeatEvolutionAlgorithm.cs
            // Note that there, the documentation does not actually correspond to the implementation.
            //
            // I would actually write down the exact number I'm getting, calculating
            // sensible rolling averages during processing of results because I save
            // the result as a double instead of an integer.
            // For me, this does not really matter though: a generation takes far longer
            // than a second, and the Log function is called only at the end of a generation...
            //if (timeSinceLastCallToLog.Milliseconds > 999)
            //{
                var evaluationsSinceLastUpdate = totalEvaluationCount - _oldTotalEvaluationCount;
                var evaluationsPerSecond = ((double)TimeSpan.TicksPerSecond*evaluationsSinceLastUpdate)/(timeSinceLastCallToLog.Ticks);
                // Reset working variables
                _oldCurrentTime = currentTime;
                _oldTotalEvaluationCount = totalEvaluationCount;
                _oldEvaluationCountPerSecond = evaluationsPerSecond;
            //}

            IEnumerable<int> specieSizes = ea.SpecieList.Select(specie => specie.GenomeList.Count).ToList();
            String specieSizeString = String.Join(";", specieSizes);
            //ea.SpecieList.Aggregate("",(accumulatedString,specie) => specie.)

            _neatsim_logger.WriteLine(
                string.Format(
                    "{0:yyyy-MM-dd HH:mm:ss.fff},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11},{12}",
                    currentTime, //1
                    currentGeneration, //2
                    minimumCost, //3
                    meanCost, //4
                    highestCost, //5
                    meanSpecieChampCost, //6
                    champComplexity, //7
                    meanComplexity, //8
                    maxComplexity, //9
                    totalEvaluationCount, //10
                    _oldEvaluationCountPerSecond, //11
                    ea.ComplexityRegulationMode, //12
                    specieSizeString //13
                )
                );
            _neatsim_logger.Flush();

            var fitnessValues = ea.GenomeList.Select(individual => individual.EvaluationInfo.AuxFitnessArr[0]._value);
            String fitnessValueString = String.Join(";", fitnessValues);
            _fitness_logger.WriteLine(fitnessValueString);
            _fitness_logger.Flush();
        }

        public void Close()
        {
            _neatsim_logger.Close();
            _fitness_logger.Close();
        }
    }
}
