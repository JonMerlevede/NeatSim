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
        private readonly StreamWriter _streamWriter;
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
            _streamWriter = new StreamWriter(path, append, Encoding.ASCII);
            _streamWriter.WriteLine(
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
                "SearchMode" // 12
                );
            _oldTotalEvaluationCount = 0;
            _oldCurrentTime = DateTime.Now;
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

            _streamWriter.WriteLine(
                //string.Format(
                    "{0:yyyy-MM-dd HH:mm:ss.fff},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11}",
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
                    ea.ComplexityRegulationMode //12
                //)
                );
            _streamWriter.Flush();
        }

        public void Close()
        {
            _streamWriter.Close();
        }
    }
}
