package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChildDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildren(children: List<Child>)

    @Query("SELECT * FROM children WHERE id = :childId")
    suspend fun getChildById(childId: String): Child?

    @Query("SELECT * FROM children")
    suspend fun getAllChildren(): List<Child>

    @Delete
    suspend fun deleteChild(child: Child)
}