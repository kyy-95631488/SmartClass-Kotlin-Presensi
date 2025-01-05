package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Address
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Product_Data
import com.callcenter.smartclass.ui.home.pesanan.data.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

class ProductDetailViewModel : ViewModel() {

    private val _product = MutableStateFlow<Product_Data?>(null)
    val product: StateFlow<Product_Data?> = _product

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _address = MutableStateFlow<Address?>(null)
    val address: StateFlow<Address?> = _address

    private val _isAddressLoading = MutableStateFlow(false)
    val isAddressLoading: StateFlow<Boolean> = _isAddressLoading

    private val _addressError = MutableStateFlow<String?>(null)

    private val _relatedProducts = MutableStateFlow<List<Product_Data>>(emptyList())
    val relatedProducts: StateFlow<List<Product_Data>> = _relatedProducts

    fun getProductById(productId: String): StateFlow<Product_Data?> {
        viewModelScope.launch {
            _isLoading.value = true
            firestore.collection("products").document(productId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val productData = document.toObject(Product_Data::class.java)?.copy(id = document.id)
                        _product.value = productData
                    } else {
                        _errorMessage.value = "Produk tidak ditemukan."
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    _errorMessage.value = exception.message
                    _isLoading.value = false
                }
        }
        return product
    }

    fun fetchAddress() {
        val user = auth.currentUser ?: return
        _isAddressLoading.value = true
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val addr = document.toObject(Address::class.java)
                    _address.value = addr
                }
                _isAddressLoading.value = false
            }
            .addOnFailureListener { exception ->
                _addressError.value = exception.message
                _isAddressLoading.value = false
            }
    }

    fun saveAddress(address: Address, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return
        _isAddressLoading.value = true
        firestore.collection("users").document(user.uid)
            .set(address, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                _address.value = address
                _isAddressLoading.value = false
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                _addressError.value = exception.message
                _isAddressLoading.value = false
                onComplete(false)
            }
    }

    fun purchaseProduct(productId: String, quantity: Int, onResult: (Boolean, String?) -> Unit) {
        if (productId.isBlank()) {
            onResult(false, "ID produk tidak valid.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val productRef = firestore.collection("products").document(productId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(productRef)
                val currentStock = snapshot.getLong("stock")?.toInt() ?: 0
                if (currentStock >= quantity) {
                    transaction.update(productRef, "stock", currentStock - quantity)
                    true
                } else {
                    throw Exception("Stok tidak mencukupi.")
                }
            }.addOnSuccessListener {
                createOrder(productId, quantity) { success, message ->
                    if (success) {
                        _product.value = _product.value?.copy(stock = _product.value?.stock?.minus(quantity) ?: 0)
                        _isLoading.value = false
                        onResult(true, "Pesanan berhasil dibuat.")
                    } else {
                        _isLoading.value = false
                        onResult(false, message ?: "Gagal membuat pesanan.")
                    }
                }
            }.addOnFailureListener { e ->
                _isLoading.value = false
                onResult(false, e.message)
            }
        }
    }

    fun createOrder(productId: String, quantity: Int, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "User tidak terautentikasi.")
            return
        }

        val orderId = firestore.collection("orders").document().id
        val product = _product.value
        if (product == null) {
            onResult(false, "Produk tidak ditemukan.")
            return
        }

        val order = Order(
            orderId = orderId,
            userId = user.uid,
            productId = productId,
            quantity = quantity,
            totalPrice = product.price * quantity,
            paymentStatus = "Pending",
            shippingStatus = "Pending",
            createdAt = Timestamp.now() // Mengisi createdAt dengan waktu saat ini
        )

        firestore.collection("orders").document(orderId).set(order)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }

    fun fetchRelatedProducts(brandId: String) {
        viewModelScope.launch {
            firestore.collection("products")
                .whereEqualTo("brandId", brandId)
                .get()
                .addOnSuccessListener { documents ->
                    val products = documents.mapNotNull { doc ->
                        doc.toObject(Product_Data::class.java)?.copy(id = doc.id)
                    }
                    _relatedProducts.value = products
                }
                .addOnFailureListener { exception ->
                    // Handle error jika diperlukan
                }
        }
    }
}
