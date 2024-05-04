package com.raif.subscribemanager.models

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Entity
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val subId: Int = 0,
    var status: String = "IN_PROGRESS"
)

@Repository
interface PaymentRepository : JpaRepository<Payment, Int> {
    fun findAllByStatus(status: String): List<Payment>
    fun findBySubIdAndStatus(subId: Int, status: String): Payment?

}