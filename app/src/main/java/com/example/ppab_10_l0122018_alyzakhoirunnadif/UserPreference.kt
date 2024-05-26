import android.content.Context
import android.content.SharedPreferences
import com.example.ppab_10_l0122018_alyzakhoirunnadif.UserModel

class UserPreference(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "user_pref"
        private const val NAME = "name"
        private const val EMAIL = "email"
        private const val AGE = "age"
        private const val PHONE_NUMBER = "phone_number"
        private const val GENDER = "gender"
        private const val PROFILE_IMAGE = "profile_image"
    }

    fun setUser(user: UserModel) {
        val editor = preferences.edit()
        editor.putString(NAME, user.name)
        editor.putString(EMAIL, user.email)
        editor.putInt(AGE, user.age ?: 0)
        editor.putString(PHONE_NUMBER, user.phoneNumber)
        editor.putString(GENDER, user.gender)
        editor.putString(PROFILE_IMAGE, user.profileImage)
        editor.apply()
    }

    fun getUser(): UserModel {
        val user = UserModel()
        user.name = preferences.getString(NAME, "")
        user.email = preferences.getString(EMAIL, "")
        user.age = preferences.getInt(AGE, 0)
        user.phoneNumber = preferences.getString(PHONE_NUMBER, "")
        user.gender = preferences.getString(GENDER, "")
        user.profileImage = preferences.getString(PROFILE_IMAGE, "")
        return user
    }
}
