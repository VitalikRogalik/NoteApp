package com.example

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load

@BindingAdapter("imageUri")
fun bindImage(imgView: ImageView, imgUri: Uri?){
    imgUri?.let {
        //без библиотек
        /*val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        imgView.setImageBitmap(bitmap)*/

        imgView.load(imgUri){
            //иначе крашится на android 11
            allowHardware(false)
            //size(Dimension.Pixels(requireActivity().windowManager.defaultDisplay.width), Dimension.Undefined)
        }
    }
}
