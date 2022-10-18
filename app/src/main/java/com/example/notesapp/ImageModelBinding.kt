package com.example.notesapp

import android.util.Log
import androidx.databinding.ViewDataBinding
import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.example.notesapp.databinding.ViewHolderImageBinding
import com.example.notesapp.models.ImageModel
import com.example.notesapp.viewmodels.NoteViewModel

@EpoxyModelClass
abstract class ImageModelBinding(private val viewModel: NoteViewModel): DataBindingEpoxyModel(){
    @EpoxyAttribute
    var position: Int? = null

    var imageModel: ImageModel? = null

    override fun getDefaultLayout(): Int {
        return R.layout.view_holder_image
    }

    override fun setDataBindingVariables(binding: ViewDataBinding?) {
        binding as ViewHolderImageBinding
        binding.imageModel = imageModel
        binding.etNoteImageText.setOnFocusChangeListener { _, hasFocus->
            if (hasFocus){
                viewModel.editTextPosition = position!!
                Log.d("tag1", "focus ${position}")
            }
        }
    }


}