package com.choidh.karrot.market.data.firebase

import com.choidh.karrot.market.DBKey.Companion.DB_USERS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class FireBaseSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabaseReference: DatabaseReference
) {

    fun login(email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (emitter.isDisposed.not()) {
                if (it.isSuccessful) {
                    emitter.onComplete()
                } else {
                    emitter.onError(it.exception)
                }
            }
        }
    }

    fun signUp(email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (emitter.isDisposed.not()) {
                if (it.isSuccessful) {
                    emitter.onComplete()
                } else {
                    emitter.onError(it.exception)
                }
            }
        }
    }

    fun saveUser(name: String) = run {
        val user = mutableMapOf<String, Any>()
        val userId = currentUser()?.uid.orEmpty()
        user["userId"] = userId
        user["name"] = name
        firebaseDatabaseReference.child(DB_USERS).child(userId).updateChildren(user)
    }

    fun logout() = firebaseAuth.signOut()

    fun currentUser() = firebaseAuth.currentUser ?: null

}