package com.example.staydesk.data.model

data class BusinessDetails(
    var hotelName: String = "",
    var hotelType: String = "",
    var starCategory: String = "",
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var pinCode: String = "",
    var phone: String = "",
    var email: String = "",
    var whatsappNumber: String = "",
    var website: String = "",
    var gstNumber: String = "",
    var imageUrls: List<String> = emptyList(),
    var description: String = "",
    var checkInTime: String = "12:00 PM",
    var checkOutTime: String = "11:00 AM",
    var isSetupComplete: Boolean = false
) {
    val propertyName: String get() = hotelName
}
