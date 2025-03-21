package com.talhakilic.photoshare.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.talhakilic.photoshare.databinding.FragmentUserBinding


class UserFragment : Fragment() {
    private var _binding : FragmentUserBinding?=null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater,container,false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.girisButton.setOnClickListener{signIn(it)}
        binding.kayitButton.setOnClickListener{signUp(it)}

        val currentUser = auth.currentUser
        if(currentUser !=null){
            val action = UserFragmentDirections.Companion.actionUserFragmentToFeedFragment()
            Navigation.findNavController(view).navigate(action)
        }

    }
    fun signUp(view: View){

        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    val action = UserFragmentDirections.Companion.actionUserFragmentToFeedFragment()
                    Navigation.findNavController(view).navigate(action)
                }
            }.addOnFailureListener{exception ->
                Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(requireContext(),"Email veya password boş geçilemez", Toast.LENGTH_LONG).show()
        }
    }
    fun signIn(view: View){
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()
        if(email.isNotEmpty() && password.isNotEmpty()){
            auth.signInWithEmailAndPassword(binding.emailText.text.toString(), binding.passwordText.text.toString()).addOnSuccessListener{
                val action = UserFragmentDirections.Companion.actionUserFragmentToFeedFragment()
                Navigation.findNavController(view).navigate(action)
            }.addOnFailureListener{
                Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}