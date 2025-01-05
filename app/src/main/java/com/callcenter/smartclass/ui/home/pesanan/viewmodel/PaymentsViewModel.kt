package com.callcenter.smartclass.ui.home.pesanan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.pesanan.data.Order
import com.callcenter.smartclass.ui.home.pesanan.data.TransactionStatusResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "PaymentsViewModel"

class PaymentsViewModel : ViewModel() {

    private val repository = OrderRepository()

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order

    private val _transactionStatus = MutableStateFlow<TransactionStatusResponse?>(null)
    val transactionStatus: StateFlow<TransactionStatusResponse?> = _transactionStatus

    /**
     * Mendapatkan order dan mulai mendengarkan perubahan real-time
     */
    fun getOrder(orderId: String) {
        Log.d(TAG, "getOrder called with orderId: $orderId")
        viewModelScope.launch {
            repository.getOrderById(orderId)
                .onStart { Log.d(TAG, "Listening to order updates") }
                .catch { e -> Log.e(TAG, "Error listening to order: ${e.localizedMessage}") }
                .collect { fetchedOrder ->
                    Log.d(TAG, "Order updated: $fetchedOrder")
                    _order.value = fetchedOrder
                }
        }
    }

    /**
     * Mengambil Snap Token untuk proses pembayaran
     */
    suspend fun fetchSnapToken(order: Order): String? {
        Log.d(TAG, "fetchSnapToken called for orderId: ${order.orderId}")
        var token = order.snapToken
        if (token.isNullOrEmpty()) {
            token = repository.generateSnapToken(order)
        } else {
            Log.d(TAG, "Using existing Snap Token: $token")
        }
        Log.d(TAG, "Snap Token: $token")
        return token
    }

    /**
     * Memperbarui status pembayaran pada Firestore
     */
    fun updatePaymentStatus(orderId: String, paymentStatus: String) {
        viewModelScope.launch {
            val success = repository.updatePaymentStatus(orderId, paymentStatus)
            if (success) {
                Log.d(TAG, "Payment status updated to $paymentStatus for orderId: $orderId")
            } else {
                Log.e(TAG, "Failed to update payment status for orderId: $orderId")
            }
        }
    }

    /**
     * Mendapatkan status transaksi dari API
     */
    fun fetchTransactionStatus(orderId: String, transactionStatus: String? = null) {
        viewModelScope.launch {
            val status = repository.getTransactionStatus(orderId, transactionStatus)
            if (status != null) {
                _transactionStatus.value = status
                updatePaymentStatus(orderId, status.transactionStatus)
            } else {
                Log.e(TAG, "Failed to fetch transaction status for orderId: $orderId")
            }
        }
    }
}
