package com.raif.subscribemanager.services

import com.raif.subscribemanager.errors.*
import com.raif.subscribemanager.models.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class DataLayer(
    private val groupEntityRepository: GroupEntityRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentRepository: PaymentRepository,
) {
    fun registerGroup(
        chatId: Long,
        ownerId: Long,
        name: String?,
        price: Double = 100.0,
        period: Int = 60
    ): GroupEntity {
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
        val newGroup = GroupEntity(chatId, name, price, period, ownerId)
        groupEntityRepository.saveAndFlush(newGroup)
        return newGroup
    }

    fun findGroup(name: String?): GroupEntity? {
        if (name == null) return null
        return groupEntityRepository.findBySearchName(name)
    }


    fun getSub(subId: Int): Subscription? {
        return subscriptionRepository.findById(subId).getOrNull()
    }

    fun saveSub(sub: Subscription) {
        subscriptionRepository.saveAndFlush(sub)
    }

    fun deleteSub(id: Int) {
        subscriptionRepository.deleteById(id)
    }

    fun getSubByLink(link: String): Subscription? {
        return subscriptionRepository.findByInviteLink(link)
    }

    fun getExpiredSubs(): List<Subscription> {
        return subscriptionRepository.findAllByNextPaymentBeforeAndStatusNot(Date.from(Instant.now()), "DEAD")
    }

    fun getUnpaidSubs(): List<Subscription> {
        return subscriptionRepository.findAllByNextPaymentIsNullAndStatusNot("DEAD")
    }

    fun getUserSubs(userId: Long): List<Subscription> {
        return subscriptionRepository.findUserSubs(userId)
    }


    fun getActivePays(): List<Payment> {
        return paymentRepository.findAllByStatus("IN_PROGRESS")
    }

    fun savePay(pay: Payment) {
        paymentRepository.saveAndFlush(pay)
    }

    fun createPay(subId: Int): Payment {
        return paymentRepository.saveAndFlush(Payment(subId = subId))
    }

    fun getPay(id: Int): Payment? {
        return paymentRepository.findById(id).getOrNull()
    }

    fun hasActivePay(subId: Int): Boolean {
        return paymentRepository.findBySubIdAndStatus(subId, "IN_PROGRESS") != null
    }
}
