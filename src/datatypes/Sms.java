package datatypes;

public class Sms {

    private String index, senderPhone, timeStamp, body;   

    public Sms() {
    }

    public Sms(String index, String senderPhone, String timeStamp, String body) {
        this.index = index;
        this.senderPhone = senderPhone;
        this.timeStamp = timeStamp;
        this.body = body;
    }

    @Override
    public String toString() {
        return "Sms{" + "index=" + index + ", senderPhone=" + senderPhone + ", timeStamp=" + timeStamp + ", body=" + body + '}';
    }    

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
