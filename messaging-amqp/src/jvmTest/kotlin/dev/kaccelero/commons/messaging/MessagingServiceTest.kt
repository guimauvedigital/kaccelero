package dev.kaccelero.commons.messaging

import dev.kaccelero.serializers.Serialization
import dev.kourier.amqp.AMQPResponse
import io.ktor.callid.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

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
            override suspend fun invoke(
                input1: IMessagingKey,
                input2: String,
                input3: AMQPResponse.Channel.Message.Delivery,
            ) {
            }
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

    @Test
    fun integrationTestPublishAndListen() = runBlocking {
        val integrationExchange = object : IMessagingExchange {
            override val exchange = "integration-test-exchange-pubsub"
        }
        val integrationQueue = object : IMessagingQueue {
            override val queue = "integration-test-queue-pubsub"
        }
        val integrationKey = object : IMessagingKey {
            override val key = "integration-test-key-pubsub"
            override val isMultiple = false
        }
        val received = CompletableDeferred<TestMessage>()
        val testValue = TestMessage("hello-world-${System.currentTimeMillis()}")
        val integrationService = MessagingService(
            host = "localhost",
            user = "guest",
            password = "guest",
            exchange = integrationExchange,
            queue = integrationQueue,
            keys = listOf(integrationKey),
            handleMessagingUseCaseFactory = {
                object : IHandleMessagingUseCase {
                    override suspend fun invoke(
                        input1: IMessagingKey,
                        input2: String,
                        input3: AMQPResponse.Channel.Message.Delivery,
                    ) {
                        if (input1.key == integrationKey.key) received.complete(
                            Serialization.json.decodeFromString(input2)
                        )
                    }
                }
            },
            coroutineScope = this,
            autoConnect = true,
            autoListen = true,
        )

        // Publish and ensure we receive the message
        integrationService.publish(integrationKey, testValue)
        val result = withTimeoutOrNull(5.seconds) { received.await() }
        assertEquals(testValue, result)

        // Cleanup
        integrationService.channel!!.exchangeDelete(integrationExchange.exchange)
        integrationService.channel!!.queueDelete(integrationQueue.queue)

        Unit // avoid returning the last expression (would make the test not trigger)
    }

    @Test
    fun integrationTestRetryAndDeadLetter() = runBlocking {
        val integrationExchange = object : IMessagingExchange {
            override val exchange = "integration-test-exchange-dlx"
        }
        val integrationQueue = object : IMessagingQueue {
            override val queue = "integration-test-queue-dlx"
        }
        val integrationKey = object : IMessagingKey {
            override val key = "integration-test-key-dlx"
            override val isMultiple = false
        }
        val testValue = TestMessage("dlx-test-${System.currentTimeMillis()}")
        val receivedOnDead = CompletableDeferred<TestMessage>()
        val failCount = 2 // must be >= maxXDeathCount to trigger dead
        var invocationCount = 0
        val integrationService = MessagingService(
            host = "localhost",
            user = "guest",
            password = "guest",
            exchange = integrationExchange,
            queue = integrationQueue,
            keys = listOf(integrationKey),
            handleMessagingUseCaseFactory = {
                object : IHandleMessagingUseCase {
                    override suspend fun invoke(
                        input1: IMessagingKey,
                        input2: String,
                        input3: AMQPResponse.Channel.Message.Delivery,
                    ) {
                        if (input1.key == integrationKey.key) {
                            invocationCount++
                            throw RuntimeException("Force DLX/DeadLetter for test")
                        }
                    }
                }
            },
            coroutineScope = this,
            autoConnect = true,
            autoListen = true,
            dead = true,
            maxXDeathCount = failCount,
        )

        // Publish a message that will fail and go to DLX/dead
        integrationService.publish(integrationKey, testValue)

        // Listen on the dead-letter queue for the failed message
        val deadLetterQueue = integrationExchange.exchange + "-dead"
        val deadJob = launch {
            integrationService.channel!!.basicConsume(
                queue = deadLetterQueue,
                noAck = true,
                onDelivery = { delivery ->
                    val msg = Serialization.json.decodeFromString<TestMessage>(delivery.message.body.decodeToString())
                    if (msg == testValue) receivedOnDead.complete(msg)
                }
            )
        }
        val result = withTimeoutOrNull(20.seconds) { receivedOnDead.await() }
        assertEquals(testValue, result, "Message should be dead-lettered after retries")
        assertTrue(invocationCount >= failCount, "Handler should be invoked at least maxXDeathCount times")
        deadJob.cancel()

        // Cleanup
        integrationService.channel!!.exchangeDelete(integrationExchange.exchange)
        integrationService.channel!!.exchangeDelete(integrationExchange.exchange + "-dlx")
        integrationService.channel!!.exchangeDelete(integrationExchange.exchange + "-dead")
        integrationService.channel!!.queueDelete(integrationQueue.queue)
        integrationService.channel!!.queueDelete(integrationExchange.exchange + "-dlx")
        integrationService.channel!!.queueDelete(integrationExchange.exchange + "-dead")

        Unit // avoid returning the last expression (would make the test not trigger)
    }

    @Test
    fun testPublishContainsNoRequestIdIfNotSpecified() = runBlocking {
        val integrationExchange = object : IMessagingExchange {
            override val exchange = "integration-test-exchange-no-request-id"
        }
        val integrationQueue = object : IMessagingQueue {
            override val queue = "integration-test-queue-no-request-id"
        }
        val integrationKey = object : IMessagingKey {
            override val key = "integration-test-key-no-request-id"
            override val isMultiple = false
        }
        val requestIdDeferred = CompletableDeferred<String?>()
        val integrationService = MessagingService(
            host = "localhost",
            user = "guest",
            password = "guest",
            exchange = integrationExchange,
            queue = integrationQueue,
            keys = listOf(integrationKey),
            handleMessagingUseCaseFactory = {
                object : IHandleMessagingUseCase {
                    override suspend fun invoke(
                        input1: IMessagingKey,
                        input2: String,
                        input3: AMQPResponse.Channel.Message.Delivery,
                    ) {
                        requestIdDeferred.complete(currentCoroutineContext()[KtorCallIdContextElement]?.callId)
                    }
                }
            },
            coroutineScope = this,
            autoConnect = true,
            autoListen = true,
        )

        // Publish and ensure headers do not contain X-Request-Id
        integrationService.publish(integrationKey, TestMessage("no-request-id"))
        val capturedRequestId = withTimeoutOrNull(10.seconds) { requestIdDeferred.await() }
        assertEquals(null, capturedRequestId)

        // Cleanup
        integrationService.channel!!.exchangeDelete(integrationExchange.exchange)
        integrationService.channel!!.queueDelete(integrationQueue.queue)

        Unit
    }

    @Test
    fun testPublishContainsOriginalRequestId() = runBlocking {
        val integrationExchange = object : IMessagingExchange {
            override val exchange = "integration-test-exchange-with-request-id"
        }
        val integrationQueue = object : IMessagingQueue {
            override val queue = "integration-test-queue-with-request-id"
        }
        val integrationKey = object : IMessagingKey {
            override val key = "integration-test-key-with-request-id"
            override val isMultiple = false
        }
        val requestIdDeferred = CompletableDeferred<String?>()
        val integrationService = MessagingService(
            host = "localhost",
            user = "guest",
            password = "guest",
            exchange = integrationExchange,
            queue = integrationQueue,
            keys = listOf(integrationKey),
            handleMessagingUseCaseFactory = {
                object : IHandleMessagingUseCase {
                    override suspend fun invoke(
                        input1: IMessagingKey,
                        input2: String,
                        input3: AMQPResponse.Channel.Message.Delivery,
                    ) {
                        requestIdDeferred.complete(currentCoroutineContext()[KtorCallIdContextElement]?.callId)
                    }
                }
            },
            coroutineScope = this,
            autoConnect = true,
            autoListen = true,
        )

        val requestId = "req-" + System.currentTimeMillis()
        withCallId(requestId) {
            integrationService.publish(integrationKey, TestMessage("with-request-id"))
        }
        val capturedRequestId = withTimeoutOrNull(10.seconds) { requestIdDeferred.await() }
        assertEquals(requestId, capturedRequestId)

        // Cleanup
        integrationService.channel!!.exchangeDelete(integrationExchange.exchange)
        integrationService.channel!!.queueDelete(integrationQueue.queue)

        Unit
    }

}
