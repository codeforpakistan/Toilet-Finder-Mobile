package com.watsoncell.toiletfinder.Interface

interface SearchToiletListener{
    fun searchToiletViewClickListener(position: Int)
    fun noSearchedToiletIsFound(isNotEmpty : Boolean)
}