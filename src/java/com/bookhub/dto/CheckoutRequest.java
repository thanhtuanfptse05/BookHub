package com.bookhub.dto;

/**
 * Customer checkout form input (explicitly excludes price/total fields — calculated server-side only).
 */
public final class CheckoutRequest {

    private final String recipientName;
    private final String recipientPhone;
    private final String shippingAddress;
    private final String note;

    public CheckoutRequest(String recipientName, String recipientPhone, String shippingAddress, String note) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.shippingAddress = shippingAddress;
        this.note = note;
    }

    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public String getNote() { return note; }
}
