package com.example.renter_app_g04.adapters

import android.R
import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.renter_app_g04.ClickDetectorInterface
import com.example.renter_app_g04.databinding.PropertyCellBinding
import com.example.renter_app_g04.model.Property


class PropertyAdapter(private var myItems:List<Property>, private val clickInterface: ClickDetectorInterface) : RecyclerView.Adapter<PropertyAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: PropertyCellBinding) : RecyclerView.ViewHolder (binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PropertyCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    // 3. How many items are in the list of data?
    override fun getItemCount(): Int {
        return myItems.size
    }


    // In a single row, what data goes in the <TextView>?
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val property: Property = myItems.get(position)
        Log.d("TESTING", "ON BIND CALLED!")
        Log.d("TESTING", property.propertyAddress)


        val options: RequestOptions = RequestOptions()
            .centerCrop()

        Glide.with(holder.binding.root).load(property.imageUrl).apply(options).into(holder.binding.ivPhoto);

        holder.binding.address.text = property.propertyAddress
        holder.binding.price.text = "$${property.monthlyRentalPrice.toInt()}"
        holder.binding.bedrooms.text = "Number of Bedrooms: ${property.numberOfBedRooms}"

        holder.binding.btnDelete.setOnClickListener {
            clickInterface.deleteRow(position)
        }
    }
}