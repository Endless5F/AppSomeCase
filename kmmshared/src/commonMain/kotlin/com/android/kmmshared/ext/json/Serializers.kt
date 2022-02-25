package com.baidu.searchbox.vision.kmm.common.json

import co.touchlab.stately.concurrency.AtomicBoolean
import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.AtomicLong
import co.touchlab.stately.concurrency.value
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StringToAtomicIntSerializer : KSerializer<AtomicInt> {
    override fun deserialize(decoder: Decoder): AtomicInt = AtomicInt(decoder.decodeString().toIntOrNull() ?: 0)

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kmm.StringToAtomicIntSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: AtomicInt) = encoder.encodeString(value.get().toString())
}

object IntToBooleanSerializer : KSerializer<Boolean> {
    override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() > 0

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kmm.IntToBooleanSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeInt(if (value) 1 else 0)
}

object IntToAtomicBooleanSerializer : KSerializer<AtomicBoolean> {
    override fun deserialize(decoder: Decoder): AtomicBoolean = AtomicBoolean(decoder.decodeInt() > 0)

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kmm.IntToAtomicBooleanSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: AtomicBoolean) = encoder.encodeInt(if (value.value) 1 else 0)
}

object IntToAtomicIntSerializer : KSerializer<AtomicInt> {
    override fun deserialize(decoder: Decoder): AtomicInt = AtomicInt(decoder.decodeInt())

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kmm.IntToAtomicIntSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: AtomicInt) = encoder.encodeInt(value.value)
}
object LongToAtomicLongSerializer : KSerializer<AtomicLong> {
    override fun deserialize(decoder: Decoder): AtomicLong = AtomicLong(decoder.decodeLong())

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kmm.LongToAtomicLongSerializer", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: AtomicLong) = encoder.encodeLong(value.value)
}