package com.example.ppab_10_l0122018_alyzakhoirunnadif.ui

import UserPreference
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.View.VISIBLE
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.example.ppab_10_l0122018_alyzakhoirunnadif.FileHelper
import com.example.ppab_10_l0122018_alyzakhoirunnadif.MainViewModel
import com.example.ppab_10_l0122018_alyzakhoirunnadif.R
import com.example.ppab_10_l0122018_alyzakhoirunnadif.SettingPreferences
import com.example.ppab_10_l0122018_alyzakhoirunnadif.UserModel
import com.example.ppab_10_l0122018_alyzakhoirunnadif.ViewModelFactory
import com.example.ppab_10_l0122018_alyzakhoirunnadif.dataStore
import com.example.ppab_10_l0122018_alyzakhoirunnadif.databinding.ActivityMainBinding
import com.google.android.material.switchmaterial.SwitchMaterial
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mUserPreference: UserPreference
    private lateinit var binding: ActivityMainBinding

    private var isPreferenceEmpty = false
    private lateinit var userModel: UserModel

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.data != null && result.resultCode == FormActivity.RESULT_CODE) {
            userModel = result.data?.getParcelableExtra<UserModel>(FormActivity.EXTRA_RESULT) as UserModel
            populateView(userModel)
            checkForm(userModel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "My User Preference"

        mUserPreference = UserPreference(this)

        showExistingPreference()

        binding.btnSave.setOnClickListener(this)

        binding.btnToggleChangeLog.setOnClickListener {
            if (binding.scrollView.visibility == View.GONE) {
                displayChangeLog()
            } else {
                hideChangeLog()
            }
        }

        val switchTheme = findViewById<SwitchMaterial>(R.id.switch_theme)

        val pref = SettingPreferences.getInstance(application.dataStore)
        val mainViewModel = ViewModelProvider(this,
            ViewModelFactory(pref))[MainViewModel::class.java]
        mainViewModel.getThemeSettings().observe(this) { isDarkModeActive: Boolean ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                switchTheme.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                switchTheme.isChecked = false
            }
        }

        switchTheme.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            mainViewModel.saveThemeSetting(isChecked)
        }

    }

    private fun showExistingPreference() {
        userModel = mUserPreference.getUser()
        populateView(userModel)
        checkForm(userModel)
    }

    private fun populateView(userModel: UserModel) {
        binding.tvName.text =
            if (userModel.name.toString().isEmpty()) "Tidak Ada" else userModel.name
        binding.tvAge.text =
            userModel.age.toString().ifEmpty { "Tidak Ada" }
        binding.tvGender.text =
            if (userModel.gender.isNullOrEmpty()) "Tidak Ada" else userModel.gender
        binding.tvEmail.text =
            if (userModel.email.toString().isEmpty()) "Tidak Ada" else userModel.email
        binding.tvPhone.text =
            if (userModel.phoneNumber.toString().isEmpty()) "Tidak Ada" else userModel.phoneNumber

        if (!userModel.profileImage.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(userModel.profileImage)
                binding.ivProfileImage.setImageURI(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.ivProfileImage.setImageResource(R.drawable.ic_person)
            }
        } else {
            binding.ivProfileImage.setImageResource(R.drawable.ic_person)
        }
    }

    private fun checkForm(userModel: UserModel) {
        when {
            userModel.name.toString().isNotEmpty() -> {
                binding.btnSave.text = getString(R.string.change)
                isPreferenceEmpty = false
            }
            else -> {
                binding.btnSave.text = getString(R.string.input_data)
                isPreferenceEmpty = true
            }
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_save) {
            val intent = Intent(this@MainActivity, FormActivity::class.java)
            when {
                isPreferenceEmpty -> {
                    intent.putExtra(
                        FormActivity.EXTRA_TYPE_FORM,
                        FormActivity.TYPE_ADD
                    )
                    intent.putExtra("USER", userModel)
                }
                else -> {
                    intent.putExtra(
                        FormActivity.EXTRA_TYPE_FORM,
                        FormActivity.TYPE_EDIT
                    )
                    intent.putExtra("USER", userModel)
                }
            }
            resultLauncher.launch(intent)
        }
    }

    private fun displayChangeLog() {
        val directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val changeLogFile = File(directory, "changelog.txt")

        if (changeLogFile.exists()) {
            try {
                val fileModel = FileHelper.readFromFile(this, "changelog.txt")
                binding.tvChangeLog.text = fileModel.data
                binding.btnToggleChangeLog.text = getString(R.string.hide_change_log)
                binding.scrollView.visibility = VISIBLE
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to read changelog", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Changelog file does not exist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideChangeLog() {
        binding.scrollView.visibility = View.GONE
        binding.btnToggleChangeLog.text = getString(R.string.show_change_log)
    }

    override fun onResume() {
        super.onResume()
        hideChangeLog()
    }
}
