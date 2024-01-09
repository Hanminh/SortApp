package com.example.sortapp

import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.w3c.dom.Text
import java.util.Collections.list

    class MyViewModel : ViewModel() {
        var arrayData = MutableLiveData< MutableList<Int> >()
        var textData = MutableLiveData< String > ()
        var dataTextView = MutableLiveData<EditText>()
        var textViewIndex = MutableLiveData< MutableList<Int> >()

        fun CreateArrayView() {
            textData.value = dataTextView.value?.text.toString()
            arrayData.value = textData.value?.split(",")?.map { it.trim().toIntOrNull()!! } as MutableList<Int>?
            textViewIndex.value = arrayData.value?.let { MutableList(it.size){it} }

        }

    }