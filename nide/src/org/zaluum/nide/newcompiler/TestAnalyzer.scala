package org.zaluum.nide.newcompiler

import java.io.FileInputStream
import java.io.File
import org.zaluum.nide.protobuf.BoxFileProtos
object TestAnalyzer {
  def main(args:Array[String]) {
    val f = new FileInputStream("../example/src/org/zaluum/example/testInner.zaluum")
    
    val bcd = BoxFileProtos.BoxClassDef.parseDelimitedFrom(f)
    val tree = ProtoParser.parse(bcd)
    val reporter = new Reporter()
    new Analyzer(reporter, tree, null)
  }
} 