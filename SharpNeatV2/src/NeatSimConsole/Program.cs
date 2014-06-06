/* ***************************************************************************
 * This file is part of SharpNEAT - Evolution of Neural Networks.
 * 
 * Copyright 2004-2006, 2009-2010 Colin Green (sharpneat@gmail.com)
 *
 * SharpNEAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SharpNEAT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SharpNEAT.  If not, see <http://www.gnu.org/licenses/>. 
 */
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Threading;
using System.Xml;
using NeatSim.Core;
using NeatSim.Experiments.Sim;
using NeatSim.Processing;
using NeatSimConsole;
using SharpNeat.Domains;
using SharpNeat.Utility;
using log4net.Config;
using SharpNeat.Core;
using SharpNeat.Domains.Xor;
using SharpNeat.EvolutionAlgorithms;
using SharpNeat.Genomes.Neat;

namespace NeatSimConsole
{
    /// <summary>
    /// Minimal console application that hardwires the setting up on a evolution algorithm and start it running.
    /// </summary>
    class Program
    {
        static IGenomeFactory<NeatGenome> _genomeFactory;
        static List<NeatGenome> _genomeList;
        static NeatEvolutionAlgorithm<NeatGenome> _ea;
        private static NeatSimLogger _neatSimLogger;
        private static INeatExperiment _experiment;

        static Options _options;

        private static readonly XmlWriterSettings XwSettings;
        
        static Program()
        {
            XwSettings = new XmlWriterSettings {Indent = true};
        }
         
        static void Main(string[] args)
        {
            // This program expects certain command line options, that are defined as annotated properties in the NeatSimConsole.Options class
            // We instantiate this class...
            _options = new Options();
            // ... and pass the arguments to this program to the options parser, who uses it to set the properties in 'options'.
            // If the command line options are incorrect, ParseArgumentsStrict prints a help message to the screen and exits...
            if (!CommandLine.Parser.Default.ParseArgumentsStrict(args, _options))
            {
                // ... therefore, this should really never ever happen.
                throw new SystemException("Something went wrong parsing arguments.");
            }
            // Now, all the properties in 'options' are set.

            FastRandom.__seedRng = new FastRandom(_options.Seed);

            // Initialise log4net (log to console). 
            // XmlConfigurator.Configure(new FileInfo("log4net.properties"));

            
            // We instatiate a remote batch simulation experiment, and use the properties set in the XML file to initialize the experiment.
            // The XML file contains properties like the number of generations to run the program for, and the number of individuals in the population.
            // For properties that are not set in the XML file, we initialize default values.
            _experiment = new RemoteBatchSimExperiment();
            var xmlConfig = new XmlDocument();
            try
            {
                xmlConfig.Load("neatsim.config.xml");
            }
            catch (FileNotFoundException e)
            {
                Console.WriteLine(@"Could not find neatsim.config.xml. Aborting.");
                return;
            }

            _experiment.Initialize("NeatSim", xmlConfig.DocumentElement);
            // The XML file cannot contain information about the inital number of connected neurons.
            // We want to initialize our population minimally, and do this by setting an absurdly small initial connections proportion.
            // The number of connected input neurons will always be at least one.
            // Note that there is an absurdly small chance that more than a single neuron will be connected in generation one.
            _experiment.NeatGenomeParameters.InitialInterconnectionsProportion = 0.0000000000001;

            // Create a genome factory with our neat genome parameters object and the appropriate number of input and output neuron genes.
            _genomeFactory = _experiment.CreateGenomeFactory();
            // Create an initial population of randomly generated genomes ('born' in generation 0).
            _genomeList = _genomeFactory.CreateGenomeList(_options.PopulationSize, 0);

            // Create evolution algorithm and attach update events.
            _ea = _experiment.CreateEvolutionAlgorithm(_genomeFactory, _genomeList);
            _ea.PausedEvent += (s, e) => Console.WriteLine(_ea.RunState == RunState.Paused
                                                               ? @"Program is paused"
                                                               : _ea.RunState == RunState.Running
                                                                     ? @"Program is unpaused."
                                                                     : @"Program is in unknown state...");
            _neatSimLogger = new NeatSimLogger(_options.LogFileName + '_' + DateTime.Now.ToString("yyyyMMdd"));
            //
            var nextGeneration = _ea.CurrentGeneration;
            var doneEvent = new AutoResetEvent(false);
            _ea.UpdateEvent += (s, e) =>
                {
                    if (_ea.CurrentGeneration < nextGeneration)
                    {
                        Console.WriteLine("Aborting!");
                        return;
                    }
                    Console.WriteLine(string.Format("gen={0:N0} bestFitness={1:N6}", _ea.CurrentGeneration, _ea.Statistics._maxFitness));
                    SaveChampionGenome();
                    _neatSimLogger.Log(_ea);
                    if (_ea.CurrentGeneration >= _options.Generations)
                    {
                        _ea.Stop();
                        _neatSimLogger.Close();
                        doneEvent.Set();
                    }
                    nextGeneration++;
                };

            // Start algorithm (it will run on a background thread).
            _ea.StartContinue();

            // Hit return to quit.
            //Console.ReadLine();
            doneEvent.WaitOne();
        }
            
        private static void SaveChampionGenome()
        {
            string filename = string.Format("{0}_{1:d}_{2:yyyyMMdd_HHmmss}.gnm.xml",
                                                        _options.ChampionPrefix, _ea.CurrentGeneration, DateTime.Now);
            // Save genome to xml file.
            using (XmlWriter xw = XmlWriter.Create(filename, XwSettings))
            {
                _experiment.SavePopulation(xw, new[] { _ea.CurrentChampGenome });
            }
        }
    
    }
}
