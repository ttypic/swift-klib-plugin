package com.ttypic.filehasher

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

actual object FileHasherFactory {
    actual fun createMd5Hasher(): FileHasher = FileMd5Hasher
}

object FileMd5Hasher : FileHasher {
    override fun hash(data: ByteArray): String {
        return BigInteger(1, MessageDigest.getInstance("MD5").digest(data))
            .toString(16)
            .padStart(32, '0')
    }
}


