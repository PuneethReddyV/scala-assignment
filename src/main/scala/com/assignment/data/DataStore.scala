package com.assignment.data

import scala.collection.mutable.{ListBuffer => MList}
import scala.collection.immutable.HashMap
import java.util.UUID

object DataStore {
	    var phoneNumberCount = 0
			val channels =  new MList[Channel]()
			val users =  new MList[User]()
			val followings =  new MList[Following]()
			val phoneNumbers =  new MList[PhoneNumber]()

			//additional data structures is used to avoid computations 
			var phoneToChannelUuid = new HashMap[PhoneNumber, MList[UUID]]
			var usersToChannelUuid = scala.collection.mutable.Map[UUID, MList[UUID]]()
			var channelStats = new HashMap[UUID, Int]

			phoneNumbers+=(PhoneNumber(requestForNewPhoneNumber))
			
			//If the number of users depending on a channel is more than this threshold value then we request for a new phone number.
			val threasholdToSupportUsersPerChannel = 1

							def requestForNewPhoneNumber():String ={ 
									phoneNumberCount = phoneNumberCount + 1
											f"$phoneNumberCount%05d" 
							}
}
