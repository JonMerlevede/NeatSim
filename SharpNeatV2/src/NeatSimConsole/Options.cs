using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using CommandLine;
using CommandLine.Text;

namespace NeatSimConsole
{
    class Options
    {
        [Option('g', "generations", Required = true,
            HelpText = "Number of generations to run NeatSim for")]
        public int Generations { get; set; }

        [Option('p', "populationsize", Required = true,
            HelpText = "Population size")]
        public int PopulationSize { get; set; }

        [Option('s', "seed", Required = true,
            HelpText = "Random seed to use")]
        public int Seed { get; set; }

        [Option('l', "log", Required = true,
            HelpText = "Name of file to log to")]
        public string LogFileName { get; set; }

        [Option('c', "champion", Required = true,
            HelpText = "Prefix for the XML files containing champion genomes")]
        public string ChampionPrefix { get; set; }

        [ParserState]
        public IParserState LastParserState { get; set; }

        //[HelpOption]
        //public string GetUsage()
        //{
        //    return HelpText.AutoBuild(this,
        //                              (HelpText current) => HelpText.DefaultParsingErrorsHandler(this, current));
        //}
    }
}
