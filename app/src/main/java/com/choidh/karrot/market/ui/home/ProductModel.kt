package com.choidh.karrot.market.home

data class ProductModel(
    val productId: Long,
    val sellerId: String,
    val timestamp: Long,
    val title: String,
    val price: Int,
    val description: String,
    val imageUrl: String
) {
    constructor() : this(0, "", 0, "", 0, "", "")
}