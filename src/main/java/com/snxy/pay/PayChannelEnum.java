package com.snxy.pay;

/**
 * Created by 24398 on 2018/11/19.
 */
public enum PayChannelEnum {
    ZXWX(1,"中信微信"),
    ZXALI(2,"中信支付宝"),
    ZXWITHHOLD(2,"中信银行代扣");

    private Integer channelId;
    private String  channelDesc;

    PayChannelEnum(Integer channelId,String channelDesc){
        this.channelId = channelId;
        this.channelDesc = channelDesc;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getChannelDesc() {
        return channelDesc;
    }

    public void setChannelDesc(String channelDesc) {
        this.channelDesc = channelDesc;
    }


    @Override
    public String toString() {
        return "PayChannelEnum{" +
                "channelId=" + channelId +
                ", channelDesc='" + channelDesc + '\'' +
                '}';
    }


    public static String getDesc(Integer channelId){
        String desc = null;
        PayChannelEnum[] enums = PayChannelEnum.values();
        for(int i=0;i< enums.length;i++){
            if(enums[i].getChannelId() == channelId){
                desc = enums[i].getChannelDesc();
            }
        }
        return desc;
    }
}
