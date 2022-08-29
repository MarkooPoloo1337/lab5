package ru.altmanea.edu.server.rest

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import roleAdmin
import ru.altmanea.edu.server.model.User
import roleUser
import ru.altmanea.edu.server.auth.authorizedRoute
import ru.altmanea.edu.server.model.Config.Companion.adminPath
import userList
import userRoles

fun Route.admin() {
    route(adminPath) {
        authenticate("auth-jwt") {
            authorizedRoute(setOf(roleAdmin)) {
                get {
                    if (userList.isNotEmpty()) {
                        call.respond(userList)
                    } else {
                        call.respondText("No users found", status = HttpStatusCode.NotFound)
                    }
                }
                delete ("{index}") {
                    val index = call.parameters["index"] ?: return@delete call.respondText(
                        "Missing or malformed id",
                        status = HttpStatusCode.BadRequest
                    )
                    val user = userList[index.toInt()]
                    val userRole = userRoles.filter { it.key == user }.toList()[0]
                    if (userRole.second.contains(roleAdmin)) {
                        call.respondText("User removed correctly", status = HttpStatusCode.Accepted)
                    }
                    else {
                        userList = userList - user
                        call.respondText("User removed correctly", status = HttpStatusCode.Accepted)
                    }
                }
                post {
                    val user = call.receive<User>()
                    userList = userList + user
                    userRoles = userRoles + Pair(user, setOf(roleUser))
                    call.respondText("Lesson stored correctly", status = HttpStatusCode.Created)
                }
            }
        }
        get ("/roles") {
            if (userRoles.isNotEmpty()) {
                call.respond(userRoles)
            } else {
                call.respondText("No roles found", status = HttpStatusCode.NotFound)
            }
        }
    }
}