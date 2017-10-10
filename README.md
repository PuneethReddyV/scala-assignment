# Scala-assignment
Ploblem Statement
1.	Channels are a mechanism for broadcasting messages.
2.	Messages can be sent a variety of ways, including via SMS.
3.	People follow one or more channels, and receive messages when channels they follow broadcast a message.
4.	Some followers have email, some have SMS, some have both.
5.	Channels can have a phone number so that when SMS messages are sent from a channel, the user can see a phone number for that channel. The user can respond and we know to which channel they are responding because the phone number is associated with that channel.
6.	Phone numbers cost money, and so they are a scarce resource. If they were free, every channel would be assigned a discrete phone number upon creation.
7.	For this reason, more than one channel can use the same number. This is fine, as long as no two channels with the same phone number are followed by same person. If one person is following two channels that both use the same phone number, when that person sends and SMS message to that shared phone number, we won't know to which channel they are communicating. When one person is following two channels that both have the same phone number, this is called a "Collision".
8.	Numbers are allocated on demand, that is, the first time that a broadcast for that channel needs to go via SMS.
9.	If phone number X is being used for both Channel A and Channel B, and no followers of either follow the other, then we are fine. As soon as a follower of A starts to follow B, we must change the phone number of one of the channels.
10.	When the phone number of a channel changes, we need to communicate to the channel users that the number has changed. It is very disruptive to change a number, and so we must minimize the number of users that are impacted. For this reason, we must take into consideration the number of recent SMS messages each channel has sent to determine which channel will be impacted the least by a change, and choose that channel to change the number.

Data model<br>
case class Channel(id: UUID, name: String, phoneNumber: Option[String])<br>
case class User(id: UUID, name: String)<br>
case class Following(channelId: UUID, UserId: UUID)<br>
case class PhoneNumber(number: String)<br>

Don’t worry about persistence for now. Maybe just use a mutable List to store the collections of objects for now.

object DataStore {
	import scala.collection.mutable.{List => MList}
		val Channels: MList = _
		val Users: MList = _
		val Followings: MList = _
		val PhoneNumbers: MList = _
	}

You can populate the data store with random data.

If you find that this data model is insufficient, please augment as needed. Also, this model is missing broadcasts and broadcast statistics. If you need data that is not represented here, as in the case of broadcast statistics, create placeholder functions that returns random data for now.

Objective
1.	Build a mechanism in Scala that assigns to channels and changes phone numbers as needed.
2.	Minimize the number of phone numbers allocated.
3.	Do not allow collisions.
4.	Minimize the impact of a channel phone number change.
5.	Minimize the number of potential channel phone number changes by balancing the phone number allocations such that least used phone numbers are the first to be allocated when a new channel needs a number.

