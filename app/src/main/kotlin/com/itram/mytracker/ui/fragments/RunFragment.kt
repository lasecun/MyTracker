package com.itram.mytracker.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.itram.mytracker.R
import com.itram.mytracker.databinding.FragmentRunBinding
import com.itram.mytracker.other.Constants.LOCATION_PERMISSIONS_REQUEST_CODE
import com.itram.mytracker.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    private lateinit var binding: FragmentRunBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentRunBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

        if (checkPermissions()) {
            // CONTINUE
        } else {
            requestLocationPermissions()
        }

    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun checkPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        return fineLocationPermission == PackageManager.PERMISSION_GRANTED
                && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
                && backgroundLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    verifyBackgroundPermission()
                }else{
                    // CONTINUE
                }
            }
        }
    }

    private fun verifyBackgroundPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // CONTINUE
        } else {
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            startActivity(intent)
        }
    }

}