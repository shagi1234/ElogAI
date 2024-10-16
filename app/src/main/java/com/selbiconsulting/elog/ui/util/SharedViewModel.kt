package com.selbiconsulting.elog.ui.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.selbiconsulting.elog.data.model.dto.DtoDefect
import com.selbiconsulting.elog.data.model.dto.DtoMainInfo
import com.selbiconsulting.elog.data.model.dto.DtoNote
import com.selbiconsulting.elog.data.model.enums.ThemeModes
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


class SharedViewModel : ViewModel() {
    val checkedDefectsCount = MutableLiveData(0)
    val encodedSignature = MutableLiveData<String>()
    val checkedNotes = MutableLiveData<List<DtoNote>>()
    val checkedUnitDefects = SingleLiveEvent<List<DtoDefect>>()
    val checkedTrailerDefects = SingleLiveEvent<List<DtoDefect>>()
    val savedUnitDefects = MutableLiveData<List<DtoDefect>>()
    val savedTrailerDefects = MutableLiveData<List<DtoDefect>>()
    val isDvirCreated = MutableLiveData<Boolean>()
    val isPcAllowed = MutableLiveData(true)

    val mainInfo = MutableLiveData<DtoMainInfo>()

    val volume = SingleLiveEvent<Int>()
    val logsPdfFiles = MutableLiveData<List<File>>()
}

class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }

    override fun setValue(value: T?) {
        pending.set(true)
        super.setValue(value)
    }

    fun call() {
        value = null
    }
}