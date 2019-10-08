package com.company.iris.cli

import org.apache.commons.cli.CommandLine

class CommandLineWrapper(private val cmds: CommandLine) {


  def getWebPort(): Int = {
    cmds.getOptionValue("w").toInt
  }

  def getAkkaPort(): Int = {
    cmds.getOptionValue("a").toInt
  }

  def getSeedNodes() : String = {
    cmds.getOptionValue("s")
  }


}