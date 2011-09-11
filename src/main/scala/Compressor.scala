/*
 * Copyright 2011 Indrajit Raychaudhuri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scala_tools.sbt

import java.io.{File, Reader, Writer, IOException}
import java.nio.charset.Charset
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException
import com.yahoo.platform.yui.compressor.{CssCompressor => YuiCssCompressor}
import com.yahoo.platform.yui.compressor.{JavaScriptCompressor => YuiJsCompressor}
import sbt._


sealed trait Compressor {

  def compress(in: File, out: File, log: Logger): Unit

  def compressWith[T](in: File, out: File, log: Logger, charset: Charset)(f: (Reader, Writer) => T): T =
    IO.reader(in, charset) { rin =>
      IO.writer(out, "", charset) { wout =>
        f(rin, wout)
      }
    }
}

case class CssCompressor(breakCol: Int = 0, charset: Charset = IO.defaultCharset) extends Compressor {

  def compress(in: File, out: File, log: Logger) =
    compressWith(in, out, log, charset) {
      new YuiCssCompressor(_).compress(_, breakCol)
    }

}

case class JsCompressor(
    breakCol: Int = 0,
    munge: Boolean = false,
    verbose: Boolean = false,
    keepSemi: Boolean = false,
    noOptimize: Boolean = false,
    charset: Charset = IO.defaultCharset) extends Compressor {

  def compress(in: File, out: File, log: Logger) =
    compressWith(in, out, log, charset) {
      new YuiJsCompressor(_, new JsErrorReporter(log)).compress(_, breakCol, munge, verbose, keepSemi, noOptimize)
    }

  private[this] class JsErrorReporter(val log: Logger) extends ErrorReporter {

    def warning(message: String, sourceName: String, line:Int, lineSource: String, lineOffset: Int) {
      doLog(Level.Warn, message, sourceName, line, lineSource, lineOffset)
    }

    def error(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {
      doLog(Level.Error, message, sourceName, line, lineSource, lineOffset)
    }

    def runtimeError(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int): EvaluatorException = {
      doLog(Level.Error, message, sourceName, line, lineSource, lineOffset)
      new EvaluatorException(message, sourceName, line, lineSource, lineOffset)
    }

    def doLog(l: Level.Value, message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {
      if (line < 0) log.log(l, message)
      else log.log(l, "%s at %d [%d:%d] %s".format(sourceName, lineSource, line, lineOffset, message))
    }
  }

}