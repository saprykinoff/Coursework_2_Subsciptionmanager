package com.raif.subscribemanager.models

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "groups")
class GroupEntity(
    @Id
    val id: Long = 0,
    var searchName: String = "",
    var price: Double = 0.0,
    var period: Int = 1,
    var ownerId: Long = 0,
)

@Repository
interface GroupEntityRepository : JpaRepository<GroupEntity, Long> {
    fun findBySearchName(name: String): GroupEntity?

}
