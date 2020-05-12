package khome.core.events

import assertk.assertThat
import assertk.assertions.isEqualTo
import khome.core.State
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.entities.EntityId
import khome.core.entities.EntitySubject
import khome.core.mapping.ObjectMapper
import khome.observing.AsyncStateObserver
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.get
import java.util.UUID
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalStdlibApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StateChangeEventTest : KhomeTestComponent() {
    private val logger = KotlinLogging.logger { }

    @ExperimentalStdlibApi

    fun `assert callback was subscribed to subject with handle`() {
        class Sut : EntitySubject<String>(EntityId("test", "entity"))

        val sut = Sut()
        sut.registerObserver(UUID.fromString("81e3d1a3-0116-4c7f-87af-668c690f1802"), AsyncStateObserver(EmptyCoroutineContext) { history, new ->
            logger.info { "Old: ${history.getOrNull(0)?.value}" }
            logger.info { "Old: ${new.value}" }
        })

        assertThat(sut.observerCount).isEqualTo(1)
    }

    @ExperimentalStdlibApi

    fun `assert that subscribed event callback was fired`() = runBlocking {
        class Sut : EntitySubject<String>(EntityId("test", "entity"))

        var state: State<String>? = null

        val sut = Sut()
        sut.registerObserver(UUID.fromString("81e3d1a3-0116-4c7f-87af-668c690f1802"), AsyncStateObserver(EmptyCoroutineContext) { old, new ->
            state = new
        })

        val oldStateResponse = """
            {
                "entity_id":"light.bed_light",
                "last_changed":"2016-11-26T01:37:10.466994+00:00",
                "state":"off",
                "attributes":{
                   "supported_features":147,
                   "friendly_name":"Bed Light"
                },
                "last_updated":"2016-11-26T01:37:10.466994+00:00"
             }
        """.trimIndent()

        val newStateResponse = """
            {
                "entity_id":"light.bed_light",
                "last_changed":"2016-11-26T01:37:24.265390+00:00",
                "state":"on",
                "attributes":{
                   "rgb_color":[
                      254,
                      208,
                      0
                   ],
                   "color_temp":380,
                   "supported_features":147,
                   "xy_color":[
                      0.5,
                      0.5
                   ],
                   "brightness":180,
                   "white_value":200,
                   "friendly_name":"Bed Light"
                },
                "last_updated":"2016-11-26T01:37:24.265390+00:00"
             }
        """.trimIndent()
        val oldState: State<String> = get<ObjectMapper>().fromJson(oldStateResponse)
        val newState: State<String> = get<ObjectMapper>().fromJson(newStateResponse)

        sut.state = oldState
        assertThat(oldState).isEqualTo(state)
    }

    @Test
    fun `assert that subscribed event callbacks were fired`() = runBlocking {
    }

    @Test
    fun `assert that unsubscribing was successful`() = runBlocking {
    }
}
