package com.watsoncell.publictoiletfinder.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences



class AppPreference{
    private val PREF_INSTRUCTION = "show instruction"
    private val PREF_USER_NAME = "USER_NAME"
    private val PREF_USER_EMAIL = "USER_EMAIL"
    private val PREF_SELECTED_LANG = "APP_LANGUAGE"
    private val PREF_APP_INTRO = "APP_INTRO"

    private val DEFAULT_LANGUAGE = "en"

    companion object{
        private val PREF_NAME = "Toilet Finder"
        private var preference : SharedPreferences? = null
        private var editor : SharedPreferences.Editor ?= null
        private var appPreference : AppPreference ?= null

        fun getInstance(context : Context) : AppPreference{
            if(appPreference == null){
                preference = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                editor = preference!!.edit()
                appPreference = AppPreference()
            }
            return appPreference!!
        }
    }

    fun setInstructionDialogVisibility(isShown : Boolean){
        editor!!.putBoolean(PREF_INSTRUCTION,isShown)
        editor!!.apply()
    }

    fun isToShowInstructionDialog() : Boolean {
        return preference!!.getBoolean(PREF_INSTRUCTION,true)
    }

    fun addUserName(userName : String){
        editor!!.putString(PREF_USER_NAME,userName)
        editor!!.apply()

    }

    fun getUserName() : String {
        return preference!!.getString(PREF_USER_NAME,"")
    }

    fun addUserEmail(userEmail : String){
        editor!!.putString(PREF_USER_EMAIL,userEmail).apply()
    }

    fun getUserEmail() : String {
        return preference!!.getString(PREF_USER_EMAIL,"")
    }

    fun setLanguage(language : String){
        editor!!.putString(PREF_SELECTED_LANG, language)
        editor!!.commit()
    }

    fun getLanguage() : String{
        return preference!!.getString(PREF_SELECTED_LANG,DEFAULT_LANGUAGE)
    }

    fun setAppIntro(isShown : Boolean){
        editor!!.putBoolean(PREF_APP_INTRO,isShown)
        editor!!.commit()
    }

    fun isAppIntroShown() : Boolean{
        return preference!!.getBoolean(PREF_APP_INTRO,false)
    }
}
