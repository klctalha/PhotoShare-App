package com.talhakilic.photoshare

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.talhakilic.photoshare.databinding.FragmentFeedBinding
import com.talhakilic.photoshare.databinding.FragmentUploadBinding
import java.util.UUID


class UploadFragment : Fragment() {
    private var _binding : FragmentUploadBinding?=null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    var secilenGorsel :Uri?=null
    var secilenBitMap : Bitmap?= null

    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var db : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLaunchers()
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUploadBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener{gorselSec(it)}
        binding.yukleButton.setOnClickListener{tiklaYuklendi(it)}

    }

    fun tiklaYuklendi(view:View){
        val uuid = UUID.randomUUID()
        val gorselAdi = "${uuid}.jpg"
       val reference = storage.reference
        val gorselRefereansi = reference.child("images").child(gorselAdi)
        if(secilenGorsel != null){
            gorselRefereansi.putFile(secilenGorsel!!).addOnSuccessListener{uploadTask->

                gorselRefereansi.downloadUrl.addOnSuccessListener{uri->
                   if(auth.currentUser !=null){
                       val downloadUrl = uri.toString()
                       val hashMap = hashMapOf<String,Any>()
                       hashMap.put("downloadUrl",downloadUrl)
                       hashMap.put("email",auth.currentUser?.email.toString())
                       hashMap.put("comment", binding.commentText.text.toString())
                       hashMap.put("date", Timestamp.now())

                       db.collection("Posts").add(hashMap).addOnSuccessListener{documentReferences->
                           val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                           Navigation.findNavController(view).navigate(action)
                       }.addOnFailureListener{exception->
                           Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                       }
                   }
                }
            }.addOnFailureListener{exception->
                Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun gorselSec(view:View){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){

            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) !=
                PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view, "Galeriye gitmek İzin vermeniz gerekmektedir",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver",
                        View.OnClickListener{
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }else{
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
            }
        }
        else{
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view, "İzin vermeniz gerekmektedir",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver",
                        View.OnClickListener{
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                }else{
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
            }

        }
    }
    private fun registerLaunchers() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode ==RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult !=null){
                    secilenGorsel = intentFromResult.data
                    try{
                        if(Build.VERSION.SDK_INT>=28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                            secilenBitMap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitMap)
                        }else{
                            secilenBitMap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitMap)
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
            }else{
                Toast.makeText(requireContext(),"İzin vermediniz, izne ihtiyacımız var", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}