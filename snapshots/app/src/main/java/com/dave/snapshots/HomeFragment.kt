package com.dave.snapshots

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dave.snapshots.databinding.FragmentHomeBinding
import com.dave.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment() {

    private lateinit var nbinding: FragmentHomeBinding
    private lateinit var nFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>
    private lateinit var nLayoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        nbinding = FragmentHomeBinding.inflate(inflater, container, false)
        return nbinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = FirebaseDatabase.getInstance().reference.child("snapshots")

        val options = FirebaseRecyclerOptions.Builder<Snapshot>()
            .setQuery(query, Snapshot::class.java).build()

        nFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options){
            private lateinit var nContext: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {

                nContext = parent.context
                val view = LayoutInflater.from(nContext)
                    .inflate(R.layout.item_snapshot, parent, false)

                return SnapshotHolder(view)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {

                val snapshot = getItem(position)

                with(holder){
                    setListener(snapshot)

                    binding.tvTitle.text = snapshot.title
                    Glide.with(nContext)
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgPhoto)
                }

            }

            @SuppressLint("NotifyDataSetChanged") //Error interno firebase ui en v 8.0.0
            override fun onDataChanged() {
                super.onDataChanged()
                nbinding.progressBar.visibility = View.GONE
                notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(nContext, error.message, Toast.LENGTH_SHORT).show()
            }
        }

        nLayoutManager = LinearLayoutManager(context)
        nbinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = nLayoutManager
            adapter = nFirebaseAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        nFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        nFirebaseAdapter.stopListening()
    }

    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view){

        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot){

        }
    }

}