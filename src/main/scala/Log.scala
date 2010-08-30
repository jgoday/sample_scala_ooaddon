package sample.oo

import java.io.{File, FileWriter}

object Log {
    val DEFAULT = "/tmp/sample_ooaddon_scala.log"

    def apply(text: String) = new Log(DEFAULT).debug(text)
    def apply(file: String, text: String) = new Log(file).debug(text)
}

class Log(file : String) {
    def debug(text : String) : Unit = {
        val fw = new FileWriter(new File(file), true)
        try { fw.write(text + "\n") }
        finally{ fw.close }
    }
}