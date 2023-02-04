package com.ttypic.filehasher

interface FileHasher {
    fun hash(data: ByteArray): String
}

expect object FileHasherFactory {
    /**
     * Creates a new instance of [FileHasher]
     */
    fun createMd5Hasher(): FileHasher
}
