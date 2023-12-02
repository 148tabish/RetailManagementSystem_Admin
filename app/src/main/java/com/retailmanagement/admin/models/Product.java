package com.retailmanagement.admin.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String productId, productName, productPrice, productDescription, productQuantity,
            productImage, productBarcode, productType, total;

    public Product(String productId, String productName
            , String productBarcode, String productPrice, String productType, String productDescription
            , String productImage, String productQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productDescription = productDescription;
        this.productQuantity = productQuantity;
        this.productImage = productImage;
        this.productBarcode = productBarcode;
        this.productType = productType;
    }
    public Product(String productId, String productName,String productType, String productQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.productType = productType;
    }
    //pid,pname,pimg,quantity,price,totprice
    public Product(String productId, String productName,String productImage, String productQuantity
            ,String productPrice, String total) {
        this.productId = productId;
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.productImage = productImage;
        this.productPrice = productPrice;
        this.total = total;
    }

    protected Product(Parcel in) {
        productId = in.readString();
        productName = in.readString();
        productPrice = in.readString();
        productDescription = in.readString();
        productQuantity = in.readString();
        productImage = in.readString();
        productBarcode = in.readString();
        productType = in.readString();
        total = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(productName);
        dest.writeString(productPrice);
        dest.writeString(productDescription);
        dest.writeString(productQuantity);
        dest.writeString(productImage);
        dest.writeString(productBarcode);
        dest.writeString(productType);
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
