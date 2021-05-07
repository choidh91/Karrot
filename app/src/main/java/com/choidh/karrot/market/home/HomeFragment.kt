package com.choidh.karrot.market.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.choidh.karrot.market.DBKey
import com.choidh.karrot.market.DBKey.Companion.DB_PRODUCTS
import com.choidh.karrot.market.DBKey.Companion.DB_USERS
import com.choidh.karrot.market.R
import com.choidh.karrot.market.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var productAdapter: ProductAdapter
    private val fbAuth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val dbUser: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_USERS)
    }
    private val dbProduct: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_PRODUCTS)
    }
    private val productList = mutableListOf<ProductModel>()

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val productItem = snapshot.getValue(ProductModel::class.java)
            productItem ?: return

            productList.add(productItem)
            productAdapter.submitList(productList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(snapshot: DataSnapshot) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(error: DatabaseError) {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        initViews()
        initRecyclerView()

        productList.clear()

        dbProduct.addChildEventListener(listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        dbProduct.removeEventListener(listener)
    }

    private fun initViews() {
        binding.addFloatingButton.setOnClickListener {
           // if (auth.currentUser != null) {
                val intent = Intent(requireContext(), AddProductActivity::class.java)
                startActivity(intent)
           // } else {
             //   Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            //}
        }
    }

    private fun initRecyclerView() {
        productAdapter = ProductAdapter()
        binding.productRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.productRecyclerView.adapter = productAdapter
    }
}