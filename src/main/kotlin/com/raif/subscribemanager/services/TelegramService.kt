package com.raif.subscribemanager.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest
import org.telegram.telegrambots.meta.api.methods.groupadministration.DeclineChatJoinRequest
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class TelegramService(
    @Value("\${telegram.botToken}")
    private val botToken: String,
    private val eventPublisher: ApplicationEventPublisher,
) : TelegramLongPollingBot(botToken) {

    @Value("\${telegram.botName}")
    private val botName = ""
    override fun getBotUsername(): String {
        return botName
    }

    private val logger = LoggerFactory.getLogger("TelegramService")
    override fun onUpdateReceived(update: Update) {
        logger.info("Received a new update: {}", update)
        eventPublisher.publishEvent(update)
    }

    fun sendMessage(
        chatId: Long,
        text: String,
        replyMarkup: ReplyKeyboard? = null,
        markdown: Boolean = false,
        replyTo: Int? = null
    ): Int {
        val send = SendMessage(chatId.toString(), text)
        if (markdown) {
            send.parseMode = "markdownV2"
        } else {
            send.parseMode = "HTML"
        }
        if (replyTo != null) {
            send.replyToMessageId = replyTo
        }
        if (replyMarkup != null) {
            send.replyMarkup = replyMarkup
        }
        val e = execute(send)
        return e.messageId
    }

    fun sendPhoto(
        chatId: Long,
        url: String,
        text: String = "",
        replyMarkup: ReplyKeyboard? = null,
        markdown: Boolean = false,
        replyTo: Int? = null
    ): Int {

        val send = SendPhoto(chatId.toString(), InputFile(url))
        send.caption = text
        if (replyTo != null) {
            send.replyToMessageId = replyTo
        }
        if (replyMarkup != null) {
            send.replyMarkup = replyMarkup
        }
        if (markdown) {
            send.parseMode = "markdownV2"
        } else {
            send.parseMode = "HTML"
        }
        val e = execute(send)
        return e.messageId
    }

    fun removeUserFromGroup(chatId: Long, userId: Long): Boolean {
        val setChatMemberStatus = UnbanChatMember(chatId.toString(), userId)
        try {
            return execute(setChatMemberStatus)

        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
        return false
    }

    fun createInviteLink(chatId: Long): ChatInviteLink {
        println("Create_Invite")
        val invite = CreateChatInviteLink(chatId.toString())
        invite.createsJoinRequest = true
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val currentTime = LocalDateTime.now().format(formatter)
        invite.name = currentTime
        return execute(invite)
    }

    fun approveRequest(req: ChatJoinRequest) {
        execute(ApproveChatJoinRequest(req.chat.id.toString(), req.user.id))
    }

    fun declineRequest(req: ChatJoinRequest) {
        execute(DeclineChatJoinRequest(req.chat.id.toString(), req.user.id))
    }
}