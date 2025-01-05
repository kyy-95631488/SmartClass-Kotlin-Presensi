package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Brand_Data
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.repo.BrandRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BrandViewModel : ViewModel() {
    private val repository = BrandRepository()

    private val _brands = MutableStateFlow<List<Brand_Data>>(emptyList())
    val brands: StateFlow<List<Brand_Data>> = _brands

    init {
        fetchBrands()
    }

    private fun fetchBrands() {
        viewModelScope.launch {
            try {
                val brandList = repository.getBrands()
                brandList.forEach { brand ->
                    brand.productCount = repository.getProductCountByBrand(brand.uid)
                }
                _brands.value = brandList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}