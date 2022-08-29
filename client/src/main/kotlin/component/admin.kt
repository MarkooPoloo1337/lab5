package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.Props
import react.dom.*
import react.fc
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.dom.Link
import react.useContext
import react.useRef
import ru.altmanea.edu.server.model.*
import ru.altmanea.edu.server.model.Config.Companion.adminPath
import userInfo
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface AdminProps : Props {
    var users: List<User>
    var deleteUser: (Int) -> Unit
    var addUser: (String, String) -> Unit
}

fun fcAdmin() = fc("Admin") { props: AdminProps ->
    val usernameRef = useRef<INPUT>()
    val passwordRef = useRef<INPUT>()
    if (props.users.isEmpty()) {
        p {
            +"у вас недостаточно прав"
        }
    } else {
        span {
            p {
                +"Имя нового пользователя"
                input {
                    ref = usernameRef
                }
            }
            p {
                +"Пароль"
                input {
                    ref = passwordRef
                }
            }
            button {
                +"Добавить пользователя"
                attrs.onClickFunction = {
                    usernameRef.current?.value?.let { username ->
                        passwordRef.current?.value?.let { password ->
                            props.addUser(username, password)
                        }
                    }
                }
            }
            p {
                +"Вы не можете добавлять и удалять пользвателей типа admin"
            }
        }
        h3 {
            +"Список пользователей"
        }
        table {
            tr {
                th {
                    +"Имя пользователя"
                }
                th {
                    +"Пароль"
                }
                th {}
            }
            props.users.mapIndexed { index, user ->
                tr {
                    td {
                        +user.username
                    }
                    td {
                        +user.password
                    }
                    td {
                        button {
                            +"x"
                            attrs.onClickFunction = {
                                props.deleteUser(index)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun fcContainerAdmin() = fc("ContainerAdmin") { _: Props ->
    val queryClient = useQueryClient()
    val token = useContext(userInfo)?.second
    val authHeader = "Authorization" to token
    val queryUser = useQuery<String, QueryError, String, String>(
        "users",
        {
            fetchText(
                adminPath,
                jso { headers = json(authHeader) }
            )
        },
        options = jso {
            refetchOnWindowFocus = false
        }
    )
    val deleteUserMutation = useMutation<Any, Any, Any, Any>(
        { index: Int ->
            axios<String>(jso {
                url = "${adminPath}/$index"
                method = "Delete"
                headers = json(authHeader)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("users")
            }
        }
    )
    val addUserMutation = useMutation<Any, Any, Any, Any>(
        { user: User ->
            axios<String>(jso {
                url = adminPath
                method = "Post"
                headers = json(
                    "Content-Type" to "application/json",
                    authHeader
                )
                data = Json.encodeToString(user)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("users")
            }
        }
    )
    if (queryUser.isLoading) div { +"Loading .." }
    else if (queryUser.isError) div { +"Error!" }
    else {
        val userItem: List<User> =
            Json.decodeFromString(queryUser.data ?: "")
        child(fcAdmin()) {
            attrs.users = userItem
            attrs.deleteUser = {
                deleteUserMutation.mutate(it, null)
            }
            attrs.addUser = { u, p ->
                addUserMutation.mutate(User(u, p), null)
            }
        }
    }
}