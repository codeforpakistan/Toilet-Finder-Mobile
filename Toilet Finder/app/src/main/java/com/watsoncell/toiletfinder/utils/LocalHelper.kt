package com.watsoncell.toiletfinder.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import java.util.*

class LocalHelper{
    companion object{
        fun onAttach(context: Context) : Context?{
            val language = getPreferenceLanguage(context)
            return setLocale(context,language)
        }
/*
        fun onAttach(context: Context,defaultLanguage: String) : Context?{
            val language = getPreferenceLanguage(context,defaultLanguage)
            return setLocale(context,language)
        }
*/

        fun setLocale(context : Context,language : String) : Context? {
           //setting preference language
           val appPreference = AppPreference.getInstance(context)
           appPreference.setLanguage(language)

            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                updateResource(context, language)
            else
                updateResourceLegacy(context,language)
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun updateResource(context : Context, language: String): Context? {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val configuration = context.resources.configuration
            configuration.setLocale(locale)
          // configuration.setLayoutDirection(locale)

            return context.createConfigurationContext(configuration)
        }

        @SuppressWarnings("deprecation")
        private fun updateResourceLegacy(context : Context, language: String): Context? {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val resource = context.resources
            val configuration = resource.configuration
            configuration.locale = locale

            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                configuration.setLayoutDirection(locale)*/

            resource.updateConfiguration(configuration,resource.displayMetrics)
            return context
        }


        private fun getPreferenceLanguage(context: Context) : String {
            val appPreference = AppPreference.getInstance(context)
            return appPreference.getLanguage()
        }

    }

}