package com.example.sortapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.sortapp.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainFragment : Fragment() {
    private lateinit var bubbleSortJob: Job
    private lateinit var selectionSortJob: Job
    private lateinit var insertionSortJob: Job
    private lateinit var quickSortJob: Job
    private val handler = Handler(Looper.getMainLooper())
    private val viewModel: MyViewModel by activityViewModels()
    private var checkRunning: Int = 0;
    private lateinit var pauseButton: Button
    private lateinit var binding: FragmentMainBinding
    private lateinit var displayLayout: LinearLayout
    private var bubblePosition: Int = 0
    private var insertionPosition1: Int = 1
    private var selectionPosition1: Int = 0
    private var selectionPosition2: Int = 1
    private var checkPause: Boolean = false
    private var bubbleRunning: Boolean = false
    private var selectionRunning: Boolean = false
    private var insertionRunning: Boolean = false
    private var quickRunning: Boolean = false
    private lateinit var seekBar: SeekBar
    private var timeRun: Long = 300
    private var timeWait1: Long = 1000
    private var timeWait2: Long = 500
    private var checkAnimation: Boolean = false
    private var set1 = AnimatorSet()
    private var set2 = AnimatorSet()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(layoutInflater)
        viewModel.dataTextView.value = binding.inputEditText
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputEditText = binding.inputEditText
        seekBar = binding.seekBar
        displayLayout = binding.displayLayout
        val randomButton = binding.RandomButton
        val bubbleSortButton = binding.bubbleSortButton
        val selectionSortButton = binding.selectionSortButton
        viewModel.dataTextView.value = binding.inputEditText
        val insertionSortButton = binding.insertionSortButton
        pauseButton = binding.pauseButton
        bubbleSortJob = GlobalScope.launch { }
        selectionSortJob = GlobalScope.launch { }
        insertionSortJob = GlobalScope.launch { }
        quickSortJob = GlobalScope.launch { }
        val quickSortButton = binding.quickSortButton
        val mergeSortButton = binding.mergeSortButton

        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val inputText = dest.toString() + source.toString()
            if (!isValidInput(inputText)) {
                return@InputFilter ""
            }
            null
        }

        inputEditText.filters = arrayOf(inputFilter)

        binding.inputEditText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                resetState()
                displayNumbers(charSequence.toString())
            }
            override fun afterTextChanged(s: Editable?) {
                resetState()
                // Kiểm tra xem có 2 dấu phẩy liên tiếp hay không
                if (s?.toString()?.contains(",,")?.equals(true) == true) {
                    // Nếu có, loại bỏ dấu phẩy cuối cùng
                    s?.delete(s.length - 1, s.length)
                }
                if (s?.toString()?.isEmpty() == false) {
                    if (s.toString().get(0).equals(',')) {
                        s?.delete(0, 1)
                    }
                }
            }
        })
        randomButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                resetState()
                handleRandomButton(7)
            }
        }

        bubbleSortButton.setOnClickListener {
            if (displayLayout.getChildAt(0) != null && !bubbleRunning ) {
                if (checkRunning == 1 || checkRunning == 0) {
                    bubbleSortJob = GlobalScope.launch(Dispatchers.Main) {
                        bubbleSortAlgorithm(bubblePosition)
                    }
                }
            }
        }

        selectionSortButton.setOnClickListener {
            if (displayLayout.getChildAt(0) != null && !selectionRunning) {
                if (checkRunning == 2 || checkRunning == 0) {
                    selectionSortJob = GlobalScope.launch(Dispatchers.Main) {
                        selectionSortAlgorithm(selectionPosition1, selectionPosition2)
                    }
                }
            }
        }

        insertionSortButton.setOnClickListener {
            if (displayLayout.getChildAt(0) != null && !insertionRunning) {
                if (checkRunning == 3 || checkRunning == 0) {
                    insertionSortJob = GlobalScope.launch(Dispatchers.Main) {
                        insertionSortAlgorithm(insertionPosition1)
                    }
                }
            }
        }

        quickSortButton.setOnClickListener {
            if (!quickRunning && displayLayout.getChildAt(0) != null) {
                quickRunning = true
                if (checkRunning == 4 || checkRunning == 0) {
                    resetAllView(0, viewModel.textViewIndex.value!!.size - 1)
                    checkRunning = 4
                    quickSortJob = GlobalScope.launch(Dispatchers.Main) {
                        quickSortAlgorithm(0, viewModel.textViewIndex.value!!.size - 1)
                        quickRunning = false
                        highlightAllViewGreen(0, viewModel.textViewIndex.value!!.size - 1)
                    }
                }
            }
        }

        pauseButton.setOnClickListener {
            handlePauseButton()
        }

        mergeSortButton.setOnClickListener {
            if (checkRunning == 0 || checkRunning == 5) {
                checkRunning = 5
                if (inputEditText.text.isNotEmpty()) {
                    if (viewModel.textViewIndex.value!!.size == 10) {
                        val action = MainFragmentDirections.actionMainFragmentToMergeSortFragment()
                        view.findNavController().navigate(action)
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter 10 Numbers to Merge Sort",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    handleRandomButton(10)
                    val action = MainFragmentDirections.actionMainFragmentToMergeSortFragment()
                    view.findNavController().navigate(action)
                }
            }
        }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                when (progress) {
                    3 -> {
                        timeRun = 300
                        timeWait1 = 1000
                        timeWait2 = 500
                    }

                    2 -> {
                        timeRun = 600
                        timeWait1 = 2000
                        timeWait2 = 1000
                    }

                    1 -> {
                        timeRun = 900
                        timeWait1 = 2800
                        timeWait2 = 1500
                    }

                    4 -> {
                        timeRun = 150
                        timeWait1 = 550
                        timeWait2 = 250
                    }

                    5 -> {
                        timeRun = 100
                        timeWait1 = 400
                        timeWait2 = 170
                    }

                    0 -> {
                        timeRun = 1200
                        timeWait1 = 3800
                        timeWait2 = 2000
                    }
                    else -> {

                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    override fun onDestroy() {
        super.onDestroy()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun isValidInput(text: String): Boolean {
        return text.matches("[0-9,]*".toRegex())
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        )
        return px.toInt()
    }

    private fun setTextView(textView: TextView, x: Int) {
        textView.setBackgroundResource(R.drawable.textview_border)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30f)
        textView.text = x.toString()
        textView.gravity = TextView.TEXT_ALIGNMENT_GRAVITY
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.CENTER_VERTICAL
        params.width = view?.let { dpToPx(57, it.context) }!!
        params.height = view?.let { dpToPx(57, it.context) }!!
        textView.layoutParams = params
        displayLayout.addView(textView)
    }

    private fun handleRandomButton(x: Int) {
        displayLayout.removeAllViews()
        resetState()
        binding.inputEditText.text.clear()
        val randomData = IntArray(x) { Random.nextInt(1, 100) }
        viewModel.arrayData.value = randomData.toMutableList()
        viewModel.textViewIndex.value = MutableList(x) { it }
        val animationHandler = Handler(Looper.getMainLooper())
        val delay: Long = 500 // milliseconds delay between each step

        for (i in randomData.indices) {
            val textView = TextView(context)
            setTextView(textView, randomData[i])
        }
    }

    private fun handlePauseButton() {
        if (checkRunning == 0) return
        if (!checkPause) {
            checkPause = true
            pauseState()
            for (i in 0..<viewModel.textViewIndex.value!!.size) {
                val view = displayLayout.getChildAt(i) as TextView
                resetTextViewsBackground(view)
            }
            pauseButton.text = "Continue"
        } else {
            checkPause = false
            pauseButton.text = "Pause"
            GlobalScope.launch(Dispatchers.Main) {
                when (checkRunning) {
                    1 -> bubbleSortJob = GlobalScope.launch(Dispatchers.Main) {
                        if (bubblePosition < viewModel.textViewIndex.value!!.size - 1)
                            bubbleSortAlgorithm(bubblePosition + 1)
                        else
                            bubbleSortAlgorithm(0)
                    }

                    2 -> selectionSortJob = GlobalScope.launch(Dispatchers.Main) {
                        selectionSortAlgorithm(selectionPosition1, selectionPosition2)
                    }

                    3 -> insertionSortJob = GlobalScope.launch(Dispatchers.Main) {
                        insertionSortAlgorithm(insertionPosition1)
                    }

                    4 -> quickSortJob = GlobalScope.launch(Dispatchers.Main) {
                        quickSortAlgorithm(0, viewModel.textViewIndex.value!!.size - 1)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun animationSwapText(view1: TextView, view2: TextView) {
        val xOfView1 = view1.x
        val yOfView1 = view1.y
        val xOfView2 = view2.x
        val yOfView2 = view2.y

        // Animation for view1

        val animView1TranslateUp = ObjectAnimator.ofFloat(view1, "translationY", 200f)
        animView1TranslateUp.duration = timeRun

        val animView1TranslateX = ObjectAnimator.ofFloat(view1, "x", xOfView2)
        animView1TranslateX.duration = timeRun

        val animView1TranslateDown = ObjectAnimator.ofFloat(view1, "translationY", 0f)
        animView1TranslateDown.duration = timeRun

        // Animation for view2
        val animView2TranslateDown = ObjectAnimator.ofFloat(view2, "translationY", -200f)
        animView2TranslateDown.duration = timeRun

        val animView2TranslateX = ObjectAnimator.ofFloat(view2, "x", xOfView1)
        animView2TranslateX.duration = timeRun

        val animView2TranslateUp = ObjectAnimator.ofFloat(view2, "translationY", 0f)
        animView2TranslateUp.duration = timeRun

        // Set up the animator sets
        set1 = AnimatorSet()
        set1.playSequentially(animView1TranslateUp, animView1TranslateX, animView1TranslateDown)

        set2 = AnimatorSet()
        set2.playSequentially(animView2TranslateDown, animView2TranslateX, animView2TranslateUp)

        set2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                checkAnimation = false
                resetTextViewsBackground(view1)
                resetTextViewsBackground(view2)
            }

            override fun onAnimationStart(animation: Animator) {
                highlightTextViewsGreen(view1)
                highlightTextViewsGreen(view2)
                checkAnimation = true
            }
        })
        // Start the animations
        set1.start()
        set2.start()
    }

    private fun animationSwapTextNoColor(view1: TextView, view2: TextView) {
        val xOfView1 = view1.x
        val yOfView1 = view1.y
        val xOfView2 = view2.x
        val yOfView2 = view2.y

        // Animation for view1

        val animView1TranslateUp = ObjectAnimator.ofFloat(view1, "translationY", 200f)
        animView1TranslateUp.duration = timeRun

        val animView1TranslateX = ObjectAnimator.ofFloat(view1, "x", xOfView2)
        animView1TranslateX.duration = timeRun

        val animView1TranslateDown = ObjectAnimator.ofFloat(view1, "translationY", 0f)
        animView1TranslateDown.duration = timeRun

        // Animation for view2
        val animView2TranslateDown = ObjectAnimator.ofFloat(view2, "translationY", -200f)
        animView2TranslateDown.duration = timeRun

        val animView2TranslateX = ObjectAnimator.ofFloat(view2, "x", xOfView1)
        animView2TranslateX.duration = timeRun

        val animView2TranslateUp = ObjectAnimator.ofFloat(view2, "translationY", 0f)
        animView2TranslateUp.duration = timeRun

        // Set up the animator sets
        set1 = AnimatorSet()
        set1.playSequentially(animView1TranslateUp, animView1TranslateX, animView1TranslateDown)

        set2 = AnimatorSet()
        set2.playSequentially(animView2TranslateDown, animView2TranslateX, animView2TranslateUp)

        set2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                checkAnimation = false
            }

            override fun onAnimationStart(animation: Animator) {
                checkAnimation = true
            }
        })

        set1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                checkAnimation = false
            }

            override fun onAnimationStart(animation: Animator) {
                checkAnimation = true
            }
        })

        // Start the animations
        set1.start()
        set2.start()
    }

    private fun animationSwapTextTwoColor(view1: TextView, view2: TextView) {
        val xOfView1 = view1.x
        val yOfView1 = view1.y
        val xOfView2 = view2.x
        val yOfView2 = view2.y

        // Animation for view1

        val animView1TranslateUp = ObjectAnimator.ofFloat(view1, "translationY", 200f)
        animView1TranslateUp.duration = timeRun

        val animView1TranslateX = ObjectAnimator.ofFloat(view1, "x", xOfView2)
        animView1TranslateX.duration = timeRun

        val animView1TranslateDown = ObjectAnimator.ofFloat(view1, "translationY", 0f)
        animView1TranslateDown.duration = timeRun

        // Animation for view2
        val animView2TranslateDown = ObjectAnimator.ofFloat(view2, "translationY", -200f)
        animView2TranslateDown.duration = timeRun

        val animView2TranslateX = ObjectAnimator.ofFloat(view2, "x", xOfView1)
        animView2TranslateX.duration = timeRun

        val animView2TranslateUp = ObjectAnimator.ofFloat(view2, "translationY", 0f)
        animView2TranslateUp.duration = timeRun

        // Set up the animator sets
        set1 = AnimatorSet()
        set1.playSequentially(animView1TranslateUp, animView1TranslateX, animView1TranslateDown)

        set2 = AnimatorSet()
        set2.playSequentially(animView2TranslateDown, animView2TranslateX, animView2TranslateUp)

        set2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                resetTextViewsBackground(view1)
                resetTextViewsBackground(view2)
                checkAnimation = false
            }

            override fun onAnimationStart(animation: Animator) {
                highlightTextViewsGreen(view1)
                highlightTextViewsPink(view2)
                checkAnimation = true
            }
        })
        // Start the animations
        set1.start()
        set2.start()
    }

    private fun resetState() {
        checkRunning = 0
        bubblePosition = 0
        binding.bubbleSortButton.setBackgroundResource(R.color.CornflowerBlue)
        binding.insertionSortButton.setBackgroundResource(R.color.CornflowerBlue)
        binding.selectionSortButton.setBackgroundResource(R.color.CornflowerBlue)
        binding.quickSortButton.setBackgroundResource(R.color.CornflowerBlue)
        checkPause = false
        binding.pauseButton.text = "Pause"
        insertionPosition1 = 0
        bubbleRunning = false
        insertionRunning = false
        selectionRunning = false
        selectionPosition1 = 0
        selectionPosition2 = 1
        quickSortJob.cancel()
        quickRunning = false
        bubbleSortJob.cancel()
        selectionSortJob.cancel()
        insertionSortJob.cancel()
    }

    private fun pauseState() {
        bubbleRunning = false
        insertionRunning = false
        selectionRunning = false
        quickSortJob.cancel()
        bubbleSortJob.cancel()
        selectionSortJob.cancel()
        insertionSortJob.cancel()
        quickSortJob.cancel()
    }

    private fun highlightTextViewsGreen(textView1: TextView) {
        textView1.setBackgroundResource(R.drawable.text_border_highlighted)
    }

    private fun highlightTextViewsPink(textView1: TextView) {
        textView1.setBackgroundResource(R.drawable.text_border_highlight_pink)
    }

    private fun highlightTextViewsYellow(textView1: TextView) {
        textView1.setBackgroundResource(R.drawable.text_border_highlight_yellow)
    }

    private fun resetTextViewsBackground(textView1: TextView) {
        textView1.setBackgroundResource(R.drawable.textview_border)

    }

    private fun displayNumbers(input: String) {
        val text = binding.inputEditText.text.toString()
        if (text.isEmpty()) {
            val displayLayout: LinearLayout = binding.displayLayout
            displayLayout.removeAllViews()
        }
        if (text.isNotEmpty()) {
            if (text[text.length - 1] != ',') {
                val displayLayout: LinearLayout = binding.displayLayout
                displayLayout.removeAllViews()
                viewModel.CreateArrayView()

                for (number in viewModel.arrayData.value!!) {
                    val textView = TextView(context)
                    setTextView(textView, number)
                }
            }
//            else {
//                Toast.makeText(context, "Please enter the number", Toast.LENGTH_SHORT).show()
//            }
        }
    }

    private suspend fun bubbleSortAlgorithm(x: Int) {
        binding.bubbleSortButton.setBackgroundResource(R.color.Green)
        if (bubbleRunning) return
        resetAllView(0, viewModel.textViewIndex.value!!.size - 1)
        bubbleRunning = true
        binding.pauseButton.text = "Pause"
        checkPause = false
        checkRunning = 1
        if (viewModel.textViewIndex.value == null) return
        val size = viewModel.textViewIndex.value?.size
        if (size != null) {
            if (size < 2) {
                return
            }
            for (i in x..size!!.minus(2)) {
                bubblePosition = i
                val view1: TextView =
                    displayLayout.getChildAt(viewModel.textViewIndex.value!![i]) as TextView
                val view2: TextView =
                    displayLayout.getChildAt(viewModel.textViewIndex.value!![i + 1]) as TextView
                highlightTextViewsGreen(view1)
                highlightTextViewsGreen(view2)
                if (viewModel.arrayData.value!![i] > viewModel.arrayData.value!![i + 1]) {
                    swapView(view1, view2, i, i + 1)
                    delay(timeWait1)
                    continue
                } else {
                    delay(timeWait2)
                    resetTextViewsBackground(view1)
                    resetTextViewsBackground(view2)
                    continue
                }
            }
            var checkSwap: Boolean
            do {
                checkSwap = false
                for (i in 0..size!!.minus(2)) {
                    bubblePosition = i
                    val view1: TextView =
                        displayLayout.getChildAt(viewModel.textViewIndex.value!![i]) as TextView
                    val view2: TextView =
                        displayLayout.getChildAt(viewModel.textViewIndex.value!![i + 1]) as TextView
                    highlightTextViewsGreen(view1)
                    highlightTextViewsGreen(view2)
                    if (viewModel.arrayData.value!![i] > viewModel.arrayData.value!![i + 1]) {
                        checkSwap = true
                        swapView(view1, view2, i, i + 1)

                        delay(timeWait1)
                        continue
                    } else {
                        delay(timeWait2)
                        resetTextViewsBackground(view1)
                        resetTextViewsBackground(view2)
                        continue
                    }
                }
            } while (checkSwap)
        }
        checkRunning = 0
        bubblePosition = 0
        bubbleRunning = false
        binding.bubbleSortButton.setBackgroundResource(R.color.CornflowerBlue)
        highlightAllViewGreen(0, viewModel.textViewIndex.value!!.size - 1)
    }

    private suspend fun selectionSortAlgorithm(x: Int, y: Int) {
        binding.selectionSortButton.setBackgroundResource(R.color.Green)
        if (selectionRunning) return
        selectionRunning = true
        binding.pauseButton.text = "Pause"
        checkPause = false
        checkRunning = 2
        val size = viewModel.textViewIndex.value?.size
        if (size == null) return
        val view1 = displayLayout.getChildAt(viewModel.textViewIndex.value!![x]) as TextView
        var minId: Int = x
        selectionPosition1 = x
        highlightTextViewsPink(view1)
        for (j in x + 1..<size) {
            if (viewModel.arrayData.value!![minId] > viewModel.arrayData.value!![j]) {
                minId = j
            }
            if (j >= y) {
                selectionPosition2 = j
                val view2 =
                    displayLayout.getChildAt(viewModel.textViewIndex.value!![j]) as TextView
                highlightTextViewsGreen(view2)
                delay(timeWait2)
                resetTextViewsBackground(view2)
            }
        }
        val view2 = displayLayout.getChildAt(viewModel.textViewIndex.value!![minId]) as TextView
        animationSwapText(view2, view2)
        delay(timeWait1)
        swapView(view1, view2, x, minId)
        delay(timeWait1)

        if (size != null) {
            for (i in x + 1..size!!.minus(2)) {
                selectionPosition1 = i
                var minId: Int = i
                val view1 = displayLayout.getChildAt(viewModel.textViewIndex.value!![i]) as TextView
                highlightTextViewsPink(view1)
                for (j in i + 1..size!!.minus(1)) {
                    selectionPosition2 = j
                    val view2 =
                        displayLayout.getChildAt(viewModel.textViewIndex.value!![j]) as TextView
                    highlightTextViewsGreen(view2)
                    delay(timeWait2)
                    if (viewModel.arrayData.value!![minId] > viewModel.arrayData.value!![j]) {
                        minId = j
                    }
                    resetTextViewsBackground(view2)
                }
                val view2 =
                    displayLayout.getChildAt(viewModel.textViewIndex.value!![minId]) as TextView
                highlightTextViewsGreen(view2)
                swapView(view2, view2, minId, minId)
                delay(timeWait1)
                swapView(view1, view2, i, minId)
                delay(timeWait1)

            }
        }
        checkRunning = 0
        selectionPosition1 = 0
        selectionRunning = false
        binding.selectionSortButton.setBackgroundResource(R.color.CornflowerBlue)
        highlightAllViewGreen(0, viewModel.textViewIndex.value!!.size - 1)
    }

    private suspend fun insertionSortAlgorithm(x: Int) {
        binding.insertionSortButton.setBackgroundResource(R.color.Green)
        if (insertionRunning) return
        insertionRunning = true
        binding.pauseButton.text = "Pause"
        checkPause = false
        checkRunning = 3
        val size = viewModel.textViewIndex.value?.size ?: return

        for (i in x..<size) {
            insertionPosition1 = i
            val view: TextView =
                displayLayout.getChildAt(viewModel.textViewIndex.value!![i]) as TextView
            highlightTextViewsPink(view)
            delay(timeWait2)

            for (j in i downTo 1) {
                if (viewModel.arrayData.value!![j] >= viewModel.arrayData.value!![j - 1]) {
                    resetTextViewsBackground(view)
                    break;
                }
                val view1 = displayLayout.getChildAt(viewModel.textViewIndex.value!![j]) as TextView
                val view2 =
                    displayLayout.getChildAt(viewModel.textViewIndex.value!![j - 1]) as TextView
                val tmp = viewModel.arrayData.value!![j]
                viewModel.arrayData.value!![j] = viewModel.arrayData.value!![j - 1]
                viewModel.arrayData.value!![j - 1] = tmp

                val tmp1 = viewModel.textViewIndex.value!![j]
                viewModel.textViewIndex.value!![j] = viewModel.textViewIndex.value!![j - 1]
                viewModel.textViewIndex.value!![j - 1] = tmp1
                insertionPosition1--;
                animationSwapTextTwoColor(view2, view1)
                delay(timeWait1)
            }
            resetTextViewsBackground(view)
        }
        checkRunning = 0
        insertionPosition1 = 0
        insertionRunning = false
        binding.insertionSortButton.setBackgroundResource(R.color.CornflowerBlue)
        highlightAllViewGreen(0, viewModel.textViewIndex.value!!.size - 1)
    }

    private suspend fun index(be: Int, en: Int, indexPivot: Int): Int {
        val pivot: Int = viewModel.arrayData.value!![indexPivot]

        val view1 =
            displayLayout.getChildAt(viewModel.textViewIndex.value!![indexPivot]) as TextView
        val view2 = displayLayout.getChildAt(viewModel.textViewIndex.value!![en]) as TextView
        highlightTextViewsGreen(view1)
        highlightTextViewsPink(view2)
        animationSwapTextNoColor(view1, view2)

        val tmp = viewModel.textViewIndex.value!![indexPivot]
        viewModel.textViewIndex.value!![indexPivot] = viewModel.textViewIndex.value!![en]
        viewModel.textViewIndex.value!![en] = tmp

        val tmp1 = viewModel.arrayData.value!![indexPivot]
        viewModel.arrayData.value!![indexPivot] = viewModel.arrayData.value!![en]
        viewModel.arrayData.value!![en] = tmp1
        delay(timeWait1)
        resetTextViewsBackground(view2)
        var storeIndex: Int = be
        for (i in be..<en) {
            if (viewModel.arrayData.value!![i] < pivot) {
                val view3 = displayLayout.getChildAt(viewModel.textViewIndex.value!![i]) as TextView
                val view4 =
                    displayLayout.getChildAt(viewModel.textViewIndex.value!![storeIndex]) as TextView
                highlightTextViewsYellow(view3)
                highlightTextViewsYellow(view4)
                animationSwapTextNoColor(view3, view4)
                delay(timeWait1)
                resetTextViewsBackground(view3)
                resetTextViewsBackground(view4)

                val tmp3 = viewModel.textViewIndex.value!![i]
                viewModel.textViewIndex.value!![i] = viewModel.textViewIndex.value!![storeIndex]
                viewModel.textViewIndex.value!![storeIndex] = tmp3

                val tmp4 = viewModel.arrayData.value!![i]
                viewModel.arrayData.value!![i] = viewModel.arrayData.value!![storeIndex]
                viewModel.arrayData.value!![storeIndex] = tmp4
                storeIndex++
            }
        }

        val view = displayLayout.getChildAt(viewModel.textViewIndex.value!![storeIndex]) as TextView
        highlightTextViewsYellow(view)
        animationSwapTextNoColor(view1, view)
        delay(timeWait1)
        resetTextViewsBackground(view)
        val temp = viewModel.textViewIndex.value!![en]
        viewModel.textViewIndex.value!![en] = viewModel.textViewIndex.value!![storeIndex]
        viewModel.textViewIndex.value!![storeIndex] = temp

        val temp1 = viewModel.arrayData.value!![en]
        viewModel.arrayData.value!![en] = viewModel.arrayData.value!![storeIndex]
        viewModel.arrayData.value!![storeIndex] = temp1

        highlightAllViewPink(0, storeIndex - 1)
        highlightAllViewYellow(storeIndex + 1, viewModel.textViewIndex.value!!.size - 1)
        highlightTextViewsGreen(view1)
        delay(timeWait1 * 2)
        resetAllView(0, viewModel.textViewIndex.value!!.size - 1)
        resetTextViewsBackground(view1)
        delay(timeWait2)
        return storeIndex
    }

    private suspend fun quickSortAlgorithm(be: Int, en: Int) {
        binding.quickSortButton.setBackgroundResource(R.color.Green)
        if (be == 0 && en == viewModel.textViewIndex.value!!.size - 1) {
            checkRunning = 4
        }
        if (be < en) {
            var indexPivot = (be + en) / 2
            indexPivot = index(be, en, indexPivot)
            if (indexPivot > be) {
                quickSortAlgorithm(be, indexPivot)
            }
            if (indexPivot < en) {
                quickSortAlgorithm(indexPivot + 1, en)
            }
        }
        if (be == 0 && en == viewModel.textViewIndex.value!!.size - 1) {
            checkRunning = 0
            binding.quickSortButton.setBackgroundResource(R.drawable.btn_bg)
            highlightAllViewGreen(0, viewModel.textViewIndex.value!!.size - 1)
        }
    }

    private fun swapView(view1: TextView, view2: TextView, id1: Int, id2: Int) {
        animationSwapText(view1, view2)

        val tmp = viewModel.textViewIndex.value!![id1]
        viewModel.textViewIndex.value!![id1] = viewModel.textViewIndex.value!![id2]
        viewModel.textViewIndex.value!![id2] = tmp

        val tmp1 = viewModel.arrayData.value!![id1]
        viewModel.arrayData.value!![id1] = viewModel.arrayData.value!![id2]
        viewModel.arrayData.value!![id2] = tmp1

    }

    private fun highlightAllViewGreen(id1: Int, id2: Int) {
        if (viewModel.textViewIndex.value == null) return
        for (i in id1..id2) {
            val view = displayLayout.getChildAt(viewModel.textViewIndex.value!![i]) as TextView
            highlightTextViewsGreen(view)
        }
    }

    private fun highlightAllViewPink(id1: Int, id2: Int) {
        if (viewModel.textViewIndex.value == null) return
        for (i in id1..id2) {
            val view = displayLayout.getChildAt(viewModel.textViewIndex.value!![i]) as TextView
            highlightTextViewsPink(view)
        }
    }

    private fun highlightAllViewYellow(id1: Int, id2: Int) {
        if (viewModel.textViewIndex.value == null) return
        for (i in id1..id2) {
            val view = displayLayout.getChildAt(viewModel.textViewIndex.value!![i]) as TextView
            highlightTextViewsYellow(view)
        }
    }

    private fun resetAllView(id1: Int, id2: Int) {
        if (viewModel.textViewIndex.value == null) return
        for (i in id1..id2) {
            val view = displayLayout.getChildAt(viewModel.textViewIndex.value!![i]) as TextView
            resetTextViewsBackground(view)
        }
    }

}




