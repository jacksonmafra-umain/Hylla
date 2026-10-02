package com.umain.hylla.store

import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.LocalDateSerializer
import com.umain.hylla.fleet.PersonId
import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One change to the fleet, as it was asked for. The journal keeps them in order. Nothing reads
 * them back today; they are the seam a future sync would send from (docs/decisions.md).
 */
@Serializable
sealed interface Operation {
    @Serializable
    @SerialName("claim")
    data class Claim(
        val device: DeviceId,
        val person: PersonId,
        @Serializable(with = LocalDateSerializer::class) val on: LocalDate,
    ) : Operation

    @Serializable
    @SerialName("return")
    data class Return(
        val device: DeviceId,
        @Serializable(with = LocalDateSerializer::class) val on: LocalDate,
    ) : Operation

    @Serializable
    @SerialName("edit")
    data class Edit(val device: DeviceId) : Operation

    @Serializable
    @SerialName("register")
    data class Register(val device: DeviceId) : Operation

    @Serializable
    @SerialName("undo")
    data object Undo : Operation
}
