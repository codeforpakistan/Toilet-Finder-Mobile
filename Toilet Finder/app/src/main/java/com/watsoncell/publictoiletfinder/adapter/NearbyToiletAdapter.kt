package com.watsoncell.publictoiletfinder.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.watsoncell.publictoiletfinder.R
import com.watsoncell.publictoiletfinder.models.NearByToiletDTO
import com.watsoncell.publictoiletfinder.utils.Constant

class NearbyToiletAdapter(
    ctx: Context,
    interfaceListener: NearbyToiletInterface,
    nearbyToilets: ArrayList<NearByToiletDTO>
) :
    RecyclerView.Adapter<NearbyToiletAdapter.NearbyToiletViewHolder>() {

    private val context = ctx
    private val nearbyToiletList = nearbyToilets
    private val nearByToiletListener: NearbyToiletInterface = interfaceListener

    private val TOILET_IMAGES =
        arrayOf(
            R.drawable.img_toilet_1,
            R.drawable.img_toilet_2,
            R.drawable.img_toilet_3,
            R.drawable.img_toilet_4,
            R.drawable.img_toilet_5,
            R.drawable.img_toilet_6,
            R.drawable.img_toilet_7
        )
    private var IMG_COUNTER = 0

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): NearbyToiletViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_row_nearby_toilet, viewGroup, false)

        val height = viewGroup.measuredHeight
        view.minimumHeight = height - 14

        return NearbyToiletViewHolder(view)
    }

    override fun getItemCount(): Int {
        return nearbyToiletList.size
    }

    override fun onBindViewHolder(holder: NearbyToiletViewHolder, position: Int) {

        Constant.nearByToiletDetail = nearbyToiletList[position]

        holder.tvToiletName.text = nearbyToiletList[position].title
        holder.tvCityName.text = nearbyToiletList[position].city
        holder.imgToilet.setImageResource(TOILET_IMAGES[IMG_COUNTER])
        holder.imgToilet.tag = TOILET_IMAGES[IMG_COUNTER]

        holder.tvToiletDistance.text = "Distance - ${nearbyToiletList[position].distance} km"

        //item view click listener
        holder.itemView.setOnClickListener {

            Constant.toiletImageResoureId = holder.imgToilet.tag as Int
            Constant.nearByToiletDetail = nearbyToiletList[position]
            nearByToiletListener.OnNearByToiletClickListener(position)
        }

        IMG_COUNTER++
        if (IMG_COUNTER == TOILET_IMAGES.size)
            IMG_COUNTER = 0
    }

    class NearbyToiletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgToilet = itemView.findViewById<ImageView>(R.id.imgNearbyToilet)
        val tvToiletName = itemView.findViewById<TextView>(R.id.tvToiletName)
        val tvToiletDistance = itemView.findViewById<TextView>(R.id.tvToiletDistance)
        val tvCityName = itemView.findViewById<TextView>(R.id.tvToiletCity)
    }

    interface NearbyToiletInterface {
        fun OnNearByToiletClickListener(position: Int)
    }
}
