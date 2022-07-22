package com.dave.snapshots

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dave.snapshots.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 21

    private lateinit var nbinding: ActivityMainBinding
    private lateinit var nActiveFragment: Fragment
    private lateinit var nFragmentManager: FragmentManager

    private  lateinit var nAuthListener: FirebaseAuth.AuthStateListener
    private var nFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        nbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(nbinding.root)

        setupAuth()
        setupBottomNav()
    }

    private fun setupAuth() {
        nFirebaseAuth = FirebaseAuth.getInstance()
        nAuthListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if (user == null){
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(
                        Arrays.asList(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build())
                    ).build(), RC_SIGN_IN
                )
            }
        }
    }

    private fun setupBottomNav(){

        nFragmentManager = supportFragmentManager

        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        nActiveFragment = homeFragment

        nFragmentManager.beginTransaction()
            .add(R.id.hostFragment, profileFragment, ProfileFragment::class.java.name)
            .hide(profileFragment).commit()

        nFragmentManager.beginTransaction()
            .add(R.id.hostFragment, addFragment, AddFragment::class.java.name)
            .hide(addFragment).commit()

        nFragmentManager.beginTransaction()
            .add(R.id.hostFragment, homeFragment, HomeFragment::class.java.name)
            .commit()

        nbinding.bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    nFragmentManager.beginTransaction().hide(nActiveFragment).show(homeFragment).commit()
                    nActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {
                    nFragmentManager.beginTransaction().hide(nActiveFragment).show(addFragment).commit()
                    nActiveFragment = addFragment
                    true
                }
                R.id.action_profile -> {
                    nFragmentManager.beginTransaction().hide(nActiveFragment).show(profileFragment).commit()
                    nActiveFragment = profileFragment
                    true
                }
                else -> false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        nFirebaseAuth?.addAuthStateListener(nAuthListener)
    }

    override fun onPause() {
        super.onPause()
        nFirebaseAuth?.removeAuthStateListener(nAuthListener)
    }
}