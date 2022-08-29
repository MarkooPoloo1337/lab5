import ru.altmanea.edu.server.model.Role
import ru.altmanea.edu.server.model.User

val userAdmin = User("admin","admin")
val userTutor = User("tutor", "tutor")
var userList = listOf(userAdmin, userTutor)

val roleAdmin = Role("admin")
val roleUser = Role("user")
var userRoles = mapOf(
    userAdmin to setOf(roleAdmin, roleUser),
    userTutor to setOf(roleUser)
)