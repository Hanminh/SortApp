package com.example.sortapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sortapp.databinding.FragmentMergeSortBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MergeSortFragment : Fragment() {
    //    private val args : MergeSortFragmentArgs by navArgs()
//    val viewModel = args.viewModel
    private lateinit var displayLayout: ConstraintLayout
    private val viewModel: MyViewModel by activityViewModels()
    private val index: MutableList<Int> = MutableList<Int>(40) { it }
    private lateinit var binding: FragmentMergeSortBinding
    private lateinit var textViewList: MutableList<TextView>
    private var mergeJob = GlobalScope.launch { }
    private var isRunning: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mergeJob.cancel()
                findNavController().popBackStack(R.id.mainFragment, false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callBack)
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentMergeSortBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewList = createListView()
        displayLayout = binding.displayLayout
        val button: Button = binding.button
        var checkRun: Boolean = false
        GlobalScope.launch(Dispatchers.Main) {
            createUIView()
            for (i in 11..36) {
                val ani = ObjectAnimator.ofFloat(textViewList[i], "alpha", 0f, 0f)
                ani.start()
            }
        }
        button.text = "Start"

        button.setOnClickListener {
            mergeJob = GlobalScope.launch(Dispatchers.Main) {
                handleButton()
            }
        }

    }

    //Hàm xử lý sự kiện nút Start, thuật toán Merge Sort được thực hiện theo thứ tự các hàm bên dưới
    //Thực hiện theo thứ tự thủ công
    private suspend fun handleButton() {
        binding.button.visibility = Button.GONE
        divideArray1(11,15)
        delay(500)
        divideArray1(21,22)
        delay(500)
        mergeSort30(21, 22)
        divideArray1(23,25)
        delay(500)
        divideArray2(31,31)
        delay(500)
        mergeSort31(31, 32, 33)
        mergeSort32(31, 32, 33, 23)
        mergeSort32(21, 23, 25, 11)
        delay(500)
        divideArray1(16,20)
        delay(500)
        divideArray1(26,27)
        delay(500)
        mergeSort30(26,27)
        divideArray1(28,30)
        delay(500)
        divideArray3(34,34)
        delay(500)
        mergeSort31(34, 35, 36)
        mergeSort32(34, 35, 36, 28)
        mergeSort32(26, 28, 30, 16)
        mergeSort32(11, 16, 20, 1)

        for (i in 1..10) {
            highlightTextViewGreen(i)
        }

    }

    //Đổi chỗ 2 phần tử trong cùng 1 hàng
    private fun swapTextView(id1: Int, id2: Int) {
        val view1 = textViewList[index[id1]]
        val view2 = textViewList[index[id2]]
        animationSwapText(view1, view2)

        val tmp = index[id1]
        index[id1] = index[id2]
        index[id2] = tmp
    }

    //Đổi chỗ 2 phần tử khác hàng _ Dùng trong bước Merge 2 dãy con đã được sắp xếp
    private fun swapTextViewDiffRow(id1: Int, id2: Int) {
        val view1 = textViewList[index[id1]]
        val x1 = view1.x
        val y1 = view1.y
        val view2 = textViewList[index[id2]]
        val x2 = view2.x
        val y2 = view2.y
        val ani1 = ObjectAnimator.ofFloat(view2, "x", x1)
        val ani2 = ObjectAnimator.ofFloat(view2, "y", y1)
        val set = AnimatorSet()
        set.playTogether(ani1, ani2)
        set.duration = 500

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                resetTextView(view1)
                resetTextView(view2)
            }

            override fun onAnimationStart(animation: Animator) {
                highlightTextView(view1)
                highlightTextView(view2)
            }
        })
        set.start()
    }
    //Duration = 500
    private fun animationSwapText(view1: TextView, view2: TextView) {
        val x1 = view1.x
        val y1 = view1.y
        val x2 = view2.x
        val y2 = view2.y
        val ani1 = ObjectAnimator.ofFloat(view1, "x", x2)
        val ani2 = ObjectAnimator.ofFloat(view2, "x", x1)
        ani1.duration = 500
        ani2.duration = 500
        val set = AnimatorSet()
        set.playTogether(ani1, ani2)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                resetTextView(view1)
                resetTextView(view2)
            }

            override fun onAnimationStart(animation: Animator) {
                highlightTextView(view1)
                highlightTextView(view2)
            }
        })
        set.start()
    }

    //Duration = 500
    private fun swapTextViewDiffRow(view1: TextView, view2: TextView) {
        val x1 = view1.x
        val y1 = view1.y
        val x2 = view2.x
        val y2 = view2.y
        val ani1 = ObjectAnimator.ofFloat(view2, "x", x1)
        val ani2 = ObjectAnimator.ofFloat(view2, "y", y1)
        val set = AnimatorSet()
        set.playTogether(ani1, ani2)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                resetTextView(view1)
                resetTextView(view2)
                view1.text = view2.text
                view2.visibility = TextView.GONE
            }

            override fun onAnimationStart(animation: Animator) {
                highlightTextView(view1)
                highlightTextView(view2)
            }
        })
        set.duration = 500
        set.start()
    }

    private fun cloneTextView(id: Int): TextView {
        val view = TextView(context)
        view.text = textViewList[index[id]].text
        view.x = textViewList[index[id]].x
        view.y = textViewList[index[id]].y
        view.width = textViewList[index[id]].width
        view.height = textViewList[index[id]].height
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        view.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.textSize))
        view.setBackgroundResource(R.drawable.textview_border_2)
        view.visibility = TextView.INVISIBLE
        return view
    }

    //Duration = 500
    private fun cloneAndMove(id1: Int, id2: Int) {
        val view = cloneTextView(id1)
        displayLayout.addView(view)
        view.visibility = TextView.VISIBLE
        highlightTextView(view)
        highlightTextView(id2)
        swapTextViewDiffRow(textViewList[index[id2]], view)
    }

    //Duration = 500
    private suspend fun divideArray1(id1: Int, id2: Int) {
        for(i in id1..id2) {
            cloneAndMove(i-10,i)
        }
        delay(501)
        for(i in id1..id2) {
            val ani = ObjectAnimator.ofFloat(textViewList[i], "alpha", 0f, 1f)
            ani.start()
        }
    }

    private suspend fun divideArray2(id1: Int, id2: Int) {
        for(i in id1..id2) {
            cloneAndMove(i-8,i)
        }
        delay(501)
        for(i in id1..id2) {
            val ani = ObjectAnimator.ofFloat(textViewList[i], "alpha", 0f, 1f)
            ani.start()
        }
    }

    private suspend fun divideArray3(id1: Int, id2: Int) {
        for(i in id1..id2) {
            cloneAndMove(i-6,i)
        }
        delay(501)
        for(i in id1..id2) {
            val ani = ObjectAnimator.ofFloat(textViewList[i], "alpha", 0f, 1f)
            ani.start()
        }
    }

    //Thực hiện sắp xếp 2 phần tử liên tiếp nhau
    private suspend fun mergeSort30(id1: Int, id2: Int) {
        val view1 = textViewList[index[id1]]
        val view2 = textViewList[index[id2]]
        highlightTextView(id1)
        highlightTextView(id2)
        if (view1.text.toString().toInt() > view2.text.toString().toInt()) {
            swapTextView(id1, id2)
            delay(700)
        } else {
            delay(500)
            resetTextView(view1)
            resetTextView(view2)
        }
    }

    //Thực hiện trong bước merge 1 dãy con 1 phần tử và 1 dãy con 2 phần tử
    private suspend fun mergeSort31(id1: Int, id2: Int, id3: Int) {
        highlightTextView(id1)
        delay(500)
        resetTextView(id1)
        if(id1<34) {
            divideArray2(id2,id3)
            delay(500)
        } else {
            divideArray3(id2,id3)
            delay(500)
        }

        highlightTextView(id2)
        highlightTextView(id3)
        delay(500)
        if (textViewList[index[id2]].text.toString()
                .toInt() > textViewList[index[id3]].text.toString().toInt()
        ) {
            swapTextView(id2, id3)
            delay(700)
        } else {
            delay(500)
            resetTextView(id2)
            resetTextView(id3)
        }
    }

    //Thực hiện trong bước merge 2 dãy con bất kì, mỗi dãy con gồm 2 phần tử trở lên
    private suspend fun mergeSort32(v1: Int, v2: Int, v3: Int, be: Int) {
        var id1 = v1
        var id2 = v2
        var k = be
        makeEmptyTextView(be, be + v3 - v1)
        delay(700)
        while ((id1 < v2) && (id2 <= v3)) {
            val view1 = textViewList[index[id1]]
            val view2 = textViewList[index[id2]]
            val view = textViewList[k]

            highlightTextView(view1)
            highlightTextView(view2)
            highlightTextView(view)
            delay(400)

            if (view1.text.toString().toInt() < view2.text.toString().toInt()) {
                cloneAndMove(id1, k)
                delay(700)
                id1++
                k++
            } else {
                cloneAndMove(id2, k)
                delay(700)
                id2++
                k++
            }
            resetTextView(view1)
            resetTextView(view2)
            resetTextView(view)
        }
        while (id1 < v2) {
            val view1 = textViewList[index[id1]]
            val view = textViewList[k]
            highlightTextView(view1)
            highlightTextView(view)
            delay(700)
            cloneAndMove(id1, k)
            delay(700)
            id1++
            k++
            resetTextView(view1)
            resetTextView(view)
        }
        while (id2 <= v3) {
            val view2 = textViewList[index[id2]]
            val view = textViewList[k]
            highlightTextView(view2)
            highlightTextView(view)
            delay(1000)
            cloneAndMove(id2, k)
            delay(800)
            k++
            id2++
            resetTextView(view2)
            resetTextView(view)
        }

    }

    private fun createListView(): MutableList<TextView> {
        val textViewList: MutableList<TextView> = mutableListOf()
        textViewList.add(requireView().findViewById(R.id.text01))//0
        textViewList.add(requireView().findViewById(R.id.text01))//1
        textViewList.add(requireView().findViewById(R.id.text02))//2
        textViewList.add(requireView().findViewById(R.id.text03))//3
        textViewList.add(requireView().findViewById(R.id.text04))//4
        textViewList.add(requireView().findViewById(R.id.text05))//5
        textViewList.add(requireView().findViewById(R.id.text06))//6
        textViewList.add(requireView().findViewById(R.id.text07))//7
        textViewList.add(requireView().findViewById(R.id.text08))//8
        textViewList.add(requireView().findViewById(R.id.text09))//9
        textViewList.add(requireView().findViewById(R.id.text010))//10
        textViewList.add(requireView().findViewById(R.id.text11))//11
        textViewList.add(requireView().findViewById(R.id.text12))//12
        textViewList.add(requireView().findViewById(R.id.text13))//13
        textViewList.add(requireView().findViewById(R.id.text14))//14
        textViewList.add(requireView().findViewById(R.id.text15))//15
        textViewList.add(requireView().findViewById(R.id.text16))//16
        textViewList.add(requireView().findViewById(R.id.text17))//17
        textViewList.add(requireView().findViewById(R.id.text18))//18
        textViewList.add(requireView().findViewById(R.id.text19))//19
        textViewList.add(requireView().findViewById(R.id.text110))//20
        textViewList.add(requireView().findViewById(R.id.text21))//21
        textViewList.add(requireView().findViewById(R.id.text22))//22
        textViewList.add(requireView().findViewById(R.id.text23))//23
        textViewList.add(requireView().findViewById(R.id.text24))//24
        textViewList.add(requireView().findViewById(R.id.text25))//25
        textViewList.add(requireView().findViewById(R.id.text26))//26
        textViewList.add(requireView().findViewById(R.id.text27))//27
        textViewList.add(requireView().findViewById(R.id.text28))//28
        textViewList.add(requireView().findViewById(R.id.text29))//29
        textViewList.add(requireView().findViewById(R.id.text210))//30
        textViewList.add(requireView().findViewById(R.id.text33))//31
        textViewList.add(requireView().findViewById(R.id.text34))//32
        textViewList.add(requireView().findViewById(R.id.text35))//33
        textViewList.add(requireView().findViewById(R.id.text38))//34
        textViewList.add(requireView().findViewById(R.id.text39))//35
        textViewList.add(requireView().findViewById(R.id.text310))//36
        return textViewList
    }

    //Hiển thị giao diện thuật toán sắp xếp
    private fun createUIView() {
        for (i in 1..10) {
            textViewList[i].text = viewModel.arrayData.value!![i - 1].toString()
            textViewList[i + 10].text = viewModel.arrayData.value!![i - 1].toString()
            textViewList[i + 20].text = viewModel.arrayData.value!![i - 1].toString()
        }
        textViewList[31].text = viewModel.arrayData.value!![2].toString()
        textViewList[32].text = viewModel.arrayData.value!![3].toString()
        textViewList[33].text = viewModel.arrayData.value!![4].toString()

        textViewList[34].text = viewModel.arrayData.value!![7].toString()
        textViewList[35].text = viewModel.arrayData.value!![8].toString()
        textViewList[36].text = viewModel.arrayData.value!![9].toString()
    }

    private fun highlightTextView(id: Int) {
        textViewList[index[id]].setBackgroundResource(R.color.blue)
    }

    private fun highlightTextViewGreen(id: Int) {
        textViewList[id].setBackgroundResource(R.color.Green)
    }

    private fun highlightTextView(view: TextView) {
        view.setBackgroundResource(R.color.blue)
    }

    private fun resetTextView(id: Int) {
        textViewList[index[id]].setBackgroundResource(R.drawable.textview_border_2)
    }

    private fun resetTextView(view: TextView) {
        view.setBackgroundResource(R.drawable.textview_border_2)
    }

    private fun makeEmptyTextView(id1: Int, id2: Int) {
        for (i in id1..id2) {
            textViewList[index[i]].text = null
        }
    }
}