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
import me.din0s.cfg.ConfigEntry
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

object ActivityHandler : ListenerAdapter() {
    private val voiceUsers = hashMapOf<String, OffsetDateTime>()

    private fun JDA.notifyOwner(keyCount: Int) {
        getUserById(ConfigEntry.OWNER_ID.value).openPrivateChannel().queue { owner ->
            val msg = when (keyCount) {
                0 -> "No keys left!"
                else -> "Key count is getting low! ($keyCount)"
            }
            owner.sendMessage("**$msg**").queue()
            LOG.warn(msg)
        }
    }

    private fun User.roll(percentage: Double, voice: Boolean = false) {
        val random = Random.nextDouble() * 100
        LOG.debug("$asTag rolled $random. (needed: <=$percentage)")
        if (random >= percentage) return
        if (KeyHandler.isEmpty()) {
            jda.notifyOwner(0)
            return
        }

        openPrivateChannel().queue({
            val embed = EmbedBuilder()
            val key = KeyHandler.pop()

            embed.setTitle("Congratulations, you won!")
            embed.setColor(Color.decode("#2ECC71"))
            embed.appendDescription("Your Steam Key is **`$key`**.\n\n")
            embed.appendDescription("Thank you for being active in our community!\n")
            embed.appendDescription("[Click here for info on how to redeem it!](https://support.steampowered.com/kb_article.php?ref=5414-tfbn-1352)")
            it.sendMessage(embed.build()).queue()

            embed.setTitle("A Steam Key was dropped!")
            embed.setDescription(
                "**Congrats, $asMention!**\nYou just won a game key for being active!\n\n" +
                        "Keep engaging in text and voice channels to get more awesome prizes! \uD83C\uDF81"
            )
            jda.getTextChannelById(ConfigEntry.CHANNEL.value).sendMessage(embed.build()).queue()

            val source = if (voice) "VC" else "TXT"
            LOG.info("$asTag won a key! [$source]")

            val keyCount = KeyHandler.keyCount()
            if (keyCount <= ConfigEntry.KEY_THRESHOLD.value.toInt()) {
                jda.notifyOwner(keyCount)
            }
        }, {})
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot || event.channel.id != ConfigEntry.CHANNEL.value) return

        val percent = ConfigEntry.TEXT_PERCENTAGE.value.toDouble()
        event.author.roll(percent)
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val user = event.entity.user
        val id = user.id
        if (user.isBot) return

        val joined = event.channelJoined
        val afk = event.entity.guild.afkChannel
        val nullOrAfk = joined in setOf(null, afk)

        if (voiceUsers.containsKey(id) && nullOrAfk) {
            val joinTime = voiceUsers.remove(id)
            val now = OffsetDateTime.now()
            val mins = ChronoUnit.MINUTES.between(joinTime, now)
            LOG.debug("${user.asTag} left his active VC. (was on for $mins mins)")
            if (mins == 0L) return

            val win = ConfigEntry.VOICE_PERCENTAGE.value.toDouble()
            val loss = 1 - win
            val lossTotal = Math.pow(loss, mins.toDouble())
            val percent = 1 - lossTotal
            user.roll(percent, true)
        } else if (!voiceUsers.containsKey(id) && !nullOrAfk) {
            voiceUsers[id] = OffsetDateTime.now()
            LOG.debug("${user.asTag} joined an active VC.")
        }
    }
}
