package com.androidx.ulife.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface HomeUssdDao {
    @Insert
    fun insert(parts: List<HomeUssdPart>)

    @Insert
    fun insert(part: HomeUssdPart)

    @Update(entity = HomeUssdPart::class)
    fun updateReqPart(part: HomeUssdPartRequest)

    @Query("select * from home_ussd_part where mcc=:mcc and mnc=:mnc order by -version,-update_time limit 1")
    fun queryPart(mcc: Int, mnc: Int): HomeUssdPart?

    @Query("select id,mcc,mnc,version,update_time from home_ussd_part where mcc=:mcc and mnc=:mnc order by -version,-update_time limit 1")
    fun queryPartReq(mcc: Int, mnc: Int): HomeUssdPartRequest?

    /**
     * mccMnc = mcc * 1000 + mnc
     */
    @Query("select id,mcc,mnc,version,update_time from (select id,mcc,mnc,version,update_time,(mcc*1000+mnc) as mcc_mnc from home_ussd_part where mcc_mnc in(:mccMnc) order by -version,-update_time) group by mcc_mnc")
    fun queryPartReqList(mccMnc: List<Int>): List<HomeUssdPartRequest>

    @Query("delete from home_ussd_part where id not in (select id from (select id,(mcc*1000+mnc) as mcc_mnc from home_ussd_part order by -version,-update_time) group by mcc_mnc)")
    fun clearOldParts(): Int

    @Query("update home_ussd_part set version=1")
    fun resetVersion():Int
}