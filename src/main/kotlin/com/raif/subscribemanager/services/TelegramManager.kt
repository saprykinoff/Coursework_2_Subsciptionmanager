package com.raif.subscribemanager.services

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class TelegramManager (
    private val telegramService: TelegramService,
    private val dataLayer: DataLayer,
)
{
    @EventListener
    fun update(update: Update) {
        if (update.hasMessage()) {
            val msg = update.message
            val text = msg.text
            if (!msg.chat.isUserChat) {
                var args = text.split(" ")
                if (args[0] == "/reg") {
                    print("reg: ")
                    val price = args.getOrNull(2)?.toDoubleOrNull() ?: 100.0
                    val period = args.getOrNull(3)?.toIntOrNull() ?: 10
                    println( dataLayer.registerGroup(msg.chatId, msg.from.id, args[1], price, period))
                }
            }
        }
    }


}