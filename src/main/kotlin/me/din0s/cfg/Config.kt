/*
 * MIT License
 *
 * Copyright 2019 Dinos Papakostas
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.din0s.cfg

import me.din0s.LOG
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

object Config {
    private val file = File("config.json")
    private lateinit var obj: JSONObject

    init {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                LOG.error("Could not create the config file! Missing permissions?")
                System.exit(1)
            }

            obj = JSONObject()
            for (entry in ConfigEntry.values())
                obj.put(entry.key, "")

            write()

            LOG.info("Generated the config file.")
            System.exit(0)
        }
    }

    private fun write() {
        FileWriter(file).use {
            it.write(obj.toString(4))
            it.flush()
        }
    }

    fun load() {
        FileInputStream(file).use {
            val parser = JSONTokener(it)
            obj = JSONObject(parser)
        }

        for (entry in ConfigEntry.values()) {
            val key = entry.key

            if (!obj.has(key)) {
                LOG.error("Missing $key in config.json!")
                System.exit(1)
            }

            entry.value = obj.getString(key)
        }
    }
}
