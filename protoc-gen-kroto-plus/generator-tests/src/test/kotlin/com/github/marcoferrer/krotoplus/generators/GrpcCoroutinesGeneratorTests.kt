/*
 * Copyright 2019 Kroto+ Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.marcoferrer.krotoplus.generators

import com.github.marcoferrer.krotoplus.coroutines.launchProducerJob
import com.github.marcoferrer.krotoplus.coroutines.withCoroutineContext
import io.grpc.examples.helloworld.*
import io.grpc.testing.GrpcServerRule
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.toList
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.BeforeTest

@UseExperimental(ObsoleteCoroutinesApi::class)
class GrpcCoroutinesGeneratorTests {

    @[Rule JvmField]
    var grpcServerRule = GrpcServerRule().directExecutor()

    val expectedMessage = "result"

    @BeforeTest
    fun setupService(){
        grpcServerRule.serviceRegistry.addService(object : GreeterCoroutineGrpc.GreeterImplBase(){

            override val initialContext: CoroutineContext
                get() = Dispatchers.Default

            override suspend fun sayHello(request: HelloRequest): HelloReply {
                return HelloReply { message = expectedMessage }
            }

            override suspend fun sayHelloClientStreaming(requestChannel: ReceiveChannel<HelloRequest>): HelloReply {
                return HelloReply {
                    message = requestChannel.toList().joinToString(separator = "|"){ it.name }
                }
            }

            override suspend fun sayHelloServerStreaming(
                request: HelloRequest,
                responseChannel: SendChannel<HelloReply>
            ) {
                repeat(3){
                    responseChannel.send { message = request.name + "-$it" }
                }
            }

            override suspend fun sayHelloStreaming(
                requestChannel: ReceiveChannel<HelloRequest>,
                responseChannel: SendChannel<HelloReply>
            ) {
                requestChannel.consumeEach { request ->
                    repeat(3) {
                        responseChannel.send { message = request.name }
                    }
                }
            }
        })
    }

    @Test
    fun `Unary rpc methods are generated`() = runBlocking {
        val stub = GreeterCoroutineGrpc.newStub(grpcServerRule.channel)
        assertEquals(expectedMessage,stub.sayHello(HelloRequest.getDefaultInstance()).message)
        assertEquals(expectedMessage,stub.sayHello().message)
        assertEquals(expectedMessage,stub.sayHello { name = "test" }.message)
    }

    @Test
    fun `Client streaming rpc methods are generated`() = runBlocking {
        val stub = GreeterCoroutineGrpc.newStub(grpcServerRule.channel)
            .withCoroutineContext()

        val (requestChannel, response) = stub.sayHelloClientStreaming()

        launchProducerJob(requestChannel){
            repeat(3){
                send { name = "name $it" }
            }
        }
        assertEquals("name 0|name 1|name 2",response.await().message)
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    @Test
    fun `Server streaming rpc methods are generated`() = runBlocking {
        val stub = GreeterCoroutineGrpc.newStub(grpcServerRule.channel)
            .withCoroutineContext()

        // Default Value Ext
        val response1 = stub.sayHelloServerStreaming()
        repeat(3){
            assertEquals("-$it",response1.receive().message)
        }
        assertNull(response1.receiveOrNull())
        assert(response1.isClosedForReceive)

        // Single Message Parameter Ext
        val response2 = stub.sayHelloServerStreaming(HelloRequest { name = "with-arg" })
        repeat(3){
            assertEquals("with-arg-$it",response2.receive().message)
        }
        assertNull(response2.receiveOrNull())
        assert(response2.isClosedForReceive)

        // Message Builder Ext
        val response3 = stub.sayHelloServerStreaming { name = "with-block" }
        repeat(3){
            assertEquals("with-block-$it",response3.receive().message)
        }
        assertNull(response3.receiveOrNull())
        assert(response3.isClosedForReceive)
    }

    @Test
    fun `Bidi streaming rpc methods are generated`() {
        runBlocking {

            val stub = GreeterCoroutineGrpc.newStub(grpcServerRule.channel)
                .withCoroutineContext()

            val (requestChannel, responseChannel) = stub.sayHelloStreaming()

//            launchProducerJob(requestChannel) {
            launch(Dispatchers.Default) {
                repeat(3) {
                    requestChannel.send { name = "name $it" }
                }
                requestChannel.close()
            }

            val results = responseChannel.toList()
            println(results)
            assertEquals(9, results.size)

            val expected = "name 0|name 0|name 0" +
                    "|name 1|name 1|name 1" +
                    "|name 2|name 2|name 2"
            assertEquals(
                expected,
                results.joinToString(separator = "|") { it.message }
            )
        }
    }
}