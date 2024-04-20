package com.raif.subscribemanager.models

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*


@Entity
@Table(name = "subs")
class Subscription (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    var qrId: String = "",
    var qrUrl: String = "",
    var userId: Long? = null,
    var createdByUserId: Long = 0,
    var inviteLink: String = "",
    var nextPayment: Date? = null,
    var createdDate: Date= Date.from(Instant.now()),
    @ManyToOne
    var group: GroupEntity = GroupEntity(),
    //var status: String = "INACTIVE", //INACTIVE - подписка создана не оплачена, ACTIVE - подписка оплачена, DEAD - подписка аннулированна
)


@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Int> {
    fun findAllByUserId(id: Long): List<Subscription>
    fun findByInviteLink(link: String): Subscription?
    fun findAllByNextPaymentBefore(date: Date): List<Subscription>
    fun findAllByNextPaymentIsNull(): List<Subscription>

}