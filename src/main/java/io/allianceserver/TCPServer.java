package io.allianceserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServer {
    private final int port;
    private static final ConcurrentHashMap<ChannelId, Channel> players = new ConcurrentHashMap<>();

    public TCPServer(int port) {
        this.port = port;
    }

    public TCPServer() {
        this(4568);
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new GameServerHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("ServerTCP avviato sulla porta " + port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class GameServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            players.put(ctx.channel().id(), ctx.channel());
            System.out.println("Nuovo giocatore connesso: " + ctx.channel().remoteAddress());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            System.out.println("Ricevuto: " + msg);
            // Invia il messaggio a tutti i giocatori connessi
            for (Channel ch : players.values()) {
                if (ch != ctx.channel()) {
                    ch.writeAndFlush("Broadcast: " + msg + "\n");
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            players.remove(ctx.channel().id());
            System.out.println("Giocatore disconnesso: " + ctx.channel().remoteAddress());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
