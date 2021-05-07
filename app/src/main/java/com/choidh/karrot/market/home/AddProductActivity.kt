package com.choidh.karrot.market.home

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.choidh.karrot.market.DBKey
import com.choidh.karrot.market.DBKey.Companion.DB_PRODUCTS
import com.choidh.karrot.market.DBKey.Companion.STORAGE_PRODUCT_IMAGE
import com.choidh.karrot.market.R
import com.choidh.karrot.market.databinding.ActivityAddProductBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private var imageUri: Uri? = null
    private val fbAuth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val fbStorage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private val dbProduct: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_PRODUCTS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContentProvider()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_ACTION_GET_CONTENT -> {
                val uri: Uri? = data?.data
                if (uri != null) {
                    binding.thumbnailImageView.setImageURI(uri)
                    imageUri = uri
                } else {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.failed_get_photo),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.failed_get_photo),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initViews() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.doneButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val price = binding.priceEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()
            val sellerId = fbAuth.currentUser?.uid.orEmpty()

            if (title.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.enter_title),
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.enter_description),
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            showProgress(true)

            if (imageUri != null) {
                val photoUri = imageUri ?: return@setOnClickListener
                uploadImage(photoUri,
                    successHandler = { uri ->
                        uploadProduct(sellerId, title, price.toInt(), description, uri)
                    }, errorHandler = {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.failed_upload_photo),
                            Toast.LENGTH_SHORT
                        ).show()
                        showProgress(false)
                    })
            } else {
                uploadProduct(sellerId, title, price.toInt(), description, "")
            }
        }

        binding.addImageButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startContentProvider()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_READ_EXTERNAL_STORAGE
                    )
                }
            }
        }
    }

    private fun showProgress(isVisible: Boolean) {
        binding.progressBar.isVisible = isVisible
    }

    // Storage Access Framework (SAF) 사진 가져오기 기능
    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_ACTION_GET_CONTENT)
    }

    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("")
            .setMessage(resources.getString(R.string.camera_permission))
            .setPositiveButton(resources.getString(R.string.allow)) { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_READ_EXTERNAL_STORAGE
                )
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .create().show()
    }

    private fun uploadImage(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        fbStorage.reference.child(STORAGE_PRODUCT_IMAGE).child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    fbStorage.reference.child(STORAGE_PRODUCT_IMAGE).child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }.addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }

    private fun uploadProduct(
        sellerId: String,
        title: String,
        price: Int,
        description: String,
        imageUri: String
    ) {
        val item = ProductModel(
            0,
            sellerId,
            System.currentTimeMillis(),
            title,
            price,
            description,
            imageUri
        )
        dbProduct.push().setValue(item)

        showProgress(false)
        finish()
    }

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1000
        private const val REQUEST_CODE_ACTION_GET_CONTENT = 2000
    }
}