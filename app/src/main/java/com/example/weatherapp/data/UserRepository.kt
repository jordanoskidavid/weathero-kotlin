package com.example.weatherapp.data

/*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getUserFlow(uid: String): Flow<User?> = userDao.getUserFlow(uid)

    suspend fun getUser(uid: String): User? = userDao.getUser(uid)

    suspend fun saveUser(user: User) {
        try {
            // Save to Room (local database)
            userDao.insertUser(user)

            // Save to Firestore (cloud database)
            val userMap = hashMapOf(
                "username" to user.username,
                "email" to user.email,
                "uid" to user.uid
            )

            firestore.collection("users")
                .document(user.uid)
                .set(userMap)
                .await()

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun syncUserFromFirestore(uid: String): User? {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            if (document.exists()) {
                val user = User(
                    uid = document.getString("uid") ?: uid,
                    username = document.getString("username") ?: "",
                    email = document.getString("email") ?: ""
                )
                userDao.insertUser(user)
                user
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} */
