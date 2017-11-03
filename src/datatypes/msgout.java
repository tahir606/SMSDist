
package datatypes;

public class msgout {
    
    private String id;
    private String sender;
    private String reciever;
    private String msg;
    private String operator;
    private String flag;

    public msgout(){
        
    }
    
    public msgout(String id, String sender, String reciever, String msg, String operator, String flag) {
        this.id = id;
        this.sender = sender;
        this.reciever = reciever;
        this.msg = msg;
        this.operator = operator;
        this.flag = flag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
    
    
          
    
}
