package dev.kaccelero.commons.messaging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MessagingServiceTest {

    private lateinit var service: MessagingService
    private lateinit var fakeExchange: IMessagingExchange
    private lateinit var fakeQueue: IMessagingQueue
    private lateinit var fakeKey: IMessagingKey
    private lateinit var fakeUseCase: IHandleMessagingUseCase
    private lateinit var scope: CoroutineScope

    @BeforeTest
    fun setup() {
        fakeExchange = object : IMessagingExchange {
            override val exchange = "test-exchange"
        }
        fakeQueue = object : IMessagingQueue {
            override val queue = "test-queue"
        }
        fakeKey = object : IMessagingKey {
            override val key = "test-key";
            override val isMultiple = false
        }
        fakeUseCase = object : IHandleMessagingUseCase {
            override suspend fun invoke(input1: IMessagingKey, input2: String) {}
        }
        scope = TestScope()
        service = MessagingService(
            host = "localhost",
            user = "guest",
            password = "guest",
            exchange = fakeExchange,
            queue = fakeQueue,
            keys = listOf(fakeKey),
            handleMessagingUseCaseFactory = { fakeUseCase },
            coroutineScope = scope,
            autoConnect = false,
            autoListen = false
        )
    }

    @Test
    fun testRoutingKeyReturnsCorrectKey() {
        val result = service.routingKey("test-key")
        assertEquals(fakeKey, result)
    }

    @Test
    fun testRoutingKeyThrowsOnInvalidKey() {
        assertFailsWith<IllegalStateException> { service.routingKey("invalid") }
    }

    @Test
    fun testConnectionAndChannelInitiallyNull() {
        assertNull(service.connection)
        assertNull(service.channel)
    }

    @Test
    fun testPublishThrowsIfChannelNotInitialized() = runTest {
        val exception = assertFailsWith<IllegalStateException> {
            service.publish(fakeKey, "value")
        }
        assertTrue(exception.message!!.contains("Channel is not initialized"))
    }

    @Test
    fun testAutoConnectSetsUpConnectionAndChannel() = runBlocking {
        var connectCalled = false
        var listenCalled = false
        val testService = object : MessagingService(
            host = "localhost",
            user = "guest",
            password = "guest",
            exchange = fakeExchange,
            queue = fakeQueue,
            keys = listOf(fakeKey),
            handleMessagingUseCaseFactory = { fakeUseCase },
            coroutineScope = this,
            autoConnect = true,
            autoListen = true
        ) {
            override suspend fun connect() {
                connectCalled = true
                channelReady.complete(Unit) // Manually complete the Deferred to unblock the test
                setupCompleted.complete(Unit) // Manually complete the Deferred to unblock the test
            }

            override suspend fun listen() {
                listenCalled = true
            }
        }
        testService.setupCompleted.await()
        assertTrue(connectCalled, "connect should be called when autoConnect is true")
        assertTrue(listenCalled, "listen should be called when autoListen is true")
    }

    @Test
    fun integrationTestCreatesResourcesOnAMQPServer() = runBlocking {
        val integrationExchange = object : IMessagingExchange {
            override val exchange = "integration-test-exchange"
        }
        val integrationQueue = object : IMessagingQueue {
            override val queue = "integration-test-queue"
        }
        val integrationKey = object : IMessagingKey {
            override val key = "integration-test-key"
            override val isMultiple = false
        }
        val integrationService = MessagingService(
            host = "localhost",
            user = "guest",
            password = "guest",
            exchange = integrationExchange,
            queue = integrationQueue,
            keys = listOf(integrationKey),
            handleMessagingUseCaseFactory = { fakeUseCase },
            coroutineScope = this,
            autoConnect = false,
            autoListen = false,
            dead = true,
            maxXDeathCount = 3,
        )
        integrationService.connect()
        integrationService.setup()
        assertNotNull(integrationService.connection, "Connection should not be null after connect")
        assertNotNull(integrationService.channel, "Channel should not be null after connect")

        // Try to declare the exchange and queue passively to ensure they exist
        integrationService.channel!!.exchangeDeclarePassive(integrationExchange.exchange)
        integrationService.channel!!.exchangeDeclarePassive(integrationExchange.exchange + "-dlx")
        integrationService.channel!!.exchangeDeclarePassive(integrationExchange.exchange + "-dead")
        integrationService.channel!!.queueDeclarePassive(integrationExchange.exchange + "-dlx")
        integrationService.channel!!.queueDeclarePassive(integrationExchange.exchange + "-dead")
        integrationService.channel!!.queueDeclarePassive(integrationQueue.queue)

        // Cleanup
        integrationService.channel!!.exchangeDelete(integrationExchange.exchange)
        integrationService.channel!!.exchangeDelete(integrationExchange.exchange + "-dlx")
        integrationService.channel!!.exchangeDelete(integrationExchange.exchange + "-dead")
        integrationService.channel!!.queueDelete(integrationQueue.queue)
        integrationService.channel!!.queueDelete(integrationExchange.exchange + "-dlx")
        integrationService.channel!!.queueDelete(integrationExchange.exchange + "-dead")

        Unit // avoid returning the last expression (would make the test not trigger)
    }

}
