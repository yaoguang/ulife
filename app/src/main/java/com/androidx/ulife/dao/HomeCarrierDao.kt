package com.androidx.ulife.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface HomeCarrierDao {
    @Insert
    fun insert(parts: List<HomeCarrierPart>)

    @Insert
    fun insert(part: HomeCarrierPart)

    @Update(entity = HomeCarrierPart::class)
    fun updateReqPart(part: HomeCarrierPartRequest)

    @Query("select * from home_carrier_part where mcc=:mcc and mnc=:mnc order by -version,-update_time limit 1")
    fun queryPart(mcc: Int, mnc: Int): HomeCarrierPart?

    @Query("select id,mcc,mnc,version,update_time from home_carrier_part where mcc=:mcc and mnc=:mnc order by -version,-update_time limit 1")
    fun queryPartReq(mcc: Int, mnc: Int): HomeCarrierPartRequest?

    /**
     * mccMnc = mcc * 1000 + mnc
     */
    @Query("select id,mcc,mnc,version,update_time from (select id,mcc,mnc,version,update_time,(mcc*1000+mnc) as mcc_mnc from home_carrier_part where mcc_mnc in(:mccMnc) order by -version,-update_time) group by mcc_mnc")
    fun queryPartReqList(mccMnc: List<Int>): List<HomeCarrierPartRequest>

    @Query("delete from home_carrier_part where id not in (select id from (select id,(mcc*1000+mnc) as mcc_mnc from home_carrier_part order by -version,-update_time) group by mcc_mnc)")
    fun clearOldParts(): Int

    @Query("update home_carrier_part set version=1")
    fun resetVersion():Int
}