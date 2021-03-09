package com.example.mobilecomputinghomework

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.mobilecomputinghomework.databinding.ReminderItemBinding
import com.example.mobilecomputinghomework.db.ReminderInfo

class ReminderAdaptor(context: Context, private  val list:List<ReminderInfo>): BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = ReminderItemBinding.inflate(inflater, parent, false)

        //set payment info values to the list item
        row.txtName.text=list[position].name
        row.txtDate.text=list[position].message
        row.txtTime.text=list[position].creation_time
        return  row.root
    }
    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}
