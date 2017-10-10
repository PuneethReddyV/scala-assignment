package com.assignment.actors.util

import akka.actor.{ Actor, Props, ActorLogging }
import com.assignment.data._
import com.assignment.responses._
import com.assignment.requests._
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.HashMap
import scala.collection.mutable.TreeSet
import scala.collection.mutable.SortedSet

/*
 *  CreateChannelRequest, BroadCastMessageRequest messages are processed.
 * 
 */

object ChannelUtilityActor{
	def props(): Props = Props(new ChannelUtilityActor)
}

class ChannelUtilityActor extends Actor with ActorLogging{
	val creationResponse  = "%s created and messages are published through %s Phone number."
			val broadCastResponse = "BoradCasting %s %s"

			override def receive = {
	  //create a new channel
			case msg : Channel =>{ 
				if(!DataStore.channels.contains( (ele:Channel) => ele.id.equals(msg.id)))
				{
					DataStore.channels += msg
							sender ! Response(String.format(creationResponse, msg.name, msg.phoneNumber.getOrElse("UNKNOWN")));
					updateList(PhoneNumber(msg.phoneNumber.getOrElse("UNKNOWN")), msg.id)
				}
			}
			//Broadcast a message with a channel id.
			case msg : BroadCastInfomation => 
			{
				val channel = DataStore.channels.find( (ele : Channel) => ele.id == msg.channelId ) match {
				case Some(v) => {
				  //get user names subscribed to this channel
					val userListNeedBeBoradCasted = findUserList(v)
							broadCasting(userListNeedBeBoradCasted, msg.message, v)
  					  DataStore.channelStats += (msg.channelId -> userListNeedBeBoradCasted.size)
							sender ! Response(String.format(broadCastResponse, "sucessful,"," to all subscribed users to this channel."))
				}
				case None => sender ! Response(String.format(broadCastResponse, "failed,"," no valid channel found"))
				}
			}

			case message => {
				log.info("{} received unknown message = {} ", context.self.path, message)
				unhandled(message)
			}

	}

	//Update a phone numbers associated to Channels when a new Channel is created.
	def updateList(searchId: PhoneNumber, channelId: UUID) = {
			DataStore.phoneToChannelUuid.find(p => p._1 == searchId) match {
			case Some(v) => v._2 += channelId
			case None => {
				    val data = new ListBuffer[UUID]()
						data += channelId
						DataStore.phoneToChannelUuid += searchId -> data
			}
			}
	}

	//get user names who are subscribed to a channel 
	def findUserList(channel : Channel) : SortedSet[String] = {
			// get Channel Phone Number and find channels using this phone number 
			val userListNeedBeBoradCasted = new TreeSet[UUID]()
					DataStore.phoneToChannelUuid.get(PhoneNumber(channel.phoneNumber.get)) match{
			  case Some(list) => list.foreach(channelUuid =>{
						val userData =  DataStore.usersToChannelUuid
								.filter(record => record._2.contains(channelUuid))
								.foreach(record => {
									if(userListNeedBeBoradCasted.contains(record._1)){
										log.info("User {} broadcasted for one or more channels with {} phone number", 
												DataStore.users.find((ele:User) => ele.id == record._1).get.name,
												DataStore.channels.find((ele:Channel) => ele.id == channelUuid).get.phoneNumber
												);
									}else{
										userListNeedBeBoradCasted += record._1
									}
								})
					})
			  case None => 
			}
			
					return userListNeedBeBoradCasted.collect({
					  case uuid : UUID => DataStore.users.find((u: User) => u.id == uuid).get.name
					  case _ => "Unknown_User_Name"
					})
	}

	//Boradcast a message
	def broadCasting(userSet : SortedSet[String], msg: String, channel : Channel) = 
			userSet
			.map(userName => 
			  println("\""+userName+"\" got message \""+msg+"\" from Channel \""+channel.name+"\" with phone number \""+channel.phoneNumber.get+"\"."))
}
