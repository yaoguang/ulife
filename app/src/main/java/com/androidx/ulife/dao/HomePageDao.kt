package com.androidx.ulife.dao

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HomePageDao {
    @Insert
    fun insert(parts: List<HomePagePart>)

    //    @Delete
    //    fun delete(part: HomePagePart)

    @Update(entity = HomePagePart::class)
    fun updateReqPart(part: HomePagePartRequest)

    @Query("select * from (select * from home_page_part order by -version,-update_time) group by part_type")
    fun queryParts(): List<HomePagePart>

    @Query("select * from home_page_part where part_type=:partType order by -version,-update_time limit 1")
    fun queryPart(partType: Int): HomePagePart?

    @Query("select id,part_type,version,update_time from home_page_part where part_type=:partType order by -version,-update_time limit 1")
    fun queryPartReq(partType: Int): HomePagePartRequest?

    @Query("select id,part_type,version,update_time from (select * from home_page_part order by -version,-update_time) group by part_type")
    fun queryPartReqList(): List<HomePagePartRequest>

    @Query("delete from home_page_part where id not in (select id from (select id,part_type from home_page_part order by -version,-update_time) group by part_type)")
    fun clearOldParts(): Int

    // 数据库有变化就会有回调
    @Query("select * from home_page_part where part_type=:partType order by -version,-update_time limit 1")
    fun queryPartLiveData(partType: Int): LiveData<HomePagePart?>
}