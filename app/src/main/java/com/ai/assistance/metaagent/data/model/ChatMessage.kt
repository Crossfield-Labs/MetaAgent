package com.ai.assistance.metaagent.data.model

import android.os.Parcel
import android.os.Parcelable
import com.ai.assistance.metaagent.util.stream.Stream
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ChatMessage(
        val sender: String, // "user" or "ai"
        var content: String = "",
        val timestamp: Long = System.currentTimeMillis(),
        val roleName: String = "", // 角色名字字段
        val provider: String = "", // 供应商
        val modelName: String = "", // 模型名称
        @Transient
        var contentStream: Stream<String>? =
                null, // 修改为Stream<String>类型，与EnhancedAIService.sendMessage返回类型匹配
        /** 非空时表示这条消息是一个编排树卡片，值为 TaskSession.taskId */
        val planSessionId: String? = null
) : Parcelable {

    /** 是否为编排树卡片消息 */
    val isPlanCard: Boolean get() = planSessionId != null

    constructor(
            parcel: Parcel
    ) : this(
        parcel.readString() ?: "", 
        parcel.readString() ?: "", 
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        null,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sender)
        parcel.writeString(content)
        parcel.writeLong(timestamp)
        parcel.writeString(roleName)
        parcel.writeString(provider)
        parcel.writeString(modelName)
        // 不需要序列化contentStream，因为它是暂时性的
        parcel.writeString(planSessionId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatMessage> {
        override fun createFromParcel(parcel: Parcel): ChatMessage {
            return ChatMessage(parcel)
        }

        override fun newArray(size: Int): Array<ChatMessage?> {
            return arrayOfNulls(size)
        }
    }
}

