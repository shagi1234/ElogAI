package com.selbiconsulting.elog.data.storage.remote

class Endpoints {
    companion object {
        var shared = Endpoints()
        const val username = "test_dunyasi"
        const val password = "qa1234567"
    }

    //username:  Mekan_Ag
    //password:  Pikir2024!


    //    val BASE_URL = "https://elog--sit.sandbox.my.salesforce-sites.com"
    val BASE_URL = "https://elog.my.salesforce-sites.com"

    val sendMsg = "$BASE_URL/services/apexrest/chat-messaging"
    val logs = "$BASE_URL/services/apexrest/logs"
    val getMsg = "$BASE_URL/services/apexrest/chat-messaging"
    val login = "$BASE_URL/services/apexrest/custom-auth"
    val driverInfo = "$BASE_URL/services/apexrest/driver-info"
    val location = "$BASE_URL/services/apexrest/location"
    val file = "$BASE_URL/services/apexrest/file"
    val dvir = "$BASE_URL/services/apexrest/dvirs"
    val device = "$BASE_URL/services/apexrest/driver-device"

//    https://elog.my.salesforce-sites.com/services/apexrest/file?contactId=003PC00000ATr1hYAD&fileId=068PC000008zD8bYAE
}