Bug reproduction for: https://github.com/lettuce-io/lettuce-core/issues/1274

Steps:
* Start redis on port `6379` (you can use docker compose file in this repo)
* Run `App.java` from your IDE
* If exception is not reproduced locally, then try to increase/decrease timeout value

Observe first exception:
```
io.netty.util.concurrent.SingleThreadEventExecutor$4 run
WARNING: Unexpected exception from an event executor: 
reactor.core.Exceptions$BubblingException: io.lettuce.core.RedisCommandTimeoutException: Command timed out after 8 millisecond(s)
	at reactor.core.Exceptions.bubble(Exceptions.java:173)
	at reactor.core.publisher.Operators.onErrorDropped(Operators.java:635)
	at reactor.core.publisher.MonoNext$NextSubscriber.onError(MonoNext.java:83)
	at io.lettuce.core.RedisPublisher$OnComplete.run(RedisPublisher.java:1094)
	at io.netty.util.concurrent.DefaultEventExecutor.run(DefaultEventExecutor.java:66)
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)
```

Observe following exceptions and stuck state for all subsequent redis commands:
```
io.lettuce.core.protocol.CommandHandler decode
WARNING: null Unexpected exception during request: java.util.concurrent.RejectedExecutionException: event executor terminated
java.util.concurrent.RejectedExecutionException: event executor terminated
	at io.netty.util.concurrent.SingleThreadEventExecutor.reject(SingleThreadEventExecutor.java:926)
	at io.netty.util.concurrent.SingleThreadEventExecutor.offerTask(SingleThreadEventExecutor.java:353)
	at io.netty.util.concurrent.SingleThreadEventExecutor.addTask(SingleThreadEventExecutor.java:346)
	at io.netty.util.concurrent.SingleThreadEventExecutor.execute(SingleThreadEventExecutor.java:828)
	at io.netty.util.concurrent.SingleThreadEventExecutor.execute(SingleThreadEventExecutor.java:818)
	at io.lettuce.core.RedisPublisher$PublishOnSubscriber.onError(RedisPublisher.java:965)
	at io.lettuce.core.RedisPublisher$State.onError(RedisPublisher.java:711)
	at io.lettuce.core.RedisPublisher$RedisSubscription.onError(RedisPublisher.java:356)
	at io.lettuce.core.RedisPublisher$RedisSubscription.onNext(RedisPublisher.java:292)
	at io.lettuce.core.RedisPublisher$SubscriptionCommand.complete(RedisPublisher.java:781)
	at io.lettuce.core.protocol.CommandHandler.complete(CommandHandler.java:680)
	at io.lettuce.core.protocol.CommandHandler.decode(CommandHandler.java:640)
	at io.lettuce.core.protocol.CommandHandler.channelRead(CommandHandler.java:591)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357)
	at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919)
	at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166)
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:719)
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:655)
	at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:581)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:493)
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.base/java.lang.Thread.run(Thread.java:834)
```