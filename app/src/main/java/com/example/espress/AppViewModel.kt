package com.example.espress

import android.content.Context
import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AppViewModel(
    db: AppDatabase,
    contextProvider: () -> Context
) : ViewModel() {
    val ctxProvider = contextProvider
    private val extractionDao by lazy { db.extractionDao() }
    val allExtractions: Flow<List<Extraction>> = extractionDao.getAll()

    fun insertExtraction(
        extraction: Extraction
    ) {
        val currentTimeMillis = System.currentTimeMillis()
        val date = DateUtils.formatDateTime(
            ctxProvider(), currentTimeMillis,
            DateUtils.FORMAT_SHOW_DATE
//            DateUtils.FORMAT_SHOW_TIME
        )

        val extractionWithTime = extraction.copy(
            uid = 0, // new entity otherwise throws error
            time = date
        )

        viewModelScope.launch(Dispatchers.IO) {
            extractionDao.insertAll(extractionWithTime)
        }
    }

    fun updateExtraction(extraction: Extraction) {
        viewModelScope.launch(Dispatchers.IO) {
            extractionDao.update(extraction)
        }
    }

    fun deleteExtraction(extraction: Extraction) {
        viewModelScope.launch(Dispatchers.IO) {
            extractionDao.delete(extraction)
        }
    }
}