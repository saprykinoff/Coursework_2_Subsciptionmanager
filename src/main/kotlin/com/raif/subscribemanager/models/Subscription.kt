package com.raif.subscribemanager.models

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*


@Entity
@Table(name = "subs")
class Subscription (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    var qrId: String = "",
    var qrUrl: String = "",
    var userId: Long = 0,
    var inviteLink: String = "",
    var nextPayment: Date? = null
)


@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Int> {

}