package com.pfeiffer.expenses.model;

import java.util.Date;

/**
 * Created by axelpfeiffer on 26.04.14.
 */
public class PartnerDevice {

    private int id_;
    private String androidId_;
    private Date lastSynchronization_;

    public PartnerDevice(int id, String androidId, Date lastSynchronization) {
        id_ = id;
        androidId_ = androidId;
        lastSynchronization_ = lastSynchronization;
    }

    public int getId() {
        return id_;
    }

    public String getAndroidId() {
        return androidId_;
    }

    public Date getLastSynchronization() {
        return lastSynchronization_;
    }

    public String toString(){return "PartnerDevice (id="+id_+", androidId="+androidId_+
            ", lastSychronization="+lastSynchronization_.toString()+")";}
}
