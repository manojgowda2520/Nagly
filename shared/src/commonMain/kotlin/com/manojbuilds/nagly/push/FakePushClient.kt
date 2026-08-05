package com.manojbuilds.nagly.push

class FakePushClient : PushClient {
    private val tags = linkedMapOf<String, String>()
    var externalId: String? = null
        private set
    var initialized: Boolean = false
        private set
    val log = mutableListOf<String>()

    override fun initialize() {
        initialized = true
        log += "initialize"
    }

    override suspend fun requestPermission(): Boolean {
        log += "requestPermission"
        return true
    }

    override fun setTag(key: String, value: String) {
        tags[key] = value
        log += "setTag:$key=$value"
    }

    override fun setExternalId(id: String) {
        externalId = id
        log += "setExternalId:$id"
    }

    fun tagsSnapshot(): Map<String, String> = tags.toMap()
}
