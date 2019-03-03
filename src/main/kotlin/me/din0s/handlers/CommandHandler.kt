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

package me.din0s.handlers

import me.din0s.LOG
import me.din0s.cfg.Config
import me.din0s.cfg.ConfigEntry
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object CommandHandler : ListenerAdapter() {
    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if (event.author.id != ConfigEntry.OWNER_ID.value) return

        if (event.message.contentRaw.equals("reload", true)) {
            LOG.info("Reloading config & key file.")
            Config.load()
            KeyHandler.load()

            val text = ConfigEntry.TEXT_PERCENTAGE.value.toDouble()
            val voice = ConfigEntry.VOICE_PERCENTAGE.value.toDouble()
            val keyCount = KeyHandler.keyCount()
            event.channel.sendMessage(
                "```css\n" +
                        "Text Percentage: $text%\n" +
                        "Voice Percentage: $voice%\n" +
                        "Keys Loaded: $keyCount" +
                        "```"
            ).queue()
        }
    }
}
