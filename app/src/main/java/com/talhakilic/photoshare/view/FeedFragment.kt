package com.talhakilic.photoshare.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.talhakilic.photoshare.R
import com.talhakilic.photoshare.adapter.PostAdapter
import com.talhakilic.photoshare.databinding.FragmentFeedBinding
import com.talhakilic.photoshare.model.Post

class FeedFragment : Fragment(),PopupMenu.OnMenuItemClickListener {
    private var _binding : FragmentFeedBinding?=null
    private val binding get() = _binding!!
    private lateinit var popup : PopupMenu
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    val postList : ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth= Firebase.auth
        db = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.menuButton.setOnClickListener{menuTiklandi(it)}
        popup = PopupMenu(requireContext(),binding.menuButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.my_popup_menu,popup.menu)
        popup.setOnMenuItemClickListener(this)
        fireStoreVerileriAl()

        adapter = PostAdapter(postList)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter=adapter
    }

    private fun fireStoreVerileriAl() {
        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(requireContext(),"Veriler yüklenmedi: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (value != null && !value.isEmpty) {
                postList.clear()
                val documents = value.documents

                for (document in documents) {
                    val comment = document.getString("comment") ?: "Henüz Yorum Yapılmamış"
                    val email = document.getString("email") ?: "Kullanıcı Bilgisi Yok"
                    val downloadUrl = document.getString("downloadUrl") ?: ""
                    val likee = document.getLong("likes")?.toInt() ?: 0


                    val post = Post(email, comment, downloadUrl,likee)
                    postList.add(post)
                }

                adapter?.notifyDataSetChanged()
            }
        }
    }


    fun menuTiklandi(view:View){
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            if(item?.itemId == R.id.yuklemeItem){
                val action= FeedFragmentDirections.Companion.actionFeedFragmentToUploadFragment()
                Navigation.findNavController(requireView()).navigate(action)
            }else if(item?.itemId == R.id.cikisItem){
                auth.signOut()
                val action = FeedFragmentDirections.Companion.actionFeedFragmentToUserFragment()
                Navigation.findNavController(requireView()).navigate(action)
            }
            return true
        }
}