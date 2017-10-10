package com.assignment.actors

import akka.actor.{Actor, ActorLogging, Props, ActorRef, Terminated }
import com.assignment.requests._
import com.assignment.data._
import com.assignment.responses._
import com.assignment.actors.util._
import java.util.UUID

/*
 * Routes the incoming message to corresponding actors 
 * Example :- 
 * 	createUserRequest,  subscribeToChannelRequest requests are routed to UsersUtilityActor
 * 	CreateChannelRequest, BroadCastMessageRequest requests are routed to ChannelUtilityActor
 */

object OperationsRouter{
	def props(userUtilActor : ActorRef, channelUtilActor : ActorRef) : Props = Props(new OperationsRouter(userUtilActor, channelUtilActor))
}

class OperationsRouter(userUtilActor : ActorRef, channelUtilActor : ActorRef) extends Actor with ActorLogging {
    
  val r = scala.util.Random
	
  override def preStart(): Unit = {
			log.info("Operations actor started")
	}

	def receive: Actor.Receive = {
	   case msg : CreateUserRequest => 
	      userUtilActor ! User(UUID.randomUUID(), msg.userName)
	   case msg : CreateChannelRequest =>
	      val randPhNum  = DataStore.phoneNumbers.apply(r.nextInt(DataStore.phoneNumbers.size)).number
	      channelUtilActor ! Channel(UUID.randomUUID(), msg.channelName, Option.apply(randPhNum))
	   case msg : SubscribeToChannelRequest => 
	       userUtilActor ! Following(msg.channelId, msg.userId)
	   case msg : BroadCastMessageRequest => 
	       channelUtilActor ! BroadCastInfomation(msg.channelId, msg.message)
	   case msg : Response => 
	       log.info(msg toString)   
	    case message => log.info("{} received unknown message = {} ", context.self.path, message)
	        unhandled(message)
			}
}
