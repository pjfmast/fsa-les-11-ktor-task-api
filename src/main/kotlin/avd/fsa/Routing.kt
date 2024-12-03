package avd.fsa

import avd.fsa.model.FakeTaskRepository
import avd.fsa.model.Priority
import avd.fsa.model.Task
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        staticResources("static", "static")

        route("/tasks") {
            get {
                val tasks = FakeTaskRepository.allTasks()
                call.respond(tasks)
            }

            get("/byName/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val task = FakeTaskRepository.taskByName(name)
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(task)
            }

            get("/byPriority/{priority}") {
                val priorityAsText = call.parameters["priority"]
                if (priorityAsText == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val priority = Priority.valueOf(priorityAsText)
                    val tasks = FakeTaskRepository.tasksByPriority(priority)

                    if (tasks.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(tasks)
                } catch (_: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            post {
                try {
                    val task = call.receive<Task>()
                    FakeTaskRepository.addTask(task)
                    call.respond(HttpStatusCode.Created)
                } catch (_: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (_: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            put {
                try {
                    val task = call.receive<Task>()
                    FakeTaskRepository.updateTask(task)
                    call.respond(HttpStatusCode.OK)
                } catch (_: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (_: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            delete("/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }

                if (FakeTaskRepository.removeTask(name)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

    }
}