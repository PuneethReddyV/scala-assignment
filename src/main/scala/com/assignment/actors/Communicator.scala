package com.assignment.actors

import akka.actor.{Actor, ActorLogging, Props, ActorRef, Terminated }

class Communicator extends Actor with ActorLogging {
  def receive: Actor.Receive = {
	   case msg : Any => print("nothing..")
  }
}