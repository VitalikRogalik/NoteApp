package com.example.notesapp.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R

abstract class SwipeToDelete: ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
){

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val icon = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_baseline_delete_outline_24)
        icon!!.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY)

        val iconMargin = 60
        val iconTop: Int = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom: Int = iconTop + icon.intrinsicHeight

        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX>0) {
                var iconLeft = itemView.left + iconMargin
                var iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                val iconWidth = iconRight - iconLeft + 2*iconMargin

                if (dX> iconWidth){
                    iconLeft = itemView.left + iconMargin + (dX- iconWidth).toInt()
                    iconRight = itemView.left + iconMargin + icon.intrinsicWidth + (dX- iconWidth).toInt()
                }
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            }else if(dX<0){
                var iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                var iconRight = itemView.right - iconMargin
                val iconWidth = iconRight - iconLeft + 2*iconMargin

                if (dX < -iconWidth){
                    iconLeft = itemView.right - iconMargin - icon.intrinsicWidth + (dX + iconWidth).toInt()
                    iconRight = itemView.right - iconMargin + (dX + iconWidth).toInt()
                }
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            }

            icon.draw(c)
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}