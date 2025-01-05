package com.callcenter.smartclass.ui.home.pesanan.viewmodel

import android.util.Log
import com.callcenter.smartclass.ui.home.pesanan.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "OrderRepository"

class OrderRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val ordersCollection = firestore.collection("orders")
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    /**
     * Mendapatkan order dengan ID tertentu menggunakan Snapshot Listener untuk mendengarkan perubahan real-time
     */
    fun getOrderById(orderId: String): Flow<Order?> = callbackFlow {
        Log.d(TAG, "Setting up snapshot listener for orderId: $orderId")
        listenerRegistration = ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Snapshot listener error: ${error.message}")
                    trySend(null).isSuccess
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val order = snapshot.toObject(Order::class.java)
                    Log.d(TAG, "Order updated: $order")
                    trySend(order).isSuccess
                } else {
                    Log.e(TAG, "Order dengan ID $orderId tidak ditemukan.")
                    trySend(null).isSuccess
                }
            }

        awaitClose {
            Log.d(TAG, "Removing snapshot listener for orderId: $orderId")
            listenerRegistration?.remove()
        }
    }

    /**
     * Memperbarui status pembayaran pada Firestore
     */
    suspend fun updatePaymentStatus(orderId: String, paymentStatus: String): Boolean {
        return try {
            ordersCollection.document(orderId)
                .update("paymentStatus", paymentStatus)
                .await()
            Log.d(TAG, "Payment status updated successfully for orderId: $orderId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating payment status for orderId: $orderId, Error: ${e.message}")
            false
        }
    }

    /**
     * Mengambil judul produk berdasarkan productId
     */
    private suspend fun fetchProductTitle(productId: String): String? {
        return try {
            val documentSnapshot = firestore.collection("products").document(productId).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.getString("title")
            } else {
                Log.e(TAG, "Produk dengan ID $productId tidak ditemukan.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product from Firestore: ${e.message}")
            null
        }
    }

    /**
     * Mendapatkan detail pengguna yang sedang login
     */
    private fun getCurrentUserDetails(): CustomerDetails? {
        val user = auth.currentUser
        return if (user != null) {
            val firstName = user.displayName?.split(" ")?.getOrNull(0) ?: "First Name"
            val lastName = user.displayName?.split(" ")?.getOrNull(1) ?: "Last Name"
            val email = user.email ?: "email@example.com"

            CustomerDetails(first_name = firstName, last_name = lastName, email = email)
        } else {
            null
        }
    }

    /**
     * Mengambil nomor telepon dan alamat pengguna dari Firestore
     */
    private suspend fun getUserPhoneAndAddress(): Pair<String?, String?> {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            return try {
                val documentSnapshot = firestore.collection("users").document(uid).get().await()

                if (documentSnapshot.exists()) {
                    val phoneNumber = documentSnapshot.getString("phoneNumber")
                    val streetAddress = documentSnapshot.getString("street")
                    Pair(phoneNumber, streetAddress)
                } else {
                    Log.e(TAG, "User dengan UID $uid tidak ditemukan di Firestore.")
                    Pair(null, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user phone and address from Firestore: ${e.message}")
                Pair(null, null)
            }
        } else {
            Log.e(TAG, "User not logged in.")
            return Pair(null, null)
        }
    }

    /**
     * Menghasilkan Snap Token menggunakan Retrofit
     */
    suspend fun generateSnapToken(order: Order): String? {
        Log.d(TAG, "Generating Snap Token for orderId: ${order.orderId}")

        val productTitle = fetchProductTitle(order.productId) ?: "Nama Produk Tidak Ditemukan"

        val customerDetails = getCurrentUserDetails() ?: CustomerDetails(
            first_name = "First Name",
            last_name = "Last Name",
            email = "email@example.com",
        )

        val (phoneNumber, streetAddress) = getUserPhoneAndAddress()

        val updatedCustomerDetails = customerDetails.copy(
            phone_number = phoneNumber ?: "No phone number",
            address = streetAddress ?: "No address"
        )

        return try {
            val request = SnapTokenRequest(
                order_id = order.orderId,
                gross_amount = order.totalPrice,
                item_details = listOf(
                    ItemDetail(
                        id = order.productId,
                        price = order.totalPrice / order.quantity,
                        quantity = order.quantity,
                        name = productTitle
                    )
                ),
                customer_details = updatedCustomerDetails
            )

            Log.d(TAG, "SnapTokenRequest: $request")

            val response = RetrofitInstance.api.generateSnapToken(request)
            Log.d(TAG, "Response received: ${response.code()} - ${response.message()}")

            if (response.isSuccessful) {
                val token = response.body()?.snapToken
                Log.d(TAG, "Snap Token generated: $token")
                if (token != null) {
                    ordersCollection.document(order.orderId)
                        .update("snapToken", token)
                        .await()
                }
                token
            } else {
                Log.e(TAG, "Failed to generate Snap Token. HTTP Status: ${response.code()}, Message: ${response.message()}")
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException while generating Snap Token: ${e.message}")
            null
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException while generating Snap Token: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while generating Snap Token: ${e.message}")
            null
        }
    }

    suspend fun getTransactionStatus(orderId: String, transactionId: String? = null): TransactionStatusResponse? {
        return try {
            val response = RetrofitInstance.api.getTransactionStatus(orderId, transactionId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to get transaction status. HTTP Status: ${response.code()}, Message: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting transaction status: ${e.message}")
            null
        }
    }
}
