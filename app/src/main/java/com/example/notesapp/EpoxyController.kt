package com.example.notesapp


import com.airbnb.epoxy.TypedEpoxyController
import com.example.notesapp.models.ImageModel
import com.example.notesapp.viewmodels.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

class EpoxyController(private val viewModel: NoteViewModel): TypedEpoxyController<List<ImageModel>>() {

    override fun buildModels(data: List<ImageModel>){
        data.forEachIndexed{ i, item ->
            addImage(item, i)
        }
    }


    /*private fun addImage(item: ImageModel) {

        image {
            id(item.imageUri.toString())
            imageModel(item)
        }
    }*/

    private fun addImage(item: ImageModel, index: Int) {
        val sdf = SimpleDateFormat("ddMyyyyhhmmss")
        val currentDate = sdf.format(Date()).toInt()

        val model = ImageModelBinding_(viewModel).id(item.hashCode()).position(index)
        model.imageModel = item
        model.addTo(this)
    }

}