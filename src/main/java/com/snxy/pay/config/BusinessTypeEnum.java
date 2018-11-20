package com.snxy.pay.config;

/**
 * Created by 24398 on 2018/11/19.
 */
public enum BusinessTypeEnum {
    ENTRY_FEE(1,"进门费"),
    ENTRY_DEPOSIT_FEE(2,"进门费和押金"),
    REFUND_DEPOSIT_FEE(3,"退押金"),
    REFUND_ENTRY_FEE(4,"退进门费和押金"),
    CANCEL_PAY(5,"撤销支付订单");

    private Integer businessTypeId;
    private String  businessTypeDesc;

    BusinessTypeEnum(Integer businessTypeId,String businessTypeDesc){
        this.businessTypeId = businessTypeId;
        this.businessTypeDesc = businessTypeDesc;
    }


    public Integer getBusinessTypeId() {
        return this.businessTypeId;
    }

    public void setBusinessTypeId(Integer businessTypeId) {
        this.businessTypeId = businessTypeId;
    }

    public String getBusinessTypeDesc() {
        return businessTypeDesc;
    }

    public void setBusinessTypeDesc(String businessTypeDesc) {
        this.businessTypeDesc = businessTypeDesc;
    }

    @Override
    public String toString() {
        return "BusinessTypeEnum{" +
                "businessTypeId=" + businessTypeId +
                ", businessTypeDesc='" + businessTypeDesc + '\'' +
                '}';
    }


    public static String getDesc(Integer businessTypeId){
        String desc = null;
        BusinessTypeEnum [] enums = BusinessTypeEnum.values();
        for(int i =0;i < enums.length;i++){
            if(enums[i].getBusinessTypeId() == businessTypeId){
                desc = enums[i].getBusinessTypeDesc();
            }
        }
        return desc;
    }

}
