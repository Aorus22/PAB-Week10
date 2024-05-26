package com.example.ppab_10_l0122018_alyzakhoirunnadif.ui

import UserPreference
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ppab_10_l0122018_alyzakhoirunnadif.R
import com.example.ppab_10_l0122018_alyzakhoirunnadif.UserModel
import com.example.ppab_10_l0122018_alyzakhoirunnadif.databinding.ActivityFormBinding
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
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
        private const val FIELD_REQUIRED = "Field tidak boleh kosong"
        private const val FIELD_DIGIT_ONLY = "Hanya boleh terisi numerik"
        private const val FIELD_IS_NOT_VALID = "Email tidak valid"
        private const val PICK_IMAGE_REQUEST = 1
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

        if (!userModel.profileImage.isNullOrEmpty()) {
            binding.ivProfile.setImageURI(Uri.parse(userModel.profileImage))
        }
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
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
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

        saveUser(name, email, age, phoneNo, gender, selectedImageUri.toString())
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveUser(name: String, email: String, age: String, phoneNo: String, gender: String, selectedImageUri:String) {
        val userPreference = UserPreference(this)

        val oldName = userModel.name ?: ""
        val oldEmail = userModel.email ?: ""
        val oldAge = userModel.age.toString()
        val oldPhoneNo = userModel.phoneNumber ?: ""
        val oldGender = userModel.gender ?: ""
        val oldProfileImage = userModel.profileImage ?: ""

        userModel.name = name
        userModel.email = email
        userModel.age = age.toInt()
        userModel.phoneNumber = phoneNo
        userModel.gender = gender

        if (selectedImageUri.isNotEmpty() && oldProfileImage.isNotEmpty()) {
            val oldImageFile = File(oldProfileImage)
            if (oldImageFile.exists()) {
                oldImageFile.delete()
            }
        }

        userModel.profileImage = selectedImageUri

        userPreference.setUser(userModel)
        Toast.makeText(this, "Data tersimpan", Toast.LENGTH_SHORT).show()

        val changeLog = StringBuilder()
        if (name != oldName) {
            changeLog.append("Name: $oldName -> $name\n")
        }
        if (email != oldEmail) {
            changeLog.append("Email: $oldEmail -> $email\n")
        }
        if (age != oldAge) {
            changeLog.append("Age: $oldAge -> $age\n")
        }
        if (phoneNo != oldPhoneNo) {
            changeLog.append("No Handphone: $oldPhoneNo -> $phoneNo\n")
        }
        if (gender != oldGender) {
            changeLog.append("Gender: $oldGender -> $gender\n")
        }
        if (userModel.profileImage != oldProfileImage) {
            changeLog.append("Profile Photo Changed\n")
        }

        if (changeLog.isNotEmpty()) {
            saveChangeLogToFile(changeLog.toString())
        }
    }


    private fun saveChangeLogToFile(changeLog: String) {
        val directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val logFile = File(directory, "changelog.txt")

        try {
            if (!logFile.exists()) {
                logFile.createNewFile()
            }

            val writer = FileWriter(logFile, true)
            writer.appendLine("Changes made at ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
            writer.appendLine(changeLog)
            writer.appendLine("---------------------------------------------")
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                val imageFile = saveImageToFile(uri)
                if (imageFile != null) {
                    binding.ivProfile.setImageURI(uri)
                    selectedImageUri = imageFile.absolutePath
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageToFile(uri: Uri): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "profilephoto$timeStamp.jpg"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, imageFileName)

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(imageFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            inputStream?.close()
            outputStream.close()
            return imageFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
