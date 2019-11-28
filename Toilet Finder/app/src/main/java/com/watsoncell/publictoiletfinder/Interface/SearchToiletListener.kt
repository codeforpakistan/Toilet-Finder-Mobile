package com.watsoncell.publictoiletfinder.Interface

interface SearchToiletListener{
    fun searchToiletViewClickListener(position: Int)
    fun noSearchedToiletIsFound(isNotEmpty : Boolean)
}