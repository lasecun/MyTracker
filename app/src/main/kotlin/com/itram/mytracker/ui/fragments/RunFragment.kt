package com.itram.mytracker.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itram.mytracker.R
import com.itram.mytracker.adapter.RunAdapter
import com.itram.mytracker.databinding.FragmentRunBinding
import com.itram.mytracker.other.SortType
import com.itram.mytracker.ui.viewmodels.MainViewModel
import com.itram.mytracker.ui.viewmodels.RunEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RunFragment : Fragment() {

    private lateinit var binding: FragmentRunBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentRunBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun setupRecycleView() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    @RequiresApi(34)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!checkPermissions()) {
            requestMultiplePermissions.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }


        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

        setupRecycleView()

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                viewLifecycleOwner.lifecycleScope.launch {
                    when (pos) {
                        0 -> viewModel.onEvent(RunEvent.SortData(SortType.DATE))
                        1 -> viewModel.onEvent(RunEvent.SortData(SortType.RUNNING_TIME))
                        2 -> viewModel.onEvent(RunEvent.SortData(SortType.DISTANCE))
                        3 -> viewModel.onEvent(RunEvent.SortData(SortType.AVG_SPEED))
                        4 -> viewModel.onEvent(RunEvent.SortData(SortType.CALORIES_BURNED))
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest {
                    runAdapter.submitList(it.runs)
                }
            }
        }
    }

    @RequiresApi(34)
    private fun checkPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val postNotificationsPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.POST_NOTIFICATIONS
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
                && postNotificationsPermission == PackageManager.PERMISSION_GRANTED
                && backgroundLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            verifyBackgroundPermission()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("This permission is needed")
                .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun verifyBackgroundPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Show PoPUp with the explanation why we need Background Location.
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

}