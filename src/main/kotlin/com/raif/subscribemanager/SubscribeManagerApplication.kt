package com.raif.subscribemanager

import com.raif.subscribemanager.services.TelegramService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
class SubscribeManagerApplication

fun main(args: Array<String>) {
    runApplication<SubscribeManagerApplication>(*args)
}

@Configuration
class BotConfig {
    @Bean
    fun telegramBotsApi(bot: TelegramService): TelegramBotsApi =
        TelegramBotsApi(DefaultBotSession::class.java).apply {
            registerBot(bot)
        }
}