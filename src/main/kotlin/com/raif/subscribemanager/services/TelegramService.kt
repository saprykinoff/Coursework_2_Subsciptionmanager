package com.raif.subscribemanager.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class TelegramService(
    @Value("\${telegram.botToken}")
    private val botToken: String,
) : TelegramLongPollingBot(botToken) {

    @Value("\${telegram.botName}")
    private val botName = ""
    override fun getBotUsername(): String {
        return botName
    }

    private var id = 1
    override fun onUpdateReceived(update: Update) {
        println(update)
        if (update.hasChatJoinRequest()) {
            val req = update.chatJoinRequest
            val userId = req.user.id
            val name = req.inviteLink.name
            val link = req.inviteLink.inviteLink
            execute(SendMessage(userId.toString(), "aboba"))
            println("req: $userId, $name, $link")
        }
        if (update.hasMessage()) {
            createInviteLink(update.message.chatId, id++)
        }
    }

    private val logger = LoggerFactory.getLogger("TelegramService")





    fun removeUserFromGroup(chatId: Long, userId: Long): Boolean {
        val setChatMemberStatus = UnbanChatMember(chatId.toString(), userId)
        try {
            return execute(setChatMemberStatus)

        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
        return false
    }


    fun createInviteLink(chatId: Long, linkId: Int): ChatInviteLink {
        println("Create_Invite")
        val invite = CreateChatInviteLink(chatId.toString())
        invite.createsJoinRequest = true
        invite.name = "Приглашалка номер #$linkId"
        return execute(invite)
    }
}