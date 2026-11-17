package models;

public class BookingCreatedResponse {

    private int bookingid;
    private BookingRequest booking;

    public BookingCreatedResponse(){

    }

    public BookingCreatedResponse(int bookingid, BookingRequest booking) {
        this.bookingid = bookingid;
        this.booking = booking;
    }

    public int getBookingid() {
        return bookingid;
    }

    public void setBookingid(int bookingid) {
        this.bookingid = bookingid;
    }

    public BookingRequest getBooking() {
        return booking;
    }

    public void setBooking(BookingRequest booking) {
        this.booking = booking;
    }

}
