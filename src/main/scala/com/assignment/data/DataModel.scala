package com.assignment.data

import java.util.UUID

//Data Model
case class Channel(id: UUID, name: String, var phoneNumber: Option[String])
case class User(id: UUID, name: String)
case class Following(channelId: UUID, userId: UUID)
case class PhoneNumber(number: String)
case class BroadCastInfomation(channelId : UUID, message: String)

