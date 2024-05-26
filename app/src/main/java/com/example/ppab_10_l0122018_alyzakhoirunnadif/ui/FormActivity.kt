package com.example.ppab_10_l0122018_alyzakhoirunnadif.ui

import UserPreference
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ppab_10_l0122018_alyzakhoirunnadif.FileHelper
import com.example.ppab_10_l0122018_alyzakhoirunnadif.FileModel
import com.example.ppab_10_l0122018_alyzakhoirunnadif.R
import com.example.ppab_10_l0122018_alyzakhoirunnadif.UserModel
import com.example.ppab_10_l0122018_alyzakhoirunnadif.databinding.ActivityFormBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class FormActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFormBinding
    private var selectedImageUri: String? = null

    companion object {
        const val EXTRA_TYPE_FORM = "extra_type_form"
        const val EXTRA_RESULT = "extra_result"
        const val RESULT_CODE = 101
        const val TYPE_ADD = 1
        const val TYPE_EDIT = 2
    }

    private lateinit var userModel: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener(this)
        binding.btnChooseImage.setOnClickListener(this)

        userModel = intent.getParcelableExtra<UserModel>("USER") as UserModel
        val formType = intent.getIntExtra(EXTRA_TYPE_FORM, 0)

        var actionBarTitle = ""
        var btnTitle = ""

        when (formType) {
            TYPE_ADD -> {
                actionBarTitle = "Add New"
                btnTitle = "Save"
            }
            TYPE_EDIT -> {
                actionBarTitle = "Change"
                btnTitle = "Update"
                showPreferenceInForm()
            }
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSave.text = btnTitle
    }

    private fun showPreferenceInForm() {
        binding.edtName.setText(userModel.name)
        binding.edtEmail.setText(userModel.email)
        binding.edtAge.setText(userModel.age.toString())
        binding.edtPhone.setText(userModel.phoneNumber)
        if (userModel.gender == "Male") {
            binding.rbMale.isChecked = true
        } else {
            binding.rbFemale.isChecked = true
        }
        if (!userModel.profileImage.isNullOrEmpty()) {
            selectedImageUri = userModel.profileImage!!
            binding.ivProfile.setImageURI(Uri.parse(selectedImageUri))
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_save -> {
                validateAndSaveUser()
            }
            R.id.btn_choose_image -> {
                chooseImage()
            }
        }
    }

    private fun validateAndSaveUser() {
        val name = binding.edtName.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val age = binding.edtAge.text.toString().trim()
        val phoneNo = binding.edtPhone.text.toString().trim()
        val gender = if (binding.rbMale.isChecked) "Male" else "Female"

        if (name.isEmpty() || email.isEmpty() || age.isEmpty() || phoneNo.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.length > 50) {
            Toast.makeText(this, "Name must be at most 50 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!TextUtils.isDigitsOnly(age) || !TextUtils.isDigitsOnly(phoneNo)) {
            Toast.makeText(this, "Age and Phone Number must be numeric", Toast.LENGTH_SHORT).show()
            return
        }

        if (!binding.rbMale.isChecked && !binding.rbFemale.isChecked) {
            Toast.makeText(this, "Gender must be selected", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Selected image is empty", Toast.LENGTH_SHORT).show()
            return
        }

        saveUser(name, email, age, phoneNo, gender, selectedImageUri!!)
        val intent = Intent()
        intent.putExtra(EXTRA_RESULT, userModel)
        setResult(RESULT_CODE, intent)
        finish()
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveUser(name: String, email: String, age: String, phoneNo: String, gender: String, selectedImageUri: String) {
        val userPreference = UserPreference(this)

        val oldUserModel = UserModel(
            name = userModel.name ?: "",
            email = userModel.email ?: "",
            age = userModel.age,
            phoneNumber = userModel.phoneNumber ?: "",
            gender = userModel.gender ?: "",
            profileImage = userModel.profileImage ?: ""
        )

        userModel.name = name
        userModel.email = email
        userModel.age = age.toInt()
        userModel.phoneNumber = phoneNo
        userModel.gender = gender

        if (selectedImageUri != oldUserModel.profileImage && selectedImageUri.isNotEmpty()) {
            userModel.profileImage = selectedImageUri

            if (oldUserModel.profileImage!!.isNotEmpty()) {
                val oldImageFile = oldUserModel.profileImage?.let { File(it) }
                oldImageFile?.takeIf { it.exists() }?.delete()
            }
        }

        userPreference.setUser(userModel)
        Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show()

        generateChangeLog(oldUserModel, userModel)
    }

    private fun generateChangeLog(oldUserModel: UserModel, newUserModel: UserModel) {
        val changeLog = StringBuilder()

        val properties = UserModel::class.java.declaredFields
        for (field in properties) {
            field.isAccessible = true
            val oldValue = field.get(oldUserModel)
            val newValue = field.get(newUserModel)
            if (field.name != "profileImage") {
                if (oldValue != newValue) {
                    changeLog.append("${field.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }}: ${oldValue ?: ""} -> ${newValue ?: ""}\n")
                }
            }
        }

        if (newUserModel.profileImage != oldUserModel.profileImage) {
            changeLog.append("Profile Photo Changed\n")
        }

        if (changeLog.isNotEmpty()) {
            saveChangeLogToFile(changeLog.toString())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveImageToFile(uri)?.let { imageFile ->
                    binding.ivProfile.setImageURI(uri)
                    selectedImageUri = imageFile.absolutePath
                } ?: run {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateImageSize(uri: Uri): Boolean {
        val contentResolver = applicationContext.contentResolver
        var inputStream: InputStream? = null
        var totalBytesRead = 0

        try {
            inputStream = contentResolver.openInputStream(uri)
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int

            while (inputStream?.read(buffer, 0, bufferSize).also { bytesRead = it!! } != -1) {
                totalBytesRead += bytesRead
                if (totalBytesRead > 5 * 1024 * 1024) {
                    Toast.makeText(this, "Maximum image size is 5 MB", Toast.LENGTH_SHORT).show()
                    return false
                }
            }

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }

        return false
    }

    private fun saveImageToFile(uri: Uri): File? {
        if (!validateImageSize(uri)) {
            return null
        }

        val contentResolver = applicationContext.contentResolver
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        val imageFile: File?

        try {
            inputStream = contentResolver.openInputStream(uri)
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "profilephoto$timeStamp.jpg"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            imageFile = File(storageDir, imageFileName)
            inputStream?.close()

            inputStream = contentResolver.openInputStream(uri)
            outputStream = FileOutputStream(imageFile)

            while (inputStream?.read(buffer, 0, bufferSize).also { bytesRead = it!! } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            return imageFile
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }

        return null
    }

    private fun saveChangeLogToFile(changeLog: String) {
        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val newLog = StringBuilder().apply {
            append("Changes made at $timestamp\n")
            append(changeLog).append("\n")
            append("---------------------------------------------\n")
        }.toString()

        val fileModel = FileModel().apply {
            filename = "changelog.txt"
            data = newLog
        }

        try {
            FileHelper.prependToFile(this, fileModel)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save change log", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!userModel.profileImage.isNullOrEmpty() && userModel.profileImage != selectedImageUri) {
            val oldImageFile = selectedImageUri?.let { File(it) }
            oldImageFile?.takeIf { it.exists() }?.delete()
        }
    }
}
