package com.raif.subscribemanager.models

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*


@Entity
@Table(name = "subs")
class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    var qrId: String = "",
    var qrUrl: String = "",
    var userId: Long? = null,
    var createdByUserId: Long = 0,
    var inviteLink: String = "",
    var nextPayment: Date? = null,
    @Column(
        insertable = false,
        updatable = false,
        columnDefinition = "TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP"
    )
    var createdDate: Date? = null,
    @ManyToOne
    var group: GroupEntity = GroupEntity(),
    var status: String = "INACTIVE", //INACTIVE - подписка создана не оплачена, ACTIVE - подписка оплачена, DEAD - подписка аннулированна
)


@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Int> {
    @Query("select s FROM Subscription s where (s.userId = ?1 or s.createdByUserId = ?1) and s.status<>\"DEAD\"")
    fun findUserSubs(userId: Long): List<Subscription>
    fun findByInviteLink(link: String): Subscription?
    fun findAllByNextPaymentBeforeAndStatusNot(date: Date, status: String): List<Subscription>
    fun findAllByNextPaymentIsNullAndStatusNot(status: String): List<Subscription>

}