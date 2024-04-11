package com.raif.subscribemanager.models

import jakarta.persistence.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "links")
class InvitationLink(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false)
    val chatId: Long = 0,

    val joinedUserId: Long = 0
)

@Repository
interface InvitationLinkRepository : JpaRepository<InvitationLink, Int> {
    fun findByChatId(chatId: Long, page: Pageable): Page<InvitationLink>
    fun countByChatId(chatId: Long): Int
}