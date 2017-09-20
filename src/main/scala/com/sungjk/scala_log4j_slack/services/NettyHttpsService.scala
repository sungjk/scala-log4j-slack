package com.sungjk.scala_log4j_slack.services

import java.net.ConnectException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.util.concurrent.GenericFutureListener

import scala.concurrent.{Future, Promise}

trait NettyHttpsService {
	val sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build()

	def getHash(data: String, secret: String): String = {
		val sha256_HMAC = Mac.getInstance("HmacSHA256")
		sha256_HMAC.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"))
		sha256_HMAC.doFinal(data.getBytes("UTF-8")).map(char=>f"$char%02x").mkString
	}

	def https[T](host: String, port: Int, method: HttpMethod, path: String, params: Map[String, String], headers: Map[CharSequence, String], contentOpt: Option[Array[Byte]])(handler: FullHttpMessage => T): Future[T] = {
		val promise = Promise[T]()

		try {
			val b = new Bootstrap()
			b.group(new NioEventLoopGroup())
				.channel(classOf[NioSocketChannel])
				.handler(new ChannelInitializer[SocketChannel]() {
					def initChannel(ch: SocketChannel): Unit = {
						val p = ch.pipeline()
						p.addLast(sslCtx.newHandler(ch.alloc()))
						p.addLast(new HttpClientCodec())
						p.addLast(new HttpContentDecompressor())
						p.addLast(new HttpObjectAggregator(65536))
						p.addLast(new SimpleChannelInboundHandler[FullHttpMessage]() {
							def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpMessage): Unit = {
								promise.success(handler(msg))
							}
						})
					}

					override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
						super.exceptionCaught(ctx, cause)
						promise.failure(cause)
					}
				})

			b.option[java.lang.Integer](ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
			b.connect(host, port).addListener(new GenericFutureListener[ChannelFuture]() {
				def operationComplete(future: ChannelFuture): Unit = {
					if (future.isSuccess) {
						val ch = future.sync().channel()

						val encoder = new QueryStringEncoder(path)
						params foreach { p => encoder.addParam(p._1, p._2) }

						val request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, encoder.toString)
						request.headers().set(HttpHeaderNames.HOST, host)
						headers foreach { header =>
							request.headers().set(header._1, header._2)
						}
						contentOpt foreach { content =>
							request.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length)
							request.content().writeBytes(content)
						}

						ch.writeAndFlush(request)
					} else {
						promise.failure(new ConnectException())
					}
				}
			})
		} catch {
			case error: Throwable =>
				promise.failure(error)
		}

		promise.future
	}
}
