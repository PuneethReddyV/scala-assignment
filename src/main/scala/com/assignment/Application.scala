package com.assignment

import akka.actor.{ActorRef, Inbox, Props, ActorSystem}
import java.util.UUID
import com.assignment.actors.util._
import com.assignment.actors._
import com.assignment.requests._
import scala.io.Source
import scala.util.control.Exception.Catch
import com.assignment.data.DataStore
import scala.util.control.Breaks._


object Application extends App{
	    val system = ActorSystem.create("messaging-system")
			val channelUtilActor = system.actorOf(ChannelUtilityActor.props, "channelUtilActor")
			val userUtilActor = system.actorOf(UsersUtilityActor.props, "userUtilActor")
			val operationActor = system.actorOf(OperationsRouter.props(userUtilActor, channelUtilActor), "operations")
			var i = -1

			//Menu options
			while(true){
				try{
					menu
					var input = Console.readInt 
					input match {
					case 1 => {
						println("Enter User Name")
						operationActor ! CreateUserRequest(Console.readLine)     
					}
					case 2 =>{
						println("Enter Channel Name")
						operationActor ! CreateChannelRequest(Console.readLine)  
					}
					case 3 =>{
						println("Select a user Name for subscription")
						i = -1
						//Print all user name for selecting a user
						DataStore.users.foreach(f => {
							i= i + 1 
									println(i+" ."+f.name)
						})
						input = Console.readInt
						if(DataStore.users.apply(input) != null ){
							println("Pick Channel to subscribe")
							i = -1
							//Print all channels name for selecting a channel
							DataStore.channels.foreach(f => {
								i= i + 1 
										println(i+" ."+f.name)
							})
							val userId = DataStore.users.apply(input).id
							input = Console.readInt
							if(DataStore.channels.apply(input) != null ){
								operationActor ! createSubscriptionRequest(userId,(DataStore.channels.apply(input).id))
							}else {
								throw new IllegalArgumentException("Invalid Input")
							}
						}else{
							throw new IllegalArgumentException("Invalid Input")
						}
					}
					case 4 =>
					{
						println("Pick Channel to broadcast a message")
						i = -1		
						//Print all channels name for selecting a channel
						DataStore.channels.foreach(f => {
							i= i + 1 
									println(i+" ."+f.name)
						})
						input = Console.readInt
						if(DataStore.channels.apply(input) != null ){
							val channelId = DataStore.channels.apply(input).id;
							println("Enter message to broadcast")
							operationActor ! createBroadCastingRequest(channelId, Console.readLine)
						}else {
							throw new IllegalArgumentException("Invalid Input")
						}
					}
					case 5 => {
						DataStore.channels.foreach(println)
						DataStore.users.foreach({
							ele => println(ele+", following => "+DataStore.usersToChannelUuid.find(p => p._1 == ele.id).get.
									_2.map(ele => DataStore.channels.find(c => c.id == ele).get.name).mkString(","))
						})
					}
					case 6 =>{
						println("Thank you for using our broadcasting system.")
						system.shutdown
						break
					}
					case _ => print("Invalid input")
					}


				}catch{
				case ex : IllegalArgumentException =>{
					println(ex.getMessage)
				}case e : Exception => {
					println(e.getMessage)
				}
				}
			}

	def menu () = {
			println("Enter your choice")
			println("1. Add User.")
			println("2. Add Channel.")
			println("3. Subscribe to a channel.")
			println("4. BroadCast a message.")
			println("5. Print stats")
			println("6. Exit.")
	}

	def createSubscriptionRequest(userId: UUID, channelId : UUID) = SubscribeToChannelRequest(userId, channelId);
	def createBroadCastingRequest(channelId: UUID, message : String) = BroadCastMessageRequest(channelId, message)


}