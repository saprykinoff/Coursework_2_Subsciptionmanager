package com.raif.subscribemanager.services

import com.raif.subscribemanager.errors.EmptyGroupNameError
import com.raif.subscribemanager.errors.GroupAlreadyRegisteredError
import com.raif.subscribemanager.errors.GroupNameAlreadyExistsError
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class TelegramManager (
    private val telegramService: TelegramService,
    private val dataLayer: DataLayer,
    private val utilityService: UtilityService
)
{
    @EventListener
    fun update(update: Update) {
        if (update.hasMessage()) {
            val msg = update.message
            val text = if (msg.hasText()) {msg.text} else {null}
            if (msg.chat.isUserChat) {
                val args = text?.split(" ")
                if (args?.getOrNull(0) in arrayOf("/sub", "/subscribe")  ) {
                    val name = args?.getOrNull(1)
                    if (name == null) {
                        telegramService.sendMessage(msg.chatId, "Укажите называние группы после /sub[scribe] ")
                        return
                    }
                    val group = dataLayer.findGroup(name)
                    if (group == null) {
                        telegramService.sendMessage(msg.chatId, "Группа не найдена")
                        return
                    }
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val current = LocalDateTime.now().format(formatter)
                    val link = telegramService.createInviteLink(group.id, current)
                    val sub = dataLayer.createSubscribe(name, link.inviteLink)
                    if (sub == null) {
                        telegramService.sendMessage(msg.chatId, "Произошла ошибка. Попробуйте позже")
                        return
                    }
                    telegramService.sendMessage(msg.chatId, "${sub.qrUrl}\n${sub.inviteLink}")

                } else if (args?.getOrNull(0) in arrayOf("/unsub", "/unsubscribe")  ) {
                    TODO()
                } else if (args?.getOrNull(0) in arrayOf("/list")) {
                    TODO()
                }

            } else if (!msg.chat.isUserChat) {
                val args = text?.split(" ")
                if (args?.getOrNull(0) in arrayOf("/reg", "/register")) {
                    val price = args?.getOrNull(2)?.toDoubleOrNull() ?: 100.0
                    val period = args?.getOrNull(3)?.toIntOrNull() ?: 10

                    try {
                        val group = dataLayer.registerGroup(msg.chatId, msg.from.id, args?.getOrNull(1), price, period)
                        telegramService.sendMessage(msg.chatId, "Группа ${group.searchName} зарегестриована на пользователя ${group.ownerId}.\n" +
                                "Цена: ${group.price} рублей за ${utilityService.secondsToTime(group.period)}")
                    } catch (e: Exception) {
                        when (e) {
                            is EmptyGroupNameError,
                            is GroupAlreadyRegisteredError,
                            is GroupNameAlreadyExistsError -> {
                                telegramService.sendMessage(msg.chatId, e.toString(), replyTo = msg.messageId)
                            }
                            else -> throw e
                        }

                    }

                }
            }
        }
    }

    fun subscribeRequest(userId: Long, name: String) {

    }

}