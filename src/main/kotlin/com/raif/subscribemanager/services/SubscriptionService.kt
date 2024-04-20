package com.raif.subscribemanager.services

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

@Service
class SubscriptionService(
    private val dataLayer: DataLayer,
    private val telegramService: TelegramService,
    private val utilityService: UtilityService
) {

    private val logger = LoggerFactory.getLogger("Subscription")
    @Scheduled(fixedDelay = 1000)
    fun manageSubs() {
        for (sub in dataLayer.getExpiredSubs()) {
            if (!dataLayer.hasActivePay(sub.id)) {
                utilityService.createPaySubQr(sub.id, sub.group.price)
                logger.info("Create payment for subscription(${sub.id}) with amount ${sub.group.price}")
            }
        }
        for (sub in dataLayer.getUnpaidSubs()) {
            if (Instant.now().isAfter(sub.createdDate.toInstant().plusSeconds(3600))) {
                telegramService.sendMessage(sub.createdByUserId, "Qr по ссылке ${sub.qrUrl} больше не активен") //TODO указать имя группы
                logger.info("Subscription(${sub.id}) has expired")
//                dataLayer.deleteSub(sub.id) TODO делать подписку dead
                continue
            }
            if (utilityService.getSubQr(sub.id)?.getString("status") == "SUBSCRIBED") { //TODO проверка подписки
                logger.info("Subscription(${sub.id}) has paid")
                val link = telegramService.createInviteLink(sub.group.id)
                sub.nextPayment = Date.from(Instant.now())
                sub.inviteLink = link.inviteLink
                dataLayer.saveSub(sub)
                telegramService.sendMessage(sub.createdByUserId, "Подписка успешно оплачена.\nВаша ссылка ${link.inviteLink}. Вы можете как воспользоваться ей сами так и поделиться с кем-то") //TODO указать имя группы
            }
        }

    }

    @Scheduled(fixedDelay = 1000)
    fun managePays() {
        val pays = dataLayer.getActivePays()
        for (pay in pays) {
            val status = utilityService.getPaySubQr(pay.id)?.getString("paymentStatus")
            if (status == "SUCCESS") {
                successPayment(pay.subId)
                pay.status = "SUCCESS"
                dataLayer.savePay(pay)
            }
            if (status == "DECLINED") {
                declinePayment(pay.subId)
                pay.status = "DECLINED"
                dataLayer.savePay(pay)
            }
        }
    }

    fun successPayment(subId: Int) {
        val sub = dataLayer.getSub(subId) ?: throw Exception()
        //sub.status = "ACTIVE"
        if (sub.userId != null){
            telegramService.sendMessage(sub.userId!!, "Ваша подписка на группу \"${sub.group.searchName}\" продлена")
        }
        sub.nextPayment = Date.from(Instant.now().plusSeconds(sub.group.period.toLong()))
        dataLayer.saveSub(sub)
    }

    fun declinePayment(subId: Int) {
        val sub = dataLayer.getSub(subId) ?: throw Exception()
        //TODO делать подписку невалидной
        //sub.status = "DEAD"
        if (sub.userId != null){
            telegramService.sendMessage(sub.userId!!, "Не удалось списать деньги для оплаты подписки на группу \"${sub.group.searchName}\". Вы были отписаны от этой группы")
            telegramService.removeUserFromGroup(sub.group.id, sub.userId!!)
        }
    }
}