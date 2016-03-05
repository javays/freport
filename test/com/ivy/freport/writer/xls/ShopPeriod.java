package com.ivy.freport.writer.xls;
public class ShopPeriod {

        public long id;
        public String shopCode;
        public String shopName;
        public String startDt;
        public String endDt;
        public double fee;
        public String status;
        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }
        public String getShopCode() {
            return shopCode;
        }
        public void setShopCode(String shopCode) {
            this.shopCode = shopCode;
        }
        public String getShopName() {
            return shopName;
        }
        public void setShopName(String shopName) {
            this.shopName = shopName;
        }
        public String getStartDt() {
            return startDt;
        }
        public void setStartDt(String startDt) {
            this.startDt = startDt;
        }
        public String getEndDt() {
            return endDt;
        }
        public void setEndDt(String endDt) {
            this.endDt = endDt;
        }
        public double getFee() {
            return fee;
        }
        public void setFee(double fee) {
            this.fee = fee;
        }
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
    }