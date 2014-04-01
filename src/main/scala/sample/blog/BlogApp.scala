package sample.blog

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import akka.contrib.pattern.ClusterSharding

object BlogApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2551", "2552", "0"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)

      ClusterSharding(system).start(
        typeName = AuthorListing.shardName,
        entryProps = Some(AuthorListing.props()),
        idExtractor = AuthorListing.idExtractor,
        shardResolver = AuthorListing.shardResolver)
      ClusterSharding(system).start(
        typeName = Post.shardName,
        entryProps = Some(Post.props(ClusterSharding(system).shardRegion(AuthorListing.shardName))),
        idExtractor = Post.idExtractor,
        shardResolver = Post.shardResolver)

      if (port != "2551" && port != "2552")
        system.actorOf(Props[Bot], "bot")
    }

  }

}

