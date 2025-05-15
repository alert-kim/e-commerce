package kr.hhplus.be.server.domain.user

import jakarta.persistence.*
import kr.hhplus.be.server.domain.user.exception.RequiredUserIdException
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
) {
    fun id(): UserId {
        return id?.let { UserId(it) } ?: throw RequiredUserIdException()
    }
}
