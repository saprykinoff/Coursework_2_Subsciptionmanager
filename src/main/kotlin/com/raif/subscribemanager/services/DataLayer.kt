package com.raif.subscribemanager.services

import com.raif.subscribemanager.errors.*
import com.raif.subscribemanager.models.*
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class DataLayer(
    private val groupEntityRepository: GroupEntityRepository,
    private val utilityService: UtilityService,
    private val subscriptionRepository: SubscriptionRepository
) {
    fun registerGroup(chatId: Long, ownerId: Long, name: String?, price: Double = 100.0, period: Int= 60): GroupEntity {
        if (name == null || name == "") {
            throw EmptyGroupNameError()
        }
        val group = groupEntityRepository.findById(chatId).getOrNull()
        if (group != null && group.ownerId != ownerId) {
            throw GroupAlreadyRegisteredError()
        }
        if (groupEntityRepository.findBySearchName(name) != null) {
            throw GroupNameAlreadyExistsError()
        }
        val newGroup = GroupEntity(chatId, name, price,period, ownerId)
        groupEntityRepository.saveAndFlush(newGroup)
        return newGroup
    }

    fun findGroup(name: String?): GroupEntity? {
        if (name == null) return null
        return groupEntityRepository.findBySearchName(name)
    }

    fun createSubscribe(name: String, url: String): Subscription? {
        val sub = Subscription()
        subscriptionRepository.saveAndFlush(sub)
        val res = utilityService.createSubscriptionQr(sub.id, name)
        if (res == null) {
            subscriptionRepository.deleteById(sub.id)
            return null
        }

        sub.qrId = res.getJSONObject("qr").getString("id")
        sub.qrUrl = res.getJSONObject("qr").getString("url")
        println("qrId: ${sub.qrId}")
        println("qrUrl: ${sub.qrUrl}")
        sub.inviteLink = url
        subscriptionRepository.saveAndFlush(sub)
        return sub
    }

}
