package com.assignment.requests

import java.util.UUID

//Request message send to OperationsRouter

case class CreateUserRequest(userName:String)
case class SubscribeToChannelRequest(userId:UUID, channelId:UUID)
case class CreateChannelRequest(channelName:String)
case class BroadCastMessageRequest(channelId:UUID, message:String)