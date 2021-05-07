package com.choidh.karrot.market.home

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.choidh.karrot.market.databinding.ItemProductBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ProductAdapter() :
    ListAdapter<ProductModel, ProductAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(articleItem: ProductModel) {
            val sdf = SimpleDateFormat("MM월 dd일")
            val date = Date(articleItem.timestamp)
            val dec = DecimalFormat("#,###")
            binding.titleTextView.text = articleItem.title
            binding.dateTextView.text = sdf.format(date).toString()
            binding.priceTextView.text = "${dec.format(articleItem.price)}원"

            if (articleItem.imageUrl.isNotEmpty()) {
                /* 이미지뷰에서 scaleType에 centercrop을 주어도 같은 효과가 나타나지만
                 이미지뷰에서 centercrop을 하는것은 이미지를 받아온다음 해당 이미지를 centercrop을 하는것이고
                 transform으로 하는것은 미리 centercrop을 한 이미지를 셋팅하는것임*/
                Glide.with(binding.thumbnailImageView)
                    .load(articleItem.imageUrl)
                    .transform(CenterCrop() , RoundedCorners(dpToPx(binding.thumbnailImageView.context, 12)))
                    .into(binding.thumbnailImageView)
            }

//            binding.root.setOnClickListener {
//                onItemClicked(articleItem)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    /* RoundedCorners에는 값이 픽셀로 들어가야한다. 30dp로 넣어도 실제로 30dp로 들어가지 않는다.
      핸드폰마다 해상도가 다르기때문에 dp라는 개념은 핸드폰 해상도에 맞게 dp값이 들어가지만
     RoundedCorners에 30이라고 넣게되면 실제로는 30pixel로 들어가게되서 핸드폰마다 다르게 보이게됨*/
    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ProductModel>() {
            // 두 아이템이 동일한 아이템인지 체크
            // 보통 아이템의 고유한 값을 가지고 비교함
            override fun areItemsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
                return oldItem.productId == newItem.productId
            }

            // 두 아이템이 동일한 내용물을 가지고 있는지 체크 (equals 비교)
            // areItemsTheSame 가 true일때만 호출됨
            override fun areContentsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}