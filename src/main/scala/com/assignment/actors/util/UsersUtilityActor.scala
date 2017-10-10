package com.assignment.actors.util

import java.util.UUID

import scala.collection.mutable.ListBuffer

import com.assignment.responses.Response

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala

import com.assignment.data._
import com.assignment.requests.BroadCastMessageRequest


object UsersUtilityActor{
	def props(): Props = Props(new UsersUtilityActor)
}

/*
 * 	createUserRequest,  subscribeToChannelRequest messages are processed.
 */

class UsersUtilityActor extends Actor with ActorLogging{

	val createUserResponse  = "%s %s"
			val followingResponse = "\"%s\" started Following \"%s\", operation %s."

			override def receive = {

			case msg : User => {
				//check for any existing users
				if(!DataStore.users.contains( (ele:User) => ele.id.equals(msg.id))){
					sender ! Response(String.format(createUserResponse, msg.name, " user is Created successfully."));
					DataStore.users += msg
							DataStore.usersToChannelUuid += msg.id -> new ListBuffer[UUID]
				}else{
					sender ! Response(String.format(createUserResponse, msg.name, " user exists, operation failed."));
				}
			}
			case message : Following => {
				//get user name
				val userName = DataStore.users.find(p=>p.id == message.userId) match {
				case Some(v) => v.name
				case None => "Unknown"
				}
				//check for channel availability 
				DataStore.channels.find( e => e.id == message.channelId) match{
				case Some(v) => {
					// is valid subscription
					if(!getSubscribedPhoneNumbersUuid(message.userId).contains(message.channelId)){
						val suscribingPhoneNumer = v.phoneNumber.get
								//is new subscription conflicting with existing phone numbers
								if(isConflicting(suscribingPhoneNumer, message.userId, getSubscribedPhoneNumber)){
									//getting conflicting channel 
									val conflictingChannel = DataStore.channels.find((ch : Channel) => ch.phoneNumber.get.equals(suscribingPhoneNumer))
											/*
											 * using the conflicting channel check whether to get a new phone number 
											 * case Boolean => No broad casting happened on EITHER of the channels, so NO_BALANCING_REQUIRED
											 * case UUID => One of the channels needs to be changed BALANCING_REQUIRED
											 * case String => here we request for new phone number only if channels are used for broadcasting BALANCING_REQUIRED
											 * case Int => End up nowhere.
											 */
											(isChannelBalancingRequired(message.channelId, conflictingChannel.get.id))	match {
											case b : Boolean => subscribeToNewChannel(message)
											case uuid : UUID => {
												val newPhoneNumer = channelLeastUsedSoFar match{
												case Some(channel) => channel.phoneNumber
												case None => Option.apply(DataStore.requestForNewPhoneNumber)
												}
												val oldPhoneNumber = DataStore.channels.find(p => p.id == uuid).get.phoneNumber
														if(newPhoneNumer != newPhoneNumer){
															DataStore.channels.find(p => p.id == uuid).get.phoneNumber = newPhoneNumer
																	sender ! BroadCastMessageRequest(uuid, "Phone Number used by this channel UUID = "+uuid+" is updated to "+newPhoneNumer+" from "+oldPhoneNumber)  
														}
												subscribeToNewChannel(message)
											} 
											case s : String => {

												if(DataStore.channelStats.get(message.channelId).getOrElse(-1) != 0){
													val newPhoneNumer = Option.apply(DataStore.requestForNewPhoneNumber)
															val oldPhoneNumber = DataStore.channels.find(p => p.id == message.channelId).get.phoneNumber
															DataStore.channels.find(p => p.id == message.channelId).get.phoneNumber = newPhoneNumer
															sender ! BroadCastMessageRequest(message.channelId, 
																	"Phone Number used by this channel UUID = "+message.channelId+" is updated to "+newPhoneNumer+" from "+oldPhoneNumber)  
												}
												subscribeToNewChannel(message)
											}case i : Int => {
												log.info("arrived at nowhere")
											}
									}
								}else{
									subscribeToNewChannel(message)
								}
						sender ! Response(String.format(followingResponse, userName, v.name, "successful."));
					}else{
						sender ! Response(String.format(followingResponse, userName, " this \""+message.channelId+"\" UUID ", "failed as user already subscribed."));
					}
				}case None => {
					sender ! Response(String.format(followingResponse, userName, " this \""+message.channelId+"\" UUID ", "failed as no such channel available"));
				}
				}
			}
			case message => log.info("{} received unknown message = {} ", context.self.path, message)
					unhandled(message)
	}

			//Get all subscribed channel UUID of a User
			val getSubscribedPhoneNumbersUuid = (uuid : UUID) => DataStore.usersToChannelUuid.get(uuid).get

			
	    //Get all subscribed channel phone number by a user
	    val getSubscribedPhoneNumber = (uuid: UUID) => getSubscribedPhoneNumbersUuid(uuid)
			.map( uuid => DataStore.channels.find((p: Channel) => p.id == uuid).get.phoneNumber.get)  

			//Check for any conflict 
			val isConflicting = (subscribingPhNumber: String, uuid : UUID, f: (UUID => ListBuffer[String]) ) => 
			f(uuid).contains(subscribingPhNumber)

			//Subscribe a new channel by a user 
			val subscribeToNewChannel = (message : Following) => DataStore.usersToChannelUuid.find(p => p._1 == message.userId)  match {
			case None => DataStore.usersToChannelUuid(message.userId) += message.channelId
			case Some(v) => DataStore.usersToChannelUuid(v._1) = v._2 += message.channelId
			}

			val  isChannelBalancingRequired = (subscried : UUID, subscrbing : UUID ) => {
				if( (DataStore.channelStats.get(subscried).getOrElse(-1) == 0 ) && (DataStore.channelStats.get(subscrbing).getOrElse(-1) == 0 )) 0
				else if( (DataStore.channelStats.get(subscried).getOrElse(-1)) > (DataStore.channelStats.get(subscrbing).getOrElse(-1)) ) subscrbing
				else if( (DataStore.channelStats.get(subscried).getOrElse(-1)) < (DataStore.channelStats.get(subscrbing).getOrElse(-1)) ) subscried
				else if( (DataStore.channelStats.get(subscried).getOrElse(-1)) == (DataStore.channelStats.get(subscrbing).getOrElse(-1)) ) "equal"
				else -1
			}

			//Find a Channel which is not used for broadcasting and channel is still supports users i.e. which is having minimum dependency than threshold
			def channelLeastUsedSoFar() : Option[Channel] = DataStore.channels
					.dropWhile(p => DataStore.channelStats.exists(ele => ele._1 == p.id && ele._2 > DataStore.threasholdToSupportUsersPerChannel))
					.headOption match {
					case Some(v) => Option.apply(v)
					case None => Option.empty 
			}
}