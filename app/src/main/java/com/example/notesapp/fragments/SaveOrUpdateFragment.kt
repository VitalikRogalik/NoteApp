package com.example.notesapp.fragments

import android.Manifest
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.Runnable
import com.example.notesapp.EpoxyController
import com.example.notesapp.MainActivity
import com.example.notesapp.R
import com.example.notesapp.databinding.BottomSheetLayoutBinding
import com.example.notesapp.databinding.FragmentSaveOrUpdateBinding
import com.example.notesapp.models.ImageModel
import com.example.notesapp.models.Note
import com.example.notesapp.utils.hideKeyboard
import com.example.notesapp.viewmodels.NoteViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import java.text.SimpleDateFormat
import java.util.*

class SaveOrUpdateFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var binding: FragmentSaveOrUpdateBinding
    private var note: Note? = null
    private var color = -1
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date())
    private val args: SaveOrUpdateFragmentArgs by navArgs()
    private lateinit var controller : EpoxyController

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            processPermissionState(isGranted)
        }

    private val getImage =
        registerForActivityResult(ActivityResultContracts.GetContent()){ imageUri ->
            if (imageUri != null){
                updateRecyclerView(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //animation
        sharedElementEnterTransition = MaterialContainerTransform().apply{
            drawingViewId = R.id.nav_host_fragment
            scrimColor = Color.TRANSPARENT
            duration = 300L
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaveOrUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        val activity = activity as MainActivity

        binding.backBtn.setOnClickListener{
            requireView().hideKeyboard()
            navController.popBackStack()
        }

        binding.saveNote.setOnClickListener{
            saveNote()
        }

        binding.addImageButton.setOnClickListener{
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        binding.etNoteContent.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                binding.bottomBar.visibility = View.VISIBLE
            }else{
                binding.bottomBar.visibility = View.GONE
            }
        }

        binding.fabColorPick.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme
            )

            val bottomSheetView: View = layoutInflater.inflate(
                R.layout.bottom_sheet_layout,
                null
            )

            with(bottomSheetDialog){
                setContentView(bottomSheetView)
                show()
            }

            val bottomSheetBinding = BottomSheetLayoutBinding.bind(bottomSheetView)

            bottomSheetBinding.apply {
                colorPicker.apply {
                    setSelectedColor(color)
                    setOnColorSelectedListener {
                        color = it
                        binding.apply {
                            parentView.setBackgroundColor(color)
                            //toolbar.setBackgroundColor(color)
                            bottomBar.setBackgroundColor(color)

                            //цвет строки состояния
                            activity.window.statusBarColor = color
                        }
                        //меняет цвет сразу после выбора с color picker
                        bottomSheetBinding.bottomSheetParent.setCardBackgroundColor(color)
                    }
                }
                //меняет цвет при создании bottom sheet
                bottomSheetParent.setCardBackgroundColor(color)
            }

            bottomSheetView.post{
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        controller = EpoxyController(noteViewModel)
        binding.epoxyRecyclerView.setController(controller)

        setUpNote()
    }

    private fun processPermissionState(isGranted: Boolean) {
        if (isGranted){
            getImage.launch("image/*")
        }else{
            Toast.makeText(
                requireContext(), "Необходимо разрешение на чтение хранилища", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateRecyclerView(imageUri: Uri) {
        val position = if (noteViewModel.dataList.isEmpty()) 0 else noteViewModel.editTextPosition+1

        noteViewModel.dataList.add(position, ImageModel("", imageUri))
        controller.setData(noteViewModel.dataList)
    }

    private fun setUpNote() {
        note = args.note?: Note(date = currentDate)
        color = note?.color ?: -1

        binding.note = note
    }

    private fun saveNote() {
        if (binding.etNoteContent.text.toString().isEmpty()
            && binding.etTitle.text.toString().isEmpty()){

            Toast.makeText(activity, resources.getString(R.string.fields_empty), Toast.LENGTH_LONG).show()
        }else{
            note = args.note
            when(note){
                null -> {
                    noteViewModel.saveNote(
                        Note(
                            id = 0,
                            title = binding.etTitle.text.toString(),
                            content = binding.etNoteContent.text.toString(),
                            date = currentDate.toString(),
                            color = color
                        )
                    )

                    navController.popBackStack()
                }
                else -> {
                    //update note
                    updateNote()
                    navController.popBackStack()
                }
            }
        }
    }

    private fun updateNote() {
        if(note!= null){
            noteViewModel.updateNote(
                Note(
                    id = note!!.id,
                    title = binding.etTitle.text.toString(),
                    content = binding.etNoteContent.text.toString(),
                    date = currentDate,
                    color = color
                )
            )
        }
    }
}