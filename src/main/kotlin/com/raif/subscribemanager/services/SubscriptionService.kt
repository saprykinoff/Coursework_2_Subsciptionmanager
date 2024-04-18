package com.raif.subscribemanager.services

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

@Service
class SubscriptionService(
    private val dataLayer: DataLayer,
    private val telegramService: TelegramService
) {
    @Scheduled(fixedDelay = 1000)
    fun manage() {
        for (e in dataLayer.getExpiredSubs()) {
            //TODO пытаться оплатить. Если не получается, удалять подписку и пользователя

        }
        for (sub in dataLayer.getUnpaidSubs()) {
            if (Instant.now().isBefore(sub.createdDate.toInstant().plusSeconds(10))) {
                telegramService.sendMessage(sub.createdByUserId, "Подписка созданная в ${sub.createdDate} больше не активна") //TODO указать имя группы
                dataLayer.deleteSub(sub.id)
                continue
            }
            if (false) { //TODO проверка подписки
                telegramService.sendMessage(sub.createdByUserId, "Подписка успешно оформлена") //TODO указать имя группы
            }
        }

    }
}