package com.raif.subscribemanager.services

import com.raif.subscribemanager.models.GroupEntity
import com.raif.subscribemanager.models.GroupEntityRepository
import com.raif.subscribemanager.models.InvitationLinkRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class DataLayer(
    private val invitationLinkRepository: InvitationLinkRepository,
    private val groupEntityRepository: GroupEntityRepository,
) {
    fun registerGroup(chatId: Long, ownerId: Long, name: String, price: Double = 100.0, period: Int= 60): String? {

        val group = groupEntityRepository.findById(chatId).getOrNull()
        if (group != null && group.ownerId != ownerId) {
            return "Группа уже зарегестрирована другим владельцем"
        }
        val newGroup = GroupEntity(chatId, name, price,period, ownerId)
        groupEntityRepository.saveAndFlush(newGroup)
        return null
    }

}
