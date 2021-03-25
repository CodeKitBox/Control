package com.kits.control.items

import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.kits.control.R
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder

class ThirdItem(val index:String, val title:String) : AbstractFlexibleItem<ThirdItem.ViewHolder>() {

    override fun equals(o: Any?): Boolean {
        if (o is ThirdItem){
           return  this.index == o.index
        }
        return false
    }

    override fun getLayoutRes(): Int {
        return R.layout.item_third
    }

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<*>?>?): ViewHolder {
        return ViewHolder(view,adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<*>?>?, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.itemView.setBackgroundColor(Color.rgb(0,0,255))
        holder.tvSimple?.text = title
    }

     class ViewHolder(view: View?, adapter: FlexibleAdapter<*>?) : FlexibleViewHolder(view, adapter){
         val tvSimple = view?.findViewById<TextView>(R.id.tvFirst)
     }
}