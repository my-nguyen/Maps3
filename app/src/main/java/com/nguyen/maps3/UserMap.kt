package com.nguyen.maps3

import java.io.Serializable

data class UserMap(val title: String, val places: List<Place>): Serializable
