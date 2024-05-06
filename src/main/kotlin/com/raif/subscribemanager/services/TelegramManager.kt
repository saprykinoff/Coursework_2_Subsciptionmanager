package com.raif.subscribemanager.services

import com.raif.subscribemanager.errors.EmptyGroupNameError
import com.raif.subscribemanager.errors.GroupAlreadyRegisteredError
import com.raif.subscribemanager.errors.GroupNameAlreadyExistsError
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException

@Service
class TelegramManager(
    private val telegramService: TelegramService,
    private val dataLayer: DataLayer,
    private val utilityService: UtilityService
) {


    private val logger = LoggerFactory.getLogger("Manager")

    @EventListener
    fun update(update: Update) {

        if (update.hasMessage()) {
            val msg = update.message
            if (msg.chat.isUserChat) {
                userController(msg)
            } else if (msg.chat.isSuperGroupChat) {
                supergroupController(msg)
            } else if (msg.chat.isGroupChat) {
                telegramService.sendMessage(
                    msg.chatId, "Для работы бота необхоимо чтобы группа была супергруппой.\n" +
                            "Для этого включите (а затем отключите если это необходимо) историю сообщений через:\n" +
                            "Три точки -> Manage group -> Chat history"
                )
            } else {
                telegramService.sendMessage(msg.chatId, "В данный момент бот работает только в супергруппах")
            }
        }
        if (update.hasChannelPost()) {
            logger.info("POST")
            supergroupController(update.channelPost)

        }
        if (update.hasChatJoinRequest()) {
            joinRequestController(update.chatJoinRequest)
        }

    }


    fun userController(msg: Message) {
        val text = if (msg.hasText()) {
            msg.text
        } else {
            null
        }
        val args = text?.split(" ")
        val command = args?.getOrNull(0)
        if (command in arrayOf("/help", "/start")) {
            telegramService.sendMessage(
                msg.chatId,
                "Это бот для организации подписочного доступа к группам.\n\n" +
                        "Если вы администратор, воспользуйтесь командой /helpadmin чтобы получить инстркцию как подключить вашу группу к боту.\n\n" +
                        "Если вы хотите подписаться на группу, воспользуйтесь командой /helpsub чтобы узнать детали"
            )
        } else if (command == "/helpadmin") {
            telegramService.sendMessage(
                msg.chatId,
                "Для создания группы выполните следующие шаги:\n\n" +
                        "1) Создайте группу в телеграмме\n" +
                        "2) Добавьте этого бота в группу и назначте его администратором (Ban users, Invite users via link)\n" +
                        "3) Используйте команду <code>/reg </code><code>name [price = 100 [duration =30]]</code> " +
                        "чтобы зарегестрировать группу в системе, где <code>name</code> это уникальное имя вашей группы, " +
                        "<code>price</code> это стоимость в рублях за <code>duration</code> дней\n" +
                        "4) Сообщите ваше уникальное имя своим подписчикам, чтобы они смогли подписываться"
            )

        } else if (command == "/helpsub") {
            telegramService.sendMessage(
                msg.chatId,
                "1) Для того чтобы оплатить подписку используйте команду <code>/sub</code> <code>name</code>, " +
                        "где <code>name</code> это уникальное имя группы на которую вы хотите подписаться\n" +
                        "2) Оплатите полученный QR-code используя сайт https://pay.raif.ru/pay/rfuture/#/reader\n" +
                        "3) После оплаты вы получите уникальную ссылку на группу. Вы можете как воспользоваться ей сами, так " +
                        "и отдать ее кому-то. Важно: Подписка уже активна, даже если никто еще не вступил по ссылке\n" +
                        "4) Перейдя по ссылке вы автоматически будете добавлены в группу\n\n" +
                        "5) Посмотреть активные подписки можно используя команду /list\n\n" +
                        "6) В случае необходимости воспользуйтесь командой /unsub <code>index</code> чтобы аннулировать подписку под номером index. " +
                        "Чтобы узнать номер воспользуйтесь коммандой /list (цифра в скобках и есть необходимый номер) "
            )
        } else if (command in arrayOf("/sub", "/subscribe")) {
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
            try {
                telegramService.createInviteLink(group.id)
            } catch (e: TelegramApiRequestException) {
                telegramService.sendMessage(
                    msg.chatId,
                    "В данной группе у бота нет прав администратора. Сообщите об это создателю группы"
                )
                return
            }
            val sub = utilityService.createSubscription(name, msg.from.id)
            if (sub == null) {
                telegramService.sendMessage(msg.chatId, "Произошла ошибка. Попробуйте позже")
                return
            }
            telegramService.sendMessage(msg.chatId, "Оплатите <a href=\"${sub.qrUrl}\">подписку</a>")

        } else if (command in arrayOf("/unsub", "/unsubscribe")) {
            val groupIndex = args?.getOrNull(1)?.toIntOrNull()
            if (groupIndex == null) {
                telegramService.sendMessage(msg.chatId, "Укажите индекс подписки которую вы хотите отменить")
                return
            }
            val subs = dataLayer.getUserSubs(msg.chatId)
            if (groupIndex < 1 || subs.size < groupIndex) {
                telegramService.sendMessage(msg.chatId, "Нет такого индекса")
                return
            }
            val sub = subs[groupIndex - 1]
            sub.status = "DEAD"
            dataLayer.saveSub(sub)
            telegramService.sendMessage(msg.chatId, "Подписка успешно удалена")
            if (sub.userId != null) {
                telegramService.removeUserFromGroup(sub.group.id, sub.userId!!)
            }

        } else if (command in arrayOf("/list")) {
            val subs = dataLayer.getUserSubs(msg.chatId)

            if (subs.isEmpty()) {
                telegramService.sendMessage(msg.chatId, "У вас нет активных подписок")
            } else {
                var textPay = "Подписки которые вы оплачиваете:\n"
                var textSub = "Подписки активные для вас:\n"
                var i = 0
                for (sub in subs) {
                    i += 1
                    val until =  if( sub.nextPayment != null) {
                        "Подписка активна до: ${sub.nextPayment}"
                    } else {
                        "Подписка не активна"
                    }
                        if (sub.userId == msg.chatId) {
                            textSub += "${i}: \"${sub.group.searchName}\"\n${until}}\n\n"
                        }
                        if (sub.createdByUserId == msg.chatId) {
                            textPay += "${i}: \"${sub.group.searchName}\"\n${until}\n\n"
                        }

                    }
                telegramService.sendMessage(msg.chatId, textPay)
                telegramService.sendMessage(msg.chatId, textSub)
            }
        } else {
            telegramService.sendMessage(
                msg.chatId,
                "Не понимаю что вы хотели сказать. Используйте /helpadmin или /helpsub"
            )
        }
    }


    fun supergroupController(msg: Message) {
        val text = if (msg.hasText()) {
            msg.text
        } else {
            null
        }
        val args = text?.split(" ")
        var command = args?.getOrNull(0)
        val botname = "@subscribeManager_sprffbot"
        if (command?.contains(botname) == true) {
            command = command.substring(0, command.length - botname.length)
        }
        if (command in arrayOf("/reg", "/register")) {
            logger.info("reg")
            val price = args?.getOrNull(2)?.toDoubleOrNull() ?: 100.0
            val period = args?.getOrNull(3)?.toIntOrNull() ?: 120
            try {
                val from = if (msg.chat.isChannelChat) {
                    0
                } else {
                    msg.from.id
                }
                val group = dataLayer.registerGroup(msg.chatId, from, args?.getOrNull(1), price, period)
                if (from != 0L) {
                    telegramService.sendMessage(
                        msg.chatId,
                        "Группа \"${group.searchName}\" зарегестриована на пользователя ${group.ownerId}.\n" +
                                "Цена: ${group.price} рублей за ${utilityService.secondsToTime(group.period)}"
                    )
                } else {
                    telegramService.sendMessage(
                        msg.chatId,
                        "Канал \"${group.searchName}\" зарегестриован.\n" +
                                "Цена: ${group.price} рублей за ${utilityService.secondsToTime(group.period)}"
                    )
                }
            } catch (e: Exception) {
                when (e) {
                    is EmptyGroupNameError,
                    is GroupAlreadyRegisteredError,
                    is GroupNameAlreadyExistsError -> {
                        telegramService.sendMessage(msg.chatId, e.message!!, replyTo = msg.messageId)
                    }

                    else -> throw e
                }

            }

        }

    }

    fun joinRequestController(req: ChatJoinRequest) {
        val sub = dataLayer.getSubByLink(req.inviteLink.inviteLink)
        if (sub != null) {
            if (sub.nextPayment == null) {
                telegramService.sendMessage(req.user.id, "Сначала необходимо оплатить подписку")
                telegramService.declineRequest(req)
                return
            }
            if (sub.userId == null) {
                sub.userId = req.user.id
                dataLayer.saveSub(sub)
            }

            if (sub.userId == req.user.id) {
                telegramService.approveRequest(req)
                logger.info("Accept user ${req.user.id} to group ${req.chat.id}")
            } else {
                telegramService.declineRequest(req)
                logger.info("Decline user ${req.user.id} to group ${req.chat.id}")
                telegramService.sendMessage(req.user.id, "Данная ссылка уже была кем-то использована")
            }
        } else {
            logger.info("Ignore user ${req.user.id} to group ${req.chat.id}")
        }
    }

}