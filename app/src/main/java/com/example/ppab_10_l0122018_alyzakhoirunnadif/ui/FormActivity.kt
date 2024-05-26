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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FormActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFormBinding

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
        private const val REQUEST_CODE_PERMISSION = 101
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
                actionBarTitle = "Tambah Baru"
                btnTitle = "Simpan"
            }
            TYPE_EDIT -> {
                actionBarTitle = "Ubah"
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
                val name = binding.edtName.text.toString().trim()
                val email = binding.edtEmail.text.toString().trim()
                val age = binding.edtAge.text.toString().trim()
                val phoneNo = binding.edtPhone.text.toString().trim()
                val gender = if (binding.rbMale.isChecked) "Male" else "Female"

                if (name.isEmpty()) {
                    binding.edtName.error = FIELD_REQUIRED
                    return
                }

                if (email.isEmpty()) {
                    binding.edtEmail.error = FIELD_REQUIRED
                    return
                }

                if (!isValidEmail(email)) {
                    binding.edtEmail.error = FIELD_IS_NOT_VALID
                    return
                }

                if (age.isEmpty()) {
                    binding.edtAge.error = FIELD_REQUIRED
                    return
                }

                if (phoneNo.isEmpty()) {
                    binding.edtPhone.error = FIELD_REQUIRED
                    return
                }

                if (!TextUtils.isDigitsOnly(phoneNo)) {
                    binding.edtPhone.error = FIELD_DIGIT_ONLY
                    return
                }

                saveUser(name, email, age, phoneNo, gender)

                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_RESULT, userModel)
                setResult(RESULT_CODE, resultIntent)

                finish()
            }
            R.id.btn_choose_image -> {
                chooseImage()
            }
        }
    }

    private fun saveUser(name: String, email: String, age: String, phoneNo: String, gender: String) {
        val userPreference = UserPreference(this)

        userModel.name = name
        userModel.email = email
        userModel.age = age.toInt()
        userModel.phoneNumber = phoneNo
        userModel.gender = gender

        userPreference.setUser(userModel)
        Toast.makeText(this, "Data tersimpan", Toast.LENGTH_SHORT).show()
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
                    userModel.profileImage = imageFile.absolutePath
                } else {
                    Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageToFile(uri: Uri): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        )
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
