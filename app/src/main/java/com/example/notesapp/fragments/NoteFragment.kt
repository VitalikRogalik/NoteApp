package com.example.notesapp.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.MainActivity
import com.example.notesapp.R
import com.example.notesapp.adapters.RvNotesAdapter
import com.example.notesapp.databinding.FragmentNoteBinding
import com.example.notesapp.utils.SwipeToDelete
import com.example.notesapp.utils.hideKeyboard
import com.example.notesapp.viewmodels.NoteViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NoteFragment : Fragment() {

    private lateinit var noteBinding: FragmentNoteBinding
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val rvAdapter by lazy {RvNotesAdapter()}
    private var mBundleRecyclerViewState: Bundle? = null
    private var mListState: Parcelable? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        //exit animation. Нужна чтобы при переходе на следующий фрашмент предыдущий не сразу
        // же удалился (отрисуется на фоне белый экран), а отрисовал анимацию ухода назад (MaterialElevationScale).
        // Также можно отрисовать просто удержание (Hold)
        exitTransition = MaterialElevationScale(false).apply {
            duration = 300L
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = 300L
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        noteBinding = FragmentNoteBinding.inflate(inflater, container, false)
        return noteBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //откладываем отрисовку до момента вызова в fun observerDataChanges() в наблюдателе
        // если отрисовать не в observer анимация может сломаться
        postponeEnterTransition()

        val activity = activity as MainActivity
        val navController = Navigation.findNavController(view)

        //extension fun
        requireView().hideKeyboard()

        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            //activity.window.statusBarColor = Color.WHITE не надо

            //чтобы можно было менять status bar color
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9E9D9D")
        }
        addListeners(navController)

        recyclerViewDisplay()
        swipeToDelete(noteBinding.rvNote)

        /*view.doOnPreDraw { startPostponedEnterTransition() }*/
    }

    override fun onPause() {
        super.onPause()
        mBundleRecyclerViewState = Bundle()
        mListState = noteBinding.rvNote.layoutManager?.onSaveInstanceState()
        mBundleRecyclerViewState!!.putParcelable("KEY_RECYCLER_STATE", mListState)
    }

    private fun swipeToDelete(rvNote: RecyclerView) {

        val swipeToDeleteCallback = object : SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                Log.d("tag1", rvAdapter.currentList.size.toString())
                //позиция элемента
                val position = viewHolder.absoluteAdapterPosition
                val note = rvAdapter.currentList[position]
                noteViewModel.deleteNote(note)

                noteBinding.search.apply {
                    hideKeyboard()
                    clearFocus()
                }

                observerDataChanges()

                val snackBar = Snackbar.make(
                    requireView(), "Note Deleted", Snackbar.LENGTH_LONG
                )

                snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {
                        super.onShown(transientBottomBar)

                        transientBottomBar?.setAction("Undo"){
                            Log.d("tag2", rvAdapter.currentList.size.toString())
                            noteViewModel.saveNote(note)
                            noteBinding.noData.isVisible = false

                            // откладываем обновление чтобы заметка успела сохранится
                            val handler = Handler()
                            handler.postDelayed(Runnable { observerDataChanges() }, 100L)
                        }
                    }
                }).apply {
                    animationMode = Snackbar.ANIMATION_MODE_FADE
                    //привязываем snackBar к fab добаления заметки
                    setAnchorView(R.id.fab_add_note)
                }
                snackBar.setActionTextColor(
                    ContextCompat.getColor(requireContext(), R.color.yellowOrange)
                )
                snackBar.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvNote)
    }

    private fun observerDataChanges() {
        noteViewModel.noteListLiveData.observe(viewLifecycleOwner){ listOfNotes->
            noteBinding.noData.isVisible = listOfNotes.isEmpty()
            rvAdapter.submitList(listOfNotes)

            restoreRvState()
            // начинаем отложенную отрисовку
            (view?.parent as? ViewGroup)?.doOnPreDraw {  startPostponedEnterTransition() }
        }

        noteViewModel.getAllNote()
    }

    private fun restoreRvState() {
        if (mBundleRecyclerViewState != null) {

            mListState = mBundleRecyclerViewState!!.getParcelable("KEY_RECYCLER_STATE")
            noteBinding.rvNote.layoutManager?.onRestoreInstanceState(mListState)
        }
    }

    private fun recyclerViewDisplay(){
        when(resources.configuration.orientation){
            Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerView(2)
            Configuration.ORIENTATION_LANDSCAPE -> setUpRecyclerView(3)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {

        noteBinding.rvNote.apply{
            layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            adapter = rvAdapter
            setHasFixedSize(true)
        }
        observerDataChanges()
    }

    private fun addListeners(navController: NavController){

        //навигация с fab
        noteBinding.fabAddNote.setOnClickListener{
            noteBinding.appBarLayout.visibility = View.INVISIBLE

            navController.navigate(
                NoteFragmentDirections
                    .actionNoteFragmentToSaveOrUpdateFragment())
        }

        //навигация с fab
        noteBinding.innerFab.setOnClickListener{
            noteBinding.appBarLayout.visibility = View.INVISIBLE

            navController.navigate(
                NoteFragmentDirections
                    .actionNoteFragmentToSaveOrUpdateFragment())
        }

        //implementing search here
        noteBinding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                noteBinding.noData.isVisible = false
            }

            override fun onTextChanged(sequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (sequence.toString().isNotEmpty()){
                    noteBinding.clearEditText.visibility = View.VISIBLE
                    val text = sequence.toString()
                    val query = "%$text%"
                    //if (query.isNotEmpty())
                    noteViewModel.searchNote(query).observe(viewLifecycleOwner){
                        rvAdapter.submitList(it)
                    }
                    //else observerDataChanges()
                }else{
                    noteBinding.clearEditText.visibility = View.GONE
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        //Убирает фокус с search view и клавиатуру при нажатии кнопки поиска
        noteBinding.search.setOnEditorActionListener{ view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                view.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }

        //Измененине fab при скролле
        //Предпоследний параметр oldScrollX, но тут они не нужны
        noteBinding.rvNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            when{
                scrollY>oldScrollY -> noteBinding.fabText.isVisible = false
                scrollX==scrollY ->  noteBinding.fabText.isVisible = true
                else ->  noteBinding.fabText.isVisible = true
            }
        }

        noteBinding.clearEditText.setOnClickListener{
            noteBinding.search.text.clear()
        }
    }
}