package com.company.iris.cli

import org.apache.commons.cli.{DefaultParser, Options, Option => CliOption}

object CommandLineOptions {
  val options = new Options()
  options.addOption(
    CliOption
      .builder("h")
      .longOpt("HOST-NAME")
      .desc("current web service external host name")
      .hasArg()
      .required()
      .argName("HOST-NAME")
      .build()
  )
  options.addOption(
    CliOption
      .builder("w")
      .longOpt("WEB-PORT")
      .desc("web service port")
      .hasArg()
      .required()
      .argName("WEB-PORT")
      .build()
  )
  options.addOption(
    CliOption
      .builder("a")
      .longOpt("AKKA-PORT")
      .desc("akka cluster node port")
      .hasArg()
      .required()
      .argName("AKKA-PORT")
      .build()
  )
  options.addOption(
    CliOption
      .builder("s")
      .longOpt("SEED-NODES")
      .desc("akka cluster seed nodes, seperate with comma, example: localhost:2551,localhost:2552")
      .hasArg()
      .required()
      .argName("SEED-NODES")
      .build()
  )

  def getOptions(): Options = {
    options
  }
  def parseArgs(args: Array[String]): CommandLineWrapper = {
    val parser = new DefaultParser()
    val cmds   = parser.parse(options, args)
    new CommandLineWrapper(cmds)
  }
}
