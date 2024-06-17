package com.homefirstindia.rmproserver.repository.v1

import com.homefirstindia.rmproserver.model.v1.common.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AddressRepository : JpaRepository<Address, String> {

    @Query("from Address where id in :ids group by latitude, longitude")
    fun getAddress(
        ids: ArrayList<String>
    ): ArrayList<Address>?

}