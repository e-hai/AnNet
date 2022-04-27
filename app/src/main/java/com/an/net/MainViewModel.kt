package com.an.net

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(context: Application) : AndroidViewModel(context) {

    init {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }
}