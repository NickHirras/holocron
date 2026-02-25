package holocron.v1.storage

interface FileStorageProvider {
    /**
     * Saves the incoming bytes.
     * @param filename A unique name or path segment for the file (e.g., UUID.jpg)
     * @param bytes The raw file bytes
     * @param contentType The MIME type of the file
     * @return The agnostic URI of the file, e.g., `holocron://assets/my_file.jpg`
     */
    suspend fun save(filename: String, bytes: ByteArray, contentType: String): String

    /**
     * Retrieves the file bytes and content type.
     * @param uri The agnostic URI, e.g., `holocron://assets/my_file.jpg`
     * @return A Pair containing the bytes and the content type, or null if not found
     */
    suspend fun get(uri: String): Pair<ByteArray, String>?
}
