package com.dave.snapshots

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dave.snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


@Suppress("DEPRECATION")
class AddFragment : Fragment() {

    private val RC_GALLERY = 18
    private val PATH_SNAPSHOT = "snapshots"

    private lateinit var nStorageReference: StorageReference
    private lateinit var nDataBaseReference: DatabaseReference
    private lateinit var nBinding: FragmentAddBinding

    private var nPhotoSelectedUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        nBinding = FragmentAddBinding.inflate(inflater, container, false)
      return nBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nBinding.btnPost.setOnClickListener{ postSnapshot() }

        nBinding.btnSelect.setOnClickListener{ openGallery() }

        nStorageReference = FirebaseStorage.getInstance().reference
        nDataBaseReference = FirebaseDatabase.getInstance()
            .reference.child(PATH_SNAPSHOT)

    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GALLERY)
    }

    private fun postSnapshot(){
        nBinding.progressBar.visibility = View.VISIBLE
        val key = nDataBaseReference.push().key!!
        val storageReference = nStorageReference.child(PATH_SNAPSHOT).child("my_photo")
        if (nPhotoSelectedUri != null){
        storageReference.putFile(nPhotoSelectedUri!!)
            .addOnProgressListener {
                val progress = (100 * it.bytesTransferred/it.totalByteCount).toDouble()
                nBinding.progressBar.progress = progress.toInt()
                nBinding.tvMessage.text = "$progress%"
            }
            .addOnCompleteListener{
                nBinding.progressBar.visibility = View.INVISIBLE
            }
            .addOnSuccessListener {
                Snackbar.make(nBinding.root, "Foto almacenada.",
                    Snackbar.LENGTH_SHORT).show()
                it.storage.downloadUrl.addOnSuccessListener {
                    saveSnapshot(key,it.toString(), nBinding.etTitle.text.toString().trim())
                    nBinding.tilTitle.visibility = View.GONE
                    nBinding.tvMessage.text = getString(R.string.post_message_title)
                }
            }
            .addOnFailureListener{
                Snackbar.make(nBinding.root, "No se pudo almacenar.",
                    Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSnapshot(key: String, url: String, title: String){
        val snapshot = Snapshot(title = title, photoUrl = url)
        nDataBaseReference.child(key).setValue(snapshot)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == RC_GALLERY){
                nPhotoSelectedUri = data?.data
                nBinding.imgPhoto.setImageURI(nPhotoSelectedUri)
                nBinding.tilTitle.visibility = View.VISIBLE
                nBinding.tvMessage.text = getString(R.string.post_message_valid_title)
            }
        }
    }


}