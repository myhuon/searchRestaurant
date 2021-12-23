package ddwucom.mobile.searchrestaurant;

import java.io.Serializable;

public class AreaDTO implements Serializable { // intent 넘어 갈 때, dto 객체 넘기려면 Serializealbe implements 해줘야한다.
    // ex) intent.putExtra(TAG, dto)
    private long id;
    private String name;
    private String phone;
    private String address;
    private String latlng;
    private String websiteUri;
    private String memo;
    private String path;

    public long getId() { return id; }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getMemo(){ return memo; }
    public void setMemo(String memo){ this.memo = memo; }
    public String getPath(){ return path; }
    public void setPath(String path){ this.path = path; }
    public String getLatLng(){ return latlng; }
    public void setLatLng(String latlng){ this.latlng = latlng; }
    public String getWebsiteUri(){ return websiteUri; }
    public void setWebsiteUri(String websiteUri){ this.websiteUri = websiteUri; }

    @Override
    public String toString() {
        return id + ". " + address + " - " + name + " (" + phone + ")" + "memo : " + memo + "imagePath : " + path + "\n";
    }

}
