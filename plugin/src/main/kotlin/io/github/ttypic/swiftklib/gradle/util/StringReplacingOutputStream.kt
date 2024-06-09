package io.github.ttypic.swiftklib.gradle.util

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.StringReader
import java.io.StringWriter

internal class StringReplacingOutputStream(
    private val delegate: OutputStream,
    private val replacements: Map<String, String>,
) : OutputStream() {
    private val stringWriter = StringWriter()
    private val bufferedWriter = BufferedWriter(stringWriter)

    override fun write(b: Int) {
        bufferedWriter.write(b)
        if (b == '\n'.code) {
            flushLine()
        }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        val str = String(b, off, len, Charsets.UTF_8)
        bufferedWriter.write(str)
        if (str.contains('\n')) {
            flushLine()
        }
    }

    private fun flushLine() {
        bufferedWriter.flush()
        val content = stringWriter.toString()
        val reader = BufferedReader(StringReader(content))
        val outputWriter = OutputStreamWriter(delegate, Charsets.UTF_8)

        reader
            .lineSequence()
            .forEach { line ->
                var replacedLine = line
                replacements.forEach { (key, value) ->
                    replacedLine = replacedLine.replace(key, value)
                }
                outputWriter.write(replacedLine)
                outputWriter.write('\n'.code)
            }

        outputWriter.flush()
        stringWriter.buffer.setLength(0)
    }

    override fun flush() {
        flushLine()
        delegate.flush()
    }

    override fun close() {
        flushLine()
        delegate.close()
    }
}
