package com.example.staydesk.data.model

import java.util.UUID

data class Category(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var imageUrl: String = ""
)
