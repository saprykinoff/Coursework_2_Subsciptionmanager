package com.raif.subscribemanager.models

import jakarta.persistence.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "groups")
class GroupEntity (
    @Id
    val id: Long = 0,

    @Column
    var searchName: String = "",

    @Column
    var price: Double = 0.0,

    @Column
    var period: Int = 1,

    @Column
    var ownerId: Long = 0,
)

@Repository
interface GroupEntityRepository : JpaRepository<GroupEntity, Long> {

}
